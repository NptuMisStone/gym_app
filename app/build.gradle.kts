plugins {
    alias(libs.plugins.android.application)
}

android { //rename package:https://stackoverflow.com/questions/16804093/rename-package-in-android-studio
    namespace = "com.NPTUMisStone.gym_app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.NPTUMisStone.gym_app"
        minSdk = 28
        targetSdk = 34
        versionCode = 69
        versionName = "0.69"
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
    implementation(libs.calendarview)                   // 自定義日曆元件
    implementation(libs.circleimageview)                // CircleImageView
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)             // 下拉式更新
    implementation(libs.fragment)                       // 分頁效果

    // Navigation Libraries
    implementation(libs.navigation.compose)             // Jetpack Compose integration
    implementation(libs.navigation.fragment)            // Views/Fragments integration
    implementation(libs.navigation.ui)
    implementation(libs.navigation.dynamic.features.fragment)
    implementation(libs.legacy.support.v4)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.play.services.ads)              // Feature module support for Fragments
    implementation(libs.lifecycle.process)

    androidTestImplementation(libs.navigation.testing)  // Testing Navigation

    // Networking Libraries
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Google Play Services
    implementation(libs.play.services.base)
    implementation(libs.play.services.maps)             // 地圖顯示與地圖導航

    // Mapbox-Maps：https://docs.mapbox.com/android/maps/guides/install/
    //implementation(libs.mapbox.navigation.ui)                     // 地圖顯示與地圖
    // implementation(libs.mapbox.maps.android)                   地圖顯示與地圖
    implementation(libs.mapbox.maps.android)
    // Mapbox-Navigation：https://docs.mapbox.com/android/navigation/guides/installation/
    //implementation (libs.mapbox.navigation.android)
    //implementation (libs.mapbox.navigationcore.navigation)
    //implementation (libs.mapbox.navigationcore.uicomponents)
    //implementation (libs.mapbox.navigationcore.uimaps)
    /*implementation (libs.mapbox.navigationcore.voice)
    implementation (libs.mapbox.navigationcore.android)*/
    //(可參考)遇见BUG之 “Dependent features configured but no package ID was set”
    // ：https://blog.csdn.net/u014235093/article/details/109116602
    //(可參考)Android Studio 一个module引用另一个模块module的方法
    // ：https://blog.csdn.net/sinat_35958166/article/details/89468978
    //(可參考)Android Studio入门（8）— Module之间相互引用
    //：https://blog.csdn.net/synola/article/details/117474728
    compileOnly (project(":navigation"))

    // Java Mail API
    implementation(files("libs/mail.jar"))              // 使用Java Mail API
    implementation(files("libs/activation.jar"))        // 使用Java Mail API 發送郵件
    implementation(files("libs/additional.jar"))        // javamail-android

    // JDBC for SQL Server
    implementation(files("libs/jtds-1.3.3.jar"))        // 使用JDBC連接SQL Server

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}