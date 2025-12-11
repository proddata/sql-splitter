plugins {
    java
    antlr
}

group = "io.kestra.sql"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.13.1") // latest stable
    implementation("org.antlr:antlr4-runtime:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.generateGrammarSource {
    arguments = listOf("-package", "io.kestra.sql.grammar")
    outputDirectory = file("build/generated-src/antlr/main/io/kestra/sql/grammar")
}

sourceSets["main"].java {
    srcDir("build/generated-src/antlr/main")
}