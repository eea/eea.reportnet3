package org.eea.dataset.persistence.data.repository;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.eea.dataset.mapper.FieldNoValidationMapper;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.util.PSQLException;



/**
 * The Class FieldExtendedRepositoryTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class FieldExtendedRepositoryTest {


  /** The field extended repository. */
  @InjectMocks
  private FieldExtendedRepositoryImpl fieldExtendedRepository;


  /** The field no validation mapper. */
  @Mock
  private FieldNoValidationMapper fieldNoValidationMapper;

  /** The field repository. */
  @Mock
  private FieldRepository fieldRepository;

  /** The entity manager. */
  @Mock
  private EntityManager entityManager;

  /** The query. */
  @Mock
  private Query query;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void findByIdFieldSchemaWithTagOrderedTest() throws PSQLException {

    List<FieldVO> fieldsVO = new ArrayList<>();
    Mockito.when(entityManager.createQuery(Mockito.anyString())).thenReturn(query);
    fieldExtendedRepository.findByIdFieldSchemaWithTagOrdered("5d4abe555b1c1e0001477410",
        "5d4abe555b1c1e0001477410", "8", "5d4abe555b1c1e0001477410", "8", DataType.NUMBER_INTEGER,
        15);
    Assert.assertEquals(fieldsVO,
        fieldExtendedRepository.findByIdFieldSchemaWithTagOrdered("5d4abe555b1c1e0001477410",
            "5d4abe555b1c1e0001477410", "8", "5d4abe555b1c1e0001477410", "8",
            DataType.NUMBER_INTEGER, 15));

  }


}
