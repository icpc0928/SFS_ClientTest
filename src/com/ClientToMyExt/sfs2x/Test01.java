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

public class Test01 implements IEventListener {

    public SmartFox sfs = new SmartFox();

    public void connection(){
        ConfigData cfg = new ConfigData();
        cfg.setZone("LeoTest");
        cfg.setDebug(true);
        cfg.setHost("127.0.0.1");
        cfg.setPort(9933);


        sfs.addEventListener(SFSEvent.CONNECTION, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                if((Boolean)baseEvent.getArguments().get("success")){
                    System.out.println("Connection OK!");
//            extensionRequest();
                    loginRequest();
                }else{
                    System.out.println("Connection failed");
                }
            }
        });
        sfs.connect(cfg);
    }

    public void loginRequest(){
        sfs.addEventListener(SFSEvent.LOGIN, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                System.out.println("Login successful!");
                //目前在Zone -> 進入RoomLobby
                joinLobbyRequest();
            }
        });
        sfs.addEventListener(SFSEvent.LOGIN_ERROR, baseEvent -> System.out.println("Login failure: " + baseEvent.getArguments().get("errorMessage")));

        sfs.send(new LoginRequest("icpc0928","","LeoTest"));
        
    }

    public void joinLobbyRequest(){
        sfs.addEventListener(SFSEvent.ROOM_JOIN, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                Room room = (Room) baseEvent.getArguments().get("room");
                System.out.println("Lobby joined successfully: " + room);
//                roomRequestHandler(room);

            }
        });
        sfs.addEventListener(SFSEvent.ROOM_JOIN_ERROR, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                System.out.println("Lobby joining failed: " + baseEvent.getArguments().get("errorMessage"));
            }
        });
        sfs.send(new JoinRoomRequest("Lobby"));
    }

    public void joinRoomRequest(){
        sfs.addEventListener(SFSEvent.ROOM_JOIN, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                Room room = (Room) baseEvent.getArguments().get("room");
                System.out.println("Room joined successfully: " + room);
            }
        });
        sfs.addEventListener(SFSEvent.ROOM_JOIN_ERROR, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                System.out.println("Room joining failed: " + baseEvent.getArguments().get("errorMessage"));
            }
        });
        sfs.send(new JoinRoomRequest("Room1"));
    }


    public void roomRequestHandler(Room room){
        sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                if(baseEvent.getArguments().get("cmd") == "GameLobbyInfoResult"){
                    ISFSObject respParams = (SFSObject) baseEvent.getArguments().get("params");
                    System.out.println(respParams.getUtfString("GameLobbyName"));
                }
            }
        });

        ISFSObject params = new SFSObject();
        params.putUtfString("GameID", "Room1");

        sfs.send(new ExtensionRequest("GameLobbyInfo", params, room));

    }



    public void extensionRequest(){
        sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, this::dispatch);

        ISFSObject params = new SFSObject();
        params.putInt("n1", 10);
        params.putInt("n2", 3);

        sfs.send(new ExtensionRequest("add2Nums", params));
        //extCmd - The name of the command which identifies an action that should be executed by the server-side Extension.
        //params - An instance of SFSObject containing custom data to be sent to the Extension. Can be null if no data needs to be sent.
        //room - If null, the specified command is sent to the current Zone server-side Extension;
        // if not null, the command is sent to the server-side Extension attached to the passed Room.
    }





    @Override
    public void dispatch(BaseEvent baseEvent) throws SFSException {

//
//        if(baseEvent.getArguments().get("cmd") == "add2Nums"){
//            ISFSObject responseParams = (SFSObject)baseEvent.getArguments().get("params");
//            //We expect an int parameter called "sum"
//            System.out.println("The sum is: " + responseParams.getInt("sum"));
//        }



    }
}
