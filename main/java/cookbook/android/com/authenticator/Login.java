package cookbook.android.com.authenticator;

import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cookbook.android.com.authenticator.utility.IntentIds;
import cookbook.android.com.authenticator.utility.database.DatabaseURLs;
import cookbook.android.com.authenticator.utility.database.TaskCompleteCallback;
import cookbook.android.com.authenticator.utility.database.threads.LoginThread;
import cookbook.android.com.authenticator.views.NetworkActivity;
import myauth.android.com.authenticator.R;

public class Login extends NetworkActivity implements TaskCompleteCallback
{
    private Handler mainHandler;
    private ProgressDialog progressDialog;

    private void checkLogin(String username, String password)
    {
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        new LoginThread(this, username, password, DatabaseURLs.URL_LOGIN).start();
    }

    //process a successful login via an asynchronous task, storing the account data in a manager and launching the front page activity after login
    private void loginSuccess(final JSONObject results)
    {
        mainHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    progressDialog.dismiss();

                    final String email = results.getJSONArray("login").getJSONObject(0).getString("email");
                    String id = results.getJSONArray("login").getJSONObject(0).getString("id");
                    final String auth = results.getJSONArray("login").getJSONObject(0).getString("auth");

                    EditText passwordField = (EditText) findViewById(R.id.password_field);
                    final String password = passwordField.getText().toString();

                    new AsyncTask<String, Void, Intent>()
                    {
                        @Override
                        protected Intent doInBackground(String... params)
                        {
                            Bundle data = new Bundle();
                            try
                            {
                                data.putString(AccountManager.KEY_ACCOUNT_NAME, email);
                                data.putString(AccountManager.KEY_ACCOUNT_TYPE, IntentIds.ACCOUNT_TYPE);
                                data.putString(AccountManager.KEY_AUTHTOKEN, auth);
                                data.putString("accountPass", password);
                            }
                            catch(Exception e)
                            {
                                data.putString("errorMessage", e.getMessage());
                            }

                            final Intent res = new Intent();
                            res.putExtras(data);
                            return res;
                        }

                        @Override
                        protected void onPostExecute(Intent intent)
                        {
                            if (intent.hasExtra("errorMessage")) {
                                Toast.makeText(getBaseContext(), intent.getStringExtra("errorMessage"), Toast.LENGTH_SHORT).show();
                            } else {
                                final android.accounts.Account account = new android.accounts.Account(email, IntentIds.ACCOUNT_TYPE);
                                //manager.setPassword(account, password);
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
                catch(JSONException ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }

    //launch the registration activity
    public void loadRegister(View view)
    {
        startActivity(new Intent(getApplicationContext(), Registration.class));
    }

    //process information entered into the username and password fields and attempt a login
    public void login(View view)
    {
        EditText usernameField = (EditText) findViewById(R.id.username_field);
        EditText passwordField = (EditText) findViewById(R.id.password_field);

        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        if(! username.trim().isEmpty() && ! password.trim().isEmpty())
        {
            checkLogin(username, password);
            return;
        }
        Toast.makeText(getApplicationContext(), "Please enter username and password", Toast.LENGTH_LONG).show();
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        mainHandler = new Handler(getMainLooper());
    }

    //callback function from completed login thread
    public void onTaskComplete(JSONObject results)
    {
        try
        {
            if(results.getString("success").equals("0"))
            {
                onTaskFailed(results.getString("message"));
                return;
            }
            else if(results.getString("success").equals("1"))
            {
                loginSuccess(results);
            }
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
            return;
        }
    }

    //callback function from failed login thread that displaying a Toast message with a failure string
    public void onTaskFailed(final String reason)
    {
        mainHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                progressDialog.dismiss();
                Toast.makeText(Login.this.getApplicationContext(), "Failed to login: " + reason, Toast.LENGTH_LONG).show();
            }
        });
    }
}
