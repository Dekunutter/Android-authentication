package cookbook.android.com.authenticator.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import myauth.android.com.authenticator.R;

public class MainMenuFragment extends Fragment
{
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.mainmenu, parent, false);
    }
}
