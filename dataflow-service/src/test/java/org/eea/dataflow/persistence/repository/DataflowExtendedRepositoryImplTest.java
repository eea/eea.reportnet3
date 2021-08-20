package org.eea.dataflow.persistence.repository;

import static org.junit.Assert.assertEquals;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.junit.Before;
import org.junit.Test;
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

}
