import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.22.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "7.21.0-play-28",
    "org.typelevel" %% "cats-core" % "2.9.0",
    "com.typesafe.play" %% "play-json-joda" % "2.9.4",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "1.3.0",
    "org.webjars.npm" % "moment" % "2.29.4",
    "uk.gov.hmrc" %% "tax-year" % "3.3.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.16" % Test,
    "org.jsoup" % "jsoup" % "1.16.1" % Test,
    "com.typesafe.play" %% "play-test" % current % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.14" % Test,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "7.21.0-play-28" % Test
  )
}
