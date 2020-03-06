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

import sbt.Keys.{parallelExecution, _}
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

// we need to override the akka version for now as newer versions are not compatible with reactivemongo
lazy val akkaVersion = "2.5.23"
lazy val akkaHttpVersion = "10.0.15"

val overrides: Seq[ModuleID] = Seq(
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
  "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % scope,
  "org.mockito" % "mockito-core" % "1.10.19" % "test"
)

val appName = "api-subscription-fields"

lazy val appDependencies: Seq[ModuleID] = compile  ++ test()

resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo)

lazy val plugins: Seq[Plugins] = Seq(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
lazy val playSettings: Seq[Setting[_]] = Seq.empty


lazy val AcceptanceTest = config("acceptance") extend Test

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins: _*)
  .configs(AcceptanceTest)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(acceptanceTestSettings: _*)
  .settings(scalaVersion := "2.12.10")
  .settings(
    libraryDependencies ++= appDependencies,
    dependencyOverrides ++= overrides,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(scoverageSettings)
  .settings(
    fork in Test := false,
    addTestReportOption(Test, "test-reports"),
    parallelExecution in Test := false
  )
  .settings(majorVersion := 0)

lazy val acceptanceTestSettings =
  inConfig(AcceptanceTest)(Defaults.testSettings) ++
    Seq(
      unmanagedSourceDirectories in AcceptanceTest := Seq(baseDirectory.value / "acceptance"),
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



def onPackageName(rootPackage: String): String => Boolean = {
  testName => testName startsWith rootPackage
}
