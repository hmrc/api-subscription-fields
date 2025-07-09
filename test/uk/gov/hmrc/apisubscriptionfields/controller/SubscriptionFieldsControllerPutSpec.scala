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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

import org.apache.pekko.actor.ActorSystem

import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{FakeRequest, StubControllerComponentsFactory}
import uk.gov.hmrc.apiplatform.modules.subscriptionfields.interface.models.UpsertFieldValuesRequest

import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.service.SubscriptionFieldsService
import uk.gov.hmrc.apisubscriptionfields.{AsyncHmrcSpec, SubscriptionFieldsTestData}

class SubscriptionFieldsControllerPutSpec extends AsyncHmrcSpec with SubscriptionFieldsTestData with JsonFormatters with StubControllerComponentsFactory {

  private val mockSubscriptionFieldsService     = mock[SubscriptionFieldsService]
  private val controller                        = new SubscriptionFieldsController(stubControllerComponents(), mockSubscriptionFieldsService)
  implicit private val actorSystem: ActorSystem = ActorSystem("test")

  def subsFieldServiceUpsertReturns(response: SubsFieldsUpsertResponse) = {
    when(mockSubscriptionFieldsService.upsert(eqTo(FakeClientId), eqTo(FakeContext), eqTo(FakeVersion), eqTo(FakeSubscriptionFields))(*)).thenReturn(successful(response))
  }

  "PUT /field/application/:clientId/context/:apiContext/version/:apiVersionNbr" should {
    "return CREATED when Field values are Valid and created in the repo" in {
      subsFieldServiceUpsertReturns(SuccessfulSubsFieldsUpsertResponse(FakeSubscriptionFieldsResponse, true))

      val json = mkJson(UpsertFieldValuesRequest(FakeSubscriptionFields))
      testSubmitResult(mkRequest(json)) { result =>
        status(result) shouldBe CREATED
      }
    }

    "return OK when Field values are Valid and updated in the repo" in {
      subsFieldServiceUpsertReturns(SuccessfulSubsFieldsUpsertResponse(FakeSubscriptionFieldsResponse, false))

      val json = mkJson(UpsertFieldValuesRequest(FakeSubscriptionFields))
      testSubmitResult(mkRequest(json)) { result =>
        status(result) shouldBe OK
      }
    }

    "return BadRequest with FieldErrorMessages when Field values are Invalid" in {
      subsFieldServiceUpsertReturns(FailedValidationSubsFieldsUpsertResponse(FakeFieldErrorMessages))

      val json = mkJson(UpsertFieldValuesRequest(FakeSubscriptionFields))
      testSubmitResult(mkRequest(json)) { result =>
        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe (Json.toJson(FakeFieldErrorMessages))
      }
    }

    "return BadRequest when api does not have subs fields" in {
      subsFieldServiceUpsertReturns(NotFoundSubsFieldsUpsertResponse)

      val json = mkJson(UpsertFieldValuesRequest(FakeSubscriptionFields))
      testSubmitResult(mkRequest(json)) { result =>
        status(result) shouldBe BAD_REQUEST
      }
    }

    "return UnprocessableEntity when no Fields are specified" in {
      val json = mkJson(UpsertFieldValuesRequest(Map.empty))

      testSubmitResult(mkRequest(json)) { result =>
        status(result) shouldBe UNPROCESSABLE_ENTITY
        val errorPayload = JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, "At least one field must be specified")
        contentAsJson(result) shouldBe (Json.toJson(errorPayload))
      }
    }
  }

  private def testSubmitResult(request: Request[JsValue])(test: Future[Result] => Unit): Unit = {
    val action: Action[JsValue] = controller.upsertSubscriptionFields(FakeClientId, FakeContext, FakeVersion)
    val result: Future[Result]  = action.apply(request)
    test(result)
  }

  private def mkRequest(jsonBody: JsValue): Request[JsValue] =
    FakeRequest()
      .withJsonBody(jsonBody)
      .map(r => r.json)

  private def mkJson(model: UpsertFieldValuesRequest) = Json.toJson(model)(Json.writes[UpsertFieldValuesRequest])
}
