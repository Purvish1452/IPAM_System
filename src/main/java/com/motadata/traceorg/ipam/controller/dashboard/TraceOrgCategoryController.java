package com.motadata.traceorg.ipam.controller.dashboard;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.*;
import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgCategory;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.supernet.TraceOrgSupernetService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@SuppressWarnings({"unchecked","SpringAutowiredFieldsWarningInspection"})
@RestController
public class TraceOrgCategoryController {

    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgSupernetService traceOrgSupernetService;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgCategoryController.class, "Category Controller");


    @RequestMapping(value = TraceOrgCommonConstants.CATEGORY_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listCategory(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            List<TraceOrgCategory> traceOrgCategories = (List<TraceOrgCategory>) this.traceOrgService.commonQuery("", TraceOrgCommonConstants.TRACE_ORG_CATEGORY);

            if (traceOrgCategories != null && !traceOrgCategories.isEmpty())
            {
                response.setData(traceOrgCategories);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
            }
            else
            {
                response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);

                response.setSuccess(TraceOrgCommonConstants.FALSE);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @RequestMapping(value = TraceOrgCommonConstants.CATEGORY_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> insertCategory(HttpServletRequest request, @RequestBody TraceOrgCategory traceOrgCategory)
    {
        Response response = new Response();

        if (traceOrgCategory.getCategoryName() != null && !traceOrgCategory.getCategoryName().trim().isEmpty())
        {
            try
            {

                if (this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_CATEGORY, TraceOrgCommonConstants.CATEGORY_NAME, traceOrgCategory.getCategoryName()))
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.CATEGORY_NAME_ALREADY_EXIST);
                }
                else
                {
                    boolean insertStatus = this.traceOrgService.insert(traceOrgCategory);

                    if (insertStatus)
                    {
                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                        response.setMessage(TraceOrgMessageConstants.CATEGORY_ADD_SUCCESS);
                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
                    }
                }

            }
            catch (Exception exception)
            {
                _logger.error(exception);

                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        else
        {
            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.CATEGORY_REST_URL+"{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> updateCategory(HttpServletRequest request,@PathVariable Long id ,@RequestBody TraceOrgCategory traceOrgCategory)
    {
        Response response = new Response();

        if (id != null && traceOrgCategory.getCategoryName() != null && !traceOrgCategory.getCategoryName().trim().isEmpty())
        {
            try
            {
                TraceOrgCategory existedCategory = (TraceOrgCategory) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_CATEGORY, id);

                if (existedCategory != null)
                {
                    List<TraceOrgCategory> categoryList = (List<TraceOrgCategory>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_CATEGORY + " where categoryName = '" + traceOrgCategory.getCategoryName() + "'");

                    if (categoryList != null && !categoryList.isEmpty() && categoryList.size() < 2)
                    {
                        if (categoryList.get(0).getId().equals(existedCategory.getId()))
                        {
                            existedCategory.setCategoryName(traceOrgCategory.getCategoryName());

                            boolean insertStatus = this.traceOrgService.insert(existedCategory);

                            if (insertStatus)
                            {
                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                response.setMessage(TraceOrgMessageConstants.CATEGORY_UPDATE_SUCCESS);
                            }
                            else
                            {
                                response.setSuccess(TraceOrgCommonConstants.FALSE);

                                response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
                            }
                        }
                        else
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.CATEGORY_NAME_ALREADY_EXIST);
                        }
                    }
                    else
                    {
                        existedCategory.setCategoryName(traceOrgCategory.getCategoryName());

                        boolean insertStatus = this.traceOrgService.insert(existedCategory);

                        if (insertStatus)
                        {
                            response.setSuccess(TraceOrgCommonConstants.TRUE);

                            response.setMessage(TraceOrgMessageConstants.CATEGORY_UPDATE_SUCCESS);
                        }
                        else
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
                        }
                    }
                }
                else
                {

                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                }

            }
            catch (Exception exception)
            {
                _logger.error(exception);

                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        else
        {
            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMail method
     *
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * Added the removeSubnetFromSupernetDetails method call
     * **/
    @RequestMapping(value = TraceOrgCommonConstants.CATEGORY_REST_URL+"{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> removeCategory(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        if(id !=null)
        {
            if(id==1)
            {
                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage("Default Category can not be removed");
            }
            else
            {
                try
                {
                    String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                    if(TraceOrgCommonUtil.getCSVImportCount() > 0)
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.IMPORT_RUNNING);
                    }
                    else if(TraceOrgCommonUtil.getSubnetScanStatus() != 0)
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.CANT_DELETE_CATEGORY_UNDER_SCAN);
                    }
                    else
                    {
                        TraceOrgCategory traceOrgCategory = (TraceOrgCategory) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_CATEGORY,id);

                        if (traceOrgCategory != null)
                        {
                            List<TraceOrgSubnetDetails> traceOrgSubnetDetailsList = (List<TraceOrgSubnetDetails>)this.traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS+" where traceOrgCategory = '"+id+"'");

                            if(traceOrgSubnetDetailsList !=null && !traceOrgSubnetDetailsList.isEmpty())
                            {
                                String subnetAddress = null;

                                for(TraceOrgSubnetDetails traceOrgSubnetDetails : traceOrgSubnetDetailsList)
                                {

                                    if(subnetAddress !=null)
                                    {
                                        subnetAddress = subnetAddress + ", " +traceOrgSubnetDetails.getSubnetAddress();
                                    }
                                    else
                                    {
                                        subnetAddress = traceOrgSubnetDetails.getSubnetAddress();
                                    }


                                    this.traceOrgService.delete(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS,TraceOrgCommonConstants.SUBNET_ID,traceOrgSubnetDetails.getId().toString());

                                    //Remove Cron Subnet Scan
                                    traceOrgCommonUtil.removeScanSubnetCron(traceOrgSubnetDetails);

                                    traceOrgSupernetService.removeSubnetFromSupernetDetails(traceOrgSubnetDetails.getId());

                                    //EVENT LOG
                                    TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

                                    traceOrgEvent.setTimestamp(new Date());

                                    traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                                    traceOrgEvent.setEventType("Delete Subnet");

                                    traceOrgEvent.setEventContext("Subnet "+traceOrgSubnetDetails.getSubnetAddress()+" is deleted from IP Address Manager by "+traceOrgCommonUtil.currentUserName(accessToken)  );

                                    traceOrgEvent.setSeverity(1);

                                    this.traceOrgService.insert(traceOrgEvent);
                                }

                                this.traceOrgService.delete(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"traceOrgCategory",TraceOrgCommonUtil.getStringValue(id));

                                this.traceOrgService.delete(TraceOrgCommonConstants.TRACE_ORG_CATEGORY,TraceOrgCommonConstants.ID,TraceOrgCommonUtil.getStringValue(id));

                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                response.setMessage(TraceOrgMessageConstants.CATEGORY_DELETE_SUCCESS);


                                if(subnetAddress!=null)
                                {
                                    String mailBody;

                                    if(subnetAddress.contains(",") && subnetAddress.split(",").length > 0 )
                                    {
                                        String[] subnets = subnetAddress.split(",");

                                        mailBody  = "Subnet Deleted in IP Address Manager By " + traceOrgCommonUtil.currentUserName(accessToken) + ".<br><br> <table style =\"border: 1px solid\" > <tr> <th style =\"border: 1px solid\">Subnet Address</th> </tr>";

                                        for(String subnet :subnets)
                                        {
                                            mailBody = mailBody + "<tr> <td style =\"border: 1px solid\">" + subnet + "</td> </tr>";
                                        }

                                        mailBody = mailBody + "</table>";

                                    }
                                    else
                                    {
                                        mailBody = "Subnet " + subnetAddress + " Deleted  in IP Address Manager By " + traceOrgCommonUtil.currentUserName(accessToken) + ".";
                                    }

                                    traceOrgCommonUtil.sendMail("Subnet Deleted In IP Address Manager", mailBody);
                                }
                            }
                            else
                            {
                                this.traceOrgService.delete(TraceOrgCommonConstants.TRACE_ORG_CATEGORY,TraceOrgCommonConstants.ID,TraceOrgCommonUtil.getStringValue(id));

                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                response.setMessage(TraceOrgMessageConstants.CATEGORY_DELETE_SUCCESS);
                            }
                        }
                        else
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.CATEGORY_ID_NOT_VALID);
                        }
                    }

                }
                catch (Exception exception)
                {
                    _logger.error(exception);

                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                }
            }
        }
        else
        {
            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
