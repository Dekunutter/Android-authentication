package cookbook.android.com.authenticator.views;

import android.Manifest;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import myauth.android.com.authenticator.R;
import cookbook.android.com.authenticator.fragments.FooterFragment;
import cookbook.android.com.authenticator.utility.IntentIds;
import cookbook.android.com.authenticator.utility.NetworkAvailability;
import cookbook.android.com.authenticator.utility.database.InternalDBContract;

//base activity for inheriting basic network features
//extends authenticator activity so that accounts can be authenticated between activities
public abstract class NetworkActivity extends cookbook.android.com.authenticator.views.AccountAuthenticatorActivity
{
    private BroadcastReceiver receiver;
    private NetworkAvailability networkAvailability;

    protected Handler threadHandler;
    protected ProgressDialog progressDialog;

    protected InternalDBContract.InternalDBHelper internalDBHelper;

    protected boolean isOnline;

    protected AccountManager manager;
    protected boolean contactsPermissionGranted = false;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //receiver = new NetworkStateReceiver(new Handler());
        ////networkAvailability = NetworkAvailability.getInstance();
        //networkAvailability = new NetworkAvailability();
        //networkAvailability.registerNetworkAvailability(this, receiver);

        threadHandler = new Handler(getMainLooper());

        internalDBHelper = new InternalDBContract.InternalDBHelper(this);

        manager = AccountManager.get(this);
    }

    //check network availability when activity is resumed
    protected void onResume()
    {
        super.onResume();

        receiver = new NetworkStateReceiver(new Handler());
        //networkAvailability = NetworkAvailability.getInstance();
        networkAvailability = new NetworkAvailability();
        networkAvailability.registerNetworkAvailability(this, receiver);
    }

    //pause network availability checks when activity is paused
    protected void onPause()
    {
        unregisterReceiver();

        super.onPause();
    }

    protected void onStop()
    {
        super.onStop();
    }

    protected void onDestroy()
    {
        super.onDestroy();
    }

    //display a pop-up dialog with an alert message
    protected void displayAlert(final String reason)
    {
        threadHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(NetworkActivity.this);
                builder.setMessage(reason);
                builder.setCancelable(true);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });
    }

    //initialize common fragments to all activities inheriting this class (footer in this case)
    protected void initFragmentContainers(Bundle savedInstanceState)
    {
        if(savedInstanceState == null)
        {
            if(findViewById(R.id.footer_container) != null)
            {
                FooterFragment footerFragment = new FooterFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.footer_container, footerFragment).commit();
            }
        }
    }

    //hide the network progress dialog and display the alert dialog on network task failure
    public void onTaskFailed(String reason)
    {
        progressDialog.dismiss();
        displayAlert(reason);
    }

    //unregister a network receiver for checking network availability
    protected void unregisterReceiver()
    {
        try
        {
            networkAvailability.unregisterNetworkAvailability(this, receiver);
        }
        catch(IllegalArgumentException ex)
        {
            ex.printStackTrace();
        }
    }

    //custom broadcast receiver class for checking network availability and displaying the results in the footer fragment
    public class NetworkStateReceiver extends BroadcastReceiver
    {
        private final Handler handler;

        public NetworkStateReceiver(Handler handler)
        {
            this.handler = handler;
        }

        //check network state of phone (online or offline)
        @Override
        public void onReceive(final Context context, final Intent intent)
        {
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                    //final NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    //final NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                    FooterFragment footer = (FooterFragment) NetworkActivity.this.getSupportFragmentManager().findFragmentById(R.id.footer_container);
                    if(footer != null)
                    {
                        if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false))
                        {
                            if(footer != null)
                            {
                                footer.setLabel("Offline");
                            }
                            isOnline = false;
                        }
                        else
                        {
                            if(footer != null)
                            {
                                footer.setLabel("Online");
                            }
                            isOnline = true;
                        }
                    }
                }
            });
        }
    }

    //save contents to the cache
    protected void saveToCache(String contents, String filename)
    {
        try
        {
            File file = new File(getCacheDir(), filename);
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(contents.getBytes());
            stream.close();
        }
        catch(FileNotFoundException ex)
        {
            ex.printStackTrace();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    //load specified values from the cache
    protected Map<String, String> loadValuesFromCache(ArrayList<String> keys, String filename)
    {
        Map<String, String> results = new HashMap<String, String>();
        try
        {
            File file = new File(getCacheDir(), filename);
            FileInputStream stream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(stream);
            BufferedReader buffer = new BufferedReader(reader);

            String line;
            while((line = buffer.readLine()) != null)
            {
                for(String key : keys)
                {
                    if(line.startsWith(key))
                    {
                        results.put(key, line.substring(line.indexOf(":") + 1));
                    }
                }
            }
        }
        catch(FileNotFoundException ex)
        {
            ex.printStackTrace();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }

        return results;
    }

    //request contact permssions from the client device
    protected void requestContactPermissions()
    {
        if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS))
        {
            //NOTE TODO: make snackbar with parent layout and an onclick to request permissions like below in else
        }
        else
        {
            requestPermissions(new String[] {Manifest.permission.WRITE_CONTACTS}, 0);
        }
    }

    //process results of the contacts permissions request on the client device
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch(requestCode)
        {
            case 0:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //NOTE TODO: show snackbar on success
                    contactsPermissionGranted = true;
                }
                else
                {
                    //NOTE TODO: show snackbar on fail
                    contactsPermissionGranted = false;
                }
                return;
        }
    }
}
