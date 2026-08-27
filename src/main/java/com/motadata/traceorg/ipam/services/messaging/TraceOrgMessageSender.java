package com.motadata.traceorg.ipam.services.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class TraceOrgMessageSender
{
    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendMessage(String queueName, HashMap<String, Object> message)
    {
        jmsTemplate.convertAndSend(queueName, message);
    }
}