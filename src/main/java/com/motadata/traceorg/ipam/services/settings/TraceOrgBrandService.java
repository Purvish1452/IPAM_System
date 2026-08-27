package com.motadata.traceorg.ipam.services.settings;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

public interface TraceOrgBrandService
{
    HashMap<String, Object> updateBrand(Long id, MultipartFile brandLogo, String productName, HttpServletRequest request);

}
