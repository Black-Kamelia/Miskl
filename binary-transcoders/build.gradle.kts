import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    val jmhPluginVersion: String by System.getProperties()
    id("me.champeau.jmh") version jmhPluginVersion
}

dependencies {
    implementation(project(":utils"))
    api(project(":jvm-bridge"))
}

val jvmVersion: String by rootProject

tasks {
    compileJmhJava {
        sourceCompatibility = jvmVersion
    }

    compileJmhKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(jvmVersion))
        }
    }

    jmh {
        warmup.set("10s")
        timeOnIteration.set("10s")
        timeUnit.set("ms")

        warmupIterations.set(2)
        iterations.set(2)
        fork.set(1)
    }

}
