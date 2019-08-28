package chp.lab.atopiot;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.core.utils.OrdinalZoom;
import com.anychart.data.Mapping;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.anychart.data.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MachineActivity extends AppCompatActivity {

    private static final String TAG = "DB0x06";
    private Context context = this;
    final String myFile = "jfuhsduy";

    // Non-Initialize
    private TextView mDisplayDate, endDate;
    private DatePickerDialog.OnDateSetListener myDateSetListener, endDateListener;
    private ProgressBar progressBar;
    private LinearLayout myLinearLayout;

    Cartesian cartesian;
    private Set set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine);

        Button back_btn = findViewById(R.id.back_btn);
        mDisplayDate = findViewById(R.id.startDate_lbl);
        endDate = findViewById(R.id.endDate_lbl);
        Button get_btn = findViewById(R.id.get_btn);
        progressBar = findViewById(R.id.progressBar);
        myLinearLayout = findViewById(R.id.linearLayout);


        Intent intent = getIntent();
        final String myMachineID = intent.getStringExtra("machineID");
        Log.d(TAG, myMachineID);

        // get realtime
        getRealTime(myMachineID);

        // Request data from server
        // GET /machine + Auth header + machineID header

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        // when click on date label
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create attribute
                // Get current date
                Calendar mCalendar = Calendar.getInstance();
                int mYear = mCalendar.get(Calendar.YEAR);
                int mMont = mCalendar.get(Calendar.MONTH);
                int mDay = mCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        context,
                        R.style.Theme_AppCompat_DayNight_Dialog,
                        myDateSetListener,
                        mYear, mMont, mDay
                );

                // Calling method
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                Log.d(TAG, "show dialog");


            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create attribute
                // Get current date
                Calendar mCalendar = Calendar.getInstance();
                int mYear = mCalendar.get(Calendar.YEAR);
                int mMont = mCalendar.get(Calendar.MONTH);
                int mDay = mCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        context,
                        R.style.Theme_AppCompat_DayNight_Dialog,
                        endDateListener,
                        mYear, mMont, mDay
                );

                // Calling method
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                Log.d(TAG, "show dialog");


            }
        });

        // Initialize
        myDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                String atDateTime = year + "-" + (month + 1) + "-" + day;
                Log.d(TAG,  atDateTime);
                mDisplayDate.setText(atDateTime);

            }
        };

        // Initialize
        endDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                String atDateTime = year + "-" + (month + 1) + "-" + day;
                Log.d(TAG,  atDateTime);
                endDate.setText(atDateTime);

            }
        };



        // Get today data
        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        int curMonth = calendar.get(Calendar.MONTH) + 1;
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        String curDate = curYear + "-" + curMonth + "-" + curDay;

        mDisplayDate.setText(curDate);
        endDate.setText(curDate);

        Log.d(TAG, "Date: " + curDate);
        getParameter(myMachineID, curDate, curDate);


        get_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmpStartDate, tmpEndDate;
                tmpStartDate = mDisplayDate.getText().toString();
                tmpEndDate = endDate.getText().toString();

                if(progressBar.getVisibility() == View.GONE) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                Log.d(TAG, "start date:" + tmpStartDate);
                Log.d(TAG, "end date:" + tmpEndDate);

                // get data
                getFromDate(myMachineID, tmpStartDate, tmpEndDate);

            }
        });

    }

    public void getParameter(final String mid, final String atDate, final String toDate){
        String url = ServerInform.getInstance(context).getServerAddressFromCache();
        // Read token from file
        final String myToken = TokenManager.getInstance(context).readFile(myFile);

        // Log.d(TAG, "mytoken= " + myToken);
        JsonObjectRequest machinesList = new JsonObjectRequest
                (Request.Method.GET, (url + "/machine"), (String) null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // check response
                        boolean resType = false;
                        try
                        {
                            resType = Boolean.valueOf(response.get("type").toString());

                            if(resType)
                            {

                                JSONArray resultJsonArray = response.getJSONArray("results");
                                Log.d(TAG, "response length= " + resultJsonArray.length());
                                // Sample data
                                JSONObject jsonKeys = resultJsonArray.getJSONObject(0);

                                Iterator keys = jsonKeys.keys();
                                ArrayList<String> ListKeys = new ArrayList<String>();

                                // Add keys to ListKeys
                                while (keys.hasNext())
                                {
                                    ListKeys.add((keys.next()).toString());

                                }

                                ArrayList<String> keysOfNumber = new ArrayList<String>();

                                for (String key:ListKeys) {
                                    Object valueObject = jsonKeys.get(key);
                                    if(valueObject instanceof Number)
                                    {
                                        keysOfNumber.add(key);
                                        Log.d(TAG, "keyOfNumber= " + key);
                                    }
                                }

                                Log.d(TAG, "keysOfNumber size= " + keysOfNumber.size());

                                int keyNum = keysOfNumber.size();

                                String[] xt = new String[resultJsonArray.length()];
                                Number[][] yi = new Number[resultJsonArray.length()][keyNum];

                                // Add data from each row to x or yi
                                for(int i = 0; i < resultJsonArray.length(); i ++)
                                {
                                    JSONObject resultJsonObject = resultJsonArray.getJSONObject(i);
                                    // Log.d(TAG, resultJsonObject.toString());
                                    // Add time to x

                                    String tmpDatetime = resultJsonObject.get("time").toString();
                                    // Keep old value
                                    String timeAxes = tmpDatetime;
                                    // Create string source follow simpledatetime format
                                    String datetimeStr = tmpDatetime.replace("Z", "");
                                    // UTC
                                    datetimeStr = datetimeStr + "+0000";
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                                    try
                                    {
                                        Date date = simpleDateFormat.parse(datetimeStr);
                                        timeAxes = date.toString();

                                    }catch(Exception datetimeErrParse)
                                    {
                                        Log.d(TAG, datetimeErrParse.toString());
                                    }
                                    xt[i] = timeAxes;
                                    // Log.d(TAG, xt[i]);

                                    for(int j = 0; j < keyNum; j ++)
                                    {
                                        String tmpKey = keysOfNumber.get(j);
                                        Number tmpValue = Float.valueOf(resultJsonObject.get(tmpKey).toString());
                                        yi[i][j] = tmpValue;
                                        // Log.d(TAG, tmpValue.toString());
                                    }
                                }
                                // Draw chart
                                // Create new line chart


                                final AnyChartView anyChartView = (AnyChartView) findViewById(R.id.myChart);
                                anyChartView.setProgressBar(findViewById(R.id.progress_bar));
                                // final AnyChartView anyChartView = new AnyChartView(context);
                                // myLinearLayout.addView(anyChartView);

                                anyChartView.setFocusable(false);

                                cartesian = AnyChart.line();
                                cartesian.animation(true);


                                cartesian.xScroller(true);
                                cartesian.xScroller().thumbs().autoHide(true);
                                cartesian.xScroller().position("beforeAxes");
                                cartesian.xScroller().minHeight(15);


                                OrdinalZoom xZoom = cartesian.xZoom();
                                xZoom.setToPointsCount(6, false, null);
                                xZoom.getStartRatio();
                                xZoom.getEndRatio();

                                cartesian.crosshair().enabled(true);
                                cartesian.crosshair()
                                        .yLabel(true)
                                        // TODO ystroke
                                        .yStroke((Stroke) null, null, null, (String) null, (String) null);

                                cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

                                String machineDetail = response.get("machineName").toString() + "(" + response.get("department") + ")";
                                cartesian.title(machineDetail);

                                cartesian.yAxis(0).title("Value");
                                cartesian.xAxis(0).labels().padding(2d, 2d, 2d, 2d);
                                cartesian.xAxis(0).labels().fontSize(2);


                                // Create sample data
                                List<DataEntry> seriesData = new ArrayList<>();

                                // Number[][] test = {{3.6, 2.3, 2.8}, {7.1, 4.0, 4.1}, {8.5, 6.2, 5.1}, {10.1, 13.0, 12.5}};
                                // Wait->change function to receive array list
                                for(int i = 0; i < resultJsonArray.length(); i++)
                                {
                                    seriesData.add(new MachineActivity.MyDataEntry(xt[i], yi[i]));
                                }

                                set = Set.instantiate();
                                set.data(seriesData);

                                Mapping myMapping[] = new Mapping[keyNum];
                                Line myLine[] = new Line[keyNum];

                                for(int i = 0; i < keyNum; i++)
                                {
                                    // (x, yi)
                                    String param = "y" + Integer.toString(i);
                                    String mappingStr = "{x:'x', value:'" + param + "'}";

                                    // Log.d(TAG, "mapping:  " + mappingStr);

                                    myMapping[i] = set.mapAs(mappingStr);

                                    myLine[i] = cartesian.line(myMapping[i]);
                                    myLine[i].name(keysOfNumber.get(i));

                                }

                                cartesian.legend().enabled(true);
                                cartesian.legend().fontSize(13d);
                                cartesian.legend().padding(0d, 0d, 10d, 0d);


                                Log.d(TAG, "drawing...");
                                anyChartView.setChart(cartesian);
                                Log.d(TAG, "complete");

                                if(progressBar.getVisibility() == View.VISIBLE) {
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                            else
                            {
                                // Call login activity
                                Log.d(TAG, "Auth failed");
                                startActivity(new Intent(MachineActivity.this, LoginActivity.class));
                            }
                        }
                        catch (JSONException err)
                        {
                            Log.d(TAG, "session timeout");

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onError Volley");
                        Toast.makeText(getApplicationContext(), "Cannot connect to server", Toast.LENGTH_SHORT).show();
                        ServerInform.getInstance(context).getServerInform();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                // Log.d(TAG, "this mid= " + mid);
                headers.put("Authorization", myToken);
                // lowwer case
                headers.put("machineid", mid);
                headers.put("atdatetime", atDate);
                headers.put("todatetime", toDate);
                headers.put("reqcmd", "now");
                // waiting for add time zone to headers
                return headers;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(machinesList);
    }

    public void getFromDate(final String mid, final String atDate, final String toDate){
        String url = ServerInform.getInstance(context).getServerAddressFromCache();
        // Read token from file
        final String myToken = TokenManager.getInstance(context).readFile(myFile);

        // Log.d(TAG, "mytoken= " + myToken);
        JsonObjectRequest machinesList = new JsonObjectRequest
                (Request.Method.GET, (url + "/machine"), (String) null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // check response
                        boolean resType = false;
                        try
                        {
                            resType = Boolean.valueOf(response.get("type").toString());

                            if(resType)
                            {

                                JSONArray resultJsonArray = response.getJSONArray("results");
                                Log.d(TAG, "response length= " + resultJsonArray.length());
                                // Sample data
                                JSONObject jsonKeys = resultJsonArray.getJSONObject(0);

                                Iterator keys = jsonKeys.keys();
                                ArrayList<String> ListKeys = new ArrayList<String>();

                                // Add keys to ListKeys
                                while (keys.hasNext())
                                {
                                    ListKeys.add((keys.next()).toString());

                                }

                                ArrayList<String> keysOfNumber = new ArrayList<String>();

                                for (String key:ListKeys) {
                                    Object valueObject = jsonKeys.get(key);
                                    if(valueObject instanceof Number)
                                    {
                                        keysOfNumber.add(key);
                                        Log.d(TAG, "keyOfNumber= " + key);
                                    }
                                }

                                Log.d(TAG, "keysOfNumber size= " + keysOfNumber.size());

                                int keyNum = keysOfNumber.size();

                                String[] xt = new String[resultJsonArray.length()];
                                Number[][] yi = new Number[resultJsonArray.length()][keyNum];

                                // Add data from each row to x or yi
                                for(int i = 0; i < resultJsonArray.length(); i ++)
                                {
                                    JSONObject resultJsonObject = resultJsonArray.getJSONObject(i);
                                    // Log.d(TAG, resultJsonObject.toString());
                                    // Add time to x

                                    String tmpDatetime = resultJsonObject.get("time").toString();
                                    // Keep old value
                                    String timeAxes = tmpDatetime;
                                    // Create string source follow simpledatetime format
                                    String datetimeStr = tmpDatetime.replace("Z", "");
                                    // UTC
                                    datetimeStr = datetimeStr + "+0000";
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                                    try
                                    {
                                        Date date = simpleDateFormat.parse(datetimeStr);
                                        timeAxes = date.toString();

                                    }catch(Exception datetimeErrParse)
                                    {
                                        // Log.d(TAG, datetimeErrParse.toString());
                                    }
                                    xt[i] = timeAxes;

                                    // Log.d(TAG, xt[i]);

                                    for(int j = 0; j < keyNum; j ++)
                                    {
                                        String tmpKey = keysOfNumber.get(j);
                                        Number tmpValue = Float.valueOf(resultJsonObject.get(tmpKey).toString());
                                        yi[i][j] = tmpValue;
                                        // Log.d(TAG, tmpValue.toString());
                                    }
                                }

                                // Create sample data
                                List<DataEntry> seriesData = new ArrayList<>();

                                for(int i = 0; i < resultJsonArray.length(); i++)
                                {
                                    seriesData.add(new MachineActivity.MyDataEntry(xt[i], yi[i]));
                                }

                                set.data(seriesData);
                                if(progressBar.getVisibility() == View.VISIBLE) {
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                            else
                            {
                                // Call login activity
                                Log.d(TAG, "Auth failed");
                                startActivity(new Intent(MachineActivity.this, LoginActivity.class));
                            }
                        }
                        catch (JSONException err)
                        {
                            Log.d(TAG, "session timeout");

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onError Volley");
                        Toast.makeText(getApplicationContext(), "Failed during get graph data", Toast.LENGTH_SHORT).show();
                        ServerInform.getInstance(context).getServerInform();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                // Log.d(TAG, "this mid= " + mid);
                headers.put("Authorization", myToken);
                // lowwer case
                headers.put("machineid", mid);
                headers.put("atdatetime", atDate);
                headers.put("todatetime", toDate);
                headers.put("reqcmd", "timewindows");
                // waiting for add time zone to headers
                return headers;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(machinesList);
    }

    private void getRealTime(final String mid){
        String url = ServerInform.getInstance(context).getServerAddressFromCache();
        // get /realtime with auth header
        // Read token from file
        final String myToken = TokenManager.getInstance(context).readFile(myFile);

        JsonObjectRequest machinesList = new JsonObjectRequest
                (Request.Method.GET, (url + "/realtime"), (String) null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // check response
                        boolean resType = false;
                        try
                        {
                            resType = Boolean.valueOf(response.get("type").toString());

                            if(resType)
                            {
                                Log.d(TAG, response.toString());
                                // add data to ListView
                                // find json keys
                                JSONObject realtimeJsonObj = response.getJSONArray("results").getJSONObject(0);

                                Iterator keys = realtimeJsonObj.keys();
                                ArrayList<String> ListKeys = new ArrayList<String>();

                                // Add keys to ListKeys
                                while (keys.hasNext())
                                {
                                    ListKeys.add((keys.next()).toString());
                                }


                                final LinearLayout myLinearLayou = findViewById(R.id.myLinearLayout);
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
                                        TextView tmpTextView  = new TextView(context);
                                        tmpTextView.setText(key + ":" + timeAxes);
                                        myLinearLayou.addView(tmpTextView);
                                    }
                                    else
                                    {
                                        TextView tmpTextView  = new TextView(context);
                                        tmpTextView.setText(key + ":" + tmpValue);
                                        myLinearLayou.addView(tmpTextView);
                                    }
                                }
                            }
                            else
                            {
                                // Call login activity
                                Log.d(TAG, "Auth failed");
                                startActivity(new Intent(MachineActivity.this, LoginActivity.class));
                            }
                        }
                        catch (JSONException err)
                        {
                            Log.d(TAG, err.toString());

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onError Volley");
                        Toast.makeText(getApplicationContext(), "Failed during get realtime data", Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                // Log.d(TAG, "this mid= " + mid);
                headers.put("authorization", myToken);
                headers.put("machineid", mid);
                return headers;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(machinesList);

    }

    private class MyDataEntry extends ValueDataEntry {

        // Constructor
        MyDataEntry(String x, Number[] y) {
            // (x, y0)
            super(x, y[0]);

            // y1, y2, y3, ..., yn if n = y.length - 1
            for(int i = 1; i < y.length; i ++)
            {
                String param = "y" + Integer.toString(i);
                setValue(param, y[i]);
            }
        }

    }

}
