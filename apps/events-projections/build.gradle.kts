plugins {
    id("io.micronaut.application") version "1.4.2"
    id("com.github.johnrengelman.shadow")
}

micronaut {
    runtime("netty")
    testRuntime("kotest")
    processing {
        incremental(true)
        annotations("com.example1.*")
    }
}

dependencies {
    implementation(project(":write-model"))
    implementation(project(":read-model"))
    implementation("io.micronaut.nats:micronaut-nats")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut:micronaut-management")

    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("io.vertx:vertx-cassandra-client")
    implementation("io.github.zero88:jooqx-core:1.0.0")
}

application {
    // Define the main class for the application.
    mainClass.set("com.example1.ApplicationKt")
}
