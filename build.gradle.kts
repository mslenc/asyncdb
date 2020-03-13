plugins {
    id("java-library")
    id("maven-publish")
}

group = "com.github.mslenc"
version = "1.3.1"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    // https://mvnrepository.com/artifact/io.netty/netty-all
    api("io.netty:netty-all:4.1.29.Final")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:1.7.25")

    testImplementation("junit:junit:4.12")

    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    testImplementation("ch.qos.logback:logback-classic:1.2.3")

    // https://mvnrepository.com/artifact/org.postgresql/postgresql/42.2.5
    testImplementation("org.postgresql:postgresql:42.2.5")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}