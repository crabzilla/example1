
val crabzillaVersion = project.properties["crabzillaVersion"]
val natsStreamingVersion = project.properties["natsStreamingVersion"]

subprojects {

    dependencies {
        implementation("io.nats:java-nats-streaming:$natsStreamingVersion")
        implementation("io.github.crabzilla:crabzilla-core:$crabzillaVersion")
        implementation("io.github.crabzilla:crabzilla-stack:$crabzillaVersion")
        implementation("io.github.crabzilla:crabzilla-pg-client:$crabzillaVersion")
        implementation("io.github.crabzilla:crabzilla-cassandra:$crabzillaVersion")
        implementation("io.vertx:vertx-core")
        implementation("io.vertx:vertx-circuit-breaker")
        implementation("io.vertx:vertx-pg-client")
        implementation("io.vertx:vertx-cassandra-client")
    }
}

