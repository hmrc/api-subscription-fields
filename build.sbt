/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning

import scala.language.postfixOps

val compile = Seq(
  "uk.gov.hmrc" %% "bootstrap-play-25" % "4.12.0",
  "uk.gov.hmrc" %% "simple-reactivemongo" % "7.19.0-play-25"
)

val overrides = Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.16.6"
)

def test(scope: String = "test,acceptance") = Seq(
  "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-25" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "4.14.0-play-25" % scope,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope,
  "org.scalatest" %% "scalatest" % "3.0.4" % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
  "org.pegdown" % "pegdown" % "1.6.0" % scope,
  "com.github.tomakehurst" % "wiremock" % "2.11.0" % scope,
  "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % scope,
  "org.mockito" % "mockito-core" % "1.9.5" % "test,it"
)

val appName = "api-subscription-fields"

lazy val appDependencies: Seq[ModuleID] = compile ++ overrides ++ test()

resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo)

lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val AcceptanceTest = config("acceptance") extend Test
lazy val IntegrationTest = config("it") extend Test

val testConfig = Seq(AcceptanceTest, Test, IntegrationTest)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) ++ plugins: _*)
  .configs(testConfig: _*)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(acceptanceTestSettings: _*)
  .settings(scalaVersion := "2.11.11")
  .settings(
    libraryDependencies ++= appDependencies,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(scoverageSettings)
  .settings(
    testOptions in Test := Seq(Tests.Filter(unitFilter), Tests.Argument("-eT")),
    fork in Test := false,
    addTestReportOption(Test, "test-reports")
  )
  .settings(Defaults.itSettings)
  .settings(
    testOptions in IntegrationTest := Seq(Tests.Filter(integrationFilter), Tests.Argument("-eT")),
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest) (base => Seq(base / "test" / "it")),
    addTestReportOption(IntegrationTest, "integration-reports"),
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false
  )
  .settings(majorVersion := 0)

lazy val acceptanceTestSettings =
  inConfig(AcceptanceTest)(Defaults.testSettings) ++
    Seq(
      testOptions in AcceptanceTest := Seq(Tests.Filter(acceptanceFilter)),
      unmanagedSourceDirectories in AcceptanceTest := Seq(
        baseDirectory.value / "test" / "acceptance",
        baseDirectory.value / "test" / "util"
      ),
      fork in AcceptanceTest := false,
      parallelExecution in AcceptanceTest := false,
      addTestReportOption(AcceptanceTest, "acceptance-reports")
    )

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := "<empty>;Reverse.*;.*model.*;.*config.*;.*(AuthService|BuildInfo|Routes).*;.*.application;.*.definition",
  coverageMinimum := 97,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  parallelExecution in Test := false
)

def unitFilter(name: String): Boolean = !integrationFilter(name) && !acceptanceFilter(name)
def integrationFilter(name: String): Boolean = name contains "integration"
def acceptanceFilter(name: String): Boolean = name contains "acceptance"

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
  tests map {
    test => Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
  }

def forkedJvmPerTestConfig(tests: Seq[TestDefinition], packages: String*): Seq[Group] =
  tests.groupBy(_.name.takeWhile(_ != '.')).filter(packageAndTests => packages contains packageAndTests._1) map {
    case (packg, theTests) => Group(packg, theTests, SubProcess(ForkOptions()))
  } toSeq

def onPackageName(rootPackage: String): String => Boolean = {
  testName => testName startsWith rootPackage
}
