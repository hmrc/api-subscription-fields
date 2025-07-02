import sbt._

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  private lazy val mongoVersion     = "2.6.0"
  private lazy val bootstrapVersion = "9.13.0"
  val commonDomainVersion           = "0.18.0"

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % mongoVersion,
    "uk.gov.hmrc"       %% "http-metrics"               % "2.9.0",
    "org.typelevel"     %% "cats-core"                  % "2.13.0",
    "eu.timepit"        %% "refined"                    % "0.11.3",
    "commons-validator"  % "commons-validator"          % "1.9.0",
    "uk.gov.hmrc"       %% "api-platform-common-domain" % commonDomainVersion
  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"           %% "bootstrap-test-play-30"              % bootstrapVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-30"             % mongoVersion,
    "uk.gov.hmrc"           %% "api-platform-common-domain-fixtures" % commonDomainVersion,
    "org.pegdown"            % "pegdown"                             % "1.6.0",
    "org.mockito"           %% "mockito-scala-scalatest"             % "2.0.0"
  ).map(d => d % "test")
}

