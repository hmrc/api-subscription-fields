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

import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.apisubscriptionfields.model.ErrorCode._
import uk.gov.hmrc.apisubscriptionfields.model.JsErrorResponse
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait CommonController extends BaseController {

  override protected def withJsonBody[T]
  (f: (T) => Future[Result])(implicit request: Request[JsValue], m: Manifest[T], reads: Reads[T]): Future[Result] = {
    Try(request.body.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(JsError(errs)) => jsonError(JsError.toJson(errs).toString())
      case Failure(e) => jsonError(e.getMessage)
    }
  }

  private def jsonError(errorText: String) = {
    Logger.error(s"A JSON error occurred: $errorText")
    Future.successful(UnprocessableEntity(JsErrorResponse(INVALID_REQUEST_PAYLOAD, "A JSON error occurred")))
  }

  def recovery: PartialFunction[Throwable, Result] = {
    case e => handleException(e)
  }

  private[controller] def handleException(e: Throwable) = {
    Logger.error(s"An unexpected error occurred: ${e.getMessage}", e)
    InternalServerError(JsErrorResponse(UNKNOWN_ERROR, "An unexpected error occurred"))
  }
}
