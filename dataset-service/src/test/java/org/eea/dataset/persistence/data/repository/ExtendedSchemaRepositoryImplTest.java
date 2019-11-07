package org.eea.dataset.persistence.data.repository;

import static org.mockito.Mockito.times;
import java.io.IOException;
import org.bson.Document;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.ExtendedSchemaRepositoryImpl;
import org.eea.exception.EEAException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

/**
 * The Class ExtendedSchemaRepositoryImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtendedSchemaRepositoryImplTest {

  /** The extended schema repository impl. */
  @InjectMocks
  private ExtendedSchemaRepositoryImpl extendedSchemaRepositoryImpl;

  /** The mongo operations. */
  @Mock
  private MongoOperations mongoOperations;

  /** The mongo template. */
  @Mock
  private MongoTemplate mongoTemplate;

  /** The mongo database. */
  @Mock
  private MongoDatabase mongoDatabase;

  @Mock
  private MongoCollection<Document> mongoCollection;

  @Mock
  private FieldSchema fieldSchema;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Delete table schema by id test.
   */
  @Test
  public void deleteTableSchemaByIdTest() {
    Mockito
        .when(mongoOperations.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    extendedSchemaRepositoryImpl.deleteTableSchemaById("5d4abe555b1c1e0001477410");
    Mockito.verify(mongoOperations, times(1)).updateMulti(Mockito.any(), Mockito.any(),
        Mockito.any(Class.class));
  }

  /**
   * Delete dataset schema by id test.
   */
  @Test
  public void deleteDatasetSchemaByIdTest() {
    Mockito.when(mongoTemplate.findAndRemove(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    extendedSchemaRepositoryImpl.deleteDatasetSchemaById("5d4abe555b1c1e0001477410");
    Mockito.verify(mongoTemplate, times(1)).findAndRemove(Mockito.any(), Mockito.any());
  }

  /**
   * Insert table schema test.
   */
  @Test
  public void insertTableSchemaTest() {
    Mockito
        .when(mongoOperations.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    extendedSchemaRepositoryImpl.insertTableSchema(new TableSchema(), "5d4abe555b1c1e0001477410");
    Mockito.verify(mongoOperations, times(1)).updateMulti(Mockito.any(), Mockito.any(),
        Mockito.any(Class.class));
  }

  /**
   * Delete field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteFieldSchemaTest1() throws EEAException {
    Mockito
        .when(mongoOperations.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertEquals(1L,
        extendedSchemaRepositoryImpl
            .deleteFieldSchema("5d4abe555b1c1e0001477410", "5d4abe555b1c1e0001477415")
            .getModifiedCount());
  }

  /**
   * Delete field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void deleteFieldSchemaTest2() throws EEAException {
    extendedSchemaRepositoryImpl.deleteFieldSchema("", "");
  }

  /**
   * Update field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest1() throws EEAException {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(fieldSchema.toJSON()).thenReturn("{\"_id\":\"sampleId\"}");
    Mockito.when(
        mongoCollection.updateMany(Mockito.any(), Mockito.any(), Mockito.any(UpdateOptions.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));

    Assert.assertEquals(1L, extendedSchemaRepositoryImpl
        .updateFieldSchema("5d5cfa24d201fb6084d90c34", fieldSchema).getModifiedCount());
  }

  /**
   * Update field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateFieldSchemaTest2() throws EEAException {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);

    extendedSchemaRepositoryImpl.updateFieldSchema("<id>", fieldSchema);
  }

  @Test
  public void findByIdTableSchemaTest() {
    Mockito.when(mongoTemplate.findOne(Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetSchema());
    extendedSchemaRepositoryImpl.findByIdTableSchema("5d4abe555b1c1e0001477410");
    Mockito.verify(mongoTemplate, times(1)).findOne(Mockito.any(), Mockito.any());
  }
}
