plugins {
    id("com.android.library")
    id("maven-publish")
}

// 读取 git 的 commit tag 作为应用的版本名，如果后面
val gitVersionTag by lazy {
    val stdout = org.apache.commons.io.output.ByteArrayOutputStream()
    rootProject.exec {
        val cmd = "git describe --tags".split(" ")
        commandLine(cmd)
        standardOutput = stdout
    }
    stdout.toString(Charsets.UTF_8).trim()
}


android {
    namespace = "com.huantansheng.easyphotos"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    buildFeatures {
        buildConfig = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.github.yalantis:ucrop:2.2.8")
    implementation("com.blankj:utilcodex:1.31.1")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.zhangls"
                artifactId = "EasyPhotos"
                version = gitVersionTag
            }
        }
    }
}
