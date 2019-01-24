package cookbook.android.com.authenticator.utility.database;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import cookbook.android.com.authenticator.utility.database.threads.HttpInterruptThread;

public class DatabaseHelper
{
    private static DatabaseHelper instance;
    private Context context;

    private DatabaseHelper(Context context)
    {
        this.context = context;
    }

    //singleton instantiation for the database helper class
    public static DatabaseHelper getHelper(Context context)
    {
        if(instance == null)
        {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    //establish connection to database server via GET and fetch results (currently unused)
    public String getFromDB(String url, String postData)
    {
        try
        {
            url += ("?" + postData);
            URL urlObj = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)urlObj.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            new Thread(new HttpInterruptThread(httpURLConnection)).start();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            if(httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                Log.println(Log.ERROR, "MySQL error", "Error getting response");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()), 2048);
            String resultString = "";
            String line = "";
            while((line = reader.readLine()) != null)
            {
                resultString += line;
            }
            reader.close();
            httpURLConnection.disconnect();

            return resultString;
        }
        catch(MalformedURLException ex)
        {
            ex.printStackTrace();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    //establish connection to database server via POST and fetch results
    public String postFromDB(String url, String postData)
    {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)urlObj.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            new Thread(new HttpInterruptThread(httpURLConnection)).start();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setFixedLengthStreamingMode(postData.getBytes().length);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            PrintWriter out = new PrintWriter(httpURLConnection.getOutputStream());
            out.print(postData);
            out.flush();
            out.close();

            if(httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                Log.println(Log.ERROR, "MySQL error", "Error getting response");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String resultString = "";
            String line = "";
            while((line = reader.readLine()) != null)
            {
                resultString += line;
            }
            reader.close();
            httpURLConnection.disconnect();

            return resultString;
        }
        catch(MalformedURLException ex)
        {
            ex.printStackTrace();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
}