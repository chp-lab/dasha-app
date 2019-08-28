package chp.lab.atopiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "DB0x03";
    private Context context = this;
    final String myFile = "jfuhsduy";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button login_btn = findViewById(R.id.login_btn);
        final EditText username_txt = findViewById(R.id.username_txt);
        final EditText password_txt = findViewById(R.id.password_txt);

        // Call login method
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = username_txt.getText().toString();
                final String password  = password_txt.getText().toString();

                Log.d(TAG, "button, onClick");
                loginUser(username, password);

            }

        });
    }
    // Send username and password to server
    public void loginUser(String username, String password){
        // this may error
        // String url = ServerInform.getInstance(context).getServerAddressFromCache();
        // Edit to
        String url = ServerInform.getInstance(context).getServerInform();
        Log.d(TAG, "login url= " + url);
        Log.d(TAG, "login as " + username + ":" + password);

        // !!!! Here is error in adroid 9.0 Pie
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
        }catch (JSONException err){
            Log.d(TAG, err.toString());
        }

        JsonObjectRequest tokenRequest = new JsonObjectRequest
                (Request.Method.POST, (url + "/index"), jsonObject ,new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        String myToken = "invalid_token";
                        String resMessage;
                        boolean resType = false;
                        try
                        {
                            myToken = response.get("token").toString();
                            resMessage = response.get("message").toString();
                            resType = Boolean.valueOf(response.get("type").toString());

                            Log.d(TAG, myToken);
                            if(resType)
                            {
                                // Login complete
                                // Call user activity
                                TokenManager.getInstance(context).writeToFile(myFile, myToken);
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                finish();

                            }
                            else{
                                // Stay in login activity
                                Toast.makeText(getApplicationContext(), resMessage, Toast.LENGTH_SHORT).show();

                            }

                            Log.d(TAG, resMessage);
                            // Show resMessage on screen

                        }
                        catch (JSONException err)
                        {
                            Log.d(TAG, "JSONExeception, error myToken");

                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "tokenRequest, onErrorResponse");
                        Toast.makeText(getApplicationContext(), "Cannot connect to server", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, ConfigActivity.class));
                    }
                });

        Log.d(TAG, "add token req");
        MySingleton.getInstance(context).addToRequestQueue(tokenRequest);

    }

}
