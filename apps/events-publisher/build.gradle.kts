plugins {
    id("io.micronaut.application") version "1.4.2"
    id("com.github.johnrengelman.shadow")
    id("com.google.cloud.tools.jib") version "0.9.0"
}

dependencies {
    implementation("io.micronaut.nats:micronaut-nats")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut:micronaut-management")
    runtimeOnly("ch.qos.logback:logback-classic")
//    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation ("io.vertx:vertx-lang-kotlin")
//    implementation("io.vertx:vertx-lang-kotlin-coroutines")
}


micronaut {
    runtime("netty")
    testRuntime("kotest")
    processing {
        incremental(true)
        annotations("com.example1.*")
    }
}

application {
    // Define the main class for the application.
    mainClass.set("com.example1.ApplicationKt")
}
