plugins {
    java
    antlr
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.kestra.sql"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // ANTLR
    antlr("org.antlr:antlr4:4.13.1")
    implementation("org.antlr:antlr4-runtime:4.13.1")

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.generateGrammarSource {
    arguments = listOf("-package", "io.kestra.sql.grammar")
    outputDirectory = file("build/generated-src/antlr/main/io/kestra/sql/grammar")
}

sourceSets {
    named("main") {
        java.srcDir("build/generated-src/antlr/main")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// ----------
// Fat JAR
// ----------
tasks.shadowJar {
    archiveClassifier.set("all")     // produces sql-splitter-0.1.0-all.jar
    mergeServiceFiles()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.kestra.sql.example.Example"
    }
}