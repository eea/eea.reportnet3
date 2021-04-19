package performance


import scala.collection.mutable.{ListBuffer, Map => MMap}
import scala.io.Source
import scala.util.Random

class CSVFeeder(csvFileName: String) {
  var data = getDataFeeder(csvFileName)

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