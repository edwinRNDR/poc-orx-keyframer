import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

/* the name of this project, default is the template version but you are free to change these */
group = "org.openrndr.extra"
version = "0.4.0-SNAPSHOT"

val applicationMainClass = "TemplateProgramKt"

/*  Which additional (ORX) libraries should be added to this project. */
val orxFeatures = setOf(
//  "orx-camera",
    "orx-compositor",
    "orx-easing",
    "orx-file-watcher",
//  "orx-parameters",
//  "orx-filter-extension",
    "orx-fx",
//  "orx-glslify",
//  "orx-gradient-descent",
//  "orx-integral-image",
//  "orx-interval-tree",
//  "orx-jumpflood",
    "orx-gui",
    "orx-image-fit",
//  "orx-kdtree",
//  "orx-mesh-generators",
//  "orx-midi",
//  "orx-no-clear",
    "orx-noise",
//  "orx-obj-loader",
//    "orx-olive",
//  "orx-osc",
//  "orx-palette",
//  "orx-poisson-fill",
//  "orx-runway",
//  "orx-shader-phrases",
    "orx-shade-styles",
//  "orx-shapes",
//  "orx-syphon",
//  "orx-temporal-blur",
//  "orx-kinect-v1",
    "orx-panel"
)

/* Which OPENRNDR libraries should be added to this project? */
val openrndrFeatures = setOf(
    "panel",
    "video"
)

/*  Which version of OPENRNDR, ORX and Panel should be used? */
val openrndrUseSnapshot = false
val openrndrVersion = if (openrndrUseSnapshot) "0.4.0-SNAPSHOT" else "0.3.40"

val orxUseSnapshot = false
val orxVersion = if (orxUseSnapshot) "0.4.0-SNAPSHOT" else "0.3.50"

//<editor-fold desc="This is code for OPENRNDR, no need to edit this .. most of the times">
val supportedPlatforms = setOf("windows", "macos", "linux-x64", "linux-arm64")

val openrndrOs = if (project.hasProperty("targetPlatform")) {
    val platform: String = project.property("targetPlatform") as String
    if (platform !in supportedPlatforms) {
        throw IllegalArgumentException("target platform not supported: $platform")
    } else {
        platform
    }
} else when (OperatingSystem.current()) {
    OperatingSystem.WINDOWS -> "windows"
    OperatingSystem.MAC_OS -> "macos"
    OperatingSystem.LINUX -> when (val h = DefaultNativePlatform("current").architecture.name) {
        "x86-64" -> "linux-x64"
        "aarch64" -> "linux-arm64"
        else -> throw IllegalArgumentException("architecture not supported: $h")
    }
    else -> throw IllegalArgumentException("os not supported")
}
//</editor-fold>

enum class Logging {
    NONE,
    SIMPLE,
    FULL
}

/*  What type of logging should this project use? */
val applicationLogging = Logging.FULL

val kotlinVersion = "1.3.71"
val spekVersion = "2.0.10"


plugins {
    antlr
    java
    kotlin("jvm") version ("1.3.71")
    maven
}

repositories {
    jcenter()
    mavenCentral()
    if (openrndrUseSnapshot || orxUseSnapshot) {
        mavenLocal()
    }
    maven(url = "https://dl.bintray.com/openrndr/openrndr")

    maven("https://dl.bintray.com/spekframework/spek")

}

fun DependencyHandler.orx(module: String): Any {
    return "org.openrndr.extra:$module:$orxVersion"
}

fun DependencyHandler.openrndr(module: String): Any {
    return "org.openrndr:openrndr-$module:$openrndrVersion"
}

fun DependencyHandler.openrndrNatives(module: String): Any {
    return "org.openrndr:openrndr-$module-natives-$openrndrOs:$openrndrVersion"
}

fun DependencyHandler.orxNatives(module: String): Any {
    return "org.openrndr.extra:$module-natives-$openrndrOs:$orxVersion"
}

val demo = sourceSets.create("demo") {
    java.srcDir("src/demo/kotlin")
}
tasks.getByName(demo.compileJavaTaskName) {
    dependsOn("compileKotlin")
}

dependencies {
    "demoRuntimeOnly"(openrndr("gl3"))
    "demoRuntimeOnly"(openrndrNatives("gl3"))
    "demoImplementation"(openrndr("openal"))
    "demoRuntimeOnly"(openrndrNatives("openal"))
    "demoImplementation"(openrndr("core"))
    "demoImplementation"(openrndr("svg"))
    "demoImplementation"(openrndr("animatable"))
    "demoImplementation"(openrndr("extensions"))
    "demoImplementation"(openrndr("filter"))
    "demoImplementation"(orx("orx-file-watcher"))
    "demoImplementation"(orx("orx-easing"))
    "demoImplementation"(orx("orx-panel"))
    "demoImplementation"(sourceSets.getByName("main").output)
    "demoImplementation"(kotlin("stdlib-jdk8"))
    "demoImplementation"("org.antlr:antlr4-runtime:4.8-1")
    "demoImplementation"("com.google.code.gson:gson:2.8.6")
    "demoRuntimeOnly"("org.apache.logging.log4j", "log4j-slf4j-impl", "2.13.1")
    "demoRuntimeOnly"("com.fasterxml.jackson.core", "jackson-databind", "2.10.3")
    "demoRuntimeOnly"("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", "2.10.3")

    antlr("org.antlr:antlr4:4.8-1")

    implementation(openrndr("core"))
    implementation("org.antlr:antlr4-runtime:4.8-1")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.jetbrains.kotlin", "kotlin-reflect")
    implementation("io.github.microutils", "kotlin-logging", "1.7.9")

    if ("video" in openrndrFeatures) {
        implementation(openrndr("ffmpeg"))
        runtimeOnly(openrndrNatives("ffmpeg"))
    }

    for (feature in orxFeatures) {
        implementation(orx(feature))
    }

    implementation(kotlin("stdlib-jdk8"))
    testImplementation("junit", "junit", "4.12")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testImplementation("org.amshove.kluent:kluent:1.60")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
}

// --------------------------------------------------------------------------------------------------------------------

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.getByName("compileKotlin").dependsOn("generateGrammarSource")

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages")
    outputDirectory = File("${project.buildDir}/generated-src/antlr/org/openrndr/extra/keyframer/antlr".toString())
    source = project.objects
        .sourceDirectorySet("antlr", "antlr")
        .srcDir("src/main/antlr").apply {
            include("*.g4")
        }
}

sourceSets.getByName("main") {
    java.srcDir("src/main/java")
    java.srcDir("src/main/kotlin")
    java.srcDir("build/generated-src/antlr")
}