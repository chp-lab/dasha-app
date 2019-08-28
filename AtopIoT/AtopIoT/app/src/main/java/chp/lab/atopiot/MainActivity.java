package chp.lab.atopiot;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


// Class
public class MainActivity extends AppCompatActivity {
    // Attribute
    private static final String TAG = "DB0x01";
    private Context context = this;
    final String myFile = "jfuhsduy";
    private final String serverModeCache = "serverMode";

    // Override Method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final ProgressBar progressBar = findViewById(R.id.mainProgressBar);
        final Button custoModeBtn = findViewById(R.id.customModeBtn);
        final Button autoModeBtn = findViewById(R.id.autoModeBtn);


        if(progressBar.getVisibility() == View.VISIBLE)
        {
            progressBar.setVisibility(View.GONE);
        }

        custoModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(progressBar.getVisibility() == View.GONE) {
                    progressBar.setVisibility(View.VISIBLE);
                    Log.d(TAG, "show progress bar");
                }

                Log.d(TAG, "custom mode selected");
                TokenManager.getInstance(context).writeToFile(serverModeCache, "custom");
                Log.d(TAG, "Get server information");
                getStatus();

            }
        });

        autoModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(progressBar.getVisibility() == View.GONE) {
                    progressBar.setVisibility(View.VISIBLE);
                    Log.d(TAG, "show progress bar");
                }

                Log.d(TAG, "auto mode selected");
                TokenManager.getInstance(context).writeToFile(serverModeCache, "auto");
                Log.d(TAG, "Get server information");
                getStatus();

            }
        });
        // Attribute


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Method for token valid
    public void getStatus(){
        String url = ServerInform.getInstance(context).getServerInform();

        Log.d(TAG, "getStatus have url= " + url);
        AlertDialog.Builder wifiDialog = new AlertDialog.Builder(context);
        wifiDialog.setTitle("Network connection failed!");
        wifiDialog.setMessage("Please connect to Wi-Fi");
        wifiDialog.setCancelable(false);

        wifiDialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "Waring complete");
                getStatus();
            }
        });

        if(url.equals("SERVER_NOT_FOUND"))
        {
            Log.d(TAG, "server configuration not found");
            wifiDialog.show();
            // Call ConfigActivity
        }
        else
        {
            // Read token from file

            final String myToken = TokenManager.getInstance(context).readFile(myFile);
            Log.d(TAG, "getStatus:mytoken= " + myToken);
            //
            // Error may occur in here
            //


            // token validation with server
            JsonObjectRequest myinform = new JsonObjectRequest
                    (Request.Method.GET, (url + "/myinform"), (String) null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // check response
                            boolean resType = false;
                            try
                            {
                                resType = Boolean.valueOf(response.get("type").toString());
                                if(resType)
                                {
                                    // Call Home Activity
                                    Log.d(TAG, "User logged in");
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                    finish();
                                }
                                else
                                {
                                    // Call login activity
                                    Log.d(TAG, "Login failed");
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                    finish();
                                }
                            }
                            catch (JSONException err)
                            {
                                Log.d(TAG, "session timeout");
                                // Call login activity
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "Cannot connect to server");
                            // Call config activity
                            startActivity(new Intent(MainActivity.this, ConfigActivity.class));
                            finish();
                        }
                    }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", myToken);
                    return headers;
                }
            };

            if(myToken.equals("invalid token"))
            {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
            else
            {
                MySingleton.getInstance(context).addToRequestQueue(myinform);
            }


        }

    }
}

