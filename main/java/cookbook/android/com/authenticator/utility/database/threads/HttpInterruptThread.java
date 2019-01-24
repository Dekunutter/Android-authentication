package cookbook.android.com.authenticator.utility.database.threads;

import java.net.HttpURLConnection;

public class HttpInterruptThread implements Runnable
{
    private HttpURLConnection connection;

    public HttpInterruptThread(HttpURLConnection connection)
    {
        this.connection = connection;
    }

    //kill the active HTTP connection after 5 seconds
    //(only called if the active connection is stalled or failing to connect since it will disconnect naturally by the calling methods otherwise)
    public void run()
    {
        try
        {
            Thread.sleep(5000);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }

        connection.disconnect();
    }
}
