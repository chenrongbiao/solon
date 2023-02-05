package org.noear.solon.cloud.extend.activemq.impl;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.noear.solon.cloud.service.CloudEventObserverManger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivemqConsumer {
	static Logger log = LoggerFactory.getLogger(ActivemqConsumer.class);
	private ActiveMQConnectionFactory factory;
	private ActivemqProducer producer;
	private Connection connection;
	private ActivemqConsumeHandler handler;
	
	public ActivemqConsumer(ActiveMQConnectionFactory factory,ActivemqProducer producer){
		this.factory = factory;
		this.producer= producer;
	}
	
	public void init(CloudEventObserverManger observerManger) throws JMSException{
		connection = factory.createConnection();
		connection.start();		
		//创建会话
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        handler = new ActivemqConsumeHandler(observerManger,producer);
        
        //订阅所有主题
        for (String topic : observerManger.topicAll()) {
	        //创建一个目标
	        Destination destination = session.createTopic(topic);
	        //创建一个消费者
	        MessageConsumer consumer = session.createConsumer(destination);
	        
	        consumer.setMessageListener(handler);
        }
	}
	
	public void close() throws JMSException{
		if(connection != null){
			connection.close();
		}
	}
}
