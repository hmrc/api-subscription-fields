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

package uk.gov.hmrc.apisubscriptionfields.repository

import java.util.UUID

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import reactivemongo.bson.BSONDocument
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.model.JsonFormatters.formatApiSubscription
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionFieldsIdsRepositorySpec extends UnitSpec
  with BeforeAndAfterAll with BeforeAndAfterEach
  with WithFakeApplication with MongoSpecSupport {

  private val repository = new SubscriptionFieldsIdMongoRepository

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
   - vedere TPA, TPDA, etc
   - move these tests as integrations tests, as done here:
   https://github.tools.tax.service.gov.uk/HMRC/third-party-application/blob/master/it/uk/gov/hmrc/repository/ApplicationRepositorySpec.scala
  */

  private def createApiSubscription(): ApiSubscription = {
    val applicationId = UUID.randomUUID()
    val apiContext = "hello_API"
    val apiVersion = "2.5-v"
    val customFields = Map("field_1" -> "value_1", "field_2" -> "value_2", "field_3" -> "value_3")
    val apiSubscriptionRequest = ApiSubscriptionRequest(applicationId, apiContext, apiVersion, customFields)
    ApiSubscription.create(apiSubscriptionRequest)
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
      await(repository.collection.find(selector).one[ApiSubscription]) shouldBe Some(apiSubscription)
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

      await(repository.save(apiSubscription))
      collectionSize shouldBe 1
    }
  }
}
