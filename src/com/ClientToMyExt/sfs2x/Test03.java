package com.ClientToMyExt.sfs2x;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;

import java.util.Map;

public class Test03 {
    SmartFox sfs = new SmartFox();

    String account = "Leo";
    String password = "P@ssw0rd";

    Test03(){
        startEventListener();
    }

    void startEventListener(){
        sfs.addEventListener(SFSEvent.CONNECTION, this::onConnection);
        sfs.addEventListener(SFSEvent.CONNECTION_LOST, this::onConnectionLost);
        sfs.addEventListener(SFSEvent.LOGIN, this::onLogin);

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
    private void onLogin(BaseEvent event){
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

}
