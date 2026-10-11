package com.firstaidhero.app.ui.screens;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.firstaidhero.app.core.AppPreferences;
import com.firstaidhero.app.core.I18n;
import com.firstaidhero.app.data.DatabaseHelper;
import com.firstaidhero.app.ui.Ui;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TreatedCasesActivity extends BaseActivity {
    private static final int PICK_PHOTO = 40;
    private DatabaseHelper db; private LinearLayout list; private EditText search; private String photoUri;
    @Override protected void onCreate(Bundle state) { super.onCreate(state); db = new DatabaseHelper(this); render(); }
    @Override protected void onResume() { super.onResume(); if (db != null && list != null) load(search == null ? "" : search.getText().toString()); }
    private void render() {
        LinearLayout content = Ui.column(this); Ui.addSectionHeader(this, content, I18n.t(this,"records"), I18n.t(this,"open_records"));
        search = Ui.input(this, I18n.t(this,"search_hint"), false); LinearLayout.LayoutParams sp = Ui.lp(this, -1, 52); sp.topMargin = Ui.dp(this, 16); content.addView(search, sp);
        Button add = Ui.button(this, "+   " + I18n.t(this,"open_records"), true); LinearLayout.LayoutParams ap = Ui.lp(this, -1, 50); ap.topMargin = Ui.dp(this, 10); content.addView(add, ap); add.setOnClickListener(v -> addRecord());
        list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams lp = Ui.lp(this, -1, -2); lp.topMargin = Ui.dp(this, 12); content.addView(list, lp);
        search.addTextChangedListener(new TextWatcher() { public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int before,int count){ load(s.toString()); } public void afterTextChanged(Editable e){} });
        AppPreferences.applyDirection(this, content); setContentView(Ui.screen(this, Ui.scroll(this, content), "records")); load("");
    }
    private EditText field(String hint, boolean multiline) { return Ui.input(this, hint, multiline); }
    private void addRecord() {
        photoUri = null; LinearLayout form = new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(Ui.dp(this, 18), Ui.dp(this, 4), Ui.dp(this, 18), 0);
        EditText desc = field(I18n.t(this,"description"), false); EditText date = field(I18n.t(this,"date"), false); date.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE); date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
        EditText procedures = field(I18n.t(this,"procedures"), true); Button photo = Ui.button(this, I18n.t(this,"photo"), false); photo.setOnClickListener(v -> pickPhoto());
        form.addView(desc); LinearLayout.LayoutParams dp = Ui.lp(this, -1, -2); dp.topMargin = Ui.dp(this, 9); form.addView(date, dp); LinearLayout.LayoutParams pp = Ui.lp(this, -1, -2); pp.topMargin = Ui.dp(this, 9); form.addView(procedures, pp); LinearLayout.LayoutParams ph = Ui.lp(this, -1, 46); ph.topMargin = Ui.dp(this, 10); form.addView(photo, ph);
        new AlertDialog.Builder(this).setTitle(I18n.t(this,"open_records")).setView(form).setNegativeButton(I18n.t(this,"cancel"), null).setPositiveButton(I18n.t(this,"save"), (d, w) -> {
            String ds = desc.getText().toString().trim(), dt = date.getText().toString().trim(), pr = procedures.getText().toString().trim();
            if (ds.isEmpty() || pr.isEmpty() || !dt.matches("\\d{4}-\\d{2}-\\d{2}")) { new AlertDialog.Builder(this).setMessage(I18n.t(this,"required")).setPositiveButton("OK", null).show(); return; }
            db.addTreatedCase(ds, dt, photoUri, pr); load(search.getText().toString());
        }).show();
    }
    private void pickPhoto() { Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); intent.setType("image/*"); intent.addCategory(Intent.CATEGORY_OPENABLE); intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); startActivityForResult(intent, PICK_PHOTO); }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) { super.onActivityResult(requestCode, resultCode, data); if (requestCode == PICK_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) { Uri uri = data.getData(); photoUri = uri.toString(); try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (SecurityException ignored) { } } }
    private void load(String query) {
        if (list == null) return; list.removeAllViews(); Cursor c = db.getTreatedCases(query); int found = 0;
        try { while (c.moveToNext()) {
            long id = c.getLong(0); String desc = c.getString(1), date = c.getString(2), procedures = c.getString(4);
            LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(Ui.dp(this, 15), Ui.dp(this, 14), Ui.dp(this, 15), Ui.dp(this, 14)); card.setBackground(Ui.rounded(this, Ui.surface(this), Ui.border(this), 18, 1));
            LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL); TextView icon = Ui.centered(this,"▤",17,Ui.RED,true); icon.setBackground(Ui.rounded(this,Ui.softAccent(this),Ui.softAccent(this),19,0)); header.addView(icon,Ui.lp(this,38,38));
            LinearLayout labels = new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(0,-2,1); llp.leftMargin=Ui.dp(this,11); header.addView(labels,llp); labels.addView(Ui.text(this,desc,15,Ui.ink(this),true)); TextView d=Ui.text(this,date,12,Ui.RED, true); LinearLayout.LayoutParams dlp=Ui.lp(this,-1,-2);dlp.topMargin=Ui.dp(this,3);labels.addView(d,dlp);card.addView(header);
            TextView note=Ui.text(this,procedures,14,Ui.muted(this),false); LinearLayout.LayoutParams nlp=Ui.lp(this,-1,-2);nlp.topMargin=Ui.dp(this,11);card.addView(note,nlp);
            Button del=Ui.button(this,I18n.t(this,"delete"),false);LinearLayout.LayoutParams blp=Ui.lp(this,-1,42);blp.topMargin=Ui.dp(this,11);card.addView(del,blp);del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage(I18n.t(this,"delete")+"?").setNegativeButton(I18n.t(this,"cancel"),null).setPositiveButton(I18n.t(this,"delete"),(x,w)->{db.deleteTreatedCase(id);load(search.getText().toString());}).show());
            LinearLayout.LayoutParams cp=Ui.lp(this,-1,-2);cp.topMargin=Ui.dp(this,9);list.addView(card,cp);found++;
        }} finally { c.close(); }
        if(found==0){TextView empty=Ui.card(this,"▤\n\n"+I18n.t(this,"empty_records"),14,Ui.muted(this),false);empty.setGravity(Gravity.CENTER);list.addView(empty,Ui.lp(this,-1,135));}
    }
    @Override protected void onDestroy(){if(db!=null)db.close();super.onDestroy();}
}
