plugins {
    id("java")
    id("maven-publish")
}

dependencies {
    implementation(project(":api"))
    implementation(libs.configlib)
    implementation(libs.bcrypt)
    implementation(libs.argon2)
    implementation(libs.libbyCore)
    implementation(libs.nbs4j) {
        exclude("org.jetbrains", "annotations")
    }
    implementation(libs.ormlite)
    implementation(libs.hicaricp){
        exclude("org.slf4j", "slf4j-api")
    }
    compileOnly(libs.jda)
    compileOnly(libs.telegramBotApi)
    compileOnly(libs.packetevents)
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