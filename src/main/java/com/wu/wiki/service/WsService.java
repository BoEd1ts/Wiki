package com.wu.wiki.service;

import com.wu.wiki.websocket.WebSocketServer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public class WsService {
    @Resource
    private WebSocketServer webSocketServer;

    @Async //启用这个方法的时候调用另外一个线程
    public void sendInfo(String message){
        webSocketServer.sendInfo(message);
    }
}
