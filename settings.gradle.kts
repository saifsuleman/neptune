rootProject.name = "neptune"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()

        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }
}