package org.eea.dataset.persistence.data.repository;

import static org.mockito.Mockito.times;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.ExtendedSchemaRepositoryImpl;
import org.eea.exception.EEAException;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.mongodb.client.FindIterable;
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

  /** The expected ex. */
  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  /** The mongo operations. */
  @Mock
  private MongoOperations mongoOperations;

  /** The mongo template. */
  @Mock
  private MongoTemplate mongoTemplate;

  /** The mongo database. */
  @Mock
  private MongoDatabase mongoDatabase;

  /** The mongo collection. */
  @Mock
  private MongoCollection<Document> mongoCollection;

  /** The field schema. */
  @Mock
  private Document fieldSchema;

  /** The table schema. */
  @Mock
  private Document tableSchema;

  /** The find. */
  @Mock
  private FindIterable<Document> find;

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

  /**
   * Creates the field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createFieldSchemaTest1() throws EEAException {
    Mockito
        .when(mongoOperations.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertEquals(UpdateResult.acknowledged(1L, 1L, null), extendedSchemaRepositoryImpl
        .createFieldSchema("5dd285cde8fd9d1ea8c42b1b", new FieldSchema()));
  }

  /**
   * Creates the field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createFieldSchemaTest2() throws EEAException {
    Mockito
        .when(mongoOperations.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenThrow(IllegalArgumentException.class);
    expectedEx.expect(EEAException.class);
    expectedEx.expectCause(IsInstanceOf.<Throwable>instanceOf(IllegalArgumentException.class));
    extendedSchemaRepositoryImpl.createFieldSchema("5dd285cde8fd9d1ea8c42b1b", new FieldSchema());
  }

  /**
   * Update table schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateTableSchemaTest1() throws EEAException {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(
        mongoCollection.updateOne(Mockito.any(), Mockito.any(), Mockito.any(UpdateOptions.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertEquals(UpdateResult.acknowledged(1L, 1L, null),
        extendedSchemaRepositoryImpl.updateTableSchema("5dd285cde8fd9d1ea8c42b1b", tableSchema));
  }

  /**
   * Update table schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateTableSchemaTest2() throws EEAException {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(
        mongoCollection.updateOne(Mockito.any(), Mockito.any(), Mockito.any(UpdateOptions.class)))
        .thenThrow(IllegalArgumentException.class);
    expectedEx.expect(EEAException.class);
    expectedEx.expectCause(IsInstanceOf.<Throwable>instanceOf(IllegalArgumentException.class));
    extendedSchemaRepositoryImpl.updateTableSchema("5dd285cde8fd9d1ea8c42b1b", tableSchema);
  }

  /**
   * Insert table in position test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertTableInPositionTest1() throws EEAException {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.updateOne(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertEquals(UpdateResult.acknowledged(1L, 1L, null), extendedSchemaRepositoryImpl
        .insertTableInPosition("5dd285cde8fd9d1ea8c42b1b", new Document(), 1));
  }

  /**
   * Insert table in position test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertTableInPositionTest2() throws EEAException {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    expectedEx.expect(EEAException.class);
    expectedEx.expectCause(IsInstanceOf.<Throwable>instanceOf(IllegalArgumentException.class));
    extendedSchemaRepositoryImpl.insertTableInPosition("", new Document(), 1);
  }

  /**
   * Insert field in position test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertFieldInPositionTest1() throws EEAException {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.updateMany(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertEquals(UpdateResult.acknowledged(1L, 1L, null), extendedSchemaRepositoryImpl
        .insertFieldInPosition("5dd285cde8fd9d1ea8c42b1b", new Document(), 1));
  }

  /**
   * Insert field in position test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertFieldInPositionTest2() throws EEAException {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    expectedEx.expect(EEAException.class);
    expectedEx.expectCause(IsInstanceOf.<Throwable>instanceOf(IllegalArgumentException.class));
    extendedSchemaRepositoryImpl.insertFieldInPosition("", new Document(), 1);
  }

  /**
   * Find table schema null test.
   */
  @Test
  public void findTableSchemaNullTest() {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.find(Mockito.any(Document.class))).thenReturn(find);
    Mockito.when(find.projection(Mockito.any())).thenReturn(find);
    Mockito.when(find.first()).thenReturn(null);

    Assert.assertNull(extendedSchemaRepositoryImpl.findTableSchema("5dd285cde8fd9d1ea8c42b1b",
        "5dd285cde8fd9d1ea8c42b1b"));
  }

  /**
   * Find table schema null document test.
   */
  @Test
  public void findTableSchemaNullDocumentTest() {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.find(Mockito.any(Document.class))).thenReturn(find);
    Mockito.when(find.projection(Mockito.any())).thenReturn(find);
    Mockito.when(find.first()).thenReturn(null);

    Assert.assertNull(extendedSchemaRepositoryImpl.findTableSchema("5dd285cde8fd9d1ea8c42b1b",
        "5dd285cde8fd9d1ea8c42b1b"));
  }

  /**
   * Find table schema null table schemas test.
   */
  @Test
  public void findTableSchemaNullTableSchemasTest() {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.find(Mockito.any(Document.class))).thenReturn(find);
    Mockito.when(find.projection(Mockito.any())).thenReturn(find);
    Mockito.when(find.first()).thenReturn(new Document());

    Assert.assertNull(extendedSchemaRepositoryImpl.findTableSchema("5dd285cde8fd9d1ea8c42b1b",
        "5dd285cde8fd9d1ea8c42b1b"));
  }

  /**
   * Find table schema null table schemas not array list test.
   */
  @Test
  public void findTableSchemaNullTableSchemasNotArrayListTest() {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.find(Mockito.any(Document.class))).thenReturn(find);
    Mockito.when(find.projection(Mockito.any())).thenReturn(find);
    Mockito.when(find.first()).thenReturn(new Document("tableSchemas", "string"));

    Assert.assertNull(extendedSchemaRepositoryImpl.findTableSchema("5dd285cde8fd9d1ea8c42b1b",
        "5dd285cde8fd9d1ea8c42b1b"));
  }

  /**
   * Find table schema null table schema test.
   */
  @Test
  public void findTableSchemaNullTableSchemaTest() {
    List<Document> list = new ArrayList<>();
    Document document = new Document("tableSchemas", list);

    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.find(Mockito.any(Document.class))).thenReturn(find);
    Mockito.when(find.projection(Mockito.any())).thenReturn(find);
    Mockito.when(find.first()).thenReturn(document);

    Assert.assertNull(extendedSchemaRepositoryImpl.findTableSchema("5dd285cde8fd9d1ea8c42b1b",
        "5dd285cde8fd9d1ea8c42b1b"));
  }

  /**
   * Find table schema null table schema not document test.
   */
  @Test
  public void findTableSchemaNullTableSchemaNotDocumentTest() {
    List<String> list = new ArrayList<>();
    list.add("string");
    Document document = new Document("tableSchemas", list);

    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.find(Mockito.any(Document.class))).thenReturn(find);
    Mockito.when(find.projection(Mockito.any())).thenReturn(find);
    Mockito.when(find.first()).thenReturn(document);

    Assert.assertNull(extendedSchemaRepositoryImpl.findTableSchema("5dd285cde8fd9d1ea8c42b1b",
        "5dd285cde8fd9d1ea8c42b1b"));
  }

  /**
   * Find table schema test.
   */
  @Test
  public void findTableSchemaTest() {
    List<Document> list = new ArrayList<>();
    list.add(new Document("key", "value"));
    Document document = new Document("tableSchemas", list);

    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.find(Mockito.any(Document.class))).thenReturn(find);
    Mockito.when(find.projection(Mockito.any())).thenReturn(find);
    Mockito.when(find.first()).thenReturn(document);

    Assert.assertEquals(new Document("key", "value"), extendedSchemaRepositoryImpl
        .findTableSchema("5dd285cde8fd9d1ea8c42b1b", "5dd285cde8fd9d1ea8c42b1b"));
  }

  /**
   * Find field schema null document test.
   */
  @Test
  public void findFieldSchemaNullDocumentTest() {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.find(Mockito.any(Document.class))).thenReturn(find);
    Mockito.when(find.projection(Mockito.any())).thenReturn(find);
    Mockito.when(find.first()).thenReturn(null);

    Assert.assertNull(extendedSchemaRepositoryImpl.findFieldSchema("5dd285cde8fd9d1ea8c42b1b",
        "5dd285cde8fd9d1ea8c42b1b"));
  }

  /**
   * Find field schema test.
   */
  @Test
  public void findFieldSchemaTest() {
    List<Document> list = new ArrayList<>();
    List<Document> list2 = new ArrayList<>();
    list2.add(new Document("_id", "5dd285cde8fd9d1ea8c42b1b"));
    list.add(new Document("recordSchema", new Document("fieldSchemas", list2)));
    Document document = new Document("tableSchemas", list);

    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.find(Mockito.any(Document.class))).thenReturn(find);
    Mockito.when(find.projection(Mockito.any())).thenReturn(find);
    Mockito.when(find.first()).thenReturn(document);

    Assert.assertEquals(new Document("_id", "5dd285cde8fd9d1ea8c42b1b"),
        extendedSchemaRepositoryImpl.findFieldSchema("5dd285cde8fd9d1ea8c42b1b",
            "5dd285cde8fd9d1ea8c42b1b"));
  }

  /**
   * Update dataset schema description test.
   */
  @Test
  public void updateDatasetSchemaDescriptionTest() {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.updateOne(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertEquals(UpdateResult.acknowledged(1L, 1L, null), extendedSchemaRepositoryImpl
        .updateDatasetSchemaDescription("5dd285cde8fd9d1ea8c42b1b", "description"));
  }

  /**
   * Find record schema null document test.
   */
  @Test
  public void findRecordSchemaNullDocumentTest() {
    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.find(Mockito.any(Document.class))).thenReturn(find);
    Mockito.when(find.projection(Mockito.any())).thenReturn(find);
    Mockito.when(find.first()).thenReturn(null);

    Assert.assertNull(extendedSchemaRepositoryImpl.findRecordSchema("5dd285cde8fd9d1ea8c42b1b",
        "5dd285cde8fd9d1ea8c42b1b"));
  }

  /**
   * Find record schema null record schema test.
   */
  @Test
  public void findRecordSchemaNullRecordSchemaTest() {
    List<Document> list = new ArrayList<>();
    list.add(new Document());
    Document document = new Document("tableSchemas", list);

    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.find(Mockito.any(Document.class))).thenReturn(find);
    Mockito.when(find.projection(Mockito.any())).thenReturn(find);
    Mockito.when(find.first()).thenReturn(document);

    Assert.assertNull(extendedSchemaRepositoryImpl.findRecordSchema("5dd285cde8fd9d1ea8c42b1b",
        "5dd285cde8fd9d1ea8c42b1b"));
  }

  /**
   * Find record schema test.
   */
  @Test
  public void findRecordSchemaTest() {
    Document response = new Document();
    Document recordSchema = new Document("recordSchema", response);
    List<Document> list = new ArrayList<>();
    list.add(recordSchema);
    Document document = new Document("tableSchemas", list);

    Mockito.when(mongoDatabase.getCollection(Mockito.any())).thenReturn(mongoCollection);
    Mockito.when(mongoCollection.find(Mockito.any(Document.class))).thenReturn(find);
    Mockito.when(find.projection(Mockito.any())).thenReturn(find);
    Mockito.when(find.first()).thenReturn(document);

    Assert.assertEquals(response, extendedSchemaRepositoryImpl
        .findRecordSchema("5dd285cde8fd9d1ea8c42b1b", "5dd285cde8fd9d1ea8c42b1b"));
  }
}
