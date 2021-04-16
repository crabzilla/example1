plugins {
     kotlin("plugin.serialization")
}

val crabzillaVersion = project.properties["crabzillaVersion"]
val kotlinSerializationVersion = project.properties["kotlinSerializationVersion"]

dependencies {
     implementation("io.github.crabzilla:crabzilla-core:$crabzillaVersion")
     implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
     testImplementation(platform("org.junit:junit-bom:5.7.1"))
     testImplementation("org.junit.jupiter:junit-jupiter")
     testImplementation("org.junit.jupiter:junit-jupiter-engine")
     testImplementation("org.assertj:assertj-core:3.12.2")
     testImplementation("io.kotest:kotest-runner-junit5:4.4.3")
}

tasks.withType<Test> {
     useJUnitPlatform()
}

