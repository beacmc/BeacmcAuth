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