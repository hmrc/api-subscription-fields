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

import play.core.PlayVersion
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
  "uk.gov.hmrc" %% "bootstrap-play-26" % "1.4.0",
  "uk.gov.hmrc" %% "simple-reactivemongo" % "7.22.0-play-26"
)

val jettyVersion = "9.2.24.v20180105"
// we need to override the akka version for now as newer versions are not compatible with reactivemongo
lazy val akkaVersion = "2.5.23"
lazy val akkaHttpVersion = "10.0.15"

val overrides: Seq[ModuleID] = Seq(
//  "org.reactivemongo" %% "reactivemongo" % "0.16.6",
  "org.eclipse.jetty" % "jetty-server" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty" % "jetty-servlet" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty" % "jetty-security" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty" % "jetty-servlets" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty" % "jetty-continuation" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty" % "jetty-xml" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty" % "jetty-client" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty" % "jetty-http" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty" % "jetty-io" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty" % "jetty-util" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty.websocket" % "websocket-api" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty.websocket" % "websocket-common" % jettyVersion % AcceptanceTest,
  "org.eclipse.jetty.websocket" % "websocket-client" % jettyVersion % AcceptanceTest,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-protobuf" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
)

def test(scope: String = "test,acceptance") = Seq(
  "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-26" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "4.16.0-play-26" % scope,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope,
  "org.scalatest" %% "scalatest" % "3.0.5" % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
  "org.pegdown" % "pegdown" % "1.6.0" % scope,
  "com.github.tomakehurst" % "wiremock" % "2.21.0" % scope,
  "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % scope,
  "org.mockito" % "mockito-core" % "1.10.19" % "test"
)

//lazy val hmrcBootstrapPlay26Version = "1.3.0"
//lazy val hmrcSimpleReactivemongoVersion = "7.22.0-play-26"
//lazy val hmrcHttpMetricsVersion = "1.6.0-play-26"
//lazy val hmrcReactiveMongoTestVersion = "4.16.0-play-26"
//lazy val hmrcTestVersion = "3.9.0-play-26"
//lazy val scalaJVersion = "2.4.1"
//lazy val scalatestPlusPlayVersion = "3.1.2"
//lazy val mockitoVersion = "1.10.19"
//lazy val wireMockVersion = "2.21.0"
//lazy val test = Seq(
////  "uk.gov.hmrc" %% "bootstrap-play-26" % hmrcBootstrapPlay26Version % "test,it" classifier "tests",
//  "uk.gov.hmrc" %% "reactivemongo-test" % hmrcReactiveMongoTestVersion % "test,it",
//  "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
//  "org.scalaj" %% "scalaj-http" % scalaJVersion % scope,
//  "org.scalatest" %% "scalatest" % "3.0.5" % scope,
//  "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
//  "org.mockito" % "mockito-core" % mockitoVersion % scope,
//  "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
//  "com.github.tomakehurst" % "wiremock" % wireMockVersion % scope
//)

val appName = "api-subscription-fields"

lazy val appDependencies: Seq[ModuleID] = compile ++ overrides ++ test()

resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo)

lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val AcceptanceTest = config("acceptance") extend Test

val testConfig = Seq(AcceptanceTest, Test)

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
  .settings(majorVersion := 0)

lazy val acceptanceTestSettings =
  inConfig(AcceptanceTest)(Defaults.testSettings) ++
    Seq(
      testOptions in AcceptanceTest := Seq(Tests.Filter(acceptanceFilter)),
      unmanagedSourceDirectories in AcceptanceTest := Seq(
        baseDirectory.value / "test" / "unit",
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

def unitFilter(name: String): Boolean = !acceptanceFilter(name)
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
