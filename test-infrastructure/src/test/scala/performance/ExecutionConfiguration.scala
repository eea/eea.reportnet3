package performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.collection.mutable.{Map => MMap}


object ExecutionConfiguration {
  
  def calculateExecutionKey(testScenario: TestScenario): String = {
    val executionKey = StringBuilder.newBuilder
    executionKey.append(testScenario.method).append("_").append(if (null != testScenario.requestBody && !"".equals(testScenario.requestBody)) "bodyJson" else "noBody").append("_").append(if (null != testScenario.uploadFileName && !"".equals(testScenario.uploadFileName)) "file" else "noFile").append("_").append(if (testScenario.useFeeder) "useFeeder" else "noFeeder")
    return executionKey.toString()
  }

  def getExpresionOutOfInteger(value: Integer): io.gatling.core.session.Expression[Int] = {
    Integer.getInteger("iterations", value).toInt
  }  
  
  def getExecutionConfigurations(): MMap[String, (String, String, Map[String, String], Integer, String, String, String, Integer, Boolean) => io.gatling.core.structure.ScenarioBuilder] = {
    val executionFunctions: MMap[String, (String, String, Map[String, String], Integer, String, String, String, Integer, Boolean) => io.gatling.core.structure.ScenarioBuilder] = collection.mutable.Map.empty
    executionFunctions.put("get_noBody_noFile_noFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      scenario(requestName)
        .repeat(getExpresionOutOfInteger(numberExecutions)) {
          exec(http(requestName)
            .get(endpoint)
            .headers(requestHeaders))
            .pause(pauseTime)
        }
    })
    executionFunctions.put("get_noBody_file_noFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      scenario(requestName)
        .repeat(getExpresionOutOfInteger(numberExecutions)) {
          exec(http(requestName)
            .get(endpoint)
            .formUpload(keyFileName, fileName)
            .headers(requestHeaders))
            .pause(pauseTime)
        }
    })
    executionFunctions.put("get_bodyJson_noFile_noFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      scenario(requestName)
        .repeat(getExpresionOutOfInteger(numberExecutions)) {
          exec(http(requestName)
            .get(endpoint)
            .body(if (requestBody.startsWith("file://")) RawFileBody(requestBody.substring(6)) else StringBody(requestBody)).asJSON
            .headers(requestHeaders))
            .pause(pauseTime)
        }
    })
    executionFunctions.put("post_noBody_noFile_noFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      scenario(requestName)
        .repeat(getExpresionOutOfInteger(numberExecutions)) {
          exec(http(requestName)
            .post(endpoint)
            .headers(requestHeaders))
            .pause(pauseTime)
        }
    })
    executionFunctions.put("post_noBody_noFile_useFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      val feeder = new CSVFeeder("../test-infrastructure/feeder/" + requestName + "_param.csv", requireAuth)
      scenario(requestName)
        .repeat(getExpresionOutOfInteger(numberExecutions)) {
          feed(feeder.apply()).exec(http(requestName)
            .post(endpoint)
            .headers(requestHeaders))
            .pause(pauseTime)
        }
    })
    executionFunctions.put("post_noBody_file_noFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      scenario(requestName)
        .repeat(getExpresionOutOfInteger(numberExecutions)) {
          exec(http(requestName)
            .post(endpoint)
            .formUpload(keyFileName, fileName)
            .headers(requestHeaders))
            .pause(pauseTime)
        }
    })
    executionFunctions.put("post_noBody_file_useFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      val feeder = new CSVFeeder("../test-infrastructure/feeder/" + requestName + "_param.csv", requireAuth)
      scenario(requestName)
        .repeat(getExpresionOutOfInteger(numberExecutions)) {
          feed(feeder.apply()).exec(http(requestName)
            .post(endpoint)
            .formUpload(keyFileName, fileName)
            .headers(requestHeaders))
            .pause(pauseTime)
        }
    })
    executionFunctions.put("post_bodyJson_noFile_noFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      scenario(requestName)
        .repeat(getExpresionOutOfInteger(numberExecutions)) {
          exec(http(requestName)
            .post(endpoint)
            .body(if (requestBody.startsWith("file://")) RawFileBody(requestBody.substring(6)) else StringBody(requestBody)).asJSON
            .headers(requestHeaders))
            .pause(pauseTime)
        }
    })
    executionFunctions.put("get_noBody_noFile_useFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      val feeder = new CSVFeeder("../test-infrastructure/feeder/" + requestName + "_param.csv", requireAuth)
      scenario(requestName).repeat(getExpresionOutOfInteger(numberExecutions)) {
        feed(feeder.apply()).exec(http(requestName)
          .get(endpoint)
          .headers(requestHeaders))
          .pause(pauseTime)
      }
    })
    executionFunctions.put("post_bodyJson_noFile_useFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      val feeder = new CSVFeeder("../test-infrastructure/feeder/" + requestName + "_param.csv", requireAuth)
      println(requestHeaders)
      println(pauseTime)
      scenario(requestName).repeat(getExpresionOutOfInteger(numberExecutions)) {
        feed(feeder.apply()).exec(http(requestName)
          .post(endpoint)
          .body(if (requestBody.startsWith("file://")) RawFileBody(requestBody.substring(6)) else StringBody(requestBody)).asJSON
          .headers(requestHeaders))
          .pause(pauseTime)
      }
    })
    executionFunctions.put("put_noBody_noFile_noFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      scenario(requestName)
        .repeat(getExpresionOutOfInteger(numberExecutions)) {
          exec(http(requestName)
            .put(endpoint)
            .headers(requestHeaders))
            .pause(pauseTime)
        }
    })
    executionFunctions.put("put_noBody_noFile_useFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      val feeder = new CSVFeeder("../test-infrastructure/feeder/" + requestName + "_param.csv", requireAuth)
      scenario(requestName).repeat(getExpresionOutOfInteger(numberExecutions)) {
        feed(feeder.apply()).exec(http(requestName)
          .put(endpoint)
          .headers(requestHeaders))
          .pause(pauseTime)
      }
    })
    executionFunctions.put("delete_noBody_noFile_useFeeder", (requestName: String, endpoint: String, requestHeaders: Map[String, String], pauseTime: Integer, requestBody: String, fileName: String, keyFileName: String, numberExecutions: Integer, requireAuth: Boolean) => {
      //val param_feeder=Iterator.continually(getRandomValue(getDataFeeder("../"+requestName+"_param.csv")))
      val feeder = new CSVFeeder("../test-infrastructure/feeder/" + requestName + "_param.csv", requireAuth)
      scenario(requestName).repeat(getExpresionOutOfInteger(numberExecutions)) {
        feed(feeder.apply()).exec(http(requestName)
          .delete(endpoint)
          .headers(requestHeaders))
          .pause(pauseTime)
      }
    })

    return executionFunctions;
  }

}