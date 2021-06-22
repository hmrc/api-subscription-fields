import sbt._
import play.core.PlayVersion

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"         %% "bootstrap-play-26"        % "1.4.0",
    "uk.gov.hmrc"         %% "simple-reactivemongo"     % "7.30.0-play-26",
    "org.julienrf"        %% "play-json-derived-codecs" % "6.0.0",
    "com.typesafe.play"   %% "play-json"                % "2.7.1",
    "uk.gov.hmrc"         %% "http-metrics"             % "1.10.0",
    "org.typelevel"       %% "cats-core"                % "2.1.0",
    "eu.timepit"          %% "refined"                  % "0.9.13",
    "be.venneborg"        %% "play26-refined"           % "0.5.0",
    "commons-validator"   %  "commons-validator"        % "1.6"
  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"               %% "reactivemongo-test"         % "4.21.0-play-26",
    "org.scalatestplus.play"    %% "scalatestplus-play"         % "3.1.3",
    "org.mockito"               %% "mockito-scala-scalatest"    % "1.14.4",
    "org.pegdown"               %  "pegdown"                    % "1.6.0",
    "com.typesafe.play"         %% "play-test"                  % play.core.PlayVersion.current,
    "com.github.tomakehurst"    %  "wiremock-jre8"              % "2.26.3"
  ).map(d => d % "test,acceptance")
}
