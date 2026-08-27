package com.motadata.traceorg.ipam.util;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public interface TraceOrgCommonConstants
{
    String WHITE_LABEL = "Motadata";

    String CURRENT_DIR = System.getProperty("user.dir");

    String CONFIG_DIR = "config";

    String IPM_CONF = "ipm-conf.yml";

    String PATH_SEPARATOR = System.getProperty("file.separator");

    String LINK_SEPARATOR = "~";

    String NEW_LINE = System.lineSeparator();

    String OS_NAME = System.getProperty("os.name");

    SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    String CLIENT_KEY = "motadata_client";

    String SECRET_KEY = "motadata_secret";

    String PLUGIN_ID = "plugin-id";

    String ERROR_CODE = "error-code";

    String ERROR_PING_FAILED = "Ping Failed [%s]";

    String ERROR_SERVICE_DOWN = "Port [%s] is not available on [%s]";

    String ERROR_AUTH = "Invalid Credentials [%s]";

    String RESULT = "result";

    String PLUGIN_CONTEXT = "plugin-context";

    String ARP_QUERY = "arp -a";

    String POWERSHELL_COMMAND = "powershell -Command \"& @@command@@\"";

    String POWERSHELL = "powershell -Command \"@@command@@\"";

    String COMMAND = "@@command@@";

    String BACKUP_DB = "BackupDB";

    String RUN_DATABASE_BACKUP = "/runDatabaseBackup/";

    Pattern IPV4_PATTERN = Pattern.compile("(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}");

    String PING_RETRY_COUNT = TraceOrgConfigUtil.loadConfigFile(TraceOrgCommonConstants.IPM_CONF).get("max-ping-check-retry-count");

    String PING_TIMEOUT = TraceOrgConfigUtil.loadConfigFile(TraceOrgCommonConstants.IPM_CONF).get("max-ping-check-timeout");

    int MAX_CONCURRENT_PING = TraceOrgCommonUtil.getMaxConcurrentPing();

    int MAX_ALERT_WORKER = TraceOrgCommonUtil.getMaxAlertWorker();

    int ACTIVEMQ_CONNECTION_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    int PROCESS_REQUEST_TIMEOUT = TraceOrgCommonUtil.getProcessRequestTimeout();

    String IPV6_NETSH_COMMAND = "netsh interface ipv6 show neighbors";

    String ALGORITHM = "AES";

    byte[] ENCRYPT_DECRYPT_KEY = new byte[]{'T', 'R', 'A', 'C', 'E', 'O', 'R', 'G', 'M', 'O', 'T', 'A', 'D', 'A', 'T', 'A'};

    String IPAM_DIR = TraceOrgCommonUtil.getIPAMPath();

    String BACKUP_DIR = TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "backup";

    String MYSQL_DIR =  TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "db" + TraceOrgCommonConstants.PATH_SEPARATOR + "bin" + TraceOrgCommonConstants.PATH_SEPARATOR;

    String SINGLE_QUOTE = "'";

    SimpleDateFormat SIMPLE_DATE_FORMAT_UNDERSCORE = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    String WINDOWS_PING_QUERY = "ping -n "+ TraceOrgCommonConstants.PING_RETRY_COUNT +" -w "+TraceOrgCommonConstants.PING_TIMEOUT +" ";

    String LINUX_PING_QUERY = "ping -c "+ TraceOrgCommonConstants.PING_RETRY_COUNT +" -w "+TraceOrgCommonConstants.PING_TIMEOUT + " ";

    String USER_NAME = "userName";

    String HOST_ADDRESS = "hostAddress";

    String CREDENTIAL_NAME = "credentialName";

    String ID = "id";

    String PAGE = "page";

    String ROLE_ADMIN = "ROLE_ADMIN";

    String AUTHORIZATION_CODE = "authorization_code";

    String PASSWORD = "password";

    String READ = "read";

    String WRITE = "write";

    String LOGIN = "login";

    String URL = "url";

    String LOGIN_URL = "/login";

    String AUTHORIZE_URL = "/oauth/authorize";

	String HOME_URL = "/loadHomePage";

    String HOME_PAGE = "WEB-INF/home";

    String LOGOUT_URL = "/logout.html";

    String USER = "User";

    String TRACE_ORG_GLOBAL_SETTING = "TraceOrgGlobalSetting";

    String ROLE_ = "ROLE_";

    String TRACE_ORG_SUBNET_DETAILS = "TraceOrgSubnetDetails";

    String TRACE_ORG_REPORT_SCHEDULER ="TraceOrgReportScheduler";

    String SUBNET_IP_DETAILS_BY_STATUS = "TraceOrgSubnetIpDetails where status='statusValue'";

    String SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID = "TraceOrgSubnetIpDetails where status='statusValue' and subnetId = 'subnetIdValue' and deactiveStatus = false";

    String STATUS_VALUE = "statusValue";

    String TRACE_ORG_SUBNET_IP_DETAILS = "TraceOrgSubnetIpDetails";

    String TRACE_ORG_IP_CHANGE_LOG = "TraceOrgIPChangeLog";

    String TRACE_ORG_DHCP_UTILIZATION = "TraceOrgDhcpUtilization";

    String TRACE_ORG_USERROLE = "TraceOrgUserRole";

    String REDIRECT = "redirect:";

    String ACCESSTOKEN = "accessToken";

    String AUTHORITIES = "authorities";

    String SCAN_SUBNET = "ScanSubnet";

    String SCAN_DHCP = "ScanDhcp";

    String ACTIVE = "active";

    boolean TRUE = true;

    boolean FALSE = false;

    String STRING_FALSE = "false";

    String USER_REST_URL = "/user/";

    String GLOBAL_SETTING_REST_URL = "/globalSetting/";

    String USER_ROLE_REST_URL = "/userRole/";

    String IP_REQUESTS_REST_URL = "/ipRequests/";

    String CUSTOM_COLUMN_REST_URL = "/customColumn/";

    String USER_BY_USERNAME = "User where userName='userNameValue'";

    String USER_NAME_VALUE = "userNameValue";

    String SUBNET_BY_SUBNET_ADDRESS = "TraceOrgSubnetDetails where subnetAddress='subnetAddressValue'";

    String SUBNET_ADDRESS_VALUE = "subnetAddressValue";

    String SUBNET_REST_URL = "/subnet/";

    String STATUS_SCAN_GATEWAY = "/statusScanGateway/";

    String SUBNET_SCAN_STATUS = "/statusScanSubnet/";

    String SUBNET_IMPORT_STATUS = "/importSubnetStatus/";

    String REPORT_SCHEDULER_REST_URL = "/reportScheduler/";

    String DHCP_SUBNET_REST_URL = "/dhcpSubnet/";

    String NORMAL_SUBNET_REST_URL = "/normalSubnet/";

    String TOP_10_SUBNET_REST_URL = "/top10SubnetUtilization/";

    String TOP_10_CATEGORY_REST_URL = "/top10CategoryUtilization/";

    String RECENT_DISCOVERED_REST_API = "/recentDiscovered/";

    String DNS_STATUS_SUMMARY = "/dnsStatusSummary/";

    String FORWARD_DNS_FAILED = "Forward DNS Lookup Failed";

    String REVERSE_DNS_FAILED = "Reverse DNS Lookup Failed";

    String FORWARD_DNS_IP_MISMATCH = "Forward DNS Lookup Returned Different IP";

    String EXPORT_CSV_SUBNET_REST_URL = "/exportCsvSubnet/";

    String EXPORT_PDF_SUBNET_REST_URL = "/exportPdfSubnet/";

    String EXPORT_PDF_NORMAL_SUBNET_REST_URL = "/exportNormalPdfSubnet/";

    String EXPORT_PDF_DHCP_SUBNET_REST_URL = "/exportDhcpPdfSubnet/";

    String EXPORT_EVENT_PDF_REST_URL = "/exportEventPdf/";

    String EXPORT_EVENT_CSV_REST_URL = "/exportEventCsv/";

    String EXPORT_EVENT_BY_DATE_PDF_REST_URL = "/exportEventPdfByDate/";

    String EVENT_BY_DATE_REST_URL = "/eventsByDate/";

    String SUBNET_CSV_REST_URL = "/subnetByCSV/";

    String IP_SUMMARY_REST_URL = "/ipSummary/";

    String PING_IP_SUMMARY_REST_URL = "/pingIpSummary/";

    String SUBNET_SCAN_REST_URL = "/scanSubnet/";

    String DHCP_SCAN_REST_URL = "/scanDhcp/";

    String ROGUE_SUBNET_IP_REST_URL = "/rogueSubnetIp/";

    String SUBNET_IP_REST_URL = "/subnetIp/";

    String CHANGE_LOG_URL = "/changeLog/";

    String CONFLICT_SUBNET_IP_REST_URL = "/conflictSubnetIp/";

    String EXPORT_CSV_SUBNET_IP_REST_URL = "/exportCsvSubnetIp/";

    String EXPORT_PDF_SUBNET_IP_REST_URL = "/exportPdfSubnetIp/";

    String EXPORT_PDF_SUBNET_CONFLICT_IP_REST_URL = "/exportPdfSubnetConflictIp/";

    String EXPORT_PDF_RECENT_DISCOVERY_REST_URL = "/exportPdfRecentlyDiscovered/";

    String EXPORT_PDF_TOP_10_CATEGORY_UTILIZATION_REST_URL = "/exportPdfTop10CategoryUtilization/";

    String EXPORT_PDF_TOP_10_SUBNET_UTILIZATION_REST_URL = "/exportPdfTop10SubnetUtilization/";

    String SUBNET_IP_BY_SUBNET_REST_URL = "/subnetIpBySubnet/";

    String USED_SUBNET_IP_BY_SUBNET_REST_URL = "/usedSubnetIpBySubnet/";

    String AVAILABLE_SUBNET_IP_BY_SUBNET_REST_URL = "/availableSubnetIpBySubnet/";

    String RESERVED_SUBNET_IP_BY_SUBNET_REST_URL = "/reservedSubnetIpBySubnet/";

    String SUBNET_AVAILABLE_IP_REST_URL = "/availableSubnetIp/";

    String SUBNET_RESERVED_IP_REST_URL = "/reservedSubnetIp/";

    String SUBNET_TRANSIENT_IP_REST_URL = "/transientSubnetIp/";

    String SUBNET_USED_IP_REST_URL = "/usedSubnetIp/";

    String SUBNET_CHECK_REST_URL = "/checkSubnet/";

    String BRAND_REST_URL = "/brand/";

    String ROGUE_DETECTION_URL = "/rogueDetection/";

    String ROGUE_DETECTION_MARKED_AUTHENTICITY = "/rogueDetectionMarkedAuthenticity/";

    String ROGUE_DETECTION_TRUSTED_MAC_ADDRESS_BY_CSV = "/rogueDetectionTrustedMACAddressByCSV/";

    String ROGUE_DETECTION_EXPORT_PDF = "/exportRogueDetectionPdf/";

    String ROGUE_DETECTION_EXPORT_CSV = "/exportRogueDetectionCSV/";

    short ALL_ROGUE_DETECTION_DETAILS_EXPORT = 0;

    short SELECTED_ROGUE_DETECTION_DETAILS_EXPORT = 1;

    String ALL_ROGUE_DETECTION_DETAILS = "allIndividualRogueDetection";

    String DISCOVERED_ROGUE_DETECTION_DETAILS = "discoveredIndividualRogueDetection";

    String TRUSTED_ROGUE_DETECTION_DETAILS = "trustedIndividualRogueDetection";

    String INDIVIDUAL_ROGUE_DETECTION_DETAILS = "rogueIndividualRogueDetection";

    String ROGUE_DETECTION_DETAILS = "rogueDetection";

    String EXPORT_PDF = "exportPdf";

    String EXPORT_CSV = "exportCsv";

    String CISCO = "cisco";

    String WINDOWS = "windows";

    String TRACE_ORG_BRAND = "TraceOrgBrand";

    String SUBNET_ADDRESS = "subnetAddress";

    String MAIL_REST_URL = "/mail/";

    String INSERT_MAIL_REST_URL = "/insertMail/";

    String DATABASE_MAINTENANCE = "/databaseMaintenance/";

    String DATABASE_BACKUP = "/databaseBackup/";

    String TRACE_ORG_MAIL_SERVER = "TraceOrgMailServer";

    String CATEGORY_REST_URL = "/category/";

    String TOP_EVENT_REST_URL = "/topEvent/";

    String EVENT_REST_URL = "/event/";

    String EVENT_SUMMARY_REST_URL = "/eventSummary/";

    String DHCP_CREDENTIAL_REST_URL = "/dhcpCredential/";

    String DHCP_UTILIZATION_REST_URL = "/dhcpUtilization/";

    String CISCO_DHCP_CREDENTIAL_REST_URL = "/ciscoDhcpCredential/";

    String WINDOWS_DHCP_CREDENTIAL_REST_URL = "/windowsDhcpCredential/";

    String CHECK_DHCP_CREDENTIAL_REST_URL = "/checkDhcpCredential/";

    String SUBNET_BY_CATEGORY = "/subnetByCategory/";

    String SUPERNET_BY_CATEGORY = "/supernetByCategory/";

    String REMOVE_SUPERNET = "/removeSupernet/";

    String ADD_SUPERNET = "/addSupernet/";

    String SUBNET_BY_REPORT = "/subnetByReport/";

    String SUBNET_IP_BY_REPORT_TIMELINE = "/subnetIpByReportTimeline/";

    String SUBNET_ROGUE_IP_BY_REPORT_TIMELINE = "/subnetIpRogueByReportTimeline/";

    String TRACE_ORG_CATEGORY = "TraceOrgCategory";

    String TRACE_ORG_GATEWAY = "TraceOrgGateway";

    String GATEWAY_REST_URL = "/gateway/";

    String GATEWAY_URL = "/gateways/";

    String DISCOVERED_SUBNET_URL = "/discoveredSubnet/";

    String SCAN_GATEWAY_URL = "/scanGateway/";

    String ROUTER_REST_URL = "/router/";

    String ALERT_CONFIGURE_REST_URL = "/configureAlert/";

    String ALERTS_REST_URL = "/alerts/";

    String TRACE_ORG_ALERT = "TraceOrgAlertConfigure";

    String TRACE_ORG_ALERT_STREAM = "TraceOrgAlertStream";

    String IP_UTILIZATION_ALERT_TYPE = "IP Utilization Exceeded";

    String IP_UTILIZATION_BELOW_ALERT_TYPE = "IP Utilization Below Limit";

    String ROGUE_DETECTION_ALERT_TYPE = "Rogue IP Detected";

    String ALERT_CLEAR = "clear";

    String TRACE_ORG_EVENT = "TraceOrgEvent";

    String TRACE_ORG_FORGOT_PASSWORD = "TraceOrgForgotPassword";

    String TRACE_ORG_DHCP_CREDENTIAL = "TraceOrgDhcpCredentialDetails";

    String CATEGORY_NAME = "categoryName";

    String MESSAGE = "message";

    String LOGO_PNG = "logo.png";

    String IP_ADDRESS = "ipAddress";

    String AVAILABLE = "Available";

    String TRANSIENT = "Transient";

    String RESERVED = "Reserved";

    String USED = "Used";

    String DISCOVERED = "Discovered";

    String ROGUE = "Rogue";

    String TRUSTED = "Trusted";

    String VENDOR_BY_MAC_ADDRESS = "TraceOrgVendor where vendorMac like '%vendorMacValue%'";

    String VENDOR_MAC_VALUE = "vendorMacValue";

    String SUBNET_IP_BY_SUBNET_ID = "TraceOrgSubnetIpDetails where deactiveStatus=false and subnetId=subnetIdValue order by INET_ATON(ipAddress)";

    String SELECTED_IP_SUBNET_ID = "TraceOrgSubnetIpDetails where deactiveStatus=false and subnetId=subnetIdValue and id in (subnetIdList)";

    String SUBNET_ID_LIST = "subnetIdList";

    String SUBNET_ID_VALUE = "subnetIdValue";

    String SUBNET_ID = "subnetId";

    String TOTAL_IP = "totalIp";

    String USED_IP = "usedIp";

    String AVAILABLE_IP = "availableIp";

    String AVAILABLE_IP_PERCENTAGE = "availableIpPercentage";

    String TRANSIENT_IP = "transientIp";

    String TRANSIENT_IP_PERCENTAGE = "transientIpPercentage";

    String USED_IP_PERCENTAGE = "usedIpPercentage";

    SimpleDateFormat VISUAL_DATE_FORMAT = new SimpleDateFormat("dd MMM, YYYY hh:mm:ss a");

    DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    short CHANGE_LOG_MONTH_LIMIT = 1;

    String CATEGORY_BY_NAME = "TraceOrgCategory where categoryName='categoryNameValue'";

    String CATEGORY_NAME_VALUE = "categoryNameValue";

    String SUBNET_DETAIL_CSV_NAME = "subnetDetails.csv";

    String SUBNET_DETAIL_CSV_PATH = "/csv/subnetDetails.csv";

    String SUBNET_IP_CSV_REST_URL = "/subnetIpByCSV/";

    String ACTIVE_SUBNET_IP_RANGE_REST_URL = "/activeSubnetIpRange/";

    String FORGOT_REST_URL = "/forgotPassword/";

    String NEW_PASSWORD_REST_URL = "/newPassword/";

    String VERIFY_PASSWORD_TOKEN_REST_URL = "/verifyPasswordToken/";

    String UPDATE_SUBNET_IP_RANGE_REST_URL = "/updateSubnetIpRange/";

    String DELETE_SUBNET_IP_RANGE_REST_URL = "/deleteSubnetIpRange/";

    String SUBNET_IP_DETAIL_CSV_NAME = "subnetIpDetails.csv";

    String TRUSTED_MAC_ADDRESS_CSV_NAME = "trustedMACAddressDetails.csv";

    String SUBNET_IP_DETAIL_CSV_PATH = "/csv/subnetIpDetails.csv";

    String TRUSTED_MAC_ADDRESS_CSV_PATH = "/csv/trustedMACAddressDetails.csv";

    String SUBNET_IP_DETAILS_BY_IP_ADDRESS= "TraceOrgSubnetIpDetails where ipAddress='ipAddressValue'";

    String IP_ADDRESS_VALUE = "ipAddressValue";

    String SELECT_VENDOR_WITH_COUNT = "Select count(*) as devicenumber,deviceType";

    String VENDOR_COUNT_BY_USED_IP = "TraceOrgSubnetIpDetails where deactiveStatus = false group by deviceType order by devicenumber desc";

    String SELECT_VENDOR_WITH_COUNT_FOR_REPORT = "Select count(*) as devicenumber,deviceType,(COUNT(*) / (SELECT COUNT(*) FROM TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in(subnetIdValue)))* 100 AS Percentage";

    String VENDOR_REST_URL = "/vendor/";

    String VENDOR_COUNT ="VendorCount";

    String VENDOR_NAME = "VendorName";

    String VENDOR_PERCENTAGE = "VendorPercentage";

    String BRAND_NAME = "brandName";

    String CSS_MODE = "cssMode";

    String SCOPE = "scope";

    String EXPIRES_IN = "expires_in";

    String REFRESH_TOKEN = "refresh_token";

    String TOKEN_TYPE = "token_type";

    String ACCESS_TOKEN = "access_token";

    String AUTHORIZATION= "Authorization";

    String BASIC = "Basic ";

    String SET_COOKIE = "Set-Cookie";

    String LOGIN_USER_URL = "/loginUser.html";

    String CHANGE_LOGIN_STATUS_URL = "/changeLoginStatus";

    String CHANGE_LOGOUT_STATUS_URL = "/changeLogoutStatus";

    String LEFT_SQUARE_BRACKET = "[";

    String RIGHT_SQUARE_BRACKET = "]";

    String ALL_IP_REPORT = "All IP";

    String USED_IP_REPORT = "Used IP";

    String RESERVED_IP_REPORT = "Reserved IP";

    String AVAILABLE_IP_REPORT = "Available IP";

    String ROGUE_IP_REPORT = "Rogue IP";

    String TRUSTED_IP_REPORT = "Trusted IP";

    String CONFLICT_IP_REPORT = "Conflict IP";

    String SUBNET_UTILIZATION_REPORT = "Subnet Utilization";

    String DHCP_UTILIZATION_REPORT = "DHCP Utilization";

    String VENDOR_SUMMARY_REPORT = "Vendor Summary";

    String TRANSIENT_IP_REPORT = "Transient IP";

    String EVENT_LOG_REPORT = "Event Log";

    Integer SCHEDULER_TIMELINE_DAILY = 0;

    Integer SCHEDULER_TIMELINE_WEEKLY = 1;

    Integer SCHEDULER_TIMELINE_MONTHLY = 2;

    String SCAN_TYPE = "scanType";

    short SUBNET_SCAN = 0;

    short DHCP_SCAN = 1;

    String TRACE_ORG_COMMON_UTIL = "traceOrgCommonUtil";

    String TRACE_ORG_SERVICE = "traceOrgService";
    String TRACE_ORG_ROGUE_DETECTION_REPOSITORY = "traceOrgRogueDetectionRepository";

    String TRACE_ORG_ALERT_SERVICE = "traceOrgAlertService";

    String TRACE_ORG_MESSAGE_SENDER = "TraceOrgMessageSender";

    String EXPORT_SUBNET_IP_BY_REPORT_TIMELINE = "/exportsubnetIpByReportTimeline/";

    String EXPORT_SUBNET_IP_CSV_BY_REPORT_TIMELINE = "/exportsubnetIpCsvByReportTimeline/";

    String SUBNET_CATEGORY_REST_URL = "/subnetCategory/";

    // Global Configuration

    String DOMAIN_NAME = TraceOrgConfigUtil.loadConfigFile(TraceOrgCommonConstants.IPM_CONF).get("server-host");

    String SERVER_PORT = TraceOrgConfigUtil.loadConfigFile(TraceOrgCommonConstants.IPM_CONF).get("server-port");

    //String PROTOCOL = TraceOrgConfigUtil.loadConfigFile(TraceOrgCommonConstants.IPM_CONF).get("server-protocol");

    String PROTOCOL = "http";

    String AUTH_SERVER_URL = TraceOrgCommonConstants.PROTOCOL+"://"+TraceOrgCommonConstants.DOMAIN_NAME + ":"+TraceOrgCommonConstants.SERVER_PORT;

    String AUTH_SERVER_TOKEN_URL = TraceOrgCommonConstants.PROTOCOL+"://"+TraceOrgCommonConstants.DOMAIN_NAME + ":"+TraceOrgCommonConstants.SERVER_PORT +"/oauth/token";

    String AVAILABILITY_AVAILABLE_PERCENTAGE = "Available (%)";

    String AVAILABILITY_USED_PERCENTAGE = "Used (%)";

    String AVAILABILITY_TRANSIENT_PERCENTAGE = "Transient (%)";

    int BATCH_SIZE = 66000;

    String LOGO_DIR = "src/main/webapp/images";

    String SYSTEM_USER = "System";

    String CHANGE_LOG_MESSAGE = "Status: @@previous_status@@ -> @@current-status@@";

    String PREVIOUS_STATUS = "@@previous_status@@";

    String CURRENT_STATUS = "@@current-status@@";

    String DISABLE = "disable";

    String ENABLE = "enable";

    Short DNS_TIME_OUT = 5; //Seconds

    Short DNS_CIRCUIT_BREAK_COUNT = 5;

    String EMPTY_STRING = "";

    Long NONE_GATEWAY_ID = -1L;

    int SCAN_TRUE = 1;

    int SCAN_FALSE = 0;

    int TRUE_VALUE = 1;

    int FALSE_VALUE = 0;

    String IP_UTILIZATION_ALERT_CLEAR_TITLE = "Subnet @SUBNET@ Utilization Alert Cleared for Exceeded Threshold";

    String IP_UTILIZATION_BELOW_ALERT_CLEAR_TITLE = "Subnet @SUBNET@ Utilization Alert Cleared for Below Threshold";

    String ROGUE_DETECTION_ALERT_CLEAR_TITLE = "Subnet @SUBNET@ Rogue IP Alert Cleared";

    String IP_UTILIZATION_ALERT_TITLE = "Subnet @SUBNET@ Utilization Exceeded Threshold";

    String IP_UTILIZATION_BELOW_ALERT_TITLE = "Subnet @SUBNET@ Utilization Dropped Below Threshold";

    String ROGUE_DETECTION_ALERT_TITLE = "Subnet @SUBNET@ Detected Rogue IP";

    String IP_UTILIZATION_ALERT_MESSAGE =  "Subnet @SUBNET@ utilization has reached @UTILIZATION@%, exceeding the threshold of @THRESHOLD@%.";

    String IP_UTILIZATION_BELOW_ALERT_MESSAGE = "Subnet @SUBNET@ utilization has dropped to @UTILIZATION@%, going below the threshold of @THRESHOLD@%.";

    String ROGUE_DETECTION_ALERT_MESSAGE =  "Subnet @SUBNET@ has following rogue ip detected, Rogue Ips :  @ROGUE_IP@.";

    String IP_UTILIZATION_MAIL_ALERT_MESSAGE =  "Subnet @SUBNET@ utilization has reached @UTILIZATION@%, exceeding the threshold of @THRESHOLD@% in the IP Address Manager.";

    String IP_UTILIZATION_BELOW_MAIL_ALERT_MESSAGE = "Subnet @SUBNET@ utilization has dropped to @UTILIZATION@%, going below the threshold of @THRESHOLD@% in the IP Address Manager.";

    String ROGUE_DETECTION_MAIL_ALERT_MESSAGE =  "Subnet @SUBNET@ has following rogue ip detected in the IP Address Manager,";

    String IP_UTILIZATION_ALERT_CLEAR_MAIL_MESSAGE =  "The alert is now cleared: Subnet @SUBNET@ utilization has dropped below the threshold of @THRESHOLD@% and is now at @UTILIZATION@% in the IP Address Manager.";

    String IP_UTILIZATION_ALERT_CLEAR_MESSAGE =  "Subnet @SUBNET@ utilization has dropped below the threshold of @THRESHOLD@% and is now at @UTILIZATION@%.";

    String IP_UTILIZATION_BELOW_ALERT_CLEAR_MAIL_MESSAGE = "The alert is now cleared: Subnet @SUBNET@ utilization has exceeded the threshold of @THRESHOLD@% and is now at @UTILIZATION@% in the IP Address Manager.";

    String IP_UTILIZATION_BELOW_ALERT_CLEAR_MESSAGE = "Subnet @SUBNET@ utilization has exceeded the threshold of @THRESHOLD@% and is now at @UTILIZATION@%.";

    String ROGUE_DETECTION_ALERT_CLEAR_MESSAGE =  "Subnet @SUBNET@ has no rogue ip detected in the IP Address Manager.";

    String SUBNET = "@SUBNET@";

    String UTILIZATION = "@UTILIZATION@";

    String THRESHOLD = "@THRESHOLD@";

    String ROGUE_IP = "@ROGUE_IP@";

    String SUCCESS = "success";

    String DATA = "data";

    long PRIMARY_MAIL_SERVER_ID = 1L;

    long SECONDARY_MAIL_SERVER_ID = 2L;

    String VALUE_SEPARATOR = "_|@#|_";

    String VALUE_SEPARATOR_WITH_ESCAPE = "_\\|@#\\|_";

    Long DEFAULT_GATEWAY_ID = 1L;

    String IS_AUTO_DISCOVERED = "is_auto_discovered";

    int CRITICAL_SEVERITY = 0;

    int WARNING_SEVERITY = 1;

    int TRANSIENT_SEVERITY = 2;

    String GATEWAY_SUCCESS_STATUS = "Success";

    String GATEWAY_FAILED_STATUS = "Failed";

    String GATEWAY_RUNNING_STATUS = "Running";

    int REMOTE_SUBNET_SCAN_TIMEOUT = 60;

    String WORK_TYPE = "workType";

    String WORK_CONTEXT = "workContext";

    String FORWARD_LOOKUP_FAILED = "forwardLookupFailed";

    String FORWARD_LOOKUP_MISMATCH = "forwardLookupMismatch";

    String IP_CONFLICT = "ipConflict";

    String IP_RESERVATION_CHANGE = "ipReservationChange";

    String IP_STATE_CHANGE = "ipStateChange";

    String IP_UTILIZATION = "ipUtilization";

    String IP_UTILIZATION_BELOW = "ipUtilizationBelow";

    String IP_UTILIZATION_BELOW_FLAG = "ipUtilizationBelowFlag";

    String IP_UTILIZATION_FLAG = "ipUtilizationFlag";

    String MAC_IP_CHANGE = "macIpChange";

    String MAC_IP_CHANGE_FLAG = "macIpChangeFlag";

    String NEW_SUBNETS_DISCOVERED = "newSubnetsDiscovered";

    String REVERSE_LOOKUP_FAILED = "reverseLookupFailed";

    String ROGUE_DETECTION = "rogueDetection";

    String ALERT_QUEUE = "alerts";

    String COMMA_SEPARATOR = ",";

    int MASK_8BIT = 0xFF;

    int MASK_32BIT = 0xFFFFFFFF;

    long MASK_32BIT_LONG = 0xFFFFFFFFL;

    String TRACE_ORG_SUPERNET_SERVICE = "traceOrgSupernetService";

    String MANUAL_DHCP_SCAN = "manualDHCPScan";

    String MANUAL_SUBNET_SCAN = "manualSubnetScan";
}
