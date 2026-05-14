import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings

val appName = "income-tax-cis"

lazy val coverageSettings: Seq[Setting[?]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq(
    "<empty>",
    ".*Reverse.*",
    ".*standardError*.*",
    ".*govuk_wrapper*.*",
    ".*main_template*.*",
    "uk.gov.hmrc.BuildInfo",
    "app.*",
    "prod.*",
    "config.*",
    "testOnly.*",
    "testOnlyDoNotUseInAppConf.*",
    "controllers.testOnly.*"
  )

  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 96, // This has been set to 96% from 97% due to the Scala 3 migration as Scala 3 brings in more generated code which is not covered by tests, and this has resulted in a drop in coverage. The threshold will be increased back to 97% once the coverage is back up to that level.
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting  := true
  )
}

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.7"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions += "-Wconf:msg=unused import&src=html/.*:s",
    scalacOptions += "-Wconf:src=routes/.*:s"
  )
  .settings(PlayKeys.playDefaultPort := 9328)
  .settings(RoutesKeys.routesImport ++= Seq("models.TaxYearPathBindable._", "models.TaxYearPathBindable.TaxYear"))
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(coverageSettings*)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
