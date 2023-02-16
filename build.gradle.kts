plugins {
    `java-library`
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("io.papermc.paperweight.userdev") version "1.3.8"
}

group = "net.saifs"
description = "neptune"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("cloud.commandframework:cloud-paper:1.8.0")
    implementation("cloud.commandframework:cloud-annotations:1.8.0")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.8.0")
    implementation("org.spongepowered:configurate-gson:4.1.2")

    api("com.zaxxer:HikariCP:5.0.0")
    api("net.wesjd:anvilgui:1.5.3-SNAPSHOT")

    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")


    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.19.3-R0.1-SNAPSHOT")
}

tasks {
    build {
        dependsOn("shadowJar")
    }
}