package com.motadata.traceorg;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public interface TraceOrgCommonConstants
{
    String CURRENT_DIR = System.getProperty("user.dir");

    String PATH_SEPARATOR = System.getProperty("file.separator");

    String ALGORITHM = "AES";

    byte[] ENCRYPT_DECRYPT_KEY = new byte[]{'T', 'R', 'A', 'C', 'E', 'O', 'R', 'G', 'M', 'O', 'T', 'A', 'D', 'A', 'T', 'A'};

    String IPAM_DIR = TraceOrgCommonConstants.CURRENT_DIR;

    String OS_NAME = System.getProperty("os.name");

    String NEW_LINE = System.lineSeparator();

    String POWERSHELL_COMMAND = "powershell -Command \"& @@command@@\"";

    String POWERSHELL = "powershell -Command \"@@command@@\"";

    String COMMAND = "@@command@@";

    String BACKUP_DB = "BackupDB";

    String CONFIG_DIR = "config";

    String IPM_CONF = "ipm-conf.yml";

    String BACKUP_DIR = TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "backup";

    String MYSQL_DIR =  TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "db" + TraceOrgCommonConstants.PATH_SEPARATOR + "bin" + TraceOrgCommonConstants.PATH_SEPARATOR;

    String SINGLE_QUOTE = "'";

    String DOUBLE_QUOTE = "\"";
}
