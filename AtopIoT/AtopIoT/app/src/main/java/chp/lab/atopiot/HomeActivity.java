package chp.lab.atopiot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// mqtt
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "DB0x05";
    private Context context = this;
    final String myFile = "jfuhsduy";
    ArrayList<String> machinesArrayList = new ArrayList<String>();
    public ArrayList<String> machineIDArraList = new ArrayList<String>();

    // mqtt
    MqttAndroidClient mqttAndroidClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final ListView myListView = findViewById(R.id.machinesListView);
        final Button logout_btn = findViewById(R.id.logout_btn);
        final Button config_btn = findViewById(R.id.config_btn);
        final ImageView newFeeds = findViewById(R.id.newsFeed);

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TokenManager.getInstance(context).deleteFile(myFile);
                try {
                    Log.d(TAG, "disconnecting");
                    disconnect(mqttAndroidClient);
                }
                catch (Exception err)
                {
                    Log.d(TAG, "logout, error on disconnect");
                }
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
            }
        });

        config_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.d(TAG, "disconnecting");
                    disconnect(mqttAndroidClient);
                }
                catch (Exception err)
                {
                    Log.d(TAG, "logout, error on disconnect");
                }
                startActivity(new Intent(HomeActivity.this, ConfigActivity.class));
                finish();
            }
        });


        getMeasurements();


        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedItem = parent.getItemAtPosition(position).toString();
                try
                {
                    String selectedMachine = clickedItem.split(":")[0];
                    Log.d(TAG, clickedItem);
                    Intent intent = new Intent(HomeActivity.this, MachineActivity.class);
                    intent.putExtra("machineID", selectedMachine);
                    startActivity(intent);

                }
                catch (Exception e)
                {
                    Log.d(TAG, "Wrong format of machineID");
                }

            }
        });

        newFeeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "newFeed Clicked");
                /*
                Intent intent = new Intent(HomeActivity.this, ScrollingActivity.class);
                intent.putExtra("machineIDArrayList", machineIDArraList);
                startActivity(intent);
                */

            }
        });


    }

    public void getMeasurements(){
        // Read token from file
        final String myToken = TokenManager.getInstance(context).readFile(myFile);
        String url = ServerInform.getInstance(context).getServerAddressFromCache();

        // Log.d(TAG, "mytoken= " + myToken);

        // token validation with server
        // Get machine list
        // GET /list
        JsonObjectRequest machinesList = new JsonObjectRequest
                (Request.Method.GET, (url + "/list"), (String) null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        // check response
                        boolean resType = false;

                        JSONArray machinesListJson;

                        try
                        {
                            resType = Boolean.valueOf(response.get("type").toString());
                            machinesListJson  = response.getJSONArray("machines");

                            final String username = response.get("detail").toString();
                            String mqttUsername;
                            String mqttPassword;
                            // Server version 8.27/2019
                            try
                            {
                                mqttUsername = response.get("mqttUsername").toString();
                                mqttPassword = response.get("mqttPassword").toString();
                            }
                            catch (JSONException oldVersionErr)
                            {
                                mqttUsername = "chp-lab";
                                mqttPassword = "atop3352";
                            }

                            Log.d(TAG, "User: username is " + username);
                            setUpMqttClient(username, mqttUsername, mqttPassword);

                            if(resType)
                            {
                                Log.d(TAG, machinesListJson.toString());
                                String detail = response.get("detail").toString();

                                Log.d(TAG, "number of machines= " + Integer.toString(machinesListJson.length()));

                                for(int i = 0; i < machinesListJson.length(); i++)
                                {
                                    JSONObject tmpObj = machinesListJson.getJSONObject(i);
                                    String machineID = (tmpObj.get("machineID")).toString();
                                    String machineName = (tmpObj).get("machineName").toString();
                                    machinesArrayList.add(machineID + ":" + machineName);
                                    machineIDArraList.add(machineID);
                                }

                                final ListView machinesListView = findViewById(R.id.machinesListView);
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                                        (context, android.R.layout.simple_list_item_1, machinesArrayList){
                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent){
                                        /// Get the Item from ListView
                                        View view = super.getView(position, convertView, parent);

                                        // Set the border of View (ListView Item)
                                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                        {
                                            // view.setBackgroundColor(getResources().getColor(R.color.myListViewColor, context.getTheme()));
                                        }
                                        else {
                                            // view.setBackgroundColor(getResources().getColor(R.color.myListViewColor));
                                        }
                                        // Return the view
                                        return view;
                                    }
                                };
                                machinesListView.setAdapter(arrayAdapter);

                            }
                            else
                            {
                                // Call login activity
                                Log.d(TAG, "Auth failed");
                                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                                finish();
                            }
                        }
                        catch (JSONException err)
                        {
                            Log.d(TAG, "session timeout");
                            // Call login activity
                            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onError Volley");
                        Toast.makeText(getApplicationContext(), "Machines not found", Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", myToken);

                return headers;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(machinesList);
    }
    public void disconnect(@NonNull MqttAndroidClient client)
        throws MqttException{
        IMqttToken mqttToken = client.disconnect();
        mqttToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(TAG, "disconnect, success");
            }
            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.d(TAG, "disconnect, failed");
            }
        });
    }

    private void setUpMqttClient(final String username, final String mqttUsername, final String mqttPassword)
    {
        final String serverUrl = ServerInform.getInstance(context).getServerAddressFromCache();
        String brokerUrl;
        final String clientID = MqttClient.generateClientId();

        try
        {
            String serverAddr;
            serverAddr = serverUrl.split(":")[1];
            brokerUrl =  "tcp:" + serverAddr + ":1883";
            Log.d(TAG, "broker url is " + brokerUrl);
        }
        catch (Exception err)
        {
            Log.d(TAG, "wrong mqtt broker: " + serverUrl);
            brokerUrl = serverUrl.replace("http", "tcp");
            brokerUrl = brokerUrl.replace("81", "1883");
        }
        Log.d(TAG, "setUpMqttClient");


        mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(), brokerUrl, clientID);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if(reconnect)
                {
                    Log.d(TAG, "Mqtt client reconnecting to... " + serverURI);
                }
                else
                {
                    Log.d(TAG, "Mqtt connected with " + serverURI);
                }

                mqttSub(username + "/notify/#");
                mqttSub(username + "/alarm/#");
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "Mqtt connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                final String incommingMsg = new String(message.getPayload());
                String mid;
                String messagePriority;

                createNotificationChannel();

                try
                {
                    // Notification format: <username>/notify/<machineID>/...
                    messagePriority = topic.split("/")[1];
                    mid = topic.split("/")[2];
                }
                catch (Exception splitErr)
                {
                    messagePriority = "notify";
                    mid = "wrongnotficationformat";
                    Toast.makeText(getApplicationContext(), "Wrong notification format", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Notification format: <username>/notify/<machineID>/...");
                }

                final String machinID = mid;
                Log.d(TAG, "Notification from machineID " + machinID);
                Log.d(TAG, "Build.VERSION.SDK_INT= " + Integer.toString(Build.VERSION.SDK_INT));
                Log.d(TAG, "Build.VERSION_CODES.O= " + Integer.toString(Build.VERSION_CODES.O));

                if(messagePriority.equals("alarm"))
                {
                    // ringing
                    Log.d(TAG, "this is alarm message");
                    Intent intent = new Intent(HomeActivity.this, AlarmActivity.class);
                    intent.putExtra("alarmMessage", topic + ": " + incommingMsg);
                    disconnect(mqttAndroidClient);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "test")
                            .setSmallIcon(R.drawable.notificationicon)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(machinID + " " + incommingMsg)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true);

                    Intent intent = new Intent(context, MachineActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("machineID", machinID);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                    builder.setContentIntent(pendingIntent);

                    Log.d(TAG, "Incomming msg: "+ topic + ":" + incommingMsg);
                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                    notificationManagerCompat.notify(notificationID(), builder.build());
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setUserName(mqttUsername);
        mqttConnectOptions.setPassword(mqttPassword.toCharArray());

        try
        {
            Log.d(TAG, "Connecting to mqtt broker...");
            mqttAndroidClient.connect(mqttConnectOptions);
        }
        catch (MqttException err)
        {
            Log.d(TAG, err.toString());
        }
    }

    private void createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Log.d(TAG, "Creating notification chanel");
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("test", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public int notificationID()
    {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(now));
        Log.d(TAG, "notificationID= " + id);
        return id;
    }

    public void mqttSub(final String topic)
    {
        Log.d(TAG, "mqttSub");
        try
        {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Subscribe for " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed subscribe for " + topic);
                }
            });
        }
        catch (MqttException err)
        {
            Log.d(TAG, err.toString());
        }
    }

    public void mqttPub(final String topic, final String msg){

        Log.d(TAG, "pub");

        try
        {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            mqttAndroidClient.publish(topic, message);
            Log.d(TAG, "Pub topic: " + topic + " meesage: " + msg);
            if(!mqttAndroidClient.isConnected())
            {
                Log.d(TAG, "Message in buffer");
            }
        }
        catch(MqttException err)
        {
            Log.d(TAG, err.toString());
        }
    }
}
