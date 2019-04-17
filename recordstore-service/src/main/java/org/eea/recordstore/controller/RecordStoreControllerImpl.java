package org.eea.recordstore.controller;

import java.util.List;
import org.eea.interfaces.controller.recordstore.RecordStoreController;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.recordstore.exception.DockerAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recordstore")
public class RecordStoreControllerImpl implements
    RecordStoreController {

  @Autowired
  private RecordStoreService recordStoreService;

  @Override
  @RequestMapping(value = "/reset", method = RequestMethod.POST)
  public void resteDataSetDataBase() {
    try {
      recordStoreService.resetDatasetDatabase();
    } catch (final DockerAccessException e) {
      e.printStackTrace();
    }
  }

  @Override
  @RequestMapping(value = "/dataset/create/{datasetName}", method = RequestMethod.POST)
  public void createEmptyDataset(
      @PathVariable("datasetName") final String datasetName) { //TODO neeed to create standar exceptions in commont interfaces
    final ConnectionDataVO connectionDataVO = null;
    try {
      recordStoreService.createEmptyDataSet(datasetName);
    } catch (final DockerAccessException e) {
      e.printStackTrace();
      //TODO Error control
    }
  }

  @Override
  @RequestMapping(value = "/connection/{datasetName}", method = RequestMethod.GET)
  public ConnectionDataVO getConnectionToDataset(
      @PathVariable("datasetName") final String datasetName) {
    ConnectionDataVO vo = null;
    try {
      vo = recordStoreService.getConnectionDataForDataset(datasetName);
    } catch (final DockerAccessException e) {
      e.printStackTrace();
    }
    return vo;
  }

  @Override
  @RequestMapping(value = "/connections", method = RequestMethod.GET)
  public List<ConnectionDataVO> getConnectionToDataset() {
    List<ConnectionDataVO> vo = null;
    try {
      vo = recordStoreService.getConnectionDataForDataset();
    } catch (final DockerAccessException e) {
      e.printStackTrace();
    }
    return vo;
  }
}
