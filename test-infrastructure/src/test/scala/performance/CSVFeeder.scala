package performance


import scala.collection.mutable.{ListBuffer, Map => MMap}
import scala.io.Source
import scala.util.Random
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import scalaj.http._
import scala.util.parsing.json.JSON

class CSVFeeder(csvFileName: String, requireAuth: Boolean) {
  var data = getDataFeeder(csvFileName)
println("\nLA AUTENTICACION: "+ requireAuth +"\n ")
    val url = sys.env("URL_BASE");
  
    def getToken(username: String, password: String) : String = {
      val result = Http(url+"/user/generateToken").param("username",username).param("password",password).postData("").asString 
    println(result.body)
    val mapper = new ObjectMapper
    val root = mapper.readTree(result.body.toString)
    val token = root.at("/accessToken").asText()
    println("token:" + token.toString)
    return token.toString
  }
  
  
  def getDataFeeder(csvFileName: String): Seq[Map[String, Any]] = {
    val lines = Source.fromFile(csvFileName).getLines.toList
    var headers = lines(0).split(",")
    val result = ListBuffer[Map[String, Any]]()

    for (i <- 1 to lines.size - 1) {
      var record = lines(i).split(",")
      var recordValues = MMap[String, Any]()
      for (i <- 0 to record.length - 1) {
        recordValues.put(headers(i), record(i));
      }
      result += recordValues.toMap;
    }
    return result.toSeq;
  }

  def apply(): Iterator[Map[String, Any]] = {
    // val position = Random.nextInt(data.size - 1)
    // val value = data(position)
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