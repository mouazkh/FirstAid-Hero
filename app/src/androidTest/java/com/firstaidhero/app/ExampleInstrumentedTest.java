package com.firstaidhero.app;

import android.content.Context;  // سياق التطبيق للوصول إلى الموارد والملفات وقواعد البيانات

import androidx.test.platform.app.InstrumentationRegistry;  // الوصول إلى سياق التطبيق أثناء الاختبار
import androidx.test.ext.junit.runners.AndroidJUnit4;  // مشغّل الاختبارات على جهاز أندرويد

import org.junit.Test;  // تعليم الطريقة كاختبار
import org.junit.runner.RunWith;  // تحديد مشغّل الاختبار

import static org.junit.Assert.*;  // دوال التحقق في الاختبارات مثل assertEquals

/** اختبار يعمل على جهاز أندرويد أو محاكٍ (من قالب Android Studio). */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    /** يتحقق من أن اسم حزمة التطبيق صحيح. */
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.firstaidhero.app", appContext.getPackageName());
    }
}
