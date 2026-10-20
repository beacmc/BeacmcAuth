plugins {
    id("java")
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