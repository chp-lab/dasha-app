package chp.lab.atopiot;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class TokenManager {
    // Create file method
    final String TAG = "DB0x02";
    private static TokenManager tokenManager;
    private static Context context;

    // Method
    private TokenManager(Context context)
    {
        this.context = context;
    }

    public static synchronized TokenManager getInstance(Context context){
        if(tokenManager == null)
        {
            tokenManager = new TokenManager(context);
        }
        return tokenManager;
    }

    public void writeToFile(String fileName, String jwt)
    {
        String filePath = context.getFilesDir().toString();

        Log.d(TAG, "write to: " + filePath + "/" + fileName);
        try
        {
            // Create new file if file not exist
            File file  = new File(filePath, fileName);

            if(file.createNewFile()){
                Log.d(TAG, "File Created");
            }
            else
            {
                Log.d(TAG, "File already exist");
            }

        }catch (IOException e){
            Log.d(TAG, "Failed on createFile");
        }

        FileOutputStream fileOutputStream;
        try{
            fileOutputStream = context.openFileOutput(fileName, context.MODE_PRIVATE);
            fileOutputStream.write(jwt.getBytes());
            fileOutputStream.close();
            Log.d(TAG, "Writing complete");
        }catch(Exception e){
            Log.d(TAG, "Failed on write to file");
        }

    }
    public String readFile(String fileName){
        String jwt = "invalid_token";

        try{
            File directory = context.getFilesDir();
            File file = new File(directory, fileName);

            Scanner scanner = new Scanner(file);
            while(scanner.hasNextLine()){
                jwt = scanner.nextLine();
                Log.d(TAG, fileName + ": " + jwt);
            }
        }catch (IOException e){
            jwt = "invalid token";
                Log.d(TAG, "Failed on readFile");
        }
        return jwt;
    }

    public void deleteFile(String fileNmae){
        try {
            context.deleteFile(fileNmae);
            Log.d(TAG, "token deleted");
        }catch (Exception e){
            Log.d(TAG, "error on delete file");
        }
    }
}
