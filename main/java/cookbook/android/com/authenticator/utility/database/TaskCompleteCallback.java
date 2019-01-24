package cookbook.android.com.authenticator.utility.database;

import org.json.JSONObject;

public abstract interface TaskCompleteCallback
{
    public abstract void onTaskComplete(JSONObject paramJSONObject);

    public abstract void onTaskFailed(String paramString);
}