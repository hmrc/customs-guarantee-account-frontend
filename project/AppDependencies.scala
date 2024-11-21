import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.5.0"
  private val mongoVersion = "2.3.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "11.6.0",
    "org.typelevel" %% "cats-core" % "2.12.0",
    "com.typesafe.play" %% "play-json-joda" % "2.10.6",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % mongoVersion,
    "org.webjars.npm" % "moment" % "2.30.1",
    "uk.gov.hmrc" %% "tax-year" % "5.0.0")

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.18" % Test,
    "org.jsoup" % "jsoup" % "1.18.1" % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
    "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test)
}
