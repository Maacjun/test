package edu.jit.nsi.iot_ms.transport.lora;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @Author: chihaojie
 * @Date: 2019/5/30 17:57
 * @Version 1.0
 * @Note
 */

@Component
public class MqttMessageSender {
    @Autowired
    private MqttGateway mqttGateway;

    @Value("${mqtt.producer.defaultTopic}")
    private String topicTmpl;

    public void send(String deviceId, String appid, String msgBody){
        String topic = topicTmpl.replace("$",appid).replace("?", deviceId);
        //mqttOutFlow.getInputChannel().send(new GenericMessage<>(JSONObject.toJSONString(mqttMessage)));
        mqttGateway.sendToMqtt(topic,0,msgBody);
    }
}
