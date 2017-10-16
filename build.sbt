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
  "uk.gov.hmrc" %% "play-reactivemongo" % "5.2.0",
  ws,
  "uk.gov.hmrc" %% "microservice-bootstrap" % "6.9.0"
)

def test(scope: String = "test,it") = Seq(
  "uk.gov.hmrc" %% "hmrctest" % "2.4.0" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "2.0.0" % scope,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope,
  "org.scalatest" %% "scalatest" % "2.2.6" % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
  "org.pegdown" % "pegdown" % "1.6.0" % scope,
  "com.github.tomakehurst" % "wiremock" % "2.2.2" % scope,
  "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % scope
)

val appName = "api-subscription-fields"

lazy val appDependencies: Seq[ModuleID] = compile ++ test()

resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo)

lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val AcceptanceTest = config("acceptance") extend Test
lazy val AsfIntegrationTest = config("it") extend Test

val testConfig = Seq(AcceptanceTest, AsfIntegrationTest, Test)


lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin) ++ plugins: _*)
  .configs(testConfig: _*)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(unitTestSettings: _*)
  .settings(integrationTestSettings: _*)
  .settings(acceptanceTestSettings: _*)
  .settings(
    libraryDependencies ++= appDependencies,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(scoverageSettings)


lazy val unitTestSettings =
  inConfig(Test)(Defaults.testTasks) ++
    Seq(
      testOptions in Test := Seq(Tests.Filter(onPackageName("unit"))),
      unmanagedSourceDirectories in Test := Seq((baseDirectory in Test).value  / "test"),
      addTestReportOption(Test, "test-reports")
    )

lazy val integrationTestSettings =
  inConfig(AsfIntegrationTest)(Defaults.testTasks) ++
    Seq(
      testOptions in AsfIntegrationTest := Seq(Tests.Filter(onPackageName("integration"))),
      fork in AsfIntegrationTest := false,
      parallelExecution in AsfIntegrationTest := false,
      addTestReportOption(AsfIntegrationTest, "int-test-reports"),
      testGrouping in AsfIntegrationTest := oneForkedJvmPerTest((definedTests in Test).value)
    )

lazy val acceptanceTestSettings =
  inConfig(AcceptanceTest)(Defaults.testTasks) ++
    Seq(
      testOptions in AcceptanceTest := Seq(Tests.Filter(onPackageName("acceptance"))),
      fork in AcceptanceTest := false,
      parallelExecution in AcceptanceTest := false,
      addTestReportOption(AcceptanceTest, "acceptance-reports")
    )

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := "<empty>;Reverse.*;model.*;.*config.*;.*(AuthService|BuildInfo|Routes).*",
  coverageMinimum := 81,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  parallelExecution in Test := false
)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
  tests map {
    test => Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
  }

def forkedJvmPerTestConfig(tests: Seq[TestDefinition], packages: String*): Seq[Group] =
  tests.groupBy(_.name.takeWhile(_ != '.')).filter(packageAndTests => packages contains packageAndTests._1) map {
    case (packg, theTests) =>
      Group(packg, theTests, SubProcess(ForkOptions()))
  } toSeq


def onPackageName(rootPackage: String): (String => Boolean) = {
  testName => testName startsWith rootPackage
}
