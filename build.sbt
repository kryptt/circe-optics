import sbtcrossproject.CrossType
import sbtghactions.JavaSpec.Distribution

ThisBuild / organization := "io.circe"

val compilerOptionsScala2 = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen"
)

val compilerOptionsScala3 = Seq(
  "-encoding",
  "utf8",
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:experimental.macros",
  "-language:higherKinds"
)

val circeVersion = "0.14.3"
val monocleLegacyVersion = "2.1.0"
val monocleVersion = "3.2.0"
val previousCirceOpticsVersion = "0.11.0"

def priorTo3(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, _)) => true
    case _            => false
  }

def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _                              => false
  }

ThisBuild / crossScalaVersions := Seq("2.12.17", "2.13.10", "3.2.1")

val baseSettings = Seq(
  scalacOptions ++= (
    if (priorTo3(scalaVersion.value))
      compilerOptionsScala2
    else
      compilerOptionsScala3
    ),
  scalacOptions ++= (
    if (priorTo2_13(scalaVersion.value))
      Seq(
        "-Xfuture",
        "-Yno-adapted-args",
        "-Ywarn-unused-import"
      )
    else if (priorTo3(scalaVersion.value))
      Seq(
        "-Ywarn-unused:imports"
      )
    else
      Seq()
  ),
  Compile / console / scalacOptions ~= {
    _.filterNot(Set("-Ywarn-unused-import", "-Ywarn-unused:imports"))
  },
  Test / console / scalacOptions ~= {
    _.filterNot(Set("-Ywarn-unused-import", "-Ywarn-unused:imports"))
  },
  coverageHighlighting := true,
  coverageEnabled := (
    if (priorTo2_13(scalaVersion.value)) false else coverageEnabled.value
  ),
)

val allSettings = baseSettings ++ publishSettings

val docMappingsApiDir = settingKey[String]("Subdirectory in site target directory for API docs")

val root = project.in(file(".")).settings(allSettings).settings(noPublishSettings).aggregate(opticsJVM, opticsJS)

lazy val optics = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("optics"))
  .settings(allSettings)
  .settings(
    moduleName := "circe-optics",
    mimaPreviousArtifacts := Set("io.circe" %% "circe-optics" % previousCirceOpticsVersion),
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion % Test,
      "io.circe" %%% "circe-testing" % circeVersion % Test,
      "org.scalatestplus" %%% "scalacheck-1-15" % "3.2.11.0" % Test,
      "org.typelevel" %%% "discipline-scalatest" % "2.2.0" % Test
    ),
    libraryDependencies ++= (
      if (priorTo2_13(scalaVersion.value))
        Seq(
            "com.github.julien-truffaut" %%% "monocle-core" % monocleLegacyVersion,
            "com.github.julien-truffaut" %%% "monocle-law" % monocleLegacyVersion % Test,
        )
      else
        Seq(
          "dev.optics" %%% "monocle-core" % monocleVersion,
          "dev.optics" %%% "monocle-law" % monocleVersion % Test,
        )
    ),
    docMappingsApiDir := "api",
  )
  .jsSettings(
    libraryDependencies +=
      "io.github.cquiroz" %%% "scala-java-time" % "2.5.0" % Test,
    coverageEnabled := false
  )

lazy val opticsJVM = optics.jvm
lazy val opticsJS = optics.js

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseVcsSign := true,
  homepage := Some(url("https://github.com/circe/circe-optics")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots".at(nexus + "content/repositories/snapshots"))
    else
      Some("releases".at(nexus + "service/local/staging/deploy/maven2"))
  },
  autoAPIMappings := true,
  apiURL := Some(url("https://circe.github.io/circe-optics/api/")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/circe/circe-optics"),
      "scm:git:git@github.com:circe/circe-optics.git"
    )
  ),
  developers := List(
    Developer(
      "travisbrown",
      "Travis Brown",
      "travisrobertbrown@gmail.com",
      url("https://twitter.com/travisbrown")
    )
  )
)

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec(Distribution.Adopt, "8"))
// No auto-publish atm. Remove this line to generate publish stage
ThisBuild / githubWorkflowPublishTargetBranches := Seq.empty
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(
    List("clean", "coverage", "test", "coverageReport", "scalafmtCheckAll"),
    id = None,
    name = Some("Test")
  ),
  WorkflowStep.Use(
    UseRef.Public(
      "codecov",
      "codecov-action",
      "v1"
    )
  )
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

credentials ++= (
  for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    username,
    password
  )
).toSeq
