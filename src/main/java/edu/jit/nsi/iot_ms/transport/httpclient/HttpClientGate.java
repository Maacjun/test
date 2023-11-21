package edu.jit.nsi.iot_ms.transport.httpclient;

import edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023.AirSoil6_1;
import edu.jit.nsi.iot_ms.serviceimpl.JSIotReport.jsplat2023.JSPlat2023Session;
import edu.jit.nsi.iot_ms.transport.httpclient.jsplat.JSPlatJNSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

@Slf4j
@Service
public class HttpClientGate {
    private static String JSPlatURL = "http://210.12.220.220:8012/dataswitch-sq/sendApi/sendSingleInfo";
    private static String JNPlatURL = "http://124.70.193.186:9100/api/1.0/public/iotdata/receive";
//    private RestTemplate restTemplate = getInstance("utf-8");
//
//    public static RestTemplate getInstance(String charset) {
//        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName(charset)));
//        return restTemplate;
//    }
    private RestTemplate rest;

    public HttpClientGate(){
        rest=restTemplate(simpleClientHttpRequestFactory());
    }

    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }


    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(5000);//单位为ms
        factory.setConnectTimeout(5000);//单位为ms
        return factory;
    }


    public boolean postMsg2JSPlat(JSPlat2023Session jsRptMsg) {
        String rpttRes="";
        try {
            rpttRes = rest.postForObject(JSPlatURL, jsRptMsg, String.class);
        }catch (Exception e){
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(output));
            log.error("Post to JSIOTPlat Error: {}", output.toString());
            return false;
        }
        log.info("JSIOTPlat Response: {}", rpttRes);

        if(rpttRes.contains("200"))
            return true;
        else
            return false;
    }

    public boolean postMsg2JSPlat(JSPlatJNSession jsRptMsg) {
        String rpttRes="";
        try {
            rpttRes = rest.postForObject(JSPlatURL, jsRptMsg, String.class);
        }catch (Exception e){
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(output));
            log.error("Post to JSIOTPlat Error: {}", output.toString());
            return false;
        }
        log.info("JSIOTPlat Response: {}", rpttRes);

        if(rpttRes.contains("200"))
            return true;
        else
            return false;
    }

    public boolean postMsg2JNPlat(JSPlatJNSession jsRptMsg) {
        String rpttRes="";
        try {
            rpttRes = rest.postForObject(JNPlatURL, jsRptMsg, String.class);
        }catch (Exception e){
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(output));
            log.error("Post to JingNingIotPlat Error: {}", output.toString());
            return false;
        }
        log.info("JingNingIotPlat Response: {}", rpttRes);

        if(rpttRes.contains("200"))
            return true;
        else
            return false;
    }

}
