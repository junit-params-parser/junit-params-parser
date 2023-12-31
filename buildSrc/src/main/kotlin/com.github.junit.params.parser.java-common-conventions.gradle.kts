import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  // Apply the java Plugin to add support for Java.
  java
  // Validate code style with Checkstyle
  checkstyle
}

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()
}

dependencies {
  constraints {
    // Define dependency versions as constraints
  }
  implementation("com.google.code.findbugs:jsr305:3.0.2")
  implementation("org.jetbrains:annotations:24.0.1")

  // Use JUnit Jupiter for testing.
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
  testImplementation("org.assertj:assertj-core:3.24.2")
  testImplementation("org.hamcrest:hamcrest:2.2")
}

//Encoding
tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
}
tasks.withType<Test> {
  systemProperty("file.encoding", "UTF-8")
}
tasks.withType<Javadoc> {
  options.encoding = "UTF-8"
}

tasks.named<Test>("test") {
  // Use JUnit Platform for unit tests.
  useJUnitPlatform()
  testLogging {
    events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
  }
}

/**
 * Conventions from `.editorconfig` has recommendation status and can not support by IDE.
 * Conventions from `checkstyle.xml` is required for compilation.
 */
apply<CheckstylePlugin>()
checkstyle {
  // Last version that supports Java 1.8
  toolVersion = "9.3"
  isIgnoreFailures = findProperty("checkstyle")?.equals("true")?.not() ?: true
  isShowViolations = true
}
tasks.withType<Checkstyle>().configureEach {
  reports {
    xml.required.set(true)
    html.required.set(true)
  }
}
