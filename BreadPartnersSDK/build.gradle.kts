plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
    id("maven-publish")
}

android {
    namespace = "com.breadfinancial.breadpartners.sdk"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    publishing {
        singleVariant("release") {}
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.compose.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    coreLibraryDesugaring(libs.android.desugar.jdk.libs)
//    #Change-breadpartnersdk
    api(libs.kotlinx.serialization.json)
    api(libs.jsoup)
    api(libs.gson)
    api(libs.glide)
    annotationProcessor(libs.glide.compiler)
    api(libs.recaptcha)
    api(libs.coordinatorlayout)
    api(libs.constraintlayout)
    api(libs.androidx.multidex)
}

afterEvaluate {
    publishing {
        publications {
            // Create Maven Publication and name it.
            register<MavenPublication>("release"){
                // Component for release variant.
                from(components["release"])
                groupId = "com.github.getbread"
                artifactId = "breadfinancialbreadpartnerssdk"
                version = "0.0.1"

                pom {
                    name = "Bread Financial Brand Partners SDK"
                    description =
                        "The Bread Financial Brand Partners SDK allows our Brand Partners to integrate Apply and Buy functionality within their native app."
                    url = "https://github.com/getbread/breadfinancial-brand-partners-android.git"

                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }

                    developers {
                        developer {
                            id = "com.breadfinancial.breadpartners"
                            name = "Bread Financial"
                            email = "BreadFinancialMobileSupport@breadfinancial.com"
                        }
                    }

                    scm {
                        url =
                            "https://github.com/getbread/breadfinancial-brand-partners-android.git"
                    }
                }
            }
        }
    }
}

version = "0.0.1"