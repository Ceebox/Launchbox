plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.chadderbox.launchbox"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.chadderbox.launchbox"
        minSdk = 34
        targetSdk = 36
        versionCode = 1

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

    defaultConfig {
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

tasks.register<Copy>("renameApk") {
    val apkDir = layout.buildDirectory.dir("outputs/apk/release").get().asFile
    val apkNameOld = "app-release.apk"
    val apkNameNew = "Launchbox.apk"

    from(apkDir.resolve(apkNameOld))
    into(apkDir)
    rename { apkNameNew }
}

tasks.named("build") {
    finalizedBy("renameApk")
}