plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}

rootProject.name = "junit-params-parser"

// Automatically include subprojects
rootDir
  .walkTopDown()
  .maxDepth(3)
  .filter { it.name == "build.gradle.kts" }
  .mapNotNull { it.relativeTo(rootDir).parent }
  .map { it.replace(File.separatorChar, ':') }
  .filterNot { it == "buildSrc" }
  .forEach { include(it) }
