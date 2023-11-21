package edu.jit.nsi.iot_ms.Fegin.SMS;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

//@Service
//@Component
@FeignClient(value = "SMS")
public interface SMSFeignClient {
    @GetMapping("/Send/warnMessage")
    Object warnMessage(@RequestParam("level") int level,@RequestParam("cellName") String cellName, @RequestParam("param")String param,
                       @RequestParam("value")float value , @RequestParam("hour") String hour, @RequestParam("phone")String phone);
}
