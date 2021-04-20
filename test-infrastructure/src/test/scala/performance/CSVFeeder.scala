package performance

import scala.collection.mutable.{ListBuffer, Map => MMap}
import scala.io.Source
import scala.util.Random
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import scalaj.http._
import scala.util.parsing.json.JSON
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class CSVFeeder(csvFileName: String, requireAuth: Boolean) {
  val url = sys.env("URL_BASE")
  var data = getDataFeeder(csvFileName, requireAuth)
  
  def getToken(username: String, password: String) : String = {
    val result = Http(url+"/user/generateToken").param("username",username).param("password",password).postData("").asString 
    val mapper = new ObjectMapper
    val root = mapper.readTree(result.body.toString)
    val token = root.at("/accessToken").asText()
    println(username +": "+token.toString)
    return token.toString
  }

  def getDataFeeder(csvFileName: String, requireAuth: Boolean): Seq[Map[String, Any]] = {
    val lines = Source.fromFile(csvFileName).getLines.toList
    var headers = lines(0).split(",")
    val result = ListBuffer[Map[String, Any]]()

    for (i <- 1 to lines.size - 1) {
      var record = lines(i).split(",")
      var recordValues = MMap[String, Any]()
      for (i <- 0 to record.length - 1) {
        recordValues.put(headers(i), record(i));
      }
      if (requireAuth) {
        recordValues.put("token", getToken(recordValues.get("username").getOrElse("").toString, recordValues.get("password").getOrElse("").toString))
      }
      result += recordValues.toMap;
    }
    return result.toSeq;
  }

  def apply(): Iterator[Map[String, Any]] = {
    var count = -1

    def complexCompute(): Int = {
      if (count < data.size - 1) {
        count += 1;
      }
      return count;
    }
    Iterator.continually(data(complexCompute()))
  }
}