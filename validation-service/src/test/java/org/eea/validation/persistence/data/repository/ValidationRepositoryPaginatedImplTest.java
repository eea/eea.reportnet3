package org.eea.validation.persistence.data.repository;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRepositoryPaginatedImplTest {



  @InjectMocks
  private ValidationRepositoryPaginatedImpl validationRepositoryPaginatedImpl;

  @Mock
  private EntityManager entityManager;

  private Pageable pageable;

  private List<EntityTypeEnum> typeEntityEnum;

  private List<ErrorTypeEnum> levelErrorsFilter;


  @Before
  public void initMocks() {
    pageable = PageRequest.of(1, 1);
    typeEntityEnum = new ArrayList<>();
    levelErrorsFilter = new ArrayList<>();
    typeEntityEnum.add(EntityTypeEnum.DATASET);
    levelErrorsFilter.add(ErrorTypeEnum.ERROR);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testFilters() {
    Query mockedQuery = mock(Query.class);
    when(entityManager.createQuery(Mockito.anyString())).thenReturn(mockedQuery);
    validationRepositoryPaginatedImpl.findAllRecordsByFilter(1L, levelErrorsFilter, typeEntityEnum,
        "Characterisation,SeasionalPeriod", pageable, "message", true);
    assertNotEquals("", null,
        validationRepositoryPaginatedImpl.findAllRecordsByFilter(1L, levelErrorsFilter,
            typeEntityEnum, "Characterisation,SeasionalPeriod", pageable, "message", true));
  }

  @Test
  public void testCount() {
    Query mockedQuery = mock(Query.class);
    List list = new ArrayList<>();
    list.add(new String("1"));
    when(mockedQuery.getResultList()).thenReturn(list);
    when(entityManager.createQuery(Mockito.anyString())).thenReturn(mockedQuery);
    validationRepositoryPaginatedImpl.countRecordsByFilter(1L, levelErrorsFilter, typeEntityEnum,
        "Characterisation,SeasionalPeriod");
    assertNotEquals("", null, validationRepositoryPaginatedImpl.countRecordsByFilter(1L,
        levelErrorsFilter, typeEntityEnum, "Characterisation,SeasionalPeriod"));

  }

}
