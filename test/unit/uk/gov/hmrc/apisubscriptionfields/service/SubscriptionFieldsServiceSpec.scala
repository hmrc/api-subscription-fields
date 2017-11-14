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

import java.util.UUID

import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository._
import uk.gov.hmrc.apisubscriptionfields.service.{SubscriptionFieldsService, UUIDCreator}
import uk.gov.hmrc.play.test.UnitSpec
import util.SubscriptionFieldsTestData

import scala.concurrent.Future

class SubscriptionFieldsServiceSpec extends UnitSpec with SubscriptionFieldsTestData with MockFactory {

  private val mockSubscriptionFieldsIdRepository = mock[SubscriptionFieldsRepository]
  private val mockUuidCreator = new UUIDCreator {
    override def uuid(): UUID = FakeRawFieldsId
  }
  private val service = new SubscriptionFieldsService(mockSubscriptionFieldsIdRepository, mockUuidCreator)

  "getAll" should {
    "return an empty list when no entry exists in the database collection" in {
      (mockSubscriptionFieldsIdRepository.fetchAll _).expects().returns(List())

      await(service.getAll) shouldBe BulkSubscriptionFieldsResponse(subscriptions = List())
    }

    "return a list containing all subscription fields" in {
      val sf1: SubscriptionFields = createSubscriptionFieldsWithApiContext(clientId = "c1")
      val sf2: SubscriptionFields = createSubscriptionFieldsWithApiContext(clientId = "c2")

      (mockSubscriptionFieldsIdRepository.fetchAll _).expects().returns(List(sf1, sf2))

      val expectedResponse = BulkSubscriptionFieldsResponse(subscriptions = List(
        SubscriptionFieldsResponse(sf1.clientId, sf1.apiContext, sf1.apiVersion, SubscriptionFieldsId(sf1.fieldsId), sf1.fields),
        SubscriptionFieldsResponse(sf2.clientId, sf2.apiContext, sf2.apiVersion, SubscriptionFieldsId(sf2.fieldsId), sf2.fields)
      ))

      await(service.getAll) shouldBe expectedResponse
    }
  }

  "get by clientId" should {
    "return None when the expected record does not exist in the database collection" in {
      (mockSubscriptionFieldsIdRepository fetchByClientId _) expects FakeClientId returns List()

      await(service.get(FakeClientId)) shouldBe None
    }

    "return the expected response when the entry exists in the database collection" in {
      val sf1 = createSubscriptionFieldsWithApiContext()
      val sf2 = createSubscriptionFieldsWithApiContext(rawContext = fakeRawContext2)
      (mockSubscriptionFieldsIdRepository fetchByClientId _) expects FakeClientId returns List(sf1, sf2)

      val result = await(service.get(FakeClientId))

      result shouldBe Some(BulkSubscriptionFieldsResponse(subscriptions = Seq(
        SubscriptionFieldsResponse(sf1.clientId, sf1.apiContext, sf1.apiVersion, SubscriptionFieldsId(sf1.fieldsId), sf1.fields),
        SubscriptionFieldsResponse(sf2.clientId, sf2.apiContext, sf2.apiVersion, SubscriptionFieldsId(sf2.fieldsId), sf2.fields))))
    }
  }

  "get" should {
    "return None when no entry exists in the repo" in {
      (mockSubscriptionFieldsIdRepository fetch(_: ClientId, _: ApiContext, _: ApiVersion))
        .expects(FakeClientId, FakeContext, FakeVersion).returns(None)

      await(service.get(FakeClientId, FakeContext, FakeVersion)) shouldBe None
    }

    "return the expected response when the entry exists in the database collection" in {
      (mockSubscriptionFieldsIdRepository fetch(_: ClientId, _: ApiContext, _: ApiVersion))
        .expects(FakeClientId, FakeContext, FakeVersion).returns(Some(FakeApiSubscription))

      val result = await(service.get(FakeClientId, FakeContext, FakeVersion))

      result shouldBe Some(FakeSubscriptionFieldsResponse)
    }
  }

  "get by fieldsId" should {
    "return None when no entry exists in the repo" in {
      (mockSubscriptionFieldsIdRepository fetchByFieldsId _) expects SubscriptionFieldsId(FakeRawFieldsId) returns None

      await(service.get(FakeFieldsId)) shouldBe None
    }

    "return the expected response when the entry exists in the database collection" in {
      (mockSubscriptionFieldsIdRepository fetchByFieldsId _).expects(SubscriptionFieldsId(FakeRawFieldsId))
        .returns(Some(FakeApiSubscription))

      await(service.get(FakeFieldsId)) shouldBe Some(FakeSubscriptionFieldsResponse)
    }
  }

  "upsert" should {
    "return false when updating an existing api subscription fields" in {
      (mockSubscriptionFieldsIdRepository saveAtomic _) expects FakeApiSubscription returns ((FakeApiSubscription, false))

      val result = await(service.upsert(FakeClientId, FakeContext, FakeVersion, subscriptionFields))

      result shouldBe ((SubscriptionFieldsResponse(fakeRawClientId, fakeRawContext, fakeRawVersion, FakeFieldsId, subscriptionFields), false))
    }

    "return true when creating a new api subscription fields" in {
      (mockSubscriptionFieldsIdRepository saveAtomic _) expects FakeApiSubscription returns ((FakeApiSubscription, true))

      val result = await(service.upsert(FakeClientId, FakeContext, FakeVersion, subscriptionFields))

      result shouldBe ((SubscriptionFieldsResponse(fakeRawClientId, fakeRawContext, fakeRawVersion, FakeFieldsId, subscriptionFields), true))
    }

    "propagate the error" in {
      (mockSubscriptionFieldsIdRepository saveAtomic _) expects FakeApiSubscription returns Future.failed(emulatedFailure)

      val caught = intercept[EmulatedFailure] {
        await(service.upsert(FakeClientId, FakeContext, FakeVersion, subscriptionFields))
      }

      caught shouldBe emulatedFailure
    }
  }

  "delete" should {
    "return true when the entry exists in the database collection" in {
      (mockSubscriptionFieldsIdRepository delete(_: ClientId, _: ApiContext, _: ApiVersion))
        .expects(FakeClientId, FakeContext, FakeVersion).returns(true)

      await(service.delete(FakeClientId, FakeContext, FakeVersion)) shouldBe true
    }

    "return false when the entry does not exist in the database collection" in {
      (mockSubscriptionFieldsIdRepository delete(_: ClientId, _: ApiContext, _: ApiVersion))
        .expects(FakeClientId, FakeContext, FakeVersion).returns(false)

      await(service.delete(FakeClientId, FakeContext, FakeVersion)) shouldBe false
    }
  }

}
