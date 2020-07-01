package com.ClientTest.sfs2x;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import sfs2x.client.ISmartFox;
import sfs2x.client.SmartFox;
import sfs2x.client.bitswarm.IMessage;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.exceptions.SFSValidationException;
import sfs2x.client.requests.*;
import sfs2x.client.util.ConfigData;

public class ClientTest implements IEventListener {

    private SmartFox sfs = new SmartFox();
    private SmartFox sfs2 = new SmartFox(true); // if true , the SmartFoxServer API debug message are logged

//    private void initSmartFox(){
//
//
//    }

    private void otherMethod(){
        //是否連線
        System.out.println("是否連接? " + sfs.isConnected());
        //房間名稱
        String roomName = "The Lobby";
        Room room = sfs.getRoomByName(roomName);
        System.out.println("The ID of Room ' " + roomName + " ' is: " + room.getId());


        //The client usually connects to a SmartFoxServer instance through a socket connection.
        //In case a socket connection can't be established, and the useBlueBox property is set to true,
        // a tunnelled http connection through the BlueBox module is attempted. When a successful connection is established,
        // the connectionMode property can be used to check the current connection mode.
        //connect 在客戶端與Server 之間建立連接。 如果未傳遞參數，則客戶端將使用通過loadConfig()方法加載的設置
        //connect的參數  host, port

        sfs.connect();
        sfs.connect("127.0.0.1", 8080);
        //
        sfs.isJoining();

        //send() 向server發送請求 request類別中可找到所有可用的請求對象
        sfs.send(new LoginRequest("icpc0928","password","zoneName"));

        sfs.send(new JoinRoomRequest("LobbyID"));

        //Example : creates an object containing some parameters and sends it to the server-side Extension
        ISFSObject params = new SFSObject();
        params.putInt("x", 10);
        params.putInt("y", 3);

        sfs.send(new ExtensionRequest("setPosition", params));

    }



    //調派器
    @Override
    public void dispatch(BaseEvent baseEvent) throws SFSException {

    }

    ////////////////////////////////////BaseEvent.getArguments() 回傳一組HashMap<String, Object>
    ////////////////////////////////////也可以用.setArguments() 賦予其HashMap的值


    //SmartFox Connection監聽事件 (含組態資料)
    private void connection(){
        ConfigData cfg = new ConfigData();
        cfg.setZone("BasicExamples");
        cfg.setDebug(true);
        cfg.setHost("127.0.0.1");
        cfg.setPort(9933);
        sfs.addEventListener(SFSEvent.CONNECTION, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                if(baseEvent.getType().equals(SFSEvent.CONNECTION)){
                    boolean success = (Boolean) baseEvent.getArguments().get("Success");
                    if(success) System.out.println("Connected OK!");
                    else System.out.println("Connection failed");
                }
            }
        });

        sfs.connect(cfg);

    }

    //SmartFox Connection  (一般)
    private void connection2(){
        sfs.addEventListener(SFSEvent.CONNECTION, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                if((Boolean)baseEvent.getArguments().get("success")){
                    System.out.println("Connection OK!");
                }else{
                    System.out.println("Connection failed");
                }
            }
        });
        sfs.connect();
    }


    //SmartFox Login 監聽事件
    private void login(){
        sfs.addEventListener(SFSEvent.LOGIN, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                System.out.println("Login Failure: " + baseEvent.getArguments().get("errorMessage"));
            }
        });

        //Login
        sfs.send(new LoginRequest("userName","","SimpleChat"));
        //           LoginRequest( userName,  password, zoneName, params)
    }


    //SmartFox JoinRoom 監聽事件
    private void joinRoom(){
        sfs.addEventListener(SFSEvent.ROOM_JOIN, new IEventListener() {
            @Override
            public void dispatch(BaseEvent evt) throws SFSException {
                System.out.println("Room joined successfully: " + (Room)evt.getArguments().get("room"));
            }
        });
        sfs.addEventListener(SFSEvent.ROOM_JOIN, new IEventListener() {
            @Override
            public void dispatch(BaseEvent evt) throws SFSException {
                System.out.println("Room joining failed: " + evt.getArguments().get("errorMessage"));
            }
        });

        // Join a Room called "Lobby"
        sfs.send(new JoinRoomRequest("Lobby"));
    }

    //SmartFox Logout 監聽事件
    private void logout(){
        sfs.addEventListener(SFSEvent.LOGOUT, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                System.out.println("Logout executed!");
            }
        });

        //Logout!
        sfs.send(new LogoutRequest());
    }

    //SmartFox Room_Remove
    private void roomRemove(){
        sfs.addEventListener(SFSEvent.ROOM_REMOVE, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                System.out.println("The following Room was removed: " + (Room)baseEvent.getArguments().get("room"));
            }
        });

    }

    //SmartFox RoomAdd 監聽
    private void roomAdd(){
        sfs.addEventListener(SFSEvent.ROOM_ADD, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                System.out.println("Room created: " + (Room)baseEvent.getArguments().get("room"));
            }
        });
        sfs.addEventListener(SFSEvent.ROOM_CREATION_ERROR, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                System.out.println("Room creation failure: " + baseEvent.getArguments().get("errorMessage"));
            }
        });

        //Define the settings of a new chat Room
        RoomSettings settings = new RoomSettings("My Chat Room");
        settings.setMaxUsers(40);
        settings.setGroupId("chats");

        // Create the Room
        sfs.send(new CreateRoomRequest(settings));
    }

    //Change Room Name
    private void changeRoomName(){
        sfs.addEventListener(SFSEvent.ROOM_NAME_CHANGE, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                System.out.println("Room " + baseEvent.getArguments().get("oldName") + " was renamed to " +
                        ((Room)baseEvent.getArguments().get("room")).getName());
            }
        });
        sfs.addEventListener(SFSEvent.ROOM_NAME_CHANGE_ERROR, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                System.out.println("Room name change failed: " + baseEvent.getArguments().get("errorMessage"));
            }
        });

        Room theRoom = sfs.getRoomByName("Leo's Room");

        //Rename the Room to a new name
        sfs.send(new ChangeRoomNameRequest(theRoom,"Leo The Great Room"));
    }

    private void configLoad(){
        sfs.addEventListener(SFSEvent.CONFIG_LOAD_SUCCESS, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                //Configuration loaded successfully, now connect
                sfs.connect();
            }
        });
        sfs.addEventListener(SFSEvent.CONFIG_LOAD_FAILURE, new IEventListener() {
            @Override
            public void dispatch(BaseEvent baseEvent) throws SFSException {
                System.out.println("Failed loading configuration file");
            }
        });
        sfs.loadConfig("testConfig.xml",false);
    }




}
