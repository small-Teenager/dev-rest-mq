package com.dev.rest.mq.config;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConsumerConfig {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQConsumerConfig.class);
    /**
     * 注入RabbitMQClinet的brokerContainerFactory
     * 如果RabbitMQ服务器没有对应的Queue，exchange，routing key，则初始化新建Queue，exchange，routing key
     * @param configurer
     * @param connectionFactory
     * @return
     */
    @Bean(name = "brokerContainerFactory")
    public SimpleRabbitListenerContainerFactory brokerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                       ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        // 手动ACK
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        // 最小的消费者数
        factory.setConcurrentConsumers(3);
        // 消费者最大数
        factory.setMaxConcurrentConsumers(5);
        // 预拉取条数
        factory.setPrefetchCount(5);
        // 初始化并新建
        try (Connection connection = connectionFactory.createConnection();
             Channel channel = connection.createChannel(false)) {
            channel.queueDeclare("QUEUE_NAME", true, false, false, null);
            channel.exchangeDeclare("EXCHANGE_NAME", BuiltinExchangeType.DIRECT);
            channel.queueBind("QUEUE_NAME", "EXCHANGE_NAME", "ROUTING_KEY");
        } catch (Exception e) {
            log.info("Declare and bind queue error!", e);
        }
        return factory;
    }

    /*------- fanout publish-subscribe ---------*/
    public static final String PUB_SUB_SMS_QUEUE = "sms-fanout-queue";
    public static final String PUB_SUB_EMAIL_QUEUE = "email-fanout-queue";

    /*------- priority queue ---------*/
    public static final String PRIORITY_QUEUE = "priority.queue";

    /*-------dlx queue---------*/
    public static final String DLX_QUEUE_NAME = "dlx_queue_name";
}