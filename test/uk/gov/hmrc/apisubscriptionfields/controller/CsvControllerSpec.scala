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

package uk.gov.hmrc.gatekeeper.controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

import play.api.test.Helpers._
import play.api.test.{FakeRequest, StubControllerComponentsFactory}
import uk.gov.hmrc.apiplatform.modules.common.domain.models._
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apisubscriptionfields.AsyncHmrcSpec
import uk.gov.hmrc.apisubscriptionfields.controller.CsvController
import uk.gov.hmrc.apisubscriptionfields.model.Types.FieldName
import uk.gov.hmrc.apisubscriptionfields.model.{BulkSubscriptionFieldsResponse, SubscriptionFields, SubscriptionFieldsId}
import uk.gov.hmrc.apisubscriptionfields.service.SubscriptionFieldsService

class CsvControllerSpec extends AsyncHmrcSpec with StubControllerComponentsFactory with ApiIdentifierFixtures with ClientIdFixtures {
  import eu.timepit.refined.auto._

  implicit val hc: HeaderCarrier = HeaderCarrier()

  trait Setup {

    val service = mock[SubscriptionFieldsService]

    val controller = new CsvController(stubControllerComponents(), service)

  }

  "CsvController" should {
    "return a csv" in new Setup {
      final val AlphanumericFieldName: FieldName = "alphanumericField"
      final val PasswordFieldName: FieldName     = "password"

      val subsFields = Seq(
        SubscriptionFields(ClientId("A2"), apiContextOne, apiVersionNbrOne, SubscriptionFieldsId.random(), Map(AlphanumericFieldName -> "A")),
        SubscriptionFields(ClientId("A1"), apiContextTwo, apiVersionNbrTwo, SubscriptionFieldsId.random(), Map(PasswordFieldName -> "X"))
      )

      when(service.getAll()).thenReturn(successful(BulkSubscriptionFieldsResponse(subsFields)))

      val result = controller.csv()(FakeRequest())

      val expectedCsv = s"""|Environment,ClientId,ApiContext,ApiVersionNbr,FieldName
                            |PRODUCTION,A1,${apiContextTwo},${apiVersionNbrTwo},${PasswordFieldName}
                            |PRODUCTION,A2,${apiContextOne},${apiVersionNbrOne},$AlphanumericFieldName
                            |""".stripMargin

      contentAsString(result) shouldBe expectedCsv
      contentType(result).value shouldBe "text/csv"
    }
  }
}
