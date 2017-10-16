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
import uk.gov.hmrc.apisubscriptionfields.repository.{InMemoryRepository, SubscriptionFields, SubscriptionFieldsRepository}
import uk.gov.hmrc.apisubscriptionfields.service.{RepositoryFedSubscriptionFieldsService, UUIDCreator}
import uk.gov.hmrc.play.test.UnitSpec
import util.SubscriptionFieldsTestData

import scala.concurrent.ExecutionContext.Implicits.global

class RepositoryFedSubscriptionFieldsServiceSpec extends UnitSpec with SubscriptionFieldsTestData with MockFactory {

  private val mockSubscriptionFieldsIdRepository = mock[SubscriptionFieldsRepository]
  private val mockUuidCreator = mock[UUIDCreator]
  private val service = new RepositoryFedSubscriptionFieldsService(mockSubscriptionFieldsIdRepository, mockUuidCreator)

  private val SomeOtherFields = Map("f3" -> "v3", "f2" -> "v2b")

  "A RepositoryFedSubscriptionFieldsService" should {
    "return Success None when no entry exist in the repo when get by application identifier is called" in {
      (mockSubscriptionFieldsIdRepository fetchById _) expects FakeSubscriptionIdentifier.encode() returns None

      val result = await(service.get(FakeSubscriptionIdentifier))

      result shouldBe None
    }

    "return Successful true when an entry exists in the repo when delete is called" in {
      (mockSubscriptionFieldsIdRepository delete _) expects FakeSubscriptionIdentifier.encode() returns true

      val result: Boolean = await(service.delete(FakeSubscriptionIdentifier))

      result shouldBe true
    }

    "return Successful false when an entry does not exist in the repo when delete is called" in {
      (mockSubscriptionFieldsIdRepository delete _) expects FakeSubscriptionIdentifier.encode() returns false

      val result: Boolean = await(service.delete(FakeSubscriptionIdentifier))

      result shouldBe false
    }
  }

  "return Successful None when no entry exist in the repo when get by fields ID is called" in {
    (mockSubscriptionFieldsIdRepository fetchByFieldsId _) expects FakeRawFieldsId returns None

    val result = await(service.get(FakeFieldsId))

    result shouldBe None
  }

  "return Successful ApiSubscription when an entry exist in the repo when get by fields ID is called" in {
    (mockSubscriptionFieldsIdRepository fetchByFieldsId _) expects FakeRawFieldsId returns Some(FakeApiSubscription)

    val result = await(service.get(FakeFieldsId))

    result shouldBe Some(ValidResponse)
  }

  //TODO this should be removed when removing InMemoryRepository
  "A Service using an in memory repository" should {

    class ListOfUUIDS(initial: Seq[UUID]) extends UUIDCreator {
      private var uuids = initial

      override def uuid(): UUID = {
        uuids match {
          case Nil => throw new RuntimeException("List of UUIDs exhausted")
          case h :: t =>
            uuids = t
            h
        }
      }
    }

    val uuids: Seq[UUID] = (1 to 10).map(_ => UUID.randomUUID())

    val service = new RepositoryFedSubscriptionFieldsService(new InMemoryRepository, new ListOfUUIDS(uuids))
    val saveExpectation = SubscriptionFields(FakeRawIdentifier, uuids.head, CustomFields)
    val updateExpectation = SubscriptionFields(FakeRawIdentifier, uuids.head, SomeOtherFields)

    def saveSomething() = {
      service.upsert(FakeSubscriptionIdentifier, CustomFields) map { _ shouldBe ((saveExpectation, true)) }
    }

    def updateIt() = {
      service.upsert(FakeSubscriptionIdentifier, SomeOtherFields) map { _ shouldBe ((updateExpectation, false)) }
    }

    def deleteIt() = {
      service.delete(FakeSubscriptionIdentifier)
    }

    "be able to handle finding nothing" in {
      service.get(FakeSubscriptionIdentifier) map { _ shouldBe None }
      service.get(FakeFieldsId) map { _ shouldBe None }
    }

    "be able to save something to the repo" in {
      saveSomething()

      service.get(FakeSubscriptionIdentifier) map { _ shouldBe Some(saveExpectation) }
      service.get(FakeFieldsId) map { _ shouldBe Some(saveExpectation) }
    }

    "be able to save something and then update it" in {
      saveSomething()
      updateIt()

      service.get(FakeSubscriptionIdentifier) map { _ shouldBe Some(updateExpectation) }
      service.get(FakeFieldsId) map { _ shouldBe Some(updateExpectation) }
    }

    "be able to save something and then delete it" in {
      saveSomething()

      service.get(FakeSubscriptionIdentifier) map { _ shouldBe Some(saveExpectation) }
      service.get(FakeFieldsId) map { _ shouldBe Some(saveExpectation) }

      deleteIt()

      service.get(FakeSubscriptionIdentifier) map { _ shouldBe empty }
      service.get(FakeFieldsId) map { _ shouldBe empty }
    }
  }
}
