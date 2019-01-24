package cookbook.android.com.authenticator.utility.database;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class DatabaseSyncAdapterService extends Service
{
    private static final String TAG = "DatabaseSyncAdapterService";
    private static SyncAdapterImpl syncAdapter = null;
    private static ContentResolver contentResolver = null;

    public DatabaseSyncAdapterService()
    {
        super();
    }

    //singleton instantiation of sync adapter instance
    private SyncAdapterImpl getSyncAdapter()
    {
        if(syncAdapter == null)
        {
            syncAdapter = new SyncAdapterImpl(this);
        }
        return syncAdapter;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        IBinder result = null;
        result = getSyncAdapter().getSyncAdapterBinder();
        return result;
    }

    //NOTE: Unfinished code
    private static void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) throws OperationCanceledException
    {
        contentResolver = context.getContentResolver();
    }

    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter
    {
        private Context context;

        public SyncAdapterImpl(Context context)
        {
            super(context, true);
            this.context = context;
        }

        //perform a sync with the database to fetch up-to-date account information
        @Override
        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
        {
            try
            {
                DatabaseSyncAdapterService.performSync(context, account, extras, authority, provider, syncResult);
            }
            catch(OperationCanceledException ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
