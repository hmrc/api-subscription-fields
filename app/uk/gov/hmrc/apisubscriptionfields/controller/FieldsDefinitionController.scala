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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.service.FieldsDefinitionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class FieldsDefinitionController @Inject() (service: FieldsDefinitionService) extends CommonController {

  import JsonFormatters._

  private def notFoundResponse(rawApiContext: String, rawApiVersion: String) =
    NotFound(JsErrorResponse(ErrorCode.NOT_FOUND_CODE, s"Fields definition not found for ($rawApiContext, $rawApiVersion)"))

  def upsertFieldsDefinition(rawApiContext: String, rawApiVersion: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[FieldsDefinitionRequest] { payload =>
      // TODO: ensure that `fieldDefinitions` is not empty (at least one field definition must be defined)
      // TODO: ensure that each field definition has an allowed type and a non-empty name
      service.upsert(ApiContext(rawApiContext), ApiVersion(rawApiVersion), payload.fieldDefinitions) map {
        case (response, true) => Created(Json.toJson(response))
        case (response, false) => Ok(Json.toJson(response))
      }
    } recover recovery
  }

  def getAllFieldsDefinitions: Action[AnyContent] = Action.async { implicit request =>
    service.getAll map (defs => Ok(Json.toJson(defs))) recover recovery
  }

  def getFieldsDefinition(rawApiContext: String, rawApiVersion: String): Action[AnyContent] = Action.async { implicit request =>
    val eventualMaybeResponse = service.get(ApiContext(rawApiContext), ApiVersion(rawApiVersion))
    asActionResult(eventualMaybeResponse, rawApiContext, rawApiVersion)
  }

  def deleteFieldsDefinition(rawApiContext: String, rawApiVersion: String): Action[AnyContent] = Action.async { implicit request =>
    service.delete(ApiContext(rawApiContext), ApiVersion(rawApiVersion)) map {
      case true => NoContent
      case false => notFoundResponse(rawApiContext, rawApiVersion)
    } recover recovery
  }

  private def asActionResult(eventualMaybeResponse: Future[Option[FieldsDefinitionResponse]], rawApiContext: String, rawApiVersion: String) = {
    eventualMaybeResponse map {
      case Some(subscriptionFields) => Ok(Json.toJson(subscriptionFields))
      case None => notFoundResponse(rawApiContext, rawApiVersion)
    } recover recovery
  }
}
