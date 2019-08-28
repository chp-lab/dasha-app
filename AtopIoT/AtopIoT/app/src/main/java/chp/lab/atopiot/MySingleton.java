package chp.lab.atopiot;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MySingleton {
    // Attribute
    private static MySingleton mySingleton;
    private RequestQueue requestQueue;
    private static Context context;

    // Method
    private MySingleton(Context context)
    {
        this.context = context;
        requestQueue = getRequestQueue();
    }
    public static synchronized MySingleton getInstance(Context context){
        if(mySingleton == null)
        {
            mySingleton = new MySingleton(context);
        }
        return mySingleton;
    }

    public RequestQueue getRequestQueue(){
        if(requestQueue == null)
        {
            requestQueue = Volley.newRequestQueue(this.context.getApplicationContext());
        }
        return requestQueue;
    }

    public<T> void addToRequestQueue(Request<T> request){
        // Add the specified request to the request queue
        getRequestQueue().add(request);
    }

}
