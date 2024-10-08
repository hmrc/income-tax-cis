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

import sbt.*

object AppDependencies {

  private val bootstrapPlay30Version = "8.5.0"

  private val jacksonAndPlayExclusions: Seq[InclusionRule] = Seq(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
    ExclusionRule(organization = "com.fasterxml.jackson.module"),
    ExclusionRule(organization = "com.fasterxml.jackson.core:jackson-annotations"),
    ExclusionRule(organization = "com.typesafe.play")
  )

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-backend-play-30"  % bootstrapPlay30Version,
    "uk.gov.hmrc"                   %% "bootstrap-backend-play-30"  % bootstrapPlay30Version,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.17.0",
    "com.beachape"                  %% "enumeratum"                 % "1.7.3",
    "com.beachape"                  %% "enumeratum-play-json"       % "1.7.3" excludeAll (jacksonAndPlayExclusions *)
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapPlay30Version    % Test,
    "org.scalatest"           %% "scalatest"                % "3.2.15"                  % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.64.0"                  % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"                   % Test,
    "com.github.tomakehurst"  %  "wiremock-jre8-standalone" % "2.35.0"                  % Test,
    "org.scalamock"           %% "scalamock"                % "5.2.0"                   % Test
  )
}
