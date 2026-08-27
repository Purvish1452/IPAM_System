package com.motadata.traceorg.ipam.util;

import ar.com.fdvs.dj.domain.DynamicReport;

import java.util.HashMap;

/**
 * Created by Administrator on 9/28/2016.
 */
public interface TraceOrgAbstractReportBuilder
{
    int PIE_CHART_TICK_LIMIT = 15;

    String SERIES_COLORS = "series-colors";

    DynamicReport build(HashMap<String, Object> data);
}
