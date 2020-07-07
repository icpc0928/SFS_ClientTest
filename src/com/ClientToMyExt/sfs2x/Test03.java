package com.ClientToMyExt.sfs2x;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;

import java.util.HashMap;
import java.util.Map;

public class Test03 {
    SmartFox sfs = new SmartFox();

    String account = "Leo";
    String password = "P@ssw0rd";

    Room roomNow;
    String gameRoomName;

    int[] gameList = {1,2,3,4,5};
    int gameID;

    Test03(){

        gameID = gameList[(int) (Math.random() * gameList.length)];

        startEventListener();
    }

    void startEventListener(){
        sfs.addEventListener(SFSEvent.CONNECTION, this::onConnection);
        sfs.addEventListener(SFSEvent.CONNECTION_LOST, this::onConnectionLost);
        sfs.addEventListener(SFSEvent.LOGIN, this::onLogin);
        sfs.addEventListener(SFSEvent.ROOM_JOIN, this::onJoinRoom);
        sfs.addEventListener(SFSEvent.ROOM_JOIN_ERROR, this::onJoinRoomError);
        sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, this::onExtResponse);

        connect();
    }

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
        //是否連上SmartFoxServer
        if(success){
            System.out.println("Connection success");

            ISFSObject loginData = new SFSObject();
            loginData.putUtfString("LoginState", "4");
            loginData.putUtfString("APIUserGameID", String.valueOf(gameID));        //玩家要登入的遊戲編號在這

            System.out.println("LoginRequest, Account: " + account);
            sfs.send(new LoginRequest(account, password, "LeoTest", loginData));
        }
    }
    public void onConnectionLost(BaseEvent event) {
        System.out.println("onConnectionLost()");
        Map<String, Object> map = event.getArguments();
        for(String key : map.keySet()){
            System.out.println("key: " + key);          //key: reason
            System.out.println(map.get(key).toString());
        }
    }
    private void onLogin(BaseEvent event){      //Login成功後回傳login out data
        System.out.println("onLogin()");
        Map<String, Object> loginOutData = event.getArguments();

        for(String key : loginOutData.keySet()){
            System.out.println("key: " + key);          //key: data, zone, user
        }

        ISFSObject data = (ISFSObject) loginOutData.get("data");  //Server端回傳的LOGIN_OUT_DATA 名字叫這個
        String myAcc = data.getUtfString("YourAccount");
        String myPwd = data.getUtfString("YourPassword");
        String myLoginState = data.getUtfString("LoginState");
        System.out.println(myAcc+" + "+myPwd + " + "+ myLoginState);
    }

    private void onJoinRoom(BaseEvent event){
        System.out.println("onJoinRoom()");
        roomNow = (Room) event.getArguments().get("room");  //<--這是關鍵字，只找目前玩家進去的Room回傳Room

        String roomGroup = roomNow.getGroupId();        //要在後台設定GroupID(自定義)

        if(roomGroup.equals("Room" + gameID)){      //如果在遊戲房間
            roomGroup = "Game";
        }

        switch(roomGroup){
            //總大廳
            case "Lobby" :
                System.out.println("switch: Lobby");
                ISFSObject lobbyData = new SFSObject();
                lobbyData.putUtfString("GameID", "Room" + String.valueOf(gameID));

                sfs.send(new ExtensionRequest("GameLobbyInfo", lobbyData, roomNow));    //server端Lobby/GameLobbyInfo做
                break;
            //進遊戲大廳
            case "GameLobby" :
                System.out.println("switch: GameLobby");
                ISFSObject gameLobbyData = new SFSObject();
                gameLobbyData.putUtfString("GameID", "Room" + String.valueOf(gameID));
                gameLobbyData.putUtfString("BetLobby", "1");

                sfs.send(new ExtensionRequest("SlotTableInfo", gameLobbyData, roomNow));


                break;
            //進遊戲房
            case "Game" :
                System.out.println("switch: Game");
                gameRoomName = roomNow.getName();

                ISFSObject gameData = new SFSObject();
                sfs.send(new ExtensionRequest("Table", gameData, roomNow));
        }


    }
    private void onJoinRoomError(BaseEvent event){

    }

    private void onExtResponse(BaseEvent event){
        System.out.println("onExtResponse()");
        Map<String, Object> serverReturn = event.getArguments();

        String cmd = serverReturn.get("cmd").toString();            //關鍵字(命令的關鍵字)
        ISFSObject params = (ISFSObject) serverReturn.get("params");//關鍵字(取得server端帶入的參數)

        HashMap<String, Object> mapParams = new HashMap<>();

        for(String key : params.getKeys()){
            mapParams.put(key, params.getClass(key));
        }
        System.out.println("The cmd is: " + cmd);
        switch(cmd){
            case "LobbyInfo" :              //Server端 UserJoinZone傳來的回應
                System.out.println("LobbyInfo");
                int roomListSize = (int)mapParams.get("RoomListSize");
                for(int i = 0; i < roomListSize; i++) System.out.println("Room"+i+": "+mapParams.get("Room" + i).toString());
                System.out.println("LobbyName: " + mapParams.get("LobbyName").toString());
                sfs.send(new JoinRoomRequest(mapParams.get("LobbyName").toString())); //加入房間(UserJoinLobby)
                break;

            case "GameLobbyInfoResult" :    //Server端 GameLobbyInfo傳來的回應
                System.out.println("GameLobbyInfoResult");
                sfs.send(new JoinRoomRequest(mapParams.get("GameLobbyName").toString()));   //進入遊戲房間(我這裡是Room1~Room5)
                break;

        }

    }

}
