pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.github.vertical-blank:sql-formatter:2.0.4")
    }
}

rootProject.name = "sql_wrapper_plugin"