plugins {
    alias(libs.plugins.android.application)
}

// إعدادات تطبيق أندرويد: اسم الحزمة، أدنى إصدار مدعوم (Android 11 / API 30)، والإصدار المستهدف.
android {
    namespace = "com.firstaidhero.app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.firstaidhero.app"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// المكتبات المستخدمة في التطبيق والاختبارات (أرقام الإصدارات في gradle/libs.versions.toml).
dependencies {
    // AndroidX Activity: طلب الصلاحيات واستقبال النتائج ومنتقي الصور الحديث (registerForActivityResult).
    implementation(libs.activity.ktx)
    // AppCompat: الشاشات المتوافقة، نوافذ الحوار AlertDialog، والوضع الليلي.
    implementation(libs.appcompat)
    // ConstraintLayout: من قالب Android Studio؛ الواجهات الحالية مبنية بـ LinearLayout.
    implementation(libs.constraintlayout)
    // Material Components: شريط التنقل السفلي BottomNavigationView.
    implementation(libs.material)
    // JUnit: اختبارات الوحدة على الحاسوب.
    testImplementation(libs.junit)
    // Espresso و AndroidX JUnit: اختبارات الواجهة على الجهاز أو المحاكي.
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}