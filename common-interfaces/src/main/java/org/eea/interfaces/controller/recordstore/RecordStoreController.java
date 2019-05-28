package org.eea.interfaces.controller.recordstore;

import java.util.List;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The interface Record store controller.
 */
public interface RecordStoreController {


  /**
   * The interface Record store controller zull.
   */
  @FeignClient(value = "recordstore", path = "/recordstore")
  public interface RecordStoreControllerZull extends RecordStoreController {

  }

  /**
   * Reste data set data base. DO NOT USE IN PRODUCTION. TO BE REMOVED. ONLY FOR TEST PURPOSES
   * @deprecated
   */
  @Deprecated
  @RequestMapping(value = "/reset", method = RequestMethod.POST)
  void resteDataSetDataBase();

  /**
   * Creates a Crunchy Container with dataset schema defined and empty. Publishes a Kafka Event with
   * connection details when finish
   *
   * @param datasetName the dataset name
   */
  @RequestMapping(value = "/dataset/create/{datasetName}", method = RequestMethod.POST)
  void createEmptyDataset(@PathVariable("datasetName") String datasetName);

  /**
   * Gets connection to dataset.
   *
   * @param datasetName the dataset name
   *
   * @return connection to dataset
   */
  @RequestMapping(value = "/connections/{datasetName}", method = RequestMethod.GET)
  ConnectionDataVO getConnectionToDataset(@PathVariable("datasetName") String datasetName);

  /**
   * Gets connection to dataset.
   *
   * @return the connection to dataset
   */
  @RequestMapping(value = "/connections", method = RequestMethod.GET)
  public List<ConnectionDataVO> getDataSetConnections();
}
