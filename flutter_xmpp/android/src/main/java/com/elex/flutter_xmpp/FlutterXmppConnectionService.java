package com.elex.flutter_xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

public class FlutterXmppConnectionService extends Service {
    private static final String TAG ="flutter_xmpp";

    public static final String READ_MESSAGE = "com.elex.xmpp.flutter_xmpp.readmessage";
    public static final String SEND_MESSAGE = "com.elex.xmpp.flutter_xmpp.sendmessage";
    public static final String DISCONNECT = "com.elex.xmpp.flutter_xmpp.disconnect";
    public static final String REGISTER = "com.elex.xmpp.flutter_xmpp.register";
    public static final String RECEIVE_MESSAGE = "com.elex.xmpp.flutter_xmpp.receivemessage";
    public static final String CONNECT_STATUS = "com.elex.xmpp.flutter_xmpp.connectStatus";
    public static final String LOGIN_STATUS = "com.elex.xmpp.flutter_xmpp.loginStatus";
    public static final String REGISTER_STATUS = "com.elex.xmpp.flutter_xmpp.registerStatus";
    public static final String LOGIN = "com.elex.xmpp.flutter_xmpp.login";

    public static final String USERNAME = "userName";
    public static final String PASSWORD = "password";
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String BUNDLE_MESSAGE_BODY = "b_body";
    public static final String BUNDLE_MESSAGE_PARAMS = "b_body_params";
    public static final String BUNDLE_MESSAGE_TYPE = "b_type";
    public static final String BUNDLE_TO = "b_to";
    public static final String BUNDLE_FROM_JID = "b_from";


    public static FlutterXmppConnection.ConnectionState sConnectionState;
    public static FlutterXmppConnection.LoggedInState sLoggedInState;
    public static FlutterXmppConnection.ConnectToServerPurpose sConnectToServerPurpose;


    private String password = "";
    private String host = "";
    private String domin = "";
    private Integer port;
    private XMPPManager xmppManager;
    private boolean mActive;
    private Thread mThread;
    private Handler mTHandler;
    private FlutterXmppConnection mConnection;

    public static FlutterXmppConnection.ConnectionState getState() {
        if (sConnectionState == null) {
            return FlutterXmppConnection.ConnectionState.DISCONNECTED;
        }
        return sConnectionState;
    }

    public static FlutterXmppConnection.LoggedInState getLoggedInState() {
        if (sLoggedInState == null) {
            return FlutterXmppConnection.LoggedInState.LOGGED_OUT;
        }
        return sLoggedInState;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        this.host = extras.getString("hostName");
        this.port = extras.getInt("port");
        start();
        return Service.START_STICKY;
    }

    public void start() {

        if(!mActive) {
            mActive = true;
            if( mThread ==null || !mThread.isAlive()) {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        mTHandler = new Handler();
                        initConnection();
                        Looper.loop();
                    }
                });
                mThread.start();
            }
        }
    }

    public void stop() {

        mActive = false;
        mTHandler.post(new Runnable() {
            @Override
            public void run() {
                if( mConnection != null) {
                    mConnection.disconnect();
                }
            }
        });
    }

    private void initConnection() {

        if( mConnection == null) {
            mConnection = new FlutterXmppConnection(this,this.host,this.port,this.domin);

        }
        try {
            mConnection.connect();
        }catch (IOException | SmackException | XMPPException e) {

            e.printStackTrace();
            stopSelf();
        }
    }
    @Override
    public void onDestroy() {

        super.onDestroy();
        stop();
    }

}


