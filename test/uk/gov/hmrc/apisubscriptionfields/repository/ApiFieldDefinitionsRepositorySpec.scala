/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mongodb.scala.model.Filters
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.apiplatform.modules.common.domain.models._
import uk.gov.hmrc.mongo.play.json.Codecs

import uk.gov.hmrc.apisubscriptionfields.SubscriptionFieldsTestData.{FakeContext, FakeVersion, NelOfFieldDefinitions, uniqueApiContext}
import uk.gov.hmrc.apisubscriptionfields.model.ApiFieldDefinitions

class ApiFieldDefinitionsRepositorySpec
    extends AnyWordSpec
    with GuiceOneAppPerSuite
    with Matchers
    with OptionValues
    with DefaultAwaitTimeout
    with FutureAwaits
    with BeforeAndAfterEach {

  private val repository = app.injector.instanceOf[ApiFieldDefinitionsMongoRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    await(repository.collection.drop().toFuture())
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    await(repository.collection.drop().toFuture())
  }

  def collectionSize: Long = {
    await(repository.collection.countDocuments().toFuture())
  }

  def createApiFieldDefinitions(apiContext: ApiContext = FakeContext) = ApiFieldDefinitions(apiContext, FakeVersion, NelOfFieldDefinitions)

  trait Setup {
    val definitions: ApiFieldDefinitions = createApiFieldDefinitions()
  }

  "save" should {

    "insert the record in the collection" in new Setup {
      collectionSize shouldBe 0

      await(repository.save(definitions)) shouldBe ((definitions, true))
      collectionSize shouldBe 1
      await(repository.collection.find(selector(definitions)).headOption()) shouldBe Some(definitions)
    }

    "update the record in the collection" in new Setup {
      collectionSize shouldBe 0

      await(repository.save(definitions)) shouldBe ((definitions, true))
      collectionSize shouldBe 1

      val edited = definitions.copy(fieldDefinitions = NelOfFieldDefinitions)
      await(repository.save(edited)) shouldBe ((edited, false))
      collectionSize shouldBe 1
      await(repository.collection.find(selector(edited)).headOption()) shouldBe Some(edited)
    }
  }

  "fetchAll" should {
    "retrieve all the field definitions from the collection" in {
      val fieldsDefinition1 = createApiFieldDefinitions(apiContext = uniqueApiContext)
      val fieldsDefinition2 = createApiFieldDefinitions(apiContext = uniqueApiContext)
      await(repository.save(fieldsDefinition1))
      await(repository.save(fieldsDefinition2))
      collectionSize shouldBe 2

      await(repository.fetchAll()) shouldBe List(fieldsDefinition1, fieldsDefinition2)
    }

    "return an empty list when there are no field definitions in the collection" in {
      await(repository.fetchAll()) shouldBe List()
    }
  }

  "fetch" should {
    "retrieve the correct record from the fields definition" in new Setup {
      await(repository.save(definitions))
      collectionSize shouldBe 1

      await(repository.fetch(FakeContext, FakeVersion)) shouldBe Some(definitions)
    }

    "return `None` when the `id` doesn't match any record in the collection" in {
      for (i <- 1 to 3) {
        val definitions = createApiFieldDefinitions(apiContext = uniqueApiContext)
        await(repository.save(definitions))
      }
      collectionSize shouldBe 3

      await(repository.fetch(ApiContext("CONTEXT_DOES_NOT_EXIST"), FakeVersion)) shouldBe None
    }
  }

  "delete" should {
    "remove the record with a specific fields definition" in {
      val definitions = createApiFieldDefinitions()

      await(repository.save(definitions))
      collectionSize shouldBe 1

      await(repository.delete(definitions.apiContext, definitions.apiVersionNbr)) shouldBe true
      collectionSize shouldBe 0
    }

    "not alter the collection for unknown fields definition" in {
      await(repository.save(createApiFieldDefinitions()))
      collectionSize shouldBe 1

      await(repository.delete(ApiContext("DOES_NOT_EXIST"), FakeVersion)) shouldBe false
      collectionSize shouldBe 1
    }
  }

  "collection" should {
    "have a unique compound index based on `apiContext` and `apiVersionNbr`" in new Setup {

      await(repository.save(definitions))
      collectionSize shouldBe 1

      await(repository.save(definitions))
      collectionSize shouldBe 1
    }
  }

  private def selector(fd: ApiFieldDefinitions) = {
    Filters.and(Filters.equal("apiContext", Codecs.toBson(fd.apiContext.value)), Filters.equal("apiVersionNbr", Codecs.toBson(fd.apiVersionNbr.value)))
  }
}
