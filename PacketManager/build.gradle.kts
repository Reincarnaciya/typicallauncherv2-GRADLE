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

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.0")

    implementation("org.slf4j:slf4j-api:2.0.9")

    implementation("ch.qos.logback:logback-classic:1.4.11")
}

java {
    modularity.inferModulePath = true
}

tasks.test {
    useJUnitPlatform()
}