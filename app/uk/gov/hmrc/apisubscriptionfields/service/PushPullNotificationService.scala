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


import uk.gov.hmrc.apisubscriptionfields.connector.PushPullNotificationServiceConnector
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.apisubscriptionfields.model.FieldDefinition
import uk.gov.hmrc.apisubscriptionfields.model.Types.FieldValue

import scala.concurrent.Future
import uk.gov.hmrc.apisubscriptionfields.model._

import scala.concurrent.ExecutionContext
import uk.gov.hmrc.http.HeaderCarrier

@Singleton
class PushPullNotificationService @Inject()(ppnsConnector: PushPullNotificationServiceConnector)(implicit ec: ExecutionContext) {
  def makeBoxName(apiContext: ApiContext, apiVersion: ApiVersion, fieldDefinition: FieldDefinition) : String = {
    val separator = "##"
    s"${apiContext.value}${separator}${apiVersion.value}${separator}${fieldDefinition.name.value}"
  }

  def subscribeToPPNS(clientId: ClientId,
                              apiContext: ApiContext,
                              apiVersion: ApiVersion,
                              oFieldValue: Option[FieldValue],
                              fieldDefinition: FieldDefinition)
                             (implicit hc: HeaderCarrier): Future[PPNSCallBackUrlValidationResponse] = {
    for {
      boxId <- ppnsConnector.ensureBoxIsCreated(makeBoxName(apiContext, apiVersion, fieldDefinition), clientId)
      result <- oFieldValue match {
        case Some(value) => ppnsConnector.updateCallBackUrl (clientId, boxId, value)
        case None => Future.successful(PPNSCallBackUrlSuccessResponse)
      }
    } yield result
  }

}
