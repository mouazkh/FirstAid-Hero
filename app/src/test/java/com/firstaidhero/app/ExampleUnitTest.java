package com.firstaidhero.app;

import org.junit.Test;  // تعليم الطريقة كاختبار

import static org.junit.Assert.*;  // دوال التحقق في الاختبارات مثل assertEquals

/** اختبار وحدة نموذجي يعمل على الحاسوب (من قالب Android Studio). */
public class ExampleUnitTest {
    /** يتحقق من أن عملية الجمع تعطي النتيجة الصحيحة. */
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}
