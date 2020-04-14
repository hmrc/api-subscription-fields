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

import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test.{FakeRequest, StubControllerComponentsFactory}
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.SubscriptionFieldsTestData
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.service.SubscriptionFieldsService
import uk.gov.hmrc.apisubscriptionfields.HmrcPlaySpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import akka.stream.Materializer
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

class SubscriptionFieldsControllerPutSpec
    extends HmrcPlaySpec
  with SubscriptionFieldsTestData
  with MockFactory
  with JsonFormatters
  with StubControllerComponentsFactory {

  private val mockSubscriptionFieldsService = mock[SubscriptionFieldsService]
  private val controller = new SubscriptionFieldsController(stubControllerComponents(), mockSubscriptionFieldsService)
  implicit private val actorSystem = ActorSystem("test")
  implicit private val mat: Materializer = ActorMaterializer.create(actorSystem)

  "PUT /field/application/:clientId/context/:apiContext/version/:apiVersion" should {
    "return CREATED when Field values are Valid and created in the repo" in {
      (mockSubscriptionFieldsService.validate(_: ApiContext, _: ApiVersion, _: Fields)).
        expects(FakeContext, FakeVersion, subscriptionFields).
        returns(Future.successful(FakeValidSubsFieldValidationResponse))

      (mockSubscriptionFieldsService.upsert(_: ClientId, _: ApiContext, _: ApiVersion, _: Fields)).
        expects(FakeClientId, FakeContext, FakeVersion, subscriptionFields).
        returns(Future.successful((FakeSubscriptionFieldsResponse, true)))

      val json = mkJson(SubscriptionFieldsRequest(subscriptionFields))
      testSubmitResult(mkRequest(json)) { result =>
        status(result) shouldBe CREATED
      }
    }

    "return OK when Field values are Valid and updated in the repo" in {
      (mockSubscriptionFieldsService.validate(_: ApiContext, _: ApiVersion, _: Fields)).
        expects(FakeContext, FakeVersion, subscriptionFields).
        returns(Future.successful(FakeValidSubsFieldValidationResponse))

      (mockSubscriptionFieldsService.upsert (_: ClientId, _: ApiContext, _: ApiVersion, _: Fields)).
        expects(FakeClientId, FakeContext, FakeVersion, subscriptionFields).
        returns(Future.successful((FakeSubscriptionFieldsResponse, false)))

      val json = mkJson(SubscriptionFieldsRequest(subscriptionFields))
      testSubmitResult(mkRequest(json)) { result =>
        status(result) shouldBe OK
      }
    }

    "return OK with FieldErrorMessages when Field values are Invalid" in {
      (mockSubscriptionFieldsService.validate(_: ApiContext, _: ApiVersion, _: Fields)).
        expects(FakeContext, FakeVersion, subscriptionFields).
        returns(Future.successful(FakeInvalidSubsFieldValidationResponse))

      val json = mkJson(SubscriptionFieldsRequest(subscriptionFields))
      testSubmitResult(mkRequest(json)) { result =>
        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe (Json.toJson(FakeFieldErrorMessages))
      }
    }

    "return UnprocessableEntity when no Fields are specified" in {
      val json = mkJson(SubscriptionFieldsRequest(Map.empty))
      testSubmitResult(mkRequest(json)) { result =>
        status(result) shouldBe UNPROCESSABLE_ENTITY
        val errorPayload = JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, "At least one field must be specified")
        contentAsJson(result) shouldBe (Json.toJson(errorPayload))
      }
    }
  }

  private def testSubmitResult(request: Request[JsValue])(test: Future[Result] => Unit) {
    val action: Action[JsValue] = controller.upsertSubscriptionFields(fakeRawClientId, fakeRawContext, fakeRawVersion)
    val result: Future[Result] = action.apply(request)
    test(result)
  }

  private def mkRequest(jsonBody: JsValue): Request[JsValue] =
    FakeRequest()
      .withJsonBody(jsonBody).map(r => r.json)

  private def mkJson(model: SubscriptionFieldsRequest) = Json.toJson(model)(Json.writes[SubscriptionFieldsRequest])
}
