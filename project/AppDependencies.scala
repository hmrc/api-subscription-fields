import sbt._
import play.core.PlayVersion

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  private lazy val mongoVersion = "0.70.0"
  private lazy val bootstrapVersion = "7.2.0"

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"               %% "bootstrap-backend-play-28"        % bootstrapVersion,
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-play-28"               % "0.68.0",
    "org.julienrf"              %% "play-json-derived-codecs"         % "6.0.0",
    "com.typesafe.play"         %% "play-json"                        % "2.9.2",
    "uk.gov.hmrc"               %% "http-metrics"                     % "2.5.0-play-28",
    "org.typelevel"             %% "cats-core"                        % "2.6.1",
    "eu.timepit"                %% "refined"                          % "0.9.13",
    "be.venneborg"              %% "play28-refined"                   % "0.6.0",
    "commons-validator"         %  "commons-validator"                % "1.6",
    "com.beachape"              %% "enumeratum-play-json"             % "1.6.0"
  )

  private lazy val testDependencies = Seq(
//    "uk.gov.hmrc"                 %% "bootstrap-test-play-28"         % bootstrapVersion,
//    "uk.gov.hmrc.mongo"           %% "hmrc-mongo-test-play-28"        % mongoVersion,
//    "org.mockito"                 %% "mockito-scala-scalatest"        % "1.16.46",
//    "org.jsoup"                   %  "jsoup"                          % "1.13.1",
//    "org.scalaj"                  %% "scalaj-http"                    % "2.4.2",
//    "com.github.tomakehurst"      %  "wiremock-jre8-standalone"       % "2.31.0",
//    "org.scalacheck"              %% "scalacheck"                     % "1.15.4",
//    "org.scalatestplus"           %% "scalacheck-1-15"                % "3.2.10.0"

    "uk.gov.hmrc"               %% "bootstrap-test-play-28"           % bootstrapVersion,
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-test-play-28"          % "0.68.0",
    "org.scalatestplus.play"    %% "scalatestplus-play"               % "3.1.3",
    "org.mockito"               %% "mockito-scala-scalatest"          % "1.16.42",
    "org.pegdown"               %  "pegdown"                          % "1.6.0",
    "com.typesafe.play"         %% "play-test"                        % play.core.PlayVersion.current,
    "com.github.tomakehurst"    %  "wiremock-jre8-standalone"         % "2.27.2"
  ).map(d => d % "test,acceptance")
}
