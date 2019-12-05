package org.eea.dataflow.persistence.repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
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
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testFindCompleted() {
    Pageable pageable = PageRequest.of(1, 1);
    Mockito.when(entityManager.createQuery(Mockito.anyString())).thenReturn(query);
    dataflowExtendedRepository.findCompleted("", pageable);
  }

}
