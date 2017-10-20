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
import uk.gov.hmrc.apisubscriptionfields.model.{BulkSubscriptionFieldsResponse, SubscriptionFieldsId, SubscriptionFieldsResponse, SubscriptionIdentifier}
import uk.gov.hmrc.apisubscriptionfields.repository.SubscriptionFieldsRepository
import uk.gov.hmrc.apisubscriptionfields.service.{SubscriptionFieldsService, UUIDCreator}
import uk.gov.hmrc.play.test.UnitSpec
import util.SubscriptionFieldsTestData

class SubscriptionFieldsServiceSpec extends UnitSpec with SubscriptionFieldsTestData with MockFactory {

  private val mockSubscriptionFieldsIdRepository = mock[SubscriptionFieldsRepository]
  private val mockUuidCreator = mock[UUIDCreator]
  private val service = new SubscriptionFieldsService(mockSubscriptionFieldsIdRepository, mockUuidCreator)

  private val SomeOtherFields = Map("f3" -> "v3", "f2" -> "v2b")

  "A RepositoryFedSubscriptionFieldsService" should {
    "return an None when no entry exist in the repo when get by application id is called" in {
      (mockSubscriptionFieldsIdRepository fetchByApplicationId  _) expects fakeRawAppId returns List()

      val result = await(service.get(FakeAppId))

      result shouldBe None
    }

    "return an Some response when entry exists in the repo when get by application id is called" in {
      val subscriptionFields1 = createSubscriptionFieldsWithApiContext()
      val subscriptionFields2 = createSubscriptionFieldsWithApiContext(rawContext = fakeRawContext2)
      (mockSubscriptionFieldsIdRepository fetchByApplicationId  _) expects fakeRawAppId returns List(subscriptionFields1, subscriptionFields2)

      val result = await(service.get(FakeAppId))

      result shouldBe Some(BulkSubscriptionFieldsResponse(fields = Seq(
        SubscriptionFieldsResponse(id = "TODO: remove this field", fieldsId = SubscriptionFieldsId(subscriptionFields1.fieldsId), fields = subscriptionFields1.fields),
        SubscriptionFieldsResponse(id = "TODO: remove this field", fieldsId = SubscriptionFieldsId(subscriptionFields2.fieldsId), fields = subscriptionFields2.fields)
      )))
    }

    "return None when no entry exist in the repo when get by composite id is called" in {
      (mockSubscriptionFieldsIdRepository fetchById _) expects FakeSubscriptionIdentifier returns None

      val result = await(service.get(FakeSubscriptionIdentifier))

      result shouldBe None
    }

    "return Some SubscriptionFieldsResponse when composite id is found" in {
      (mockSubscriptionFieldsIdRepository fetchById _) expects FakeSubscriptionIdentifier returns Some(FakeApiSubscription)

      val result = await(service.get(FakeSubscriptionIdentifier))

      result shouldBe Some(FakeSubscriptionFieldsResponse)
    }

    "return Successful true when an entry exists in the repo when delete is called" in {
      (mockSubscriptionFieldsIdRepository delete _) expects FakeSubscriptionIdentifier returns true

      val result: Boolean = await(service.delete(FakeSubscriptionIdentifier))

      result shouldBe true
    }

    "return Successful false when an entry does not exist in the repo when delete is called" in {
      (mockSubscriptionFieldsIdRepository delete _) expects FakeSubscriptionIdentifier returns false

      val result: Boolean = await(service.delete(FakeSubscriptionIdentifier))

      result shouldBe false
    }

    "return Successful None when no entry exist in the repo when get by fields ID is called" in {
      (mockSubscriptionFieldsIdRepository fetchByFieldsId _) expects FakeRawFieldsId returns None

      val result = await(service.get(FakeFieldsId))

      result shouldBe None
    }

    "return Successful ApiSubscription when an entry exist in the repo when get by fields ID is called" in {
      (mockSubscriptionFieldsIdRepository fetchByFieldsId _) expects FakeRawFieldsId returns Some(FakeApiSubscription)

      val result = await(service.get(FakeFieldsId))

      result shouldBe Some(FakeSubscriptionFieldsResponse)
    }
  }

}
