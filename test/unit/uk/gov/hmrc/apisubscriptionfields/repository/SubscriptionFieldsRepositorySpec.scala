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
import reactivemongo.bson.BSONDocument
import uk.gov.hmrc.apisubscriptionfields.model.JsonFormatters
import uk.gov.hmrc.apisubscriptionfields.repository.{MongoDbProvider, MongoFormatters, SubscriptionFields, SubscriptionFieldsMongoRepository}
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec
import util.SubscriptionFieldsTestData

import scala.concurrent.ExecutionContext.Implicits.global

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

  private val repository = new SubscriptionFieldsMongoRepository(mongoDbProvider)

  override def beforeEach() {
    super.beforeEach()
    await(repository.drop)
  }

  override def afterAll() {
    super.afterAll()
     await(repository.drop)
  }

  /*
  TODO:
   - look at TPA, TPDA, etc
   - move these tests as integrations tests, as done here:
   https://github.tools.tax.service.gov.uk/HMRC/third-party-application/blob/master/it/uk/gov/hmrc/repository/ApplicationRepositorySpec.scala
  */

  private def createApiSubscription(): SubscriptionFields = {
    val customFields = Map("field_1" -> "value_1", "field_2" -> "value_2", "field_3" -> "value_3")
    SubscriptionFields(s"${UUID.randomUUID().toString}-WhoCaresSoLongAsItsFixed", UUID.randomUUID(), customFields)
  }

  private def collectionSize: Int = {
    await(repository.collection.count())
  }

  "save" should {
    "insert the record in the collection" in {
      collectionSize shouldBe 0
      val apiSubscription = createApiSubscription()
      await(repository.save(apiSubscription))
      collectionSize shouldBe 1

      import reactivemongo.json._

      val selector = BSONDocument("id" -> apiSubscription.id)
      await(repository.collection.find(selector).one[SubscriptionFields]) shouldBe Some(apiSubscription)
    }
  }

  "fetchById" should {
    "retrieve the correct record from the `id` " in {
      val apiSubscription = createApiSubscription()
      await(repository.save(apiSubscription))
      collectionSize shouldBe 1

      await(repository.fetchById(apiSubscription.id)) shouldBe Some(apiSubscription)
    }

    "return `None` when the `id` doesn't match any record in the collection" in {
      for (i <- 1 to 3) {
        await(repository.save(createApiSubscription()))
      }
      collectionSize shouldBe 3

      await(repository.fetchById("ID")) shouldBe None
    }
  }

  "fetchByFieldsId" should {
    "retrieve the correct record from the `fieldsId` " in {
      val apiSubscription = createApiSubscription()
      await(repository.save(apiSubscription))
      collectionSize shouldBe 1

      await(repository.fetchByFieldsId(apiSubscription.fieldsId)) shouldBe Some(apiSubscription)
    }

    "return `None` when the `fieldsId` doesn't match any record in the collection" in {
      for (i <- 1 to 3) {
        await(repository.save(createApiSubscription()))
      }
      collectionSize shouldBe 3

      await(repository.fetchByFieldsId(UUID.fromString("1-2-3-4-5"))) shouldBe None
    }
  }

  "delete" should {
    "remove the record with a specific id" in {
      val apiSubscription = createApiSubscription()

      await(repository.save(apiSubscription))
      collectionSize shouldBe 1

      await(repository.delete(apiSubscription.id))
      collectionSize shouldBe 0
    }

    "not alter the collection for unknown ids" in {
      for (i <- 1 to 3) {
        await(repository.save(createApiSubscription()))
      }
      collectionSize shouldBe 3

      await(repository.delete("ID"))
      collectionSize shouldBe 3
    }
  }

  "collection" should {
    "have a unique index on `id` " in {
      val apiSubscription = createApiSubscription()

      await(repository.save(apiSubscription))
      collectionSize shouldBe 1

      await(repository.save(apiSubscription.copy(fieldsId = UUID.randomUUID())))
      collectionSize shouldBe 1
    }
  }

}
