package com.motadata.traceorg.ipam.services.impl.settings;

import com.motadata.traceorg.ipam.dao.TraceOrgDao;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgCustomColumn;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgCustomColumnRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import de.siegmar.fastcsv.reader.CsvRow;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class UnitTestTraceOrgCustomColumnServiceImplTest
{
    @InjectMocks
    private TraceOrgCustomColumnServiceImpl traceOrgCustomColumnService;

    @Mock
    private TraceOrgCustomColumnRepository traceOrgCustomColumnRepository;

    @Mock
    private TraceOrgDao traceOrgDao;

    @Mock
    private TraceOrgService traceOrgService;

    @InjectMocks
    private TraceOrgCommonUtil traceOrgCommonUtil;

    private TraceOrgCustomColumn customColumn;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {

        traceOrgCustomColumnService=new TraceOrgCustomColumnServiceImpl(traceOrgCustomColumnRepository, traceOrgDao, traceOrgService);

        customColumn = new TraceOrgCustomColumn();

        customColumn.setId(1L);

        customColumn.setColumnName("TestColumn");

        traceOrgCustomColumnService = new TraceOrgCustomColumnServiceImpl(traceOrgCustomColumnRepository, traceOrgDao, traceOrgService);

        traceOrgCommonUtil = new TraceOrgCommonUtil();

        Field repositoryField = TraceOrgCommonUtil.class.getDeclaredField("traceOrgCustomColumnRepository");

        repositoryField.setAccessible(true);

        repositoryField.set(traceOrgCommonUtil, traceOrgCustomColumnRepository);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateCustomColumnSuccess()
    {
        when(traceOrgCustomColumnRepository.save(any(TraceOrgCustomColumn.class))).thenReturn(customColumn);

        HashMap<String, Object> result = traceOrgCustomColumnService.createCustomColumn(customColumn, "Header");

        assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));

        assertEquals(TraceOrgMessageConstants.CUSTOM_COLUMN_ADD_SUCCESS, result.get(TraceOrgCommonConstants.MESSAGE));
    }

    @Test
    public void testCreateMaxCustomColumn()
    {
        TraceOrgCustomColumn mockColumn = new TraceOrgCustomColumn();

        mockColumn.setColumnAt("columnA");

        List<TraceOrgCustomColumn> mockList = new ArrayList<>();

        for (int i = 0; i < 10; i++)
        {
            mockList.add(new TraceOrgCustomColumn());
        }

        when(traceOrgCustomColumnRepository.findByColumnAt("columnA")).thenReturn(mockList);

        HashMap<String, Object> result = traceOrgCustomColumnService.createCustomColumn(mockColumn, "Header");

        assertEquals(TraceOrgCommonConstants.FALSE, result.get(TraceOrgCommonConstants.SUCCESS));

        assertEquals(TraceOrgMessageConstants.MAX_CUSTOM_COLUMN, result.get(TraceOrgCommonConstants.MESSAGE));
    }

    @Test
    public void testCreateCustomColumnFailure()
    {
        when(traceOrgCustomColumnRepository.existsByColumnName(Mockito.any())).thenReturn(true);

        HashMap<String, Object> result = traceOrgCustomColumnService.createCustomColumn(customColumn, "Header");

        assertEquals(TraceOrgCommonConstants.FALSE, result.get(TraceOrgCommonConstants.SUCCESS));

        assertEquals(TraceOrgMessageConstants.COLUMN_ALREADY_EXIST, result.get(TraceOrgCommonConstants.MESSAGE));
    }

    @Test
    public void testRemoveCustomColumnSuccess()
    {
        when(traceOrgCustomColumnRepository.findOne(1L)).thenReturn(customColumn);

        doNothing().when(traceOrgDao).removeKeyFromCustomColumns(anyString());

        doNothing().when(traceOrgCustomColumnRepository).delete(1L);

        HashMap<String, Object> result = traceOrgCustomColumnService.removeCustomColumn(1L);

        assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));

        assertEquals(TraceOrgMessageConstants.CUSTOM_COLUMN_DELETE_SUCCESS, result.get(TraceOrgCommonConstants.MESSAGE));
    }

    @Test
    public void testListAllCustomColumnSuccess()
    {
        List<TraceOrgCustomColumn> customColumnList = Collections.singletonList(customColumn);

        when(traceOrgCustomColumnRepository.findAll()).thenReturn(customColumnList);

        HashMap<String, Object> result = traceOrgCustomColumnService.listAllCustomColumn();

        assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));

        assertEquals(customColumnList, result.get(TraceOrgCommonConstants.DATA));
    }

    @Test
    public void testGenerateCsvSuccess()
    {
        when(traceOrgCustomColumnRepository.findByColumnAt("subnetIp"))
                .thenReturn(Collections.singletonList(customColumn));

        String fileName = traceOrgCustomColumnService.generateCsv();

        assertNotNull(fileName);

        assertTrue(fileName.endsWith(".csv"));
    }

    @Test
    public void testCheckSubnetIPFileDataValidDataReturnsFalse() throws Exception
    {
        List<String> customCols = Arrays.asList("Custom1");

        TraceOrgCustomColumn column1 = new TraceOrgCustomColumn();

        column1.setColumnName("Custom1");

        TraceOrgCustomColumn column2 = new TraceOrgCustomColumn();

        column2.setColumnName("Custom2");

        List<TraceOrgCustomColumn> mockColumns = Arrays.asList(column1, column2);

        when(traceOrgCustomColumnRepository.findByColumnAt("subnetIp")).thenReturn(mockColumns);

        List<String> fields = new ArrayList<>(Arrays.asList(
                "IP Address", "Mac Address", "Status", "IP To Dns",
                "Dns To Ip", "Vendor", "Authenticity", "Last Alive Time"
        ));

        fields.addAll(customCols);

        CsvRow csvRow = createCsvRow(1L, null, fields);

        boolean result = traceOrgCommonUtil.checkSubnetIPFileData(csvRow);

        assertFalse(result);
    }

    @Test
    public void testCheckSubnetIPFileDataValidDataReturnsTrue() throws Exception
    {
        List<String> customCols = Arrays.asList("Custom1", "Custom2");

        TraceOrgCustomColumn column1 = new TraceOrgCustomColumn();

        column1.setColumnName("Custom1");

        TraceOrgCustomColumn column2 = new TraceOrgCustomColumn();

        column2.setColumnName("Custom2");

        List<TraceOrgCustomColumn> mockColumns = Arrays.asList(column1, column2);

        when(traceOrgCustomColumnRepository.findByColumnAt("subnetIp")).thenReturn(mockColumns);

        List<String> fields = new ArrayList<>(Arrays.asList(
                "IP Address", "Mac Address", "Status", "IP To Dns",
                "Dns To Ip", "Vendor", "Authenticity", "Last Alive Time"
        ));

        fields.addAll(customCols);

        CsvRow csvRow = createCsvRow(1L, null, fields);

        boolean result = traceOrgCommonUtil.checkSubnetIPFileData(csvRow);

        assertTrue(result);
    }

    private CsvRow createCsvRow(long lineNumber, Map<String, Integer> headerMap, List<String> fields) throws Exception
    {
        Constructor<CsvRow> constructor = CsvRow.class.getDeclaredConstructor(long.class, Map.class, List.class);

        constructor.setAccessible(true);

        return constructor.newInstance(lineNumber, headerMap, fields);
    }
}
