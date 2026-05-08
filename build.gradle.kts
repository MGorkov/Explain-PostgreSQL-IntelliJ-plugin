import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

plugins {
  id("java")
  alias(libs.plugins.kotlin)
  alias(libs.plugins.intellijPlatform)
}

group = "com.mgorkov"

val buildTimeAndDate = OffsetDateTime.now()
val buildDate = DateTimeFormatter.ofPattern("YYYYMMdd").format(buildTimeAndDate)

version = "1.4.1-" + buildDate

kotlin {
  jvmToolchain(17)
}

repositories {
  mavenCentral()
  gradlePluginPortal()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    intellijIdea(providers.gradleProperty("platformVersion"))
    bundledPlugin("com.intellij.database")
    plugin("DBN", "3.7.3.0")
  }
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = providers.gradleProperty("pluginSinceBuild")
    }
  }
}

tasks {
  wrapper {
    gradleVersion = providers.gradleProperty("gradleVersion").get()
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}
