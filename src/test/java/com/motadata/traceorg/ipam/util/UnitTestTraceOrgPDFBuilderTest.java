package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.controller.TestCasesApplicationTests;
import org.junit.Assert;
import org.junit.Test;

public class UnitTestTraceOrgPDFBuilderTest extends TestCasesApplicationTests
{
    @Test
    public void whenImagesFolderNotFoundThenCreateFolderAndSetLogoImage()
    {
        boolean imageWriteStatus = TraceOrgPDFBuilder.setLogoImage();

        Assert.assertEquals(Boolean.TRUE, imageWriteStatus);
    }
}
