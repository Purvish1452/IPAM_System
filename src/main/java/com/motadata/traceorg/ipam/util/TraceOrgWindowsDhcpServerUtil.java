package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.*;
import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgVendor;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpCredentialDetails;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpUtilization;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hardik.
 */

@SuppressWarnings("ALL")
public class TraceOrgWindowsDhcpServerUtil
{

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgWindowsDhcpServerUtil.class, "GUI / Windows DHCP Server Util");

    public Response discover(TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails) throws Exception
    {
        HashMap<String,Object> context = new HashMap<>();

        Response response = new Response();

        boolean status;

        String host = null;

        if (traceOrgDhcpCredentialDetails != null)
        {
            try
            {
                host = TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getHostAddress());

                _logger.debug("discovery request:" + host);

                if (traceOrgDhcpCredentialDetails.getUserName()!=null && traceOrgDhcpCredentialDetails.getPassword()!=null)
                {
                    int port = 5985;

                    if (traceOrgDhcpCredentialDetails.getPort() != null)
                    {
                        port = TraceOrgCommonUtil.getIntegerValue(traceOrgDhcpCredentialDetails.getPort());
                    }

                    _logger.info("Windows DHCP server discovery received for host "+host + " and port "+port);

                    status = TraceOrgCommonUtil.isHostReachable(host);

                    if (status)
                    {
                        status = TraceOrgCommonUtil.isPortReachable(host, port);

                        if (status)
                        {
                            context.put("plugin-context",traceOrgDhcpCredentialDetails);

                            String output = TraceOrgPythonEngineUtil.discover(context);

                            if(output !=null && output.trim().length()>0)
                            {
                                response.setMessage(TraceOrgMessageConstants.DHCP_CREDENTIAL_ADD_SUCCESS);

                                response.setSuccess(TraceOrgCommonConstants.TRUE);
                            }

                            else
                            {
                                if(TraceOrgPythonEngineUtil.getError(context) != null && TraceOrgPythonEngineUtil.getError(context).contains("InvalidCredentialsError"))
                                {
                                    response.setMessage(String.format(TraceOrgCommonConstants.ERROR_AUTH, host));

                                    response.setSuccess(TraceOrgCommonConstants.FALSE);
                                }

                                else
                                {
                                    if(TraceOrgPythonEngineUtil.getError(context) != null)
                                    {
                                        if(TraceOrgPythonEngineUtil.getError(context).contains("Stopped"))
                                        {
                                            response.setMessage("Unreachable server");

                                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                                            _logger.warn("Unreachable server");
                                        }
                                        else
                                        {
                                            response.setMessage(TraceOrgPythonEngineUtil.getError(context) + " ["+host+"]");

                                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                                            _logger.warn(TraceOrgPythonEngineUtil.getError(context) + " ["+host+"]");
                                        }
                                    }
                                }
                            }
                        }

                        else
                        {
                            response.setMessage(String.format(TraceOrgCommonConstants.ERROR_SERVICE_DOWN, port, host));

                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            _logger.warn(String.format(TraceOrgCommonConstants.ERROR_SERVICE_DOWN, port, host));
                        }
                    }

                    else
                    {
                        response.setMessage(String.format(TraceOrgCommonConstants.ERROR_PING_FAILED, host));

                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        _logger.warn(String.format(TraceOrgCommonConstants.ERROR_PING_FAILED, host));
                    }
                }
                else
                {
                    response.setMessage("No valid credentials found");

                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    _logger.warn("no valid credentials found");
                }
            }

            catch (Exception exception)
            {
                _logger.error(exception);

                response.setMessage(exception.getMessage() + " ["+host+"]");

                response.setSuccess(TraceOrgCommonConstants.FALSE);

                _logger.warn("failed to discover " + host);
            }
        }

        return response;
    }

    public static HashMap<String, Object> collect(TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails) throws Exception
    {
        HashMap<String, Object> metrics = null;

        HashMap<String,Object> context = new HashMap<>();

        String host;

        String columnValue;

        SimpleDateFormat simpleDateFormat;

        String token,tokenValue;

        ArrayList<HashMap<String,Object>> scopes;

        ArrayList<HashMap<String,Object>> reservations;

        ArrayList<HashMap<String,Object>> rangePolicies;

        ArrayList<HashMap<String,Object>> leaseOutput;

        HashMap<String,Object> scope;

        HashMap<String,Object> reservation;

        HashMap<String,Object> rangePolicy;

        HashMap<String,Object> leaseMetrics;

        String scopeId;

        StringBuilder scopeIds;

        if(traceOrgDhcpCredentialDetails != null)
        {
            _logger.debug("metric collection request:" + traceOrgDhcpCredentialDetails.getHostAddress());

            host = TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getHostAddress());

            try
            {
                if (traceOrgDhcpCredentialDetails.getUserName() != null && traceOrgDhcpCredentialDetails.getPassword() != null && traceOrgDhcpCredentialDetails.getHostAddress() != null)
                {
                    context.put("plugin-context",traceOrgDhcpCredentialDetails);

                    HashMap<String, Object> outputs = TraceOrgPythonEngineUtil.collect(context);

                    if (outputs != null && outputs.size() > 0)
                    {
                        metrics = new HashMap<>();

                        String[] scopeMetricTokens;

                        String tokens[];

                        String output = TraceOrgCommonUtil.getStringValue(outputs.get("dhcp-server-statistics"));

                        if(output != null)
                        {
                            scopeMetricTokens = output.replace("\"","").trim().split("\\r\\n");

                            if(scopeMetricTokens.length > 0 )
                            {
                                for (String scopeMetricToken : scopeMetricTokens) {
                                    columnValue = scopeMetricToken.trim();

                                    if (columnValue.length() > 0) {
                                        tokens = columnValue.split(" :");

                                        if (tokens.length >= 2) {
                                            token = tokens[0].trim();

                                            tokenValue = tokens[1].trim();

                                            if (token.equalsIgnoreCase("ServerStartTime")) {

                                                String serverTime = TraceOrgCommonUtil.getStringValue(tokenValue);

                                                simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

                                                Date serverDate = simpleDateFormat.parse(serverTime);

                                                Date systemDate = new Date();

                                                long milliSeconds = systemDate.getTime() - serverDate.getTime();

                                                metrics.put("Uptime (Seconds)", TraceOrgCommonUtil.convertToSeconds(milliSeconds));

                                                metrics.put("Uptime", TraceOrgCommonUtil.formatTime(TraceOrgCommonUtil.convertToSeconds(milliSeconds)));

                                                metrics.put("Server start time", serverTime);

                                            }
                                            else if (token.equalsIgnoreCase("TotalScopes"))
                                            {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "IP Address Scopes");
                                            }
                                            else if (token.equalsIgnoreCase("ScopesWithDelayConfigured")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Scope Config Delay");
                                            }
                                            else if (token.equalsIgnoreCase("ScopesWithDelayOffers")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Scopes Delay Offers");
                                            }
                                            else if (token.equalsIgnoreCase("TotalAddresses")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "IP Addresses");
                                            }
                                            else if (token.equalsIgnoreCase("AddressesInUse")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Used IP Addresses");
                                            }
                                            else if (token.equalsIgnoreCase("AddressesAvailable")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Available IP Addresses");
                                            }
                                            else if (token.equalsIgnoreCase("PercentageInUse")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "IP Address Pool Utilization (%)");
                                            }
                                            else if (token.equalsIgnoreCase("PercentagePendingOffers")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Pending Offers (%)");
                                            }
                                            else if (token.equalsIgnoreCase("PercentageAvailable")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "IP Address Pool Free (%)");
                                            }
                                            else if (token.equalsIgnoreCase("Discovers")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Discovers");
                                            }
                                            else if (token.equalsIgnoreCase("Offers")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Offers");
                                            }
                                            else if (token.equalsIgnoreCase("PendingOffers")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Pending Offers");
                                            }
                                            else if (token.equalsIgnoreCase("DelayedOffers")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Delayed Offers");
                                            }
                                            else if (token.equalsIgnoreCase("Requests")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Requests");
                                            }
                                            else if (token.equalsIgnoreCase("Acks")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Acks");
                                            }
                                            else if (token.equalsIgnoreCase("Naks")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Naks");
                                            }
                                            else if (token.equalsIgnoreCase("Declines")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Declines");
                                            }
                                            else if (token.equalsIgnoreCase("Releases")) {
                                                TraceOrgCommonUtil.extractMetricValue(metrics, tokenValue, null, "Releases");
                                            }

                                        }
                                    }
                                }
                            }
                        }

                        output = TraceOrgCommonUtil.getStringValue(outputs.get("dhcp-scopes"));

                        if(output != null)
                        {
                            scopeIds = new StringBuilder();

                            scopes = new ArrayList<>();

                            String[] scopeTokens = output.replace("\"","").trim().split("\\r\\n\\r\\n");

                            if(scopeTokens.length > 0)
                            {
                                for (String scopeToken : scopeTokens) {
                                    scope = new HashMap<>();

                                    scopeMetricTokens = scopeToken.replace("\"", "").trim().split("\\r\\n");

                                    if (scopeMetricTokens.length > 0) {
                                        for (String scopeMetricToken : scopeMetricTokens) {
                                            tokens = scopeMetricToken.split(" :");

                                            if (tokens.length >= 2) {
                                                token = tokens[0].trim();

                                                tokenValue = tokens[1].trim();

                                                if (tokenValue.length() > 0) {
                                                    if (token.equalsIgnoreCase("ScopeId")) {
                                                        scopeId = TraceOrgCommonUtil.getStringValue(tokenValue);

                                                        scope.put("Scope", scopeId);

                                                        scopeIds.append(scopeId).append(",");

                                                    } else if (token.equalsIgnoreCase("Name")) {
                                                        scope.put("Scope Name", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    } else if (token.equalsIgnoreCase("Description")) {
                                                        scope.put("Scope Description", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    } else if (token.equalsIgnoreCase("SuperscopeName")) {
                                                        scope.put("Scope Superscope", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    } else if (token.equalsIgnoreCase("SubnetMask")) {
                                                        scope.put("Scope Subnet Mask", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    } else if (token.equalsIgnoreCase("StartRange")) {
                                                        scope.put("Scope Start Range", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    } else if (token.equalsIgnoreCase("EndRange")) {
                                                        scope.put("Scope End Range", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    } else if (token.equalsIgnoreCase("LeaseDuration")) {
                                                        scope.put("Scope Lease Duration", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    } else if (token.equalsIgnoreCase("NapProfile")) {
                                                        scope.put("Scope Nap Profile", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    } else if (token.equalsIgnoreCase("NapEnable")) {
                                                        scope.put("Scope Nap Enabled", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    } else if (token.equalsIgnoreCase("Delay(ms)")) {
                                                        TraceOrgCommonUtil.extractMetricValue(scope, tokenValue, null, "Scope Delay (ms)");

                                                    } else if (token.equalsIgnoreCase("State")) {
                                                        scope.put("Scope State", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    } else if (token.equalsIgnoreCase("Type")) {
                                                        scope.put("Scope Type", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    } else if (token.equalsIgnoreCase("MaxBootpClients")) {
                                                        TraceOrgCommonUtil.extractMetricValue(scope, tokenValue, null, "Scope MaxBootp Clients");

                                                    } else if (token.equalsIgnoreCase("ActivatePolicies")) {
                                                        scope.put("Scope Activated Policies", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    }

                                                }
                                            }
                                        }

                                        if (scope.size() > 0) {
                                            scopes.add(scope);
                                        }
                                    }
                                }

                                if(scopes.size() > 0)
                                {
                                    metrics.put("Scopes", scopes);

                                    setDHCPScopeMetrics(scopes,TraceOrgCommonUtil.getStringValue(outputs.get("dhcp-scope-statistics")));
                                }
                            }

                            if(scopeIds.length() > 0)
                            {
                                context.put("scope-ids",TraceOrgCommonUtil.getStringValue(scopeIds.deleteCharAt(scopeIds.length()-1)));

                                outputs = TraceOrgPythonEngineUtil.collect(context);

                                if(outputs != null && outputs.size() > 0)
                                {
                                    output = TraceOrgCommonUtil.getStringValue(outputs.get("dhcp-scope-reservations"));

                                    if(output != null)
                                    {
                                        reservations = new ArrayList<>();

                                        String[]  reservationTokens = output.replace("\"", "").trim().split("\\r\\n\\r\\n");

                                        if (reservationTokens.length > 0)
                                        {
                                            for(String reservationToken1 : reservationTokens) {
                                                reservation = new HashMap<>();

                                                String[] reservationMetricTokens = reservationToken1.replace("\"", "").trim().split("\\r\\n");

                                                if (reservationMetricTokens.length > 0) {
                                                    for (String reservationToken : reservationMetricTokens) {
                                                        tokens = reservationToken.split(" :");

                                                        if (tokens.length >= 2) {
                                                            token = tokens[0].trim();

                                                            tokenValue = tokens[1].trim();

                                                            if (tokenValue.length() > 0) {

                                                                if (token.equalsIgnoreCase("IPAddress")) {
                                                                    reservation.put("IP Address", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                                } else if (token.equalsIgnoreCase("ClientId")) {
                                                                    reservation.put("Client Id", TraceOrgCommonUtil.getStringValue(tokenValue.trim().replaceAll("-", ":")));
                                                                } else if (token.equalsIgnoreCase("ScopeId")) {
                                                                    reservation.put("Scope", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                                } else if (token.equalsIgnoreCase("Name")) {
                                                                    reservation.put("Name", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                                } else if (token.equalsIgnoreCase("Type")) {
                                                                    reservation.put("Type", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                                } else if (token.equalsIgnoreCase("Description")) {
                                                                    reservation.put("Description", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                                }

                                                            }
                                                        }
                                                    }
                                                }

                                                if (reservation.size() > 0) {
                                                    reservations.add(reservation);
                                                }
                                            }
                                        }

                                        if(reservations.size() > 0)
                                        {
                                            metrics.put("Reservations", reservations);
                                        }
                                    }

                                    output = TraceOrgCommonUtil.getStringValue(outputs.get("dhcp-scope-range-policies"));

                                    if(output != null)
                                    {
                                        rangePolicies = new ArrayList<>();

                                        String[]  rangePolicyTokens = output.replace("\"", "").trim().split("\\r\\n\\r\\n");

                                        if (rangePolicyTokens.length > 0)
                                        {
                                            for (String rangePolicyToken : rangePolicyTokens) {
                                                rangePolicy = new HashMap<>();

                                                String[] rangePolicyMetricTokens = rangePolicyToken.replace("\"", "").trim().split("\\r\\n");

                                                if (rangePolicyMetricTokens.length > 0) {
                                                    for (String rangePolicyMetricToken : rangePolicyMetricTokens) {
                                                        tokens = rangePolicyMetricToken.split(" :");

                                                        if (tokens.length >= 2) {
                                                            token = tokens[0].trim();

                                                            tokenValue = tokens[1].trim();

                                                            if (tokenValue.length() > 0) {

                                                                if (token.equalsIgnoreCase("Name")) {
                                                                    rangePolicy.put("Range Policy", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                                } else if (token.equalsIgnoreCase("ScopeId")) {
                                                                    rangePolicy.put("Range Policy Scope", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                                } else if (token.equalsIgnoreCase("StartRange")) {
                                                                    rangePolicy.put("Range Policy Start Range", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                                } else if (token.equalsIgnoreCase("EndRange")) {
                                                                    rangePolicy.put("Range Policy End Range", TraceOrgCommonUtil.getStringValue(tokenValue));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                if (rangePolicy.size() > 0) {
                                                    rangePolicies.add(rangePolicy);
                                                }

                                            }
                                        }

                                        if(rangePolicies.size() > 0)
                                        {
                                            metrics.put("Range Policies", rangePolicies);
                                        }
                                    }

                                    // lease details
                                    output = TraceOrgCommonUtil.getStringValue(outputs.get("dhcp-server-lease"));

                                    if(output != null)
                                    {
                                        leaseOutput = new ArrayList<>();

                                        String[]  leaseTokens = output.replace("\"", "").trim().split("\\r\\n\\r\\n");

                                        if (leaseTokens.length > 0)
                                        {
                                            for (String leaseToken : leaseTokens)
                                            {
                                                leaseMetrics = new HashMap<>();

                                                String[] leaseMetricTokens = leaseToken.replace("\"", "").trim().split("\\r\\n");

                                                if (leaseMetricTokens.length > 0)
                                                {
                                                    for (String leaseMetricToken : leaseMetricTokens)
                                                    {
                                                        tokens = leaseMetricToken.split(" :");

                                                        if (tokens.length >= 2)
                                                        {
                                                            token = tokens[0].trim();

                                                            tokenValue = tokens[1].trim();

                                                            if (tokenValue.length() > 0)
                                                            {
                                                                if (token.equalsIgnoreCase("IPAddress"))
                                                                {
                                                                    leaseMetrics.put("Lease", tokenValue);
                                                                }
                                                                else if (token.equalsIgnoreCase("ScopeId"))
                                                                {
                                                                    leaseMetrics.put("Lease Scope", tokenValue);
                                                                }
                                                                else if (token.equalsIgnoreCase("Description"))
                                                                {
                                                                    leaseMetrics.put("Lease Description", tokenValue);
                                                                }
                                                                else if (token.equalsIgnoreCase("ClientId"))
                                                                {
                                                                    leaseMetrics.put("Lease Mac", tokenValue.trim().replaceAll("-", ":"));
                                                                }
                                                                else if (token.equalsIgnoreCase("HostName"))
                                                                {
                                                                    leaseMetrics.put("Lease Hostname", tokenValue);
                                                                }
                                                                else if (token.equalsIgnoreCase("ClientType"))
                                                                {
                                                                    leaseMetrics.put("Lease ClientType", tokenValue);
                                                                }
                                                                else if (token.equalsIgnoreCase("AddressState"))
                                                                {
                                                                    leaseMetrics.put("Lease AddressState", tokenValue);
                                                                }
                                                                else if (token.equalsIgnoreCase("LeaseExpiryTime"))
                                                                {
                                                                    leaseMetrics.put("Lease ExpiryTime", tokenValue);
                                                                }
                                                                else if (token.equalsIgnoreCase("ServerIP"))
                                                                {
                                                                    leaseMetrics.put("Lease ServerIP", tokenValue);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                if (leaseMetrics.size() > 0) {
                                                    leaseOutput.add(leaseMetrics);
                                                }
                                            }
                                        }

                                        if(leaseOutput.size() > 0)
                                        {
                                            metrics.put("Lease Metrics", leaseOutput);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);

                _logger.warn("failed to collect windows dhcp server metric " + host);

            }
        }
        return metrics;
    }

    private static void setDHCPScopeMetrics(List<HashMap<String,Object>> scopes, String output)
    {
        HashMap<String,Object> scopeMetrics;

        String tokens [];

        String token,tokenValue;

        try
        {
            if(output != null)
            {
                String[] scopeTokens = output.replace("\"","").trim().split("\\r\\n\\r\\n");

                if(scopeTokens.length > 0)
                    for (String scopeToken : scopeTokens)
                    {
                        scopeMetrics = new HashMap<>();

                        String[] scopeMetricTokens = scopeToken.replace("\"", "").trim().split("\\r\\n");

                        if (scopeMetricTokens.length > 0)
                        {
                            for (String scopeMetricToken : scopeMetricTokens)
                            {
                                tokens = scopeMetricToken.split(" :");

                                if (tokens.length >= 2)
                                {
                                    token = tokens[0].trim();

                                    tokenValue = tokens[1].trim();

                                    if (tokenValue.length() > 0)
                                    {
                                        if (token.equalsIgnoreCase("ScopeId"))
                                        {
                                            scopeMetrics.put("Scope", TraceOrgCommonUtil.getStringValue(tokenValue));
                                        }
                                        else if (token.equalsIgnoreCase("AddressesFree"))
                                        {
                                            TraceOrgCommonUtil.extractMetricValue(scopeMetrics, tokenValue, null, "Scope Available IP Addresses");
                                        }
                                        else if (token.equalsIgnoreCase("AddressesInUse"))
                                        {
                                            TraceOrgCommonUtil.extractMetricValue(scopeMetrics, tokenValue, null, "Scope Used IP Addresses");
                                        }
                                        else if (token.equalsIgnoreCase("PendingOffers")) {
                                            TraceOrgCommonUtil.extractMetricValue(scopeMetrics, tokenValue, null, "Scope Pending Offers");
                                        }
                                        else if (token.equalsIgnoreCase("ReservedAddress")) {
                                            TraceOrgCommonUtil.extractMetricValue(scopeMetrics, tokenValue, null, "Scope Reserved Addresses");
                                        }
                                        else if (token.equalsIgnoreCase("PercentageInUse"))
                                        {
                                            TraceOrgCommonUtil.extractMetricValue(scopeMetrics, tokenValue, null, "Scope IP Address Pool Utilization (%)");

                                            scopeMetrics.put("Scope IP Address Pool Free (%)", 100 - TraceOrgCommonUtil.getLongValue(scopeMetrics.get("Scope IP Address Pool Utilization (%)")));
                                        }
                                        else if (token.equalsIgnoreCase("SuperscopeName"))
                                        {
                                            scopeMetrics.put("Scope Superscope", TraceOrgCommonUtil.getStringValue(tokenValue));
                                        }
                                        else if (token.equalsIgnoreCase("AddressesFreeOnThisServer"))
                                        {
                                            TraceOrgCommonUtil.extractMetricValue(scopeMetrics, tokenValue, null, "Scope Available IP Addresses (Current Server)");
                                        }
                                        else if (token.equalsIgnoreCase("AddressesFreeOnPartnerServer"))
                                        {
                                            TraceOrgCommonUtil.extractMetricValue(scopeMetrics, tokenValue, null, "Scope Available IP Addresses (Partner Server)");
                                        }
                                        else if (token.equalsIgnoreCase("AddressesInUseOnThisServer"))
                                        {
                                            TraceOrgCommonUtil.extractMetricValue(scopeMetrics, tokenValue, null, "Scope Used IP Addresses (Current Server)");
                                        }
                                    }
                                }

                            }

                            for (HashMap<String, Object> scope : scopes)
                            {
                                if (TraceOrgCommonUtil.getStringValue(scope.get("Scope")).equalsIgnoreCase(TraceOrgCommonUtil.getStringValue(scopeMetrics.get("Scope"))))
                                {
                                    scope.putAll(scopeMetrics);
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
    }


    //Get Subnet Details

    public List<TraceOrgSubnetDetails> getSubnetDetails(TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails) throws Exception
    {
        HashMap<String,Object> context = new HashMap<>();

        List<TraceOrgSubnetDetails> traceOrgSubnetDetailList = new ArrayList<>();

        String host;

        String token,tokenValue;

        String scopeId;

        StringBuilder scopeIds;

        if(traceOrgDhcpCredentialDetails != null)
        {
            _logger.debug("metric collection request:" + traceOrgDhcpCredentialDetails.getHostAddress());

            host = TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getHostAddress());

            try
            {
                if (traceOrgDhcpCredentialDetails.getUserName() != null && traceOrgDhcpCredentialDetails.getPassword() != null && traceOrgDhcpCredentialDetails.getHostAddress() != null)
                {
                    context.put("plugin-context",traceOrgDhcpCredentialDetails);

                    HashMap<String, Object> outputs = TraceOrgPythonEngineUtil.collect(context);

                    if (outputs != null && outputs.size() > 0)
                    {
                        String[] scopeMetricTokens;

                        String tokens[];

                        String output = TraceOrgCommonUtil.getStringValue(outputs.get("dhcp-scopes"));

                        if(output != null)
                        {
                            scopeIds = new StringBuilder();

                            String[] scopeTokens = output.replace("\"","").trim().split("\\r\\n\\r\\n");

                            if(scopeTokens.length > 0)
                            {
                                for (String scopeToken : scopeTokens)
                                {
                                    TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

                                    scopeMetricTokens = scopeToken.replace("\"", "").trim().split("\\r\\n");

                                    if (scopeMetricTokens.length > 0)
                                    {
                                        for (String scopeMetricToken : scopeMetricTokens)
                                        {
                                            tokens = scopeMetricToken.split(" :");

                                            if (tokens.length >= 2)
                                            {
                                                token = tokens[0].trim();

                                                tokenValue = tokens[1].trim();

                                                if (tokenValue.length() > 0)
                                                {
                                                    if (token.equalsIgnoreCase("ScopeId"))
                                                    {
                                                        scopeId = TraceOrgCommonUtil.getStringValue(tokenValue);

                                                        traceOrgSubnetDetails.setSubnetAddress(scopeId);

                                                        scopeIds.append(scopeId).append(",");

                                                    }
                                                    else if (token.equalsIgnoreCase("Name"))
                                                    {
                                                        traceOrgSubnetDetails.setSubnetName(TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    }
                                                    else if (token.equalsIgnoreCase("Description"))
                                                    {
                                                        traceOrgSubnetDetails.setDescription(TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    }
                                                    else if (token.equalsIgnoreCase("SubnetMask"))
                                                    {
                                                        traceOrgSubnetDetails.setSubnetMask(TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    }
                                                    else if (token.equalsIgnoreCase("Type"))
                                                    {
                                                        traceOrgSubnetDetails.setType(TraceOrgCommonUtil.getStringValue(tokenValue));
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    traceOrgSubnetDetails.setTraceOrgDhcpCredentialDetailsId(traceOrgDhcpCredentialDetails);

                                    traceOrgSubnetDetails.setSubnetCidr(this.traceOrgCommonUtil.convertNetmaskToCIDR(InetAddress.getByName(traceOrgSubnetDetails.getSubnetMask())));

                                    traceOrgSubnetDetails.setTotalIp(this.traceOrgCommonUtil.countTotalIp(traceOrgSubnetDetails.getSubnetAddress(), traceOrgSubnetDetails.getSubnetCidr()));

                                    traceOrgSubnetDetails.setAvailableIp(this.traceOrgCommonUtil.countTotalIp(traceOrgSubnetDetails.getSubnetAddress(), traceOrgSubnetDetails.getSubnetCidr()) - 2);

                                    traceOrgSubnetDetails.setUsedIp(0L);

                                    traceOrgSubnetDetails.setTransientIp(0L);

                                    traceOrgSubnetDetailList.add(traceOrgSubnetDetails);
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);

                _logger.warn("failed to collect windows dhcp server metric " + host);

            }
        }

        return traceOrgSubnetDetailList;
    }


    //GET SUBNET IP DETAILS

    /**
     * IPAM-145 : System should have rogue device detection capability
     * added logic for set authenticity.
     * @param traceOrgSubnetDetails
     * @param traceOrgService
     * @param traceOrgRogueDetectionRepository
     * @param rogueIps
     * @param traceOrgCommonUtil
     * @return
     * @throws Exception
     */
    public boolean getIpDetailsBySubnet(TraceOrgSubnetDetails traceOrgSubnetDetails, TraceOrgService traceOrgService, TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository, List<String> rogueIps, TraceOrgCommonUtil traceOrgCommonUtil) throws Exception
    {
        TraceOrgSubnetUtil traceOrgSubnetUtil =  new TraceOrgSubnetUtil();

        boolean result = false;

        TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails =traceOrgSubnetDetails.getTraceOrgDhcpCredentialDetailsId();

        HashMap<String,Object> context = new HashMap<>();

        String token,tokenValue;

        context.put(TraceOrgCommonConstants.PLUGIN_CONTEXT,traceOrgDhcpCredentialDetails);

        context.put("scope-ids",traceOrgSubnetDetails.getSubnetAddress());

        HashMap<String, Object> outputs = TraceOrgPythonEngineUtil.collect(context);

        SubnetUtils subnetUtils = new SubnetUtils(traceOrgSubnetDetails.getSubnetAddress() + "/"+ traceOrgSubnetDetails.getSubnetCidr());

        String[] ipArrayList = subnetUtils.getInfo().getAllAddresses();

        List<String> ipList = new ArrayList<>(Arrays.asList(ipArrayList));

        if(outputs != null && outputs.size() > 0)
        {
            String output = TraceOrgCommonUtil.getStringValue(outputs.get("dhcp-server-lease"));

            // lease details
            if(output != null)
            {
                String[]  leaseTokens = output.replace("\"", "").trim().split("\\r\\n\\r\\n");

                if (leaseTokens.length > 0)
                {
                    for (String leaseToken : leaseTokens)
                    {
                        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

                        String[] leaseMetricTokens = leaseToken.replace("\"", "").trim().split("\\r\\n");

                        if (leaseMetricTokens.length > 0)
                        {
                            for (String leaseMetricToken : leaseMetricTokens)
                            {
                                String[] tokens = leaseMetricToken.split(" :");

                                if (tokens.length >= 2)
                                {
                                    token = tokens[0].trim();

                                    tokenValue = tokens[1].trim();

                                    if (tokenValue.length() > 0)
                                    {
                                        if (token.equalsIgnoreCase("IPAddress"))
                                        {
                                            traceOrgSubnetIpDetails.setIpAddress(tokenValue);
                                        }
                                        else if (token.equalsIgnoreCase("Description"))
                                        {
                                            traceOrgSubnetIpDetails.setDescription(tokenValue);
                                        }
                                        else if (token.equalsIgnoreCase("ClientId"))
                                        {
                                            traceOrgSubnetIpDetails.setMacAddress(tokenValue.trim().replaceAll("-", ":"));

                                            List<TraceOrgVendor> vendorDetails = (List<TraceOrgVendor>)traceOrgService.commonQuery("",TraceOrgCommonConstants.VENDOR_BY_MAC_ADDRESS.replace(TraceOrgCommonConstants.VENDOR_MAC_VALUE,traceOrgSubnetIpDetails.getMacAddress().substring(0,8).replace(":","")));

                                            if(vendorDetails != null && !vendorDetails.isEmpty())
                                            {
                                                traceOrgSubnetIpDetails.setDeviceType(vendorDetails.get(0).getVendorName());
                                            }
                                        }
                                        else if (token.equalsIgnoreCase("HostName"))
                                        {
                                            traceOrgSubnetIpDetails.setHostName(tokenValue);
                                        }
                                        else if (token.equalsIgnoreCase("LeaseExpiryTime"))
                                        {
                                            traceOrgSubnetIpDetails.setLeaseExpireDate(new Date(tokenValue));
                                        }
                                        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.USED);

                                        traceOrgSubnetIpDetails.setAuthenticity("discovered");

                                        traceOrgSubnetIpDetails.setLastAliveTime(new Date());
                                    }
                                }
                            }
                        }

                        ipList.remove(traceOrgSubnetIpDetails.getIpAddress());

                        traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);

                        traceOrgSubnetUtil.insertSubnetIp(traceOrgSubnetIpDetails, traceOrgService, traceOrgRogueDetectionRepository, rogueIps, traceOrgSubnetDetails, traceOrgCommonUtil);

                        result = true;
                    }

                }
            }
        }

        for(String ip : ipList)
        {
            TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

            traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);

            traceOrgSubnetIpDetails.setIpAddress(ip);

            traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.AVAILABLE);

            traceOrgSubnetIpDetails.setAuthenticity("-");

            traceOrgSubnetUtil.insertSubnetIp(traceOrgSubnetIpDetails,traceOrgService, traceOrgRogueDetectionRepository, rogueIps, traceOrgSubnetDetails, traceOrgCommonUtil);
        }

        return result;
    }

    public boolean getDhcpUtilizationDetails(TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails,TraceOrgService traceOrgService) throws Exception
    {
        List<TraceOrgDhcpUtilization> traceOrgDhcpUtilizationList = (List<TraceOrgDhcpUtilization>)traceOrgService.commonQuery("","TraceOrgDhcpUtilization where dhcpCredentialDetailId = '"+traceOrgDhcpCredentialDetails.getId()+"'");

        TraceOrgDhcpUtilization traceOrgDhcpUtilization =null;

        if(traceOrgDhcpUtilizationList !=null && !traceOrgDhcpUtilizationList.isEmpty())
        {
             traceOrgDhcpUtilization = traceOrgDhcpUtilizationList.get(0);
        }
        else
        {
               traceOrgDhcpUtilization = new TraceOrgDhcpUtilization();
        }

        HashMap<String,Object> context = new HashMap<>();

        String columnValue;

        String host;

        boolean result = false;

        String token,tokenValue;

        if(traceOrgDhcpCredentialDetails != null)
        {
            _logger.debug("metric collection request:" + traceOrgDhcpCredentialDetails.getHostAddress());

            host = TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getHostAddress());

            try
            {
                if (traceOrgDhcpCredentialDetails.getUserName() != null && traceOrgDhcpCredentialDetails.getPassword() != null && traceOrgDhcpCredentialDetails.getHostAddress() != null)
                {
                    context.put("plugin-context",traceOrgDhcpCredentialDetails);

                    HashMap<String, Object> outputs = TraceOrgPythonEngineUtil.collect(context);

                    if (outputs != null && outputs.size() > 0)
                    {
                        String[] scopeMetricTokens;

                        String tokens[];

                        String output = TraceOrgCommonUtil.getStringValue(outputs.get("dhcp-server-statistics"));

                        if(output != null)
                        {
                            scopeMetricTokens = output.replace("\"", "").trim().split("\\r\\n");

                            if (scopeMetricTokens.length > 0)
                            {
                                for (String scopeMetricToken : scopeMetricTokens)
                                {
                                    columnValue = scopeMetricToken.trim();

                                    if (columnValue.length() > 0)
                                    {
                                        tokens = columnValue.split(" :");

                                        if (tokens.length >= 2)
                                        {
                                            token = tokens[0].trim();

                                            tokenValue = tokens[1].trim();

                                            if (token.equalsIgnoreCase("TotalScopes"))
                                            {
                                                traceOrgDhcpUtilization.setAddressScopes(tokenValue);
                                            }
                                            else if (token.equalsIgnoreCase("Discovers"))
                                            {
                                                traceOrgDhcpUtilization.setDiscovers(tokenValue);
                                            }
                                            else if (token.equalsIgnoreCase("Offers"))
                                            {
                                                traceOrgDhcpUtilization.setOffers(tokenValue);
                                            }
                                            else if (token.equalsIgnoreCase("Requests"))
                                            {
                                                traceOrgDhcpUtilization.setRequests(tokenValue);
                                            }
                                            else if (token.equalsIgnoreCase("Acks"))
                                            {
                                                traceOrgDhcpUtilization.setAcks(tokenValue);
                                            }
                                            else if (token.equalsIgnoreCase("Naks"))
                                            {
                                                traceOrgDhcpUtilization.setNaks(tokenValue);
                                            }
                                            else if (token.equalsIgnoreCase("Declines"))
                                            {
                                                traceOrgDhcpUtilization.setDeclines(tokenValue);
                                            }
                                            else if (token.equalsIgnoreCase("Releases"))
                                            {
                                                traceOrgDhcpUtilization.setReleases(tokenValue);
                                            }

                                            traceOrgDhcpUtilization.setDhcpCredentialDetailId(traceOrgDhcpCredentialDetails);

                                            result = traceOrgService.insert(traceOrgDhcpUtilization);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);

                _logger.warn("failed to collect windows dhcp server metric " + host);

            }
        }
        return result;
    }

}
