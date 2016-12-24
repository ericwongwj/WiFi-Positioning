package seu.cse.kai.wifinavi;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.sql.SQLException;

public class APContentProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://seu.cse.kai.provider.ap/aps");

    public APContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        int count;
        switch (uriMatcher.match(uri)) {
            case APS:
                count = apDB.delete(AP_TABLE, selection, selectionArgs);
                break;
            case AP_ID:
                String segment = uri.getPathSegments().get(1);
                count = apDB.delete(AP_TABLE, KEY_ID + "=" + segment
                        + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ")" : ""), selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data at the given URI.
        switch (uriMatcher.match(uri)) {
            case APS:
                return "vnd.android.cursor.dir/vnd.kai.ap";
            case AP_ID:
                return "vnd.android.cursor.item/vnd.kai.ap";
            default:
                throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        long rowID = apDB.insert(AP_TABLE, "ap", values);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new UnsupportedOperationException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        Context context = getContext();
        apDatabaseHelper dbHelper = new apDatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        apDB = dbHelper.getWritableDatabase();
        return (apDB == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables((AP_TABLE));
        switch (uriMatcher.match(uri)) {
            case AP_ID:
                qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                break;
        }
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = KEY_RSSI;
        }
        else {
            orderBy = sortOrder;
        }
        Cursor c = qb.query(apDB, projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(),uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        int count;
        switch (uriMatcher.match(uri)) {
            case APS:
                count = apDB.update(AP_TABLE, values, selection, selectionArgs);
                break;
            case AP_ID:
                String segment = uri.getPathSegments().get(1);
                count = apDB.update(AP_TABLE, values, KEY_ID + "=" + segment
                        + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ")" : ""), selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    // Database
    private SQLiteDatabase apDB;
    private static final String TAG = "APProvider";
    private static final String DATABASE_NAME = "aps.db";
    private static final int DATABASE_VERSION = 1;
    private static final String AP_TABLE = "aps";

    // Column names
    public static final String KEY_ID = "_id";
    public static final String KEY_SSID = "ssid";
    public static final String KEY_RSSI = "rssi";

    // Column indexes
    public static final int SSID_COLUMN = 1;
    public static final int RSSI_COLUMN = 2;

    // Helper class
    private static class apDatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_CREATE =
                "create table " + AP_TABLE + " ("
                        + KEY_ID + " integer primary key autoincrement, "
                        + KEY_SSID + " TEXT,"
                        + KEY_RSSI + " FLOAT);";

        public apDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", " +
                    "which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + AP_TABLE);
            onCreate(db);
        }
    }

    // UriMatcher: handle requests using URIs
    public static final int APS = 1;
    public static final int AP_ID = 2;
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("seu.cse.kai.provider.ap", "aps", APS); // all aps
        uriMatcher.addURI("seu.cse.kai.provider.ap", "aps/#", AP_ID); // a single ap
    }

}
