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
import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApiContext, ApiVersionNbr, ClientId}
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apisubscriptionfields.connector.PushPullNotificationServiceConnector
import uk.gov.hmrc.apisubscriptionfields.model.Types.FieldValue
import uk.gov.hmrc.apisubscriptionfields.model.{FieldDefinition, _}

@Singleton
class PushPullNotificationService @Inject() (ppnsConnector: PushPullNotificationServiceConnector)(implicit ec: ExecutionContext) {

  def makeBoxName(apiContext: ApiContext, apiVersion: ApiVersionNbr, fieldDefinition: FieldDefinition): String = {
    val separator = "##"
    s"${apiContext.value}${separator}${apiVersion.value}${separator}${fieldDefinition.name.value}"
  }

  def subscribeToPPNS(
      clientId: ClientId,
      apiContext: ApiContext,
      apiVersion: ApiVersionNbr,
      oFieldValue: Option[FieldValue],
      fieldDefinition: FieldDefinition
    )(implicit
      hc: HeaderCarrier
    ): Future[PPNSCallBackUrlValidationResponse] = {
    for {
      boxId  <- ppnsConnector.ensureBoxIsCreated(makeBoxName(apiContext, apiVersion, fieldDefinition), clientId)
      result <- oFieldValue match {
                  case Some(value) => ppnsConnector.updateCallBackUrl(clientId, boxId, value)
                  case None        => Future.successful(PPNSCallBackUrlSuccessResponse)
                }
    } yield result
  }

}
