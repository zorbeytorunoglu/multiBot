plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "com.zorbeytorunoglu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.21")
    implementation("net.dv8tion:JDA:5.0.0-beta.10")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("de.sciss:jump3r:1.0.5")
    implementation("ch.qos.logback:logback-classic:1.2.8")
    implementation("org.jsoup:jsoup:1.16.1")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.zorbeytorunoglu.multiBot.MainKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("com.zorbeytorunoglu.multiBot.MainKt")
}