package com.beacmc.beacmcauth.core.library;

import com.alessiodp.libby.Library;

public class Libraries {

    public static final Library JDA = Library.builder()
            .groupId("net{}dv8tion")
            .artifactId("JDA")
            .version("6.7.0")
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
            .version("5.3.2")
            .build();

    public static final Library OKHTTP_LOGGING = Library.builder()
            .groupId("com{}squareup{}okhttp3")
            .artifactId("logging-interceptor")
            .version("5.3.2")
            .build();

    public static final Library KOTLIN = Library.builder()
            .groupId("org{}jetbrains{}kotlin")
            .artifactId("kotlin-stdlib")
            .version("2.3.21")
            .build();


    public static final Library TELEGRAM = Library.builder()
            .groupId("com{}github{}pengrad")
            .artifactId("java-telegram-bot-api")
            .version("10.3.0")
            .build();
}
