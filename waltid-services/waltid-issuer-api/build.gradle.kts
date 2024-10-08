import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

object Versions {
    const val KOTLIN_VERSION = "2.0.0" // also change 2 plugins
    const val KTOR_VERSION = "2.3.12" // also change 1 plugin
    const val COROUTINES_VERSION = "1.8.1"
    const val EXPOSED_VERSION = "0.43.0"
    const val HOPLITE_VERSION = "2.8.0.RC3"
}

plugins {
    kotlin("jvm") // Versions.KOTLIN_VERSION
    kotlin("plugin.serialization")  // Versions.KOTLIN_VERSION

    id("io.ktor.plugin") version "2.3.12" // Versions.KTOR_VERSION
    id("org.owasp.dependencycheck") version "9.2.0"
    id("com.github.jk1.dependency-license-report") version "2.8"
    application
    `maven-publish`

    id("com.github.ben-manes.versions")
}

group = "id.walt"
//version = "1.SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.waltid.dev/releases")
    mavenLocal()
}


dependencies {
    api(project(":waltid-services:waltid-service-commons"))

    /* -- KTOR -- */

    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-auth-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-sessions-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-auto-head-response-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-double-receive-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-host-common-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-status-pages-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-compression-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-cors-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-forwarded-header-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-call-logging-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-call-id-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-server-cio-jvm:${Versions.KTOR_VERSION}")

    // Ktor client
    implementation("io.ktor:ktor-client-core-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-client-serialization-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-client-content-negotiation:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-client-json-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-client-okhttp-jvm:${Versions.KTOR_VERSION}")
    implementation("io.ktor:ktor-client-logging-jvm:${Versions.KTOR_VERSION}")


    /* -- Kotlin -- */

    // Kotlinx.serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${Versions.KTOR_VERSION}")

    // Date
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES_VERSION}")

    /* -- Misc --*/

    // Config
    implementation("com.sksamuel.hoplite:hoplite-core:${Versions.HOPLITE_VERSION}")
    implementation("com.sksamuel.hoplite:hoplite-hocon:${Versions.HOPLITE_VERSION}")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    implementation("org.slf4j:jul-to-slf4j:2.0.13")
    implementation("io.klogging:klogging-jvm:0.7.0")
    implementation("io.klogging:slf4j-klogging:0.7.0")


    // Test
    testImplementation(kotlin("test"))
    //testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${Versions.KOTLIN_VERSION}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES_VERSION}")
    //testImplementation("io.ktor:ktor-server-tests-jvm:${Versions.KTOR_VERSION}")

    // OIDC
    api(project(":waltid-libraries:waltid-openid4vc"))

    // walt.id
    api(project(":waltid-libraries:waltid-crypto"))
    api(project(":waltid-libraries:waltid-did"))

    api(project(":waltid-libraries:waltid-verifiable-credentials"))
    api(project(":waltid-libraries:waltid-sdjwt"))
    api(project(":waltid-libraries:waltid-mdoc-credentials"))

    implementation("com.nimbusds:nimbus-jose-jwt:9.40")

    // crypto
    implementation("com.augustcellars.cose:cose-java:1.1.0")

    // Multiplatform / Hashes
    testImplementation(project.dependencies.platform("org.kotlincrypto.hash:bom:0.4.0"))
    testImplementation("org.kotlincrypto.hash:sha2")
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Use the following condition to optionally run the integration tests:
    // > gradle build -PrunIntegrationTests
    if (!project.hasProperty("runIntegrationTests")) {
        exclude("id/walt/test/integration/**")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

tasks.withType<Zip> {
    isZip64 = true
}
tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        windowsScript.writeText(
            windowsScript.readText().replace(Regex("set CLASSPATH=.*"), "set CLASSPATH=%APP_HOME%\\\\lib\\\\*")
        )
    }
}

tasks.withType<ProcessResources> {
    doLast {
        layout.buildDirectory.get().file("resources/main/version.properties").asFile.run {
            parentFile.mkdirs()
            Properties().run {
                setProperty("version", rootProject.version.toString())
                writer().use { store(it, "walt.id version store") }
            }
        }
    }
}

application {
    mainClass.set("id.walt.issuer.MainKt")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                name.set("walt.id issuer")
                description.set(
                    """
                    Kotlin/Java library for walt.id issuer
                    """.trimIndent()
                )
                url.set("https://walt.id")
            }
            from(components["java"])
        }
    }

    repositories {
        maven {
            val releasesRepoUrl = uri("https://maven.waltid.dev/releases")
            val snapshotsRepoUrl = uri("https://maven.waltid.dev/snapshots")
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            val envUsername = System.getenv("MAVEN_USERNAME")
            val envPassword = System.getenv("MAVEN_PASSWORD")

            val usernameFile = File("secret_maven_username.txt")
            val passwordFile = File("secret_maven_password.txt")

            val secretMavenUsername = envUsername ?: usernameFile.let { if (it.isFile) it.readLines().first() else "" }
            val secretMavenPassword = envPassword ?: passwordFile.let { if (it.isFile) it.readLines().first() else "" }

            credentials {
                username = secretMavenUsername
                password = secretMavenPassword
            }
        }
    }
}

/*licenseReport {
    renderers = arrayOf<ReportRenderer>(InventoryHtmlReportRenderer("xyzkit-licenses-report.html", "XYZ Kit"))
    filters = arrayOf<DependencyFilter>(LicenseBundleNormalizer())
}*/
