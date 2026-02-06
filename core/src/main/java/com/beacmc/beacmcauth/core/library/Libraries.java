package com.beacmc.beacmcauth.core.library;

import com.alessiodp.libby.Library;

public class Libraries {

    public static final Library JDA = Library.builder()
            .groupId("net{}dv8tion")
            .artifactId("JDA")
            .version("5.2.2")
            .resolveTransitiveDependencies(true)
            .excludeTransitiveDependency("club{}minnced", "opus-java")
            .build();

    public static final Library POSTGRESQL = Library.builder()
            .groupId("org{}postgresql")
            .artifactId("postgresql")
            .version("42.7.4")
            .build();

    public static final Library MARIADB = Library.builder()
            .groupId("org{}mariadb{}jdbc")
            .artifactId("mariadb-java-client")
            .version("3.5.0")
            .build();

    public static final Library SQLITE = Library.builder()
            .groupId("org{}xerial")
            .artifactId("sqlite-jdbc")
            .version("3.47.0.0")
            .build();

    public static final Library OKHTTP = Library.builder()
            .groupId("com{}squareup{}okhttp3")
            .artifactId("okhttp")
            .version("4.12.0")
            .build();

    public static final Library KOTLIN = Library.builder()
            .groupId("org{}jetbrains{}kotlin")
            .artifactId("kotlin-stdlib")
            .version("1.9.23")
            .build();

    public static final Library OKIO = Library.builder()
            .groupId("com{}squareup{}okio")
            .artifactId("okio-jvm")
            .version("3.9.0")
            .build();


    public static final Library TELEGRAM = Library.builder()
            .groupId("com{}github{}pengrad")
            .artifactId("java-telegram-bot-api")
            .version("7.11.0")
            .build();
}
