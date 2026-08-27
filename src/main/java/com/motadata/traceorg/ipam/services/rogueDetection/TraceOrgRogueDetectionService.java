package com.motadata.traceorg.ipam.services.rogueDetection;

import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

public interface TraceOrgRogueDetectionService
{
    HashMap<String, Object> loadRogueDetectionDetails();

    HashMap<String, Object> loadIndividualRogueDetectionDetails(String page);

    HashMap<String, Object> markedAuthenticityOfMAC(String  id, boolean status,String accessToken);

    HashMap<String, Object> deleteMACAddresses(String  id);

    HashMap<String, Object> importTrustedMACAddressFromCSV(MultipartFile trustedMACAddressCsv, HttpServletRequest request);

    HashMap<String, Object> exportRogueDetectionDetails(short condition, String type,String id, String url);

}
