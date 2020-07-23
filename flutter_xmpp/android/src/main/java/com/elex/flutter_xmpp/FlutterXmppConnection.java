package com.elex.flutter_xmpp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaIdFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.iqregister.packet.Registration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FlutterXmppConnection implements ConnectionListener, IncomingChatMessageListener {
    private static final String TAG ="flutter_xmpp";

    private Context mApplicationContext;
    private String mUsername;
    private String mPassword;
    private String mServiceName;
    private String mResource = "Android";
    private String mHost;
    private Integer mPort;
    private String mDomin;
    private XMPPTCPConnection mConnection;
    private BroadcastReceiver uiThreadMessageReceiver;


    public static enum ConnectionState{
        CONNECTED ,DISCONNECTED;
    }

    public static enum ConnectToServerPurpose{
        LOGIN,REGISTER;
    }

    public static enum LoggedInState{
        LOGGED_IN , LOGGED_OUT;
    }

    public FlutterXmppConnection(Context context,String hostName, Integer port,String domin){
        mApplicationContext = context.getApplicationContext();
        mHost = hostName;
        mPort = port;
        if (domin.isEmpty()){
            mDomin = hostName;
        }else {
            mDomin = domin;
        }


    }

    private void sendMessage (String messageType, String body, String to, Bundle args)
    {

        EntityBareJid jid = null;
        ChatManager chatManager = ChatManager.getInstanceFor(mConnection);
        try {
            jid = JidCreate.entityBareFrom(to+"@"+mDomin);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        Chat chat = chatManager.chatWith(jid);
        try {
            MessageExtension messageExtension = new MessageExtension();
            messageExtension.setMessageType(messageType);
            if (args!=null){
                String duration = args.get("duration").toString();
                if (duration.length()!=0){
                    messageExtension.setDuration(duration);
                }
            }
            Message message = new Message(jid, Message.Type.chat);
            message.setBody(body);
            message.addExtension(messageExtension);
            chat.send(message);


        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void connect() throws IOException, XMPPException, SmackException{
//        mUsername = userName;
//        mPassword = password;
        XMPPTCPConnectionConfiguration.Builder conf = XMPPTCPConnectionConfiguration.builder();
        conf.setXmppDomain(mDomin);
        conf.setResource("Android");
        if (validIP(mHost)){
            InetAddress address = InetAddress.getByName(mHost);
            conf.setHostAddress(address);
        }else {
            conf.setHost(mHost);
        }
        if (mPort!=0){
            conf.setPort(mPort);
        }
//        conf.setUsernameAndPassword(mUsername,mPassword);
        conf.setResource(mResource);
        conf.setKeystoreType(null);
        conf.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        conf.setCompressionEnabled(true);
        mConnection = new XMPPTCPConnection(conf.build());
        mConnection.addConnectionListener(this);
        try {
            mConnection.connect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        setupUiThreadBroadCastMessageReceiver();
        ChatManager.getInstanceFor(mConnection).addIncomingListener(this);

    }

    private void register(String userName,String password){
        if (FlutterXmppConnectionService.getState() == ConnectionState.CONNECTED){
            AccountManager accountManager = AccountManager.getInstance(mConnection);

            try {
                if (accountManager.supportsAccountCreation()){
                    accountManager.sensitiveOperationOverInsecureConnection(true);
                    accountManager.createAccount(Localpart.from(userName),password);
                }

            } catch (SmackException.NoResponseException | XmppStringprepException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
                e.printStackTrace();
                loginFail();
            }

            Intent intent = new Intent(FlutterXmppConnectionService.REGISTER_STATUS);
            intent.setPackage(mApplicationContext.getPackageName());
            intent.putExtra(FlutterXmppConnectionService.CODE,"200");
            intent.putExtra(FlutterXmppConnectionService.MESSAGE,"注册成功");
            mApplicationContext.sendBroadcast(intent);

        }

    }



    private void setupUiThreadBroadCastMessageReceiver() {
        Log.d(TAG,"setupUiThreadBroadCastMessageReceiver");

        uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                Log.d(TAG,"broadcast " + action);
                if (action == FlutterXmppConnectionService.LOGIN){

                    try {
                        mUsername = intent.getStringExtra(FlutterXmppConnectionService.USERNAME);
                        mPassword = intent.getStringExtra(FlutterXmppConnectionService.PASSWORD);
                        if (FlutterXmppConnectionService.getState() == ConnectionState.CONNECTED){
                            mConnection.login(mUsername,mPassword);
                        }else {
                            mConnection.connect();
                            FlutterXmppConnectionService.sConnectToServerPurpose = ConnectToServerPurpose.LOGIN;
                        }

                    } catch (XMPPException | SmackException | IOException | InterruptedException e) {
                        e.printStackTrace();
                        loginFail();
                    }
                }else if (action == FlutterXmppConnectionService.SEND_MESSAGE){

                    sendMessage(
                            intent.getStringExtra(FlutterXmppConnectionService.BUNDLE_MESSAGE_TYPE),
                            intent.getStringExtra(FlutterXmppConnectionService.BUNDLE_MESSAGE_BODY),
                            intent.getStringExtra(FlutterXmppConnectionService.BUNDLE_TO),
                            intent.getBundleExtra(FlutterXmppConnectionService.BUNDLE_MESSAGE_PARAMS));
                }else if (action == FlutterXmppConnectionService.DISCONNECT){

                   if (FlutterXmppConnectionService.getState() == ConnectionState.CONNECTED){
                       mConnection.disconnect();
                   }
                }else if (action == FlutterXmppConnectionService.REGISTER){

                    mUsername = intent.getStringExtra(FlutterXmppConnectionService.USERNAME);
                    mPassword = intent.getStringExtra(FlutterXmppConnectionService.PASSWORD);
                    if (FlutterXmppConnectionService.getState() == ConnectionState.CONNECTED){
                        register(mUsername,mPassword);
                    }
                }

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(FlutterXmppConnectionService.SEND_MESSAGE);
        filter.addAction(FlutterXmppConnectionService.LOGIN);
        filter.addAction(FlutterXmppConnectionService.READ_MESSAGE);
        filter.addAction(FlutterXmppConnectionService.REGISTER);
        filter.addAction(FlutterXmppConnectionService.DISCONNECT);
        mApplicationContext.registerReceiver(uiThreadMessageReceiver,filter);

    }

    private void loginFail(){
        Intent intent = new Intent(FlutterXmppConnectionService.LOGIN_STATUS);
        intent.setPackage(mApplicationContext.getPackageName());
        intent.putExtra(FlutterXmppConnectionService.CODE,"205");
        intent.putExtra(FlutterXmppConnectionService.MESSAGE,"登录失败");
        mApplicationContext.sendBroadcast(intent);
    }

    private void resgisterFail(){
        Intent intent = new Intent(FlutterXmppConnectionService.REGISTER_STATUS);
        intent.setPackage(mApplicationContext.getPackageName());
        intent.putExtra(FlutterXmppConnectionService.CODE,"206");
        intent.putExtra(FlutterXmppConnectionService.MESSAGE,"注册失败");
        mApplicationContext.sendBroadcast(intent);
    }
    public void disconnect() {

        if (mConnection != null){
            mConnection.disconnect();
            mConnection = null;
        }

        // Unregister the message broadcast receiver.
        if( uiThreadMessageReceiver != null) {
            mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
            uiThreadMessageReceiver = null;
        }
    }

    public static boolean validIP (String ip) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private void connectStateChange(){
        Intent intent = new Intent(FlutterXmppConnectionService.CONNECT_STATUS);
        intent.setPackage(mApplicationContext.getPackageName());
        switch (FlutterXmppConnectionService.sConnectionState){
            case CONNECTED:
            {
                intent.putExtra(FlutterXmppConnectionService.CODE,"200");
                intent.putExtra(FlutterXmppConnectionService.MESSAGE,"连接成功");
            }break;
            case DISCONNECTED:
            {
                intent.putExtra(FlutterXmppConnectionService.CODE,"401");
                intent.putExtra(FlutterXmppConnectionService.MESSAGE,"连接断开");
            }break;


        }
        mApplicationContext.sendBroadcast(intent);
    }
    @Override
    public void connected(XMPPConnection connection) {
        FlutterXmppConnectionService.sConnectionState=ConnectionState.CONNECTED;
        connectStateChange();
        if ( FlutterXmppConnectionService.sConnectToServerPurpose == ConnectToServerPurpose.LOGIN){
            try {
                mConnection.login(mUsername,mPassword);
            } catch (XMPPException | SmackException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG,"连接成功");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        FlutterXmppConnectionService.sConnectionState=ConnectionState.CONNECTED;
        Log.d(TAG,"验证成功");
        Intent intent = new Intent(FlutterXmppConnectionService.LOGIN_STATUS);
        intent.setPackage(mApplicationContext.getPackageName());
        intent.putExtra(FlutterXmppConnectionService.CODE,"200");
        intent.putExtra(FlutterXmppConnectionService.MESSAGE,"登录成功");
        mApplicationContext.sendBroadcast(intent);
    }


    @Override
    public void connectionClosed() {
        FlutterXmppConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
        connectStateChange();
        Log.d(TAG,"连接关闭");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        FlutterXmppConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
        connectStateChange();
        //被挤下线
        if (e.getMessage().contains("conflict")){

        }
        Log.d(TAG,"连接异常关闭");
    }

    @Override
    public void reconnectionSuccessful() {
        FlutterXmppConnectionService.sConnectionState=ConnectionState.CONNECTED;
        Log.d(TAG,"重连成功");
    }

    @Override
    public void reconnectingIn(int seconds) {

    }

    @Override
    public void reconnectionFailed(Exception e) {
        FlutterXmppConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
        connectStateChange();
        Log.d(TAG,"重连失败");
    }

    @Override
    public void newIncomingMessage(EntityBareJid messageFrom, Message message, Chat chat) {
        Log.d(TAG,message.toString());
        DelayInformation inf = null;
        inf = (DelayInformation)message.getExtension(DelayInformation.ELEMENT,DelayInformation.NAMESPACE);
        if (inf!=null){
            Date date = inf.getStamp();
        }
        String from = message.getFrom().toString();
        String to = message.getTo().toString();
        String fromJid="";
        String toJid="";
        if (from.contains("/")){
            fromJid = from.split("/")[0];

        }else {
            fromJid = from;
        }
        if (to.contains("/")){
            toJid = to.split("/")[0];

        }else {
            toJid = to;
        }


        Intent intent = new Intent(FlutterXmppConnectionService.RECEIVE_MESSAGE);
        intent.setPackage(mApplicationContext.getPackageName());
        intent.putExtra(FlutterXmppConnectionService.BUNDLE_FROM_JID,fromJid.split("@")[0]);
        intent.putExtra(FlutterXmppConnectionService.BUNDLE_TO,toJid.split("@")[0]);
        intent.putExtra(FlutterXmppConnectionService.BUNDLE_MESSAGE_BODY,message.getBody());
        intent.putExtra(FlutterXmppConnectionService.BUNDLE_MESSAGE_TYPE,message.getType().toString());

        mApplicationContext.sendBroadcast(intent);
    }

}
