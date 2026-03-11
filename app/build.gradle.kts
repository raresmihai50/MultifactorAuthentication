plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.multifactorauth"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    //implementation ("org.springframework.boot:spring-boot-starter-actuator")

    // 1. Ne permite să creăm API-uri (partea de Web)
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // 2. Ne rezolvă erorile cu jakarta.persistence și ne leagă la DB
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // 3. Driver-ul pentru a comunica cu PostgreSQL din Docker
    runtimeOnly("org.postgresql:postgresql")
    
    // Pentru testare
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}