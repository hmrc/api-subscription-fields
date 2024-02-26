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

package uk.gov.hmrc.apisubscriptionfields.service

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

import uk.gov.hmrc.apiplatform.modules.common.domain.models._
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apisubscriptionfields.connector.PushPullNotificationServiceConnector
import uk.gov.hmrc.apisubscriptionfields.model.BoxId
import uk.gov.hmrc.apisubscriptionfields.model.Types.{FieldName, FieldValue}

@Singleton
class PushPullNotificationService @Inject() (ppnsConnector: PushPullNotificationServiceConnector) {

  def makeBoxName(apiContext: ApiContext, apiVersionNbr: ApiVersionNbr, fieldName: FieldName): String = {
    val separator = "##"
    s"${apiContext.value}${separator}${apiVersionNbr.value}${separator}${fieldName.value}"
  }

  def ensureBoxIsCreated(clientId: ClientId, apiContext: ApiContext, apiVersionNbr: ApiVersionNbr, fieldName: FieldName)(implicit hc: HeaderCarrier): Future[BoxId] = {
    ppnsConnector.ensureBoxIsCreated(makeBoxName(apiContext, apiVersionNbr, fieldName), clientId)
  }

  def updateCallbackUrl(clientId: ClientId, boxId: BoxId, fieldValue: FieldValue)(implicit hc: HeaderCarrier): Future[Either[String, Unit]] = {
    ppnsConnector.updateCallBackUrl(clientId, boxId, fieldValue)
  }
}
