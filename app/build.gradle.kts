plugins {
    alias(libs.plugins.android.application)
}

android { //rename package:https://stackoverflow.com/questions/16804093/rename-package-in-android-studio
    namespace = "com.NPTUMisStone.gym_app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.NPTUMisStone.gym_app"
        minSdk = 30
        targetSdk = 34
        versionCode = 60
        versionName = "0.60"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // UI Libraries
    implementation(libs.car.ui.lib)
    implementation(libs.shimmer)                        // 等待效果
    implementation(libs.activity)
    implementation(libs.material)
    implementation(libs.appcompat)
    implementation(libs.calendarview)                   // 自定義日曆元件：https://blog.csdn.net/coffee_shop/article/details/130709029
    implementation(libs.circleimageview)                // CircleImageView：https://www.geeksforgeeks.org/how-to-create-a-circularimageview-in-android-using-hdodenhof-library/
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)             // 下拉式更新：https://mnya.tw/cc/word/1520.html
    implementation(libs.fragment)                       // 分頁效果

    // Navigation Libraries
    implementation(libs.navigation.compose)             // Jetpack Compose integration
    implementation(libs.navigation.fragment)            // Views/Fragments integration
    implementation(libs.navigation.ui)
    implementation(libs.navigation.dynamic.features.fragment)
    implementation(libs.legacy.support.v4)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.ads) // Feature module support for Fragments
    implementation(libs.lifecycle.process)

    androidTestImplementation(libs.navigation.testing)  // Testing Navigation

    // Networking Libraries
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Google Play Services
    implementation(libs.play.services.base)

    // Java Mail API
    implementation(files("libs/mail.jar"))              // 使用Java Mail API 詳解：https://blog.csdn.net/weixin_42789969/article/details/13668166
    implementation(files("libs/activation.jar"))        // 使用Java Mail API 發送郵件：https://www.youtube.com/watch?v=RahBCY5BfS0&ab_channel=MusfickJamil
    implementation(files("libs/additional.jar"))        // javamail-android：https://code.google.com/archive/p/javamail-android/downloads

    // JDBC for SQL Server
    implementation(files("libs/jtds-1.3.3.jar"))        // 使用JDBC連接SQL Server

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}