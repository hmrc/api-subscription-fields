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
import bloop.integrations.sbt.BloopDefaults

import scala.language.postfixOps

val appName = "api-subscription-fields"

scalaVersion := "2.13.12"

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val plugins: Seq[Plugins] = Seq(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val AcceptanceTest = config("acceptance") extend Test

lazy val acceptanceTestSettings =
  inConfig(AcceptanceTest)(Defaults.testSettings) ++
  inConfig(AcceptanceTest)(BloopDefaults.configSettings) ++
    Seq(
      AcceptanceTest / unmanagedSourceDirectories := Seq(baseDirectory.value / "acceptance"),
      AcceptanceTest / fork := false,
      AcceptanceTest / parallelExecution := false,
      addTestReportOption(AcceptanceTest, "acceptance-reports")
    )

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins: _*)
  .configs(AcceptanceTest)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(acceptanceTestSettings: _*)
  .settings(headerSettings(AcceptanceTest) ++ automateHeaderSettings(AcceptanceTest))
  .settings(ScoverageSettings())
  .settings(
    routesImport ++= Seq(
      "uk.gov.hmrc.apisubscriptionfields.model._",
      "uk.gov.hmrc.apisubscriptionfields.controller.Binders._"
    )
  )
  .settings(
    libraryDependencies ++= AppDependencies(),
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(
    Test / fork := false,
    addTestReportOption(Test, "test-reports"),
    Test / parallelExecution := false
  )
  .settings(majorVersion := 0)
  .settings(
    scalacOptions ++= Seq(
    "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
    "-Wconf:cat=unused&src=.*Routes\\.scala:s",
    "-Wconf:cat=unused&src=.*ReverseRoutes\\.scala:s",
    "-Xlint:-byname-implicit"
    )
  )
  .settings(
    commands += Command.command("testAll") { state => "test" :: "acceptance:test" :: state }
  )

def onPackageName(rootPackage: String): String => Boolean = { testName => testName startsWith rootPackage }

// Note that this task has to be scoped globally
Global / bloopAggregateSourceDependencies := true

commands ++= Seq(
  Command.command("run-all-tests") { state => "test" :: "acceptance:test" :: state },
  Command.command("clean-and-test") { state => "clean" :: "compile" :: "run-all-tests" :: state },
  // Coverage does not need compile !
  Command.command("pre-commit") { state => "clean" :: "scalafmtAll" :: "scalafixAll" :: "coverage" ::  "run-all-tests" :: "coverageOff" :: "coverageReport" :: state }
)
