package org.eea.validation.persistence.data.repository;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.hibernate.Session;
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

/**
 * The Class ValidationRepositoryPaginatedImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationRepositoryPaginatedImplTest {



  /** The validation repository paginated impl. */
  @InjectMocks
  private ValidationRepositoryPaginatedImpl validationRepositoryPaginatedImpl;

  /** The entity manager. */
  @Mock
  private EntityManager entityManager;

  /** The pageable. */
  private Pageable pageable;

  /** The type entity enum. */
  private List<EntityTypeEnum> typeEntityEnum;

  /** The level errors filter. */
  private List<ErrorTypeEnum> levelErrorsFilter;

  /** The session. */
  @Mock
  private Session session;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    pageable = PageRequest.of(1, 1);
    typeEntityEnum = new ArrayList<>();
    levelErrorsFilter = new ArrayList<>();
    typeEntityEnum.add(EntityTypeEnum.DATASET);
    levelErrorsFilter.add(ErrorTypeEnum.ERROR);
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test filters.
   */
  @Test
  public void testFilters() {
    Query mockedQuery = mock(Query.class);
    when(entityManager.createQuery(Mockito.anyString())).thenReturn(mockedQuery);
    validationRepositoryPaginatedImpl.findAllRecordsByFilter(1L, levelErrorsFilter, typeEntityEnum,
        "Characterisation,SeasionalPeriod", "", pageable, "message", true);
    assertNotEquals("", null,
        validationRepositoryPaginatedImpl.findAllRecordsByFilter(1L, levelErrorsFilter,
            typeEntityEnum, "Characterisation,SeasionalPeriod", "", pageable, "message", true));
  }

  /**
   * Test count.
   */
  @Test
  public void testCount() {
    Query mockedQuery = mock(Query.class);
    List list = new ArrayList<>();
    list.add(new String("1"));
    when(mockedQuery.getResultList()).thenReturn(list);
    when(entityManager.createQuery(Mockito.anyString())).thenReturn(mockedQuery);
    validationRepositoryPaginatedImpl.countRecordsByFilter(1L, levelErrorsFilter, typeEntityEnum,
        "Characterisation,SeasionalPeriod", "");
    assertNotEquals("", null, validationRepositoryPaginatedImpl.countRecordsByFilter(1L,
        levelErrorsFilter, typeEntityEnum, "Characterisation,SeasionalPeriod", ""));

  }

  /**
   * Find group records by filter test.
   */
  @Test
  public void findGroupRecordsByFilterTest() {
    Mockito.when(entityManager.getDelegate()).thenReturn(session);
    assertNull(validationRepositoryPaginatedImpl.findGroupRecordsByFilter(1L, levelErrorsFilter,
        typeEntityEnum, "Characterisation,SeasionalPeriod", "", pageable, "message", true, true));
  }



}
