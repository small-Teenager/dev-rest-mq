package com.dev.rest.mq;

import com.dev.rest.mq.config.RabbitMQConsumerConfig;
import com.dev.rest.mq.dto.MessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RabbitmqMessageConsumer {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqMessageConsumer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 监听消息队列，队列名称QUEUE_NAME
     * 通过brokerContainerFactory获取到对应的queue
     */
    @RabbitListener(queues = "QUEUE_NAME", containerFactory = "brokerContainerFactory")
    public void onMessage(Message message, Channel channel) throws Exception {
        log.info("Consumed message: {},channel:{}", message, channel);
        try {
            MessageDTO dto = parse(new String(message.getBody()), MessageDTO.class);
            log.info("Consumed message content: {}", dto);
            // 由于之前配置的手动ack，需要手动回调rabbitMQ服务器，通知已经完成消费
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("Consumed error:{}", e);
            // 由于之前配置的手动ack，需要手动回调rabbitMQ服务器，通知出现问题
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    public static <T> T parse(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("IOException, json = {}, clazz = {}", json, clazz, e);
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e1) {
                log.error("InstantiationException or IllegalAccessException, clazz = {}", clazz, e1);
                return null;
            }
        }
    }

    /**
     * delay queue consumer
     *
     * @param message
     * @param channel
     */
    @RabbitListener(queues = RabbitMQConsumerConfig.DLX_QUEUE_NAME)
    public void delayQueueMessage(Message message, Channel channel) {
        log.info("delay queue consumer time is:{}", LocalDateTime.now());
        log.info("delay queue consumed message: {},channel:{}", message, channel);
    }

    /*------- fanout publish-subscribe ---------*/
    @RabbitListener(queues = RabbitMQConsumerConfig.PUB_SUB_EMAIL_QUEUE)
    public void pubSubEmailMessage(Message message, Channel channel) {
        log.info("publish-subscribe email consumer time is:{}", LocalDateTime.now());
        log.info("publish-subscribe email consumed message: {},channel:{}", message, channel);
    }

    @RabbitListener(queues = RabbitMQConsumerConfig.PUB_SUB_SMS_QUEUE)
    public void pubSubSmsMessage(Message message, Channel channel) {
        log.info("publish-subscribe sms consumer time is:{}", LocalDateTime.now());
        log.info("publish-subscribe sms consumed message: {},channel:{}", message, channel);
    }

    /**
     * priority queue + concurrent consumers
     *
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = RabbitMQConsumerConfig.PRIORITY_QUEUE, containerFactory = "customContainerFactory")
    public void priorityMessage(Message message, Channel channel) throws IOException {
        log.info("priority queue consumed message: {},channel:{}", message, channel);
        log.info("thread id:{},thread name:{}", Thread.currentThread().getId(), Thread.currentThread().getName());
    }
}