package com.beacmc.beacmcauth.core.library;

import com.alessiodp.libby.Library;

public class Libraries {

    public static final Library JDA = Library.builder()
            .groupId("net{}dv8tion")
            .artifactId("JDA")
            .version("5.0.0-beta.20")
            .resolveTransitiveDependencies(true)
            .excludeTransitiveDependency("club{}minnced", "opus-java")
            .build();

    public static final Library ORMLITE = Library.builder()
            .groupId("com{}j256{}ormlite")
            .artifactId("ormlite-jdbc")
            .version("6.1")
            .relocate("com{}j256{}ormlite", "com{}beacmc{}beacmcauth{}libs")
            .build();

    public static final Library POSTGRESQL = Library.builder()
            .groupId("org{}postgresql")
            .artifactId("postgresql")
            .version("42.7.4")
            .relocate("org{}postgresql", "com{}beacmc{}beacmcauth{}libs")
            .build();

    public static final Library MARIADB = Library.builder()
            .groupId("org{}mariadb{}jdbc")
            .artifactId("mariadb-java-client")
            .version("3.5.0")
            .relocate("org{}mariadb{}jdbc", "com{}beacmc{}beacmcauth{}libs")
            .build();

    public static final Library SQLITE = Library.builder()
            .groupId("org{}xerial")
            .artifactId("sqlite-jdbc")
            .version("3.47.0.0")
            .relocate("org{}xerial", "com{}beacmc{}beacmcauth{}libs")
            .build();

    public static final Library TELEGRAM = Library.builder()
            .groupId("com{}github{}pengrad")
            .artifactId("java-telegram-bot-api")
            .version("7.11.0")
            .relocate("com{}github{}pengrad", "com{}beacmc{}beacmcauth{}libs")
            .build();
}
