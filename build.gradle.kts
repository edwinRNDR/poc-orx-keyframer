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
val antlrKotlinVersion = "94f8764bcf"

buildscript {
    val antlrKotlinVersion = "94f8764bcf"

    dependencies {
        classpath("com.strumenta.antlr-kotlin:antlr-kotlin-gradle-plugin:$antlrKotlinVersion")
    }

    repositories {
        maven("https://jitpack.io")
    }
}

plugins {
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
    maven("https://jitpack.io")

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

dependencies {
    implementation("com.strumenta.antlr-kotlin:antlr-kotlin-runtime-jvm:$antlrKotlinVersion")
    /*  This is where you add additional (third-party) dependencies */

//    implementation("org.jsoup:jsoup:1.12.2")
    implementation("com.google.code.gson:gson:2.8.6")

    implementation("org.jetbrains.kotlin", "kotlin-reflect")

    //<editor-fold desc="Managed dependencies">
    runtimeOnly(openrndr("gl3"))
    runtimeOnly(openrndrNatives("gl3"))
    implementation(openrndr("openal"))
    runtimeOnly(openrndrNatives("openal"))
    implementation(openrndr("core"))
    implementation(openrndr("svg"))
    implementation(openrndr("animatable"))
    implementation(openrndr("extensions"))
    implementation(openrndr("filter"))

    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.5")
    implementation("io.github.microutils", "kotlin-logging", "1.7.9")

    when (applicationLogging) {
        Logging.NONE -> {
            runtimeOnly("org.slf4j", "slf4j-nop", "1.7.30")
        }
        Logging.SIMPLE -> {
            runtimeOnly("org.slf4j", "slf4j-simple", "1.7.30")
        }
        Logging.FULL -> {
            runtimeOnly("org.apache.logging.log4j", "log4j-slf4j-impl", "2.13.1")
            runtimeOnly("com.fasterxml.jackson.core", "jackson-databind", "2.10.3")
            runtimeOnly("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", "2.10.3")
        }
    }

    if ("video" in openrndrFeatures) {
        implementation(openrndr("ffmpeg"))
        runtimeOnly(openrndrNatives("ffmpeg"))
    }

    for (feature in orxFeatures) {
        implementation(orx(feature))
    }

    if ("orx-kinect-v1" in orxFeatures) {
        runtimeOnly(orxNatives("orx-kinect-v1"))
    }

    if ("orx-olive" in orxFeatures) {
        implementation("org.jetbrains.kotlin", "kotlin-scripting-compiler-embeddable")
    }

    implementation(kotlin("stdlib-jdk8"))
    testImplementation("junit", "junit", "4.12")
    //</editor-fold>

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

// run generate task before build
// not required if you add the generated sources to version control
// you can call the task manually in this case to update the generated sources
tasks.getByName("compileKotlin").dependsOn("generateKotlinGrammarSource")

// you have to add the generated sources to kotlin compiler source directory list
configure<SourceSetContainer> {
    named("main") {
        withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
            kotlin.srcDir("build/generated-src/antlr/main")
            // kotlin.srcDir("src/main/kotlin-antlr")
        }
    }
}
// in antlr-kotlin-plugin <0.0.5, the configuration was applied by the plugin.
// starting from verison 0.0.5, you have to apply it manually:
tasks.register<com.strumenta.antlrkotlin.gradleplugin.AntlrKotlinTask>("generateKotlinGrammarSource") {
    // the classpath used to run antlr code generation
    antlrClasspath = configurations.detachedConfiguration(
        // antlr itself
        // antlr is transitive added by antlr-kotlin-target,
        // add another dependency if you want to choose another antlr4 version (not recommended)
        // project.dependencies.create("org.antlr:antlr4:$antlrVersion"),

        // antlr target, required to create kotlin code
        project.dependencies.create("com.strumenta.antlr-kotlin:antlr-kotlin-target:$antlrKotlinVersion")
    )
    maxHeapSize = "64m"
    packageName = "org.openrndr.extra.keyframer.antlr"
    //arguments = listOf("-no-visitor", "-no-listener")
    source = project.objects
        .sourceDirectorySet("antlr", "antlr")
        .srcDir("src/main/antlr").apply {
            include("*.g4")
        }
    // outputDirectory is required, put it into the build directory
    // if you do not want to add the generated sources to version control
    outputDirectory = File("build/generated-src/antlr/main")
    // use this settings if you want to add the generated sources to version control
    // outputDirectory = File("src/main/kotlin-antlr")
}