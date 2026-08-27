package com.motadata.traceorg.ipam.logger;

import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Hardik.
 */

public class TraceOrgLogger
{
    private static final String LOG_DIRECTORY = "log";
    private static final String INFO_FILE = "$$$-IPAM-INFO.log";
    private static final String FATAL_FILE = "$$$-IPAM-FATAL.log";
    private static final String WARNING_FILE = "$$$-IPAM-WARNING.log";
    private static final String TRACE_FILE = "$$$-IPAM-TRACE.log";
    private static final String DEBUG_FILE = "$$$-IPAM-DEBUG.log";
    private static final String ERROR_FILE = "$$$-IPAM-ERROR.log";
    private static final String INVALID_QUERY_FILE = "$$$-IPAM-INVALID_QUERY.log";
    private final String m_class;
    private final String m_component;

    private static final File currentFile = new File(TraceOrgCommonConstants.CURRENT_DIR);

    private static final File parentFile = new File(currentFile.getParent());

    private static final String parentFilePath = parentFile.getParent();

    public TraceOrgLogger(Class clazz, String component) {
        this.m_class = clazz.getName();
        this.m_component = component;
    }

    public void error(Exception exception) {
        try {
            if(TraceOrgCommonUtil.errorEnabled()) {
                String logFile = parentFilePath + TraceOrgCommonConstants.PATH_SEPARATOR + LOG_DIRECTORY;
                File logDir = new File(logFile);
                if(!logDir.exists()) {
                    logDir.mkdir();
                }

                Date date = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
                String currentDate = simpleDateFormat.format(date);
                logFile = logFile + TraceOrgCommonConstants.PATH_SEPARATOR + ERROR_FILE;
                logFile = logFile.replace("$$$", currentDate);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss a");
                String currentTime = simpleTimeFormat.format(date);
                bufferedWriter.write(currentDate + " " + currentTime + " ERROR [" + this.m_component + "][" + this.m_class + "]:" + exception.getMessage() + TraceOrgCommonConstants.NEW_LINE + this.getStackTrace(exception.getStackTrace()) + TraceOrgCommonConstants.NEW_LINE);
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        } catch (Exception var10) {
            ;
        }

    }

    private String getStackTrace(StackTraceElement[] stackTraceElements) {
        StringBuilder stackTrace = new StringBuilder();

        try {
            if(stackTraceElements != null) {
                StackTraceElement[] var3 = stackTraceElements;
                int var4 = stackTraceElements.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    StackTraceElement stackTraceElement = var3[var5];
                    stackTrace.append("\tat ").append(stackTraceElement).append(TraceOrgCommonConstants.NEW_LINE);
                }
            }
        } catch (Exception var7) {
            ;
        }

        return stackTrace.toString();
    }

    public void info(Object message) {
        try {
            if(TraceOrgCommonUtil.infoEnabled()) {
                String logFile = parentFilePath + TraceOrgCommonConstants.PATH_SEPARATOR + LOG_DIRECTORY;
                File logDir = new File(logFile);
                if(!logDir.exists()) {
                    logDir.mkdir();
                }

                Date date = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
                String currentDate = simpleDateFormat.format(date);
                logFile = logFile + TraceOrgCommonConstants.PATH_SEPARATOR + INFO_FILE;
                logFile = logFile.replace("$$$", currentDate);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss a");
                String currentTime = simpleTimeFormat.format(date);
                bufferedWriter.write(currentDate + " " + currentTime + " INFO " + "[" + this.m_component + "]:" + message + TraceOrgCommonConstants.NEW_LINE);
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        } catch (Exception var10) {
            ;
        }

    }

    public void warn(Object message) {
        try {
            if(TraceOrgCommonUtil.warningEnabled()) {
                String logFile = parentFilePath + TraceOrgCommonConstants.PATH_SEPARATOR + LOG_DIRECTORY;
                File logDir = new File(logFile);
                if(!logDir.exists()) {
                    logDir.mkdir();
                }

                Date date = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
                String currentDate = simpleDateFormat.format(date);
                logFile = logFile + TraceOrgCommonConstants.PATH_SEPARATOR + WARNING_FILE;
                logFile = logFile.replace("$$$", currentDate);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss a");
                String currentTime = simpleTimeFormat.format(date);
                bufferedWriter.write(currentDate + " " + currentTime + " WARN " + "[" + this.m_component + "]:" + message + TraceOrgCommonConstants.NEW_LINE);
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        } catch (Exception var10) {
            ;
        }

    }

    public void debug(Object message) {
        try {
            if(TraceOrgCommonUtil.debugEnabled()) {
                String logFile = parentFilePath + TraceOrgCommonConstants.PATH_SEPARATOR + LOG_DIRECTORY;
                File logDir = new File(logFile);
                if(!logDir.exists()) {
                    logDir.mkdir();
                }

                Date date = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
                String currentDate = simpleDateFormat.format(date);
                logFile = logFile + TraceOrgCommonConstants.PATH_SEPARATOR + DEBUG_FILE;
                logFile = logFile.replace("$$$", currentDate);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss a");
                String currentTime = simpleTimeFormat.format(date);
                bufferedWriter.write(currentDate + " " + currentTime + " DEBUG " + "[" + this.m_component + "]:" + message + TraceOrgCommonConstants.NEW_LINE);
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        } catch (Exception var10) {
            ;
        }

    }

    public void fatal(Object message) {
        try {
            String logFile = parentFilePath + TraceOrgCommonConstants.PATH_SEPARATOR + LOG_DIRECTORY;
            File logDir = new File(logFile);
            if(!logDir.exists()) {
                logDir.mkdir();
            }

            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
            String currentDate = simpleDateFormat.format(date);
            logFile = logFile + TraceOrgCommonConstants.PATH_SEPARATOR + FATAL_FILE;
            logFile = logFile.replace("$$$", currentDate);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss a");
            String currentTime = simpleTimeFormat.format(date);
            bufferedWriter.write(currentDate + " " + currentTime + " FATAL " + "[" + this.m_component + "]:" + message + TraceOrgCommonConstants.NEW_LINE);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception var10) {
            ;
        }

    }

    public void trace(Object message) {
        try {
            if(TraceOrgCommonUtil.traceEnabled()) {
                String logFile = parentFilePath + TraceOrgCommonConstants.PATH_SEPARATOR + LOG_DIRECTORY;
                File logDir = new File(logFile);
                if(!logDir.exists()) {
                    logDir.mkdir();
                }

                Date date = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
                String currentDate = simpleDateFormat.format(date);
                logFile = logFile + TraceOrgCommonConstants.PATH_SEPARATOR + TRACE_FILE;
                logFile = logFile.replace("$$$", currentDate);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss a");
                String currentTime = simpleTimeFormat.format(date);
                bufferedWriter.write(currentDate + " " + currentTime + " TRACE " + "[" + this.m_component + "]:" + message + TraceOrgCommonConstants.NEW_LINE);
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        } catch (Exception var10) {
            ;
        }

    }

    public void invalidQuery(Object message) {
        try {
            if(TraceOrgCommonUtil.infoEnabled()) {
                String logFile = parentFilePath + TraceOrgCommonConstants.PATH_SEPARATOR + LOG_DIRECTORY;
                File logDir = new File(logFile);
                if(!logDir.exists()) {
                    logDir.mkdir();
                }

                Date date = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
                String currentDate = simpleDateFormat.format(date);
                logFile = logFile + TraceOrgCommonConstants.PATH_SEPARATOR + INVALID_QUERY_FILE;
                logFile = logFile.replace("$$$", currentDate);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss a");
                String currentTime = simpleTimeFormat.format(date);
                bufferedWriter.write(currentDate + " " + currentTime + " QUERY " + "[" + this.m_component + "]:" + message + TraceOrgCommonConstants.NEW_LINE);
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        } catch (Exception var10) {
            ;
        }

    }
}
