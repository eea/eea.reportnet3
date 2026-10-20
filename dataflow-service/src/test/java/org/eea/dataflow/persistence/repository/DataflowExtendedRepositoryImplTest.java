package org.eea.dataflow.persistence.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.exception.EEAException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class DataflowExtendedRepositoryImplTest {

  @InjectMocks
  DataflowExtendedRepositoryImpl dataflowExtendedRepository;

  @Mock
  private EntityManager entityManager;

  @Mock
  Query query;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testFindCompleted() {
    Pageable pageable = PageRequest.of(1, 1);
    Mockito.when(entityManager.createQuery(Mockito.anyString())).thenReturn(query);
    List<Dataflow> result = dataflowExtendedRepository.findCompleted("", pageable);
    assertEquals("failed assertion", query.getResultList(), result);
  }

  @Test
  public void testFindAllPaginatedByCountryOnlyReturnsPublicAndOfficialReportingDataflows()
      throws EEAException {
    List<Dataflow> dataflows = Collections.singletonList(new Dataflow());
    Mockito.when(entityManager.createNativeQuery(Mockito.anyString(), Mockito.eq(Dataflow.class)))
        .thenReturn(query);
    Mockito.when(query.getResultList()).thenReturn(dataflows);

    List<Dataflow> result = dataflowExtendedRepository.findAllPaginatedByCountry("[]",
        PageRequest.of(0, 10), new HashMap<>(), null, true, "AT");

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(entityManager).createNativeQuery(sqlCaptor.capture(),
        Mockito.eq(Dataflow.class));
    String sql = sqlCaptor.getValue();
    int where = sql.lastIndexOf("r.has_datasets = TRUE");
    int orderBy = sql.lastIndexOf(" order by ");
    int isPublic = sql.indexOf("show_public_info = true");
    int isOfficial = sql.indexOf("official_reporting = true");

    assertEquals("failed assertion", dataflows, result);
    // public condition must come after the main where clause starts (> where) and before order by (< orderBy)
    assertTrue("public condition missing from where clause", isPublic > where && isPublic < orderBy);
    // official reporting condition must come after the main where clause starts (> where) and before order by (< orderBy)
    assertTrue("official reporting condition missing from where clause",
        isOfficial > where && isOfficial < orderBy);
  }

}
