package com.firstaidhero.app.data;

import android.content.Context;  // سياق التطبيق للوصول إلى الموارد والملفات وقواعد البيانات
import android.graphics.Bitmap;  // صورة محمّلة في الذاكرة
import android.graphics.BitmapFactory;  // تحميل الصور من الملفات مع إمكانية تصغيرها
import android.graphics.ImageDecoder;  // فك ترميز الصور مع تصحيح اتجاهها وتصغيرها (أندرويد 9 فما فوق)
import android.net.Uri;  // عنوان لمورد مثل صورة أو جهة اتصال أو رقم هاتف (tel:)
import java.io.File;  // التعامل مع الملفات والمجلدات
import java.io.FileOutputStream;  // الكتابة إلى ملف
import java.io.IOException;  // خطأ في القراءة أو الكتابة
import java.io.OutputStream;  // الكتابة إلى مصدر بيانات

/**
 * حفظ صور الحالات المعالجة: تُنسخ الصورة إلى مجلد خاص بالتطبيق (files/photos) بعد تصغيرها،
 * ويُخزَّن اسم الملف فقط في قاعدة البيانات، فتبقى الصورة متاحة دون إنترنت حتى لو حُذفت من المعرض.
 */
public final class PhotoStore {
    /** أقصى طول لأطول ضلع في الصورة المحفوظة، وجودة ضغط JPEG. */
    private static final int MAX_EDGE_PX = 1280;
    private static final int JPEG_QUALITY = 85;

    /** مُنشئ خاص يمنع إنشاء كائن من الفئة. */
    private PhotoStore() { }

    /** مسار مجلد الصور الخاص بالتطبيق. */
    private static File dir(Context context) { return new File(context.getFilesDir(), "photos"); }

    /**
     * تفك ترميز الصورة المختارة (مع تصحيح اتجاهها حسب EXIF)، وتصغّرها إلى 1280 بكسل كحد أقصى،
     * ثم تحفظها بصيغة JPEG باسم فريد وتعيد اسم الملف. عند الفشل تحذف الملف الناقص وترمي استثناء.
     */
    public static String importPhoto(Context context, Uri source) throws IOException {
        Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.getContentResolver(), source), (decoder, info, src) -> {
            int w = info.getSize().getWidth(), h = info.getSize().getHeight();
            float scale = Math.min(1f, MAX_EDGE_PX / (float) Math.max(w, h));
            decoder.setTargetSize(Math.max(1, Math.round(w * scale)), Math.max(1, Math.round(h * scale)));
            decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
        });
        File folder = dir(context);
        if (!folder.exists() && !folder.mkdirs()) throw new IOException("Cannot create " + folder);
        String name = "case_" + System.currentTimeMillis() + ".jpg";
        File target = new File(folder, name);
        try (OutputStream out = new FileOutputStream(target)) {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) throw new IOException("JPEG encoding failed");
        } catch (IOException e) {
            target.delete();
            throw e;
        } finally {
            bitmap.recycle();
        }
        return name;
    }

    /**
     * تحمّل الصورة بالحجم المطلوب فقط لتوفير الذاكرة (مصغّرة للقائمة أو كبيرة للعرض).
     * تعيد null إذا كان الملف مفقوداً أو تالفاً، وتدعم روابط content:// القديمة (قبل الإصدار 5).
     */
    public static Bitmap load(Context context, String name, int minEdgePx) {
        if (name == null || name.isEmpty()) return null;
        try {
            if (name.startsWith("content:")) {
                return ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.getContentResolver(), Uri.parse(name)), (decoder, info, src) -> {
                    int edge = Math.max(info.getSize().getWidth(), info.getSize().getHeight());
                    decoder.setTargetSampleSize(Math.max(1, edge / Math.max(1, minEdgePx)));
                });
            }
            File file = new File(dir(context), name);
            if (!file.exists()) return null;
            BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getPath(), bounds);
            int edge = Math.max(bounds.outWidth, bounds.outHeight), sample = 1;
            while (edge / (sample * 2) >= minEdgePx) sample *= 2;
            BitmapFactory.Options opts = new BitmapFactory.Options(); opts.inSampleSize = sample;
            return BitmapFactory.decodeFile(file.getPath(), opts);
        } catch (IOException | SecurityException | IllegalArgumentException e) {
            return null;
        }
    }

    /** تحذف ملف الصورة عند حذف السجل أو استبدال صورته؛ روابط content:// القديمة لا تُحذف لأن التطبيق لا يملكها. */
    public static void delete(Context context, String name) {
        if (name == null || name.isEmpty() || name.startsWith("content:")) return;
        File file = new File(dir(context), name);
        if (file.exists()) file.delete();
    }
}
