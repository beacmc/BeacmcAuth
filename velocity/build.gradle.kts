plugins {
    id("java")
    id("maven-publish")
}

dependencies {
    compileOnly(libs.velocity)
    compileOnly(libs.bungeecordChat)
    implementation(project(":api"))
    implementation(project(":core"))

    implementation(libs.bstatsVelocity)
    implementation(libs.libbyVelocity)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }

        repositories {
            maven {
                name = "GitHubPackages"

                url = uri("https://maven.pkg.github.com/beacmc/BeacmcAuth")

                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                        ?: providers.gradleProperty("gpr.user").orNull
                    password = System.getenv("GITHUB_TOKEN")
                        ?: providers.gradleProperty("gpr.key").orNull
                }
            }
        }
    }
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        filesMatching("velocity-plugin.json") {
            expand(props)
        }
    }
}