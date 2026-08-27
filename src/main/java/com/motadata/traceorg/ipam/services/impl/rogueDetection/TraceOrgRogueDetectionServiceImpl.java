package com.motadata.traceorg.ipam.services.impl.rogueDetection;

import com.motadata.traceorg.ipam.controller.rogueDetection.TraceOrgRogueDetectionController;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.rogueDetection.TraceOrgRogueDetection;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetIpDetailsRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.rogueDetection.TraceOrgRogueDetectionService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import com.motadata.traceorg.ipam.util.TraceOrgPDFBuilder;
import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import de.siegmar.fastcsv.writer.CsvWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TraceOrgRogueDetectionServiceImpl implements TraceOrgRogueDetectionService
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgRogueDetectionController.class, "Rogue Detection Service");

    @Autowired
    private TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    @Autowired
    private TraceOrgSubnetIpDetailsRepository traceOrgSubnetIpDetailsRepository;

    private TraceOrgRogueDetection traceOrgRogueDetection;

    private TraceOrgSubnetIpDetails traceOrgSubnetIpDetails;

    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Load all type of authenticity data.
     * @return
     */
    @Override
    public HashMap<String, Object> loadRogueDetectionDetails()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgRogueDetection> traceOrgRogueDetections = traceOrgRogueDetectionRepository.findAll();

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

            result.put(TraceOrgCommonConstants.DATA, traceOrgRogueDetections);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * load individual authenticity data.
     * @param authenticity
     * @return
     */
    @Override
    public HashMap<String, Object> loadIndividualRogueDetectionDetails(String authenticity)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgRogueDetection> traceOrgRogueDetections = traceOrgRogueDetectionRepository.findByAuthenticity(authenticity);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

            result.put(TraceOrgCommonConstants.DATA, traceOrgRogueDetections);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * marked authenticity of mac for admin role only
     * @param id
     * @param status
     * @param accessToken
     * @return
     */
    @Override
    public HashMap<String, Object> markedAuthenticityOfMAC(String id, boolean status, String accessToken)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id != null && !id.isEmpty())
            {
                boolean responseStatus = false;

                if(id.contains(","))
                {
                    String[] macIds = id.split(",");

                    for (String macIdString: macIds)
                    {
                        long macId = Long.parseLong(macIdString);

                        boolean updateAuthenticityStatus = updateAuthenticity(macId, status, accessToken);

                        if (updateAuthenticityStatus)
                        {
                            responseStatus = TraceOrgCommonConstants.TRUE;
                        }
                        else
                        {
                            responseStatus = TraceOrgCommonConstants.FALSE;

                            break;
                        }
                    }
                }
                else
                {
                    long macId = Long.parseLong(id);

                    responseStatus = updateAuthenticity(macId, status, accessToken);
                }

                if(responseStatus)
                {
                    if(status)
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SUBNET_IP_ROGUE_SUCCESS);
                    }
                    else
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SUBNET_IP_TRUST_SUCCESS);
                    }
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.MARKED_AUTHENTICITY_FAIL);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Delete mac from rogue detection.
     * @param id
     * @return
     */
    @Override
    public HashMap<String, Object> deleteMACAddresses(String id)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id != null && !id.isEmpty())
            {
                boolean deleteStatus = false;

                HashMap<String, Object> nonDiscoveredAuthenticity = new HashMap<>();

                if(id.contains(","))
                {
                    String[] macIds = id.split(",");

                    for (String macIdString: macIds)
                    {
                        long macId = Long.parseLong(macIdString);

                        boolean existsStatus = deleteMacAddress(macId, nonDiscoveredAuthenticity);

                        if (existsStatus)
                        {
                            deleteStatus = TraceOrgCommonConstants.TRUE;
                        }
                        else
                        {
                            deleteStatus = TraceOrgCommonConstants.FALSE;

                            break;
                        }
                    }
                }
                else
                {
                    long macId = Long.parseLong(id);

                    deleteStatus = deleteMacAddress(macId, nonDiscoveredAuthenticity);
                }

                if(deleteStatus)
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    if(!nonDiscoveredAuthenticity.isEmpty() && nonDiscoveredAuthenticity.get("updateStatus").equals(Boolean.TRUE))
                    {
                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.DELETE_MAC_ADDRESSES_NON_DISCOVERED_AUTHENTICITY_SUCCESS);
                    }
                    else
                    {
                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.DELETE_MAC_ADDRESSES_SUCCESS);
                    }

                    _logger.debug("Rogue Detection MAC addresses deleted successfully..");
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.DELETE_MAC_ADDRESSES_FAIL);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }

        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * mark trusted authenticity of csv mac addresses.
     *
     * IPAM-172 : IPAM | Mismatched "DiscoveredAt" DateTime Format in Rogue Detection UI After Adding New Data or Importing Trusted MAC Addresses
     * Change the date format based on existing date.
     * @param trustedMACAddressCsv
     * @param request
     * @return
     */
    @Override
    public HashMap<String, Object> importTrustedMACAddressFromCSV(MultipartFile trustedMACAddressCsv, HttpServletRequest request)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(trustedMACAddressCsv.getOriginalFilename().toLowerCase().endsWith("csv"))
            {
                boolean importStatus = traceOrgCommonUtil.importCSVFile(trustedMACAddressCsv, request, TraceOrgCommonConstants.TRUSTED_MAC_ADDRESS_CSV_NAME);

                if(importStatus)
                {
                    File importFile = new File(request.getRealPath(TraceOrgCommonConstants.TRUSTED_MAC_ADDRESS_CSV_PATH));

                    CsvReader csvReader = new CsvReader();

                    CsvContainer csv = csvReader.read(importFile, StandardCharsets.UTF_8);

                    boolean validFileStatus = false;

                    for (CsvRow csvRow : csv.getRows())
                    {
                        if (csvRow.getOriginalLineNumber() == 1)
                        {
                            validFileStatus = checkTrustedMACAddressFileData(csvRow);
                        }

                        if(validFileStatus)
                        {
                            if(csvRow.getOriginalLineNumber() > 1)
                            {
                                if(csvRow.getField(0) != null && !csvRow.getField(0).isEmpty() &&  csvRow.getField(0).contains(":") &&
                                        csvRow.getField(1) != null && !csvRow.getField(1).isEmpty())
                                {
                                    traceOrgRogueDetection = traceOrgRogueDetectionRepository.findByMacAddressAndIpAddress(String.valueOf(csvRow.getField(0)), String.valueOf(csvRow.getField(1)));

                                    if(traceOrgRogueDetection != null && traceOrgRogueDetection.getMacAddress().equals(csvRow.getField(0)) && traceOrgRogueDetection.getIpAddress().equals(csvRow.getField(1)))
                                    {
                                        traceOrgRogueDetection.setAuthenticity("trusted");

                                        traceOrgSubnetIpDetails = traceOrgSubnetIpDetailsRepository.findByMacAddressAndIpAddress(traceOrgRogueDetection.getMacAddress(), traceOrgRogueDetection.getIpAddress());

                                        if(traceOrgSubnetIpDetails != null)
                                        {
                                            traceOrgSubnetIpDetails.setAuthenticity("trusted");

                                            traceOrgSubnetIpDetailsRepository.save(traceOrgSubnetIpDetails);
                                        }
                                    }
                                    else
                                    {
                                        traceOrgRogueDetection = new TraceOrgRogueDetection();

                                        traceOrgRogueDetection.setMacAddress(csvRow.getField(0));

                                        traceOrgRogueDetection.setIpAddress(csvRow.getField(1));

                                        traceOrgRogueDetection.setDiscoveredAt(new Date(TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(LocalDateTime.now())));

                                        traceOrgRogueDetection.setAuthenticity("trusted");
                                    }

                                    traceOrgRogueDetectionRepository.save(traceOrgRogueDetection);
                                }
                                else
                                {
                                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.EMPTY_FILE);

                                    validFileStatus = false;

                                    break;
                                }
                            }
                        }
                        else
                        {
                            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.FILE_NOT_VALID);

                            break;
                        }
                    }

                    if(validFileStatus)
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.TRUSTED_MAC_ADDRESSES_IMPORT_SUCCESS);
                    }
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.FILE_NOT_VALID);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.FILE_NOT_VALID);
            }

        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * export the rogue detection data.
     * @param condition
     * @param type
     * @param id
     * @param url
     * @return
     */
    @Override
    public HashMap<String, Object> exportRogueDetectionDetails(short condition, String type, String id, String url)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgRogueDetection> traceOrgRogueDetections = new ArrayList<>();

            switch (condition)
            {
                case TraceOrgCommonConstants.ALL_ROGUE_DETECTION_DETAILS_EXPORT:

                    getSelectedAuthenticityData(url, traceOrgRogueDetections);

                    break;

                case TraceOrgCommonConstants.SELECTED_ROGUE_DETECTION_DETAILS_EXPORT:

                    getSelectedIDData(id, traceOrgRogueDetections);

                    break;
            }

            if(!traceOrgRogueDetections.isEmpty())
            {
                String fileName = null;

                if(type.equals(TraceOrgCommonConstants.EXPORT_PDF))
                {
                    fileName = buildPDFReport(traceOrgRogueDetections);
                }
                else if(type.equals(TraceOrgCommonConstants.EXPORT_CSV))
                {
                    fileName = buildCSVReport(traceOrgRogueDetections);
                }

                if(fileName != null)
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.DATA, fileName);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.EXPORT_ROGUE_DETECTION_SUCCESS);
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * delete mac address from rogue detection.
     * @param macId
     * @param nonDiscoveredAuthenticity
     * @return
     */
    private boolean deleteMacAddress(long macId, HashMap<String, Object> nonDiscoveredAuthenticity)
    {
        boolean deleteStatus = false;

        try
        {
            traceOrgRogueDetection = traceOrgRogueDetectionRepository.findOne(macId);

            if(traceOrgRogueDetection != null)
            {
                if(traceOrgRogueDetection.getAuthenticity().equalsIgnoreCase("rogue") || traceOrgRogueDetection.getAuthenticity().equalsIgnoreCase("trusted"))
                {
                    traceOrgRogueDetection.setAuthenticity("discovered");

                    traceOrgRogueDetectionRepository.save(traceOrgRogueDetection);

                    traceOrgSubnetIpDetails = traceOrgSubnetIpDetailsRepository.findByMacAddressAndIpAddress(traceOrgRogueDetection.getMacAddress(), traceOrgRogueDetection.getIpAddress());

                    if(traceOrgSubnetIpDetails != null)
                    {
                        traceOrgSubnetIpDetails.setAuthenticity("discovered");

                        traceOrgSubnetIpDetailsRepository.save(traceOrgSubnetIpDetails);
                    }

                    nonDiscoveredAuthenticity.put("updateStatus",Boolean.TRUE);

                    deleteStatus = Boolean.TRUE;
                }
                else
                {
                    traceOrgRogueDetectionRepository.delete(macId);

                    traceOrgSubnetIpDetails = traceOrgSubnetIpDetailsRepository.findByMacAddressAndIpAddress(traceOrgRogueDetection.getMacAddress(), traceOrgRogueDetection.getIpAddress());

                    if(traceOrgSubnetIpDetails != null)
                    {
                        traceOrgSubnetIpDetails.setAuthenticity("-");

                        traceOrgSubnetIpDetailsRepository.save(traceOrgSubnetIpDetails);
                    }

                    deleteStatus = !traceOrgRogueDetectionRepository.exists(macId);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return deleteStatus;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * get individual data based on id.
     * @param id
     * @param traceOrgRogueDetections
     */
    private void getSelectedIDData(String id, List<TraceOrgRogueDetection> traceOrgRogueDetections)
    {
        try
        {
            if(id != null && !id.isEmpty())
            {
                String[] ids = id.split(",");

                for(String individualID : ids)
                {
                    TraceOrgRogueDetection traceOrgRogueDetection = traceOrgRogueDetectionRepository.findOne(Long.parseLong(individualID));

                    traceOrgRogueDetections.add(traceOrgRogueDetection);
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
     * Get data based on authenticity.
     * @param url
     * @param traceOrgRogueDetections
     */
    private void getSelectedAuthenticityData(String url, List<TraceOrgRogueDetection> traceOrgRogueDetections)
    {
        try
        {
            if(url != null)
            {
                switch (url)
                {
                    case TraceOrgCommonConstants.ROGUE_DETECTION_DETAILS:
                    case TraceOrgCommonConstants.ALL_ROGUE_DETECTION_DETAILS:

                        traceOrgRogueDetections.addAll(traceOrgRogueDetectionRepository.findAll());

                        break;

                    case TraceOrgCommonConstants.DISCOVERED_ROGUE_DETECTION_DETAILS:

                        traceOrgRogueDetections.addAll(traceOrgRogueDetectionRepository.findByAuthenticity(TraceOrgCommonConstants.DISCOVERED.toLowerCase()));

                        break;

                    case TraceOrgCommonConstants.TRUSTED_ROGUE_DETECTION_DETAILS:

                        traceOrgRogueDetections.addAll(traceOrgRogueDetectionRepository.findByAuthenticity(TraceOrgCommonConstants.TRUSTED.toLowerCase()));

                        break;

                    case TraceOrgCommonConstants.INDIVIDUAL_ROGUE_DETECTION_DETAILS:

                        traceOrgRogueDetections.addAll(traceOrgRogueDetectionRepository.findByAuthenticity(TraceOrgCommonConstants.ROGUE.toLowerCase()));

                        break;
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
     * Update the authenticity of particular mac.
     * @param macId
     * @param rogueStatus
     * @param accessToken
     * @return
     */
    private boolean updateAuthenticity(long macId, boolean rogueStatus, String accessToken)
    {
        boolean result = false;

        try
        {
            traceOrgRogueDetection = traceOrgRogueDetectionRepository.findOne(macId);

            if(traceOrgRogueDetection != null)
            {
                TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

                traceOrgSubnetIpDetails = traceOrgSubnetIpDetailsRepository.findByMacAddressAndIpAddress(traceOrgRogueDetection.getMacAddress(), traceOrgRogueDetection.getIpAddress());

                if(traceOrgSubnetIpDetails != null)
                {
                    if(rogueStatus)
                    {
                        traceOrgSubnetIpDetails.setAuthenticity("rogue");
                    }
                    else
                    {
                        traceOrgSubnetIpDetails.setAuthenticity("trusted");
                    }

                    traceOrgSubnetIpDetailsRepository.save(traceOrgSubnetIpDetails);
                }

                if(rogueStatus)
                {
                    traceOrgRogueDetection.setAuthenticity("rogue");

                    traceOrgEvent.setEventType("IP Mark As Rogue");

                    traceOrgEvent.setEventContext("IP "+traceOrgRogueDetection.getIpAddress()+" Mark as Rogue in IP Address Manager.");
                }
                else
                {
                    traceOrgRogueDetection.setAuthenticity("trusted");

                    traceOrgEvent.setEventType("IP Mark As Trusted");

                    traceOrgEvent.setEventContext("IP "+traceOrgRogueDetection.getIpAddress()+" Mark as Trusted in IP Address Manager.");
                }

                traceOrgRogueDetectionRepository.save(traceOrgRogueDetection);

                //EVENT LOG

                traceOrgEvent.setTimestamp(new Date());

                traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                traceOrgEvent.setSeverity(1);

                traceOrgService.insert(traceOrgEvent);

                result = Boolean.TRUE;
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
     * Check the import trusted mac csv.
     * @param csvRow
     * @return
     */
    private boolean checkTrustedMACAddressFileData(CsvRow csvRow)
    {
        boolean result = false;

        try
        {
            if(csvRow.getField(0).contains("MAC Address") && csvRow.getField(1).contains("IP Address"))
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
     * IPAM-145 : System should have rogue device detection capability
     * build pdf rogue detection report.
     * @param traceOrgRogueDetections
     * @return
     */
    private String buildPDFReport(List<TraceOrgRogueDetection> traceOrgRogueDetections)
    {
        String fileName = null;

        try
        {
            List<HashMap<String, Object>> authenticitySummaryObject = new ArrayList<>();

            LinkedHashSet<String> columns = new LinkedHashSet<String>()
            {{
                add("Mac Address");

                add("IP Address");

                add("Discovered At");

                add("NIC Type");

                add("Authenticity");
            }};

            List<Object> pdfResults = new ArrayList<>();

            List<Object> pdfResult;

            Integer discoveredIp = 0;

            Integer rogueIp = 0;

            Integer trustedIp = 0;

            for(TraceOrgRogueDetection traceOrgRogueDetection : traceOrgRogueDetections)
            {
                if(traceOrgRogueDetection.getAuthenticity().equalsIgnoreCase(TraceOrgCommonConstants.DISCOVERED))
                {
                    discoveredIp++;
                }
                else if(traceOrgRogueDetection.getAuthenticity().equalsIgnoreCase(TraceOrgCommonConstants.ROGUE))
                {
                    rogueIp++;
                }
                else if(traceOrgRogueDetection.getAuthenticity().equalsIgnoreCase(TraceOrgCommonConstants.TRUSTED))
                {
                    trustedIp++;
                }

                pdfResult = new ArrayList<>();

                pdfResult.add(traceOrgRogueDetection.getMacAddress());

                pdfResult.add(traceOrgRogueDetection.getIpAddress());

                pdfResult.add(traceOrgRogueDetection.getDiscoveredAt());

                pdfResult.add(traceOrgRogueDetection.getNicType());

                pdfResult.add(traceOrgRogueDetection.getAuthenticity());

                pdfResults.add(pdfResult);
            }

            HashMap<String, Object> discoveredIpSummary = new HashMap<>();

            discoveredIpSummary.put("status","Discovered (%)");

            discoveredIpSummary.put("value",new DecimalFormat("#.00").format((double)(discoveredIp*100)/traceOrgRogueDetections.size()));

            HashMap<String, Object> rogueIpSummary = new HashMap<>();

            rogueIpSummary.put("status","Rogue (%)");

            rogueIpSummary.put("value",new DecimalFormat("#.00").format((double)(rogueIp*100)/traceOrgRogueDetections.size()));

            HashMap<String, Object> trustedIpSummary = new HashMap<>();

            trustedIpSummary.put("status","Trusted (%)");

            trustedIpSummary.put("value",new DecimalFormat("#.00").format((double)(trustedIp*100)/traceOrgRogueDetections.size()));

            authenticitySummaryObject.add(discoveredIpSummary);

            authenticitySummaryObject.add(rogueIpSummary);

            authenticitySummaryObject.add(trustedIpSummary);

            HashMap<String, Object> results = new HashMap<>();

            results.put("grid-result", pdfResults);

            results.put("columns",columns);

            results.put("ipSummary", authenticitySummaryObject);

            List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

            visualizationResults.add(results);

            HashMap<String, Object> params = new HashMap<String, Object>();

            params.put("title", "Authenticity Summary");

            HashMap<String, Object> gridReport = new HashMap<>();

            gridReport.put("Title", "Rogue Detection Details ");

            fileName = "Rogue Detection Details "+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".pdf";

            fileName = fileName.replace(" ","_").replace(":","_").replace(",","");

            TraceOrgPDFBuilder.addGridReport(1, visualizationResults, params, fileName, gridReport);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return fileName;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * build csv rogue detection report.
     * @param traceOrgRogueDetections
     * @return
     */
    private String buildCSVReport(List<TraceOrgRogueDetection> traceOrgRogueDetections)
    {
        String fileName = null;

        try
        {
            CsvWriter csvWriter = new CsvWriter();

            Collection<String[]> data = new ArrayList<>();

            data.add(new String[] { "MAC Address","IP Address","Discovered At","NIC Type","Authenticity" });

            for(TraceOrgRogueDetection traceOrgRogueDetection : traceOrgRogueDetections)
            {
                String nicType = "";

                if(traceOrgRogueDetection.getNicType() != null && !traceOrgRogueDetection.getNicType().isEmpty())
                {
                    nicType = traceOrgRogueDetection.getNicType();
                }

                data.add(new String[] {traceOrgRogueDetection.getMacAddress(), traceOrgRogueDetection.getIpAddress(), traceOrgRogueDetection.getDiscoveredAt(), nicType, traceOrgRogueDetection.getAuthenticity()});
            }

            fileName = ("Rogue Detection Details_"+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".csv").replace(" ","_").replace(":","_").replace(",","");

            File file = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR + fileName);

            csvWriter.write(file, StandardCharsets.UTF_8, data);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return fileName;
    }
}
