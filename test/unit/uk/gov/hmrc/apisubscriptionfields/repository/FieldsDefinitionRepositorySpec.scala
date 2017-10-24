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

import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import reactivemongo.api.DB
import reactivemongo.bson.BSONDocument
import uk.gov.hmrc.apisubscriptionfields.model.{ApiContext, JsonFormatters}
import uk.gov.hmrc.apisubscriptionfields.repository._
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec
import util.FieldsDefinitionTestData

import scala.concurrent.ExecutionContext.Implicits.global

class FieldsDefinitionRepositorySpec extends UnitSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with MongoSpecSupport
  with MongoFormatters
  with JsonFormatters
  with FieldsDefinitionTestData
  with MockFactory { self =>

  private val mongoDbProvider = new MongoDbProvider {
    override val mongo: () => DB = self.mongo
  }

  private val repository = new FieldsDefinitionMongoRepository(mongoDbProvider)

  override def beforeEach() {
    super.beforeEach()
    await(repository.drop)
  }

  override def afterAll() {
    super.afterAll()
    await(repository.drop)
  }

  private def collectionSize: Int = {
    await(repository.collection.count())
  }

  private def createFieldsDefinition = FieldsDefinition(fakeRawContext, fakeRawVersion, FakeFieldsDefinitions)

  private trait Setup {
    val fieldsDefinition = createFieldsDefinition
  }

  "save" should {
    import reactivemongo.play.json._

    "insert the record in the collection" in new Setup {
      collectionSize shouldBe 0

      await(repository.save(fieldsDefinition)) shouldBe true
      collectionSize shouldBe 1
      await(repository.collection.find(selector(fieldsDefinition)).one[FieldsDefinition]) shouldBe Some(fieldsDefinition)
    }

    "update the record in the collection" in new Setup {
      collectionSize shouldBe 0

      await(repository.save(fieldsDefinition)) shouldBe true
      collectionSize shouldBe 1

      val edited = fieldsDefinition.copy(fieldDefinitions = Seq.empty)
      await(repository.save(edited)) shouldBe false
      collectionSize shouldBe 1
      await(repository.collection.find(selector(edited)).one[FieldsDefinition]) shouldBe Some(edited)
    }
  }

  "fetchAll" should {
    "retrieve all the definition records" in {
      val fieldsDefinition1 = createFieldsDefinition(apiContext = uniqueApiContext)
      val fieldsDefinition2 = createFieldsDefinition(apiContext = uniqueApiContext)
      await(repository.save(fieldsDefinition1))
      await(repository.save(fieldsDefinition2))
      collectionSize shouldBe 2

      await(repository.fetchAll()) shouldBe List(fieldsDefinition1, fieldsDefinition2)
    }

    "return an empty list when there are no definitions in the collection" in {
      await(repository.fetchAll()) shouldBe List()
    }
  }

  "fetch with fields definition" should {
    "retrieve the correct record from the fields definition" in new Setup {
      await(repository.save(fieldsDefinition)) shouldBe true
      collectionSize shouldBe 1

      await(repository.fetch(FakeContext, FakeVersion)) shouldBe Some(fieldsDefinition)
    }

    "return `None` when the `id` doesn't match any record in the collection" in {
      for (i <- 1 to 3) {
        val result = await(repository.save(createFieldsDefinition(apiContext = uniqueApiContext)))
        result shouldBe true
      }
      collectionSize shouldBe 3

      await(repository.fetch(ApiContext("CONTEXT_DOES_NOT_EXIST"), FakeVersion)) shouldBe None
    }
  }

  "collection" should {
    "have a unique compound index based on `apiContext` and `apiVersion`" in new Setup {

      await(repository.save(fieldsDefinition)) shouldBe true
      collectionSize shouldBe 1

      await(repository.save(fieldsDefinition)) shouldBe false
      collectionSize shouldBe 1
    }
  }

  private def selector(fd: FieldsDefinition) = {
    BSONDocument("apiContext" -> fd.apiContext, "apiVersion" -> fd.apiVersion)
  }
}
