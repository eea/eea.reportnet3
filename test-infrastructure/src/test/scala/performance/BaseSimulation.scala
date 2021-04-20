package performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.collection.JavaConversions._
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.util.{List => JList, Map => JMap}
import java.io.{BufferedReader, FileReader}
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.{Map => MMap}
import java.io.File
import java.io.FileInputStream
import scalaj.http._
import scala.util.parsing.json.JSON
//import performance.ExecutionConfiguration
//import performance.CSVFeeder
import scala.collection.mutable.{ListBuffer, Map => MMap}

class BaseSimulation extends Simulation {

  val configLoadTestFile = sys.env("LOAD_TEST_PATH");
  //val reader = new FileInputStream(new File("C:\\proyectos\\EEA\\desarrollo\\repornet\\test-infrastructure\\src\\test\\scala\\resources\\load_test.yml"));
  val reader = new FileInputStream(new File(configLoadTestFile));
  val mapper = new ObjectMapper(new YAMLFactory())
  mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  val config: GatlingConfig = mapper.readValue(reader, classOf[GatlingConfig])
  val url = sys.env("URL_BASE");
  //val url = "http://localhost:8090";
  val httpProtocol = http
    .baseURL(url) // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")


  val executionFunctions = ExecutionConfiguration.getExecutionConfigurations

  var scenariosList = new ListBuffer[io.gatling.core.structure.PopulationBuilder]()
  val assertionList = new ListBuffer[io.gatling.commons.stats.assertion.Assertion]()
  val testScenarioFunction: (TestScenario) => Unit = (testScenario) => {
    var scalaHeaders: MMap[String, String] = collection.mutable.Map.empty
    if (null != testScenario.headers && !testScenario.headers.isEmpty()) {
      scalaHeaders = collection.mutable.Map(testScenario.headers.toSeq: _*)
    }
    val finalHeaders = scalaHeaders.toMap
    //Retrieve the proper scenario builder function and execute it
    val scn = executionFunctions.get(ExecutionConfiguration.calculateExecutionKey(testScenario)).get(testScenario.requestName, testScenario.endpoint, finalHeaders, testScenario.pauseTime, testScenario.requestBody, testScenario.uploadFileName, testScenario.uploadFileKey, testScenario.numberExecutions, testScenario.requireAuth)
    //Add the just created scenario to the list of scenarios
    scenariosList += scn.inject(atOnceUsers(testScenario.usersNumber)).protocols(httpProtocol)
    //Define assertions
    assertionList += details(testScenario.requestName).responseTime.max.lt(testScenario.timeOut)
    println(testScenario.requestName + ": Done")
  }
  config.gatlingScenarios.scenarios.foreach(testScenarioFunction)

  var allScenarios = scenariosList.toList

  setUp(allScenarios).assertions(assertionList)

}

class GatlingConfig(@JsonProperty("gatling_config") _gatlingScenarios: GatlingScenarios) {
  var gatlingScenarios = _gatlingScenarios
}

class GatlingScenarios(@JsonProperty("scenarios") _scenarios: JList[TestScenario]) {
  var scenarios = _scenarios
}

class TestScenario(@JsonProperty("requestName") _requestName: String, @JsonProperty("endpoint") _endpoint: String, @JsonProperty("usersNumber") _usersNumber: Integer, @JsonProperty("method") _method: String,
                   @JsonProperty("headers") _headers: JMap[String, String], @JsonProperty("puaseTime") _pauseTime: Integer, @JsonProperty("requestBody") _requestBody: String, @JsonProperty("uploadFileName") _uploadFileName: String,
                   @JsonProperty("uploadFileKey") _uploadFileKey: String, @JsonProperty("timeOut") _timeOut: Integer, @JsonProperty("numberExecutions") _numberExecutions: Integer, @JsonProperty("useFeeder") _useFeeder: Boolean,@JsonProperty("requireAuth") _requireAuth: Boolean) {
  var endpoint = _endpoint
  var usersNumber = _usersNumber
  var method = _method
  var headers = _headers
  var requestName = _requestName
  var pauseTime = _pauseTime
  var requestBody = _requestBody
  var uploadFileName = _uploadFileName
  var uploadFileKey = _uploadFileKey
  var timeOut = _timeOut
  var numberExecutions = 1;
  if (_numberExecutions != null) {
    numberExecutions = _numberExecutions;
  }
  var useFeeder = _useFeeder
  var requireAuth = _requireAuth
}





