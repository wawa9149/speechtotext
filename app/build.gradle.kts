import java.util.Properties

plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.stt_app"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.stt_app"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        buildFeatures {
//            buildConfig = true
//        }
//        buildTypes {
//            debug {
//                val localProperties = Properties()
//                localProperties.load(project.rootProject.file("local.properties").inputStream())
//                buildConfigField("String", "API_KEY", "\"${localProperties.getProperty("API_KEY")}\"")
//            }
//            release {
//                val localProperties = Properties()
//                localProperties.load(project.rootProject.file("local.properties").inputStream())
//                buildConfigField("String", "API_KEY", "\"${localProperties.getProperty("API_KEY")}\"")
//            }
//        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        pickFirst("META-INF/INDEX.LIST")
        pickFirst("META-INF/DEPENDENCIES")
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.google.cloud:google-cloud-speech:4.18.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation("com.google.cloud:google-cloud-core:2.7.1")
    implementation("com.google.api-client:google-api-client:1.34.1")
    implementation("com.google.api:gax:2.32.0")
    implementation(platform("com.google.cloud:libraries-bom:25.4.0"))
    implementation("com.google.cloud:google-cloud-storage:2.7.2")
    constraints {
        implementation("com.google.guava:guava:31.1-android")
    }
    implementation("com.squareup.okhttp3:okhttp:3.10.0")
    implementation("io.grpc:grpc-stub:1.56.1")
    implementation("io.grpc:grpc-okhttp:1.46.0") // gRPC OkHttp 프로바이더

}
