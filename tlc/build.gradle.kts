plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "ca.edtoaster"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.ow2.asm:asm:9.6")
    implementation("com.github.ajalt.clikt:clikt:4.2.1")
    implementation("cc.ekblad.konbini:konbini:0.1.2")
//    implementation("io.arrow-kt:arrow-core:1.2.0")
//    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.0")
//    implementation("org.typemeta:funcj-parser:0.6.18")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}