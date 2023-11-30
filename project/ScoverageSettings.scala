import scoverage.ScoverageKeys._
  
object ScoverageSettings {
  def apply() = Seq(
    coverageMinimumStmtTotal := 94.00,
    coverageMinimumBranchTotal := 83.00,
    coverageFailOnMinimum := true,

    // Semicolon-separated list of regexs matching classes to exclude
    coverageExcludedPackages := Seq(
      "<empty>",
      "com.kenshoo.play.metrics.*",
      "prod.*",
      "testOnlyDoNotUseInAppConf.*",
      "app.*",
      "uk.gov.hmrc.apisubscriptionfields.config",
      "uk.gov.hmrc.BuildInfo"
    ).mkString(";")
  )
}