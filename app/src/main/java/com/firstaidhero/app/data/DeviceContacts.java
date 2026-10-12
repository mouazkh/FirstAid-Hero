package com.firstaidhero.app.data;

import android.content.Context;  // سياق التطبيق للوصول إلى الموارد والملفات وقواعد البيانات
import android.database.Cursor;  // مؤشر لقراءة نتائج الاستعلام صفاً صفاً
import android.net.Uri;  // عنوان لمورد مثل صورة أو جهة اتصال أو رقم هاتف (tel:)
import android.provider.ContactsContract;  // أسماء جداول وأعمدة جهات اتصال الجهاز

/**
 * قراءة أسماء وأرقام جهات اتصال الجهاز ليختار المستخدم رقم طوارئ بدل كتابته.
 * لا تُرسل أي بيانات خارج الجهاز.
 */
public final class DeviceContacts {
    /** الأعمدة المطلوبة من جهات الاتصال (المعرّف، الاسم، الرقم) وموقع عمودي الاسم والرقم. */
    public static final String[] PROJECTION = {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    public static final int COL_NAME = 1;
    public static final int COL_NUMBER = 2;

    /** مُنشئ خاص يمنع إنشاء كائن من الفئة. */
    private DeviceContacts() { }

    /** تجلب كل أرقام الهواتف مرتبة أبجدياً حسب الاسم. تتطلب صلاحية READ_CONTACTS. */
    public static Cursor query(Context context) {
        return context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
    }

    /** تقرأ جهة الاتصال الواحدة التي أعادها منتقي النظام (ACTION_PICK)، ولا تحتاج صلاحية. */
    public static Cursor lookup(Context context, Uri uri) {
        return context.getContentResolver().query(uri, PROJECTION, null, null, null);
    }
}
