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

    int[] gameList = {1};
    int gameID;

    int betCount = 0;
    int runCount = 0;
    int baseCount = 0;

    String startPoint;
    String userPoint;
    double totalBet;
    double totalWinLose;
    double totalPayout;

    String account = "Leo";
    String password = "P@ssw0rd";

    Room roomNow;
    String gameRoomName;

    int[] lineTotalBet;


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

        if(baseCount != betCount)
        {
            System.out.println(account + " is disconnet !!!");
            System.out.println("=============" + account + "==============");
            System.out.println(account + ", gameID : " + gameID);
            System.out.println(account + ", gameRoomName : " + gameRoomName);
            System.out.println(account + ", startPoint : " + startPoint);
            System.out.println(account + ", userPoint : " + userPoint);
            System.out.println(account + ", betCount : " + baseCount);
//			System.out.println(account + ", totalBet : " + totalBet);
            System.out.println(account + ", totalPayout : " + totalPayout);
            System.out.println(account + ", totalWinLose : " + (Double.parseDouble(userPoint) - Double.parseDouble(startPoint)));
//			System.out.println(account + ", rtp : " + (double) totalPayout / (double) totalBet);
            System.out.println("=============" + account + "==============");
            System.out.println("------------------------------------------");
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
                //debug
                System.out.println("roomNow: " +roomNow.getName());
                System.out.println("GameID: " + "Room" + gameID);
                sfs.send(new ExtensionRequest("SlotTableInfo", gameLobbyData, roomNow));
                break;
            //進遊戲房
            case "Game" :
                System.out.println("switch: Game");
                gameRoomName = roomNow.getName();

                ISFSObject gameData = new SFSObject();
                sfs.send(new ExtensionRequest("Table", gameData, roomNow));
                break;
        }
    }

    private void onJoinRoomError(BaseEvent event){
        System.out.println("onJoinRoomError()");
        Map<String, Object> map = event.getArguments();
        for(String key : map.keySet()){
            System.out.println("key: " + key);          //key: reason
            System.out.println(map.get(key).toString());
        }
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

        System.out.println("The Response cmd is: " + cmd);

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
                System.out.println("GameLobbyName: " + mapParams.get("GameLobbyName").toString());
                sfs.send(new JoinRoomRequest(mapParams.get("GameLobbyName").toString()));   //進入遊戲房間(我這裡是Room1~Room5)
                break;

            case "SlotTableInfoResult" :
                System.out.println("SlotTableInfoResult");
                sfs.send(new JoinRoomRequest(mapParams.get("TableName").toString()));
                break;

            case "TableInfo" :
                System.out.println("TableInfo");
                startPoint = mapParams.get("UserPoint").toString();
                userPoint = mapParams.get("UserPoint").toString();
                play("0");
                break;

            case "BetResult" :
                System.out.println("BetResult");
                if(!mapParams.get("State").toString().equals("0")){
                    System.out.println(account + "Bet failed");
                    return;
                }
                baseCount++;

                userPoint = mapParams.get("UserPointAfter").toString();
                totalPayout += Double.parseDouble(mapParams.get("TotalWinPoint").toString());

                play(mapParams.get("GameState").toString());
                break;

            case "FreeSpinResult" :
                System.out.println("FreeSpinResult");
                if(!mapParams.get("State").toString().equals("0")){
                    System.out.println(account + " FreeSpin failed");
                    return;
                }

                userPoint = mapParams.get("UserPointAfter").toString();
                totalPayout += Double.parseDouble(mapParams.get("TotalWinPoint").toString());

                play(mapParams.get("GameState").toString());
                break;

            case "BonusResult" :
                System.out.println("BonusResult");
                if(!mapParams.get("State").toString().equals("0")){
                    System.out.println(account + " Bonus failed");
                    return;
                }

                userPoint = mapParams.get("UserPointAfter").toString();
                totalPayout += Double.parseDouble(mapParams.get("TotalWinPoint").toString());

                play(mapParams.get("GameState").toString());
                break;


        }

    }

    private void play(String gameState){
        if(baseCount >= betCount){
            sfs.disconnect();

            System.out.println("=============" + account + "==============");
            System.out.println(account + ", gameID : " + gameID);
            System.out.println(account + ", gameRoomName : " + gameRoomName);
            System.out.println(account + ", startPoint : " + startPoint);
            System.out.println(account + ", userPoint : " + userPoint);
            System.out.println(account + ", betCount : " + baseCount);
            System.out.println(account + ", totalPayout : " + totalPayout);
            System.out.println(account + ", totalWinLose : " + (Double.parseDouble(userPoint) - Double.parseDouble(startPoint)));
            System.out.println("=============" + account + "==============");
            System.out.println("------------------------------------------");
            return;
        }

        switch (gameState){
            case "0" :

                ISFSObject betData = new SFSObject();

                int betRand = (int) (Math.random() * 8);

                betData.putUtfString("LineBet", String.valueOf(betRand));
//				totalBet += lineTotalBet[betRand];
                sfs.send(new ExtensionRequest("Bet", betData, roomNow));

                break;

            case "1" :

                ISFSObject freeData = new SFSObject();
                sfs.send(new ExtensionRequest("FreeSpin", freeData, roomNow));

                break;

            case "2" :

                ISFSObject bonusData = new SFSObject();
                bonusData.putUtfString("Option", String.valueOf((int) (Math.random() * 3)));
                sfs.send(new ExtensionRequest("Bonus", bonusData, roomNow));

                break;
        }
    }

    private int[] convertObjectArray(Object[] obj)
    {
        int[] array = new int[obj.length];

        for(int i = 0; i < array.length; i++)
            array[i] = (int) obj[i];

        return array;
    }

}
