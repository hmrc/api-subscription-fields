/*
 * Copyright 2023 HM Revenue & Customs
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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.Configuration

@ImplementedBy(classOf[ApplicationConfigImpl])
trait ApplicationConfig {
  def pushPullNotificationServiceURL: String
}

@Singleton
class ApplicationConfigImpl @Inject() (config: Configuration) extends ApplicationConfig {
  private val HOCON = config.underlying

  // Moving away from complex layers configurations
  val pushPullNotificationServiceURL = HOCON.getString("microservice.services.push-pull-notification.uri")
}
