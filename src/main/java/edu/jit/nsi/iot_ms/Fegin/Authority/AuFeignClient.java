package edu.jit.nsi.iot_ms.Fegin.Authority;

import edu.jit.nsi.iot_ms.domain.authResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

//@Service
//@Component
@FeignClient(value = "auth-service",url = "https://www.njzhny.com:8443")
public interface AuFeignClient {
    @GetMapping(value = "/userInfoByName")
    authResponse userInfoByName(@RequestParam("username")String username , @RequestHeader("Authorization") String token);
    @PostMapping(value = "/auth/login", consumes = "application/x-www-form-urlencoded")
    authResponse logIn(@RequestParam("username") String username, @RequestParam("password") String password);
}
