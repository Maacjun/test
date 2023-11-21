package edu.jit.nsi.iot_ms.transport.udp;


import edu.jit.nsi.iot_ms.serviceimpl.impl.EComGWServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EComGWHandler extends IoHandlerAdapter {
    @Autowired
    EComGWServiceImpl ecomService;

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
//        log.error("出现异常 :" + session.getRemoteAddress().toString() + " : " + cause.toString());
        log.error("EComGWHandler 出现异常 :" + session.getId() + " : " + cause.toString());
        session.closeNow();
    }


    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
//        log.info("EComGWHandler 接受到数据 :" + message);
        ecomService.msghandler(session,message);
    }


    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        log.info("EComGWHandler 发送消息内容 : " + message.toString());
//        session.write(message);
//        session.closeNow(); //短连接
    }

}
