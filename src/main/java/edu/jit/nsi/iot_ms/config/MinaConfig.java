package edu.jit.nsi.iot_ms.config;

import edu.jit.nsi.iot_ms.transport.tcp.EP400Handler;
import edu.jit.nsi.iot_ms.transport.tcp.JitEmbedHandler;
import edu.jit.nsi.iot_ms.transport.udp.EComGWHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.ExpiringSessionRecycler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.prefixedstring.PrefixedStringCodecFactory;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;


/**
 * @className: MinaConfig
 * @author: kay
 * @date: 2019/7/16 21:25
 * @packageName: com.jit.iot.jit.edu.nsi.config
 */
@Slf4j
@Configuration
public class MinaConfig {
    @Autowired
    JitEmbedHandler jitEmbedHandler;

    @Autowired
    EP400Handler ep400Handler;

    @Autowired
    EComGWHandler ecomHandler;

    // socket占用端口
    @Value("${jit_embed.port}")
    private int jit_port;

    @Value("${ep400.port}")
    private int ep_port;

    @Value("${ecomm_gw.port}")
    private int ecom_port;

    /** 15秒发送一次心跳包 */
    private static final int HEARTBEATRATE = 15;

    @Bean
    public LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }

    @Bean
    public InetSocketAddress inetJitSocketAddress() {
        return new InetSocketAddress(jit_port);
    }

    @Bean
    public InetSocketAddress inetEPSocketAddress() {
        return new InetSocketAddress(ep_port);
    }
    @Bean
    public InetSocketAddress inetEcomSocketAddress() {
        return new InetSocketAddress(ecom_port);
    }

    @Bean
    public IoAcceptor ioJitAcceptor() throws Exception {
        IoAcceptor acceptor = new NioSocketAcceptor();
        //acceptor.getFilterChain().addLast("logger", loggingFilter());
        acceptor.getFilterChain().addLast("coderc",   // 使用自定义编码解码工厂类
                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));//设置编码过滤器
        acceptor.getSessionConfig().setReadBufferSize(1024*1024);//设置缓冲区
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);  //配置会话信息
        //心跳设置
////        KeepAliveMessageFactory heartBeatFactory = new KeepAliveMessageFactoryImpl();
////        KeepAliveFilter heartBeat = new KeepAliveFilter(heartBeatFactory,IdleStatus.BOTH_IDLE);
////        heartBeat.setForwardEvent(true); //设置是否forward到下一个filter
////        heartBeat.setRequestInterval(HEARTBEATRATE);        //设置心跳频率
////        heartBeat.setRequestTimeoutHandler(new RequestTimeout());//设置心跳失败
////        acceptor.getFilterChain().addLast("heartbeat", heartBeat);
        //心跳检测结束
        acceptor.setHandler(jitEmbedHandler); //自定义处理业务的代码：自定义的类
        acceptor.bind(inetJitSocketAddress());//绑定端口号
        log.info("Socket服务器在端口：" + jit_port + "已经启动");

        return acceptor;
    }

    @Bean
    public IoAcceptor ioEPAcceptor() throws Exception {
        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("coderc",   // 使用自定义编码解码工厂类
                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"),"\r", "\r")));//设置编码过滤器
        acceptor.getSessionConfig().setReadBufferSize(1024);//设置缓冲区
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);  //配置会话信息
        acceptor.setHandler(ep400Handler); //自定义处理业务的代码：自定义的类
        acceptor.bind(inetEPSocketAddress());//绑定端口号
        log.info("Socket服务器在端口：" + ep_port + "已经启动");

        return acceptor;
    }

    @Bean
    public IoAcceptor ioEComGWAcceptor() throws Exception {
        NioDatagramAcceptor acceptor =new NioDatagramAcceptor();
//        PrefixedStringCodecFactory codefct= new PrefixedStringCodecFactory(Charset.forName("HEX"));
//        codefct.setDecoderPrefixLength(1);
//        acceptor.getFilterChain().addLast("coderc",   // 使用自定义编码解码工厂类
//                new ProtocolCodecFilter(codefct));//设置编码过滤器
//        acceptor.setSessionRecycler(new ExpiringSessionRecycler(15 * 1000));
//        acceptor.getSessionConfig().setReuseAddress(true);
//        acceptor.getSessionConfig().setReceiveBufferSize(1024);
//        acceptor.getSessionConfig().setSendBufferSize(1024);
        acceptor.getFilterChain().addLast("logger",new LoggingFilter());
        acceptor.setHandler(ecomHandler); //自定义处理业务的代码：自定义的类
        acceptor.bind(inetEcomSocketAddress());//绑定端口号
        log.info("Socket UDP服务器在端口：" + ecom_port + "已经启动");

        return acceptor;
    }
}

