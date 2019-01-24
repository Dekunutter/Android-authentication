package cookbook.android.com.authenticator.views;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

//base activity inherited for common account authentication features
public class AccountAuthenticatorActivity extends AppCompatActivity
{
    private AccountAuthenticatorResponse response = null;
    private Bundle result = null;

    public final void setAccountAuthenticatorResult(Bundle result)
    {
        this.result = result;
    }

    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        response = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if(response != null)
        {
            response.onRequestContinued();
        }
    }

    public void finish()
    {
        if(response != null)
        {
            if(result != null)
            {
                response.onResult(result);
            }
            else
            {
                response.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
            }
            response = null;
        }

        super.finish();
    }
}
