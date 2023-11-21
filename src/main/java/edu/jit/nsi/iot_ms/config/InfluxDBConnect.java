package edu.jit.nsi.iot_ms.config;

import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @packageName: com.example.iot_server.utils
 * @className: InfluxDBConnect
 * @Description:
 * @author: xxz
 * @date: 2019/7/22 10:46
 */
@Slf4j
@Configuration
public class InfluxDBConnect {
    @Value("${influxdb.username}")
    private String username;//用户名
    @Value("${influxdb.password}")
    private String password;//密码
    @Value("${influxdb.url}")
    private String openurl;//连接地址
    @Value("${influxdb.database}")
    private String database;//数据库
    @Value("${influxdb.retention-policy}")
    private String retentionPolicy;  // 保留策略



    // 数据保存策略
    public static String policyNamePix = "logRetentionPolicy_";

    @Primary
    @Bean
    public InfluxDB influxdb() {
        InfluxDB influxDB = InfluxDBFactory.connect(openurl, username, password);
        influxDB.setRetentionPolicy(retentionPolicy);
        influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
        try{
            Pong pong = influxDB.ping();
        }catch (Exception e) {
            e.printStackTrace();
            log.error("connect Influx DB fail!");
        }

        log.info("connect Influx DB succeed!");
        return influxDB;
    }

}
