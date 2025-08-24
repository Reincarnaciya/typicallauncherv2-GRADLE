plugins {
    id("java")
}

group = "space.typro.packetmanager"  // Уникальное имя группы
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":DirectoryManager"))

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("com.google.code.gson:gson:2.13.1")

    implementation("org.slf4j:slf4j-api:2.0.9")

    implementation("ch.qos.logback:logback-classic:1.4.11")
}

java {
    modularity.inferModulePath = true
}

tasks.test {
    useJUnitPlatform()
}