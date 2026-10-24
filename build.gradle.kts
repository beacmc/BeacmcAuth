plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.6.1"
}

allprojects {
    group = "com.beacmc.beacmcauth"
    version = "3.1.0-BETA"

    repositories {
        mavenCentral()

        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
        maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://repo.shieldcommunity.net/repository/maven-public/") }
    }
}

dependencies {
    implementation(project(":bungeecord"))
    implementation(project(":velocity"))
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")

        val shadePath = "com.beacmc.beacmcauth.shaded."
        relocate("de.exlll.configlib", shadePath + "configlib")
        relocate("com.j256.ormlite", shadePath + "ormlite")
        relocate("org.snakeyaml", shadePath + "snakeyaml")
        relocate("org.bstats", shadePath + "bstats")
        relocate("at.favre.lib.crypto.bcrypt", shadePath + "bcrypt")
        relocate("de.mkammerer.argon2", shadePath + "argon2")
        relocate("at.favre.lib.bytes", shadePath + "favre.lib.bytes")
        relocate("cz.koca2000.nbs4j", shadePath + "nbs4j")
        relocate("jakarta", shadePath + "jakarta")
        relocate("com.alessiodp.libby", shadePath + "libby")
        relocate("com.zaxxer", shadePath + "hikaricp")
    }

    build {
        dependsOn(shadowJar)
    }
}

val libsCatalog = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")

subprojects {
    apply {
        plugin("java-library")
    }

    plugins.withId("java") {
        dependencies {
            add("compileOnly", libsCatalog.findLibrary("lombok").get())
            add("compileOnly", libsCatalog.findLibrary("kyoriAdvanture").get())
            add("annotationProcessor", libsCatalog.findLibrary("lombok").get())
        }

        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }
    }
}