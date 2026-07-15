import sbt._

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  private lazy val mongoVersion        = "2.12.0"
  private lazy val bootstrapVersion    = "10.7.1"
  private val commonDomainVersion      = "1.1.0"
  private val applicationDomainVersion = "1.3.0"
  private val mockitoScalaVersion      = "2.2.1"

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"              % mongoVersion,
    "uk.gov.hmrc"       %% "http-metrics"                    % "2.9.0",
    "org.typelevel"     %% "cats-core"                       % "2.13.0",
    "org.apache.commons" % "commons-csv"                     % "1.10.0",
    "commons-validator"  % "commons-validator"               % "1.9.0",
    "uk.gov.hmrc"       %% "api-platform-application-domain" % applicationDomainVersion
  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"              % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"             % mongoVersion,
    "uk.gov.hmrc"       %% "api-platform-common-domain-fixtures" % commonDomainVersion,
    "org.mockito"       %% "mockito-scala-scalatest"             % mockitoScalaVersion
  ).map(d => d % "test")
}
