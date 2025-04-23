plugins {
    id("java")
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.gabriaum"
version = "1.0-SNAPSHOT"

val directory = "C://API"
val lombok = "org.projectlombok:lombok:1.18.36"

repositories {
    mavenCentral()

    maven("https://jitpack.io")
}

dependencies {

    compileOnly(files("$directory/yunity.jar"))
    compileOnly(lombok)

    annotationProcessor(lombok)
}

bukkit {
    name = "ClimatePool"
    main = "com.gabriaum.change.ChangeMain"
    version = project.version.toString()
    apiVersion = "1.8.8"
    author = "gabriaum"
    description = "A simple plugin to change the weather in Minecraft."
}

tasks.shadowJar {

    archiveFileName.set("ClimatePool.jar")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}