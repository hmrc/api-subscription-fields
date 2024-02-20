import sbt._
import play.core.PlayVersion

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  private lazy val mongoVersion     = "1.7.0"
  private lazy val bootstrapVersion = "7.23.0"
  val commonDomainVersion           = "0.11.0"

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % mongoVersion,
    "org.julienrf"      %% "play-json-derived-codecs"   % "6.0.0",
    "com.typesafe.play" %% "play-json"                  % "2.9.2",
    "uk.gov.hmrc"       %% "http-metrics"               % "2.7.0",
    "org.typelevel"     %% "cats-core"                  % "2.9.0",
    "eu.timepit"        %% "refined"                    % "0.9.13",
    "be.venneborg"      %% "play28-refined"             % "0.6.0",
    "commons-validator"  % "commons-validator"          % "1.6",
    "uk.gov.hmrc"       %% "api-platform-common-domain" % commonDomainVersion
  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"           %% "bootstrap-test-play-28"          % bootstrapVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-28"         % mongoVersion,
    "uk.gov.hmrc"           %% "api-platform-test-common-domain" % commonDomainVersion,
    "org.pegdown"            % "pegdown"                         % "1.6.0",
    "org.mockito"           %% "mockito-scala-scalatest"         % "1.17.29",
    "com.github.tomakehurst" % "wiremock-jre8-standalone"        % "2.27.1",
    "org.scalatest"         %% "scalatest"                       % "3.2.17"
  ).map(d => d % "test,acceptance")
}
