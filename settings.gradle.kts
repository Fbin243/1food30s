pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        // Define your repositories here, including JitPack
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "app1food30s"
include(":app")
 