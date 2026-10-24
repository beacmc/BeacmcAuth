plugins {
    id("java")
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
    compileOnly(libs.jakartaMail)
    compileOnly(libs.bungeecordChat)
}