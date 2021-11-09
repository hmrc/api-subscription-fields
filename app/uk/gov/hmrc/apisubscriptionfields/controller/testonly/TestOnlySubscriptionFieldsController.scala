/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.apisubscriptionfields.controller.testonly

import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.apisubscriptionfields.controller.{CommonController, SubscriptionFieldsController, SubscriptionFieldsRequest}
import uk.gov.hmrc.apisubscriptionfields.model.Types.Fields
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository.SubscriptionFieldsRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestOnlySubscriptionFieldsController @Inject()(cc: ControllerComponents,
                                                     repository: SubscriptionFieldsRepository)
                                                    (implicit ec: ExecutionContext) extends CommonController {

  def upsertSubscriptionFields(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion,
                               fieldsId: SubscriptionFieldsId): Action[JsValue] = Action.async(parse.json) { implicit request =>
    import JsonFormatters._
    withJsonBody[SubscriptionFieldsRequest] { payload =>
      for {
        upserted <- upsertSubscriptionFields(clientId, apiContext, apiVersion, payload.fields, fieldsId)
        response = upserted match {
          case SuccessfulSubsFieldsUpsertResponse(response, true)  => Created(Json.toJson(response))
          case SuccessfulSubsFieldsUpsertResponse(response, false) => Ok(Json.toJson(response))
        }
      } yield response
    }
  }

  private def upsertSubscriptionFields(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion,
                                       fields: Fields, fieldsId: SubscriptionFieldsId): Future[SuccessfulSubsFieldsUpsertResponse] = {
    val subscriptionFields = SubscriptionFields(clientId, apiContext, apiVersion, fieldsId, fields)

    repository.saveAtomic(subscriptionFields)
      .map { case (subsFields, isInsert) =>
        SuccessfulSubsFieldsUpsertResponse(subsFields, isInsert)
      }
  }

  protected def controllerComponents: ControllerComponents = cc
}
