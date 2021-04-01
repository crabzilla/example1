plugins {
     kotlin("plugin.serialization")
}

val crabzillaVersion = project.properties["crabzillaVersion"]
val kotlinSerializationVersion = project.properties["kotlinSerializationVersion"]

dependencies {
     implementation("io.github.crabzilla:crabzilla-core:$crabzillaVersion")
     implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
}
