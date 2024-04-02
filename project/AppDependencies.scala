import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "8.0.0",
    "org.typelevel" %% "cats-core" % "2.10.0",
    "com.typesafe.play" %% "play-json-joda" % "2.9.4",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % "1.8.0",
    "org.webjars.npm" % "moment" % "2.29.4",
    "uk.gov.hmrc" %% "tax-year" % "4.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.16" % Test,
    "org.jsoup" % "jsoup" % "1.16.1" % Test,
    //"com.typesafe.play" %% "play-test" % current % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.29" % Test,
  )
}
