plugins {
    kotlin("jvm") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
    application
}

group = "com.oli"
version = "0.0.1"

application {
    mainClass.set("com.oli.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    // RabbitMQ
    implementation("com.rabbitmq:amqp-client:5.16.0")
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.5.0")
    // Logback as logging backend
    implementation("ch.qos.logback:logback-classic:1.2.11")

    testImplementation(kotlin("test"))
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