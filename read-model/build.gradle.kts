
plugins {
    id("nu.studer.jooq") version "5.2"
    id("java")
}

dependencies {
    jooqGenerator("org.postgresql:postgresql:42.2.14")
}

buildscript {
    configurations["classpath"].resolutionStrategy.eachDependency {
        if (requested.group == "org.jooq") {
            useVersion("3.14.8")
        }
    }
}

// tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") { allInputsDeclared.set(true) }

jooq {
    version.set("3.14.8")  // default (can be omitted)
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)  // default (can be omitted)

    configurations {
        create("main") {  // name of the jOOQ configuration
            generateSchemaSourceOnCompilation.set(true)  // default (can be omitted)

            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/example1_read"
                    user = "user1"
                    password = "pwd1"
                    // properties.add(Property().withKey("ssl").withValue("true"))
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
//                        forcedTypes.addAll(arrayOf(
//                            ForcedType()
//                                .withName("varchar")
//                                .withIncludeExpression(".*")
//                                .withIncludeTypes("JSONB?"),
//                            ForcedType()
//                                .withName("varchar")
//                                .withIncludeExpression(".*")
//                                .withIncludeTypes("INET")
//                        ).toList())
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.example1.jooq"
                        // directory = "build/generated-src/jooq/main"  // default (can be omitted)
                         directory = "build/src/main/java"  // default (can be omitted)
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}