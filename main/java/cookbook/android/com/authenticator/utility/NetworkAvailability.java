package cookbook.android.com.authenticator.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

//unused class for checking network availability on client
public class NetworkAvailability
{
    private ConnectivityManager manager;
    private ConnectivityManager.NetworkCallback callback;
    private boolean receiverRegistered;

    private static NetworkAvailability instance;

    public NetworkAvailability()
    {

    }

    //singleton instantiation
    public static NetworkAvailability getInstance()
    {
        if(instance == null)
        {
            instance = new NetworkAvailability();
        }
        return instance;
    }

    //check if the network is available
    public static boolean isAvailable(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = manager.getActiveNetworkInfo();
        return ((network != null) && (network.isConnectedOrConnecting()));
    }

    //register network availability via broadcast receiver, ensuring client version meets minimum standards
    public void registerNetworkAvailability(final Context context, BroadcastReceiver receiver)
    {
        context.registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGED"));
        receiverRegistered = true;

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            context.registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        else
        {
            manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            callback = new ConnectivityManager.NetworkCallback()
            {
                @Override
                public void onAvailable(Network network)
                {
                    context.sendBroadcast(getNetworkAvailabilityIntent(true));
                }

                @Override
                public void onLost(Network network)
                {
                    context.sendBroadcast(getNetworkAvailabilityIntent(false));
                }
            };
            manager.registerNetworkCallback(builder.build(), callback);

            if(isAvailable(context))
            {
                context.sendBroadcast(getNetworkAvailabilityIntent(true));
            }
            else
            {
                context.sendBroadcast(getNetworkAvailabilityIntent(false));
            }
        }
    }

    //unregister network availability via broadcast receiver, ensuring client version meets minimum standards
    public void unregisterNetworkAvailability(Context context, BroadcastReceiver receiver)
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            context.unregisterReceiver(connectivityReceiver);
        }
        else
        {
            manager.unregisterNetworkCallback(callback);
        }

        if(receiverRegistered)
        {
            context.unregisterReceiver(receiver);
            receiverRegistered = false;
        }
    }

    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false))
            {
                context.sendBroadcast(getNetworkAvailabilityIntent(false));
            }
            else
            {
                context.sendBroadcast(getNetworkAvailabilityIntent(true));
            }
        }
    };

    //create intent containing network availability information for passing information between activities
    private Intent getNetworkAvailabilityIntent(boolean isNetworkAvailable)
    {
        Intent intent = new Intent("android.net.conn.CONNECTIVITY_CHANGED");
        intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, !isNetworkAvailable);
        return intent;
    }
}