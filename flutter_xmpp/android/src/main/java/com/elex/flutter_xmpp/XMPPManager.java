package com.elex.flutter_xmpp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

public class XMPPManager implements ConnectionListener {
    private  static final String TGA = "XMPPManager";
    private static XMPPManager mInstance;
    private Context mApplicationContext;
    private String mUsername;
    private String mPassword;
    private String mServiceName;
    private String mDomin;
    private String mHost;
    private Integer mPort;
    private String mResource = "Android";
    private XMPPTCPConnection mConnection;
    private ConnectResult mConnectResult;
    private LoginResult mLoginResult;
    private ConnectToServerPurpose mconnectToServerPurpose;
    private XMPPManager(){}

    public static enum ConnectToServerPurpose {
        ConnectToServerPurposeLogin,ConnectToServerPurposeRegister
    }
    public static XMPPManager getmInstance() {
        if (mInstance == null){
            synchronized (XMPPManager.class){
                if (mInstance == null){
                    mInstance = new XMPPManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     * @param hostName 服务地址
     * @param port 服务端口
     * @param domin 服务器域名 默认为hostname
     * @param connectResult 连接回调
     */
    public void init( String hostName, Integer port,String domin,ConnectResult connectResult){
        mHost = hostName;
        mPort = port;
        mDomin = domin;
        mConnectResult = connectResult;
        XMPPTCPConnectionConfiguration.Builder conf = XMPPTCPConnectionConfiguration.builder();
        if (domin ==null){
            mDomin = hostName;
        }else {
            mDomin = domin;
        }
        conf.setHost(hostName);

        if (mPort!=0){
            conf.setPort(mPort);
        }
//            conf.setServiceName(hostName);
//        conf.setResource(mResource);
        conf.setKeystoreType(null);
        conf.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        conf.setCompressionEnabled(true);
        mConnection = new XMPPTCPConnection(conf.build());
        mConnection.addConnectionListener(this);


    }

    public void login(String userID,String password,LoginResult loginResult){
        mconnectToServerPurpose = ConnectToServerPurpose.ConnectToServerPurposeLogin;
        mLoginResult = loginResult;
        mPassword = password;
        if (mConnection.isConnected()){

            mConnection.disconnect();
        }

//            mConnection.connect();


    }




    @Override
    public void connected(XMPPConnection connection) {
        Log.d(TGA,"连接成功");
        if (mconnectToServerPurpose == ConnectToServerPurpose.ConnectToServerPurposeLogin){
            try {
                mConnection.login(mUsername,mPassword);
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.d(TGA,"登录回调");
    }

    @Override
    public void connectionClosed() {
        Log.d(TGA,"连接关闭");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.d(TGA,"异常关闭");
    }

    @Override
    public void reconnectionSuccessful() {
        Log.d(TGA,"重连成功");
    }

    @Override
    public void reconnectingIn(int seconds) {
        Log.d(TGA,"重连成功");
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.d(TGA,"重连失败");
    }


    public interface ConnectResult{
        void  connectResult(Map map);
    }

    public interface LoginResult{
        void  loginResult(Map map);
    }


}
