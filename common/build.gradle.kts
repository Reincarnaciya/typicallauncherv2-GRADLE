plugins {
    id("java")
}

group = "space.typro.common"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("org.slf4j:slf4j-api:2.0.9")
}

java {
    modularity.inferModulePath = true
}