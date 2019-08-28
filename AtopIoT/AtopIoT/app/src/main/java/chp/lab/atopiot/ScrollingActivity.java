package chp.lab.atopiot;

import android.content.Intent;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.charts.Cartesian;
import com.anychart.core.utils.OrdinalZoom;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ScrollingActivity extends AppCompatActivity {
    private static final String TAG = "DB0x08";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Get today data
        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        int curMonth = calendar.get(Calendar.MONTH) + 1;
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        String curDate = curYear + "-" + curMonth + "-" + curDay;

        ArrayList<String> myMidList = getIntent().getExtras().getStringArrayList("machineIDArrayList");
        for(String myMid: myMidList)
        {
            Log.d(TAG, myMid);
            getNews(myMid);
        }

    }

    // Version 2.1 start dev 08/27/2019
    public void getNews(final String mid)
    {
        String url = ServerInform.getInstance(this).getServerAddressFromCache();

        // Read token from file
        final String myToken = TokenManager.getInstance(this).readFile(getString(R.string.myFile));
        // Send req via authorization headers
        JsonObjectRequest machineNews = new JsonObjectRequest
                (Request.Method.GET, url + "/realtime", (String) null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        boolean resType = false;
                        Log.d(TAG, response.toString());
                        try
                        {
                            resType = Boolean.valueOf(response.get("type").toString());
                            if(resType)
                            {
                                JSONObject realtimeJsonObj = response.getJSONArray("results").getJSONObject(0);
                                // Sample data
                                Iterator keys = realtimeJsonObj.keys();
                                ArrayList<String> ListKeys = new ArrayList<String>();

                                // Add keys to ListKeys
                                while (keys.hasNext())
                                {
                                    ListKeys.add((keys.next()).toString());
                                }


                                /*
                                final LinearLayout myLinearLayou = findViewById(R.id.newsFeedLinearLayout);

                                for(String key:ListKeys)
                                {
                                    String tmpValue = realtimeJsonObj.get(key).toString();
                                    Log.d(TAG, key + ":" + tmpValue);
                                    if(key.equals("time"))
                                    {
                                        // Keep old value
                                        String timeAxes = tmpValue;
                                        // Create string source in simple datetime format
                                        String datetimeStr = tmpValue.replace("Z", "");
                                        datetimeStr = datetimeStr + "+0000";
                                        Log.d(TAG, "utc= " + datetimeStr);
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

                                        try
                                        {
                                            // Convert datetime str to yyyy-MM-dd'T'HH:mm:ss.SSSZ format
                                            Date date = simpleDateFormat.parse(datetimeStr);
                                            Log.d(TAG, date.toString());
                                            // Update to local time and convert to sting
                                            timeAxes = date.toString();
                                        }
                                        catch(Exception dateErr)
                                        {
                                            // No change
                                            Log.d(TAG, "Failed on parse datetime");
                                        }
                                        TextView tmpTextView  = new TextView(getApplicationContext());
                                        tmpTextView.setText(key + ":" + timeAxes);
                                        myLinearLayou.addView(tmpTextView);
                                    }
                                    else
                                    {
                                        TextView tmpTextView  = new TextView(getApplicationContext());
                                        tmpTextView.setText(key + ":" + tmpValue);
                                        myLinearLayou.addView(tmpTextView);
                                    }

                                }
                                */

                                // Create news feed post
                                // textView
                            }
                            else
                            {
                                // Call login activity
                                Log.d(TAG, "getNews Auth failed");
                            }
                        }
                        catch(JSONException jsonErr)
                        {
                            Log.d(TAG, "getNews " + jsonErr.toString());
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "newsFeed onErrorResponse");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("authorization", myToken);
                // lowwer case
                headers.put("machineid", mid);
                // waiting for add time zone to headers
                return headers;
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(machineNews);
    }
}
