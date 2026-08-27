package com.motadata.traceorg.ipam.scheduler.database;

import com.google.common.base.Strings;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgDatabaseMaintenance;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class TraceOrgDatabaseMaintenanceSchedule
{
    @Autowired
    private TraceOrgService traceOrgService;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgDatabaseMaintenanceSchedule.class, "GUI / Database Maintenance Scheduler");

    /**
     * IPAM-142 : IPAM | Alert notification for Monitor IP capacity and receive alerts on IP depletion
     * Delete alert_stream data during the data retention job.
     * */
    @Scheduled(cron = "0 01 00 * * ?")
    public void scanSubnet() throws Exception
    {
        TraceOrgDatabaseMaintenance traceOrgDatabaseMaintenance =  (TraceOrgDatabaseMaintenance)this.traceOrgService.getById("TraceOrgDatabaseMaintenance",1L);

        _logger.info("database maintanance job executed...");

        try
        {
            if(traceOrgDatabaseMaintenance != null && traceOrgDatabaseMaintenance.getMaintainedDays() > 0 && (Strings.isNullOrEmpty(traceOrgDatabaseMaintenance.getStatus()) || traceOrgDatabaseMaintenance.getStatus().equals(TraceOrgCommonConstants.ENABLE)))
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                Date dataFrom = new Date();

                String currentDate = dateFormat.format(dataFrom);

                this.traceOrgService.sqlQueryAction("SET SQL_SAFE_UPDATES = 0");

                this.traceOrgService.sqlQueryAction("Delete from event where DATEDIFF('"+currentDate+"',timestamp) > "+ traceOrgDatabaseMaintenance.getMaintainedDays()+"");

                this.traceOrgService.sqlQueryAction("Delete from ip_change_log where DATEDIFF('"+currentDate+"',timestamp) > "+ traceOrgDatabaseMaintenance.getMaintainedDays()+"");

                this.traceOrgService.sqlQueryAction("Delete from alert_stream where DATEDIFF('"+currentDate+"',timestamp) > "+ traceOrgDatabaseMaintenance.getMaintainedDays()+"");

                this.traceOrgService.sqlQueryAction("SET SQL_SAFE_UPDATES = 1");
            }

            List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetails =(List<TraceOrgSubnetIpDetails>)this.traceOrgService.commonQuery("TraceOrgSubnetIpDetails where status='Transient'  and  DATEDIFF(CURDATE(),lastAliveTime) >= 7");

            if(traceOrgSubnetIpDetails!=null && !traceOrgSubnetIpDetails.isEmpty())
            {
                for(TraceOrgSubnetIpDetails traceOrgSubnetIpDetail:traceOrgSubnetIpDetails)
                {
                    traceOrgSubnetIpDetail.setStatus(TraceOrgCommonConstants.AVAILABLE);

                    traceOrgSubnetIpDetail.setPreviousStatus(TraceOrgCommonConstants.AVAILABLE);

                    traceOrgSubnetIpDetail.setModifiedDate(new Date());

                    this.traceOrgService.insert(traceOrgSubnetIpDetail);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }
}
