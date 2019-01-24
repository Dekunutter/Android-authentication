package cookbook.android.com.authenticator.utility.database.threads;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cookbook.android.com.authenticator.utility.database.DatabaseHelper;
import cookbook.android.com.authenticator.utility.database.TaskCompleteCallback;

public class LoginThread extends Thread
{
    private TaskCompleteCallback callback;
    private Context context;
    private String emailToSearch;
    private String passwordToMatch;
    private String url;

    public LoginThread(Context context, String email, String password, String url)
    {
        this.context = context;
        callback = (TaskCompleteCallback)context;
        emailToSearch = email;
        passwordToMatch = password;
        this.url = url;
    }

    //Call server-side PHP scripts to login to the system as a specified account
    //Results returned via callback in JSON format
    @Override
    public void run()
    {
        try
        {
            String postData = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(emailToSearch, "UTF-8") + "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(passwordToMatch, "UTF-8");
            String results = DatabaseHelper.getHelper(context).postFromDB(url, postData);
            if (results != null)
            {
                JSONObject jsonResults = new JSONObject(results);
                callback.onTaskComplete(jsonResults);
            }
            else
            {
                callback.onTaskFailed("Not able to connect to server");
            }
        }
        catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
    }
}
