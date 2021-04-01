plugins {
    id("io.micronaut.application") version "1.4.2"
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
    implementation(project(":core"))
    implementation(project(":read-model"))
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut:micronaut-management")

    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("io.vertx:vertx-pg-client")
    implementation("io.github.zero88:jooqx-core:1.0.0")
}

application {
    // Define the main class for the application.
    mainClass.set("com.example1.ApplicationKt")
}
