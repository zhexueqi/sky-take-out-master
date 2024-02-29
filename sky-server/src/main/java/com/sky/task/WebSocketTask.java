package com.sky.task;

import com.alibaba.fastjson.JSON;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class WebSocketTask {
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 通过WebSocket每隔5秒向客户端发送消息
     */
//    @Scheduled(cron = "0/5 * * * * ?")
    public void sendMessageToClient() {
        webSocketServer.sendToAllClient("这是来自服务端的消息：" + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()));
    }

//    @Scheduled(cron = "0/10 * * * * ?")
//    public void sendPaymentSuccessToClient() {
//
//
//        Map map = new HashMap();
//        map.put("type",1); //1为订单提醒，2为催单
//        map.put("orderId",16);
//        map.put("content","订单号:"+"1709198914243");
//        String json = JSON.toJSONString(map);
//
//        webSocketServer.sendToAllClient(json);
//    }
}
