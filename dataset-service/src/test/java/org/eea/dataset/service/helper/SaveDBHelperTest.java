package org.eea.dataset.service.helper;

import java.util.ArrayList;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * The Class SaveDBHelperTest.
 */
public class SaveDBHelperTest {

  /** The save helper. */
  @InjectMocks
  SaveDBHelper saveHelper;

  /** The record repository. */
  @Mock
  private RecordRepository recordRepository;

  /** The table repository. */
  @Mock
  private TableRepository tableRepository;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test save lists of records.
   */
  @Test
  public void testSaveListsOfRecords() {
    saveHelper.saveListsOfRecords(new ArrayList<>());
  }

  /**
   * Test save table.
   */
  @Test
  public void testSaveTable() {
    saveHelper.saveTable(new TableValue());
  }

}
