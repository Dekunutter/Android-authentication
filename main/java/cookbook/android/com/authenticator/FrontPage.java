package cookbook.android.com.authenticator;

import android.os.Bundle;

import cookbook.android.com.authenticator.fragments.FooterFragment;
import cookbook.android.com.authenticator.fragments.MainMenuFragment;
import cookbook.android.com.authenticator.views.NetworkActivity;
import myauth.android.com.authenticator.R;

public class FrontPage extends NetworkActivity
{
    @Override
    protected void initFragmentContainers(Bundle savedInstanceState)
    {
        if(savedInstanceState == null)
        {
            if(findViewById(R.id.main_container) != null)
            {
                MainMenuFragment mainFragment = new MainMenuFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.main_container, mainFragment).commit();
            }
            if(findViewById(R.id.footer_container) != null)
            {
                FooterFragment footerFragment = new FooterFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.footer_container, footerFragment).commit();
            }
        }
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frontpage);

        initFragmentContainers(savedInstanceState);
    }
}