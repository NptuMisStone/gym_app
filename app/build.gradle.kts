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
        versionCode = 1115
        versionName = "1.115"
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
    implementation(libs.android.car.ui)                          // 汽車UI庫
    implementation(libs.android.material)                        // 材料設計元件
    implementation(libs.androidx.activity)                       // 活動庫
    implementation(libs.androidx.appcompat)                      // AppCompat庫
    implementation(libs.androidx.constraintlayout)               // ConstraintLayout庫
    implementation(libs.androidx.fragment)                       // 分頁效果
    implementation(libs.androidx.swiperefreshlayout)             // 下拉式更新
    implementation(libs.facebook.shimmer)                        // 等待效果
    implementation(libs.github.calendarview)                     // 自定義日曆元件
    implementation(libs.github.scrolldatepicker)                 // 可左右拉的週曆
    implementation(libs.hdodenhof.circleimageview)               // 圓形圖像視圖
    implementation(libs.zhanghai.materialRatingBar)              // 評分欄材料


    // Navigation Libraries
    implementation(libs.android.play.services.ads)               // 廣告服務
    implementation(libs.androidx.legacy.support.v4)              // 遺留支持庫
    implementation(libs.androidx.lifecycle.livedata.ktx)         // LiveData KTX
    implementation(libs.androidx.lifecycle.process)              // 生命週期處理
    implementation(libs.androidx.lifecycle.viewmodel.ktx)        // ViewModel KTX
    implementation(libs.androidx.navigation.compose)             // Jetpack Compose整合
    implementation(libs.androidx.navigation.dynamic.features.fragment) // 動態功能片段
    implementation(libs.androidx.navigation.fragment)            // 視圖/片段整合
    implementation(libs.androidx.navigation.ui)                  // 導航UI
    androidTestImplementation(libs.androidx.navigation.testing)  // 測試導航

    // Networking Libraries
    implementation(libs.okhttp)                                  // OkHttp庫
    implementation(libs.retrofit2.converter.gson)                // Retrofit Gson轉換器
    implementation(libs.retrofit2.retrofit)                      // Retrofit庫

    // Google Play Services
    implementation(libs.android.maps.utils)                      // 地圖工具
    implementation(libs.android.places)                          // 地點庫
    implementation(libs.android.play.services.base)              // 基本服務
    implementation(libs.android.play.services.maps)              // 地圖顯示與地圖導航

    // Java Mail API
    implementation(files("libs/activation.jar"))                 // 使用Java Mail API發送郵件
    implementation(files("libs/additional.jar"))                 // javamail-android
    implementation(files("libs/mail.jar"))                       // 使用Java Mail API

    // JDBC for SQL Server
    implementation(files("libs/jtds-1.3.3.jar"))                 // 使用JDBC連接SQL Server

    // Testing Libraries
    androidTestImplementation(libs.androidx.espresso.core)       // Espresso核心
    androidTestImplementation(libs.androidx.ext.junit)           // AndroidX JUnit擴展
    testImplementation(libs.junit)                               // JUnit庫

    // Mapbox Libraries
    // MapView專案
    implementation(libs.mapbox.maps.android)                     // Mapbox地圖Android
    // Mapbox-Maps：https://docs.mapbox.com/android/maps/guides/install/
    // Mapbox-Navigation：https://docs.mapbox.com/android/navigation/guides/installation/
    //(可參考)遇见BUG之 “Dependent features configured but no package ID was set”
    // ：https://blog.csdn.net/u014235093/article/details/109116602
    //(可參考)Android Studio 一个module引用另一个模块module的方法
    // ：https://blog.csdn.net/sinat_35958166/article/details/89468978
    //(可參考)Android Studio入门（8）— Module之间相互引用
    //：https://blog.csdn.net/synola/article/details/117474728
}