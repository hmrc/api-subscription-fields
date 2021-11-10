import sbt._
import play.core.PlayVersion

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"               %% "bootstrap-backend-play-28"        % "5.16.0",
    "uk.gov.hmrc"               %% "simple-reactivemongo"             % "8.0.0-play-28",
    "org.julienrf"              %% "play-json-derived-codecs"         % "6.0.0",
    "com.typesafe.play"         %% "play-json"                        % "2.9.2",
    "uk.gov.hmrc"               %% "http-metrics"                     % "2.3.0-play-28",
    "org.typelevel"             %% "cats-core"                        % "2.6.1",
    "eu.timepit"                %% "refined"                          % "0.9.13",
    "be.venneborg"              %% "play28-refined"                   % "0.6.0",
    "commons-validator"         %  "commons-validator"                % "1.6"
  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"               %% "reactivemongo-test"               % "5.0.0-play-28",
    "org.scalatestplus.play"    %% "scalatestplus-play"               % "3.1.3",
    "org.mockito"               %% "mockito-scala-scalatest"          % "1.16.42",
    "org.pegdown"               %  "pegdown"                          % "1.6.0",
    "com.typesafe.play"         %% "play-test"                        % play.core.PlayVersion.current,
    "com.github.tomakehurst"    %  "wiremock-jre8-standalone"         % "2.27.2"
  ).map(d => d % "test,acceptance")
}
