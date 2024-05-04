import java.net.URI
import javax.print.DocFlavor.URL

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.1")
    }
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.23-1.0.19" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://repo.platform.here.com/artifactory/gradle-premium")

//        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
//        flatDir {
//            dirs("libs")
//        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}