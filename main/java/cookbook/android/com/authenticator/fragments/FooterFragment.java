package cookbook.android.com.authenticator.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import myauth.android.com.authenticator.R;

public class FooterFragment extends Fragment
{
    private View view;
    private TextView status;

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.footer, parent, false);
        status = (TextView) view.findViewById(R.id.network_status);
        return view;
    }

    //Footers are simple and just contain a label which I usually use for displaying the online status of the logged-in user
    public void setLabel(String message)
    {
        status.setText(message);
    }
}
