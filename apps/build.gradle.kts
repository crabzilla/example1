
val crabzillaVersion = project.properties["crabzillaVersion"]
val natsStreamingVersion = project.properties["natsStreamingVersion"]

subprojects {

    dependencies {
        implementation("io.nats:java-nats-streaming:$natsStreamingVersion")
        implementation("io.github.crabzilla:crabzilla-core:$crabzillaVersion")
        implementation("io.github.crabzilla:crabzilla-pg-client:$crabzillaVersion")
        implementation("io.vertx:vertx-core")
    }
}

