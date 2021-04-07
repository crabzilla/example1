plugins {
    application
//    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation("io.nats:jnats:2.10.0")
    implementation("io.vertx:vertx-config")
    implementation ("io.vertx:vertx-lang-kotlin")
    implementation("io.vertx:vertx-hazelcast")
    implementation("io.vertx:vertx-lang-kotlin-coroutines")
    implementation("ch.qos.logback:logback-classic:1.2.3")
}

application {
    mainClass.set("com.example1.ApplicationKt")
}

