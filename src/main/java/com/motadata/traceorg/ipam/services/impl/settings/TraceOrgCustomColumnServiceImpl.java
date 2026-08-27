package com.motadata.traceorg.ipam.services.impl.settings;

import com.motadata.traceorg.ipam.dao.TraceOrgDao;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgCustomColumn;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgCustomColumnRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.settings.TraceOrgCustomColumnService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import de.siegmar.fastcsv.writer.CsvWriter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * IPAM-160 IPAM Roadmap : The solution must be flexible to allow the creation of custom fields for objects in IPAM. This must be configurable via the Web GUI.
 * Add and remove custom columns.
 */
@Service
public class TraceOrgCustomColumnServiceImpl implements TraceOrgCustomColumnService
{

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgCustomColumnServiceImpl.class, "Custom Column Service");

    private TraceOrgCustomColumnRepository traceOrgCustomColumnRepository;

    private TraceOrgDao traceOrgDao;

    private TraceOrgService traceOrgService;

    public TraceOrgCustomColumnServiceImpl(TraceOrgCustomColumnRepository traceOrgCustomColumnRepository, TraceOrgDao traceOrgDao, TraceOrgService traceOrgService) {
        this.traceOrgCustomColumnRepository = traceOrgCustomColumnRepository;
        this.traceOrgDao = traceOrgDao;
        this.traceOrgService = traceOrgService;
    }

    private static final int OFF = 0;

    private static final int ON = 1;

    @Override
    public HashMap<String, Object> createCustomColumn(TraceOrgCustomColumn customColumn, String header)
    {

        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgCustomColumn> customColumnList = traceOrgCustomColumnRepository.findByColumnAt(customColumn.getColumnAt());

            if(customColumnList.size()==10)
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.MAX_CUSTOM_COLUMN);
            }
            else
            {
                if(!traceOrgCustomColumnRepository.existsByColumnName(customColumn.getColumnName()))
                {
                    traceOrgCustomColumnRepository.save(customColumn);

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.CUSTOM_COLUMN_ADD_SUCCESS);
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.COLUMN_ALREADY_EXIST);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> listAllCustomColumn()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgCustomColumn> customColumnList = traceOrgCustomColumnRepository.findAll();

            if(customColumnList != null)
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.DATA, customColumnList);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> removeCustomColumn(Long id)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            TraceOrgCustomColumn customColumn = traceOrgCustomColumnRepository.findOne(id);

            traceOrgDao.removeKeyFromCustomColumns(customColumn.getColumnName());

            traceOrgCustomColumnRepository.delete(id);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.CUSTOM_COLUMN_DELETE_SUCCESS);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return result;
    }

    @Override
    public String generateCsv()
    {
        try
        {
            String fileName = ("Subnet Ip Summary "+"_"+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".csv").replace(" ","_").replace(":","_").replace(",","");

            File file = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR + fileName);

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

            data.add(headers.toArray(new String[0]));

            CsvWriter csvWriter = new CsvWriter();

            List<String> dummyData = new ArrayList<>(Arrays.asList(
                    "192.168.1.1", "00:1A:2B:3C:4D:5E", "Active", "host1.local",
                    "192.168.1.1", "Cisco", "trusted", "2025-04-03 10:15:30"
            ));

            for (int i = 0; i < customColumnNames.size(); i++)
            {
                dummyData.add("N/A");
            }

            data.add(dummyData.toArray(new String[0]));

            csvWriter.write(file, StandardCharsets.UTF_8, data);

            return fileName;
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return "";
    }
}
