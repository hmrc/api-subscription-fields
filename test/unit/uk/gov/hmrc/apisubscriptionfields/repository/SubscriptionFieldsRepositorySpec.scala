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

package unit.uk.gov.hmrc.apisubscriptionfields.repository

import java.util.UUID

import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import reactivemongo.api.DB
import reactivemongo.bson._
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository.{MongoDbProvider, MongoFormatters, SubscriptionFields, SubscriptionFieldsMongoRepository}
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec
import util.SubscriptionFieldsTestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionFieldsRepositorySpec extends UnitSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with MongoSpecSupport
  with MongoFormatters
  with JsonFormatters
  with SubscriptionFieldsTestData
  with MockFactory { self =>

  private val mongoDbProvider = new MongoDbProvider {
    override val mongo: () => DB = self.mongo
  }

  private val repository = new SubscriptionFieldsMongoRepository(mongoDbProvider) {

    import play.api.libs.json._
    import reactivemongo.play.json.ImplicitBSONHandlers._

    def saveByFieldsId(subscription: SubscriptionFields): Future[Boolean] = {
      collection.update(selector = Json.obj("fieldsId" -> subscription.fieldsId), update = subscription, upsert = true).map {
        updateWriteResult => handleSaveError(updateWriteResult, s"Could not save subscription fields: $subscription",
          updateWriteResult.upserted.nonEmpty
        )
      }
    }

  }

  override def beforeEach() {
    super.beforeEach()
    await(repository.drop)
  }

  override def afterAll() {
    super.afterAll()
    await(repository.drop)
  }

  private def createApiSubscriptionFields(clientId: String = fakeRawClientId): SubscriptionFields = {
    val fields = Map("field_1" -> "value_1", "field_2" -> "value_2", "field_3" -> "value_3")
    SubscriptionFields(clientId, fakeRawContext, fakeRawVersion, UUID.randomUUID(), fields)
  }

  private def collectionSize: Int = {
    await(repository.collection.count())
  }

  "save" should {
    val apiSubscriptionFields = createApiSubscriptionFields()

    import reactivemongo.play.json._

    "insert the record in the collection" in {
      collectionSize shouldBe 0

      await(repository.save(apiSubscriptionFields)) shouldBe true
      collectionSize shouldBe 1
      await(repository.collection.find(selector(apiSubscriptionFields)).one[SubscriptionFields]) shouldBe Some(apiSubscriptionFields)
    }

    "update the record in the collection" in {
      collectionSize shouldBe 0

      await(repository.save(apiSubscriptionFields)) shouldBe true
      collectionSize shouldBe 1

      val edited = apiSubscriptionFields.copy(fields = Map("field4" -> "value_4"))
      await(repository.save(edited)) shouldBe false
      collectionSize shouldBe 1
      await(repository.collection.find(selector(edited)).one[SubscriptionFields]) shouldBe Some(edited)
    }
  }

  private def selector(s: SubscriptionFields) = {
    BSONDocument("clientId" -> s.clientId, "apiContext" -> s.apiContext, "apiVersion" -> s.apiVersion)
  }

  "fetchByClientId" should {
    "retrieve the correct records for a clientId" in {
      val apiSubForApp1Context1 = createSubscriptionFieldsWithApiContext()
      val apiSubForApp1Context2 = createSubscriptionFieldsWithApiContext(rawContext = fakeRawContext2)
      val apiSubForApp2Context1 = createSubscriptionFieldsWithApiContext(clientId = fakeRawClientId2)

      await(repository.save(apiSubForApp1Context1))
      await(repository.save(apiSubForApp1Context2))
      await(repository.save(apiSubForApp2Context1))
      collectionSize shouldBe 3

      await(repository.fetchByClientId(fakeRawClientId)) shouldBe List(apiSubForApp1Context1, apiSubForApp1Context2)
      await(repository.fetchByClientId(fakeRawClientId2)) shouldBe List(apiSubForApp2Context1)
    }

    "return an empty list when clientId is not found" in {
      await(repository.fetchByClientId("CLIENT_ID_DOES_NOT_EXIST_IN_DB")) shouldBe List()
    }

  }

  "fetch using clientId, apiContext, apiVersion" should {
    "retrieve the correct record" in {
      val apiSubscription = createApiSubscriptionFields()
      await(repository.save(apiSubscription)) shouldBe true
      collectionSize shouldBe 1

      await(repository.fetch(FakeClientId, FakeContext, FakeVersion)) shouldBe Some(apiSubscription)
    }

    "return None when no subscription fields are found in the collection" in {
      for (i <- 1 to 3) {
        val result = await(repository.save(createApiSubscriptionFields(clientId = uniqueClientId)))
        result shouldBe true
      }
      collectionSize shouldBe 3

      val found = await(repository.fetch(ClientId("DOES_NOT_EXIST"), FakeContext, FakeVersion))
      found shouldBe None
    }
  }

  "fetch by fieldsId" should {
    "retrieve the correct record from the `fieldsId` " in {
      val apiSubscription = createApiSubscriptionFields()
      await(repository.save(apiSubscription)) shouldBe true
      collectionSize shouldBe 1

      await(repository.fetchByFieldsId(apiSubscription.fieldsId)) shouldBe Some(apiSubscription)
    }

    "return `None` when the `fieldsId` doesn't match any record in the collection" in {
      for (i <- 1 to 3) {
        val isInserted = await(repository.save(createApiSubscriptionFields(clientId = uniqueClientId)))
        isInserted shouldBe true
      }
      collectionSize shouldBe 3

      await(repository.fetchByFieldsId(UUID.fromString("1-2-3-4-5"))) shouldBe None
    }
  }

  "delete by compound key" should {
    "remove the record with a specific subscription field" in {
      val apiSubscription: SubscriptionFields = createApiSubscriptionFields()

      await(repository.save(apiSubscription)) shouldBe true
      collectionSize shouldBe 1

      await(repository.delete(ClientId(apiSubscription.clientId), ApiContext(apiSubscription.apiContext), ApiVersion(apiSubscription.apiVersion))) shouldBe true
      collectionSize shouldBe 0
    }

    "not alter the collection for unknown subscription fields" in {
      for (i <- 1 to 3) {
        val result = await(repository.save(createApiSubscriptionFields(clientId = uniqueClientId)))
        result shouldBe true
      }
      collectionSize shouldBe 3

      val result = await(repository.delete(ClientId("DOES_NOT_EXIST"), FakeContext, FakeVersion))
      result shouldBe false
      collectionSize shouldBe 3
    }
  }

  "collection" should {
    val apiSubscription = createApiSubscriptionFields("A_FIXED_CLIENTID")

    "have a unique compound index based on `clientId`, `apiContext` and `apiVersion`" in {
      await(repository.save(apiSubscription)) shouldBe true
      collectionSize shouldBe 1

      await(repository.save(apiSubscription.copy(fieldsId = UUID.randomUUID()))) shouldBe false
      collectionSize shouldBe 1
    }

    "have a unique index based on `fieldsId`" in {
      await(repository.save(apiSubscription)) shouldBe true
      collectionSize shouldBe 1

      val result = await(repository.saveByFieldsId(apiSubscription.copy(apiVersion = "2.2")))
      result shouldBe false
      collectionSize shouldBe 1
    }

    "have a non-unique index based on `clientId`" in {
      await(repository.save(apiSubscription)) shouldBe true
      collectionSize shouldBe 1

      val result = await(repository.save(apiSubscription.copy(apiContext = fakeRawContext2, fieldsId = UUID.randomUUID())))
      result shouldBe true
      collectionSize shouldBe 2
    }
  }

}
