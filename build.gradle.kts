plugins {
    kotlin("jvm") version "1.8.0"
}

allprojects {
    group = "ru.meldren"
    version = "1.0"

    repositories {
        mavenCentral()
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
}