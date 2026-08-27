package com.motadata.traceorg.ipam.util;

import ar.com.fdvs.dj.core.DJConstants;
import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.builders.SubReportBuilder;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.Subreport;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.report.TraceOrgReportColumn;
import com.motadata.traceorg.ipam.entity.report.TraceOrgReportHeaderFooter;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.jfree.ui.Align;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@SuppressWarnings("ALL")
public class TraceOrgPDFBuilder
{
    private static final int MEDIUM = 12;

    private static String COLUMNS = "columns";

    private static String GRID_RESULT = "grid-result";

    private static String REPORT_TIMELINE = "timeline";

    private static String REPORT_COLUMNS = "columns";

    private static String REPORT_TITLE = "title";

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgPDFBuilder.class, "PDF Builder");

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Set the title of chart for the reprot.
     * @param counter
     * @param visualizationResults
     * @param params
     * @param fileName
     * @param gridReport
     */
    public static void addGridReport(int counter, List<HashMap<String, Object>> visualizationResults, HashMap<String, Object> params, String fileName, HashMap<String, Object> gridReport)
    {
        TraceOrgReportHeaderFooter headerFooter = getHeaderFooter();

        DynamicReportBuilder dynamicReportBuilder = new DynamicReportBuilder();

        setHeaderFooter(dynamicReportBuilder, headerFooter);

        final int[] parentCounter = {counter};

        try
        {
            String reportFor = null;

            String ipSummary = null;

            String mainTitle = null;

            for(HashMap<String,Object> visualizationResult : visualizationResults)
            {
                HashMap<String, Object> result = new HashMap<>();

                List<List<Object>> gridResult = (List<List<Object>>) visualizationResult.get(GRID_RESULT);

                if(visualizationResult.containsKey("logFor"))
                {
                    reportFor = visualizationResult.get("logFor").toString();
                }

                if(visualizationResult.containsKey("ipSummary"))
                {
                    ipSummary = "ipSummary";
                }

                List<TraceOrgReportColumn> gridColumns = new ArrayList<>();

                if (visualizationResult.get(COLUMNS) instanceof LinkedHashSet)
                {
                    LinkedHashSet<String> columns = (LinkedHashSet<String>) visualizationResult.get(COLUMNS);

                    if (columns != null && columns.size() > 0)
                    {
                        final int[] indexer = {1};

                        for(String column : columns)
                        {
                            gridColumns.add(new TraceOrgReportColumn(column, "value" + indexer[0]));

                            indexer[0]++;
                        }
                    }
                }
                else
                {
                    List<String> columns = (List<String>) visualizationResult.get(COLUMNS);

                    if (columns != null && columns.size() > 0)
                    {
                        final int[] indexer = {1};

                        for(String column : columns)
                        {
                            gridColumns.add(new TraceOrgReportColumn(column, "value" + indexer[0]));

                            indexer[0]++;
                        }
                    }
                }

                result.put(REPORT_COLUMNS, gridColumns);

                result.put(REPORT_TITLE, gridReport.get("Title"));

                result.put(REPORT_TIMELINE, TraceOrgCommonConstants.SIMPLE_DATE_FORMAT.format(new Date()));

                if (gridResult != null && gridResult.size() > 0)
                {
                    LinkedList<HashMap<String, Object>> finalGridResult = new LinkedList<>();

                    for(List<Object> parentRow : gridResult)
                    {

                        HashMap<String, Object> row = new HashMap<>();

                        final int[] indexer = {1};

                        for (Object childRow : parentRow)
                        {
                            row.put("value" + indexer[0], childRow);

                            ++indexer[0];
                        }

                        finalGridResult.add(row);
                    }

                    DynamicReport dynamicGridReport = null;

                    DynamicReport dynamicChartReport = null;


                    if(reportFor!=null &&  reportFor.equalsIgnoreCase("EVENT"))
                    {
                        dynamicGridReport  = TraceOrgGridReportBuilder.eventBuild(result);
                    }
                    else if(reportFor!=null &&  reportFor.equalsIgnoreCase("VENDOR_REPORT"))
                    {
                        dynamicGridReport  = TraceOrgGridReportBuilder.vendorSummaryBuild(result);
                    }
                    else if(reportFor!=null &&  reportFor.equalsIgnoreCase("IP_REPORT"))
                    {
                        dynamicGridReport  = TraceOrgGridReportBuilder.ipSummaryBuild(result);
                    }
                    else
                    {
                        dynamicGridReport = TraceOrgGridReportBuilder.build(result);
                    }

                    if(!params.isEmpty())
                    {
                        mainTitle = String.valueOf(params.remove("title"));
                    }

                    if(ipSummary!=null)
                    {
                        HashMap<String,Object> results = new HashMap<>();

                        List<TraceOrgReportColumn> traceOrgReportColumns = new ArrayList<>();

                        traceOrgReportColumns.add(new TraceOrgReportColumn("value","value"));

                        results.put("series",traceOrgReportColumns);

                        results.put("timeline","");

                        results.put("x-axis",new TraceOrgReportColumn("status","status"));

                        results.put("title","IP Summary");

                        results.put("x-axis-label","status");

                        dynamicChartReport = prepareHistoricalPieReport((List<HashMap<String, Object>>)visualizationResult.get("ipSummary"),"status","value",results, mainTitle);

                        if (dynamicGridReport != null)
                        {
                            prepareReportChart(dynamicReportBuilder, dynamicChartReport, parentCounter[0],(List<HashMap<String, Object>>)visualizationResult.get("ipSummary") , params,dynamicGridReport,++parentCounter[0],finalGridResult);

                            parentCounter[0] = parentCounter[0] + 2;
                        }
                    }
                    else
                    {
                        if (dynamicGridReport != null)
                        {
                            prepareReport(dynamicReportBuilder, dynamicGridReport, parentCounter[0], finalGridResult, params);

                            ++parentCounter[0];
                        }
                    }
                }
            }

            DynamicReport dr = dynamicReportBuilder.build();

            JasperPrint jasperPrint = DynamicJasperHelper.generateJasperPrint(dr, getLayoutManager(), params);

            if (jasperPrint != null)
            {
                exportPDF(jasperPrint, TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR+ fileName);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

    }

    private static ClassicLayoutManager getLayoutManager()
    {
        return new ClassicLayoutManager();
    }

    private static void prepareReport(DynamicReportBuilder dynamicReportBuilder, DynamicReport dynamicReport, int counter, List result, HashMap<String, Object> params)
    {
        try
        {
            if (dynamicReport != null)
            {
                dynamicReport.setWhenNoDataType(DJConstants.WHEN_NO_DATA_TYPE_ALL_SECTIONS_NO_DETAIL);

                String dataSourcePath = "widgetContext_dataSource_" + counter;

                JRDataSource dataSource = new JRBeanCollectionDataSource((Collection<?>) result);

                params.put(dataSourcePath, dataSource);

                Subreport subreport = new SubReportBuilder().setStartInNewPage(Boolean.FALSE).setDataSource(DJConstants.DATA_SOURCE_ORIGIN_PARAMETER, DJConstants.DATA_SOURCE_TYPE_JRDATASOURCE, dataSourcePath).setDynamicReport(dynamicReport, getLayoutManager()).build();

                if (counter > 1)
                {
                    subreport.setStartInNewPage(Boolean.TRUE);
                }

                dynamicReportBuilder.addConcatenatedReport(subreport);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    private static void prepareReportChart(DynamicReportBuilder dynamicReportBuilder, DynamicReport dynamicReport, int counter, List result, HashMap<String, Object> params,DynamicReport dynamicReport2,int counter2,List gridResult)
    {
        try
        {
            if (dynamicReport != null)
            {
                dynamicReport.setWhenNoDataType(DJConstants.WHEN_NO_DATA_TYPE_ALL_SECTIONS_NO_DETAIL);

                String dataSourcePath = "widgetContext_dataSource_" + counter;

                JRDataSource dataSource = new JRBeanCollectionDataSource((Collection<?>) result);

                params.put(dataSourcePath, dataSource);

                Subreport subreport = new SubReportBuilder().setStartInNewPage(Boolean.FALSE).setDataSource(DJConstants.DATA_SOURCE_ORIGIN_PARAMETER, DJConstants.DATA_SOURCE_TYPE_JRDATASOURCE, dataSourcePath).setDynamicReport(dynamicReport, getLayoutManager()).build();

                /*if (counter > 1)
                {
                    subreport.setStartInNewPage(Boolean.TRUE);
                }*/

                String dataSourcePath2 = "widgetContext_dataSource_" + counter2;

                JRDataSource dataSource2= new JRBeanCollectionDataSource((Collection<?>) gridResult);

                params.put(dataSourcePath2, dataSource2);

                Subreport subreport1 = new SubReportBuilder().setStartInNewPage(Boolean.FALSE).setDataSource(DJConstants.DATA_SOURCE_ORIGIN_PARAMETER, DJConstants.DATA_SOURCE_TYPE_JRDATASOURCE, dataSourcePath2).setDynamicReport(dynamicReport2, getLayoutManager()).build();

                subreport1.setStartInNewPage(Boolean.TRUE);

                dynamicReportBuilder.addConcatenatedReport(subreport);

                dynamicReportBuilder.addConcatenatedReport(subreport1);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    private static void setHeaderFooter(DynamicReportBuilder dynamicReportBuilder, TraceOrgReportHeaderFooter headerFooter)
    {
        try
        {
            dynamicReportBuilder.setMargins(15, 15, 15, 15);

            if (headerFooter.isLandscape())
            {
                dynamicReportBuilder.setPageSizeAndOrientation(Page.Page_A4_Landscape());
            }
            else
            {
                dynamicReportBuilder.setPageSizeAndOrientation(Page.Page_A4_Portrait());
            }

            dynamicReportBuilder.setTitleStyle(getReportTitleStyle());

            dynamicReportBuilder.setTitle(headerFooter.getTitle());

            setLogoImage();

            dynamicReportBuilder.addImageBanner(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Images"+TraceOrgCommonConstants.PATH_SEPARATOR+"logo.png", 75, 75, (byte) Align.TOP_LEFT);

            dynamicReportBuilder.setWhenNoDataAllSectionNoDetail();

            dynamicReportBuilder.addAutoText(AutoText.AUTOTEXT_PAGE_X_SLASH_Y, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_CENTER);

            if (headerFooter.getPoweredBy() != null && headerFooter.getPoweredBy().length() > 0)
            {
                dynamicReportBuilder.addAutoText("powered by " + headerFooter.getPoweredBy(), AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_RIGHT, 275);
            }
            else
            {
                dynamicReportBuilder.addAutoText("powered by " + TraceOrgCommonConstants.WHITE_LABEL, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_RIGHT, 275);
            }

            dynamicReportBuilder.addAutoText(new SimpleDateFormat("MMM dd,yyyy hh:mm:ss aaa").format(Calendar.getInstance().getTime()), AutoText.POSITION_FOOTER, AutoText.ALIGMENT_LEFT, 150);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    public static boolean setLogoImage()
    {
        BufferedImage logoImage = null;

        boolean imageWriteStatus = false;

        try
        {
            File file = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Images");

            if (!file.exists())
            {
                file.mkdirs();
            }

            File readLogoImageFilePath = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+TraceOrgCommonConstants.LOGO_DIR+TraceOrgCommonConstants.PATH_SEPARATOR+"logo.png");

            logoImage = ImageIO.read(readLogoImageFilePath);

            File writeLogoImageFilePath = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Images/logo.png");

            imageWriteStatus = ImageIO.write(logoImage, "png", writeLogoImageFilePath);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return imageWriteStatus;
    }

    public static Style getReportTitleStyle()
    {
        Style titleStyle = new Style();
        titleStyle.setFont(new ar.com.fdvs.dj.domain.constants.Font(MEDIUM, ar.com.fdvs.dj.domain.constants.Font._FONT_ARIAL, true, false, false));
        titleStyle.setHorizontalAlign(HorizontalAlign.LEFT);
        titleStyle.setVerticalAlign(VerticalAlign.MIDDLE);
        titleStyle.setTextColor(Color.BLACK);
        titleStyle.setTransparency(Transparency.OPAQUE);

        return titleStyle;
    }

    public static TraceOrgReportHeaderFooter getHeaderFooter()
    {
        TraceOrgReportHeaderFooter headerFooter = new TraceOrgReportHeaderFooter();

        try
        {
            headerFooter.setLandscape(Boolean.TRUE);
        }
        catch (Exception exception)
        {

        }

        return headerFooter;
    }


    public static void exportPDF(JasperPrint jp, String path)
    {
        FileOutputStream fileOutputStream = null;

        try
        {
            JRPdfExporter exporter = new JRPdfExporter();

            File outputFile = new File(path);

            File directory = outputFile.getParentFile();

            if (!directory.exists())
            {
                directory.mkdirs();
            }

            fileOutputStream = new FileOutputStream(outputFile);

            SimpleExporterInput simpleExporterInput = new SimpleExporterInput(jp);

            OutputStreamExporterOutput simpleOutputStreamExporterOutput = new SimpleOutputStreamExporterOutput(fileOutputStream);

            exporter.setExporterInput(simpleExporterInput);

            exporter.setExporterOutput(simpleOutputStreamExporterOutput);

            exporter.exportReport();

        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        finally
        {
            if (fileOutputStream != null)
            {
                try
                {
                    fileOutputStream.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Set the title of the chart.
     * @param rowResults
     * @param xAxis
     * @param yAxis
     * @param result
     * @param mainTitle
     * @return
     */
    private static DynamicReport prepareHistoricalPieReport(List<HashMap<String, Object>> rowResults, String xAxis, String yAxis, HashMap<String, Object> result, String mainTitle)
    {
        DynamicReport dynamicReport = null;

        try
        {
            if (rowResults != null)
            {
                if (rowResults.size() > TraceOrgAbstractReportBuilder.PIE_CHART_TICK_LIMIT)
                {
                    int indexer = 0;

                    double value = 0;

                    List<HashMap<String, Object>> filteredRows = new ArrayList<>();

                    for (HashMap<String, Object> row : rowResults)
                    {
                        try
                        {
                            if (indexer <= TraceOrgAbstractReportBuilder.PIE_CHART_TICK_LIMIT)
                            {
                                String partialValue = row.get(yAxis).toString();

                                if(partialValue.matches("[0-9]+"))
                                {
                                    row.put("value", Double.valueOf(yAxis).longValue());
                                }
                                else
                                {
                                    row.put("value", value);
                                }

                                row.put(xAxis, TraceOrgCommonUtil.getStringValue(row.get(xAxis)) + " (" + row.get("value") + ")");

                                filteredRows.add(row);
                            }
                            else
                            {
                                value += Double.valueOf(TraceOrgCommonUtil.getStringValue(row.get(yAxis)));
                            }

                            indexer++;
                        }
                        catch (Exception ignored)
                        {
                            //@INFO: ignoring exception getting this due to parsing of value
                        }
                    }

                    HashMap<String, Object> row = new HashMap<>();

                    String partialValue = TraceOrgCommonUtil.getStringValue(value);

                    if(partialValue.matches("[0-9]+"))
                    {
                        row.put(xAxis, "Other (" + Double.valueOf(value).longValue() + ")");
                    }
                    else
                    {
                        row.put(xAxis, "Other (" + value + ")");
                    }


                    filteredRows.add(row);

                    rowResults.clear();

                    rowResults.addAll(filteredRows);
                }
                else
                {
                    HashMap<String, Color> defaultAvailabilityColors = TraceOrgGridReportBuilder.getAvailabilityStatusDescriptionColors();

                    List<Color> availabilityColors = new ArrayList<>();

                    DecimalFormat decimalFormat = new DecimalFormat("#.00");

                    rowResults.forEach(row ->
                    {
                        try
                        {
                            String partialValue = row.get(yAxis).toString();

                            if(partialValue.matches("[0-9]+"))
                            {
                                long value = Double.valueOf(partialValue).longValue();

                                row.put("value", value);

                                String xAxisTick = TraceOrgCommonUtil.getStringValue(row.get(xAxis));

                                row.put(xAxis, xAxisTick + " (" + row.get("value") + ")");

                                //if we get value 0 we dont need color for that series so ignoring those.
                                if (value > 0 && defaultAvailabilityColors.containsKey(xAxisTick))
                                {
                                    availabilityColors.add(defaultAvailabilityColors.get(xAxisTick));
                                }
                            }
                            else
                            {
                                double value = Double.valueOf(TraceOrgCommonUtil.getStringValue(row.get(yAxis)));

                                row.put("value", value);

                                String xAxisTick = TraceOrgCommonUtil.getStringValue(row.get(xAxis));

                                row.put(xAxis, xAxisTick + " (" + row.get("value") + ")");

                                //if we get value 0 we dont need color for that series so ignoring those.
                                if (value > 0 && defaultAvailabilityColors.containsKey(xAxisTick))
                                {
                                    availabilityColors.add(defaultAvailabilityColors.get(xAxisTick));
                                }
                            }
                        }
                        catch (Exception ignored)
                        {
                            //@INFO: ignoring exception getting this due to parsing of value
                        }
                    });

                    if (availabilityColors.size() > 0)
                    {
                        result.put(TraceOrgAbstractReportBuilder.SERIES_COLORS, availabilityColors);
                    }
                }

                dynamicReport = TraceOrgGridReportBuilder.getInstance().pieChartBuild(result, mainTitle);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return dynamicReport;
    }
}
