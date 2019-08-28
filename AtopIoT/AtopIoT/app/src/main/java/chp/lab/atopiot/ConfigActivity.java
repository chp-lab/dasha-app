package chp.lab.atopiot;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ConfigActivity extends AppCompatActivity {

    private static final String TAG = "DB0x08";
    private Context context = this;
    private final String serverModeCache = "serverMode";
    private final String localServerAddrCache = "localserver.conf";
    private final String cloudServerAddrCache = "cloudserver.conf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        final TextView retryConnect_btn = findViewById(R.id.retryConnect_btn);
        // New way 08/20/2019
        final EditText localServerEditText = findViewById(R.id.localServer_txt);
        final EditText cloudServerEditText = findViewById(R.id.cloudServer_txt);

        localServerEditText.setText(TokenManager.getInstance(context).readFile(localServerAddrCache));
        cloudServerEditText.setText(TokenManager.getInstance(context).readFile(cloudServerAddrCache));

        final Button saveConfigBtn = findViewById(R.id.saveConfig_btn);

        saveConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "save button clicked");
                // call confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm!");
                builder.setMessage("Save configuration ?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        TokenManager.getInstance(context).writeToFile(localServerAddrCache, localServerEditText.getText().toString());
                        TokenManager.getInstance(context).writeToFile(cloudServerAddrCache, cloudServerEditText.getText().toString());

                        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "Use old configuration", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });

        retryConnect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start main activity to check server connection again
                Log.d(TAG, "retryConnect clicked");
                startActivity(new Intent(ConfigActivity.this, MainActivity.class));
                finish();
            }
        });

    }


}


