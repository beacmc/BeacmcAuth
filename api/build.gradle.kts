plugins {
    id("java")
    id("maven-publish")
}

dependencies {
    compileOnly(libs.configlib)
    compileOnly(libs.packetevents)
    compileOnly(libs.ormlite)
    compileOnly(libs.nbs4j)
    compileOnly(libs.bcrypt)
    compileOnly(libs.hicaricp)
    compileOnly(libs.libbyCore)
    compileOnly(libs.jda)
    compileOnly(libs.bungeecordChat)
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