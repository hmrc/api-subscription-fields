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

package uk.gov.hmrc.apisubscriptionfields.controller

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.apiplatform.modules.common.domain.models._

import uk.gov.hmrc.apisubscriptionfields.model.ErrorCode._
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.service.SubscriptionFieldsService

@Singleton
class SubscriptionFieldsController @Inject() (cc: ControllerComponents, service: SubscriptionFieldsService)(implicit ec: ExecutionContext) extends CommonController {

  import JsonFormatters._

  private def notFoundResponse(message: String) = {
    NotFound(JsErrorResponse(ErrorCode.NOT_FOUND, message))
  }

  private def notFoundMessage(clientId: ClientId, apiContext: ApiContext, apiVersionNbr: ApiVersionNbr): String = {
    s"Subscription fields not found for (${clientId.value}, ${apiContext.value}, ${apiVersionNbr.value})"
  }

  private def fieldIdNotFoundMessage(subscriptionFieldsId: SubscriptionFieldsId): String = {
    s"FieldsId (${subscriptionFieldsId.value.toString}) was not found"
  }

  private def clientIdNotFoundMessage(clientId: ClientId): String = {
    s"ClientId (${clientId.value.toString}) was not found"
  }

  def getSubscriptionFields(clientId: ClientId, apiContext: ApiContext, apiVersionNbr: ApiVersionNbr): Action[AnyContent] = Action.async { _ =>
    val eventualMaybeResponse = service.get(clientId, apiContext, apiVersionNbr)
    asActionResult(eventualMaybeResponse, notFoundMessage(clientId, apiContext, apiVersionNbr))
  }

  def getSubscriptionFieldsByFieldsId(subscriptionFieldsId: SubscriptionFieldsId): Action[AnyContent] = Action.async { _ =>
    val eventualMaybeResponse = service.getBySubscriptionFieldId(subscriptionFieldsId)
    asActionResult(eventualMaybeResponse, fieldIdNotFoundMessage(subscriptionFieldsId))
  }

  def getBulkSubscriptionFieldsByClientId(clientId: ClientId): Action[AnyContent] = Action.async { _ =>
    val eventualMaybeResponse = service.getByClientId(clientId)
    asBulkActionResult(eventualMaybeResponse, clientIdNotFoundMessage(clientId))
  }

  def getAllSubscriptionFields: Action[AnyContent] = Action.async { _ =>
    service.getAll map (fields => Ok(Json.toJson(fields))) recover recovery
  }

  private def asActionResult[T](eventualMaybeResponse: Future[Option[T]], notFoundMessage: String)(implicit writes: Writes[T]) = {
    eventualMaybeResponse map {
      case Some(payload) => Ok(Json.toJson(payload))
      case None          => notFoundResponse(notFoundMessage)
    } recover recovery
  }

  private def asBulkActionResult(eventualMaybeResponse: Future[Option[BulkSubscriptionFieldsResponse]], notFoundMessage: String) = {
    eventualMaybeResponse map {
      case Some(subscriptionFields) => Ok(Json.toJson(subscriptionFields))
      case None                     => notFoundResponse(notFoundMessage)
    } recover recovery
  }

  def upsertSubscriptionFields(clientId: ClientId, apiContext: ApiContext, apiVersionNbr: ApiVersionNbr): Action[JsValue] = Action.async(parse.json) { implicit request =>
    import JsonFormatters._

    withJsonBody[SubscriptionFieldsRequest] { payload =>
      if (payload.fields.isEmpty) {
        Future.successful(UnprocessableEntity(JsErrorResponse(INVALID_REQUEST_PAYLOAD, "At least one field must be specified")))
      } else {
        service
          .upsert(clientId, apiContext, apiVersionNbr, payload.fields)
          .map(_ match {
            case NotFoundSubsFieldsUpsertResponse                             => BadRequest(Json.toJson("reason" -> "field definitions not found")) // TODO
            case FailedValidationSubsFieldsUpsertResponse(fieldErrorMessages) => BadRequest(Json.toJson(fieldErrorMessages))
            case SuccessfulSubsFieldsUpsertResponse(response, true)           => Created(Json.toJson(response))
            case SuccessfulSubsFieldsUpsertResponse(response, false)          => Ok(Json.toJson(response))
          })
          .recover(recovery)
      }
    }
  }

  def deleteSubscriptionFields(clientId: ClientId, apiContext: ApiContext, apiVersionNbr: ApiVersionNbr): Action[AnyContent] = Action.async { _ =>
    service.delete(clientId, apiContext, apiVersionNbr) map {
      case true  => NoContent
      case false => notFoundResponse(notFoundMessage(clientId, apiContext, apiVersionNbr))
    } recover recovery
  }

  def deleteAllSubscriptionFieldsForClient(clientId: ClientId): Action[AnyContent] = Action.async { _ =>
    service.delete(clientId) map {
      case true  => NoContent
      case false => notFoundResponse(clientIdNotFoundMessage(clientId))
    } recover recovery
  }

  override protected def controllerComponents: ControllerComponents = cc

}
