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

package uk.gov.hmrc.apisubscriptionfields.config

import javax.inject.Inject

import com.typesafe.config.Config
import uk.gov.hmrc.play.config.ServicesConfig

class AppContext @Inject()(val config: Config) extends ServicesConfig {

  lazy val isExternalTestEnvironment: Boolean = runModeConfiguration.getBoolean("isExternalTestEnvironment").getOrElse(false)
  lazy val devHubTitle: String = if (isExternalTestEnvironment) "Developer Sandbox" else "Developer Hub"

  lazy val fetchApplicationTtlInSecs : Int = getConfig("fetchApplicationTtlInSeconds", runModeConfiguration.getInt)
  lazy val fetchSubscriptionTtlInSecs : Int = getConfig("fetchSubscriptionTtlInSeconds", runModeConfiguration.getInt)

  lazy val publishApiDefinition = runModeConfiguration.getBoolean("publishApiDefinition").getOrElse(false)
  lazy val apiContext = runModeConfiguration.getString("api.context").getOrElse("api-subscription-fields")
  lazy val access = runModeConfiguration.getConfig(s"api.access")

  override def toString() = {
    "AppContext{" + (
      Seq(s"fetchApplicationTtlInSecs=$fetchApplicationTtlInSecs",
          s"fetchSubscriptionTtlInSecs=$fetchSubscriptionTtlInSecs",
          s"isExternalTestEnvironment=$isExternalTestEnvironment"
      ) mkString ",") +"}"
  }

  private def getConfig(key: String) = runModeConfiguration.getString(key)
    .getOrElse(throw new RuntimeException(s"[$key] is not configured!"))

  private def getConfig[T](key: String, f: String => Option[T]) = f(key)
    .getOrElse(throw new RuntimeException(s"[$key] is not configured!"))
}
