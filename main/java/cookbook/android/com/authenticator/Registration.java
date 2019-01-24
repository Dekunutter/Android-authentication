package cookbook.android.com.authenticator;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cookbook.android.com.authenticator.utility.IntentIds;
import cookbook.android.com.authenticator.utility.database.DatabaseURLs;
import cookbook.android.com.authenticator.utility.database.TaskCompleteCallback;
import cookbook.android.com.authenticator.utility.database.threads.RegisterThread;
import cookbook.android.com.authenticator.views.NetworkActivity;
import myauth.android.com.authenticator.R;

public class Registration extends NetworkActivity implements TaskCompleteCallback
{
    private Handler mainHandler;
    private ProgressDialog progressDialog;

    //process a successful registration, adding the current account to a manager and launching the front page activity
    private void registerSuccess(final JSONObject results)
    {
        mainHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    progressDialog.dismiss();

                    final String email = results.getJSONArray("register").getJSONObject(0).getString("email");
                    String id = results.getJSONArray("register").getJSONObject(0).getString("id");
                    final String auth = results.getJSONArray("register").getJSONObject(0).getString("auth");

                    EditText passwordField = (EditText) findViewById(R.id.register_password_field);
                    final String password = passwordField.getText().toString();

                    new AsyncTask<String, Void, Intent>() {

                        @Override
                        protected Intent doInBackground(String... params)
                        {
                            Bundle data = new Bundle();
                            try {
                                data.putString(AccountManager.KEY_ACCOUNT_NAME, email);
                                data.putString(AccountManager.KEY_ACCOUNT_TYPE, IntentIds.ACCOUNT_TYPE);
                                data.putString(AccountManager.KEY_AUTHTOKEN, auth);
                                data.putString("accountPass", password);

                            } catch (Exception e) {
                                data.putString("errorMessage", e.getMessage());
                            }

                            final Intent res = new Intent();
                            res.putExtras(data);
                            return res;
                        }

                        @Override
                        protected void onPostExecute(Intent intent)
                        {
                            if (intent.hasExtra("errorMessage"))
                            {
                                Toast.makeText(getBaseContext(), intent.getStringExtra("errorMessage"), Toast.LENGTH_SHORT).show();
                            } else {
                                final android.accounts.Account account = new android.accounts.Account(email, IntentIds.ACCOUNT_TYPE);
                                manager.addAccountExplicitly(account, password, null);
                                manager.setAuthToken(account, IntentIds.AUTH_TYPE, auth);

                                setAccountAuthenticatorResult(intent.getExtras());
                                setResult(RESULT_OK, intent);

                                Intent intent2 = new Intent(getApplicationContext(), FrontPage.class);
                                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2);

                                finish();
                            }
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                catch (JSONException ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }

    //launch login activity
    public void loadLogin(View view)
    {
        startActivity(new Intent(getApplicationContext(), Login.class));
    }

    public void onCreate(Bundle svedInstanceState)
    {
        super.onCreate(svedInstanceState);
        setContentView(R.layout.registration);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        mainHandler = new Handler(getMainLooper());

        android.accounts.Account account = null;
    }

    //callback function of completed registration thread
    public void onTaskComplete(JSONObject results)
    {
        try
        {
            if(results.getString("success").equals("0"))
            {
                onTaskFailed(results.getString("message"));
            }
            else if(results.getString("success").equals("1"))
            {
                registerSuccess(results);
            }
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }


    //callback function of failed registration thread to display a Toast message with a failure string
    public void onTaskFailed(final String reason)
    {
        mainHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                progressDialog.dismiss();

                Toast.makeText(getApplicationContext(), "Failed to register user: " + reason, Toast.LENGTH_LONG).show();
            }
        });
    }

    //fetch information from username and password fields and attempt to register a new account with them
    public void register(View view)
    {
        EditText usernameField = (EditText) findViewById(R.id.register_username_field);
        EditText passwordField = (EditText) findViewById(R.id.register_password_field);

        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        if(!username.trim().isEmpty() && !password.trim().isEmpty())
        {

                progressDialog.setMessage("Registering...");
                progressDialog.show();

                RegisterThread registerThread = new RegisterThread(this, username, password, DatabaseURLs.URL_REGISTER);
                registerThread.start();
        }
    }
}
