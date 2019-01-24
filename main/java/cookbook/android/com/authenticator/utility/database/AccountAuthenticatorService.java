package cookbook.android.com.authenticator.utility.database;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import cookbook.android.com.authenticator.utility.IntentIds;
import cookbook.android.com.authenticator.utility.database.threads.LoginThread;
import cookbook.android.com.authenticator.views.NetworkActivity;

public class AccountAuthenticatorService extends Service
{
    private static AccountAuthenticatorImpl accountAuthenticator = null;

    public AccountAuthenticatorService()
    {
        super();
    }

    @Override
    public void onCreate()
    {
        getAuthenticator();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        IBinder results = getAuthenticator().getIBinder();
        return results;
    }

    //singleton instantiation of the account authenticator service implementation
    private AccountAuthenticatorImpl getAuthenticator()
    {
        if(accountAuthenticator == null)
        {
            accountAuthenticator = new AccountAuthenticatorImpl(this);
        }
        return accountAuthenticator;
    }

    //Implementation class of the account authenticator service
    private static class AccountAuthenticatorImpl extends AbstractAccountAuthenticator implements TaskCompleteCallback
    {
        private Context context;
        private String authToken;

        public AccountAuthenticatorImpl(Context context)
        {
            super(context);
            this.context = context;
        }

        //edit properties of the existing background service (nothing to edit in this implementation)
        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType)
        {
            return null;
        }

        //Add account to the background service
        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException
        {
            Bundle reply = new Bundle();

            Intent intent = new Intent(context, NetworkActivity.class);
            intent.putExtra(IntentIds.ACCOUNT_TYPE, accountType);
            intent.putExtra(IntentIds.AUTH_TYPE, authTokenType);
            intent.putExtra(IntentIds.ACCOUNT_NAME, true);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            reply.putParcelable(AccountManager.KEY_INTENT, intent);

            return reply;
        }

        //re-confirm credential of current account within the service. Not used for login, but for re-confirming the account for security reasons in the background
        //Not currently used though, but could be for a more secure implementation
        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException
        {
            return null;
        }

        //fetch the authentication token of the supplied account
        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException
        {
            //get the account manager from the current context so that we can fetch its auth token
            final AccountManager manager = AccountManager.get(context);
            authToken = manager.peekAuthToken(account, authTokenType);

            //try to authenticate the user if there was no auth token found
            if(TextUtils.isEmpty(authToken))
            {
                final String password = manager.getPassword(account);
                if(password != null)
                {
                    try
                    {
                        //authToken = new ParseComServerAuthenticate().userSignIn(account.name, password, authTokenType);
                        LoginThread loginThread = new LoginThread(context, account.name, password, DatabaseURLs.URL_LOGIN);
                        loginThread.start();
                        loginThread.join();
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }

            //if we found an auth token, return it
            if(!TextUtils.isEmpty(authToken))
            {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                return result;
            }

            //everything failed so launch the Login screen
            final Intent intent = new Intent(context, NetworkActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            intent.putExtra(IntentIds.ACCOUNT_TYPE, account.type);
            intent.putExtra(IntentIds.AUTH_TYPE, authTokenType);
            intent.putExtra(IntentIds.ACCOUNT_NAME, account.name);
            final Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }

        //return a string specifying the current privileges the logged in account has
        @Override
        public String getAuthTokenLabel(String authTokenType)
        {
            if("Full access".equals(authTokenType))
            {
                return "Full access to a Cookbook account";
            }
            else if("Read only".equals(authTokenType))
            {
                return "Read only access to a Cookbook account";
            }
            else
            {
                return authTokenType + " (Label)";
            }
        }

        //update account credentials (not currently used)
        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException
        {
            return null;
        }

        //check if the supplied account has particular features
        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException
        {
            final Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
            return result;
        }

        //callback function for processing the results of a finished a login thread called by the service
        @Override
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
                    authToken = results.getJSONArray("login").getJSONObject(0).getString("auth");
                }
            }
            catch(JSONException ex)
            {
                ex.printStackTrace();
            }
        }

        //callback function for processing the failure of a login thread called by the service
        @Override
        public void onTaskFailed(String paramString)
        {
            authToken = null;
        }
    }
}
