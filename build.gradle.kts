import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java-library")
    id("checkstyle")
    id("signing")
    id("net.ltgt.errorprone") version "5.1.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "wtf.ranked"
version = System.getenv("VERSION") ?: "1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
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

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
            groupId = "wtf.ranked",
            artifactId = "godemiche",
            version = System.getenv("VERSION") ?: "1.0"
    )

    configure(JavaLibrary(
            JavadocJar.Javadoc(),
            SourcesJar.Sources()
    ))

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
            url.set("https://github.com/rankedproject/godemiche")
        }
    }
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

tasks.register("verify") {
    group = "verification"
    description = "Strict CI validation gate for publishing"

    dependsOn("check", "javadoc")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("failed", "skipped")
    }
}
