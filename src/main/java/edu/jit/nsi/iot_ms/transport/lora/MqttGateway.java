package edu.jit.nsi.iot_ms.transport.lora;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @Author: chihaojie
 * @Date: 2020/4/8 19:07
 * @Version 1.0
 * @Note
 */
@Component
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {

    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, String payload);
    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos, String payload);
}