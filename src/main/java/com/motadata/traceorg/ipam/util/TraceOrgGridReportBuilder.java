package com.motadata.traceorg.ipam.util;

import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.chart.DJChart;
import ar.com.fdvs.dj.domain.chart.builder.DJPie3DChartBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.report.TraceOrgReportColumn;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 1/5/18.
 */
public class TraceOrgGridReportBuilder
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgGridReportBuilder.class, "Report Builder");

    private static final TraceOrgGridReportBuilder m_instance = new TraceOrgGridReportBuilder();

    static int MARGIN_TOP = 1;

    static int MARGIN_BOTTOM = 1;

    static int MARGIN_LEFT = 0;

    static int MARGIN_RIGHT = 0;

    static final int[] DEFAULT_SERIES_COLORS = {-9979940, -142336, -8079519, -3389624, -3308883, -13680524, -12284339, -4736961, -4622273, -4637123, -7261849, -6710887, -757040, -4026360, -14979410, -1006838, -366592, -6160347, -2621325, -2338899, -5635841, -9830145, -16756497, -14966302, -16733271, -16741888, -10442473, -5979136, -9599132, -10193273, -9019254, -7898802, -6678950, -4236779, -12349217, -8727011, -16724737, -8869348};

    private static final int MEDIUM = 12;

    private static final int SMALL = 10;

    private static final int SMALLER = 8;

    private TraceOrgGridReportBuilder()
    {

    }

    public static TraceOrgGridReportBuilder getInstance()
    {
        return m_instance;
    }



    /*
    * @INFO: required properties: columns
    * */
    public static DynamicReport build(HashMap<String, Object> reportData)
    {
        DynamicReport dynamicReport = null;

        try
        {
            if (reportData != null)
            {
                String title = (String) reportData.get("title");

                boolean isPageBreak = false;

                if (reportData.containsKey("isPageBreak"))
                {
                    isPageBreak = (boolean) reportData.get("isPageBreak");
                }

                List<TraceOrgReportColumn> columns = (List<TraceOrgReportColumn>) reportData.get("columns");

                if (columns != null && columns.size() > 0)
                {
                    FastReportBuilder fastReportBuilder = new FastReportBuilder();

                    fastReportBuilder.setTitleStyle(getSubReportTitleStyle());

                    fastReportBuilder.setSubtitleStyle(getSubReportSubTitleStyle());

                    fastReportBuilder.setMargins(MARGIN_TOP, MARGIN_BOTTOM, MARGIN_LEFT, MARGIN_RIGHT);

                    if (title != null && title.length() > 0)
                    {
                        fastReportBuilder.setTitle("Name : " + title);

                       /* if(reportData.get(REPORT_TIMELINE) != null)
                        {
                            fastReportBuilder.setSubtitle("Time Span: " + reportData.get(REPORT_TIMELINE));
                        }*/
                    }

                    setDefaultStyle(fastReportBuilder);

                    for (TraceOrgReportColumn column : columns)
                    {
                        AbstractColumn abstractColumn = ColumnBuilder.getNew().setColumnProperty(column.getProperty(), Object.class.getName()).setTitle(column.getPropertyLabel()).setStyle(getRowStyle()).build();

                        fastReportBuilder.addColumn(abstractColumn);
                    }

                    fastReportBuilder.setUseFullPageWidth(Boolean.TRUE).setIgnorePagination(isPageBreak);

                    dynamicReport = fastReportBuilder.build();
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return dynamicReport;
    }



    public static DynamicReport ipSummaryBuild(HashMap<String, Object> reportData)
    {
        DynamicReport dynamicReport = null;

        try
        {
            if (reportData != null)
            {
                String title = (String) reportData.get("title");

                boolean isPageBreak = false;

                if (reportData.containsKey("isPageBreak"))
                {
                    isPageBreak = (boolean) reportData.get("isPageBreak");
                }

                List<TraceOrgReportColumn> columns = (List<TraceOrgReportColumn>) reportData.get("columns");

                if (columns != null && columns.size() > 0)
                {
                    FastReportBuilder fastReportBuilder = new FastReportBuilder();

                    fastReportBuilder.setTitleStyle(getSubReportTitleStyle());

                    fastReportBuilder.setSubtitleStyle(getSubReportSubTitleStyle());

                    fastReportBuilder.setMargins(MARGIN_TOP, MARGIN_BOTTOM, MARGIN_LEFT, MARGIN_RIGHT);

                    if (title != null && title.length() > 0)
                    {
                        fastReportBuilder.setTitle("Name : " + title);

                       /* if(reportData.get(REPORT_TIMELINE) != null)
                        {
                            fastReportBuilder.setSubtitle("Time Span: " + reportData.get(REPORT_TIMELINE));
                        }*/
                    }

                    setDefaultStyle(fastReportBuilder);

                    for (TraceOrgReportColumn column : columns)
                    {
                        AbstractColumn abstractColumn = ColumnBuilder.getNew().setColumnProperty(column.getProperty(), Object.class.getName()).setTitle(column.getPropertyLabel()).setStyle(getRowStyle()).build();
                        if(columns.indexOf(column)==1)
                        {
                            fastReportBuilder.addColumn(column.getPropertyLabel(),column.getProperty(),Object.class,30);
                        }
                        else if(columns.indexOf(column)==3)
                        {
                            fastReportBuilder.addColumn(column.getPropertyLabel(),column.getProperty(),Object.class,45);
                        }
                        else if(columns.indexOf(column)==4)
                        {
                            fastReportBuilder.addColumn(column.getPropertyLabel(),column.getProperty(),Object.class,60);
                        }
                        else if(columns.indexOf(column)==7)
                        {
                            fastReportBuilder.addColumn(column.getPropertyLabel(),column.getProperty(),Object.class,20);
                        }
                        else
                        {
                            fastReportBuilder.addColumn(abstractColumn);
                        }
                    }

                    fastReportBuilder.setUseFullPageWidth(Boolean.TRUE).setIgnorePagination(isPageBreak);

                    dynamicReport = fastReportBuilder.build();
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return dynamicReport;
    }


    public static DynamicReport vendorSummaryBuild(HashMap<String, Object> reportData)
    {
        DynamicReport dynamicReport = null;

        try
        {
            if (reportData != null)
            {
                String title = (String) reportData.get("title");

                boolean isPageBreak = false;

                if (reportData.containsKey("isPageBreak"))
                {
                    isPageBreak = (boolean) reportData.get("isPageBreak");
                }

                List<TraceOrgReportColumn> columns = (List<TraceOrgReportColumn>) reportData.get("columns");

                if (columns != null && columns.size() > 0)
                {
                    FastReportBuilder fastReportBuilder = new FastReportBuilder();

                    fastReportBuilder.setTitleStyle(getSubReportTitleStyle());

                    fastReportBuilder.setSubtitleStyle(getSubReportSubTitleStyle());

                    fastReportBuilder.setMargins(MARGIN_TOP, MARGIN_BOTTOM, MARGIN_LEFT, MARGIN_RIGHT);

                    if (title != null && title.length() > 0)
                    {
                        fastReportBuilder.setTitle("Name : " + title);

                       /* if(reportData.get(REPORT_TIMELINE) != null)
                        {
                            fastReportBuilder.setSubtitle("Time Span: " + reportData.get(REPORT_TIMELINE));
                        }*/
                    }

                    setDefaultStyle(fastReportBuilder);

                    for (TraceOrgReportColumn column : columns)
                    {
                        AbstractColumn abstractColumn = ColumnBuilder.getNew().setColumnProperty(column.getProperty(), Object.class.getName()).setTitle(column.getPropertyLabel()).setStyle(getRowStyle()).build();

                        if(columns.indexOf(column)==0)
                        {
                            fastReportBuilder.addColumn(column.getPropertyLabel(),column.getProperty(),Object.class,200);
                        }
                        else
                        {
                            fastReportBuilder.addColumn(abstractColumn);
                        }
                    }

                    fastReportBuilder.setUseFullPageWidth(Boolean.TRUE).setIgnorePagination(isPageBreak);

                    dynamicReport = fastReportBuilder.build();
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return dynamicReport;
    }

    public static DynamicReport eventBuild(HashMap<String, Object> reportData)
    {
        DynamicReport dynamicReport = null;

        try
        {
            if (reportData != null)
            {
                String title = (String) reportData.get("title");

                boolean isPageBreak = false;

                if (reportData.containsKey("isPageBreak"))
                {
                    isPageBreak = (boolean) reportData.get("isPageBreak");
                }

                List<TraceOrgReportColumn> columns = (List<TraceOrgReportColumn>) reportData.get("columns");

                if (columns != null && columns.size() > 0)
                {
                    FastReportBuilder fastReportBuilder = new FastReportBuilder();

                    fastReportBuilder.setTitleStyle(getSubReportTitleStyle());

                    fastReportBuilder.setSubtitleStyle(getSubReportSubTitleStyle());

                    fastReportBuilder.setMargins(MARGIN_TOP, MARGIN_BOTTOM, MARGIN_LEFT, MARGIN_RIGHT);

                    if (title != null && title.length() > 0)
                    {
                        fastReportBuilder.setTitle("Name : " + title);

                       /* if(reportData.get(REPORT_TIMELINE) != null)
                        {
                            fastReportBuilder.setSubtitle("Time Span: " + reportData.get(REPORT_TIMELINE));
                        }*/
                    }

                    setDefaultStyle(fastReportBuilder);

                    for (TraceOrgReportColumn column : columns)
                    {
                        AbstractColumn abstractColumn = ColumnBuilder.getNew().setColumnProperty(column.getProperty(), Object.class.getName()).setTitle(column.getPropertyLabel()).setStyle(getRowStyle()).build();
                        if(columns.indexOf(column)==1)
                        {
                            fastReportBuilder.addColumn(column.getPropertyLabel(),column.getProperty(),Object.class,200);
                        }
                        else
                        {
                            fastReportBuilder.addColumn(abstractColumn);
                        }
                    }

                    fastReportBuilder.setUseFullPageWidth(Boolean.TRUE).setIgnorePagination(isPageBreak);

                    dynamicReport = fastReportBuilder.build();
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return dynamicReport;
    }



    private static void setDefaultStyle(FastReportBuilder frb)
    {
        frb.setDefaultStyles(null, null, getGridHeaderStyle(), getColumnStyle());
    }

    static Style getRowStyle()
    {
        Style rowStyle = new Style();
        rowStyle.setFont(getSmallerFont());
        rowStyle.setBorder(Border.THIN());
        rowStyle.getBorder().setColor(Color.GRAY);
        rowStyle.setTransparency(ar.com.fdvs.dj.domain.constants.Transparency.OPAQUE);

        return rowStyle;
    }


    static Style getSubReportTitleStyle()
    {
        Style titleStyle = new Style();
        titleStyle.setFont(new Font(MEDIUM, Font._FONT_ARIAL, true, false, false));
        titleStyle.setHorizontalAlign(HorizontalAlign.LEFT);
        titleStyle.setVerticalAlign(VerticalAlign.MIDDLE);
        titleStyle.setTextColor(Color.BLACK);
        titleStyle.setTransparency(ar.com.fdvs.dj.domain.constants.Transparency.OPAQUE);

        return titleStyle;
    }

    static Style getSubReportSubTitleStyle()
    {
        Style subTitleStyle = new Style();
        subTitleStyle.setFont(new Font(SMALL, Font._FONT_ARIAL, true, false, false));
        subTitleStyle.setHorizontalAlign(HorizontalAlign.LEFT);
        subTitleStyle.setVerticalAlign(VerticalAlign.MIDDLE);
        subTitleStyle.setTextColor(Color.BLACK);
        subTitleStyle.setTransparency(ar.com.fdvs.dj.domain.constants.Transparency.OPAQUE);

        return subTitleStyle;
    }

    static Style getColumnStyle()
    {
        Style columnStyle = new Style();
        columnStyle.setFont(getSmallerFont());
        columnStyle.setBorder(Border.THIN());
        columnStyle.setHorizontalAlign(HorizontalAlign.LEFT);
        columnStyle.setVerticalAlign(VerticalAlign.MIDDLE);
        columnStyle.getBorder().setColor(Color.GRAY);
        columnStyle.setTransparency(ar.com.fdvs.dj.domain.constants.Transparency.OPAQUE);

        return columnStyle;
    }

    static Style getGridHeaderStyle()
    {
        Style headerStyle = new Style();
        headerStyle.setFont(new Font(SMALLER, Font._FONT_ARIAL, true, false, false));
        headerStyle.setBorder(Border.THIN());
        headerStyle.getBorder().setColor(Color.GRAY);
        headerStyle.setHorizontalAlign(HorizontalAlign.LEFT);
        headerStyle.setVerticalAlign(VerticalAlign.MIDDLE);
        headerStyle.setBackgroundColor(Color.LIGHT_GRAY);
        headerStyle.setTextColor(Color.BLACK);
        headerStyle.setTransparency(ar.com.fdvs.dj.domain.constants.Transparency.OPAQUE);

        return headerStyle;
    }


    /**
     * IPAM-145 : System should have rogue device detection capability
     * Set the title of pie chart in report.
     * @param reportData
     * @param mainTitle
     * @return
     */
    public DynamicReport pieChartBuild(HashMap<String, Object> reportData, String mainTitle)
    {
        DynamicReport dynamicReport = null;

        try
        {
            if (reportData != null)
            {
                String title = "Subnet Summary";

                if(mainTitle != null)
                {
                    title = mainTitle;
                }

                TraceOrgReportColumn rcXAxis = (TraceOrgReportColumn) reportData.get("x-axis");

                List<TraceOrgReportColumn> series = (List<TraceOrgReportColumn>) reportData.get("series");

                if (rcXAxis != null && series != null && series.size() == 1)
                {
                    DynamicReportBuilder dynamicReportBuilder = new DynamicReportBuilder();

                    dynamicReportBuilder.setTitleStyle(getSubReportTitleStyle());

                    dynamicReportBuilder.setSubtitleStyle(getSubReportSubTitleStyle());

                    dynamicReportBuilder.setShowDetailBand(Boolean.FALSE);

                    dynamicReportBuilder.setPrintColumnNames(Boolean.FALSE);

                    dynamicReportBuilder.setMargins(MARGIN_TOP, MARGIN_BOTTOM, MARGIN_LEFT, MARGIN_RIGHT);

                    if (title != null && title.trim().length() > 0)
                    {
                        dynamicReportBuilder.setTitle("Name : " + title);
                    }

                    AbstractColumn x_axis = ColumnBuilder.getNew().setColumnProperty(rcXAxis.getProperty(), Object.class.getName()).setTitle(rcXAxis.getPropertyLabel()).build();

                    AbstractColumn y_axis = ColumnBuilder.getNew().setColumnProperty(series.get(0).getProperty(), Object.class.getName()).setTitle(series.get(0).getPropertyLabel()).build();

                    dynamicReportBuilder.addColumn(x_axis);

                    dynamicReportBuilder.addColumn(y_axis);

                    DJPie3DChartBuilder chartBuilder = new DJPie3DChartBuilder().setX(0).setY(0).setShowLegend(Boolean.FALSE).setWidth(500).setHeight(300).setKey((PropertyColumn) x_axis).addSerie(y_axis).setDepthFactor(0.1).setCircular(Boolean.TRUE);

                    if(reportData.containsKey("series-colors") && reportData.get("series-colors") != null)
                    {
                        List<Color> availabilityColors = (List<Color>) reportData.get("series-colors");

                        for(Color color : availabilityColors)
                        {
                            chartBuilder.addSeriesColor(new Color(color.getRGB()));
                        }
                    }
                    else
                    {
                        for(int color : DEFAULT_SERIES_COLORS)
                        {
                            chartBuilder.addSeriesColor(new Color(color));
                        }
                    }


                    DJChart djChart = chartBuilder.build();

                    dynamicReportBuilder.setUseFullPageWidth(Boolean.TRUE);

                    dynamicReportBuilder.addChart(djChart);

                    dynamicReport = dynamicReportBuilder.build();
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        _logger.debug("end building 3D pie chart into report...!");

        return dynamicReport;
    }

    public static HashMap<String, Color> getAvailabilityStatusDescriptionColors()
    {
        HashMap<String, Color> availabilityColors = new HashMap<>();

        availabilityColors.put(TraceOrgCommonConstants.AVAILABILITY_USED_PERCENTAGE, new Color(255, 187, 68, 1));

        availabilityColors.put(TraceOrgCommonConstants.AVAILABILITY_AVAILABLE_PERCENTAGE, new Color(53, 110, 53, 1));

        availabilityColors.put(TraceOrgCommonConstants.AVAILABILITY_TRANSIENT_PERCENTAGE, new Color(50, 118, 177, 1));

        return availabilityColors;
    }

    private static Font getSmallerFont()
    {
        return new Font(SMALLER, Font._FONT_ARIAL, false, false, false);
    }
}

