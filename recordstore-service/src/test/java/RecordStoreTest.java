import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.eea.kafka.io.KafkaSender;
import org.eea.recordstore.controller.RecordStoreControllerImpl;
import org.eea.recordstore.exception.DockerAccessException;
import org.eea.recordstore.service.DockerInterfaceService;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.recordstore.service.impl.DockerClientBuilderBean;
import org.eea.recordstore.service.impl.DockerInterfaceServiceImpl;
import org.eea.recordstore.service.impl.RecordStoreServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import com.github.dockerjava.api.model.Container;

@RunWith(MockitoJUnitRunner.class)
public class RecordStoreTest {

  @InjectMocks
  RecordStoreServiceImpl recordStoreServiceImpl;
  
  @InjectMocks
  RecordStoreControllerImpl recordStoreControllerImpl;
  
  @InjectMocks
  DockerInterfaceServiceImpl dockerInterfaceServiceImpl;
  
  @Mock
  RecordStoreService recordStoreService;
  
  @Mock
  DockerInterfaceService dockerInterfaceService;
  @Mock
  KafkaSender kafkaSender;
  @Mock
  DockerClientBuilderBean dockerClient;
  
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }
  
  
  @Test
  public void testCreateDataset() throws DockerAccessException{
    
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONTAINER_NAME", "crunchy-postgres");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "IP_POSTGRE_DB", "localhost");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "USER_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "PASS_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONN_STRING_POSTGRE", "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "SQL_GET_DATASETS_NAME", "select * from pg_namespace where nspname like 'dataset%'");
    
    //when(dockerInterfaceService.createContainer(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new Container());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    recordStoreServiceImpl.createEmptyDataSet("test");

  }
  
  @Test
  public void testResetDataset() throws DockerAccessException{
    
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONTAINER_NAME", "crunchy-postgres");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "IP_POSTGRE_DB", "localhost");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "USER_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "PASS_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONN_STRING_POSTGRE", "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "SQL_GET_DATASETS_NAME", "select * from pg_namespace where nspname like 'dataset%'");
    
    recordStoreServiceImpl.resetDatasetDatabase();
    
  }
  
  @Test
  public void testConnectionData() throws DockerAccessException{
    
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONTAINER_NAME", "crunchy-postgres");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "IP_POSTGRE_DB", "localhost");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "USER_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "PASS_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONN_STRING_POSTGRE", "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "SQL_GET_DATASETS_NAME", "select * from pg_namespace where nspname like 'dataset%'");
    
    recordStoreServiceImpl.getConnectionDataForDataset("test");
    
  }
  
  @Test
  public void testConnectionData2() throws DockerAccessException{
    
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONTAINER_NAME", "crunchy-postgres");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "IP_POSTGRE_DB", "localhost");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "USER_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "PASS_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONN_STRING_POSTGRE", "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "SQL_GET_DATASETS_NAME", "select * from pg_namespace where nspname like 'dataset%'");
    
    recordStoreServiceImpl.getConnectionDataForDataset();
    
  }
  
  @Test(expected = UnsupportedOperationException.class)
  public void testCreateDataSetFromAnother() throws DockerAccessException{
    
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONTAINER_NAME", "crunchy-postgres");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "IP_POSTGRE_DB", "localhost");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "USER_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "PASS_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONN_STRING_POSTGRE", "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "SQL_GET_DATASETS_NAME", "select * from pg_namespace where nspname like 'dataset%'");
    
    recordStoreServiceImpl.createDataSetFromOther("test","test");
    
  }
  
  @Test
  public void testCreateEmptyDataSetController() throws DockerAccessException{
    
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONTAINER_NAME", "crunchy-postgres");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "IP_POSTGRE_DB", "localhost");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "USER_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "PASS_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONN_STRING_POSTGRE", "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "SQL_GET_DATASETS_NAME", "select * from pg_namespace where nspname like 'dataset%'");
    
    recordStoreControllerImpl.createEmptyDataset("test");
    
  }
  
  @Test
  public void testResetDataSetController() throws DockerAccessException{
    
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONTAINER_NAME", "crunchy-postgres");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "IP_POSTGRE_DB", "localhost");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "USER_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "PASS_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONN_STRING_POSTGRE", "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "SQL_GET_DATASETS_NAME", "select * from pg_namespace where nspname like 'dataset%'");
    
    recordStoreControllerImpl.resteDataSetDataBase();
    
  }
  
  @Test
  public void testConnectionController() throws DockerAccessException{
    
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONTAINER_NAME", "crunchy-postgres");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "IP_POSTGRE_DB", "localhost");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "USER_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "PASS_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONN_STRING_POSTGRE", "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "SQL_GET_DATASETS_NAME", "select * from pg_namespace where nspname like 'dataset%'");
    
    recordStoreControllerImpl.getConnectionToDataset("test");
    
  }
  
  @Test
  public void testCreateContainer() throws DockerAccessException{
    
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONTAINER_NAME", "crunchy-postgres");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "IP_POSTGRE_DB", "localhost");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "USER_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "PASS_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONN_STRING_POSTGRE", "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "SQL_GET_DATASETS_NAME", "select * from pg_namespace where nspname like 'dataset%'");
    ReflectionTestUtils.setField(dockerInterfaceServiceImpl, "containerName", "crunchy-postgres");
    ReflectionTestUtils.setField(dockerInterfaceServiceImpl, "envs", new ArrayList<>());
    
    when(dockerInterfaceServiceImpl.createContainer(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new Container());
    dockerInterfaceServiceImpl.createContainer("test","test","0000");
    
  }
  
}
