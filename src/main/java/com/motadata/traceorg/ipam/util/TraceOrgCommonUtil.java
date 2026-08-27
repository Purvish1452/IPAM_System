package com.motadata.traceorg.ipam.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.motadata.traceorg.ipam.entity.discovery.TraceOrgFlags;
import com.motadata.traceorg.ipam.entity.settings.*;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.*;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpCredentialDetails;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpUtilization;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.login.TraceOrgDetail;
import com.motadata.traceorg.ipam.entity.report.TraceOrgReportScheduler;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.repository.discovery.TraceOrgFlagRepository;
import com.motadata.traceorg.ipam.repository.event.TraceOrgEventRepository;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgCustomColumnRepository;
import com.motadata.traceorg.ipam.scheduler.database.TraceOrgDatabaseBackup;
import com.motadata.traceorg.ipam.scheduler.dhcp.TraceOrgDhcpScanQueue;
import com.motadata.traceorg.ipam.scheduler.report.TraceOrgReportSchedulerJob;
import com.motadata.traceorg.ipam.scheduler.subnet.TraceOrgScanSubnetUpdateQueue;
import com.motadata.traceorg.ipam.scheduler.subnet.TraceOrgSubnetScheduleScanJob;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.alert.TraceOrgAlertService;
import com.motadata.traceorg.ipam.services.messaging.TraceOrgMessageSender;
import com.motadata.traceorg.ipam.services.supernet.TraceOrgSupernetService;
import de.siegmar.fastcsv.reader.CsvRow;
import de.siegmar.fastcsv.writer.CsvWriter;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.net.util.SubnetUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.multipart.MultipartFile;
import org.xbill.DNS.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.SocketTimeoutException;
import java.security.Key;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("ALL")
public class TraceOrgCommonUtil
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgCommonUtil.class, "GUI / Common Util");

    private static final JSONSerializer m_jsonSerializer = new JSONSerializer();

    private static final JSONDeserializer<HashMap<String, Object>> m_jsonDeserializer = new JSONDeserializer<>();

    private final static AtomicInteger m_logLevel = new AtomicInteger(2);

    public  static AtomicInteger m_scanStatus = new AtomicInteger(0);

    private static final AtomicInteger m_csvImportStatus = new AtomicInteger(0);

    private static final AtomicInteger m_ScanStatus = new AtomicInteger(0);

    @Autowired
    TraceOrgFlagRepository  traceOrgFlagRepository;

    @Autowired
    TraceOrgEventRepository traceOrgEventRepository;

    @Autowired
    TraceOrgMessageSender traceOrgMessageSender;

    @Autowired
    private TraceOrgSupernetService traceOrgSupernetService;

    @Autowired
    TraceOrgCustomColumnRepository traceOrgCustomColumnRepository;

    public TraceOrgCommonUtil() {}

    public TraceOrgCommonUtil(TraceOrgService traceOrgService)
    {
        this.traceOrgService = traceOrgService;
    }

    @Autowired
    private TokenStore tokenStore;

    public void setTraceOrgService(TraceOrgService traceOrgService) {
        this.traceOrgService = traceOrgService;
    }

    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    @Autowired
    private TraceOrgAlertService traceOrgAlertService;

    @Autowired
    private TraceOrgFactoryUtil traceOrgFactoryUtil;

    public static final String error = "Error";

    private AccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();

    public static ConcurrentHashMap<String,String> m_scanSubnet = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String,Object> m_scheduleScanSubnet = new ConcurrentHashMap<>();

    public static Scheduler quartzThread = null;

    public static final String SCHEDULE_SUBNET_SCAN_JOB_CRON_EXPRESSION = "0 * * ? * *";

    public static void initializeQuartzThread()
    {
        try
        {
            quartzThread = new StdSchedulerFactory().getScheduler();

            quartzThread.start();
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    public static void createTmpDirForReport() {
        File file = new File(new File(TraceOrgCommonConstants.CURRENT_DIR).getParent() + TraceOrgCommonConstants.PATH_SEPARATOR + "temp");

        if (file.exists() == false) {
            boolean isFileCreated = file.mkdir();
            if (isFileCreated == false) {
                _logger.error(new FileNotFoundException(("Required folders for report download not created.")));
            }
        }
    }

    public String generateUUID()
    {
        SecureRandom random = new SecureRandom();

        BigInteger bigInteger = new BigInteger(30, random);

        return bigInteger.toString(32);

    }

    public String getToken(HttpServletRequest request)
    {
        String token = null;

        if (request.getHeader("Cookie") != null && !request.getHeader("Cookie").isEmpty())
        {

            Cookie[] cookies = request.getCookies();

            if (cookies != null)
            {
                for (Cookie cookie : cookies)
                {
                    if (cookie.getName().equals("token"))
                    {
                        token = cookie.getValue();
                    }
                }
            }
        }
        if(checkToken(token))
        {
            return token;
        }
        else
        {
            return null;
        }
    }

    public static String getAccessToken()
    {
        try
        {
            OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();

            String json = new Gson().toJson(oAuth2Authentication.getDetails());

            Type listType = new TypeToken<TraceOrgDetail>() {}.getType();

            TraceOrgDetail TraceOrgDetail = new Gson().fromJson(json, listType);

            return TraceOrgDetail.getTokenValue();

        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return null;
    }

    public String getUserName(HttpServletRequest request)
    {
        String userName = null;

        try
        {
            if (request.getHeader("Cookie") != null && !request.getHeader("Cookie").isEmpty())
            {
                Cookie[] cookies = request.getCookies();

                if (cookies != null)
                {
                    for (Cookie cookie : cookies)
                    {
                        if (cookie.getName().equals("userName"))
                        {
                            userName = cookie.getValue();
                        }
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return userName;
    }

    public static String getUserName()
    {
        OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();

        return oAuth2Authentication.getName();
    }

    @SuppressWarnings("unchecked")
    public List<String> getAuthorityList(TraceOrgService service, String userName)
    {
        List<String> authorityList = new ArrayList<>();

        try
        {
            if (userName != null)
            {
                List<TraceOrgUser> traceOrgUserList = (List<TraceOrgUser>) service.commonQuery("", "TraceOrgUser where userName='" + userName+ "' and status=true");

                if (traceOrgUserList != null && traceOrgUserList.size() > 0)
                {
                    for (TraceOrgUser traceOrgUser : traceOrgUserList)
                    {
                        List<TraceOrgUserRole> traceOrgUserRoleList =(List<TraceOrgUserRole>)service.commonQuery("","TraceOrgUserRole where id='"+ traceOrgUser.getUserRoleId().getId()+"'");

                        if(traceOrgUserRoleList != null && !traceOrgUserRoleList.isEmpty())
                        {
                            for (TraceOrgUserRole traceOrgUserRole : traceOrgUserRoleList)
                            {
                                authorityList.add("ROLE_"+ traceOrgUserRole.getRole().toUpperCase());
                            }
                        }
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return authorityList;
    }

    @SuppressWarnings("unchecked")
    public boolean checkToken(String value)
    {
        boolean tokenStatus = Boolean.FALSE;

        Map<String, Object> response = new HashMap<>();

        try
        {
            if(value != null)
            {
                OAuth2AccessToken token = tokenStore.readAccessToken(value);

                if (token == null)
                {
                    response.put("active", false);
                }
                else if (token.isExpired())
                {
                    response.put("active", false);
                }
                else
                {
                    OAuth2Authentication authentication = tokenStore.readAuthentication(token.getValue());

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    response = (Map<String, Object>)accessTokenConverter.convertAccessToken(token, authentication);

                    response.put("authorities",getAuthorityList(traceOrgService,String.valueOf(response.get("user_name"))));

                    response.put("active", true);

                    tokenStatus = Boolean.TRUE;
                }
            }
            else
            {
                _logger.warn("Token is null..");
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return tokenStatus;
    }

    @SuppressWarnings("unchecked")
    public String currentUserRole(String accessToken)
    {
        Map<String, Object> response = new HashMap<>();

        String role = null;

        try
        {
            OAuth2AccessToken token = tokenStore.readAccessToken(accessToken);

            if(token!=null)
            {
                OAuth2Authentication authentication = tokenStore.readAuthentication(token.getValue());

                response = (Map<String, Object>)accessTokenConverter.convertAccessToken(token, authentication);

                if(getAuthorityList(traceOrgService,String.valueOf(response.get("user_name")))!=null && !getAuthorityList(traceOrgService,String.valueOf(response.get("user_name"))).isEmpty())
                {
                    role = getAuthorityList(traceOrgService,String.valueOf(response.get("user_name"))).get(0);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return role;
    }


    public String currentUserName(String accessToken)
    {
        Map<String, Object> response;

        String userName = null;

        try
        {
            OAuth2AccessToken token = tokenStore.readAccessToken(accessToken);

            if (token == null)
            {
                _logger.warn("Token Invalid");
            }
            else if (token.isExpired())
            {
                _logger.warn("Token Expired");
            }
            else
            {
                OAuth2Authentication authentication = tokenStore.readAuthentication(token.getValue());

                response = (Map<String, Object>)accessTokenConverter.convertAccessToken(token, authentication);

                if(response.get("user_name") != null)
                {
                    userName = response.get("user_name").toString();
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return userName;
    }


    public TraceOrgUser currentUser(String accessToken)
    {
        Map<String, Object> response;

        TraceOrgUser traceOrgUser = null;

        try
        {
            OAuth2AccessToken token = tokenStore.readAccessToken(accessToken);

            if (token == null)
            {
                _logger.warn("Token Invalid");
            }
            else if (token.isExpired())
            {
                _logger.warn("Token Expired");
            }
            else
            {
                OAuth2Authentication authentication = tokenStore.readAuthentication(token.getValue());

                response = (Map<String, Object>)accessTokenConverter.convertAccessToken(token, authentication);

                if(response.get("user_name") != null)
                {
                    String userName = getStringValue(response.get("user_name"));

                    traceOrgUser = (TraceOrgUser)this.traceOrgService.findByUserName(userName);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return traceOrgUser;
    }

    /***
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Used the userId as the username instead of FromEmail..
     * */
    public Response testMailServer(TraceOrgMailServer traceOrgMailServer)
    {
        Response response = new Response();

        try
        {
            TraceOrgCommonUtil.sendMail(traceOrgMailServer.getMailHost(),
                    traceOrgMailServer.getMailPort(),
                    "IPAM Test Mail",
                    "Hello " + traceOrgMailServer.getMailUserName() + ", <br><br> <t>Test Message...<br><br> Thank You.",
                    traceOrgMailServer.getMailFromEmail(),
                    traceOrgMailServer.getMailToEmail(),
                    traceOrgMailServer.getMailProtocol(),
                    traceOrgMailServer.getMailUserId(),
                    traceOrgMailServer.getMailPassword(),
                    traceOrgMailServer.getMailTimeout());

            response.setSuccess(TraceOrgCommonConstants.TRUE);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);
        }
        return response;
    }


    //Hardik Vala

    public static boolean debugEnabled()
    {
        return 1 >= getLogLevel();
    }

    public static boolean warningEnabled()
    {
        return 3 >= getLogLevel();
    }

    public static boolean infoEnabled()
    {
        return 2 >= getLogLevel();
    }

    public static boolean errorEnabled()
    {
        return 4 >= getLogLevel();
    }

    public static boolean traceEnabled()
    {
        return 0 >= getLogLevel();
    }

    public static void setLogLevel(int logLevel)
    {
        m_logLevel.set(logLevel);
    }

    public static short getLogLevel()
    {
        return m_logLevel.shortValue();
    }

    public static short getSubnetScanStatus()
    {
        return m_scanStatus.shortValue();
    }

    public static void sendMail(String mailServerHost, int mailServerPort, String subject, String message, String sender, String recipients, String securityType, String userName, String password, int timeout) throws Exception
    {
        HtmlEmail email = new HtmlEmail();
        email.setHostName(mailServerHost);
        email.setSmtpPort(mailServerPort);
        email.setFrom(sender);
        //email.setDebug(true);
        email.setSubject(subject);
        if (message != null && message.length() > 0) {
            email.setHtmlMsg(message);
        } else {
            email.setHtmlMsg("Empty Email Body !!!");
        }

        if (recipients != null && recipients.length() > 0) {
            String[] var11 = recipients.split(",");
            int var12 = var11.length;

            for (int var13 = 0; var13 < var12; ++var13) {
                String recipient = var11[var13];
                email.addTo(recipient);
            }
        }

        email.setSocketTimeout(timeout * 1000);
        email.setSocketConnectionTimeout(timeout * 1000);
        if (userName != null && password != null && userName.length() > 0 && password.length() > 0) {
            email.setAuthentication(userName, password);
        }

        if (securityType.equalsIgnoreCase("ssl")) {
            email.setSslSmtpPort(String.valueOf(mailServerPort));
            email.setSSLOnConnect(true);
        } else if (securityType.equalsIgnoreCase("tls")) {
            email.setStartTLSEnabled(true);
        }

        email.send();
    }

    public  void sendMailWithAttachment(String mailServerHost, int mailServerPort, String subject, String message, String sender, String recipients, String securityType, String userName, String password, int timeout,File file) throws Exception
    {
        HtmlEmail email = new HtmlEmail();

        email.setHostName(mailServerHost);

        email.setSmtpPort(mailServerPort);

        email.setFrom(sender);

        email.setDebug(false);

        email.setSubject(subject);

        email.attach(file);

        if (message != null && message.length() > 0)
        {
            email.setHtmlMsg(message);
        }
        else
        {
            email.setHtmlMsg("Empty Email Body !!!");
        }

        if (recipients != null && recipients.length() > 0)
        {
            String[] var11 = recipients.split(",");

            int var12 = var11.length;

            for (int var13 = 0; var13 < var12; ++var13)
            {
                String recipient = var11[var13];

                email.addTo(recipient);
            }
        }

        email.setSocketTimeout(timeout * 1000);

        email.setSocketConnectionTimeout(timeout * 1000);

        if (userName != null && password != null && userName.length() > 0 && password.length() > 0)
        {
            email.setAuthentication(userName, password);
        }

        if (securityType.equalsIgnoreCase("ssl"))
        {
            email.setSslSmtpPort(String.valueOf(mailServerPort));

            email.setSSLOnConnect(true);
        }
        else if (securityType.equalsIgnoreCase("tls"))
        {
            email.setStartTLSEnabled(true);
        }
        email.send();
    }

    public static String getJSON(Object target) {
        return m_jsonSerializer.deepSerialize(target);
    }

    public static HashMap<String, Object> deserialize(String target) {
        return m_jsonDeserializer.deserialize(target, HashMap.class);
    }

    public static HashMap<String, Object> deserialize(FileReader fileReader) {
        return m_jsonDeserializer.deserialize(fileReader, HashMap.class);
    }

    public static void extractMetricValue(HashMap<String, Object> metricDetails, String counterValue, String counterValues, String key)
    {
        if(!counterValue.trim().matches("\\d+") && !counterValue.trim().matches("\\d+.\\d+"))
        {
            metricDetails.put(key, convertToLongValue(counterValues.trim()));
        }
        else
        {
            metricDetails.put(key, convertToLongValue(counterValue.trim()));
        }

    }

    static int convertToSeconds(long milliSeconds) {
        return (int)(milliSeconds / 1000L);
    }

    static String formatTime(long seconds) {
        long days = seconds / 86400L;
        long hours = seconds % 86400L / 3600L;
        long minutes = seconds % 86400L % 3600L / 60L;
        return days + " days " + hours + " hours " + minutes + " minutes ";
    }

    static int getIntegerValue(Object target)
    {
        int value = 0;
        if (target != null) {
            value = Integer.parseInt(target.toString());
        }

        return value;
    }

    private static short getShortValue(Object target)
    {
        short value = 0;
        if (target != null) {
            value = Short.parseShort(target.toString());
        }

        return value;
    }

    public static float getFloatValue(Object target)
    {
        float value = 0.0F;
        if (target != null) {
            value = Float.parseFloat(target.toString());
        }

        return value;
    }

    public static double getDoubleValue(Object target)
    {
        double value = 0.0D;
        if (target != null) {
            value = Double.parseDouble(target.toString());
        }

        return value;
    }

    static long convertToLongValue(Object target)
    {
        long value = 0L;
        if (target != null && target.toString().length() > 0 && !target.toString().equalsIgnoreCase("-Infinity") && !target.toString().equalsIgnoreCase("Infinity") && !target.toString().equalsIgnoreCase("NaN")) {
            value = Math.round((new BigDecimal(target.toString())).doubleValue());
        }

        return value;
    }

    static long getLongValue(Object target)
    {
        long value = 0L;
        if (target != null) {
            value = Long.parseLong(target.toString());
        }

        return value;
    }

    static HashMap<String, Object> getMapValue(Object target)
    {
        HashMap<String, Object> value = null;
        if (target != null) {
            value = (HashMap<String, Object>) target;
        }

        return value;
    }

    public static byte[] getByteValues(Object target)
    {
        byte[] value = null;
        if (target != null) {
            value = target.toString().getBytes();
        }

        return value;
    }

    public static String getStringValue(Object target)
    {
        String value = null;
        if (target != null) {
            value = String.valueOf(target);
        }

        return value;
    }

    public void fileUpload(MultipartFile multipartFile,HttpServletRequest request)
    {
        String appendFilePath = File.separator;

        String originalFileName = null;

        @SuppressWarnings("deprecation")
        String UPLOAD_PATH = request.getRealPath("/images/") + File.separator + appendFilePath;

        String  imageForReportPath = TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Images"+TraceOrgCommonConstants.PATH_SEPARATOR;

        File file = new File(UPLOAD_PATH);

        if (!file.exists())
        {
            file.mkdirs();
        }

        try
        {
            originalFileName = "logo.png";

            File saveFile = new File(UPLOAD_PATH + originalFileName);

            File reportImageFile = new File(imageForReportPath + originalFileName);

            if (!saveFile.exists())
                saveFile.createNewFile();

            if (!reportImageFile.exists())
                reportImageFile.createNewFile();

            byte[] bytes = multipartFile.getBytes();

            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(saveFile));

            bufferedOutputStream.write(bytes);

            bufferedOutputStream.close();

            BufferedOutputStream bufferedOutputStream2 = new BufferedOutputStream(new FileOutputStream(reportImageFile));

            bufferedOutputStream2.write(bytes);

            bufferedOutputStream2.close();

        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added IPV6 support
     * */
    static String resolveHost(String ip, String dnsAddress)
    {
        try
        {
            if (TraceOrgCommonConstants.IPV4_PATTERN.matcher(ip).matches() || (ip != null && ip.contains(":")))
            {
                String result = reverseLookup(ip, dnsAddress);

                return (!Strings.isNullOrEmpty(result) && !result.equals(ip)) ? result : null;
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return null;  // Explicitly return null when reverse lookup fails
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added IPV6 support
     * */
    static String resolveIp(String host, String dnsAddress, boolean isIpv6)
    {
        try
        {
            if (host != null && !host.contains(":") && !TraceOrgCommonConstants.IPV4_PATTERN.matcher(host).matches())
            {
                String result = lookup(host, dnsAddress, isIpv6);

                return (!Strings.isNullOrEmpty(result) && !result.equals(host)) ? result : null;
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return null;  // Explicitly return null when lookup fails
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added IPV6 support
     * IPAM-140 IPAM | Add v3 Support for SNMP in Remote Subnet
     * Added a circuit breaker in DNS.
     * */
    private static String lookup(String hostIp, String dnsAddress, boolean isIpv6)
    {
        try
        {
            Resolver extendedResolver;

            if (!Strings.isNullOrEmpty(dnsAddress))
            {
                String[] dns = dnsAddress.split(",");

                extendedResolver = new ExtendedResolver(dns);

            }
            else
            {
                extendedResolver = new ExtendedResolver();
            }

            extendedResolver.setTimeout(TraceOrgCommonConstants.DNS_TIME_OUT);

            Lookup lookup = new Lookup(hostIp, org.xbill.DNS.Type.ANY);

            lookup.setResolver(extendedResolver);

            lookup.setCache(null);

            Record[] records = lookup.run();

            if (lookup.getResult() == Lookup.SUCCESSFUL && records != null && records.length > 0)
            {
                for (Record record : records)
                {
                    if (record instanceof ARecord && !isIpv6)
                    {
                        return ((ARecord) record).getAddress().getHostAddress();
                    }
                    if (record instanceof AAAARecord && isIpv6)
                    {
                        return ((AAAARecord) record).getAddress().getHostAddress();
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            _logger.warn("lookup failed for host " + hostIp + " and dns address " + dnsAddress);
        }

        return null; // Return null if lookup fails
    }

    private static String reverseLookup(String hostIp, String dnsAddress)
    {
        try
        {
            Resolver extendedResolver;

            if (!Strings.isNullOrEmpty(dnsAddress))
            {
                String[] dns = dnsAddress.split(",");

                extendedResolver = new ExtendedResolver(dns);

            }
            else
            {
                extendedResolver = new ExtendedResolver();
            }

            extendedResolver.setTimeout(TraceOrgCommonConstants.DNS_TIME_OUT);

            Name name = ReverseMap.fromAddress(hostIp);

            Record record = Record.newRecord(name, org.xbill.DNS.Type.PTR, DClass.IN);

            Message query = Message.newQuery(record);

            Message response = extendedResolver.send(query);

            Record[] answers = response.getSectionArray(Section.ANSWER);

            if (answers.length != 0)
            {
                String result = answers[0].rdataToString();

                return result.endsWith(".") ? result.substring(0, result.length() - 1) : result;
            }
        }
        catch (SocketTimeoutException exception)
        {
            if (TraceOrgSubnetUtil.dnsCircuitBreakCount.incrementAndGet() >= TraceOrgCommonConstants.DNS_CIRCUIT_BREAK_COUNT)
            {
                TraceOrgSubnetUtil.isDnsCircuitBreak.set(TraceOrgCommonConstants.TRUE);

                _logger.warn("Timeout occurred " + TraceOrgCommonConstants.DNS_CIRCUIT_BREAK_COUNT + " times. DNS circuit break for " + dnsAddress);
            }

            _logger.error(exception);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            _logger.warn("Reverse lookup failed for host " + hostIp + " and dns address " + dnsAddress);
        }

        return null;  // Return null instead of input IP when reverse lookup fails
    }

    public static int getMaxPingCheckTimeout()
    {
        int second = 10;

        try
        {
            HashMap<String, String> configDetails = TraceOrgConfigUtil.loadConfigFile(TraceOrgCommonConstants.IPM_CONF);

            if(configDetails != null && configDetails.get("max-ping-check-timeout") != null && configDetails.size() > 0)
            {
                second = TraceOrgCommonUtil.getIntegerValue(configDetails.get("max-ping-check-timeout"));
            }
        }
        catch (Exception var2)
        {
            _logger.error(var2);
        }

        return second;
    }

    static int getMaxPingCheckRetryCount()
    {
        int retry = 3;

        try
        {
            HashMap<String, String> configDetails = TraceOrgConfigUtil.loadConfigFile(TraceOrgCommonConstants.IPM_CONF);

            if(configDetails != null && configDetails.get("max-ping-check-retry-count") != null && configDetails.size() > 0)
            {
                retry = TraceOrgCommonUtil.getIntegerValue(configDetails.get("max-ping-check-retry-count"));
            }
        }
        catch (Exception var2)
        {
            _logger.error(var2);
        }

        return retry;
    }

    static int getMaxConcurrentPing()
    {
        int concurrentPing = 500;

        try
        {
            HashMap<String, String> configDetails = TraceOrgConfigUtil.loadConfigFile(TraceOrgCommonConstants.IPM_CONF);

            if(configDetails != null && configDetails.get("max-concurrent-ping") != null && configDetails.size() > 0)
            {
                concurrentPing = TraceOrgCommonUtil.getIntegerValue(configDetails.get("max-concurrent-ping"));
            }
        }
        catch (Exception var2)
        {
            _logger.error(var2);
        }

        return concurrentPing;
    }

    public static int getMaxAlertWorker()
    {
        int alertWorker = 3;

        try
        {
            HashMap configDetails = TraceOrgConfigUtil.loadConfigFile(TraceOrgCommonConstants.IPM_CONF);

            if ((configDetails != null) && configDetails.get("max-alert-worker") != null && (configDetails.size() > 0))
            {
                alertWorker = TraceOrgCommonUtil.getIntegerValue(configDetails.get("max-alert-worker"));
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return alertWorker;
    }

    static int getProcessRequestTimeout()
    {
        int timeout = 1200;

        try
        {
            HashMap<String, String> configDetails = TraceOrgConfigUtil.loadConfigFile(TraceOrgCommonConstants.IPM_CONF);

            if(configDetails != null && configDetails.get("process-request-timeout") != null && configDetails.size() > 0)
            {
                timeout = TraceOrgCommonUtil.getIntegerValue(configDetails.get("process-request-timeout"));
            }
        }
        catch (Exception var2)
        {
            _logger.error(var2);
        }

        return timeout;
    }

    static boolean isHostReachable(String ipAddress) throws Exception
    {
        return isHostReachable(ipAddress, (short) getMaxPingCheckRetryCount(), (long) getMaxPingCheckTimeout());
    }

    static boolean isHostReachable(String ipAddress, short retryCount, long timeout) throws Exception
    {
        _logger.debug("pinging " + ipAddress);

        boolean isReachable = false;

        Process process = null;

        BufferedReader bufferedReader = null;

        InputStream inputStream = null;

        String fileSeparator = System.getProperty("file.separator");

        boolean startLogging = false;

        ArrayList<String> commands = new ArrayList<>();

        try
        {
            if(fileSeparator.equalsIgnoreCase("/"))
            {
                commands.add("ping");

                commands.add("-c");

                commands.add(String.valueOf(retryCount));

                commands.add("-w");

                commands.add(String.valueOf(timeout));

                commands.add(ipAddress);

                ProcessBuilder processBuilder = new ProcessBuilder(commands);

                process = processBuilder.start();

                inputStream = process.getInputStream();

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while((line = bufferedReader.readLine()) != null)
                {
                    line = line.toLowerCase();

                    if(line.trim().contains("ping statistics"))
                    {
                        _logger.debug("ping statistics are available of" + ipAddress);

                        startLogging = true;
                    }

                    if(startLogging)
                    {
                        if(line.trim().contains("min"))
                        {
                            isReachable = true;

                            break;
                        }
                        isReachable = false;
                    }
                }

            }
            else if(fileSeparator.equalsIgnoreCase("\\"))
            {
                commands.add("ping");

                commands.add("-n");

                commands.add(String.valueOf(retryCount));

                commands.add("-w");

                commands.add(String.valueOf(timeout));

                commands.add(ipAddress);

                ProcessBuilder processBuilder = new ProcessBuilder(commands);

                process = processBuilder.start();

                inputStream = process.getInputStream();

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while((line = bufferedReader.readLine()) != null)
                {
                    line = line.toLowerCase();

                    if(line.trim().contains("ping statistics"))
                    {
                        _logger.debug("ping statistics are available of" + ipAddress);

                        startLogging = true;
                    }

                    if(startLogging)
                    {
                        if(line.trim().startsWith("ping statistics for "+ipAddress+":"))
                        {
                            isReachable = true;

                            break;
                        }
                        isReachable = false;
                    }
                }

            }

            if(isReachable)
            {
                _logger.debug("[" + ipAddress + "] is reachable.");

            }
            else
            {
                _logger.warn("[" + ipAddress + "] is not reachable.");
            }

            if (inputStream != null)
            {
                inputStream.close();
            }

            if (bufferedReader != null)
            {
                bufferedReader.close();
            }

            if (process != null)
            {
                process.getErrorStream().close();

                process.getOutputStream().close();

                process.getInputStream().close();

                process.destroy();
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            _logger.warn(ipAddress + " host is not reachable");
        }
        return isReachable;
    }

    static boolean isPortReachable(String ipAddress, int port)
    {
        _logger.debug("checking port " + port + " of " + ipAddress);

        boolean result = false;

        try
        {
            Socket socket = new Socket(ipAddress, port);

            socket.close();

            result = true;

            _logger.debug(port + " port is open of " + ipAddress);
        }
        catch (Exception var4)
        {
            _logger.error(var4);

            _logger.warn(port + " port is not open of " + ipAddress);
        }

        return result;
    }
    //Krunal Thakkar
    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added IPV6 support
     * */
    public boolean checkSubnet(String ip,int cidr)
    {
        boolean result = Boolean.FALSE;

        try
        {
            if(isIPv6Address(ip))
            {
                IPAddress address = new IPAddressString(ip).getAddress();

                if(address != null)
                {
                    String mask = address.getNetworkSection(cidr).toString().split("/")[0];

                    long count = mask.chars().filter(ch -> ch == ':').count();

                    if(count < 7)
                    {
                        mask += "::";
                    }

                    IPAddress subnetAddress = new IPAddressString(mask).getAddress();

                    if(subnetAddress !=null && subnetAddress.toString().equals(address.toString()))
                    {
                        result = Boolean.TRUE;
                    }
                }
            }
            else
            {
                SubnetUtils utils = new SubnetUtils(ip+"/"+cidr);

                result =  ip.equals(utils.getInfo().getNetworkAddress());
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return result;
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added method to check isIPv6Address
     * */
    public static boolean isIPv6Address(String ipAddress)
    {
        try
        {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);

            return inetAddress instanceof java.net.Inet6Address;
        }
        catch (Exception exception)
        {
            return false;
        }
    }

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the method to validate the isIPv4Address
     * @param ipAddress
     * @return
     */
    public static boolean isIPv4Address(String ipAddress)
    {
        try
        {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);

            return inetAddress instanceof java.net.InetAddress;
        }
        catch (Exception exception)
        {
            return false;
        }
    }

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the method to convert ip address to integer
     * @param ipAddress
     * @return
     */
    public static int convertIpAddressToInterger(String ipAddress)
    {
        byte[] bytes = new byte[0];

        int intIPAddress = 0;

        try
        {
            bytes = InetAddress.getByName(ipAddress).getAddress();

            intIPAddress = ((bytes[0] & TraceOrgCommonConstants.MASK_8BIT) << 24) | ((bytes[1] & TraceOrgCommonConstants.MASK_8BIT) << 16) | ((bytes[2] & TraceOrgCommonConstants.MASK_8BIT) << 8) | (bytes[3] & TraceOrgCommonConstants.MASK_8BIT);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return intIPAddress;
    }

    public String getSubnetMask(String ip,int cidr)
    {
        String subnetMask = "";

        try
        {
            SubnetUtils utils = new SubnetUtils(ip+"/"+cidr);

            subnetMask =  utils.getInfo().getNetmask();
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return subnetMask;
    }
    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * This method is only used for IPv4.
     * */
    public Long countTotalIp(String ip,int cidr)
    {
        Long count = 0L;

        try
        {
            if(!TraceOrgCommonUtil.isIPv6Address(ip))
            {
                SubnetUtils utils = new SubnetUtils(ip + "/" + cidr);

                utils.setInclusiveHostCount(true);

                count =  (long) utils.getInfo().getAddressCount();
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return count;
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Run the netsh command and filter all IPs that are in the specified range.
     * */
    public List<String> getIPV6Addresses(String subnetDetails)
    {
        HashSet<String> ipList = new HashSet<>();

        Process process = null;

        try
        {
            Runtime runtime = this.traceOrgFactoryUtil.getRuntime();

            process = runtime.exec(TraceOrgCommonConstants.IPV6_NETSH_COMMAND);

            try (BufferedReader bufferedInputStream = this.traceOrgFactoryUtil.getBufferedReader(process.getInputStream()))
            {
                IPAddressString ipWithMask = new IPAddressString(subnetDetails);

                IPAddressSeqRange range = ipWithMask.getAddress().toSequentialRange();

                String line;

                while ((line = bufferedInputStream.readLine()) != null)
                {
                    String[] result = line.trim().split("\\s+");

                    if (result.length > 0)
                    {
                        String ipv6 = result[0];

                        IPAddress ipAddress = new IPAddressString(ipv6).getAddress();

                        if (ipAddress != null && range.contains(ipAddress))
                        {
                            ipList.add(ipAddress.toString());
                        }
                    }
                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        finally
        {
            if(process != null)
            {
                process.destroy();
            }
        }

        return new ArrayList<>(ipList);
    }

    /**
     * IPAM-136 IPAM | Table is not Updated and due to this device is not appear in UI in EXE
     * Added generateKey method
     * */
    public static Key generateKey()
    {
        return new SecretKeySpec(TraceOrgCommonConstants.ENCRYPT_DECRYPT_KEY, TraceOrgCommonConstants.ALGORITHM);
    }

    /**
     * IPAM-136 IPAM | Table is not Updated and due to this device is not appear in UI in EXE
     * Added decrypt method
     * */
    public static String decrypt(String value)
    {

        String result = "";

        try
        {
            if (value != null && value.length() > 0)
            {
                Key key = generateKey();

                Cipher cipher = Cipher.getInstance(TraceOrgCommonConstants.ALGORITHM);

                cipher.init(Cipher.DECRYPT_MODE, key);

                byte[] decodedValue = Base64.getDecoder().decode(value);

                byte[] decodedBytes = cipher.doFinal(decodedValue);

                result = new String(decodedBytes);
            }

        }

        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;

    }

    /**
     * IPAM-136 IPAM | Table is not Updated and due to this device is not appear in UI in EXE
     * Added encrypt method
     * */
    public static String encrypt(String value)
    {
        String result = null;

        try
        {
            Key key = generateKey();

            Cipher cipher = Cipher.getInstance(TraceOrgCommonConstants.ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedValue = cipher.doFinal(value.getBytes());

            result = Base64.getEncoder().encodeToString(encryptedValue);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * This will set the status of all filtered IPs to 'used'.
     * IPAM-131 IPAM | IPAM displays duplicated IP details in IPv6.
     * If there is only one IP, it was dumping the same IP as the first and last IP in IPv6.
     * IPAM-145 : System should have rogue device detection capability
     * Added authenticity for ipv6 and ipv4 during insert time of subnet.
     * */
    public boolean ipList(TraceOrgSubnetDetails traceOrgSubnetDetails)
    {
        boolean result = false;

        List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsList = new ArrayList<>();

        try
        {
            String[] addresses;

            if(TraceOrgCommonUtil.isIPv6Address(traceOrgSubnetDetails.getSubnetAddress()))
            {
                List<String> ipList = getIPV6Addresses(traceOrgSubnetDetails.getSubnetAddress()+"/"+traceOrgSubnetDetails.getSubnetCidr());

                addresses = ipList.toArray(new String[0]);

                traceOrgSubnetDetails.setIpv6(true);

                traceOrgSubnetDetails.setTotalIp((long) addresses.length);

                traceOrgSubnetDetails.setAvailableIp((long) addresses.length);
            }
            else
            {
                SubnetUtils utils = new SubnetUtils(traceOrgSubnetDetails.getSubnetAddress()+"/"+traceOrgSubnetDetails.getSubnetCidr());

                utils.setInclusiveHostCount(true);

                addresses = utils.getInfo().getAllAddresses();
            }

            List<TraceOrgSubnetDetails> traceOrgSubnetDetailsList = (List<TraceOrgSubnetDetails>)this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_BY_SUBNET_ADDRESS.replace(TraceOrgCommonConstants.SUBNET_ADDRESS_VALUE,traceOrgSubnetDetails.getSubnetAddress()));

            if(traceOrgSubnetDetailsList != null && !traceOrgSubnetDetailsList.isEmpty() && addresses.length > 0)
            {
                TraceOrgSubnetIpDetails traceOrgSubnetIpDetailsFirst = new TraceOrgSubnetIpDetails();

                traceOrgSubnetIpDetailsFirst.setIpAddress(addresses[0]);

                traceOrgSubnetIpDetailsFirst.setSubnetId(traceOrgSubnetDetailsList.get(0));

                if(traceOrgSubnetDetails.isIpv6())
                {
                    traceOrgSubnetIpDetailsFirst.setStatus(TraceOrgCommonConstants.USED);

                    traceOrgSubnetIpDetailsFirst.setPreviousStatus(TraceOrgCommonConstants.USED);

                    traceOrgSubnetIpDetailsFirst.setAuthenticity("discovered");
                }
                else
                {
                    traceOrgSubnetIpDetailsFirst.setStatus(TraceOrgCommonConstants.RESERVED);

                    traceOrgSubnetIpDetailsFirst.setPreviousStatus(TraceOrgCommonConstants.RESERVED);

                    traceOrgSubnetIpDetailsFirst.setAuthenticity("-");
                }

                traceOrgSubnetIpDetailsFirst.setCreatedDate(new Date());

                traceOrgSubnetIpDetailsFirst.setModifiedDate(new Date());

                traceOrgSubnetIpDetailsList.add(traceOrgSubnetIpDetailsFirst);

                IntStream.range(1, addresses.length - 1).forEach(index -> {
                    TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

                    traceOrgSubnetIpDetails.setIpAddress(addresses[index]);

                    traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetailsList.get(0));

                    if(traceOrgSubnetDetails.isIpv6())
                    {
                        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.USED);

                        traceOrgSubnetIpDetails.setPreviousStatus(TraceOrgCommonConstants.USED);

                        traceOrgSubnetIpDetails.setAuthenticity("discovered");
                    }
                    else
                    {
                        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.AVAILABLE);

                        traceOrgSubnetIpDetails.setPreviousStatus(TraceOrgCommonConstants.AVAILABLE);

                        traceOrgSubnetIpDetails.setAuthenticity("-");
                    }
                    traceOrgSubnetIpDetails.setCreatedDate(new Date());

                    traceOrgSubnetIpDetails.setModifiedDate(new Date());

                    traceOrgSubnetIpDetailsList.add(traceOrgSubnetIpDetails);
                });

                if(addresses.length > 1)
                {
                    TraceOrgSubnetIpDetails traceOrgSubnetIpDetailsLast = new TraceOrgSubnetIpDetails();

                    traceOrgSubnetIpDetailsLast.setIpAddress(addresses[addresses.length-1]);

                    traceOrgSubnetIpDetailsLast.setSubnetId(traceOrgSubnetDetailsList.get(0));

                    if(traceOrgSubnetDetails.isIpv6())
                    {
                        traceOrgSubnetIpDetailsLast.setStatus(TraceOrgCommonConstants.USED);

                        traceOrgSubnetIpDetailsLast.setPreviousStatus(TraceOrgCommonConstants.USED);

                        traceOrgSubnetIpDetailsLast.setAuthenticity("discovered");
                    }
                    else
                    {
                        traceOrgSubnetIpDetailsLast.setStatus(TraceOrgCommonConstants.RESERVED);

                        traceOrgSubnetIpDetailsLast.setPreviousStatus(TraceOrgCommonConstants.RESERVED);

                        traceOrgSubnetIpDetailsLast.setAuthenticity("-");
                    }

                    traceOrgSubnetIpDetailsLast.setCreatedDate(new Date());

                    traceOrgSubnetIpDetailsLast.setModifiedDate(new Date());

                    traceOrgSubnetIpDetailsList.add(traceOrgSubnetIpDetailsLast);
                }

                result = this.traceOrgService.insertAll(traceOrgSubnetIpDetailsList);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    public String getIdList(String []subnetIpIdString)
    {
        String idList = "";

        try
        {
            if (subnetIpIdString != null && subnetIpIdString.length > 0)
            {
                Set<String> idSet = new HashSet<>(Arrays.asList(subnetIpIdString));

                // removing subnet IP id from set
                idSet.remove(subnetIpIdString[0]);

                if (!idSet.isEmpty())
                {
                    idList = "'" + StringUtils.join(idSet, "','") + "'";
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return idList;
    }

    public List<TraceOrgSubnetIpDetails> getSubnetIpDetailsList(String[] subnetIpIdString)
    {
        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        try
        {
            if (subnetIpIdString.length == 1)
            {
                subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_BY_SUBNET_ID.replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,subnetIpIdString[0]));
            }
            else
            {
                String idList = getIdList(subnetIpIdString);

                subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SELECTED_IP_SUBNET_ID.replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,subnetIpIdString[0]).replace(TraceOrgCommonConstants.SUBNET_ID_LIST, idList));
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return subnetIpDetailsList;
    }

    public boolean importCSVFile(MultipartFile multipartFile,HttpServletRequest request,String fileName)
    {
        boolean result = false;

        try
        {
            String appendFilePath = File.separator;

            String originalFileName = null;

            @SuppressWarnings("deprecation")
            String UPLOAD_PATH = request.getRealPath("/csv/") + File.separator + appendFilePath;

            File file = new File(UPLOAD_PATH);

            if (!file.exists())
            {
                file.mkdirs();
            }

            originalFileName = fileName;

            File saveFile = new File(UPLOAD_PATH + originalFileName);

            if (!saveFile.exists())
            {
                saveFile.createNewFile();
            }

            byte[] bytes = multipartFile.getBytes();

            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(saveFile));

            bufferedOutputStream.write(bytes);

            bufferedOutputStream.close();

            result = true;
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return result;
    }

    public boolean checkSubnetFileData(CsvRow csvRow)
    {
        boolean result = false;

        try
        {
            if(csvRow.getField(0).contains("Category Name") && csvRow.getField(1).contains("Subnet Address") && csvRow.getField(2).contains("Subnet Mask")
                    && csvRow.getField(3).contains("Subnet CIDR") && csvRow.getField(4).contains("Subnet Name") && csvRow.getField(5).contains("VLAN Name")
                    && csvRow.getField(6).contains("Location") && csvRow.getField(7).contains("Description") && csvRow.getField(8).contains("DNS Address")
                    && csvRow.getField(9).contains("Scheduled Hours") && csvRow.getField(10).contains("Duration") && csvRow.getField(11).contains("Local Subnet")
                    && csvRow.getField(12).contains("Gateway IP"))
            {
                result = true;
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return result;
    }

    /**
     * IPAM-160 IPAM Roadmap : The solution must be flexible to allow the creation of custom fields for objects in IPAM. This must be configurable via the Web GUI.
     * Validate header with custom columns
     * @param csvRow
     * @return
     */
    public boolean checkSubnetIPFileData(CsvRow csvRow)
    {
        boolean result = true;

        List<TraceOrgCustomColumn> customColumn = traceOrgCustomColumnRepository.findByColumnAt("subnetIp");

        Collection<String[]> data = new ArrayList<>();

        List<String> customColumnNames = customColumn.stream()
                .map(TraceOrgCustomColumn::getColumnName)
                .collect(Collectors.toList());

        List<String> headers = new ArrayList<>(Arrays.asList(
                "IP Address", "Mac Address", "Status", "IP To Dns",
                "Dns To Ip", "Vendor", "Authenticity", "Last Alive Time"
        ));

        headers.addAll(customColumnNames);
        try
        {
            if(csvRow.getFields().size()!=headers.size())
            {
                result = false;
            }
            else
            {
                for (int headerIndex = 0; headerIndex < headers.size(); headerIndex++) {
                    if (!csvRow.getField(headerIndex).equals(headers.get(headerIndex))) {
                        result = false;
                        break;
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return result;
    }

    public boolean insertSubnetIp(TraceOrgSubnetIpDetails traceOrgSubnetIpDetails)
    {
        boolean result = false;

        try
        {
            if(this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS,TraceOrgCommonConstants.SUBNET_ID,traceOrgSubnetIpDetails.getSubnetId().getId().toString()))
            {
                List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>)this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_IP_ADDRESS.replace(TraceOrgCommonConstants.IP_ADDRESS_VALUE,traceOrgSubnetIpDetails.getIpAddress()));

                if(traceOrgSubnetIpDetailsList != null && !traceOrgSubnetIpDetailsList.isEmpty())
                {
                    TraceOrgSubnetIpDetails traceOrgSubnetIpDetailsExisted =  traceOrgSubnetIpDetailsList.get(0);

                    traceOrgSubnetIpDetailsExisted.setMacAddress(traceOrgSubnetIpDetails.getMacAddress());

                    if(traceOrgSubnetIpDetails.getStatus().equals(TraceOrgCommonConstants.USED) && traceOrgSubnetIpDetailsExisted.getMacAddress()!=null)
                    {
                        if(traceOrgSubnetIpDetailsExisted.getPreviousMacAddress()!=null && !traceOrgSubnetIpDetailsExisted.getPreviousMacAddress().isEmpty())
                        {
                            if(!traceOrgSubnetIpDetailsExisted.getPreviousMacAddress().equals(traceOrgSubnetIpDetailsExisted.getMacAddress()))
                            {
                                traceOrgSubnetIpDetailsExisted.setConflictMac(traceOrgSubnetIpDetailsExisted.getPreviousMacAddress());
                            }
                            else
                            {
                                traceOrgSubnetIpDetailsExisted.setConflictMac(null);
                            }
                            traceOrgSubnetIpDetailsExisted.setPreviousMacAddress(traceOrgSubnetIpDetailsExisted.getMacAddress());
                        }
                        else
                        {
                            traceOrgSubnetIpDetailsExisted.setPreviousMacAddress(traceOrgSubnetIpDetails.getMacAddress());
                        }
                    }

                    traceOrgSubnetIpDetailsExisted.setDescription(traceOrgSubnetIpDetails.getDescription());

                    traceOrgSubnetIpDetailsExisted.setDeviceType(traceOrgSubnetIpDetails.getDeviceType());

                    traceOrgSubnetIpDetailsExisted.setDnsStatus(traceOrgSubnetIpDetails.getDnsStatus());

                    traceOrgSubnetIpDetailsExisted.setHostName(traceOrgSubnetIpDetails.getHostName());

                    if(traceOrgSubnetIpDetails.getLastAliveTime()!=null)
                    {
                        traceOrgSubnetIpDetailsExisted.setLastAliveTime(new Date(traceOrgSubnetIpDetails.getLastAliveTime()));
                    }

                    traceOrgSubnetIpDetailsExisted.setIpToDns(traceOrgSubnetIpDetails.getIpToDns());

                    traceOrgSubnetIpDetailsExisted.setDnsToIp(traceOrgSubnetIpDetails.getDnsToIp());

                    //traceOrgSubnetIpDetailsExisted.setDeactiveStatus(traceOrgSubnetIpDetails.isDeactiveStatus());

                    if(traceOrgSubnetIpDetailsExisted.getStatus().equals(TraceOrgCommonConstants.USED) && traceOrgSubnetIpDetails.getStatus().equals(TraceOrgCommonConstants.AVAILABLE))
                    {
                        traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.TRANSIENT);

                        traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.USED);

                        traceOrgSubnetIpDetails.setModifiedDate(new Date());
                    }
                    else if(traceOrgSubnetIpDetailsExisted.getStatus().equals(TraceOrgCommonConstants.TRANSIENT) && traceOrgSubnetIpDetails.getStatus().equals(TraceOrgCommonConstants.AVAILABLE))
                    {
                        traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.TRANSIENT);

                        traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.AVAILABLE);

                        traceOrgSubnetIpDetails.setModifiedDate(new Date());
                    }
                    else if(traceOrgSubnetIpDetailsExisted.getStatus().equals(TraceOrgCommonConstants.TRANSIENT) && traceOrgSubnetIpDetails.getStatus().equals(TraceOrgCommonConstants.USED))
                    {
                        traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.USED);

                        traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.AVAILABLE);

                        traceOrgSubnetIpDetails.setModifiedDate(new Date());
                    }
                    else if(traceOrgSubnetIpDetailsExisted.getStatus().equals(TraceOrgCommonConstants.RESERVED) && traceOrgSubnetIpDetails.getStatus().equals(TraceOrgCommonConstants.USED))
                    {
                        traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.USED);

                        traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.AVAILABLE);

                        traceOrgSubnetIpDetails.setModifiedDate(new Date());
                    }
                    else if(traceOrgSubnetIpDetailsExisted.getStatus().equals(TraceOrgCommonConstants.RESERVED) && traceOrgSubnetIpDetails.getStatus().equals(TraceOrgCommonConstants.AVAILABLE))
                    {
                        traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.RESERVED);

                        traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.AVAILABLE);

                        traceOrgSubnetIpDetails.setModifiedDate(new Date());
                    }
                    else
                    {
                        traceOrgSubnetIpDetailsExisted.setPreviousStatus(traceOrgSubnetIpDetailsExisted.getStatus());

                        traceOrgSubnetIpDetailsExisted.setStatus(traceOrgSubnetIpDetails.getStatus());
                    }

                    boolean updateStatus = this.traceOrgService.insert(traceOrgSubnetIpDetailsExisted);

                    if (updateStatus)
                    {
                        result = true;
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return result;
    }


    /**
     * IPAM-145 : System should have rogue device detection capability
     * Change the column rogue to authenticity.
     * @param request
     * @param subnetIpDetailsList
     * @return
     */
    public String exportSubnetIpCsv(HttpServletRequest request, List<TraceOrgSubnetIpDetails> subnetIpDetailsList)
    {
        String fileName = ("Subnet Ip Summary "+subnetIpDetailsList.get(0).getSubnetId().getSubnetAddress()+"_"+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".csv").replace(" ","_").replace(":","_").replace(",","");

        try
        {
            File file = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR + fileName);

            CsvWriter csvWriter = new CsvWriter();

            Collection<String[]> data = new ArrayList<>();

            List<TraceOrgCustomColumn> customColumn=traceOrgCustomColumnRepository.findByColumnAt("subnetIp");

            List<String> customColumnNames = customColumn.stream()
                    .map(TraceOrgCustomColumn::getColumnName)
                    .collect(Collectors.toList());

            List<String> headers = new ArrayList<>(Arrays.asList(
                    "IP Address", "Mac Address", "Status", "IP To Dns",
                    "Dns To Ip", "Vendor", "Authenticity", "Last Alive Time"
            ));

            headers.addAll(customColumnNames);

            data.add(headers.toArray(new String[0]));

            ObjectMapper objectMapper = new ObjectMapper();

            for (TraceOrgSubnetIpDetails traceOrgSubnetIpDetails : subnetIpDetailsList) {
                // Base fields
                List<String> rowData = new ArrayList<>();
                rowData.add(traceOrgSubnetIpDetails.getIpAddress());
                rowData.add(traceOrgSubnetIpDetails.getMacAddress());
                rowData.add(traceOrgSubnetIpDetails.getStatus());
                rowData.add(traceOrgSubnetIpDetails.getIpToDns());
                rowData.add(traceOrgSubnetIpDetails.getDnsToIp());
                rowData.add(traceOrgSubnetIpDetails.getDeviceType());
                rowData.add(traceOrgSubnetIpDetails.getAuthenticity());
                rowData.add(traceOrgSubnetIpDetails.getLastAliveTime());

                // Parse the JSON response from getCustomColumn()
                String customColumnJson = String.valueOf(traceOrgSubnetIpDetails.getCustomColumns());

                try {
                    JsonNode customColumnNode = objectMapper.readTree(customColumnJson);
                    customColumnNode.fields().forEachRemaining(entry -> rowData.add(entry.getValue().asText()));
                } catch (Exception exception) {
                    _logger.error(exception);
                }

                // Add row data to the list
                data.add(rowData.toArray(new String[0]));
            }

            csvWriter.write(file, StandardCharsets.UTF_8, data);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

            return fileName;
    }

    public String exportSubnetCsv(HttpServletRequest request,List<TraceOrgSubnetDetails> subnetDetailsList)
    {
        String fileName = ("Subnet Summary"+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".csv").replace(" ","_").replaceAll(":","_").replace(",","");

        try
        {
            File file = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR + fileName);

            CsvWriter csvWriter = new CsvWriter();

            Collection<String[]> data = new ArrayList<>();
            data.add(new String[] { "Category Name","Subnet Address","Subnet Mask","Subnet CIDR","Subnet Name" ,"VLAN Name","Location","Description","DNS Address","Scheduled Hours" });

            for(TraceOrgSubnetDetails traceOrgSubnetDetails:subnetDetailsList)
            {
                String vlanName = "";
                if(traceOrgSubnetDetails.getVlanName()!=null && !traceOrgSubnetDetails.getVlanName().isEmpty())
                {
                    vlanName = traceOrgSubnetDetails.getVlanName();
                }

                String location = "";
                if(traceOrgSubnetDetails.getLocation()!=null && !traceOrgSubnetDetails.getLocation().isEmpty())
                {
                    location = traceOrgSubnetDetails.getLocation();
                }

                String description = "";
                if(traceOrgSubnetDetails.getDescription()!=null && !traceOrgSubnetDetails.getDescription().isEmpty())
                {
                    description = traceOrgSubnetDetails.getDescription();
                }

                String dnsAddress = "";
                if(traceOrgSubnetDetails.getDnsAddress()!=null && !traceOrgSubnetDetails.getDnsAddress().isEmpty())
                {
                    dnsAddress = traceOrgSubnetDetails.getDnsAddress();
                }

                data.add(new String[] { traceOrgSubnetDetails.getTraceOrgCategory().getCategoryName(),traceOrgSubnetDetails.getSubnetAddress(),traceOrgSubnetDetails.getSubnetMask(),traceOrgSubnetDetails.getSubnetCidr().toString(),traceOrgSubnetDetails.getSubnetName(),vlanName,location,description,dnsAddress,traceOrgSubnetDetails.getScheduleHour().toString()});
            }

            csvWriter.write(file, StandardCharsets.UTF_8, data);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return fileName;
    }

    //SUBNET MASK TO CIDR

    public int convertNetmaskToCIDR(InetAddress netmask)
    {
        int cidr = 0;

        try
        {
            byte[] netmaskBytes = netmask.getAddress();

            boolean zero = false;

            for(byte b : netmaskBytes)
            {
                int mask = 0x80;

                for(int index = 0; index < 8; index++)
                {

                    int result = b & mask;

                    if(result == 0)
                    {
                        zero = true;
                    }
                    else if(zero)
                    {
                        throw new IllegalArgumentException("Invalid netmask.");
                    }
                    else
                    {
                        cidr++;
                    }
                    mask >>>= 1;
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return cidr;
    }

    public String exportSubnetUtilizationReportPdf(Integer exportTimeline)
    {
        String fileName = null;

        List<TraceOrgSubnetDetails> subnetDetailsList = null;

        try
        {
            switch (exportTimeline)
            {
                case 1 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where Date(modifiedDate) = CURDATE() ");
                    break;
                case 2 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where DATE(modifiedDate) = DATE(CURDATE() -1)");
                    break;
                case 3 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where  WEEK(modifiedDate) =  WEEK(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                case 4 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where   MONTH(modifiedDate)= MONTH(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                case 5 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where   QUARTER(modifiedDate) = QUARTER(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) ");
                    break;
                case 6 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where   QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) ");
                    break;
                case 7 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where  TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 ");
                    break;
                case 8 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where   YEAR(modifiedDate) = YEAR(curdate()) ");
                    break;
                case 9 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where   YEAR(modifiedDate) = YEAR(curdate()) - 1");
                    break;
                case 10 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);
                    break;
                case 11 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where  WEEK(modifiedDate) =  WEEK(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                case 12 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where  MONTH(modifiedDate)= MONTH(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                default:
                    subnetDetailsList = null;
                    break;
            }

            if(subnetDetailsList !=null && !subnetDetailsList.isEmpty())
            {
                HashMap<String, Object> gridReport = new HashMap<>();

                gridReport.put("Title", "Subnet Utilization Report");
                try
                {

                    LinkedHashSet<String> columns = new LinkedHashSet<String>()
                    {{
                        add("Subnet Address");

                        add("Subnet Type");

                        add("Subnet Name");

                        add("All IP");

                        add("Used IP");

                        add("Avaliable IP");

                        add("Transient IP");

                        add("% In Space  Used");

                    }};

                    List<Object> pdfResults = new ArrayList<>();

                    List<Object> pdfResult;

                    String subnetAddress = null;

                    for (TraceOrgSubnetDetails traceOrgSubnetDetail : subnetDetailsList)
                    {
                        pdfResult = new ArrayList<>();

                        pdfResult.add(traceOrgSubnetDetail.getSubnetAddress());

                        pdfResult.add(traceOrgSubnetDetail.getType());

                        pdfResult.add(traceOrgSubnetDetail.getSubnetName());

                        pdfResult.add(traceOrgSubnetDetail.getTotalIp());

                        pdfResult.add(traceOrgSubnetDetail.getUsedIp());

                        pdfResult.add(traceOrgSubnetDetail.getAvailableIp());

                        pdfResult.add(traceOrgSubnetDetail.getTransientIp());

                        pdfResult.add(traceOrgSubnetDetail.getUsedIpPercentage());

                        pdfResults.add(pdfResult);
                    }
                    HashMap<String, Object> results = new HashMap<>();

                    results.put("grid-result", pdfResults);

                    results.put("columns", columns);

                    List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                    visualizationResults.add(results);

                    gridReport.put("Title", "Subnet Utilization Report ");

                    fileName = "Subnet Utilization Report " + TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date()) + ".pdf";

                    fileName = fileName.replace(" ", "_").replace(":", "_").replace(",", "");

                    TraceOrgPDFBuilder.addGridReport(1, visualizationResults, new HashMap<String, Object>(), fileName, gridReport);
                }
                catch (Exception exception)
                {
                    _logger.error(exception);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return fileName;
    }


    public String exportSubnetUtilizationReportCsv(Integer exportTimeline)
    {
        String fileName = null;

        List<TraceOrgSubnetDetails> subnetDetailsList = null;

        try
        {
            switch (exportTimeline)
            {
                case 1 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where Date(modifiedDate) = CURDATE() ");
                    break;
                case 2 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where DATE(modifiedDate) = DATE(CURDATE() -1)");
                    break;
                case 3 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where  WEEK(modifiedDate) =  WEEK(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                case 4 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where   MONTH(modifiedDate)= MONTH(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                case 5 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where   QUARTER(modifiedDate) = QUARTER(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) ");
                    break;
                case 6 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where   QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) ");
                    break;
                case 7 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where  TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 ");
                    break;
                case 8 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where   YEAR(modifiedDate) = YEAR(curdate()) ");
                    break;
                case 9 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where   YEAR(modifiedDate) = YEAR(curdate()) - 1");
                    break;
                case 10 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);
                    break;
                case 11 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where  WEEK(modifiedDate) =  WEEK(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                case 12 :
                    subnetDetailsList = (List<TraceOrgSubnetDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where  MONTH(modifiedDate)= MONTH(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                default:
                    subnetDetailsList = null;
                    break;
            }

            if(subnetDetailsList !=null && !subnetDetailsList.isEmpty())
            {
                try
                {
                    fileName = "SUBNET_UTLIZATION_"+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".csv";

                    File file = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR +fileName);

                    CsvWriter csvWriter = new CsvWriter();

                    Collection<String[]> data = new ArrayList<>();

                    data.add(new String[] { "Subnet Address","Subnet Type","Subnet Name","All IP","Used IP","Available IP","Transient IP","% In Space  Used"});

                    for(TraceOrgSubnetDetails traceOrgSubnetDetails:subnetDetailsList)
                    {
                        data.add(new String[] { traceOrgSubnetDetails.getSubnetAddress(),traceOrgSubnetDetails.getType(),traceOrgSubnetDetails.getSubnetName(),traceOrgSubnetDetails.getTotalIp().toString(),traceOrgSubnetDetails.getUsedIp().toString(),traceOrgSubnetDetails.getAvailableIp().toString(),traceOrgSubnetDetails.getTransientIp().toString(),""+traceOrgSubnetDetails.getUsedIpPercentage()});
                    }
                    csvWriter.write(file, StandardCharsets.UTF_8, data);
                }
                catch (Exception exception)
                {
                    _logger.error(exception);
                }

            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return fileName;
    }


    public String exportDHCPUtilizationReportPdf(Integer exportTimeline)
    {
        String fileName = null;

        List<TraceOrgDhcpCredentialDetails> dhcpCredentialDetailsList = null;

        try
        {
            switch (exportTimeline)
            {
                case 1 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where Date(modifiedDate) = CURDATE() ");
                    break;
                case 2 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where DATE(modifiedDate) = DATE(CURDATE() -1)");
                    break;
                case 3 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where  WEEK(modifiedDate) =  WEEK(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                case 4 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where   MONTH(modifiedDate)= MONTH(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                case 5 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where   QUARTER(modifiedDate) = QUARTER(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) ");
                    break;
                case 6 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where   QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) ");
                    break;
                case 7 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where  TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 ");
                    break;
                case 8 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where   YEAR(modifiedDate) = YEAR(curdate()) ");
                    break;
                case 9 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where   YEAR(modifiedDate) = YEAR(curdate()) - 1");
                    break;
                case 10 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL);
                    break;
                case 11 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where  WEEK(modifiedDate) =  WEEK(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                case 12 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where  MONTH(modifiedDate)= MONTH(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                default:
                    dhcpCredentialDetailsList = null;
                    break;
            }

            if(dhcpCredentialDetailsList !=null && !dhcpCredentialDetailsList.isEmpty())
            {
                HashMap<String, Object> gridReport = new HashMap<>();

                gridReport.put("Title", "DHCP Server Utilization Report");
                try
                {
                    LinkedHashSet<String> columns = new LinkedHashSet<String>()
                    {{
                        add("Host Address");

                        add("Server Type");

                        add("Total Scopes");

                        add("Declines");

                        add("Request");

                        add("Releases");

                        add("Naks");

                        add("Offers");

                        add("Discovers");

                        add("Ack");

                    }};

                    List<Object> pdfResults = new ArrayList<>();

                    List<Object> pdfResult;

                    String subnetAddress = null;

                    for (TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails : dhcpCredentialDetailsList)
                    {
                        TraceOrgDhcpUtilization traceOrgDhcpUtilization = (TraceOrgDhcpUtilization)traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_DHCP_UTILIZATION,traceOrgDhcpCredentialDetails.getId());

                        if(traceOrgDhcpUtilization !=null)
                        {
                            pdfResult = new ArrayList<>();

                            pdfResult.add(traceOrgDhcpCredentialDetails.getHostAddress());

                            pdfResult.add(traceOrgDhcpCredentialDetails.getType());

                            pdfResult.add(traceOrgDhcpUtilization.getAddressScopes());

                            pdfResult.add(traceOrgDhcpUtilization.getDeclines());

                            pdfResult.add(traceOrgDhcpUtilization.getRequests());

                            pdfResult.add(traceOrgDhcpUtilization.getReleases());

                            pdfResult.add(traceOrgDhcpUtilization.getNaks());

                            pdfResult.add(traceOrgDhcpUtilization.getOffers());

                            pdfResult.add(traceOrgDhcpUtilization.getDiscovers());

                            pdfResult.add(traceOrgDhcpUtilization.getAcks());

                            pdfResults.add(pdfResult);
                        }
                    }
                    HashMap<String, Object> results = new HashMap<>();

                    results.put("grid-result", pdfResults);

                    results.put("columns", columns);

                    List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                    visualizationResults.add(results);

                    gridReport.put("Title", "DHCP Server Utilization Report ");

                    fileName = "DHCP Server Utilization Report " + TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date()) + ".pdf";

                    fileName = fileName.replace(" ", "_").replace(":", "_").replace(",", "");

                    TraceOrgPDFBuilder.addGridReport(1, visualizationResults, new HashMap<String, Object>(), fileName, gridReport);
                }
                catch (Exception exception)
                {
                    _logger.error(exception);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return fileName;
    }


    public String exportDHCPUtilizationReportCsv(Integer exportTimeline)
    {
        String fileName = null;

        List<TraceOrgDhcpCredentialDetails> dhcpCredentialDetailsList = null;

        try
        {
            switch (exportTimeline)
            {
                case 1 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where Date(modifiedDate) = CURDATE() ");
                    break;
                case 2 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where DATE(modifiedDate) = DATE(CURDATE() -1)");
                    break;
                case 3 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where  WEEK(modifiedDate) =  WEEK(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                case 4 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where   MONTH(modifiedDate)= MONTH(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                case 5 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where   QUARTER(modifiedDate) = QUARTER(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) ");
                    break;
                case 6 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where   QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) ");
                    break;
                case 7 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where  TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 ");
                    break;
                case 8 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where   YEAR(modifiedDate) = YEAR(curdate()) ");
                    break;
                case 9 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where   YEAR(modifiedDate) = YEAR(curdate()) - 1");
                    break;
                case 10 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL);
                    break;
                case 11 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where  WEEK(modifiedDate) =  WEEK(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                case 12 :
                    dhcpCredentialDetailsList = (List<TraceOrgDhcpCredentialDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where  MONTH(modifiedDate)= MONTH(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE())");
                    break;
                default:
                    dhcpCredentialDetailsList = null;
                    break;
            }

            if(dhcpCredentialDetailsList !=null && !dhcpCredentialDetailsList.isEmpty())
            {
                try
                {
                    fileName = "DHCP_UTLIZATION_"+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".csv";

                    File file = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR +fileName);

                    CsvWriter csvWriter = new CsvWriter();

                    Collection<String[]> data = new ArrayList<>();

                    data.add(new String[] { "Host Address", "Server Type", "Total Scopes", "Declines", "Request", "Releases", "Naks", "Offers", "Discovers", "Ack"});

                    for(TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails:dhcpCredentialDetailsList)
                    {
                        TraceOrgDhcpUtilization traceOrgDhcpUtilization = (TraceOrgDhcpUtilization)traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_DHCP_UTILIZATION,traceOrgDhcpCredentialDetails.getId());

                        if(traceOrgDhcpUtilization !=null)
                        {
                            data.add(new String[] { traceOrgDhcpCredentialDetails.getHostAddress(), traceOrgDhcpCredentialDetails.getType(), traceOrgDhcpUtilization.getAddressScopes(), traceOrgDhcpUtilization.getDeclines(), traceOrgDhcpUtilization.getRequests(), traceOrgDhcpUtilization.getReleases(), traceOrgDhcpUtilization.getNaks(), traceOrgDhcpUtilization.getOffers(), traceOrgDhcpUtilization.getDiscovers(), traceOrgDhcpUtilization.getAcks()});
                        }

                    }
                    csvWriter.write(file, StandardCharsets.UTF_8, data);
                }
                catch (Exception exception)
                {
                    _logger.error(exception);
                }

            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return fileName;
    }

    public String exportVendorSummaryReportPdf(List< Map<String,Object>> vendorSummaryList,String ipStatus)
    {
        String fileName = null;

        HashMap<String, Object> gridReport = new HashMap<>();

        List<HashMap<String, Object>> ipSummaryObject = new ArrayList<>();

        try
        {
            if(vendorSummaryList !=null && !vendorSummaryList.isEmpty())
            {
                gridReport.put("Title","Vendor Summary Report" );

                LinkedHashSet<String> columns = new LinkedHashSet<String>()
                {{

                    add("Vendor Name");

                    add("Vendor Count");

                    add("Percentage");

                }};

                List<Object> pdfResults = new ArrayList<>();

                List<Object> pdfResult = new ArrayList<>();

                HashMap<String, Object> results = new HashMap<>();

                for (Map<String,Object> vendorDetails : vendorSummaryList)
                {
                    pdfResult = new ArrayList<>();

                    pdfResult.add(vendorDetails.get(TraceOrgCommonConstants.VENDOR_NAME));

                    pdfResult.add(vendorDetails.get(TraceOrgCommonConstants.VENDOR_COUNT));

                    pdfResult.add(vendorDetails.get(TraceOrgCommonConstants.VENDOR_PERCENTAGE));

                    pdfResults.add(pdfResult);
                }

                results.put("grid-result", pdfResults);

                results.put("columns", columns);

                results.put("logFor", "VENDOR_REPORT");

                List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                visualizationResults.add(results);

                fileName = ipStatus.toUpperCase()+" Report " + TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date()) + ".pdf";

                fileName = fileName.replace(" ", "_").replace(":", "_").replace(",", "");

                TraceOrgPDFBuilder.addGridReport(1, visualizationResults, new HashMap<String, Object>(), fileName, gridReport);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return fileName;
    }


    /**
     * IPAM-145 : System should have rogue device detection capability
     * Change the column rogue to authenticity.
     * @param traceOrgSubnetIpDetails
     * @param ipStatus
     * @return
     */
    public String exportIpReportPdf(List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetails,String ipStatus)
    {
        String fileName = null;

        HashMap<String, Object> gridReport = new HashMap<>();

        List<HashMap<String, Object>> ipSummaryObject = new ArrayList<>();

        Integer availableIp = 0;

        Integer usedIp = 0;

        Integer transientIp = 0;

        try
        {
            if(traceOrgSubnetIpDetails !=null && !traceOrgSubnetIpDetails.isEmpty())
            {
                gridReport.put("Title",ipStatus+" IP Report" );

                Collection<String[]> data = new ArrayList<>();

                List<TraceOrgCustomColumn> customColumn=traceOrgCustomColumnRepository.findByColumnAt("subnetIp");

                List<String> customColumnNames = customColumn.stream()
                        .map(TraceOrgCustomColumn::getColumnName)
                        .collect(Collectors.toList());

                List<String> headers = new ArrayList<>(Arrays.asList(
                        "IP Address","Status","Scope","Mac Address","Vendor","IP To DNS","DNS To IP","Authenticity","Last Alive Time"
                ));

                headers.addAll(customColumnNames);

                List<Object> pdfResults = new ArrayList<>();

                List<Object> pdfResult = new ArrayList<>();

                HashMap<String, Object> results = new HashMap<>();

                for (TraceOrgSubnetIpDetails traceOrgSubnetIpDetail : traceOrgSubnetIpDetails)
                {
                    JsonNode customColumnNode=traceOrgSubnetIpDetail.getCustomColumns();

                    if(traceOrgSubnetIpDetail.getStatus().equalsIgnoreCase(TraceOrgCommonConstants.AVAILABLE))
                    {
                        availableIp++;
                    }
                    else if(traceOrgSubnetIpDetail.getStatus().equalsIgnoreCase(TraceOrgCommonConstants.USED))
                    {
                        usedIp++;
                    }
                    else if(traceOrgSubnetIpDetail.getStatus().equalsIgnoreCase(TraceOrgCommonConstants.TRANSIENT))
                    {
                        transientIp++;
                    }

                    pdfResult = new ArrayList<>();

                    pdfResult.add(traceOrgSubnetIpDetail.getIpAddress());

                    pdfResult.add(traceOrgSubnetIpDetail.getStatus());

                    pdfResult.add(traceOrgSubnetIpDetail.getSubnetId().getSubnetName());

                    pdfResult.add(traceOrgSubnetIpDetail.getMacAddress());

                    pdfResult.add(traceOrgSubnetIpDetail.getDeviceType());

                    pdfResult.add(traceOrgSubnetIpDetail.getIpToDns());

                    pdfResult.add(traceOrgSubnetIpDetail.getDnsToIp());

                    pdfResult.add(traceOrgSubnetIpDetail.getAuthenticity());

                    pdfResult.add(traceOrgSubnetIpDetail.getLastAliveTime());

                    Iterator<String> fieldNames = customColumnNode.fieldNames();

                    while (fieldNames.hasNext()) {
                        String key = fieldNames.next();
                        pdfResult.add(customColumnNode.get(key).asText());
                    }

                    pdfResults.add(pdfResult);
                }

                if(ipStatus.equalsIgnoreCase("All IP"))
                {
                    HashMap<String, Object> availableIpSummary = new HashMap<>();

                    availableIpSummary.put("status","Available (%)");

                    availableIpSummary.put("value",new DecimalFormat("#.00").format((double)(availableIp*100)/traceOrgSubnetIpDetails.size()));

                    HashMap<String, Object> usedIpSummary = new HashMap<>();

                    usedIpSummary.put("status","Used (%)");

                    usedIpSummary.put("value",new DecimalFormat("#.00").format((double)(usedIp*100)/traceOrgSubnetIpDetails.size()));

                    HashMap<String, Object> transientIpSummary = new HashMap<>();

                    transientIpSummary.put("status","Transient (%)");

                    transientIpSummary.put("value",new DecimalFormat("#.00").format((double)(transientIp*100)/traceOrgSubnetIpDetails.size()));

                    ipSummaryObject.add(availableIpSummary);

                    ipSummaryObject.add(usedIpSummary);

                    ipSummaryObject.add(transientIpSummary);

                    results.put("ipSummary", ipSummaryObject);
                }

                results.put("grid-result", pdfResults);

                results.put("columns", headers);

                results.put("logFor", "IP_REPORT");

                List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                visualizationResults.add(results);

                fileName = ipStatus+" IP Report " + TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date()) + ".pdf";

                fileName = fileName.replace(" ", "_").replace(":", "_").replace(",", "");

                TraceOrgPDFBuilder.addGridReport(1, visualizationResults, new HashMap<String, Object>(), fileName, gridReport);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return fileName;
    }

    public String exportVendorSummaryReportCsv(List<Map<String,Object>> vendorSummaryList,String ipStatus)
    {
        String fileName = null;

        if(vendorSummaryList !=null && !vendorSummaryList.isEmpty())
        {
            try
            {
                fileName =  ipStatus.toUpperCase()+ "_" +TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".csv";

                fileName = fileName.replace(" ", "_").replace(":", "_").replace(",", "");

                File file = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR +fileName);

                CsvWriter csvWriter = new CsvWriter();

                Collection<String[]> data = new ArrayList<>();

                data.add(new String[] { "Vendor Name","Vendor Count","Percentage"});

                for(Map<String ,Object> vendorDetail:vendorSummaryList)
                {
                    data.add(new String[] { (String)vendorDetail.get(TraceOrgCommonConstants.VENDOR_NAME),vendorDetail.get(TraceOrgCommonConstants.VENDOR_COUNT).toString(),vendorDetail.get(TraceOrgCommonConstants.VENDOR_PERCENTAGE).toString()});
                }

                csvWriter.write(file, StandardCharsets.UTF_8, data);
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }

        }
        return fileName;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Create one common method for export pdf and csv based on ipStatus.
     * @param result
     * @param subnetId
     * @param ipStatus
     * @param exportTimeline
     * @param pdfReport
     */
    public void exportSubnetIpReportByTimeline(HashMap<String, Object> result, String subnetId, String ipStatus, Integer exportTimeline, boolean pdfReport)
    {
        try
        {
            List<TraceOrgSubnetIpDetails> subnetIpDetailsList = null ;

            List<Map<String,Object>> vendorSummaryList = new ArrayList<>() ;

            switch (ipStatus.toUpperCase())
            {
                case "USED IP" :
                    ipStatus = TraceOrgCommonConstants.USED;
                    break;
                case "AVAILABLE IP" :
                    ipStatus = TraceOrgCommonConstants.AVAILABLE;
                    break;
                case "RESERVED IP" :
                    ipStatus = TraceOrgCommonConstants.RESERVED;
                    break;
                case "TRANSIENT IP" :
                    ipStatus = TraceOrgCommonConstants.TRANSIENT;
                    break;
            }

            if(ipStatus.equals(TraceOrgCommonConstants.USED) || ipStatus.equals(TraceOrgCommonConstants.AVAILABLE) || ipStatus.equals(TraceOrgCommonConstants.TRANSIENT) || ipStatus.equals(TraceOrgCommonConstants.RESERVED))
            {
                switch (exportTimeline)
                {
                    case 1 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where Date(modifiedDate) = CURDATE() and  deactiveStatus = false and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress) ");
                        break;
                    case 2 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where DATE(modifiedDate) = DATE(CURDATE() -1) and  deactiveStatus = false and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress) ");
                        break;
                    case 3 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress) ");
                        break;
                    case 4 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress) ");
                        break;
                    case 5 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                        break;
                    case 6 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                        break;
                    case 7 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 and  deactiveStatus = false and subnetId in ("+subnetId+") and status= '"+ipStatus+"'  order by INET_ATON(ipAddress)");
                        break;
                    case 8 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(YEAR, modifiedDate, NOW()) < 1 and  deactiveStatus = false and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                        break;
                    case 9 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where YEAR(modifiedDate) = YEAR(curdate()) - 1 and  deactiveStatus = false and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                        break;
                    case 10 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where subnetId in ("+subnetId+") and  deactiveStatus = false and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                        break;
                    case 11 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                        break;
                    case 12 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                        break;
                    default:
                        subnetIpDetailsList = null;
                        break;
                }
            }
            else if(ipStatus.equalsIgnoreCase("ALL IP"))
            {
                switch (exportTimeline)
                {
                    case 1 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where Date(modifiedDate) = CURDATE() and  deactiveStatus = false and subnetId in ("+subnetId+") order by INET_ATON(ipAddress) ");
                        break;
                    case 2 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where DATE(modifiedDate) = DATE(CURDATE() -1) and  deactiveStatus = false and subnetId in ("+subnetId+") order by INET_ATON(ipAddress) ");
                        break;
                    case 3 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") order by INET_ATON(ipAddress) ");
                        break;
                    case 4 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") order by INET_ATON(ipAddress) ");
                        break;
                    case 5 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") order by INET_ATON(ipAddress)");
                        break;
                    case 6 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") order by INET_ATON(ipAddress)");
                        break;
                    case 7 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 and  deactiveStatus = false and subnetId in ("+subnetId+")  order by INET_ATON(ipAddress)");
                        break;
                    case 8 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(YEAR, modifiedDate, NOW()) < 1 and  deactiveStatus = false and subnetId in ("+subnetId+")  order by INET_ATON(ipAddress)");
                        break;
                    case 9 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where YEAR(modifiedDate) = YEAR(curdate()) - 1 and  deactiveStatus = false and subnetId in ("+subnetId+") order by INET_ATON(ipAddress)");
                        break;
                    case 10 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where subnetId in ("+subnetId+") and  deactiveStatus = false order by INET_ATON(ipAddress)");
                        break;
                    case 11 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") order by INET_ATON(ipAddress)");
                        break;
                    case 12 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") order by INET_ATON(ipAddress)");
                        break;
                    default:
                        subnetIpDetailsList = null;
                        break;
                }
            }
            else if (ipStatus.equalsIgnoreCase("ROGUE IP"))
            {
                switch (exportTimeline)
                {
                    case 1 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where Date(modifiedDate) = CURDATE() and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'rogue'  order by INET_ATON(ipAddress) ");
                        break;
                    case 2 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where DATE(modifiedDate) = DATE(CURDATE() -1) and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'rogue'  order by INET_ATON(ipAddress) ");
                        break;
                    case 3 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress) ");
                        break;
                    case 4 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress) ");
                        break;
                    case 5 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress)");
                        break;
                    case 6 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress)");
                        break;
                    case 7 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'rogue'  order by INET_ATON(ipAddress)");
                        break;
                    case 8 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(YEAR, modifiedDate, NOW()) < 1 and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'rogue'  order by INET_ATON(ipAddress)");
                        break;
                    case 9 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where YEAR(modifiedDate) = YEAR(curdate()) - 1 and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress)");
                        break;
                    case 10 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where subnetId in ("+subnetId+") and  deactiveStatus = false and authenticity = 'rogue' order by INET_ATON(ipAddress)");
                        break;
                    case 11 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress)");
                        break;
                    case 12 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress)");
                        break;
                    default:
                        subnetIpDetailsList = null;
                        break;
                }
            }
            else if (ipStatus.equalsIgnoreCase("TRUSTED IP"))
            {
                switch (exportTimeline)
                {
                    case 1 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where Date(modifiedDate) = CURDATE() and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'trusted'  order by INET_ATON(ipAddress) ");
                        break;
                    case 2 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where DATE(modifiedDate) = DATE(CURDATE() -1) and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'trusted'  order by INET_ATON(ipAddress) ");
                        break;
                    case 3 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress) ");
                        break;
                    case 4 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress) ");
                        break;
                    case 5 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress)");
                        break;
                    case 6 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress)");
                        break;
                    case 7 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'trusted'  order by INET_ATON(ipAddress)");
                        break;
                    case 8 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(YEAR, modifiedDate, NOW()) < 1 and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'trusted'  order by INET_ATON(ipAddress)");
                        break;
                    case 9 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where YEAR(modifiedDate) = YEAR(curdate()) - 1 and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress)");
                        break;
                    case 10 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where subnetId in ("+subnetId+") and  deactiveStatus = false and authenticity = 'trusted' order by INET_ATON(ipAddress)");
                        break;
                    case 11 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress)");
                        break;
                    case 12 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress)");
                        break;
                    default:
                        subnetIpDetailsList = null;
                        break;
                }
            }
            else if(ipStatus.equalsIgnoreCase("VENDOR SUMMARY"))
            {
                List<Object> vendorList = null;

                switch (exportTimeline)
                {
                    case 1 :
                        vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and Date(modifiedDate) = CURDATE() and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                        break;
                    case 2 :
                        vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and DATE(modifiedDate) = DATE(CURDATE() -1) and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                        break;
                    case 3 :
                        vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and WEEK(modifiedDate) =  WEEK(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                        break;
                    case 4 :
                        vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and MONTH(modifiedDate)= MONTH(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                        break;
                    case 5 :
                        vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and QUARTER(modifiedDate) = QUARTER(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                        break;
                    case 6 :
                        vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                        break;
                    case 7 :
                        vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6  group by deviceType order by devicenumber desc");
                        break;
                    case 8 :
                        vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and TIMESTAMPDIFF(YEAR, modifiedDate, NOW()) < 1 group by deviceType order by devicenumber desc");
                        break;
                    case 9 :
                        vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and YEAR(modifiedDate) = YEAR(curdate()) - 1 group by deviceType order by devicenumber desc");
                        break;
                    case 10 :
                        vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  group by deviceType order by devicenumber desc");
                        break;
                    case 11 :
                        vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and WEEK(modifiedDate) =  WEEK(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                        break;
                    case 12 :
                        vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and MONTH(modifiedDate)= MONTH(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                        break;
                    default:
                        vendorList = null;
                        break;
                }

                if(vendorList != null && !vendorList.isEmpty())
                {
                    Integer totalCount = 0;

                    for(Object vendorOutputs : vendorList)
                    {
                        Map<String,Object> vendorDetails = new HashMap<>();

                        Gson gson= new Gson();

                        Type listType = new TypeToken<List<String>>() {}.getType();

                        List<String> vendorOutputsList = gson.fromJson(gson.toJson(vendorOutputs), listType);

                        totalCount = totalCount + Integer.parseInt(vendorOutputsList.get(0));
                    }

                    for(Object vendorOutputs : vendorList)
                    {
                        Map<String,Object> vendorDetails = new HashMap<>();

                        Gson gson= new Gson();

                        Type listType = new TypeToken<List<String>>() {}.getType();

                        List<String> vendorOutputsList = gson.fromJson(gson.toJson(vendorOutputs), listType);

                        DecimalFormat decimalFormat = new DecimalFormat();

                        decimalFormat.setMaximumFractionDigits(2);

                        if(vendorOutputsList !=null && !vendorOutputsList.isEmpty())
                        {
                            if(vendorOutputsList.get(1) == null)
                            {
                                vendorDetails.put(TraceOrgCommonConstants.VENDOR_NAME,"Others");
                            }
                            else
                            {
                                vendorDetails.put(TraceOrgCommonConstants.VENDOR_NAME,vendorOutputsList.get(1));
                            }
                            vendorDetails.put(TraceOrgCommonConstants.VENDOR_COUNT,Long.parseLong(vendorOutputsList.get(0)));

                            vendorDetails.put(TraceOrgCommonConstants.VENDOR_PERCENTAGE,decimalFormat.format((float)(Long.parseLong(vendorOutputsList.get(0))*100)/totalCount));
                        }
                        if(vendorDetails!=null && !vendorDetails.isEmpty())
                        {
                            vendorSummaryList.add(vendorDetails);
                        }
                    }
                }
            }

            if(!ipStatus.equalsIgnoreCase("VENDOR SUMMARY"))
            {
                if(subnetIpDetailsList!=null && !subnetIpDetailsList.isEmpty())
                {
                    if(pdfReport)
                    {
                        result.put(TraceOrgCommonConstants.DATA, exportIpReportPdf(subnetIpDetailsList,ipStatus));
                    }
                    else
                    {
                        result.put(TraceOrgCommonConstants.DATA, exportIpReportCsv(subnetIpDetailsList,ipStatus));
                    }

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.NO_DATA_AVAILABLE);
                }
            }
            else
            {
                if(vendorSummaryList!=null && !vendorSummaryList.isEmpty())
                {
                    if(pdfReport)
                    {
                        result.put(TraceOrgCommonConstants.DATA, traceOrgCommonUtil.exportVendorSummaryReportPdf(vendorSummaryList,ipStatus));
                    }
                    else
                    {
                        result.put(TraceOrgCommonConstants.DATA, traceOrgCommonUtil.exportVendorSummaryReportCsv(vendorSummaryList,ipStatus));
                    }

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.NO_DATA_AVAILABLE);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Change the column rogue to authenticity.
     * @param traceOrgSubnetIpDetails
     * @param ipStatus
     * @return
     */
    public String exportIpReportCsv(List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetails,String ipStatus)
    {
        String fileName = null;

        if(traceOrgSubnetIpDetails !=null && !traceOrgSubnetIpDetails.isEmpty())
        {
            try
            {
                fileName =  ipStatus.toUpperCase()+ "_" +TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".csv";

                fileName = fileName.replace(" ", "_").replace(":", "_").replace(",", "");

                File file = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR +fileName);

                CsvWriter csvWriter = new CsvWriter();

                Collection<String[]> data = new ArrayList<>();

                List<TraceOrgCustomColumn> customColumn=traceOrgCustomColumnRepository.findByColumnAt("subnetIp");

                List<String> customColumnNames = customColumn.stream()
                        .map(TraceOrgCustomColumn::getColumnName)
                        .collect(Collectors.toList());

                List<String> headers = new ArrayList<>(Arrays.asList(
                        "IP Address","Status","Scope","Mac Address","Vendor","IP To DNS","DNS To IP","Authenticity","Last Alive Time"
                ));

                headers.addAll(customColumnNames);

                data.add(headers.toArray(new String[0]));

                ObjectMapper objectMapper = new ObjectMapper();

                for (TraceOrgSubnetIpDetails subnetIpDetails : traceOrgSubnetIpDetails)
                {
                    List<String> rowData = new ArrayList<>();
                    rowData.add(subnetIpDetails.getIpAddress());
                    rowData.add(subnetIpDetails.getStatus());
                    rowData.add(subnetIpDetails.getSubnetId().getSubnetName());
                    rowData.add(subnetIpDetails.getMacAddress());
                    rowData.add(subnetIpDetails.getDeviceType());
                    rowData.add(subnetIpDetails.getIpToDns());
                    rowData.add(subnetIpDetails.getDnsToIp());
                    rowData.add(subnetIpDetails.getAuthenticity());
                    rowData.add(subnetIpDetails.getLastAliveTime());

                    String customColumnJson = String.valueOf(subnetIpDetails.getCustomColumns());

                    try {
                        JsonNode customColumnJsonNode = objectMapper.readTree(customColumnJson);
                        customColumnJsonNode.fields().forEachRemaining(entry -> rowData.add(entry.getValue().asText()));
                    } catch (Exception e) {
                        _logger.error(e);
                    }

                    data.add(rowData.toArray(new String[0]));
                }

                csvWriter.write(file, StandardCharsets.UTF_8, data);
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }

        }
        return fileName;
    }

    public String exportAllEventReportPdf(Integer exportTimeline)
    {
        String fileName = null;

        List<TraceOrgEvent> traceOrgEventList = null;

        try
        {
            switch (exportTimeline)
            {
                case 1 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  Date(timestamp) = CURDATE() order by id desc");
                    break;
                case 2 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where DATE(timestamp) = DATE(CURDATE() -1) order by id desc");
                    break;
                case 3 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where WEEK(timestamp) =  WEEK(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 4 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where MONTH(timestamp)= MONTH(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 5 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  QUARTER(timestamp) = QUARTER(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 6 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where QUARTER(timestamp) = QUARTER(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 7 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where TIMESTAMPDIFF(MONTH, timestamp, NOW()) < 6  order by id desc");
                    break;
                case 8 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where YEAR(timestamp) = YEAR(curdate()) order by id desc ");
                    break;
                case 9 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where YEAR(timestamp) = YEAR(curdate()) - 1 order by id desc");
                    break;
                case 10 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT +" order by id desc" );
                    break;
                case 11 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  WEEK(timestamp) =  WEEK(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 12 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  MONTH(timestamp)= MONTH(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                default:
                    traceOrgEventList = null;
                    break;
            }

            if(traceOrgEventList !=null && !traceOrgEventList.isEmpty())
            {
                HashMap<String, Object> gridReport = new HashMap<>();

                gridReport.put("Title", "Event Log Report");
                try
                {

                    LinkedHashSet<String> columns = new LinkedHashSet<String>()
                    {{
                        add("Event Type");

                        add("Event Context");

                        add("Time");

                        add("Username");

                    }};

                    List<Object> pdfResults = new ArrayList<>();

                    List<Object> pdfResult;

                    for (TraceOrgEvent traceOrgEvent : traceOrgEventList)
                    {
                        pdfResult = new ArrayList<>();

                        pdfResult.add(traceOrgEvent.getEventType());

                        pdfResult.add(traceOrgEvent.getEventContext());

                        pdfResult.add(traceOrgEvent.getTimestamp());

                        if (traceOrgEvent.getDoneBy() != null)
                        {
                            pdfResult.add(traceOrgEvent.getDoneBy().getUserName());
                        }
                        else
                        {
                            pdfResult.add(" ");
                        }

                        pdfResults.add(pdfResult);
                    }

                    HashMap<String, Object> results = new HashMap<>();

                    results.put("grid-result", pdfResults);

                    results.put("columns", columns);

                    results.put("logFor","EVENT");

                    List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                    visualizationResults.add(results);

                    gridReport.put("Title", "Event Log Report ");

                    fileName = "Event Log Report " + TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date()) + ".pdf";

                    fileName = fileName.replace(" ", "_").replace(":", "_").replace(",", "");

                    TraceOrgPDFBuilder.addGridReport(1, visualizationResults, new HashMap<String, Object>(), fileName, gridReport);
                }
                catch (Exception exception)
                {
                    _logger.error(exception);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return fileName;
    }

    public String exportAllEventReportCsv(Integer exportTimeline)
    {
        String fileName = null;

        List<TraceOrgEvent> traceOrgEventList = null;

        try
        {
            switch (exportTimeline)
            {
                case 1 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  Date(timestamp) = CURDATE() order by id desc");
                    break;
                case 2 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where DATE(timestamp) = DATE(CURDATE() -1) order by id desc");
                    break;
                case 3 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where WEEK(timestamp) =  WEEK(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 4 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where MONTH(timestamp)= MONTH(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 5 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  QUARTER(timestamp) = QUARTER(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 6 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where QUARTER(timestamp) = QUARTER(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 7 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where TIMESTAMPDIFF(MONTH, timestamp, NOW()) < 6  order by id desc");
                    break;
                case 8 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where YEAR(timestamp) = YEAR(curdate()) order by id desc ");
                    break;
                case 9 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where YEAR(timestamp) = YEAR(curdate()) - 1 order by id desc");
                    break;
                case 10 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT +" order by id desc" );
                    break;
                case 11 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  WEEK(timestamp) =  WEEK(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 12 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  MONTH(timestamp)= MONTH(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                default:
                    traceOrgEventList = null;
                    break;
            }

            if(traceOrgEventList !=null && !traceOrgEventList.isEmpty())
            {
                try
                {
                    fileName = "Event_"+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".csv";

                    fileName = fileName.replace(" ", "_").replace(":", "_").replace(",", "");

                    File file = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR +fileName);

                    CsvWriter csvWriter = new CsvWriter();

                    Collection<String[]> data = new ArrayList<>();

                    data.add(new String[] { "Event Type","Event Context","Time","Username"});

                    for(TraceOrgEvent traceOrgEvent:traceOrgEventList)
                    {
                        if (traceOrgEvent.getDoneBy() != null)
                        {
                            data.add(new String[] { traceOrgEvent.getEventType(),traceOrgEvent.getEventContext(),traceOrgEvent.getTimestamp(),traceOrgEvent.getDoneBy().getUserName()});
                        }
                        else
                        {
                            data.add(new String[] { traceOrgEvent.getEventType(),traceOrgEvent.getEventContext(),traceOrgEvent.getTimestamp(),"-"});
                        }

                    }
                    csvWriter.write(file, StandardCharsets.UTF_8, data);
                }
                catch (Exception exception)
                {
                    _logger.error(exception);
                }

            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return fileName;
    }


    public String exportAllConflictIpReportPdf(Integer exportTimeline)
    {
        String fileName = null;

        List<TraceOrgEvent> traceOrgEventList = null;

        try
        {
            switch (exportTimeline)
            {
                case 1 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and Date(timestamp) = CURDATE() order by id desc");
                    break;
                case 2 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and DATE(timestamp) = DATE(CURDATE() -1) order by id desc");
                    break;
                case 3 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and WEEK(timestamp) =  WEEK(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 4 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and MONTH(timestamp)= MONTH(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 5 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  eventType ='Conflict IP'  and QUARTER(timestamp) = QUARTER(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 6 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and QUARTER(timestamp) = QUARTER(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 7 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and TIMESTAMPDIFF(MONTH, timestamp, NOW()) < 6  order by id desc");
                    break;
                case 8 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and YEAR(timestamp) = YEAR(curdate()) order by id desc ");
                    break;
                case 9 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and YEAR(timestamp) = YEAR(curdate()) - 1 order by id desc");
                    break;
                case 10 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT +" where eventType ='Conflict IP' order by id desc" );
                    break;
                case 11 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and  WEEK(timestamp) =  WEEK(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 12 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and  MONTH(timestamp)= MONTH(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                default:
                    traceOrgEventList = null;
                    break;
            }

            if(traceOrgEventList !=null && !traceOrgEventList.isEmpty())
            {
                HashMap<String, Object> gridReport = new HashMap<>();

                gridReport.put("Title", "Conflict Ip Report");
                try
                {

                    LinkedHashSet<String> columns = new LinkedHashSet<String>()
                    {{
                        add("Event Type");

                        add("Event Context");

                        add("Time");

                    }};

                    List<Object> pdfResults = new ArrayList<>();

                    List<Object> pdfResult;

                    for (TraceOrgEvent traceOrgEvent : traceOrgEventList)
                    {
                        pdfResult = new ArrayList<>();

                        pdfResult.add(traceOrgEvent.getEventType());

                        pdfResult.add(traceOrgEvent.getEventContext());

                        pdfResult.add(traceOrgEvent.getTimestamp());

                        pdfResults.add(pdfResult);
                    }
                    HashMap<String, Object> results = new HashMap<>();

                    results.put("grid-result", pdfResults);

                    results.put("columns", columns);

                    List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                    visualizationResults.add(results);

                    gridReport.put("Title", "Conflict Ip Report ");

                    fileName = "Conflict Ip Report " + TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date()) + ".pdf";

                    fileName = fileName.replace(" ", "_").replace(":", "_").replace(",", "");

                    TraceOrgPDFBuilder.addGridReport(1, visualizationResults, new HashMap<String, Object>(), fileName, gridReport);
                }
                catch (Exception exception)
                {
                    _logger.error(exception);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return fileName;
    }

    public String exportAllConflictIpReportCsv(Integer exportTimeline)
    {
        String fileName = null;

        List<TraceOrgEvent> traceOrgEventList = null;

        try
        {
            switch (exportTimeline)
            {
                case 1 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and Date(timestamp) = CURDATE() order by id desc");
                    break;
                case 2 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and DATE(timestamp) = DATE(CURDATE() -1) order by id desc");
                    break;
                case 3 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and WEEK(timestamp) =  WEEK(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 4 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and MONTH(timestamp)= MONTH(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 5 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  eventType ='Conflict IP'  and QUARTER(timestamp) = QUARTER(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 6 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and QUARTER(timestamp) = QUARTER(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 7 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and TIMESTAMPDIFF(MONTH, timestamp, NOW()) < 6  order by id desc");
                    break;
                case 8 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and YEAR(timestamp) = YEAR(curdate()) order by id desc ");
                    break;
                case 9 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and YEAR(timestamp) = YEAR(curdate()) - 1 order by id desc");
                    break;
                case 10 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT +" where eventType ='Conflict IP' order by id desc" );
                    break;
                case 11 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and  WEEK(timestamp) =  WEEK(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 12 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where eventType ='Conflict IP'  and  MONTH(timestamp)= MONTH(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                default:
                    traceOrgEventList = null;
                    break;
            }

            if(traceOrgEventList !=null && !traceOrgEventList.isEmpty())
            {
                try
                {
                    fileName = "Conflict_IP_"+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".csv";

                    fileName = fileName.replace(" ", "_").replace(":", "_").replace(",", "");

                    File file = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR +fileName);

                    CsvWriter csvWriter = new CsvWriter();

                    Collection<String[]> data = new ArrayList<>();

                    data.add(new String[] { "Event Type","Event Context","Time"});

                    for(TraceOrgEvent traceOrgEvent:traceOrgEventList)
                    {
                        data.add(new String[] { traceOrgEvent.getEventType(),traceOrgEvent.getEventContext(),traceOrgEvent.getTimestamp()});
                    }
                    csvWriter.write(file, StandardCharsets.UTF_8, data);
                }
                catch (Exception exception)
                {
                    _logger.error(exception);
                }

            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return fileName;
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * This method is only used for IPv4.
     * */
    public static boolean isValidIp(TraceOrgSubnetDetails traceOrgSubnetDetails,String ipAddress)
    {
        boolean result = Boolean.FALSE;

        try
        {
            if(TraceOrgCommonConstants.IPV4_PATTERN.matcher(traceOrgSubnetDetails.getSubnetAddress()).matches())
            {
                SubnetUtils subnetUtils = new SubnetUtils(traceOrgSubnetDetails.getSubnetAddress() + "/" + traceOrgSubnetDetails.getSubnetCidr());

                subnetUtils.setInclusiveHostCount(true);

                SubnetUtils.SubnetInfo  subnetInfo = (subnetUtils).getInfo();

                result = subnetInfo.isInRange(ipAddress);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return result;
    }

    public void scheduleCustomJob(TraceOrgReportScheduler traceOrgReportScheduler, TraceOrgService traceOrgService, TraceOrgCommonUtil traceOrgCommonUtil)
    {
        Integer index = 0;

        try
        {
            if (traceOrgReportScheduler != null)
            {
                String date = TraceOrgCommonUtil.getStringValue(traceOrgReportScheduler.getSchedulerDate());

                String time = TraceOrgCommonUtil.getStringValue(traceOrgReportScheduler.getSchedulerTime());

                TraceOrgCronExpressionManager cronExpressionManager = new TraceOrgCronExpressionManager();

                DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                Date schedulerTime = simpleDateFormat.parse(traceOrgReportScheduler.getSchedulerDate()+" "+traceOrgReportScheduler.getSchedulerTime());

                if (date != null && time != null && schedulerTime.after(new Date()))
                {
                    // recurring cron expression using date and time

                    cronExpressionManager.setRecurring(true);

                    cronExpressionManager.setDate(date);

                    cronExpressionManager.setTime(time);

                }

                String cronExpressions = cronExpressionManager.getCronExpression();

                if (cronExpressions != null && cronExpressions.length() > 0)
                {
                    queueReportCustomJob(index, cronExpressions,traceOrgReportScheduler,traceOrgService,traceOrgCommonUtil);
                }

                short timeLine = TraceOrgCommonUtil.getShortValue(traceOrgReportScheduler.getSchedulerTimeLine());

                String jobTime = null;

                if (traceOrgReportScheduler.getRepeatHourTime() != null)
                {
                    jobTime = TraceOrgCommonUtil.getStringValue(traceOrgReportScheduler.getRepeatHourTime());
                }

                cronExpressionManager = new TraceOrgCronExpressionManager();

                if (timeLine == TraceOrgCommonConstants.SCHEDULER_TIMELINE_DAILY)
                {
                    cronExpressionManager.setDaily(true);

                    cronExpressionManager.setTime(jobTime);
                }
                else if (timeLine == TraceOrgCommonConstants.SCHEDULER_TIMELINE_WEEKLY)
                {
                    cronExpressionManager.setWeekDay(TraceOrgCommonUtil.getStringValue(traceOrgReportScheduler.getRepeatDay()));

                    cronExpressionManager.setWeekly(true);

                    cronExpressionManager.setTime(jobTime);

                }
                else if (timeLine == TraceOrgCommonConstants.SCHEDULER_TIMELINE_MONTHLY)
                {
                    cronExpressionManager.setMonth(TraceOrgCommonUtil.getStringValue(traceOrgReportScheduler.getRepeatMonth()));

                    cronExpressionManager.setDay(TraceOrgCommonUtil.getStringValue(traceOrgReportScheduler.getRepeatDate()));

                    cronExpressionManager.setMonthly(true);

                    cronExpressionManager.setTime(jobTime);

                }

                cronExpressions = cronExpressionManager.getCronExpression();

                if (cronExpressions != null && cronExpressions.length() > 0)
                {

                    for (String cronExpression : cronExpressions.split(TraceOrgCommonConstants.LINK_SEPARATOR))
                    {
                        index++;

                        queueReportCustomJob(index, cronExpression,traceOrgReportScheduler,traceOrgService,traceOrgCommonUtil);

                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    public void queueReportCustomJob(Integer index, String cronExpression, TraceOrgReportScheduler traceOrgReportScheduler,TraceOrgService traceOrgService,TraceOrgCommonUtil traceOrgCommonUtil)
    {
        if (org.quartz.CronExpression.isValidExpression(cronExpression))
        {
            try
            {
                HashMap<String,Object> mapData = new HashMap<>();

                mapData.put("traceOrgReportScheduler",traceOrgReportScheduler);

                mapData.put("traceOrgService",traceOrgService);

                mapData.put("traceOrgCommonUtil",traceOrgCommonUtil);

                JobKey jobKey = JobKey.jobKey(traceOrgReportScheduler.getId()+"ReportScheduler"+index,"ReportScheduler");

                JobDetail job = JobBuilder.newJob(TraceOrgReportSchedulerJob.class).withIdentity(jobKey).usingJobData(new JobDataMap(mapData)).storeDurably().build();

                Trigger trigger = TriggerBuilder
                        .newTrigger()
                        .withIdentity(traceOrgReportScheduler.getId()+"ReportScheduler"+index, "ReportScheduler")
                        .withSchedule(
                                CronScheduleBuilder.cronSchedule(cronExpression))
                        .build();

                quartzThread.scheduleJob(job,trigger);
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }
        }
        else
        {
            _logger.warn("Cron Expression is Not valid");
        }
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Added the traceOrgRogueDetectionRepository object for rogue detection.
     * @param cronExpression
     * @param traceOrgSubnetDetails
     * @param traceOrgService
     * @param traceOrgCommonUtil
     */
    public void scanSubnetCronJob(String cronExpression, TraceOrgSubnetDetails traceOrgSubnetDetails, TraceOrgService traceOrgService, TraceOrgCommonUtil traceOrgCommonUtil)
    {
        _logger.info("cronExpression ::"+cronExpression);

        if (org.quartz.CronExpression.isValidExpression(cronExpression))
        {
            try
            {
                _logger.info(traceOrgSubnetDetails.getSubnetAddress() +" subnet Scheduled for Every "+traceOrgSubnetDetails.getScheduleHour()+" "+traceOrgSubnetDetails.getDuration());

                HashMap<String,Object> mapData = new HashMap<>();

                mapData.put("subnetDetails",traceOrgSubnetDetails);

                mapData.put("traceOrgService",traceOrgService);

                mapData.put(TraceOrgCommonConstants.TRACE_ORG_ALERT_SERVICE, traceOrgAlertService);

                mapData.put("traceOrgRogueDetectionRepository", traceOrgRogueDetectionRepository);

                mapData.put("traceOrgCommonUtil",traceOrgCommonUtil);

                mapData.put(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE,traceOrgSupernetService);

                JobKey jobKey = JobKey.jobKey(TraceOrgCommonConstants.SCAN_SUBNET+traceOrgSubnetDetails.getSubnetAddress(),TraceOrgCommonConstants.SCAN_SUBNET);

                JobDetail job = JobBuilder.newJob(TraceOrgScanSubnetUpdateQueue.class).withIdentity(jobKey).usingJobData(new JobDataMap(mapData)).storeDurably().build();

                Trigger trigger = TriggerBuilder
                        .newTrigger()
                        .withIdentity(TraceOrgCommonConstants.SCAN_SUBNET+traceOrgSubnetDetails.getSubnetAddress(),TraceOrgCommonConstants.SCAN_SUBNET)
                        .withSchedule(
                                CronScheduleBuilder.cronSchedule(cronExpression))
                        .build();

                quartzThread.scheduleJob(job,trigger);
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }
        }
        else
        {
            _logger.warn("Cron Expression is Not valid");
        }
    }

    public void removeScanSubnetCron(TraceOrgSubnetDetails traceOrgSubnetDetails)
    {
        try
        {
            for (JobKey jobKey : quartzThread.getJobKeys(GroupMatcher.jobGroupEquals(TraceOrgCommonConstants.SCAN_SUBNET)))
            {
                if (jobKey.getName().trim().startsWith(TraceOrgCommonConstants.SCAN_SUBNET+traceOrgSubnetDetails.getSubnetAddress()))
                {
                    quartzThread.deleteJob(jobKey);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }


    /**
     * IPAM-145 : System should have rogue device detection capability
     * Added the traceOrgRogueDetectionRepository object for rogue detection.
     * IPAM-192 : Subnet should be added into respective supernet during DHCP Auto scan
     * added the traceOrgSupernetService into job data map
     * @param cronExpression
     * @param traceOrgDhcpCredentialDetails
     * @param traceOrgService
     * @param traceOrgCommonUtil
     * @param traceOrgCiscoDHCPServerUtil
     * @param traceOrgWindowsDhcpServerUtil
     */
    public void scanDhcpCronJob(String cronExpression, TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails,TraceOrgService traceOrgService,TraceOrgCommonUtil traceOrgCommonUtil,TraceOrgCiscoDHCPServerUtil traceOrgCiscoDHCPServerUtil,TraceOrgWindowsDhcpServerUtil traceOrgWindowsDhcpServerUtil)
    {
        if (org.quartz.CronExpression.isValidExpression(cronExpression))
        {
            try
            {

                _logger.info(traceOrgDhcpCredentialDetails.getHostAddress() +" Server Scheduled for Every "+traceOrgDhcpCredentialDetails.getScheduleHour()+" "+traceOrgDhcpCredentialDetails.getDuration());

                HashMap<String,Object> subnetDetails = new HashMap<>();

                subnetDetails.put("traceOrgDhcpCredentialDetails",traceOrgDhcpCredentialDetails);

                subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_SERVICE,this.traceOrgService);

                subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_ALERT_SERVICE, this.traceOrgAlertService);

                subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_ROGUE_DETECTION_REPOSITORY, this.traceOrgRogueDetectionRepository);

                subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL,traceOrgCommonUtil);

                subnetDetails.put("traceOrgCiscoDHCPServerUtil",traceOrgCiscoDHCPServerUtil);

                subnetDetails.put("traceOrgWindowsDhcpServerUtil",traceOrgWindowsDhcpServerUtil);

                subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE,traceOrgSupernetService);

                subnetDetails.put(TraceOrgCommonConstants.SCAN_TYPE,TraceOrgCommonConstants.DHCP_SCAN);

                JobKey jobKey = JobKey.jobKey(TraceOrgCommonConstants.SCAN_DHCP+traceOrgDhcpCredentialDetails.getHostAddress(),TraceOrgCommonConstants.SCAN_DHCP);

                JobDetail job = JobBuilder.newJob(TraceOrgDhcpScanQueue.class).withIdentity(jobKey).usingJobData(new JobDataMap(subnetDetails)).storeDurably().build();

                Trigger trigger = TriggerBuilder
                        .newTrigger()
                        .withIdentity(TraceOrgCommonConstants.SCAN_DHCP+traceOrgDhcpCredentialDetails.getHostAddress(),TraceOrgCommonConstants.SCAN_DHCP)
                        .withSchedule(
                                CronScheduleBuilder.cronSchedule(cronExpression))
                        .build();

                quartzThread.scheduleJob(job,trigger);
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }
        }
        else
        {
            _logger.warn("Cron Expression is Not valid");
        }
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added method to scheduleBackupJob
     * */
    public void scheduleBackupJob()
    {
        try
        {
            TraceOrgDatabaseMaintenance traceOrgDatabase = (TraceOrgDatabaseMaintenance) this.traceOrgService.getById("TraceOrgDatabaseMaintenance", 1L);

            if(traceOrgDatabase != null && traceOrgDatabase.getDuration() !=null
                    && traceOrgDatabase.getScheduleHour()!=null && traceOrgDatabase.getBackupPath() != null
                    && traceOrgDatabase.getScheduleHour() > 0 )
            {
                String cronExpression = null;

                switch (traceOrgDatabase.getDuration())
                {
                    case "Days" :
                        cronExpression = "0 0 0 1-31/"+traceOrgDatabase.getScheduleHour()+" * ?";
                        break;
                    case "Month" :
                        cronExpression = "0 0 0 1 1-12/"+traceOrgDatabase.getScheduleHour()+" ?";
                        break;
                }

                backupCronJob(cronExpression,traceOrgDatabase);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added method for backupCronJob
     *
     * IPAM-145 : System should have rogue device detection capability
     * Added the traceOrgCommonUtil object for schedule backup.
     * */
    public void backupCronJob(String cronExpression, TraceOrgDatabaseMaintenance traceOrgDatabase)
    {
        if (org.quartz.CronExpression.isValidExpression(cronExpression))
        {
            try
            {
                _logger.info("Backup Scheduled for Every " + traceOrgDatabase.getScheduleHour() + " " + traceOrgDatabase.getDuration());

                JobKey jobKey = JobKey.jobKey(TraceOrgCommonConstants.BACKUP_DB + traceOrgDatabase.getId(), TraceOrgCommonConstants.BACKUP_DB);

                TriggerKey triggerKey = TriggerKey.triggerKey(TraceOrgCommonConstants.BACKUP_DB + traceOrgDatabase.getId(), TraceOrgCommonConstants.BACKUP_DB);

                if (quartzThread.checkExists(jobKey))
                {
                    Trigger newTrigger = TriggerBuilder
                            .newTrigger()
                            .withIdentity(triggerKey)
                            .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                            .build();

                    quartzThread.rescheduleJob(triggerKey, newTrigger);
                }
                else
                {
                    HashMap<String, Object> mapData = new HashMap<>();

                    mapData.put("path", traceOrgDatabase.getBackupPath());

                    mapData.put(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL, traceOrgCommonUtil);

                    JobDetail job = JobBuilder.newJob(TraceOrgDatabaseBackup.class).withIdentity(jobKey)
                            .usingJobData(new JobDataMap(mapData)).storeDurably().build();

                    Trigger trigger = TriggerBuilder
                            .newTrigger()
                            .withIdentity(TraceOrgCommonConstants.BACKUP_DB + traceOrgDatabase.getId(), TraceOrgCommonConstants.BACKUP_DB)
                            .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                            .build();

                    quartzThread.scheduleJob(job, trigger);
                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }
        }
        else
        {
            _logger.warn("Cron Expression is Not valid");
        }
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added method for backup
     * */
    public boolean backup(String backupPath)
    {
        boolean result = false;

        File backupFolder = null;

        try
        {
            _logger.info("Starting database backup at backupPath: " + backupPath);

            backupFolder =  new File(TraceOrgCommonConstants.BACKUP_DIR);

            if(!backupFolder.exists()) { backupFolder.mkdirs(); }

            StringBuilder command = new StringBuilder();

            command.append(TraceOrgCommonConstants.SINGLE_QUOTE).append(TraceOrgCommonConstants.MYSQL_DIR).append("mysqldump").append(TraceOrgCommonConstants.SINGLE_QUOTE)
                    .append(" --single-transaction -u root --password=").append(TraceOrgCommonUtil.decrypt("ba03YfDjVoJ3NELSbea67w=="))
                    .append(" ipam").append(" > ").append(TraceOrgCommonConstants.SINGLE_QUOTE).append(backupFolder)
                    .append(TraceOrgCommonConstants.PATH_SEPARATOR).append("backup.sql").append(TraceOrgCommonConstants.SINGLE_QUOTE);

            String powerShellCommand = TraceOrgCommonConstants.POWERSHELL_COMMAND.replace(TraceOrgCommonConstants.COMMAND,command.toString());

            _logger.info("Executing command: " + powerShellCommand);

            Process process = Runtime.getRuntime().exec(powerShellCommand);

            boolean exitValue = process.waitFor(5, TimeUnit.MINUTES);

            if (exitValue)
            {
                command.setLength(0);

                command.append("[Convert]::ToBase64String([IO.File]::ReadAllBytes(")
                        .append(TraceOrgCommonConstants.SINGLE_QUOTE).append(backupFolder)
                        .append(TraceOrgCommonConstants.PATH_SEPARATOR).append("backup.sql").append(TraceOrgCommonConstants.SINGLE_QUOTE)
                        .append(")) > ").append(TraceOrgCommonConstants.SINGLE_QUOTE)
                        .append(backupPath).append(TraceOrgCommonConstants.PATH_SEPARATOR).append("DatabaseBackup").append("_")
                        .append(TraceOrgCommonConstants.SIMPLE_DATE_FORMAT_UNDERSCORE.format(new Date()))
                        .append(TraceOrgCommonConstants.SINGLE_QUOTE);

                powerShellCommand = TraceOrgCommonConstants.POWERSHELL.replace(TraceOrgCommonConstants.COMMAND,command.toString());

                _logger.info("Executing command: " + powerShellCommand);

                process = Runtime.getRuntime().exec(powerShellCommand);

                exitValue = process.waitFor(5, TimeUnit.MINUTES);

                if(exitValue)
                {
                    _logger.info("The database backup completed successfully.");

                    result = true;
                }
                else
                {
                    _logger.fatal("The SQL encryption process in backup exited due to a timeout.");
                }
            }
            else
            {
                _logger.fatal("The backup process exited due to a timeout.");
            }

        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        finally
        {
            if(backupFolder != null)
            {
                File file = new File(backupFolder + TraceOrgCommonConstants.PATH_SEPARATOR + "backup.sql");

                if(file.exists())
                {
                    file.delete();
                }
            }
        }

        return result;
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added method for removeBackupJob
     * */
    public void removeBackupJob()
    {
        try
        {
            JobKey jobKey = JobKey.jobKey(TraceOrgCommonConstants.BACKUP_DB + "1", TraceOrgCommonConstants.BACKUP_DB);

            if (quartzThread.checkExists(jobKey))
            {
                boolean isDeleted = quartzThread.deleteJob(jobKey);

                if(isDeleted)
                {
                    _logger.info("Backup job removed successfully!");
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    public void removeScanDhcpCron(TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails)
    {
        try
        {
            for (JobKey jobKey : quartzThread.getJobKeys(GroupMatcher.jobGroupEquals(TraceOrgCommonConstants.SCAN_DHCP)))
            {
                if (jobKey.getName().trim().startsWith(TraceOrgCommonConstants.SCAN_DHCP+traceOrgDhcpCredentialDetails.getHostAddress()))
                {
                    quartzThread.deleteJob(jobKey);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }


    public void removeReportCustomJob(TraceOrgReportScheduler traceOrgReportScheduler)
    {
        try
        {
            for (JobKey jobKey : quartzThread.getJobKeys(GroupMatcher.jobGroupEquals("ReportScheduler")))
            {
                if (jobKey.getName().trim().startsWith(traceOrgReportScheduler.getId() + "ReportScheduler"))
                {
                    quartzThread.deleteJob(jobKey);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    static void scheduleSubnetScanCheckJob()
    {
        try
        {
            JobDetail scheduleSubnetScanJob = JobBuilder.newJob(TraceOrgSubnetScheduleScanJob.class).withIdentity("schedule-subnet-scan-job", "fixed-job").build();

            Trigger scheduleSubnetScanJobTrigger = TriggerBuilder.newTrigger().withIdentity("schedule-subnet-scan-job-trigger", "fixed-job").withSchedule(CronScheduleBuilder.cronSchedule(TraceOrgCommonUtil.SCHEDULE_SUBNET_SCAN_JOB_CRON_EXPRESSION)).build();


            TraceOrgCommonUtil.quartzThread.scheduleJob(scheduleSubnetScanJob, scheduleSubnetScanJobTrigger);

        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    public static int getBufferSize()
    {

        int bufferSize = 32;

        try
        {
            com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();

            int memory = (int) operatingSystemMXBean.getTotalPhysicalMemorySize() / (1024 * 1024 * 1024);

            if (memory < 4)
            {
                bufferSize = 32;
            }
            else if (memory > 4 && memory < 8)
            {
                bufferSize = 64;
            }

            else if (memory > 8 && memory < 12)
            {
                bufferSize = 128;
            }

            else if (memory > 12 && memory < 16)
            {
                bufferSize = 256;
            }

            else if (memory > 16 && memory < 24)
            {
                bufferSize = 512;
            }

            else if (memory > 24)
            {
                bufferSize = 1024;
            }

        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return bufferSize;
    }

    public static Integer getCSVImportCount()
    {
        return m_csvImportStatus.intValue();
    }

    public static Integer getScanCount()
    {
        return m_ScanStatus.intValue();
    }

    public static void incrementCSVImportCount()
    {
        m_csvImportStatus.set(m_csvImportStatus.intValue() + 1);
    }

    public static void incrementScanStatusCount()
    {
        m_ScanStatus.set(m_ScanStatus.intValue() + 1);
    }

    public static void decrementCSVImportCount()
    {
        m_csvImportStatus.set(m_csvImportStatus.intValue() - 1);
    }

    public static void decrementScanStatusCount()
    {
        m_ScanStatus.set(m_ScanStatus.intValue() - 1);
    }

    public static Object convertToFormattedValue(Object value, DecimalFormat decimalFormat)
    {
        try
        {
            value = decimalFormat.format(value);
        }
        catch (Exception var3)
        {
            _logger.error(var3);
        }

        return value;
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added a method for writing the plugin context file for the Go engine
     * */
    public static String writePluginContextFile(HashMap<String, Object> context)
    {
        String fileName = null;

        try
        {
            File cacheFolder =  new File(TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "cache");

            if(!cacheFolder.exists())
            {
                cacheFolder.mkdirs();
            }

            fileName = UUID.randomUUID().toString().toLowerCase().trim() + ".txt";

            File file = new File(cacheFolder + TraceOrgCommonConstants.PATH_SEPARATOR + fileName);

            try (FileWriter fileWriter = new FileWriter(file); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, 10240))
            {
                bufferedWriter.write(TraceOrgCommonUtil.getJSON(context));
            }
            catch (IOException exception)
            {
                _logger.error(exception);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return fileName;
    }


    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added method to getIPAMPath
     * */
    public static String getIPAMPath()
    {
        String path = null;

        try
        {
            File currentFile = new File(TraceOrgCommonConstants.CURRENT_DIR);

            File parentFile = new File(currentFile.getParent());

            path = parentFile.getParent();
        }
        catch (Exception e)
        {
            _logger.error(e);
        }

        return path;
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added method to sendConflictIpMail
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMail method
     * */
    public static void sendConflictIpMail(TraceOrgService traceOrgService, TraceOrgSubnetIpDetails traceOrgSubnetIpDetailsExisted)
    {
        TraceOrgMailServer traceOrgMailServer =(TraceOrgMailServer) traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_MAIL_SERVER, 1L);

        if(traceOrgMailServer != null)
        {
            try
            {
                new TraceOrgCommonUtil(traceOrgService).sendMail("Conflict IP In IP Address Manager",
                        "IP Address "+traceOrgSubnetIpDetailsExisted.getIpAddress()+" with  Mac Address "+traceOrgSubnetIpDetailsExisted.getMacAddress()+" conflicted with  Mac Address "+traceOrgSubnetIpDetailsExisted.getConflictMac()+" in IP Address Manager.");
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }
        }
    }

    /**
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMail method
     * */
    public void sendMail(String subject, String message)
    {
        try
        {
            sendMail(subject, message, TraceOrgCommonConstants.PRIMARY_MAIL_SERVER_ID);
        }
        catch (Exception exception)
        {
            try
            {
                sendMail(subject, message, TraceOrgCommonConstants.SECONDARY_MAIL_SERVER_ID);
            }
            catch (Exception e)
            {
                _logger.error(exception);
            }

            _logger.error(exception);
        }
    }

    /**
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMailWithAttachment method
     * */
    public void sendMailWithAttachment(String subject, String message, String emailTo, File file)
    {
        try
        {
            sendMailWithAttachment(subject, message, emailTo, file, TraceOrgCommonConstants.PRIMARY_MAIL_SERVER_ID);
        }
        catch (Exception exception)
        {
            try
            {
                sendMailWithAttachment(subject, message, emailTo, file, TraceOrgCommonConstants.SECONDARY_MAIL_SERVER_ID);
            }
            catch (Exception e)
            {
                _logger.error(exception);
            }

            _logger.error(exception);
        }
    }

    /**
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMail method
     * */
    public void sendMail(String subject, String message, long mailServerId) throws Exception
    {
        TraceOrgMailServer traceOrgMailServer =(TraceOrgMailServer) traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_MAIL_SERVER, mailServerId);

        if(traceOrgMailServer != null)
        {
            if(Strings.isNullOrEmpty(traceOrgMailServer.getMailUserId())) traceOrgMailServer.setMailUserId(traceOrgMailServer.getMailFromEmail());

            TraceOrgCommonUtil.sendMail(traceOrgMailServer.getMailHost(),
                    traceOrgMailServer.getMailPort(),
                    subject,
                    "Hello " + traceOrgMailServer.getMailUserName() + ", <br><br> <t> " + message + " <br><br> Thank You.",
                    traceOrgMailServer.getMailFromEmail(),
                    traceOrgMailServer.getMailToEmail(),
                    traceOrgMailServer.getMailProtocol(),
                    traceOrgMailServer.getMailUserId(),
                    traceOrgMailServer.getMailPassword(),
                    traceOrgMailServer.getMailTimeout());
        }
    }

    /**
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMailWithAttachment method
     * */
    public void sendMailWithAttachment(String subject, String message, String emailTo, File file, long mailServerId) throws Exception
    {
        TraceOrgMailServer traceOrgMailServer =(TraceOrgMailServer) traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_MAIL_SERVER, mailServerId);

        if(traceOrgMailServer != null)
        {
            if(Strings.isNullOrEmpty(traceOrgMailServer.getMailUserId())) traceOrgMailServer.setMailUserId(traceOrgMailServer.getMailFromEmail());

            if(Strings.isNullOrEmpty(emailTo)) emailTo = traceOrgMailServer.getMailToEmail();

            sendMailWithAttachment(traceOrgMailServer.getMailHost(),
                    traceOrgMailServer.getMailPort(),
                    subject,
                    "Hello " + traceOrgMailServer.getMailUserName() + ", <br><br> <t> " + message + " <br><br> Thank You.",
                    traceOrgMailServer.getMailFromEmail(),
                    emailTo,
                    traceOrgMailServer.getMailProtocol(),
                    traceOrgMailServer.getMailUserId(),
                    traceOrgMailServer.getMailPassword(),
                    traceOrgMailServer.getMailTimeout(),
                    file);
        }
    }

    public boolean checkGatewayIp(String gatewayIp) {
        String zeroTo255
                = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])";

        String IP_REGEXP
                = zeroTo255 + "\\." + zeroTo255 + "\\."
                + zeroTo255 + "\\." + zeroTo255;

        Pattern IP_PATTERN
                = Pattern.compile(IP_REGEXP);

        return IP_PATTERN.matcher(gatewayIp).matches();
    }

    /**
     * IPAM-157 : IPAM Refactor: Restructure code for setting module
     * Added a method to validate whether the request has admin access or not.
     * */
    public boolean validateRequest(HttpServletRequest request, Response response, boolean checkAdminRole)
    {
        boolean result = false;

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            if(checkToken(accessToken))
            {
                if(!checkAdminRole || currentUserRole(accessToken).equals(TraceOrgCommonConstants.ROLE_ADMIN))
                {
                    result = true;
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.DO_NOT_HAVE_ACCESS);
                }
            }
            else
            {
                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.TOKEN_NOT_RECOGNISED);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }

        return result;
    }

    /**
     * IPAM-157 : IPAM Refactor: Restructure code for setting module
     * Added a method to build generic response.
     * */
    public void buildResponse(HashMap<String, Object> result, Response response)
    {
        try
        {
            if(result != null && result.get(TraceOrgCommonConstants.SUCCESS) != null)
            {
                response.setSuccess((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));

                if(result.get(TraceOrgCommonConstants.MESSAGE) != null)
                {
                    response.setMessage(getStringValue(result.get(TraceOrgCommonConstants.MESSAGE)));
                }

                if(result.get(TraceOrgCommonConstants.DATA) != null)
                {
                    response.setData(result.get(TraceOrgCommonConstants.DATA));
                }
            }
            else
            {
                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added a method to set the flag value.
     * */
    public void setFlag(String key, boolean value)
    {
        try
        {
            TraceOrgFlags traceOrgFlags = traceOrgFlagRepository.findByFlag(key);

            if(traceOrgFlags == null)
            {
                traceOrgFlags = new TraceOrgFlags();

                traceOrgFlags.setFlag(key);
            }

            traceOrgFlags.setValue(value);

            traceOrgFlagRepository.save(traceOrgFlags);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to log the event.
     * */
    public void logEvent(String eventType, String eventContext, int severity, TraceOrgUser traceOrgUser)
    {
        try
        {
            TraceOrgEvent traceOrgEvent = new TraceOrgEvent();

            traceOrgEvent.setTimestamp(new Date());

            traceOrgEvent.setDoneBy(traceOrgUser);

            traceOrgEvent.setEventType(eventType);

            traceOrgEvent.setEventContext(eventContext);

            traceOrgEvent.setSeverity(severity);

            traceOrgEventRepository.save(traceOrgEvent);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    public boolean getBoolean(String value)
    {
        return value != null && Boolean.parseBoolean(value);
    }

    public void sendMessage(String queueName, HashMap<String, Object> context, String workType)
    {
        HashMap<String, Object> message = new HashMap<>();

        message.put(TraceOrgCommonConstants.WORK_TYPE, workType);

        message.put(TraceOrgCommonConstants.WORK_CONTEXT, context);

        traceOrgMessageSender.sendMessage(queueName, message);
    }

    /**
     * IPAM-174 : IPAM | Duplicate Emails sent on the same day for repeat mode for Report Scheduler
     * added the method to get the current username
     * @return
     */
    public String getCurrentUserName()
    {
        String userName = "";

        try
        {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principal instanceof UserDetails)
            {
                userName = ((UserDetails) principal).getUsername();
            }
            else
            {
                userName = principal.toString();
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return userName;
    }
}
