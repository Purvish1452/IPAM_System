package com.motadata.traceorg.ipam.util;

/**
 * Created by hardik on 17/6/18.
 */

public interface TraceOrgAbstractCommonUtil
{
    boolean init() throws Exception;

    void destroy() throws Exception;

    boolean reInit() throws Exception;
}
