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

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.apiplatform.modules.common.domain.models._
import uk.gov.hmrc.apiplatform.modules.subscriptionfields.interface.models.FieldDefinitionsRequest

import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.service.ApiFieldDefinitionsService
import uk.gov.hmrc.apisubscriptionfields.utils.ApplicationLogger

@Singleton
class ApiFieldDefinitionsController @Inject() (cc: ControllerComponents, service: ApiFieldDefinitionsService)(implicit ec: ExecutionContext)
    extends CommonController
    with ApplicationLogger {

  import JsonFormatters._

  private def badRequestWithTag(fn: (UUID) => String): Result = {
    val errorTag = java.util.UUID.randomUUID()
    appLogger.warn(fn(errorTag))
    BadRequest(s"""{"tag": "$errorTag"}""")
  }

  private def notFoundResponse(apiContext: ApiContext, apiVersionNbr: ApiVersionNbr) =
    NotFound(JsErrorResponse(ErrorCode.NOT_FOUND, s"Fields definition not found for (${apiContext.value}, ${apiVersionNbr.value})"))

  def validateFieldsDefinition(): Action[JsValue] = Action(parse.json) { request =>
    Try(request.body.validate[FieldDefinitionsRequest]) match {
      case Success(JsSuccess(payload, _)) => Ok("")
      case Success(JsError(errs))         => {
        badRequestWithTag((tag: UUID) => s"A JSON error occurred: [${tag.toString}] ${Json.prettyPrint(JsError.toJson(errs))}")
      }
      case Failure(e)                     => {
        badRequestWithTag { (tag: UUID) => s"An error occurred during JSON validation: [${tag.toString}] ${e.getMessage}" }
      }
    }
  }

  def upsertFieldsDefinition(apiContext: ApiContext, apiVersionNbr: ApiVersionNbr): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[FieldDefinitionsRequest] { payload =>
      service.upsert(apiContext, apiVersionNbr, payload.fieldDefinitions) map {
        case (response, true)  => Created(Json.toJson(response))
        case (response, false) => Ok(Json.toJson(response))
      }
    } recover recovery
  }

  def getAllFieldsDefinitions: Action[AnyContent] = Action.async { _ =>
    service.getAll() map (defs => Ok(Json.toJson(defs))) recover recovery
  }

  def getFieldsDefinition(apiContext: ApiContext, apiVersionNbr: ApiVersionNbr): Action[AnyContent] = Action.async { _ =>
    val eventualMaybeResponse = service.get(apiContext, apiVersionNbr)
    asActionResult(eventualMaybeResponse, apiContext, apiVersionNbr)
  }

  def deleteFieldsDefinition(apiContext: ApiContext, apiVersionNbr: ApiVersionNbr): Action[AnyContent] = Action.async { _ =>
    service.delete(apiContext, apiVersionNbr) map {
      case true  => NoContent
      case false => notFoundResponse(apiContext, apiVersionNbr)
    } recover recovery
  }

  private def asActionResult(eventualMaybeResponse: Future[Option[ApiFieldDefinitions]], apiContext: ApiContext, apiVersionNbr: ApiVersionNbr) = {
    eventualMaybeResponse map {
      case Some(subscriptionFields) => Ok(Json.toJson(subscriptionFields))
      case None                     => notFoundResponse(apiContext, apiVersionNbr)
    } recover recovery
  }

  override protected def controllerComponents: ControllerComponents = cc
}
