package edu.jit.nsi.iot_ms;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@SpringBootApplication
@Slf4j
@MapperScan("edu.jit.nsi.iot_ms.mapper")
@EnableScheduling
@EnableEurekaClient
@EnableFeignClients(basePackages = "edu.jit.nsi.iot_ms.Fegin")
@EnableCaching
public class IoTApplication {

	public static void main(String[] args) {
		SpringApplication.run(IoTApplication.class, args);
		log.info("iot-ms application is running.........");

	}

}
