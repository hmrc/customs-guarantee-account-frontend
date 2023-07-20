import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.19.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "7.14.0-play-28",
    "org.typelevel" %% "cats-core" % "2.3.0",
    "com.typesafe.play" %% "play-json-joda" % "2.9.2",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "1.3.0",
    "org.webjars.npm" % "moment" % "2.29.1",
    "uk.gov.hmrc" %% "tax-year" % "3.2.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.9" % Test,
    "org.jsoup" % "jsoup" % "1.10.2" % Test,
    "com.typesafe.play" %% "play-test" % current % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.16.46" % Test
  )
}
