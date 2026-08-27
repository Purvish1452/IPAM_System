package com.motadata.traceorg.ipam.services.messaging;

import com.motadata.traceorg.ipam.services.alert.TraceOrgAlertService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class TraceOrgMessageListener
{
    @Autowired
    TraceOrgAlertService traceOrgAlertService;

    @JmsListener(destination = TraceOrgCommonConstants.ALERT_QUEUE, containerFactory = "jmsListenerContainerFactory")
    public void receiveMessage(HashMap<String, Object> message)
    {
        traceOrgAlertService.inspectAlert(message);
    }
}