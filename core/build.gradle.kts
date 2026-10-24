plugins {
    id("java")
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