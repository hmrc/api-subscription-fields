/*
 * Copyright 2021 HM Revenue & Customs
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

import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import uk.gov.hmrc.apisubscriptionfields.model.Types.IsInsert
import uk.gov.hmrc.apisubscriptionfields.utils.ApplicationLogger

trait MongoErrorHandler extends ApplicationLogger {

  def handleDeleteError(result: WriteResult, exceptionMsg: => String): Boolean = {
    handleError(result, databaseAltered, exceptionMsg)
  }

  def handleSaveError(updateWriteResult: UpdateWriteResult, exceptionMsg: => String): IsInsert = {

    def handleUpsertError(result: WriteResult) =
      if (databaseAltered(result))
        updateWriteResult.upserted.nonEmpty
      else
        throw new RuntimeException(exceptionMsg)

    handleError(updateWriteResult, handleUpsertError, exceptionMsg)
  }

  private def handleError(result: WriteResult, f: WriteResult => Boolean, exceptionMsg: String): Boolean = {
    result.writeConcernError.fold(f(result)) {
      errMsg => {
        val errorMsg = s"""$exceptionMsg. $errMsg"""
        appLogger.error(errorMsg)
        throw new RuntimeException(errorMsg)
      }
    }
  }

  private def databaseAltered(writeResult: WriteResult): Boolean = writeResult.n > 0
}
