/*
 * Copyright 2018 HM Revenue & Customs
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

import reactivemongo.api.commands._
import reactivemongo.bson.BSONInteger
import uk.gov.hmrc.play.test.UnitSpec

class MongoErrorHandlerSpec extends UnitSpec {

  private val mongoErrorHandler = new MongoErrorHandler {}
  private val BsonValue = BSONInteger(1)
  private val SomeWriteConcernError = Some(WriteConcernError(1, "ERROR"))

  "handleDeleteError" should {
    "return true if there are no database errors and at least one record deleted" in {
      val successfulWriteResult = writeResult(alteredRecords = 1)

      mongoErrorHandler.handleDeleteError(successfulWriteResult, "ERROR_MSG") shouldBe true
    }

    "return false if there are no database errors and no record deleted" in {
      val noDeletedRecordsWriteResult = writeResult(alteredRecords = 0)

      mongoErrorHandler.handleDeleteError(noDeletedRecordsWriteResult, "ERROR_MSG") shouldBe false
    }

    "throw a RuntimeException if there is a database error" in {
      val writeConcernError = Some(WriteConcernError(1, "ERROR"))
      val errorWriteResult = writeResult(alteredRecords = 0, writeConcernError = writeConcernError)

      val caught = intercept[RuntimeException](mongoErrorHandler.handleDeleteError(errorWriteResult, "ERROR_MSG"))

      caught.getMessage shouldBe "ERROR_MSG. WriteConcernError(1,ERROR)"
    }
  }

  "handleSaveError" should {
    "return true if there are no database errors and at least one record inserted" in {
      val Inserted = Seq(Upserted(0, BsonValue))
      val successfulInsertWriteResult = updateWriteResult(alteredRecords = 1, upserted = Inserted)

      mongoErrorHandler.handleSaveError(successfulInsertWriteResult, "ERROR_MSG") shouldBe true
    }

    "return false if there are no database errors and no record deleted and at least one record updated" in {
      val successfulUpdateWriteResult = updateWriteResult(alteredRecords = 1)

      mongoErrorHandler.handleSaveError(successfulUpdateWriteResult, "ERROR_MSG") shouldBe false
    }

    "throw a RuntimeException if there is a database error" in {
      val errorUpdateWriteResult = updateWriteResult(alteredRecords = 0, writeConcernError = SomeWriteConcernError)

      val caught = intercept[RuntimeException](mongoErrorHandler.handleSaveError(errorUpdateWriteResult, "ERROR_MSG"))

      caught.getMessage shouldBe "ERROR_MSG. WriteConcernError(1,ERROR)"
    }

    "throw a RuntimeException if there are no records altered" in {
      val errorUpdateWriteResult = updateWriteResult(alteredRecords = 0)

      val caught = intercept[RuntimeException](mongoErrorHandler.handleSaveError(errorUpdateWriteResult, "ERROR_MSG"))

      caught.getMessage shouldBe "ERROR_MSG"
    }

  }

  private def writeResult(alteredRecords: Int, writeErrors: Seq[WriteError] = Nil,
                          writeConcernError: Option[WriteConcernError] = None) = {
    DefaultWriteResult(
      ok = true,
      n = alteredRecords,
      writeErrors = writeErrors,
      writeConcernError = writeConcernError,
      code = None,
      errmsg = None)
  }

  private def updateWriteResult(alteredRecords: Int, upserted: Seq[Upserted] = Nil, writeErrors: Seq[WriteError] = Nil,
                                writeConcernError: Option[WriteConcernError] = None) = {
    UpdateWriteResult(
      ok = true,
      n = alteredRecords,
      nModified = 0,
      upserted = upserted,
      writeErrors = writeErrors,
      writeConcernError = writeConcernError,
      code = None,
      errmsg = None)
  }

}
