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
    await(repository.ensureIndexes)
  }

  override def afterAll() {
    super.afterAll()
     await(repository.drop)
  }

  private val applicationId = UUID.randomUUID()
  private val apiContext = "hello_API"
  private val apiVersion = "2.5-v"
  private val customFields = Map("field_1" -> "value_1", "field_2" -> "value_2", "field_3" -> "value_3")
  private val apiSubscriptionRequest = ApiSubscriptionRequest(applicationId, apiContext, apiVersion, customFields)
  private val apiSubscription = ApiSubscription.create(apiSubscriptionRequest)

  /*
  TODO:
   - vedere TPA, TPDA, etc
   - move these tests as integrations tests, as done here:
   https://github.tools.tax.service.gov.uk/HMRC/third-party-application/blob/master/it/uk/gov/hmrc/repository/ApplicationRepositorySpec.scala
  */

  "save" should {
    "insert the record in the collection" in {
      await(repository.save(apiSubscription)) shouldBe apiSubscription
      await(repository.collection.count()) shouldBe 1

      import reactivemongo.json._

      val selector = BSONDocument("id" -> apiSubscription.id)
      await(repository.collection.find(selector).one[ApiSubscription]) shouldBe Some(apiSubscription)
    }
  }

  "fetch" should {
    "retrieve the record from the id" in {
      await(repository.save(apiSubscription))

      await(repository.fetch(apiSubscription.id)) shouldBe Some(apiSubscription)
    }
  }

  "delete" should {
    "TODO" in {
    }
  }

  "collection" should {
    "have unique index on `id` " in {
    }
  }
}
