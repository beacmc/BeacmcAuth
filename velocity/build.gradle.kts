plugins {
    id("java")
}

dependencies {
    compileOnly(libs.velocity)
    compileOnly(libs.bungeecordChat)
    implementation(project(":api"))
    implementation(project(":core"))

    implementation(libs.bstatsVelocity)
    implementation(libs.libbyVelocity)
}