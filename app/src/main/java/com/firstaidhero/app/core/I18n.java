package com.firstaidhero.app.core;

import android.content.Context;

/** Bilingual UI copy; Android's Noto Arabic UI fallback is selected for Arabic text. */
public final class I18n {
    private I18n() { }
    public static String t(Context c, String key) {
        boolean ar = AppPreferences.isArabic(c);
        switch (key) {
            case "tagline": return ar ? "إرشادات إسعاف أولي، حتى دون اتصال" : "First-aid guidance, even offline";
            case "start": return ar ? "ابدأ" : "Get started";
            case "next": return ar ? "التالي" : "Next";
            case "skip": return ar ? "تخطي" : "Skip";
            case "onboard1_title": return ar ? "تصرف بسرعة وبوضوح" : "Act quickly, with clarity";
            case "onboard1_body": return ar ? "اعثر على إرشادات الطوارئ من الشاشة الرئيسية دون قوائم معقدة." : "Find emergency guidance from a simple home screen without digging through menus.";
            case "onboard2_title": return ar ? "يعمل دون إنترنت" : "Works offline";
            case "onboard2_body": return ar ? "تبقى الحالات المحفوظة متاحة على جهازك دون اتصال." : "Saved cases stay on your device and remain available without a connection.";
            case "onboard3_title": return ar ? "جهات اتصال موثوقة" : "Trusted contacts, close by";
            case "onboard3_body": return ar ? "أضف أرقام الطوارئ المحلية وجهات الاتصال التي تريد الوصول إليها بسرعة." : "Add local emergency numbers and trusted contacts for quick access.";
            case "home": return ar ? "كيف يمكننا مساعدتك؟" : "How can we help?";
            case "home_sub": return ar ? "إرشادات واضحة للحظات التي تهم." : "Clear guidance for moments that matter.";
            case "search_hint": return ar ? "ابحث عن حالة طارئة..." : "Search an emergency";
            case "common": return ar ? "دليل الطوارئ" : "Emergency guide";
            case "contacts": return ar ? "جهات الاتصال" : "Emergency contacts";
            case "contacts_sub": return ar ? "اتصل بخدمات الطوارئ أو تواصل مع جهات الاتصال الموثوقة بسرعة." : "Call emergency services or reach your trusted contacts quickly.";
            case "emergency_numbers": return ar ? "أرقام الطوارئ" : "Emergency numbers";
            case "my_contacts": return ar ? "جهات الاتصال الخاصة بي" : "My contacts";
            case "empty_personal": return ar ? "لم تتم إضافة جهات اتصال شخصية بعد." : "No personal contacts added yet.";
            case "choose_from_contacts": return ar ? "اختيار من جهات اتصال الجهاز" : "Choose from device contacts";
            case "choose_contact": return ar ? "اختر جهة اتصال" : "Choose a contact";
            case "no_device_contacts": return ar ? "لا توجد جهات اتصال على هذا الجهاز." : "No contacts found on this device.";
            case "contacts_permission_title": return ar ? "الوصول إلى جهات الاتصال" : "Access your contacts";
            case "contacts_permission_body": return ar ? "نحتاج إذنك لقراءة جهات الاتصال واختيار رقم طوارئ منها. تبقى جميع البيانات على جهازك." : "Allow access to pick an emergency number from your device contacts. All data stays on your device.";
            case "contacts_denied": return ar ? "لم يُمنح الإذن. يمكنك الاختيار من تطبيق جهات الاتصال أو إدخال الرقم يدوياً." : "Permission not granted. You can pick from the Contacts app or type the number manually.";
            case "records": return ar ? "سجل الحالات" : "Case history";
            case "settings": return ar ? "الإعدادات" : "Settings";
            case "tab_home": return ar ? "الرئيسية" : "Home";
            case "tab_records": return ar ? "السجل" : "Records";
            case "tab_contacts": return ar ? "الاتصال" : "Contacts";
            case "tab_settings": return ar ? "الإعدادات" : "Settings";
            case "offline": return ar ? "المحتوى متاح دون إنترنت" : "Available offline";
            case "severity": return ar ? "مستوى الخطورة" : "Severity";
            case "symptoms": return ar ? "أعراض شائعة" : "Common signs";
            case "steps": return ar ? "ما الذي يمكنك فعله" : "What you can do";
            case "medical_notice": return ar ? "إرشادات هذا الإصدار أولية وتحتاج إلى مراجعة طبية محلية. عند وجود خطر مباشر، اتصل برقم الطوارئ المحلي واتبع تعليمات المجيب." : "Starter guidance needs local medical review. If there is immediate danger, call your local emergency number and follow the responder’s instructions.";
            case "no_results": return ar ? "لا توجد نتائج مطابقة." : "No matching cases found.";
            case "severity_critical": return ar ? "حرج" : "Critical";
            case "severity_moderate": return ar ? "متوسط" : "Moderate";
            case "severity_minor": return ar ? "طفيف" : "Minor";
            case "language": return ar ? "اللغة" : "Language";
            case "language_en": return "English";
            case "language_ar": return "العربية";
            case "appearance": return ar ? "المظهر" : "Appearance";
            case "theme_system": return ar ? "حسب إعدادات الجهاز" : "System default";
            case "theme_light": return ar ? "فاتح" : "Light";
            case "theme_dark": return ar ? "داكن" : "Dark";
            case "settings_hint": return ar ? "خصص لغة التطبيق ومظهره." : "Choose your language and appearance.";
            case "saved_on_device": return ar ? "تُحفظ التفضيلات على هذا الجهاز." : "Your preferences are saved on this device.";
            case "add": return ar ? "إضافة جهة اتصال" : "Add contact";
            case "save": return ar ? "حفظ" : "Save";
            case "cancel": return ar ? "إلغاء" : "Cancel";
            case "delete": return ar ? "حذف" : "Delete";
            case "name": return ar ? "الاسم" : "Name";
            case "phone": return ar ? "رقم الهاتف" : "Phone number";
            case "description": return ar ? "وصف الحالة" : "Case description";
            case "date": return ar ? "التاريخ (YYYY-MM-DD)" : "Date (YYYY-MM-DD)";
            case "procedures": return ar ? "الإجراءات التي تمت" : "Actions performed";
            case "photo": return ar ? "اختيار صورة" : "Choose photo";
            case "empty_records": return ar ? "لا توجد حالات مسجلة بعد." : "No treated cases recorded yet.";
            case "call": return ar ? "اتصال" : "Call";
            case "edit": return ar ? "تعديل" : "Edit";
            case "open_contacts": return ar ? "أرقام الطوارئ" : "Emergency contacts";
            case "open_records": return ar ? "تسجيل حالة معالجة" : "Log a treated case";
            case "police": return ar ? "الشرطة" : "Police";
            case "fire": return ar ? "الإطفاء" : "Fire";
            case "ambulance": return ar ? "الإسعاف" : "Ambulance";
            case "required": return ar ? "يرجى تعبئة الحقول المطلوبة." : "Please complete the required fields.";
            default: return key;
        }
    }
}
