plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.apache.curator:curator-framework:5.6.0")
    implementation("org.apache.curator:curator-recipes:5.6.0")

    implementation("com.beust:jcommander:1.82")
}

tasks.test {
    useJUnitPlatform()
}