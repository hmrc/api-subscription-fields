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

  private def createFieldsDefinition = FieldsDefinition(UUID.randomUUID().toString, FakeFieldsDefinitions)

  private trait Setup {
    val fieldsDefinition = createFieldsDefinition
  }

  "save" should {
    "insert the record in the collection" in new Setup {
      collectionSize shouldBe 0
      await(repository.save(fieldsDefinition))
      collectionSize shouldBe 1

      import reactivemongo.json._

      val selector = BSONDocument("id" -> fieldsDefinition.id)
      await(repository.collection.find(selector).one[FieldsDefinition]) shouldBe Some(fieldsDefinition)
    }
  }

  "fetchById" should {
    "retrieve the correct record from the `id` " in new Setup {
      await(repository.save(fieldsDefinition))
      collectionSize shouldBe 1

      await(repository.fetchById(fieldsDefinition.id)) shouldBe Some(fieldsDefinition)
    }

    "return `None` when the `id` doesn't match any record in the collection" in {
      for (i <- 1 to 3) {
        await(repository.save(createFieldsDefinition))
      }
      collectionSize shouldBe 3

      await(repository.fetchById("ID")) shouldBe None
    }
  }

  "collection" should {
    "have a unique index on `id` " in new Setup {

      await(repository.save(fieldsDefinition))
      collectionSize shouldBe 1

      await(repository.save(fieldsDefinition.copy(fields = Seq(FakeFieldDefinitionUrl))))
      collectionSize shouldBe 1
    }
  }

}
