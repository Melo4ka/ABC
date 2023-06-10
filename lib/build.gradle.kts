plugins {
    java
    kotlin("jvm") version "1.6.0"
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "ru.meldren"
            artifactId = "ABC"
            version = "1.0"

            from(components["java"])
        }
    }
}