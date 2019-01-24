package cookbook.android.com.authenticator.utility.database.threads;

import android.content.Context;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;

import cookbook.android.com.authenticator.utility.database.DatabaseHelper;
import cookbook.android.com.authenticator.utility.database.TaskCompleteCallback;

public class RegisterThread extends Thread
{
    private Context context;
    private TaskCompleteCallback callback;
    private String emailToSearch, passwordToMatch, url;

    public RegisterThread(Context context, String email, String password, String url)
    {
        this.context = context;
        this.callback = (TaskCompleteCallback) context;
        emailToSearch = email;
        passwordToMatch = password;
        this.url = url;
    }

    //Call server-side PHP scripts to register a new account
    //Results returned via callback in JSON format
    @Override
    public void run()
    {
        try
        {
            String postData = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(emailToSearch, "UTF-8") + "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(passwordToMatch, "UTF-8");

            String result = DatabaseHelper.getHelper(context).postFromDB(url, postData);
            if(result != null)
            {
                JSONObject jsonResults = new JSONObject(result);
                callback.onTaskComplete(jsonResults);
            }
            else
            {
                callback.onTaskFailed("Not able to connect to server");
            }
        }
        catch(UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
        }
        catch(JSONException ex)
        {
            ex.printStackTrace();
        }
    }
}