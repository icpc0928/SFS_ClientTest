package com.ClientToMyExt.sfs2x;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;

import java.util.List;
import java.util.Map;

public class Test02  {
    SmartFox sfs = new SmartFox();

    String account = "Leo";
    String password = "P@ssw0rd";
    int n1 = 10;
    int n2 = 3;

   Test02(){

       startEventListener();
   }

   //載入所有必要監聽，然後連線
   void startEventListener(){
       sfs.addEventListener(SFSEvent.CONNECTION,this::onConnection);
       sfs.addEventListener(SFSEvent.CONNECTION_LOST,this::onConnectionLost);
       sfs.addEventListener(SFSEvent.LOGIN,this::onLogin);
       sfs.addEventListener(SFSEvent.ROOM_JOIN,this::onJoinRoom);
       sfs.addEventListener(SFSEvent.ROOM_JOIN_ERROR,this::onJoinRoomError);
       sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE,this::onExtResponse);   //接收擴展回應
       sfs.addEventListener(SFSEvent.CRYPTO_INIT,this::onExtResponse);

       connect();
   }

    //設定連線資訊 然後連線
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
           //連線成功
           System.out.println("Connection success!");
           //Connection成功後還不能表示已經Login成功了，LoginRequest帶入的資料只能做帳密驗整等等，確認是合法成員"之後"才有辦法繼續溝通
           ISFSObject loginData = new SFSObject();
           loginData.putUtfString("LoginState", "1234");
           sfs.send(new LoginRequest(account,password,"LeoTest",loginData));
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

   //擴展回傳
   private void onExtResponse(BaseEvent event){
       System.out.println("ExtResponse");
        //取得擴展傳送過來的參數
        Map<String, Object> serverReturn = event.getArguments();
        //取得命令的關鍵字
        String cmd = serverReturn.get("cmd").toString();
        //取得Server端所帶入的參數
        ISFSObject params = (ISFSObject) serverReturn.get("params");

        switch(cmd){
            case "Login":
                System.out.println("cmd = Login");
                break;
            case "LobbyInfo":
                System.out.println("cmd = LobbyInfo");
                int roomListSize = params.getInt("RoomListSize");

                for(int i = 0; i < roomListSize; i++){
                    String roomName = params.getUtfString("Room"+i);
                    System.out.println("LobbyName: " + roomName);
                }
                String roomName = params.getUtfString("Room0");
                int sum = params.getInt("Sum");
                System.out.println("Sum: " + sum);
                sfs.send(new JoinRoomRequest(roomName));
                break;

            case "Test01":
                System.out.println("cmd = Test01");
                int n1 = params.getInt("n1");
                int n2 = params.getInt("n2");

                //傳送
                ISFSObject request = new SFSObject();
                request.putInt("n3",n1);
                request.putInt("n4",n2);

                Room room = (Room)event.getArguments().get("room");
                System.out.println("Room is:" + room.getName() + "ID: " + room.getId());

                sfs.send(new ExtensionRequest("Test01",request, room));

                break;
            case "Test02" :
                System.out.println("cmd = Test02");
                int sum02 = params.getInt("sum");
                System.out.println("sum = " + sum02);

                sfs.send(new JoinRoomRequest("Room1"));
                break;
        }

   }
}
