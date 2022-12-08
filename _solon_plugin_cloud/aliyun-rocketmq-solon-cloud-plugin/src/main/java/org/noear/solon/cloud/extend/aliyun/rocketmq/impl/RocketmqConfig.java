package org.noear.solon.cloud.extend.aliyun.rocketmq.impl;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.PropertyValueConst;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.cloud.CloudProps;
import org.noear.solon.cloud.extend.aliyun.rocketmq.RocketmqProps;

import java.util.Properties;

/**
 * @author cgy
 * @since 1.11.3
 */
public class RocketmqConfig {
    /**
     * 生产组
     */
    private String producerGroup;

    /**
     * 消费组
     */
    private String consumerGroup;

    private String server;

    private long timeout;

    private String accessKey;

    private String secretKey;

    private String messageModel;

    public RocketmqConfig(CloudProps cloudProps) {
        server = cloudProps.getEventServer();
        timeout = cloudProps.getEventPublishTimeout();
        producerGroup = cloudProps.getValue(RocketmqProps.PROP_EVENT_producerGroup);
        consumerGroup = cloudProps.getValue(RocketmqProps.PROP_EVENT_consumerGroup);
        accessKey = cloudProps.getValue(RocketmqProps.PROP_EVENT_accessKey);
        secretKey = cloudProps.getValue(RocketmqProps.PROP_EVENT_secretKey);
        messageModel = cloudProps.getValue(RocketmqProps.PROP_EVENT_MessageModel, PropertyValueConst.CLUSTERING);
        if (Utils.isEmpty(producerGroup)) {
            producerGroup = "DEFAULT";
        }
        if (Utils.isEmpty(consumerGroup)) {
            consumerGroup = Solon.cfg().appGroup() + "_" + Solon.cfg().appName();
        }
    }

    public Properties getProducerProperties() {
        Properties producer = getProperties();
        producer.put(PropertyKeyConst.GROUP_ID, producerGroup);
        producer.put(PropertyKeyConst.SendMsgTimeoutMillis, timeout);
        return producer;
    }

    public Properties getConsumerProperties() {
        Properties consumer = getProperties();
        consumer.put(PropertyKeyConst.ConsumeTimeout, timeout);
        consumer.put(PropertyKeyConst.GROUP_ID, consumerGroup);
        consumer.put(PropertyKeyConst.MessageModel, messageModel);
        return consumer;
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.AccessKey, accessKey);
        properties.put(PropertyKeyConst.SecretKey, secretKey);
        properties.put(PropertyKeyConst.NAMESRV_ADDR, server);
        return properties;
    }
}
