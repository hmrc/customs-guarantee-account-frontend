import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.targetJvm

val appName = "customs-guarantee-account-frontend"

val silencerVersion = "1.17.13"
val testDirectory = "test"

lazy val scalastyleSettings = Seq(scalastyleConfig := baseDirectory.value /  "scalastyle-config.xml",
  (Test / scalastyleConfig) := baseDirectory.value/ testDirectory /  "test-scalastyle-config.xml")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.13.8",
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
    scalacOptions += "-P:silencer:pathFilters=routes",
    scalacOptions ++= Seq("-Wunused:imports", "-Wunused:params",
      "-Wunused:patvars", "-Wunused:implicits", "-Wunused:explicits", "-Wunused:privates"),
    Test / scalacOptions ++= Seq("-Wunused:imports", "-Wunused:params",
      "-Wunused:patvars", "-Wunused:implicits", "-Wunused:explicits", "-Wunused:privates"),
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
  )
  .settings(PlayKeys.playDefaultPort := 9395)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalastyleSettings)

