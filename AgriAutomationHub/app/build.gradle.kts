import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.agriautomationhub"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.agriautomationhub"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Expose API Keys from local.properties
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }

        buildConfigField("String", "OPENWEATHER_API_KEY", "\"${properties.getProperty("OPENWEATHER_API_KEY")}\"")
        buildConfigField("String", "AZURE_OPENAI_API_KEY", "\"${properties.getProperty("AZURE_OPENAI_API_KEY")}\"")
        buildConfigField("String", "CROPCARE_API_KEY", "\"${properties.getProperty("CROPCARE_API_KEY")}\"")
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"${properties.getProperty("GOOGLE_MAPS_API_KEY")}\"")

        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = properties.getProperty("GOOGLE_MAPS_API_KEY") ?: ""
    }

    buildFeatures {
        viewBinding = true
        mlModelBinding = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/ASL2.0",
                "META-INF/LICENSE",
                "META-INF/LICENSE.tx",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/ASL2.0"
            )
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.2.0")
// Firebase BoM (aligns versions automatically)
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))

// Add Firebase products (no version needed)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-database")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation ("com.google.maps.android:android-maps-utils:3.4.0")
    implementation ("com.google.android.libraries.places:places:3.4.0")


    implementation("com.google.android.gms:play-services-auth:19.2.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("org.apache.poi:poi:5.2.3")
    implementation ("org.apache.poi:poi-ooxml:5.2.3")
    implementation ("org.json:json:20210307")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("org.apache.poi:poi-ooxml:5.2.3")
    implementation ("org.apache.commons:commons-csv:1.10.0")
    implementation(libs.recyclerview)
    implementation(libs.play.services.maps)
//    implementation(libs.places.compat)
    testImplementation ("junit:junit:4.13.2")
//    implementation ("com.microsoft.sqlserver:mssql-jdbc:12.8.1.jre11")
    implementation(libs.play.services.fitness)
    implementation(libs.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(files("libs/jtds-1.3.1.jar"))
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.github.bumptech.glide:glide:4.13.0")
    implementation ("com.google.code.gson:gson:2.8.7")
    implementation ("androidx.work:work-runtime:2.7.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.0")
    implementation ("androidx.room:room-runtime:2.4.3")
    annotationProcessor ("androidx.room:room-compiler:2.4.3")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation("org.apache.poi:poi-ooxml:5.2.5")

    implementation ("com.github.CanHub:Android-Image-Cropper:4.3.2")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("org.jsoup:jsoup:1.17.2")

}