package org.eea.dataset.service.helper;

import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class FileTreatmentHelper.
 */
@Component
public class SaveDBHelper {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SaveDBHelper.class);

  /** The record repository. */
  @Autowired
  private RecordRepository recordRepository;

  /** The table repository. */
  @Autowired
  private TableRepository tableRepository;

  /**
   * Instantiates a new file loader helper.
   */
  public SaveDBHelper() {
    super();
  }

  /**
   * Prueba transactional.
   *
   * @param listaGeneral the lista general
   */
  @Transactional
  public void saveListsOfRecords(List<List<RecordValue>> listaGeneral) {
    long preSave = System.currentTimeMillis();
    listaGeneral.parallelStream().forEach(taki -> recordRepository.saveAll(taki));
    long postSave = System.currentTimeMillis();
    LOG.info("datasetRepository.saveAndFlush(): {}", postSave - preSave);
  }

  /**
   * Save table.
   *
   * @param tableValue the dataset
   */
  @Transactional
  public void saveTable(TableValue tableValue) {
    tableRepository.saveAndFlush(tableValue);
  }

}
