package com.firstaidhero.app.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Reads names and numbers from the device contacts so users can pick an
 * emergency contact instead of typing it. All data stays on the device.
 */
public final class DeviceContacts {
    public static final String[] PROJECTION = {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    public static final int COL_NAME = 1;
    public static final int COL_NUMBER = 2;

    private DeviceContacts() { }

    /** All device phone contacts, sorted by display name. Requires READ_CONTACTS. */
    public static Cursor query(Context context) {
        return context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
    }

    /** Reads the single contact returned by the system picker (ACTION_PICK). No permission needed. */
    public static Cursor lookup(Context context, Uri uri) {
        return context.getContentResolver().query(uri, PROJECTION, null, null, null);
    }
}
