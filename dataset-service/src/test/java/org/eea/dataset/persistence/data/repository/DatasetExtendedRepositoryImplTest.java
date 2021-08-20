package org.eea.dataset.persistence.data.repository;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class DatasetExtendedRepositoryImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetExtendedRepositoryImplTest {

  /** The dataset extended repository impl. */
  @InjectMocks
  private DatasetExtendedRepositoryImpl datasetExtendedRepositoryImpl;

  /** The entity manager. */
  @Mock
  private EntityManager entityManager;

  /** The query. */
  @Mock
  private Query query;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Delete schema test.
   */
  @Test
  public void deleteSchemaTest() {
    when(entityManager.createNativeQuery(Mockito.any())).thenReturn(query);

    datasetExtendedRepositoryImpl.deleteSchema("schema");
    Mockito.verify(query, times(1)).executeUpdate();
  }
}
