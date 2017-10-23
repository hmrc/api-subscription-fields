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

import javax.inject.{Inject, Singleton}

import play.api.Logger
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
    NotFound(JsErrorResponse(ErrorCode.NOT_FOUND_CODE, s"Id ($rawApiContext, $rawApiVersion) was not found"))

  def upsertFieldsDefinition(rawApiContext: String, rawApiVersion: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[FieldsDefinitionRequest] { payload =>
      Logger.debug(s"[upsertFieldsDefinition] apiContext: $rawApiContext apiVersion: $rawApiVersion")
      service.upsert(FieldsDefinitionIdentifier(ApiContext(rawApiContext), ApiVersion(rawApiVersion)), payload.fieldDefinitions) map {
        case true => Created
        case false => Ok
      } recover {
        case e: Exception => BadRequest(e.getMessage)
      }
    } recover recovery
  }

  def getAllFieldsDefinitions: Action[AnyContent] = Action.async { implicit request =>
    // TODO
    Future.successful(notFoundResponse("TODO", "TODO"))
  }

  def getFieldsDefinition(rawApiContext: String, rawApiVersion: String): Action[AnyContent] = Action.async { implicit request =>
    Logger.debug(s"[getFieldsDefinition] apiContext: $rawApiContext apiVersion: $rawApiVersion")
    val eventualMaybeResponse = service.get(FieldsDefinitionIdentifier(ApiContext(rawApiContext), ApiVersion(rawApiVersion)))
    asActionResult(eventualMaybeResponse, rawApiContext, rawApiVersion)
  }

  private def asActionResult(eventualMaybeResponse: Future[Option[FieldsDefinitionResponse]], rawApiContext: String, rawApiVersion: String) = {
    eventualMaybeResponse map {
      case Some(subscriptionFields) => Ok(Json.toJson(subscriptionFields))
      case None => notFoundResponse(rawApiContext, rawApiVersion)
    } recover recovery
  }
}
