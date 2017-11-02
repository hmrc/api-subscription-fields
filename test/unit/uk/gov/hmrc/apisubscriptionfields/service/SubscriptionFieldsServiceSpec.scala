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

package unit.uk.gov.hmrc.apisubscriptionfields.service

import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository._
import uk.gov.hmrc.apisubscriptionfields.service.{SubscriptionFieldsService, UUIDCreator}
import uk.gov.hmrc.play.test.UnitSpec
import util.SubscriptionFieldsTestData

class SubscriptionFieldsServiceSpec extends UnitSpec with SubscriptionFieldsTestData with MockFactory {

  private val mockSubscriptionFieldsIdRepository = mock[SubscriptionFieldsRepository]
  private val mockUuidCreator = mock[UUIDCreator]
  private val service = new SubscriptionFieldsService(mockSubscriptionFieldsIdRepository, mockUuidCreator)

  "get by clientId" should {
    "return None when the expected record does not exist in the database collection" in {
      (mockSubscriptionFieldsIdRepository fetchByClientId _) expects FakeClientId returns List()

      val result = await(service.get(FakeClientId))

      result shouldBe None
    }

    "return the expected response when the entry exists in the database collection" in {
      val subscriptionFields1 = createSubscriptionFieldsWithApiContext()
      val subscriptionFields2 = createSubscriptionFieldsWithApiContext(rawContext = fakeRawContext2)
      (mockSubscriptionFieldsIdRepository fetchByClientId _) expects FakeClientId returns List(subscriptionFields1, subscriptionFields2)

      val result = await(service.get(FakeClientId))

      result shouldBe Some(BulkSubscriptionFieldsResponse(subscriptions = Seq(
        SubscriptionFieldsResponse(clientId = subscriptionFields1.clientId, apiVersion = subscriptionFields1.apiVersion, apiContext = subscriptionFields1.apiContext, fieldsId = SubscriptionFieldsId(subscriptionFields1.fieldsId), fields = subscriptionFields1.fields),
        SubscriptionFieldsResponse(clientId = subscriptionFields2.clientId, apiVersion = subscriptionFields2.apiVersion, apiContext = subscriptionFields2.apiContext, fieldsId = SubscriptionFieldsId(subscriptionFields2.fieldsId), fields = subscriptionFields2.fields)
      )))
    }
  }

  "get" should {
    "return None when no entry exists in the repo" in {
      (mockSubscriptionFieldsIdRepository fetch(_: ClientId, _: ApiContext, _: ApiVersion)) expects(FakeClientId, FakeContext, FakeVersion) returns None

      val result = await(service.get(FakeClientId, FakeContext, FakeVersion))

      result shouldBe None
    }

    "return the expected response when the entry exists in the database collection" in {
      (mockSubscriptionFieldsIdRepository fetch(_: ClientId, _: ApiContext, _: ApiVersion)) expects(FakeClientId, FakeContext, FakeVersion) returns Some(FakeApiSubscription)

      val result = await(service.get(FakeClientId, FakeContext, FakeVersion))

      result shouldBe Some(FakeSubscriptionFieldsResponse)
    }
  }

  "get by fieldsId" should {
    "return None when no entry exists in the repo" in {
      (mockSubscriptionFieldsIdRepository fetchByFieldsId _) expects SubscriptionFieldsId(FakeRawFieldsId) returns None

      val result = await(service.get(FakeFieldsId))

      result shouldBe None
    }

    "return the expected response when the entry exists in the database collection" in {
      (mockSubscriptionFieldsIdRepository fetchByFieldsId _) expects SubscriptionFieldsId(FakeRawFieldsId) returns Some(FakeApiSubscription)

      val result = await(service.get(FakeFieldsId))

      result shouldBe Some(FakeSubscriptionFieldsResponse)
    }
  }

  "delete" should {
    "return true when the entry exists in the database collection" in {
      (mockSubscriptionFieldsIdRepository delete(_: ClientId, _: ApiContext, _: ApiVersion)) expects(FakeClientId, FakeContext, FakeVersion) returns true

      val result: Boolean = await(service.delete(FakeClientId, FakeContext, FakeVersion))

      result shouldBe true
    }

    "return false when the entry does not exist in the database collection" in {
      (mockSubscriptionFieldsIdRepository delete(_: ClientId, _: ApiContext, _: ApiVersion)) expects(FakeClientId, FakeContext, FakeVersion) returns false

      val result: Boolean = await(service.delete(FakeClientId, FakeContext, FakeVersion))

      result shouldBe false
    }
  }

}
