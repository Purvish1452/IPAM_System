package com.motadata.traceorg.ipam.services.impl.settings;

import com.motadata.traceorg.ipam.controller.settings.TraceOrgGlobalSettingController;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgBrand;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgBrandRepository;
import com.motadata.traceorg.ipam.services.settings.TraceOrgBrandService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Service
public class TraceOrgBrandServiceImpl implements TraceOrgBrandService
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgGlobalSettingController.class, "Brand Service");

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgBrandRepository traceOrgBrandRepository;

    @Override
    public HashMap<String, Object> updateBrand(Long id, MultipartFile brandLogo, String productName, HttpServletRequest request)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id != null && productName != null && !productName.trim().isEmpty() && brandLogo != null)
            {
                if(brandLogo.getOriginalFilename().toLowerCase().endsWith("jpg") || brandLogo.getOriginalFilename().toLowerCase().endsWith("jpeg") || brandLogo.getOriginalFilename().toLowerCase().endsWith("png"))
                {
                    traceOrgCommonUtil.fileUpload(brandLogo, request);

                    TraceOrgBrand traceOrgBrand = traceOrgBrandRepository.findOne(1L);

                    traceOrgBrand.setProductName(productName);

                    traceOrgBrand.setProductImg(TraceOrgCommonConstants.LOGO_PNG);

                    traceOrgBrandRepository.save(traceOrgBrand);

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.BRAND_UPDATE_SUCCESS);
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

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }
}
