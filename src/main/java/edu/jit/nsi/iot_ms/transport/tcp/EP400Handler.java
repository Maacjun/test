package edu.jit.nsi.iot_ms.transport.tcp;


import edu.jit.nsi.iot_ms.serviceimpl.impl.EPServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EP400Handler extends IoHandlerAdapter {
    @Autowired
    EPServiceImpl epService;

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
//        log.error("出现异常 :" + session.getRemoteAddress().toString() + " : " + cause.toString());
        log.error("出现异常 :" + session.getId() + " : " + cause.toString());
        session.closeNow();
    }


    @Override
    public void sessionCreated(IoSession session) throws Exception {
//        log.info("连接创建 : " + session.getRemoteAddress().toString());
        session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
//        log.info("连接打开: " + session.getRemoteAddress().toString());
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
//        log.info("接受到数据 :" + message);
        epService.msghandler(session,message);
    }


    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        log.info("返回消息内容 : " + message.toString());
        //session.closeNow(); //短连接
    }

    /*@Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        if (status == IdleStatus.READER_IDLE) {
            log.info("进入读空闲状态");
            //session.closeNow();
        } else if (status == IdleStatus.BOTH_IDLE) {
              log.info("BOTH空闲");
            //session.closeNow();
        }
    }*/

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        String address = session.getRemoteAddress().toString().replace("/", "");
        log.info(address + "  会话关闭了!");
        epService.connClose(session);
        session.closeNow();
    }


}
