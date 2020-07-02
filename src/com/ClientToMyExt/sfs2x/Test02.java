package com.ClientToMyExt.sfs2x;

import com.smartfoxserver.v2.exceptions.SFSException;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.util.ConfigData;

public class Test02  {
    SmartFox sfs = new SmartFox();

   Test02(){

       startEventListener();
   }

   void startEventListener(){
       sfs.addEventListener(SFSEvent.CONNECTION,this::onConnection);
       sfs.addEventListener(SFSEvent.CONNECTION_LOST,this::onConnectionLost);
       sfs.addEventListener(SFSEvent.LOGIN,this::onLogin);
       sfs.addEventListener(SFSEvent.ROOM_JOIN,this::onJoinRoom);
       sfs.addEventListener(SFSEvent.ROOM_JOIN_ERROR,this::onJoinRoomError);
       sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE,this::onExtResponse);
       sfs.addEventListener(SFSEvent.CRYPTO_INIT,this::onExtResponse);

       connect();
   }

    //連線
   private void connect(){

       String host = "127.0.0.1";
       String zone = "LeoTest";
       int port = 9933;

       ConfigData config = new ConfigData();

       config.setHost(host);
       config.setZone(zone);
       config.setPort(port);

       sfs.connect(config);
   }


   private void onConnection(BaseEvent event){
       boolean success = (Boolean) event.getArguments().get("success");
       if(success){
            System.out.println("Connection success!");
       }else{
           System.out.println("Connection failed");
       }
   }

   private void onConnectionLost(BaseEvent event) {

   }
   private void onLogin(BaseEvent event){

   }
   private void onJoinRoom(BaseEvent event){

   }
   private void onJoinRoomError(BaseEvent event){

   }
   private void onExtResponse(BaseEvent event){

   }
}
