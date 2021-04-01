/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/6.8.3/userguide/multi_project_builds.html
 */

rootProject.name = "example1"

include("core", "read-model")

include(":apps:commands-handler")
include(":apps:events-projector")
include(":apps:events-publisher")

project(":apps:commands-handler").projectDir = file("apps/commands-handler")
project(":apps:events-projector").projectDir = file("apps/events-projector")
project(":apps:events-publisher").projectDir = file("apps/events-publisher")

