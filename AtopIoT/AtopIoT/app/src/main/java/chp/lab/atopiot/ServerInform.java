package chp.lab.atopiot;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ServerInform {
    private static final String TAG = "DB0x07";
    private static Context context;
    private static ServerInform serverInform;
    private final String serverModeCache = "serverMode";
    private final String cloudServerAddrCache = "cloudserver.conf";
    private final String localServerAddrCache = "localserver.conf";

    // Constructor
    public ServerInform(Context context)
    {
        this.context = context;
    }

    public static synchronized ServerInform getInstance(Context context){
        if(serverInform == null)
        {
            serverInform = new ServerInform(context);
        }
        return serverInform;
    }

    public String getServerInform(){
        // default server address
        String serverIP;
        // developing-> auto server detection
        // Check internet connection first
        String APP_MODE = TokenManager.getInstance(context).readFile(serverModeCache);

        if(APP_MODE.equals("custom"))
        {
            // force to local mode only
            serverIP = TokenManager.getInstance(context).readFile(localServerAddrCache);
            if(serverIP.equals("invalid token"))
            {
                Log.d(TAG, "invalid localServerAddrCache");
                // default local ip addr
                serverIP = "http://192.168.1.12:81";
                Log.d(TAG, "creating default localServerAddrCache");
                TokenManager.getInstance(this.context).writeToFile(localServerAddrCache, serverIP);
                Log.d(TAG, "default localServerAddrCache created");
            }
            return serverIP;

        }
        else
        {
            // APP_MODE AUTO, LOCAL or CLOUD
            // Cloud mode
            if(internetIsConnected())
            {
                // read from file
                // if error on read, write default ip to file cloudserver.conf
                Log.d(TAG, "Cloud server");
                // get cloud server ip addr from cach
                serverIP = TokenManager.getInstance(this.context).readFile(cloudServerAddrCache);
                Log.d(TAG, "cloud server ip addr is " + serverIP);
                if(serverIP.equals("invalid token"))
                {
                    Log.d(TAG, "invalid cloudServerAddrCache");
                    // use app default server ip addr
                    serverIP = "http://54.254.186.136:81";
                    // record to cloudserver.conf
                    Log.d(TAG, "creating default cloudServerAddrCache");
                    TokenManager.getInstance(this.context).writeToFile(cloudServerAddrCache, serverIP);
                    Log.d(TAG, "default cloudServerAddrCache created");
                }
                TokenManager.getInstance(this.context).writeToFile(serverModeCache, "cloud");

            }
            // Local mode
            else
            {
                // wait->find local server ip
                // read from file
                // if error on read, find local server
                Log.d(TAG, "Local server");
                if(isWifiConnected(this.context))
                {
                    // get local server from cache
                    serverIP = TokenManager.getInstance(this.context).readFile(localServerAddrCache);
                    if(serverIP.equals("invalid token"))
                    {
                        Log.d(TAG, "invalid localServerAddrCache");
                        // default local ip addr
                        serverIP = "http://192.168.1.12:81";
                        Log.d(TAG, "creating default localServerAddrCache");
                        TokenManager.getInstance(this.context).writeToFile(localServerAddrCache, serverIP);
                        Log.d(TAG, "default localServerAddrCache created");
                    }
                }
                else
                {
                    // Wi-Fi not connected
                    Log.d(TAG, "Check Wifi connection");
                    serverIP = "SERVER_NOT_FOUND";
                }
                TokenManager.getInstance(this.context).writeToFile(serverModeCache, "local");
                Log.d(TAG, "server ip addr: " + serverIP);

            }
            return serverIP;
        }
    }

    public boolean testServerConnected(String url) {
        TokenManager.getInstance(this.context).deleteFile(localServerAddrCache);
        return false;
    }

    public String getServerAddressFromCache()
    {
        String APP_MODE = TokenManager.getInstance(context).readFile(serverModeCache);
        Log.d(TAG, "getServerAddrFromCache");

        if(APP_MODE.equals("cloud"))
        {
            String cloudServerAddr = TokenManager.getInstance(context).readFile(cloudServerAddrCache);
            Log.d(TAG, "cloudServerAddr is " + cloudServerAddr);
            if(cloudServerAddr.equals("invalid token"))
            {
                // Call show cloud config dialog method
                Log.d(TAG, "cloudServerAddrCache not found!");
                return "SERVER_NOT_FOUND";
            }
            else
            {
                return cloudServerAddr;
            }
        }
        else
        {
            String localServerAddr = TokenManager.getInstance(context).readFile(localServerAddrCache);
            Log.d(TAG, "localServerAddr is " + localServerAddr);
            if(localServerAddr.equals("invalid token"))
            {
                // Call local server config dialog
                Log.d(TAG, "localServerAddrCache not found!");
                return "SERVER_NOT_FOUND";
            }
            else
            {
                return  localServerAddr;
            }

        }
    }

    // please check this function
    public boolean internetIsConnected(){

        Runtime runtime = Runtime.getRuntime();
        Log.d(TAG, "checking internet connection...");
        try
        {
            Process process = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = process.waitFor();
            Log.d(TAG, "internet connected");
            Log.d(TAG, Integer.toString(exitValue));
            return true;

        }
        catch (Exception err)
        {
            Log.d(TAG, "Cannot connect to internet");
            return false;
        }

    }

    public boolean isWifiConnected(Context context)
    {
        Log.d(TAG, "check wifi");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Log.d(TAG, "wifiManager created");

        if(wifiManager.isWifiEnabled())
        {
            Log.d(TAG, "wifi adaptor is on");
            // Wi-Fi adaptor is on
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if(wifiInfo.getNetworkId() == -1)
            {
                Log.d(TAG, "not connect to access point");
                // Not connect to and access point
                return false;
            }
            else
            {
                Log.d(TAG, "wifi connected");
                return true;
            }
        }
        else
        {
            Log.d(TAG, "wifi adaptor is off");
            // Wi-Fi adaptor is off
            return false;
        }
    }
}


