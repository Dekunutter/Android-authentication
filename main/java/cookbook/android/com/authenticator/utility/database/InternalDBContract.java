package cookbook.android.com.authenticator.utility.database;

import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//Old contract class for connecting to the original database.
//Redundant code but not removing just in case I need to use something similar in the future
public class InternalDBContract
{
    private InternalDBHelper helper;
    private static UriMatcher matcher;

    private static final String TEXT_TYPE = "TEXT";
    private static final String COMMA_SEP = ", ";

    private static final String SQL_CREATE_SHOPPING_LIST = "CREATE TABLE " + InternalDB.TABLE_SHOPPINGLIST + " (" + InternalDB.SHOPPINGLIST_ID + " INTEGER PRIMARY KEY," + InternalDB.SHOPPINGLIST_NAME + TEXT_TYPE + COMMA_SEP + InternalDB.SHOPPINGLIST_AMOUNT + TEXT_TYPE + " )";
    private static final String SQL_DELETE_SHOPPING_LIST = "DROP CREATE IF EXISTS " + InternalDB.TABLE_SHOPPINGLIST;

    private InternalDBContract()
    {

    }

    //hardcoded strings for SQL queries
    public static class InternalDB implements BaseColumns
    {
        public static final String TABLE_SHOPPINGLIST = "shoppinglist";
        public static final String SHOPPINGLIST_NAME = "name";
        public static final String SHOPPINGLIST_ID = "_id";
        public static final String SHOPPINGLIST_AMOUNT = "amount";
    }

    public static class InternalDBHelper extends SQLiteOpenHelper
    {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Cookbook.db";

        public InternalDBHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        //execute SQL create query
        @Override
        public void onCreate(SQLiteDatabase sqlDb)
        {
            //do for each table I store in internal DB
            sqlDb.execSQL(SQL_CREATE_SHOPPING_LIST);
        }

        //execute SQL delete query
        public void onUpgrade(SQLiteDatabase sqlDb, int oldVersion, int newVersion)
        {
            //do for each table
            sqlDb.execSQL(SQL_DELETE_SHOPPING_LIST);
            onCreate(sqlDb);
        }

        @Override
        public void onDowngrade(SQLiteDatabase sqlDb, int oldVersion, int newVersion)
        {
            onUpgrade(sqlDb, oldVersion, newVersion);
        }

        public static JSONObject cursorToJSON(Cursor cursor, String tableName)
        {
            JSONArray resultSet = new JSONArray();
            cursor.moveToFirst();
            while(cursor.isAfterLast() == false)
            {
                int columns = cursor.getColumnCount();
                JSONObject row = new JSONObject();
                for(int i = 0; i < columns; i++)
                {
                    if(cursor.getColumnName(i) != null)
                    {
                        try
                        {
                            row.put(cursor.getColumnName(i), cursor.getString(i));
                        }
                        catch(Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
                resultSet.put(row);
                cursor.moveToNext();
            }
            cursor.close();

            try
            {
                JSONObject finalResults = new JSONObject();
                finalResults.put(tableName, resultSet);
                return finalResults;
            }
            catch(JSONException ex)
            {
                ex.printStackTrace();
                return null;
            }
        }
    }
}
