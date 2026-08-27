package com.motadata.traceorg.ipam.util;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class TraceOrgFactoryUtil
{
    public BufferedReader getBufferedReader(InputStream inputStream)
    {
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    public Runtime getRuntime()
    {
        return Runtime.getRuntime();
    }
}
