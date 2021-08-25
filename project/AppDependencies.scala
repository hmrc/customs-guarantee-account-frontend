import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.4.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.76.0-play-28",
    "uk.gov.hmrc" %% "play-frontend-govuk" % "0.77.0-play-28",
    "org.typelevel" %% "cats-core" % "2.3.0",
    "com.typesafe.play" %% "play-json-joda" % "2.9.2",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "0.50.0",
    "org.webjars.npm" % "moment" % "2.29.1",
    "org.webjars.npm" % "hmrc-frontend" % "1.5.0",
    "uk.gov.hmrc" %% "tax-year" % "1.3.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % "5.4.0" % Test,
    "org.scalatest" %% "scalatest" % "3.2.9" % Test,
    "org.jsoup" % "jsoup" % "1.10.2" % Test,
    "com.typesafe.play" %% "play-test" % current % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.16.37" % Test
  )
}
