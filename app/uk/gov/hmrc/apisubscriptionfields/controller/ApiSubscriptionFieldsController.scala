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

import play.api.mvc.Action
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

trait ApiSubscriptionFieldsController extends BaseController {

	def id() = Action.async { implicit request =>
    /*
     TODO:
       parse `ApiSubscriptionRequest`
       - [application_id]_[api_context]_[api_context] is the id of the mongo record, but decide good separators
       - generate UUID (the header to be passed in the incoming requests)
       - save record to mongo
       */
    val subscriptionFieldsId = UUID.randomUUID()
    Future.successful(Created(s"$subscriptionFieldsId"))
  }
}

object ApiSubscriptionFieldsController extends ApiSubscriptionFieldsController
