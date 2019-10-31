package org.eea.dataset.persistence.data.repository;

import static org.mockito.Mockito.times;
import java.io.IOException;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.ExtendedSchemaRepositoryImpl;
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
import com.mongodb.client.result.UpdateResult;

@RunWith(MockitoJUnitRunner.class)
public class ExtendedSchemaRepositoryImplTest {

  @InjectMocks
  private ExtendedSchemaRepositoryImpl extendedSchemaRepositoryImpl;

  @Mock
  private MongoOperations mongoOperations;

  @Mock
  private MongoTemplate mongoTemplate;

  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void deleteTableSchemaByIdTest() {
    Mockito
        .when(mongoOperations.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    extendedSchemaRepositoryImpl.deleteTableSchemaById("5d4abe555b1c1e0001477410");
    Mockito.verify(mongoOperations, times(1)).updateMulti(Mockito.any(), Mockito.any(),
        Mockito.any(Class.class));
  }

  @Test
  public void deleteDatasetSchemaByIdTest() {
    Mockito.when(mongoTemplate.findAndRemove(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    extendedSchemaRepositoryImpl.deleteDatasetSchemaById("5d4abe555b1c1e0001477410");
    Mockito.verify(mongoTemplate, times(1)).findAndRemove(Mockito.any(), Mockito.any());
  }

  @Test
  public void insertTableSchemaTest() {
    Mockito
        .when(mongoOperations.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    extendedSchemaRepositoryImpl.insertTableSchema(new TableSchema(), "5d4abe555b1c1e0001477410");
    Mockito.verify(mongoOperations, times(1)).updateMulti(Mockito.any(), Mockito.any(),
        Mockito.any(Class.class));
  }

  @Test
  public void deleteFieldSchemaTest() {
    Mockito
        .when(mongoOperations.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertEquals(1L,
        extendedSchemaRepositoryImpl
            .deleteFieldSchema("5d4abe555b1c1e0001477410", "5d4abe555b1c1e0001477415")
            .getModifiedCount());
  }
}
