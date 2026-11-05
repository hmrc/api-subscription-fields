import sbt.*

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  private lazy val mongoVersion        = "2.14.0"
  private lazy val bootstrapVersion    = "10.8.0"
  private val commonDomainVersion      = "1.4.0"
  private val applicationDomainVersion = "1.6.0"

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"        % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"               % mongoVersion,
    "org.typelevel"     %% "cats-core"                        % "2.13.0",
    "org.apache.commons" % "commons-csv"                      % "1.10.0",
    "commons-validator"  % "commons-validator"                % "1.9.0",
    "uk.gov.hmrc"       %% "api-platform-application-domain"  % applicationDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-common-domain"       % commonDomainVersion,
  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"              % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"             % mongoVersion,
    "uk.gov.hmrc"       %% "api-platform-common-domain-fixtures" % commonDomainVersion,
  ).map(d => d % "test")
}
