plugins {
    id("java")
    id("maven-publish")
}

dependencies {
    compileOnly(libs.bungeecord)
    implementation(project(":core"))
    implementation(project(":api"))

    implementation(libs.bstatsBungeecord)
    implementation(libs.libbyBungee)
    compileOnly(libs.nullcordxApi)
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        filesMatching("bungee.yml") {
            expand(props)
        }
    }
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