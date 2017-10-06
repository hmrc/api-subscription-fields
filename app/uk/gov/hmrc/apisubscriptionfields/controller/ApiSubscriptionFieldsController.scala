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

package uk.gov.hmrc.apisubscriptionfields.controller

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.apisubscriptionfields.model.ErrorCode._
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.service.SubscriptionFieldsService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ApiSubscriptionFieldsController @Inject() (service: SubscriptionFieldsService) extends CommonController {

  import JsonFormatters._

  private val NOT_FOUND_RESPONSE = NotFound(JsErrorResponse(SUBSCRIPTION_FIELDS_ID_NOT_FOUND, "Subscription Fields were not found"))

  def getSubscriptionFields(rawAppId: UUID, rawApiContext: String, rawApiVersion: String): Action[AnyContent] = Action.async { implicit request =>
    val eventualMaybeResponse = service.get(SubscriptionIdentifier(AppId(rawAppId), ApiContext(rawApiContext), ApiVersion(rawApiVersion)))
    asActionResult(eventualMaybeResponse)
  }

  def getSubscriptionFieldsByFieldsId(rawFieldsId: UUID): Action[AnyContent] = Action.async { implicit request =>
    val eventualMaybeResponse = service.get(SubscriptionFieldsId(rawFieldsId))
    asActionResult(eventualMaybeResponse)
  }

  private def asActionResult(eventualMaybeResponse: Future[Option[SubscriptionFieldsResponse]]) = {
    eventualMaybeResponse map {
      case Some(subscriptionFields) => Ok(Json.toJson(subscriptionFields))
      case None => NOT_FOUND_RESPONSE
    } recover recovery
  }

  def upsertSubscriptionFields(rawAppId: UUID, rawApiContext: String, rawApiVersion: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
      withJsonBody[SubscriptionFieldsRequest] { payload =>
        service.upsert(SubscriptionIdentifier(AppId(rawAppId), ApiContext(rawApiContext), ApiVersion(rawApiVersion)), payload.fields) map {
          case (response, true) => Created(Json.toJson(response))
          case (response, false) => Ok(Json.toJson(response))
        } recover {
          case e: Exception => BadRequest(e.getMessage)
        }
      } recover recovery
    }

  def deleteSubscriptionFields(rawAppId: UUID, rawApiContext: String, rawApiVersion: String): Action[AnyContent] = Action.async { implicit request =>
    val identifier = SubscriptionIdentifier(AppId(rawAppId), ApiContext(rawApiContext), ApiVersion(rawApiVersion))
    //TODO remove service.get
    service.get(identifier) flatMap {
      case None => Future.successful(NOT_FOUND_RESPONSE)
      case Some(_) => service.delete(identifier).map(deleted => if (deleted) NoContent else NOT_FOUND_RESPONSE)
    } recover recovery
  }

}
