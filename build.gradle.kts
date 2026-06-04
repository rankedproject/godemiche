import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java-library")
    id("maven-publish")
    id("checkstyle")
    id("signing")
    id("net.ltgt.errorprone") version "5.1.0"
}

group = "wtf.ranked"
version = System.getenv("VERSION") ?: "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.49.0")
    errorprone("com.uber.nullaway:nullaway:0.13.5")
    compileOnly("org.jetbrains:annotations:26.1.0")

    implementation("com.google.guava:guava:33.6.0-jre")
    implementation("org.jspecify:jspecify:1.0.0")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.awaitility:awaitility:4.3.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

checkstyle {
    toolVersion = "13.5.0"
    configDirectory.set(rootProject.file(".checkstyle"))
    configFile = rootProject.file(".checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "godemiche"

            pom {
                name.set("Godemiche")
                description.set("Lightweight scheduled task utility library")
                url.set("https://github.com/rankedproject/godemiche")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("ranked")
                        name.set("RankedProject")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/rankedproject/godemiche.git")
                    developerConnection.set("scm:git:ssh://git@github.com/rankedproject/godemiche.git")
                    url.set("https://github.com/rankedproject/godemiche")
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_PASSWORD")
    )
    sign(publishing.publications["mavenJava"])
}

tasks.named("publish") {
    dependsOn("verify")
}

tasks.named("check") {
    dependsOn("checkstyleMain", "checkstyleTest", "test")
}

tasks.register("verify") {
    group = "verification"
    description = "Strict CI validation gate for publishing"

    dependsOn(
            "build",
            "check",
            "javadoc"
    )
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disableWarningsInGeneratedCode = true
        allErrorsAsWarnings = false
        option("NullAway:AnnotatedPackages", "wtf.ranked")
    }
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("failed", "skipped")
    }
}
