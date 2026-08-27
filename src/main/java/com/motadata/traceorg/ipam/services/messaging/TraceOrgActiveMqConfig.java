package com.motadata.traceorg.ipam.services.messaging;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;

@Configuration
@EnableJms
public class TraceOrgActiveMqConfig
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgActiveMqConfig.class, "ActiveMq Config");

    /**
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Taking a connection with the persist flag set to false in ActiveMQ.
     * */
    @Bean
    public ConnectionFactory connectionFactory()
    {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false&jms.useAsyncSend=true");

        activeMQConnectionFactory.setMaxThreadPoolSize(TraceOrgCommonConstants.ACTIVEMQ_CONNECTION_POOL_SIZE);

        return activeMQConnectionFactory;
    }

    /**
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Starting the listener with the default concurrency of 3.
     * */
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory()
    {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory());

        factory.setConcurrency(TraceOrgCommonUtil.getStringValue(TraceOrgCommonConstants.MAX_ALERT_WORKER));

        factory.setErrorHandler(error -> _logger.error(new Exception(error)));

        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate()
    {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());

        jmsTemplate.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        return jmsTemplate;
    }
}