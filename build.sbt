import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{targetJvm, itSettings}
val appName = "customs-guarantee-account-frontend"
val silencerVersion = "1.17.13"
val bootstrapVersion = "7.22.0"
val scala2_13_8 = "2.13.8"
val testDirectory = "test"
ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := scala2_13_8
lazy val scalastyleSettings = Seq(scalastyleConfig := baseDirectory.value /  "scalastyle-config.xml",
  (Test / scalastyleConfig) := baseDirectory.value/ testDirectory /  "test-scalastyle-config.xml")
lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())
  .settings(libraryDependencies ++= Seq("uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % Test))
lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    targetJvm                        := "jvm-11",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    ScoverageKeys.coverageExcludedFiles :=
      "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;.*views.html.*;" +
        ".*javascript.*;.*Routes.*;.*GuiceInjector;" +
        ".*ControllerConfiguration;.*LanguageSwitchController",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageMinimumBranchTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    TwirlKeys.templateImports ++= Seq(
      "config.AppConfig",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._"
    ),
    scalacOptions ++= Seq(
      "-P:silencer:pathFilters=routes",
      "-P:silencer:pathFilters=target/.*",
      "-Wunused:imports", "-Wunused:params",
      "-Wunused:patvars", "-Wunused:implicits",
      "-Wunused:explicits", "-Wunused:privates"),
    Test / scalacOptions ++= Seq("-Wunused:imports", "-Wunused:params",
      "-Wunused:patvars", "-Wunused:implicits", "-Wunused:explicits", "-Wunused:privates"),
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
  )
  .configs(IntegrationTest)
  .settings(PlayKeys.playDefaultPort := 9395)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalastyleSettings)