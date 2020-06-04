/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.apisubscriptionfields.model.ErrorCode._
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.service.SubscriptionFieldsService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

@Singleton
class SubscriptionFieldsController @Inject()(cc: ControllerComponents, service: SubscriptionFieldsService)(implicit ec: ExecutionContext) extends CommonController {

  import JsonFormatters._

  private def notFoundResponse(message: String) = {
    NotFound(JsErrorResponse(NOT_FOUND_CODE, message))
  }

  private def notFoundMessage(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): String = {
    s"Subscription fields not found for (${clientId.value}, ${apiContext.value}, ${apiVersion.value})"
  }

  private def notFoundMessage(subscriptionFieldsId: SubscriptionFieldsId): String = {
    s"FieldsId (${subscriptionFieldsId.value.toString}) was not found"
  }

  private def notFoundMessage(clientId: ClientId): String = {
    s"ClientId (${clientId.value}) was not found"
  }

  def getSubscriptionFields(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Action[AnyContent] = Action.async { _ =>
    val eventualMaybeResponse = service.get(clientId, apiContext, apiVersion)
    asActionResult(eventualMaybeResponse, notFoundMessage(clientId, apiContext, apiVersion))
  }

  def getSubscriptionFieldsByFieldsId(subscriptionFieldsId: SubscriptionFieldsId): Action[AnyContent] = Action.async { _ =>
    val eventualMaybeResponse = service.get(subscriptionFieldsId)
    asActionResult(eventualMaybeResponse, notFoundMessage(subscriptionFieldsId))
  }

  def getBulkSubscriptionFieldsByClientId(clientId: ClientId): Action[AnyContent] = Action.async { _ =>
    val eventualMaybeResponse = service.get(clientId)
    asBulkActionResult(eventualMaybeResponse, notFoundMessage(clientId))
  }

  def getAllSubscriptionFields: Action[AnyContent] = Action.async { _ =>
    service.getAll map (fields => Ok(Json.toJson(fields))) recover recovery
  }

  private def asActionResult[T](eventualMaybeResponse: Future[Option[T]], notFoundMessage: String)(implicit writes: Writes[T]) = {
    eventualMaybeResponse map {
      case Some(payload) => Ok(Json.toJson(payload))
      case None => notFoundResponse(notFoundMessage)
    } recover recovery
  }

  private def asBulkActionResult(eventualMaybeResponse: Future[Option[BulkSubscriptionFieldsResponse]], notFoundMessage: String) = {
    eventualMaybeResponse map {
      case Some(subscriptionFields) => Ok(Json.toJson(subscriptionFields))
      case None => notFoundResponse(notFoundMessage)
    } recover recovery
  }

  def upsertSubscriptionFields(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Action[JsValue] = Action.async(parse.json) { implicit request =>
    import JsonFormatters._

    withJsonBody[SubscriptionFieldsRequest] { payload =>
      if(payload.fields.isEmpty) {
        Future.successful(UnprocessableEntity(JsErrorResponse(INVALID_REQUEST_PAYLOAD, "At least one field must be specified")))
      }
      else {
        service.validate(clientId, apiContext, apiVersion, payload.fields) flatMap {
            case ValidSubsFieldValidationResponse => {
              service.upsert(clientId, apiContext, apiVersion, payload.fields) map {
                case (response, true) => Created(Json.toJson(response))
                case (response, false) => Ok(Json.toJson(response))
              }
            }
            case InvalidSubsFieldValidationResponse(fieldErrorMessages) => Future.successful(BadRequest(Json.toJson(fieldErrorMessages)))
          }
        } recover recovery
      }
  }

  def deleteSubscriptionFields(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Action[AnyContent] = Action.async { _ =>
    service.delete(clientId, apiContext, apiVersion) map {
      case true => NoContent
      case false => notFoundResponse(notFoundMessage(clientId, apiContext, apiVersion))
    } recover recovery
  }

  def deleteAllSubscriptionFieldsForClient(clientId: ClientId): Action[AnyContent] = Action.async { _ =>
    service.delete(clientId) map {
      case true => NoContent
      case false => notFoundResponse(notFoundMessage(clientId))
    } recover recovery
  }

  override protected def controllerComponents: ControllerComponents = cc

}
