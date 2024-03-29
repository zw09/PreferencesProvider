package com.ddmeng.preferencesprovider.provider;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ddmeng.preferencesprovider.BuildConfig;
import com.ddmeng.preferencesprovider.R;
import com.ddmeng.preferencesprovider.provider.base.BaseContentProvider;
import com.ddmeng.preferencesprovider.provider.preferences.PreferencesColumns;
import com.ddmeng.preferencesprovider.utils.LogUtils;

import java.util.Arrays;

public class PreferencesProvider extends BaseContentProvider {
    private static final String TAG = PreferencesProvider.class.getSimpleName();

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TYPE_CURSOR_ITEM = "vnd.android.cursor.item/";
    private static final String TYPE_CURSOR_DIR = "vnd.android.cursor.dir/";
    private static final String LIBRARY_DEFAULT_AUTHORITY = "com.ddmeng.preferencesprovider.provider";

    public static String CONTENT_URI_BASE;

    private static final int URI_TYPE_PREFERENCES = 0;
    private static final int URI_TYPE_PREFERENCES_ID = 1;


    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public boolean onCreate() {
        super.onCreate();
        String authority = getContext().getString(R.string.preferences_provider_authority);
        LogUtils.d("messi", "oncreate authority : " + authority);
        if (LIBRARY_DEFAULT_AUTHORITY.equals(authority)) {
            throw new IllegalStateException("Please don't use the library's default authority for your app. \n " +
                    "Multiple apps with the same authority will fail to install on the same device.\n " +
                    "Please add the line: \n " +
                    "==================================================================================================\n " +
                    " resValue \"string\", \"preferences_provider_authority\", \"${applicationId}.preferencesprovider\" \n " +
                    "==================================================================================================\n " +
                    "in your build.gradle file");
        }
        setAuthority(authority);
        return true;
    }

    private static void setAuthority(String authority) {
        URI_MATCHER.addURI(authority, PreferencesColumns.TABLE_NAME, URI_TYPE_PREFERENCES);
        URI_MATCHER.addURI(authority, PreferencesColumns.TABLE_NAME + "/#", URI_TYPE_PREFERENCES_ID);
        CONTENT_URI_BASE = "content://" + authority;
    }

    @Override
    protected SQLiteOpenHelper createSqLiteOpenHelper() {
        return PreferencesSQLiteOpenHelper.getInstance(getContext());
    }

    @Override
    protected boolean hasDebug() {
        return DEBUG;
    }

    @Override
    public String getType(Uri uri) {
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_TYPE_PREFERENCES:
                return TYPE_CURSOR_DIR + PreferencesColumns.TABLE_NAME;
            case URI_TYPE_PREFERENCES_ID:
                return TYPE_CURSOR_ITEM + PreferencesColumns.TABLE_NAME;

        }
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (DEBUG) Log.d(TAG, "insert uri=" + uri + " values=" + values);
        return super.insert(uri, values);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (DEBUG) Log.d(TAG, "bulkInsert uri=" + uri + " values.length=" + values.length);
        return super.bulkInsert(uri, values);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (DEBUG)
            Log.d(TAG, "update uri=" + uri + " values=" + values + " selection=" + selection + " selectionArgs=" + Arrays.toString(selectionArgs));
        return super.update(uri, values, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (DEBUG)
            Log.d(TAG, "delete uri=" + uri + " selection=" + selection + " selectionArgs=" + Arrays.toString(selectionArgs));
        return super.delete(uri, selection, selectionArgs);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (DEBUG)
            Log.d(TAG, "query uri=" + uri + " selection=" + selection + " selectionArgs=" + Arrays.toString(selectionArgs) + " sortOrder=" + sortOrder
                    + " groupBy=" + uri.getQueryParameter(QUERY_GROUP_BY) + " having=" + uri.getQueryParameter(QUERY_HAVING) + " limit=" + uri.getQueryParameter(QUERY_LIMIT));
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    protected QueryParams getQueryParams(Uri uri, String selection, String[] projection) {
        QueryParams res = new QueryParams();
        String id = null;
        int matchedId = URI_MATCHER.match(uri);
        switch (matchedId) {
            case URI_TYPE_PREFERENCES:
            case URI_TYPE_PREFERENCES_ID:
                res.table = PreferencesColumns.TABLE_NAME;
                res.idColumn = PreferencesColumns._ID;
                res.tablesWithJoins = PreferencesColumns.TABLE_NAME;
                res.orderBy = PreferencesColumns.DEFAULT_ORDER;
                break;

            default:
                throw new IllegalArgumentException("The uri '" + uri + "' is not supported by this ContentProvider");
        }

        switch (matchedId) {
            case URI_TYPE_PREFERENCES_ID:
                id = uri.getLastPathSegment();
        }
        if (id != null) {
            if (selection != null) {
                res.selection = res.table + "." + res.idColumn + "=" + id + " and (" + selection + ")";
            } else {
                res.selection = res.table + "." + res.idColumn + "=" + id;
            }
        } else {
            res.selection = selection;
        }
        return res;
    }
}
