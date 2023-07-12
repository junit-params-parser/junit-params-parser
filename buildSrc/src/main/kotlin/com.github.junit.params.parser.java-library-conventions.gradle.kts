plugins {
  // Apply the common convention plugin for shared build configuration between library and application projects.
  id("com.github.junit.params.parser.java-common-conventions")

  // Apply the java-library plugin for API and implementation separation.
  `java-library`

  `maven-publish`
}

group = "com.github.junit-params-parser"
version = property("version").toString()

java {
  withJavadocJar()
  withSourcesJar()
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      groupId = project.group.toString()
      artifactId = path.replace(':', '-').substring(1)
      version = project.version.toString()

      pom {
        name.set("Library for parse arguments in parameterized tests")
        description.set(project.description)
        url.set("https://github.com/junit-params-parser/junit-params-parser")
        scm {
          url.set("https://github.com/junit-params-parser/junit-params-parser")
          connection.set("scm:git:https://github.com/junit-params-parser/junit-params-parser.git")
          developerConnection.set("scm:git:git@github.com:junit-params-parser/junit-params-parser.git")
        }
        licenses {
          license {
            name.set("MIT")
            url.set("https://github.com/junit-params-parser/junit-params-parser/blob/main/LICENSE")
            distribution.set("repo")
          }
        }
        developers {
          developer {
            id.set("valery1707")
            name.set("Valeriy Vyrva")
            email.set("valery1707@gmail.com")
            roles.add("architect")
            roles.add("developer")
          }
        }
        contributors {
        }
      }
    }
  }
}
