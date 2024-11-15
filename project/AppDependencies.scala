import sbt._

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  private lazy val mongoVersion     = "2.1.0"
  private lazy val bootstrapVersion = "9.5.0"
  val commonDomainVersion           = "0.17.0"

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % mongoVersion,
    "uk.gov.hmrc"       %% "http-metrics"               % "2.8.0",
    "org.typelevel"     %% "cats-core"                  % "2.10.0",
    "eu.timepit"        %% "refined"                    % "0.10.2",
    "commons-validator"  % "commons-validator"          % "1.6",
    "uk.gov.hmrc"       %% "api-platform-common-domain" % commonDomainVersion
  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"           %% "bootstrap-test-play-30"              % bootstrapVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-30"             % mongoVersion,
    "uk.gov.hmrc"           %% "api-platform-common-domain-fixtures" % commonDomainVersion,
    "org.pegdown"            % "pegdown"                             % "1.6.0",
    "org.mockito"           %% "mockito-scala-scalatest"             % "1.17.30"
  ).map(d => d % "test")
}

