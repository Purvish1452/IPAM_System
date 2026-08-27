package com.motadata.traceorg;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TraceOrgDatabaseBackupRestore
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgDatabaseBackupRestore.class, "Database Restore Utility");

    private static final String DB_HOST = TraceOrgConfigUtil.getDatabaseHost();

    private static final int DB_PORT = TraceOrgConfigUtil.getDatabasePort();

    public static void main(String[] args)
    {
        try
        {
            if(startMariaDbService())
            {
                _logger.info("Starting database restore...");

                StringBuilder command = new StringBuilder();

                command.append("[IO.File]::WriteAllBytes(")
                        .append(TraceOrgCommonConstants.SINGLE_QUOTE).append(TraceOrgCommonConstants.BACKUP_DIR)
                        .append(TraceOrgCommonConstants.PATH_SEPARATOR).append("restore_database.sql")
                        .append(TraceOrgCommonConstants.SINGLE_QUOTE).append(",")
                        .append("[Convert]::FromBase64String([IO.File]::ReadAllText(").append(TraceOrgCommonConstants.SINGLE_QUOTE)
                        .append(args[0]).append(TraceOrgCommonConstants.SINGLE_QUOTE).append(")))");

                String powerShellCommand = TraceOrgCommonConstants.POWERSHELL.replace(TraceOrgCommonConstants.COMMAND,command.toString());

                _logger.info("Executing command: " + powerShellCommand);

                Process process = Runtime.getRuntime().exec(powerShellCommand);

                boolean exitValue = process.waitFor(5, TimeUnit.MINUTES);

                if (exitValue)
                {
                    command.setLength(0);

                    command.append("Get-Content -Path ").append(TraceOrgCommonConstants.SINGLE_QUOTE).append(TraceOrgCommonConstants.BACKUP_DIR)
                            .append(TraceOrgCommonConstants.PATH_SEPARATOR).append("restore_database.sql").append(TraceOrgCommonConstants.SINGLE_QUOTE).append(" -Raw | Set-Content -Path ")
                            .append(TraceOrgCommonConstants.SINGLE_QUOTE).append(TraceOrgCommonConstants.BACKUP_DIR).append(TraceOrgCommonConstants.PATH_SEPARATOR)
                            .append("restore_database_utf8.sql").append(TraceOrgCommonConstants.SINGLE_QUOTE).append("  -Encoding UTF8");

                    powerShellCommand = TraceOrgCommonConstants.POWERSHELL.replace(TraceOrgCommonConstants.COMMAND,command.toString());

                    _logger.info("Executing command: " + powerShellCommand);

                    process = Runtime.getRuntime().exec(powerShellCommand);

                    exitValue = process.waitFor(5, TimeUnit.MINUTES);

                    if(exitValue)
                    {
                        _logger.info("SQL file converted to UTF-8 successfully!");

                        String[] commands = {
                                TraceOrgCommonConstants.MYSQL_DIR + "mysql",
                                "-u", "root",
                                "--password=" + TraceOrgCommonUtil.decrypt("ba03YfDjVoJ3NELSbea67w=="),
                                "ipam"
                        };

                        File inputFile = new File(TraceOrgCommonConstants.BACKUP_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "restore_database_utf8.sql");

                        ProcessBuilder processBuilder = new ProcessBuilder(commands);

                        processBuilder.redirectInput(inputFile);

                        process = processBuilder.start();

                        exitValue = process.waitFor(5, TimeUnit.MINUTES);

                        if(exitValue)
                        {
                            truncateTokenTables();

                            _logger.info("The database restore completed successfully.");
                        }
                        else
                        {
                            _logger.fatal("The SQL restore exited due to a timeout.");
                        }
                    }
                }
                else
                {
                    _logger.fatal("The restore process exited due to a timeout.");
                }
            }

        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        finally
        {
            File file = new File(TraceOrgCommonConstants.BACKUP_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "restore_database.sql");

            if(file.exists())
            {
                file.delete();
            }

            file = new File(TraceOrgCommonConstants.BACKUP_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "restore_database_utf8.sql");

            if(file.exists())
            {
                file.delete();
            }

            shutdownMariaDBProcess();
        }
    }

    public static void truncateTokenTables()
    {
        String url = "jdbc:mysql://" + TraceOrgConfigUtil.getDatabaseHost() + ":" + TraceOrgConfigUtil.getDatabasePort() + "/ipam?max-connections=1000&createDatabaseIfNotExist=true&useSSL=false&autoReconnect=true";

        String user = "root";

        String password = TraceOrgCommonUtil.decrypt("ba03YfDjVoJ3NELSbea67w==");

        try(Connection connection = DriverManager.getConnection(url, user, password);Statement statement = connection.createStatement();)
        {
            statement.execute("TRUNCATE TABLE oauth_access_token");

            statement.execute("TRUNCATE TABLE oauth_refresh_token");

            _logger.info("Truncation of token tables completed successfully.");
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }
    private static boolean startMariaDbService()
    {
        boolean result = false;

        try
        {
            if(!TraceOrgCommonUtil.isPortReachable(DB_HOST,DB_PORT))
            {
                _logger.info("Starting MariaDB service on " + DB_HOST + ":" + DB_PORT);

                Process process = startDatabaseProcess();

                if(process != null)
                {
                    result = true;
                }
            }
        }
        catch (Exception exception)
        {
            _logger.fatal("Failed to start mariadb service...");

            _logger.error(exception);
        }
        return result;
    }

    private static Process startDatabaseProcess()
    {
        Process process = null;

        try
        {
            List<String> arguments = new ArrayList<>();

            if(TraceOrgCommonConstants.OS_NAME.equals("Windows 95"))
            {
                arguments.add("command.com");
            }
            else
            {
                arguments.add("cmd.exe");
            }

            arguments.add("/C");

            arguments.add("mysqld.exe");

            ProcessBuilder processBuilder = new ProcessBuilder(arguments);

            processBuilder.directory(new File(TraceOrgCommonConstants.MYSQL_DIR));

            CountDownLatch waitHandler = new CountDownLatch(1);

            TraceOrgIPAMProcessHelper traceOrgIPAMProcessHelper = new TraceOrgIPAMProcessHelper(processBuilder,waitHandler);

            new Thread(traceOrgIPAMProcessHelper).start();

            waitHandler.await();

            process = traceOrgIPAMProcessHelper.getProcess();

            _logger.info("MariaDB service started on "+ DB_HOST + ":" + DB_PORT);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return process;
    }

    private static Process shutdownMariaDBProcess()
    {
        Process process = null;

        try
        {
            List<String> arguments = new ArrayList<>();

            if(TraceOrgCommonConstants.OS_NAME.equals("Windows 95"))
            {
                arguments.add("command.com");
            }
            else
            {
                arguments.add("cmd.exe");
            }

            arguments.add("/C");

            arguments.add("mysqladmin -u root shutdown -pMind@123");

            ProcessBuilder processBuilder = new ProcessBuilder(arguments);

            processBuilder.directory(new File(TraceOrgCommonConstants.MYSQL_DIR));

            CountDownLatch waitHandler = new CountDownLatch(1);

            TraceOrgIPAMProcessHelper traceOrgIPAMProcessHelper = new TraceOrgIPAMProcessHelper(processBuilder,waitHandler);

            new Thread(traceOrgIPAMProcessHelper).start();

            waitHandler.await();

            process = traceOrgIPAMProcessHelper.getProcess();

            _logger.info("MariaDB service stopped.....");
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return process;
    }

}
