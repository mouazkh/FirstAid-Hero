package com.firstaidhero.app.ui.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.firstaidhero.app.core.AppPreferences;
import com.firstaidhero.app.core.I18n;
import com.firstaidhero.app.ui.Ui;

public class OnboardingActivity extends BaseActivity {
    private int page = 0;
    private static final String[] TITLES = {"onboard1_title","onboard2_title","onboard3_title"};
    private static final String[] BODIES = {"onboard1_body","onboard2_body","onboard3_body"};
    private static final String[] ICONS = {"✚","⌁","☎"};
    @Override protected void onCreate(Bundle state) { super.onCreate(state); if(state!=null) page=state.getInt("page",0); render(); }
    @Override protected void onSaveInstanceState(Bundle out) { out.putInt("page",page); super.onSaveInstanceState(out); }
    private void render() {
        LinearLayout root = Ui.column(this);
        LinearLayout brand = new LinearLayout(this); brand.setGravity(Gravity.CENTER_VERTICAL); TextView mark=Ui.centered(this,"✚",17,Ui.RED,true); mark.setBackground(Ui.rounded(this,Ui.softAccent(this),Ui.softAccent(this),19,0)); brand.addView(mark,Ui.lp(this,38,38));
        TextView name=Ui.text(this,"FirstAid Hero",16,Ui.ink(this),true); LinearLayout.LayoutParams namep=Ui.lp(this,-2,-2);namep.leftMargin=Ui.dp(this,9);brand.addView(name,namep);root.addView(brand);
        LinearLayout stage=new LinearLayout(this);stage.setOrientation(LinearLayout.VERTICAL);stage.setGravity(Gravity.CENTER);stage.setPadding(Ui.dp(this,8),0,Ui.dp(this,8),0);root.addView(stage,new LinearLayout.LayoutParams(-1,0,1));
        TextView icon=Ui.centered(this,ICONS[page],46,Ui.RED,true);icon.setBackground(Ui.rounded(this,Ui.softAccent(this),Ui.softAccent(this),52,0));LinearLayout.LayoutParams ip=Ui.lp(this,104,104);ip.gravity=Gravity.CENTER;stage.addView(icon,ip);
        TextView title=Ui.centered(this,I18n.t(this,TITLES[page]),25,Ui.ink(this),true);LinearLayout.LayoutParams tp=Ui.lp(this,-1,-2);tp.topMargin=Ui.dp(this,24);stage.addView(title,tp);
        TextView body=Ui.centered(this,I18n.t(this,BODIES[page]),16,Ui.muted(this),false);body.setPadding(Ui.dp(this,8),Ui.dp(this,11),Ui.dp(this,8),Ui.dp(this,4));stage.addView(body,Ui.lp(this,-1,-2));
        TextView note=Ui.card(this,I18n.t(this,"medical_notice"),13,Ui.muted(this),false);LinearLayout.LayoutParams np=Ui.lp(this,-1,-2);np.topMargin=Ui.dp(this,24);stage.addView(note,np);
        TextView progress=Ui.centered(this,page==0?"●  ○  ○":page==1?"○  ●  ○":"○  ○  ●",15,Ui.RED,true);LinearLayout.LayoutParams pr=Ui.lp(this,-1,35);root.addView(progress,pr);
        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER_VERTICAL);
        Button skip=Ui.button(this,I18n.t(this,"skip"),false);skip.setOnClickListener(v->finishOnboarding());actions.addView(skip,new LinearLayout.LayoutParams(0,Ui.dp(this,50),1));
        Button next=Ui.button(this,I18n.t(this,page==2?"start":"next"),true);next.setOnClickListener(v->{if(page<2){page++;render();}else finishOnboarding();});LinearLayout.LayoutParams nlp=new LinearLayout.LayoutParams(0,Ui.dp(this,50),1);nlp.leftMargin=Ui.dp(this,10);actions.addView(next,nlp);root.addView(actions);
        AppPreferences.applyDirection(this,root);setContentView(Ui.shell(this,Ui.scroll(this,root)));
    }
    private void finishOnboarding(){AppPreferences.setOnboardingComplete(this);startActivity(new Intent(this,MainActivity.class));finish();}
}
