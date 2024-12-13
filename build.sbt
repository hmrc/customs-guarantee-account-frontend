import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{itSettings, targetJvm}

val appName          = "customs-guarantee-account-frontend"
val silencerVersion  = "1.7.14"
val bootstrapVersion = "9.5.0"
val scala3_3_4       = "3.3.4"
val testDirectory    = "test"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := scala3_3_4

lazy val scalastyleSettings = Seq(
  scalastyleConfig := baseDirectory.value / "scalastyle-config.xml",
  (Test / scalastyleConfig) := baseDirectory.value / testDirectory / "test-scalastyle-config.xml"
)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())
  .settings(libraryDependencies ++= Seq("uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test))

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    targetJvm := "jvm-11",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    ScoverageKeys.coverageExcludedFiles :=
      "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;.*views.html.*;" +
        ".*javascript.*;.*Routes.*;.*GuiceInjector;" +
        ".*ControllerConfiguration;.*LanguageSwitchController",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageMinimumBranchTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    TwirlKeys.templateImports ++= Seq(
      "config.AppConfig",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._"
    ),
    scalacOptions := scalacOptions.value
      .diff(Seq("-Wunused:all")) ++ Seq("-Wconf:src=routes/.*:s", "-Wconf:msg=Flag.*repeatedly:s"),
    Test / scalacOptions ++= Seq(
      "-Wunused:imports",
      "-Wunused:params",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates"
    ),
    libraryDependencies ++= Seq(
      compilerPlugin(
        "com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.for3Use2_13With("", ".12")
      ),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.for3Use2_13With("", ".12")
    )
  )
  .settings(PlayKeys.playDefaultPort := 9395)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalastyleSettings)
  .settings(
    scalafmtDetailedError := true,
    scalafmtPrintDiff := true,
    scalafmtFailOnErrors := true
  )

addCommandAlias(
  "runAllChecks",
  ";clean;compile;coverage;test;it/test;scalafmtCheckAll;scalastyle;Test/scalastyle;coverageReport"
)
