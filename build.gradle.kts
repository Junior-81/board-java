plugins {
    id("java")
    id("application")
}

group = "br.com.dio"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("br.com.dio.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.liquibase:liquibase-core:4.29.1")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.projectlombok:lombok:1.18.34")

    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.test {
    useJUnitPlatform()
}

// Configurar encoding para UTF-8
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Task personalizada para executar com configurações específicas
tasks.register("runBoard") {
    group = "application"
    description = "Run the Board application"
    dependsOn("classes")
    
    doLast {
        javaexec {
            mainClass.set("br.com.dio.Main")
            classpath = sourceSets["main"].runtimeClasspath
            systemProperty("file.encoding", "UTF-8")
            standardInput = System.`in`
        }
    }
}
