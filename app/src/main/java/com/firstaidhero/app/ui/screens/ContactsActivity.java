package com.firstaidhero.app.ui.screens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.firstaidhero.app.core.AppPreferences;
import com.firstaidhero.app.core.I18n;
import com.firstaidhero.app.R;
import com.firstaidhero.app.data.DatabaseHelper;
import com.firstaidhero.app.data.DeviceContacts;
import com.firstaidhero.app.ui.Ui;
import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends BaseActivity {
    private DatabaseHelper db;
    private LinearLayout emergencyList;
    private LinearLayout userList;
    private EditText pendingName;
    private EditText pendingPhone;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> contactPickerLauncher;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        db = new DatabaseHelper(this);
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) showDeviceContactPicker();
            else openSystemContactPicker();
        });
        contactPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                fillFromUri(result.getData().getData());
            }
        });
        render();
    }
    @Override protected void onResume() { super.onResume(); if (db != null && emergencyList != null) load(); }

    private void render() {
        LinearLayout content = Ui.column(this);
        Ui.addSectionHeader(this, content, I18n.t(this, "contacts"), I18n.t(this, "contacts_sub"));

        LinearLayout emergencyCard = new LinearLayout(this);
        emergencyCard.setOrientation(LinearLayout.VERTICAL);
        emergencyCard.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Ui.RED, Ui.RED_DARK});
        bg.setCornerRadius(Ui.dp(this, 22));
        emergencyCard.setBackground(bg);
        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        ImageView mark = icon(R.drawable.ic_nav_contacts, Ui.WHITE, 0x33FFFFFF, 8, 40, I18n.t(this, "emergency_numbers"));
        head.addView(mark, Ui.lp(this, 40, 40));
        TextView emTitle = Ui.text(this, I18n.t(this, "emergency_numbers"), 15, Ui.WHITE, true);
        LinearLayout.LayoutParams etp = Ui.lp(this, -1, -2); etp.leftMargin = Ui.dp(this, 12); head.addView(emTitle, etp);
        emergencyCard.addView(head);
        emergencyList = new LinearLayout(this);
        emergencyList.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams elp = Ui.lp(this, -1, -2); elp.topMargin = Ui.dp(this, 14); emergencyCard.addView(emergencyList, elp);
        LinearLayout.LayoutParams ecp = Ui.lp(this, -1, -2); ecp.topMargin = Ui.dp(this, 18); content.addView(emergencyCard, ecp);

        Button add = Ui.button(this, "+   " + I18n.t(this, "add"), true);
        LinearLayout.LayoutParams ap = Ui.lp(this, -1, 52); ap.topMargin = Ui.dp(this, 14); content.addView(add, ap);
        add.setOnClickListener(v -> edit(-1, "", ""));

        Ui.addLabel(this, content, I18n.t(this, "my_contacts"), 17, Ui.ink(this), true, 24);
        userList = new LinearLayout(this);
        userList.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ulp = Ui.lp(this, -1, -2); ulp.topMargin = Ui.dp(this, 12); content.addView(userList, ulp);

        AppPreferences.applyDirection(this, content);
        setContentView(Ui.screen(this, Ui.scroll(this, content), "contacts"));
        load();
    }

    private void load() {
        emergencyList.removeAllViews();
        userList.removeAllViews();
        Cursor c = db.getContacts();
        int presetCount = 0;
        int userCount = 0;
        try {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                String name = I18n.t(this, c.getString(1));
                String phone = c.getString(2);
                boolean preset = c.getInt(3) == 1;
                if (preset) {
                    if (presetCount > 0) emergencyList.addView(hairline(0x33FFFFFF));
                    emergencyList.addView(emergencyRow(name, phone));
                    presetCount++;
                } else {
                    LinearLayout.LayoutParams lp = Ui.lp(this, -1, -2);
                    lp.topMargin = Ui.dp(this, 10);
                    userList.addView(userCard(id, name, phone), lp);
                    userCount++;
                }
            }
        } finally { c.close(); }
        if (userCount == 0) {
            TextView empty = Ui.text(this, I18n.t(this, "empty_personal"), 14, Ui.muted(this), false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(Ui.dp(this, 16), Ui.dp(this, 26), Ui.dp(this, 16), Ui.dp(this, 26));
            empty.setBackground(Ui.rounded(this, Ui.surface(this), Ui.border(this), 18, 1));
            userList.addView(empty, Ui.lp(this, -1, -2));
        }
    }

    private View hairline(int color) {
        View v = new View(this);
        v.setBackgroundColor(color);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, 1));
        return v;
    }

    private LinearLayout emergencyRow(String name, String phone) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(Ui.dp(this, 2), Ui.dp(this, 10), Ui.dp(this, 2), Ui.dp(this, 10));
        ImageView icon = icon(R.drawable.ic_nav_contacts, Ui.WHITE, 0x33FFFFFF, 8, 40, name);
        row.addView(icon, Ui.lp(this, 40, 40));
        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(0, -2, 1);
        wp.leftMargin = Ui.dp(this, 12);
        row.addView(words, wp);
        words.addView(Ui.text(this, name, 15, Ui.WHITE, true));
        TextView num = Ui.text(this, phone, 13, 0xFFFFE7E8, false);
        LinearLayout.LayoutParams np = Ui.lp(this, -1, -2); np.topMargin = Ui.dp(this, 2); words.addView(num, np);
        TextView chevron = Ui.centered(this, AppPreferences.isArabic(this) ? "‹" : "›", 22, 0xFFF3C2C5, false);
        row.addView(chevron, Ui.lp(this, 24, 36));
        row.setContentDescription(name + " · " + phone);
        row.setClickable(true);
        row.setFocusable(true);
        row.setForeground(getDrawable(android.R.drawable.list_selector_background));
        row.setOnClickListener(v -> dial(phone));
        return row;
    }

    private LinearLayout userCard(long id, String name, String phone) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        card.setBackground(Ui.rounded(this, Ui.surface(this), Ui.border(this), 18, 1));
        LinearLayout info = new LinearLayout(this);
        info.setGravity(Gravity.CENTER_VERTICAL);
        TextView avatar = Ui.centered(this, name.isEmpty() ? "•" : name.substring(0, 1).toUpperCase(), 18, Ui.RED, true);
        avatar.setBackground(Ui.rounded(this, Ui.softAccent(this), Ui.softAccent(this), 23, 0));
        info.addView(avatar, Ui.lp(this, 46, 46));
        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(0, -2, 1);
        labelLp.leftMargin = Ui.dp(this, 12);
        info.addView(labels, labelLp);
        labels.addView(Ui.text(this, name, 16, Ui.ink(this), true));
        TextView num = Ui.text(this, phone, 13, Ui.muted(this), false);
        LinearLayout.LayoutParams np = Ui.lp(this, -1, -2); np.topMargin = Ui.dp(this, 3); labels.addView(num, np);
        card.addView(info);
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END);
        LinearLayout.LayoutParams al = Ui.lp(this, -1, 40); al.topMargin = Ui.dp(this, 12); card.addView(actions, al);
        actions.addView(actionButton(R.drawable.ic_nav_contacts, Ui.WHITE, Ui.RED, 8, 40, I18n.t(this, "call"), v -> dial(phone)));
        ImageView editBtn = actionButton(R.drawable.ic_edit, Ui.RED, Ui.softAccent(this), 8, 40, I18n.t(this, "edit"), v -> edit(id, name, phone));
        ImageView deleteBtn = actionButton(R.drawable.ic_delete, Ui.RED, Ui.softAccent(this), 8, 40, I18n.t(this, "delete"), v -> confirmDelete(id, name));
        LinearLayout.LayoutParams em = Ui.lp(this, 40, 40); em.leftMargin = Ui.dp(this, 8); actions.addView(editBtn, em);
        LinearLayout.LayoutParams dm = Ui.lp(this, 40, 40); dm.leftMargin = Ui.dp(this, 8); actions.addView(deleteBtn, dm);
        return card;
    }

    private ImageView actionButton(int drawable, int tint, int bg, int innerDp, int sizeDp, String description, View.OnClickListener listener) {
        ImageView v = icon(drawable, tint, bg, innerDp, sizeDp, description);
        v.setOnClickListener(listener);
        return v;
    }

    private ImageView icon(int drawable, int tint, int bg, int innerDp, int sizeDp, String description) {
        ImageView v = new ImageView(this);
        v.setImageResource(drawable);
        v.setColorFilter(tint);
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        v.setPadding(Ui.dp(this, innerDp), Ui.dp(this, innerDp), Ui.dp(this, innerDp), Ui.dp(this, innerDp));
        v.setBackground(Ui.rounded(this, bg, bg, sizeDp / 2, 0));
        v.setContentDescription(description);
        v.setClickable(true);
        v.setFocusable(true);
        v.setForeground(getDrawable(android.R.drawable.list_selector_background));
        return v;
    }

    private void dial(String phone) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(phone))));
    }

    private void confirmDelete(long id, String name) {
        new AlertDialog.Builder(this)
                .setMessage(I18n.t(this, "delete") + " " + name + "?")
                .setNegativeButton(I18n.t(this, "cancel"), null)
                .setPositiveButton(I18n.t(this, "delete"), (d, w) -> { db.deleteContact(id); load(); })
                .show();
    }

    private void edit(long id, String oldName, String oldPhone) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(Ui.dp(this, 20), Ui.dp(this, 4), Ui.dp(this, 20), 0);
        EditText name = Ui.input(this, I18n.t(this, "name"), false);
        name.setText(oldName);
        form.addView(name);
        EditText phone = Ui.input(this, I18n.t(this, "phone"), false);
        phone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        LinearLayout.LayoutParams pp = Ui.lp(this, -1, -2);
        pp.topMargin = Ui.dp(this, 10);
        form.addView(phone, pp);
        phone.setText(oldPhone);
        Button choose = Ui.button(this, I18n.t(this, "choose_from_contacts"), false);
        LinearLayout.LayoutParams cp = Ui.lp(this, -1, 46);
        cp.topMargin = Ui.dp(this, 12);
        form.addView(choose, cp);
        choose.setOnClickListener(v -> chooseFromDeviceContacts());
        pendingName = name;
        pendingPhone = phone;
        new AlertDialog.Builder(this)
                .setTitle(id < 0 ? I18n.t(this, "add") : I18n.t(this, "edit"))
                .setView(form)
                .setNegativeButton(I18n.t(this, "cancel"), null)
                .setPositiveButton(I18n.t(this, "save"), (d, w) -> {
                    String n = name.getText().toString().trim();
                    String p = phone.getText().toString().trim();
                    if (n.isEmpty() || p.replaceAll("[+0-9() .-]", "").length() != 0 || p.replaceAll("\\D", "").length() < 3) {
                        new AlertDialog.Builder(this).setMessage(I18n.t(this, "required")).setPositiveButton("OK", null).show();
                        return;
                    }
                    db.saveContact(id, n, p);
                    load();
                })
                .show();
    }

    private void chooseFromDeviceContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            showDeviceContactPicker();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            new AlertDialog.Builder(this)
                    .setTitle(I18n.t(this, "contacts_permission_title"))
                    .setMessage(I18n.t(this, "contacts_permission_body"))
                    .setNegativeButton(I18n.t(this, "cancel"), null)
                    .setPositiveButton("OK", (d, w) -> permissionLauncher.launch(Manifest.permission.READ_CONTACTS))
                    .show();
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private void openSystemContactPicker() {
        Toast.makeText(this, I18n.t(this, "contacts_denied"), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        try { contactPickerLauncher.launch(intent); } catch (Exception e) { }
    }

    private void showDeviceContactPicker() {
        Cursor c = DeviceContacts.query(this);
        if (c == null) { openSystemContactPicker(); return; }
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        List<String[]> picked = new ArrayList<>();
        try {
            while (c.moveToNext() && picked.size() < 100) {
                String name = c.getString(DeviceContacts.COL_NAME);
                String number = c.getString(DeviceContacts.COL_NUMBER);
                if (name == null || number == null || name.trim().isEmpty()) continue;
                rows.addView(deviceContactRow(name, number));
                picked.add(new String[]{name, number});
            }
        } finally { c.close(); }
        if (picked.isEmpty()) {
            new AlertDialog.Builder(this).setMessage(I18n.t(this, "no_device_contacts")).setPositiveButton("OK", null).show();
            return;
        }
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.addView(rows);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(I18n.t(this, "choose_contact"))
                .setView(scroll)
                .setNegativeButton(I18n.t(this, "cancel"), null)
                .create();
        for (int i = 0; i < rows.getChildCount(); i++) {
            final String[] pair = picked.get(i);
            View row = rows.getChildAt(i);
            row.setOnClickListener(v -> { applyPicked(pair[0], pair[1]); dialog.dismiss(); });
        }
        dialog.show();
    }

    private LinearLayout deviceContactRow(String name, String number) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(Ui.dp(this, 6), Ui.dp(this, 8), Ui.dp(this, 6), Ui.dp(this, 8));
        TextView avatar = Ui.centered(this, name.isEmpty() ? "•" : name.substring(0, 1).toUpperCase(), 15, Ui.RED, true);
        avatar.setBackground(Ui.rounded(this, Ui.softAccent(this), Ui.softAccent(this), 18, 0));
        row.addView(avatar, Ui.lp(this, 36, 36));
        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(0, -2, 1);
        ll.leftMargin = Ui.dp(this, 12);
        row.addView(labels, ll);
        labels.addView(Ui.text(this, name, 15, Ui.ink(this), true));
        TextView num = Ui.text(this, number, 13, Ui.muted(this), false);
        LinearLayout.LayoutParams np = Ui.lp(this, -1, -2); np.topMargin = Ui.dp(this, 2); labels.addView(num, np);
        row.setContentDescription(name + " · " + number);
        row.setClickable(true);
        row.setFocusable(true);
        row.setForeground(getDrawable(android.R.drawable.list_selector_background));
        return row;
    }

    private void fillFromUri(Uri uri) {
        Cursor c = null;
        try { c = DeviceContacts.lookup(this, uri); } catch (SecurityException e) { c = null; }
        if (c != null) {
            try {
                if (c.moveToFirst()) applyPicked(c.getString(DeviceContacts.COL_NAME), c.getString(DeviceContacts.COL_NUMBER));
            } finally { c.close(); }
        }
    }

    private void applyPicked(String name, String phone) {
        if (pendingName != null) pendingName.setText(name == null ? "" : name);
        if (pendingPhone != null) pendingPhone.setText(phone == null ? "" : phone);
    }

    @Override protected void onDestroy() { if (db != null) db.close(); super.onDestroy(); }
}
