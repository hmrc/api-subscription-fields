/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.apisubscriptionfields.model

import eu.timepit.refined.api._

import play.api.libs.json._

object RefinedJson extends RefinedJson

trait RefinedJson {
  // Based on Refined[T,P] - where T is the underlying type and P is the refinement
  // Access to RefType[F] allows for unwrapping from P to T and refining from T to P
  // All readers need a Validate

  private def rawToJsResult[T, P, F[T, _]](valueT: T)(implicit reftype: RefType[F], validate: Validate[T, P]): JsResult[F[T, P]] =
    reftype.refine[P](valueT) match {
      case Right(valueP) => JsSuccess(valueP)
      case Left(error)   => JsError(error)
    }

  implicit def writeRefined[T, P, F[_, _]](
      implicit writesT: Writes[T],
      reftype: RefType[F]
    ): Writes[F[T, P]] = Writes(value => writesT.writes(reftype.unwrap(value)))

  implicit def readRefined[T, P, F[_, _]](
      implicit readsT: Reads[T],
      reftype: RefType[F],
      validate: Validate[T, P]
    ): Reads[F[T, P]] =
    Reads(jsValue => readsT.reads(jsValue).flatMap(rawToJsResult(_)))

  implicit def formatRefined[T, P](
      implicit writesT: Writes[T],
      readsT: Reads[T],
      validate: Validate[T, P],
      reftype: RefType[Refined]
    ): Format[Refined[T, P]] = Format[Refined[T, P]](readRefined[T, P, Refined], writeRefined[T, P, Refined])

  implicit def keyWrites[P, F[T_, _]](
      implicit reftype: RefType[F]
    ): KeyWrites[F[String, P]] =
    KeyWrites(value => reftype.unwrap(value))

  implicit def keyReads[P, F[_, _]](
      implicit validate: Validate[String, P],
      reftype: RefType[F]
    ): KeyReads[F[String, P]] =
    KeyReads(rawToJsResult(_))
}
