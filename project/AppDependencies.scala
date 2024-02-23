import sbt._
import play.core.PlayVersion

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  private lazy val mongoVersion     = "1.7.0"
  private lazy val bootstrapVersion = "8.4.0"
  val commonDomainVersion           = "0.12.0-SNAPSHOT"

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % mongoVersion,
    // "org.julienrf"      %% "play-json-derived-codecs"   % "10.1.0",
    // "com.typesafe.play" %% "play-json"                  % "2.9.2",
    "uk.gov.hmrc"       %% "http-metrics"               % "2.8.0",
    "org.typelevel"     %% "cats-core"                  % "2.9.0",
    "eu.timepit"        %% "refined"                    % "0.10.2",
    "commons-validator"  % "commons-validator"          % "1.6",
    "uk.gov.hmrc"       %% "api-platform-common-domain" % commonDomainVersion
  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"           %% "bootstrap-test-play-30"          % bootstrapVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-30"         % mongoVersion,
    "uk.gov.hmrc"           %% "api-platform-test-common-domain" % commonDomainVersion,
    "org.pegdown"            % "pegdown"                         % "1.6.0",
    "org.mockito"           %% "mockito-scala-scalatest"         % "1.17.29",
  ).map(d => d % "test,acceptance")
}
