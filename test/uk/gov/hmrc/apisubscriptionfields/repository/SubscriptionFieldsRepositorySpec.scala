/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.apisubscriptionfields.repository

import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, OptionValues}
import uk.gov.hmrc.apisubscriptionfields.model._
import Types._
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, ReturnDocument}
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.apisubscriptionfields.AsyncHmrcSpec
import uk.gov.hmrc.apisubscriptionfields.SubscriptionFieldsTestData.{FakeClientId, FakeClientId2, FakeContext, FakeContext2, FakeVersion, createSubscriptionFieldsWithApiContext, fakeRawContext2, fieldN, uniqueClientId}
import uk.gov.hmrc.mongo.play.json.Codecs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionFieldsRepositorySpec extends AsyncHmrcSpec
  with GuiceOneAppPerSuite
  with Matchers
  with OptionValues
  with DefaultAwaitTimeout
  with FutureAwaits
  with BeforeAndAfterEach {

  private val repository = app.injector.instanceOf[SubscriptionFieldsMongoRepository]


    import play.api.libs.json._

  override def beforeEach() {
    super.beforeEach()
    await(repository.collection.drop.toFuture())
  }

  private def createApiSubscriptionFields(clientId: ClientId = FakeClientId): SubscriptionFields = {
    val fields = Map(fieldN(1) -> "value_1", fieldN(2) -> "value_2", fieldN(3) -> "value_3")
    SubscriptionFields(clientId, FakeContext, FakeVersion, SubscriptionFieldsId(UUID.randomUUID()), fields)
  }

  private def collectionSize: Long = {
    await(repository.collection.countDocuments().toFuture())
  }

  private def selector(s: SubscriptionFields) = {
    Filters.and(Filters.equal("apiContext", Codecs.toBson(s.apiContext.value)),
      Filters.equal("apiVersion", Codecs.toBson(s.apiVersion.value)),
      Filters.equal("clientId", Codecs.toBson(s.clientId.value)))
  }

  def saveByFieldsId(subscription: SubscriptionFields): Future[SubscriptionFields] = {
    val query = Filters.equal("fieldsId", Codecs.toBson(subscription.fieldsId.value))
    repository.collection.findOneAndUpdate(filter = query,
      update = set("fieldsId", Codecs.toBson(subscription.fieldsId.value)),
      options = FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER)
    ).toFuture()
  }

  "saveAtomic" should {
    val apiSubscriptionFields = createApiSubscriptionFields()

    "insert the record in the collection" in {
      collectionSize shouldBe 0

      await(repository.saveAtomic(apiSubscriptionFields)) shouldBe ((apiSubscriptionFields, true))
      collectionSize shouldBe 1
      await(repository.collection.find(selector(apiSubscriptionFields)).headOption()) shouldBe Some(apiSubscriptionFields)
    }

    "update the record in the collection" in {
      collectionSize shouldBe 0

      await(repository.saveAtomic(apiSubscriptionFields)) shouldBe ((apiSubscriptionFields, true))
      collectionSize shouldBe 1

      val edited = apiSubscriptionFields.copy(fields = Map(fieldN(4) -> "value_4"))
      await(repository.saveAtomic(edited)) shouldBe ((edited, false))
      collectionSize shouldBe 1
      await(repository.collection.find(selector(edited)).headOption()) shouldBe Some(edited)
    }
  }

  "fetchByClientId" should {
    "retrieve the correct records for a clientId" in {
      val apiSubForApp1Context1 = createSubscriptionFieldsWithApiContext()
      val apiSubForApp1Context2 = createSubscriptionFieldsWithApiContext(rawContext = fakeRawContext2)
      val apiSubForApp2Context1 = createSubscriptionFieldsWithApiContext(clientId = FakeClientId2)

      await(repository.saveAtomic(apiSubForApp1Context1))
      await(repository.saveAtomic(apiSubForApp1Context2))
      await(repository.saveAtomic(apiSubForApp2Context1))
      collectionSize shouldBe 3

      await(repository.fetchByClientId(FakeClientId)) shouldBe List(apiSubForApp1Context1, apiSubForApp1Context2)
      await(repository.fetchByClientId(FakeClientId2)) shouldBe List(apiSubForApp2Context1)
    }

    "return an empty list when clientId is not found" in {
      await(repository.fetchByClientId(ClientId(UUID.randomUUID.toString))) shouldBe List()
    }

  }

  "fetch using clientId, apiContext, apiVersion" should {
    "retrieve the correct record" in {
      val apiSubscription = createApiSubscriptionFields()
      await(repository.saveAtomic(apiSubscription))
      collectionSize shouldBe 1

      await(repository.fetch(FakeClientId, FakeContext, FakeVersion)) shouldBe Some(apiSubscription)
    }

    "return None when no subscription fields are found in the collection" in {
      for (i <- 1 to 3) {
        val apiSubscription = createApiSubscriptionFields(clientId = uniqueClientId)
        await(repository.saveAtomic(apiSubscription))
      }
      collectionSize shouldBe 3

      val found = await(repository.fetch(ClientId(UUID.randomUUID().toString), FakeContext, FakeVersion))
      found shouldBe None
    }
  }

  "fetchByFieldsId" should {
    "retrieve the correct record from the `fieldsId` " in {
      val apiSubscription = createApiSubscriptionFields()
      await(repository.saveAtomic(apiSubscription))
      collectionSize shouldBe 1

      await(repository.fetchByFieldsId(apiSubscription.fieldsId)) shouldBe Some(apiSubscription)
    }

    "return `None` when the `fieldsId` doesn't match any record in the collection" in {
      for (i <- 1 to 3) {
        await(repository.saveAtomic(createApiSubscriptionFields(clientId = uniqueClientId)))
      }
      collectionSize shouldBe 3

      await(repository.fetchByFieldsId(SubscriptionFieldsId(UUID.fromString("1-2-3-4-5")))) shouldBe None
    }
  }

  "fetchAll" should {
    "retrieve all the subscription fields from the collection" in {
      val subscriptionFields1 = createApiSubscriptionFields(clientId = uniqueClientId)
      val subscriptionFields2 = createApiSubscriptionFields(clientId = uniqueClientId)
      val subscriptionFields3 = createApiSubscriptionFields(clientId = uniqueClientId)
      await(repository.saveAtomic(subscriptionFields1))
      await(repository.saveAtomic(subscriptionFields2))
      await(repository.saveAtomic(subscriptionFields3))
      collectionSize shouldBe 3

      await(repository.fetchAll) shouldBe List(subscriptionFields1, subscriptionFields2, subscriptionFields3)
    }

    "return an empty list when there are no subscription fields in the collection" in {
      await(repository.fetchAll) shouldBe List()
    }
  }

  "delete" should {
    "remove the record with a specific subscription field" in {
      val apiSubscription: SubscriptionFields = createApiSubscriptionFields()

      await(repository.saveAtomic(apiSubscription))
      collectionSize shouldBe 1

      await(repository.delete(apiSubscription.clientId, apiSubscription.apiContext, apiSubscription.apiVersion)) shouldBe true
      collectionSize shouldBe 0
    }

    "not alter the collection for unknown subscription fields" in {
      for (i <- 1 to 3) {
        await(repository.saveAtomic(createApiSubscriptionFields(clientId = uniqueClientId)))
      }
      collectionSize shouldBe 3

      await(repository.delete(ClientId(UUID.randomUUID().toString), FakeContext, FakeVersion))
      collectionSize shouldBe 3
    }

    "remove all of the records for a specific client ID" in {
      val clientId = uniqueClientId
      val context1 = "customs/declarations"
      val context2 = "other-context"

      await(repository.saveAtomic(createSubscriptionFieldsWithApiContext(clientId, context1)))
      await(repository.saveAtomic(createSubscriptionFieldsWithApiContext(clientId, context2)))
      collectionSize shouldBe 2

      await(repository.delete(clientId)) shouldBe true
      collectionSize shouldBe 0
    }

    "not alter the collection for other client IDs" in {
      for (i <- 1 to 3) {
        await(repository.saveAtomic(createApiSubscriptionFields(clientId = uniqueClientId)))
      }
      collectionSize shouldBe 3

      await(repository.delete(ClientId(UUID.randomUUID().toString)))
      collectionSize shouldBe 3
    }
  }

  "collection" should {
    val apiSubscription = createApiSubscriptionFields(FakeClientId)

    "have a unique compound index based on `clientId`, `apiContext` and `apiVersion`" in {
      await(repository.saveAtomic(apiSubscription))
      collectionSize shouldBe 1

      await(repository.saveAtomic(apiSubscription.copy(fieldsId = SubscriptionFieldsId(UUID.randomUUID()))))
      collectionSize shouldBe 1
    }

    "have a unique index based on `fieldsId`" in {
      await(repository.saveAtomic(apiSubscription))
      collectionSize shouldBe 1

      await(saveByFieldsId(apiSubscription.copy(apiVersion = ApiVersion("2.2"))))
      collectionSize shouldBe 1
    }

    "have a non-unique index based on `clientId`" in {
      await(repository.saveAtomic(apiSubscription))
      collectionSize shouldBe 1

      await(repository.saveAtomic(apiSubscription.copy(apiContext = FakeContext2, fieldsId = SubscriptionFieldsId(UUID.randomUUID()))))
      collectionSize shouldBe 2
    }
  }

}
