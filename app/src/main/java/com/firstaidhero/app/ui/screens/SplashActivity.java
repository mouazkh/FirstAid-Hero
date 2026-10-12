package com.firstaidhero.app.ui.screens;

import android.content.Intent;  // طلب لفتح شاشة أخرى أو تطبيق آخر (الاتصال، منتقي جهات الاتصال)
import android.graphics.Typeface;  // اختيار نوع الخط وسماكته
import android.graphics.drawable.GradientDrawable;  // خلفية بلون أو تدرج مع زوايا دائرية وإطار
import android.os.Bundle;  // حاوية لحفظ حالة الشاشة وتمرير البيانات بين الشاشات
import android.os.Handler;  // جدولة تنفيذ كود بعد مدة زمنية
import android.os.Looper;  // الخيط الرئيسي للواجهة الذي يعمل عليه Handler
import android.view.Gravity;  // محاذاة العناصر والنصوص
import android.widget.LinearLayout;  // حاوية ترتب العناصر عمودياً أو أفقياً
import android.widget.TextView;  // عرض نص
import com.firstaidhero.app.core.AppPreferences;  // إعدادات التطبيق المحفوظة (اللغة، المظهر)
import com.firstaidhero.app.core.I18n;  // نصوص الواجهة بالعربية والإنجليزية
import com.firstaidhero.app.ui.Ui;  // نظام التصميم المشترك (ألوان، نصوص، أزرار)

/** شاشة البداية: شعار التطبيق على خلفية حمراء، ثم الانتقال تلقائياً. */
public class SplashActivity extends BaseActivity {
    /** تبني شاشة البداية وتنتظر 650 ملّي ثانية، ثم تفتح شاشات التعريف أول مرة أو الشاشة الرئيسية بعدها. */
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); Ui.setSystemBarIcons(this, false, false);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);root.setPadding(Ui.dp(this,28),0,Ui.dp(this,28),0);
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Ui.RED,Ui.RED_DARK});root.setBackground(bg);
        TextView mark=Ui.centered(this,"✚",50,Ui.WHITE,true);mark.setBackground(Ui.rounded(this,0x33FFFFFF,0x33FFFFFF,48,0));root.addView(mark,Ui.lp(this,96,96));
        TextView title=Ui.centered(this,"FirstAid Hero",30,Ui.WHITE,true);title.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));LinearLayout.LayoutParams tp=Ui.lp(this,-1,-2);tp.topMargin=Ui.dp(this,18);root.addView(title,tp);
        TextView subtitle=Ui.centered(this,I18n.t(this,"tagline"),15,0xFFFFE7E8,false);LinearLayout.LayoutParams sp=Ui.lp(this,-1,-2);sp.topMargin=Ui.dp(this,7);root.addView(subtitle,sp);setContentView(root);
        new Handler(Looper.getMainLooper()).postDelayed(()->{Class<?> destination=AppPreferences.isOnboardingComplete(this)?MainActivity.class:OnboardingActivity.class;startActivity(new Intent(this,destination));finish();},650);
    }
}
