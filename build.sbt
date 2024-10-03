Global / onChangedBuildSource := ReloadOnSourceChanges

// give the user a nice default project!
ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "3.4.0"

def excludeJars(cp: Classpath) =
  cp filter { f =>
    Set(
      "scala-asm-9.4.0-scala-1.jar",
      "interface-1.3.5.jar",
      "interface-1.0.19.jar",
      "jline-terminal-3.19.0.jar",
      "jline-reader-3.19.0.jar",
      "jline-3.22.0.jar",
      "scala-compiler-2.13.10.jar",
      "scala3-compiler_3-3.3.1.jar",
      "flink-shaded-zookeeper-3-3.7.1-17.0.jar",
      "flink-shaded-jackson-2.14.2-17.0.jar",
      "annotations-24.0.1.jar"
    ).contains(
      f.data.getName
    )
  }

lazy val root = (project in file(".")).settings(
  name := "flink-scala-option",
  assemblyPackageScala / assembleArtifact := false,
  assembly / assemblyExcludedJars := {
    val cp = (assembly / fullClasspath).value
    excludeJars(cp)
  },
  libraryDependencies ++= Seq(
    ("org.flinkextended" %% "flink-scala-api" % "1.18.1_1.1.5").excludeAll(
      ExclusionRule(organization = "org.apache.flink"),
      ExclusionRule(organization = "org.scalameta"),
      ExclusionRule(organization = "com.google.code.findbugs")
    ),
    "org.apache.flink" % "flink-clients" % "1.18.1" % Provided,
    "ch.qos.logback" % "logback-classic" % "1.4.14" % Provided
  )
)

// make run command include the provided dependencies
Compile / run := Defaults
  .runTask(
    Compile / fullClasspath,
    Compile / run / mainClass,
    Compile / run / runner
  )
  .evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true
