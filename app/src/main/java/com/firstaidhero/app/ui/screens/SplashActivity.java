package com.firstaidhero.app.ui.screens;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.firstaidhero.app.core.AppPreferences;
import com.firstaidhero.app.core.I18n;
import com.firstaidhero.app.ui.Ui;

public class SplashActivity extends BaseActivity {
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
