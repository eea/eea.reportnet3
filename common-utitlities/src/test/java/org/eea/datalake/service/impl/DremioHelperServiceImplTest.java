package org.eea.datalake.service.impl;

import feign.FeignException;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.exception.DremioApiException;
import org.eea.interfaces.controller.dremio.controller.DremioApiController;
import org.eea.interfaces.vo.dremio.DremioAuthResponse;
import org.eea.interfaces.vo.dremio.DremioDirectoryItemsResponse;
import org.eea.interfaces.vo.dremio.DremioSqlResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import static org.eea.utils.LiteralConstants.S3_IMPORT_FILE_PATH;
import static org.eea.utils.LiteralConstants.S3_IMPORT_TABLE_NAME_FOLDER_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class DremioHelperServiceImplTest {

    @InjectMocks
    private DremioHelperServiceImpl dremioHelperService;

    @Mock
    private DremioApiController dremioApiController;

    @Mock
    private S3Service s3Service;

    @Mock
    private FeignException feignException;

    @Before
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getDirectoryItemsSuccessTest(){
        String testDirectoryPath = "testDirectoryPath";
        S3PathResolver s3PathResolver = new S3PathResolver(1L, 1L, 1L, "test", "test", S3_IMPORT_FILE_PATH);
        Mockito.when(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)).thenReturn(testDirectoryPath);
        Mockito.when(dremioApiController.getDirectoryItems(anyString(), anyString())).thenReturn(new DremioDirectoryItemsResponse());
        dremioHelperService.getDirectoryItems(s3PathResolver, "test");
    }

    @Test(expected = DremioApiException.class)
    public void getDirectoryItemsThrowsDremioApiException(){
        String testDirectoryPath = "testDirectoryPath";
        S3PathResolver s3PathResolver = new S3PathResolver(1L, 1L, 1L, "test", "test", S3_IMPORT_FILE_PATH);
        Mockito.when(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)).thenReturn(testDirectoryPath);
        Mockito.when(feignException.status()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Mockito.when(dremioApiController.getDirectoryItems(any(), anyString())).thenThrow(feignException);
        dremioHelperService.getDirectoryItems(s3PathResolver, "test");
    }


    @Test
    public void getDirectoryItemsInvalidTokenTestSuccess(){
        String testDirectoryPath = "testDirectoryPath";
        DremioAuthResponse dremioAuthResponse = new DremioAuthResponse();
        dremioAuthResponse.setToken("token");
        S3PathResolver s3PathResolver = new S3PathResolver(1L, 1L, 1L, "test", "test", S3_IMPORT_FILE_PATH);
        Mockito.when(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)).thenReturn(testDirectoryPath);
        Mockito.when(feignException.status()).thenReturn(HttpStatus.UNAUTHORIZED.value());
        Mockito.when(dremioApiController.getDirectoryItems(any(), anyString())).thenThrow(feignException).thenReturn(new DremioDirectoryItemsResponse());
        Mockito.when(dremioApiController.login(any())).thenReturn(dremioAuthResponse);
        dremioHelperService.getDirectoryItems(s3PathResolver, "test");
    }

    @Test(expected = DremioApiException.class)
    public void getDirectoryItemsInvalidTokenTestThrowsDremioApiException(){
        String testDirectoryPath = "testDirectoryPath";
        DremioAuthResponse dremioAuthResponse = new DremioAuthResponse();
        dremioAuthResponse.setToken("token");
        S3PathResolver s3PathResolver = new S3PathResolver(1L, 1L, 1L, "test", "test", S3_IMPORT_FILE_PATH);
        Mockito.when(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)).thenReturn(testDirectoryPath);
        Mockito.when(feignException.status()).thenReturn(HttpStatus.UNAUTHORIZED.value());
        Mockito.when(dremioApiController.getDirectoryItems(any(), anyString())).thenThrow(feignException).thenThrow(feignException);
        Mockito.when(dremioApiController.login(any())).thenReturn(dremioAuthResponse);
        dremioHelperService.getDirectoryItems(s3PathResolver, "test");
    }

    @Test
    public void promoteFolderOrFileSuccessTest(){
        String testDirectoryPath = "testDirectoryPath";
        S3PathResolver s3PathResolver = new S3PathResolver(1L, 1L, 1L, "test", "test", S3_IMPORT_FILE_PATH);
        Mockito.when(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)).thenReturn(testDirectoryPath);
        DremioHelperServiceImpl spyDremioHelperService = Mockito.spy(dremioHelperService);
        Mockito.doReturn(false).when(spyDremioHelperService).checkFolderPromoted(any(), anyString());
        Mockito.doReturn("test").when(spyDremioHelperService).getFolderId(any(), anyString());
        spyDremioHelperService.promoteFolderOrFile(s3PathResolver, "test");
    }

    @Test(expected = DremioApiException.class)
    public void promoteFolderOrFileThrowsDremioApiException(){
        String testDirectoryPath = "testDirectoryPath";
        S3PathResolver s3PathResolver = new S3PathResolver(1L, 1L, 1L, "test", "test", S3_IMPORT_FILE_PATH);
        Mockito.when(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)).thenReturn(testDirectoryPath);
        Mockito.when(dremioApiController.getDirectoryItems(anyString(), anyString())).thenReturn(new DremioDirectoryItemsResponse());
        DremioHelperServiceImpl spyDremioHelperService = Mockito.spy(dremioHelperService);
        Mockito.doReturn(false).when(spyDremioHelperService).checkFolderPromoted(any(), anyString());
        Mockito.doReturn("test").when(spyDremioHelperService).getFolderId(any(), anyString());
        Mockito.when(feignException.status()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Mockito.doThrow(feignException).when(dremioApiController).promote(any(), anyString(), any());
        spyDremioHelperService.promoteFolderOrFile(s3PathResolver, "test");
    }


    @Test
    public void promoteFolderOrFileInvalidTokenTestSuccess(){
        String testDirectoryPath = "testDirectoryPath";
        DremioAuthResponse dremioAuthResponse = new DremioAuthResponse();
        dremioAuthResponse.setToken("token");
        S3PathResolver s3PathResolver = new S3PathResolver(1L, 1L, 1L, "test", "test", S3_IMPORT_FILE_PATH);
        Mockito.when(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)).thenReturn(testDirectoryPath);
        Mockito.when(dremioApiController.getDirectoryItems(anyString(), anyString())).thenReturn(new DremioDirectoryItemsResponse());
        DremioHelperServiceImpl spyDremioHelperService = Mockito.spy(dremioHelperService);
        Mockito.doReturn(false).when(spyDremioHelperService).checkFolderPromoted(any(), anyString());
        Mockito.doReturn("test").when(spyDremioHelperService).getFolderId(any(), anyString());
        Mockito.when(feignException.status()).thenReturn(HttpStatus.UNAUTHORIZED.value());
        Mockito.doThrow(feignException).doNothing().when(dremioApiController).promote(any(), anyString(), any());
        Mockito.when(dremioApiController.login(any())).thenReturn(dremioAuthResponse);
        spyDremioHelperService.promoteFolderOrFile(s3PathResolver, "test");
    }

    @Test(expected = DremioApiException.class)
    public void promoteFolderOrFileInvalidTokenTestThrowsDremioApiException(){
        String testDirectoryPath = "testDirectoryPath";
        DremioAuthResponse dremioAuthResponse = new DremioAuthResponse();
        dremioAuthResponse.setToken("token");
        S3PathResolver s3PathResolver = new S3PathResolver(1L, 1L, 1L, "test", "test", S3_IMPORT_FILE_PATH);
        Mockito.when(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)).thenReturn(testDirectoryPath);
        Mockito.when(dremioApiController.getDirectoryItems(anyString(), anyString())).thenReturn(new DremioDirectoryItemsResponse());
        DremioHelperServiceImpl spyDremioHelperService = Mockito.spy(dremioHelperService);
        Mockito.doReturn(false).when(spyDremioHelperService).checkFolderPromoted(any(), anyString());
        Mockito.doReturn("test").when(spyDremioHelperService).getFolderId(any(), anyString());
        Mockito.when(feignException.status()).thenReturn(HttpStatus.UNAUTHORIZED.value());
        Mockito.doThrow(feignException).doThrow(feignException).when(dremioApiController).promote(any(), anyString(), any());
        Mockito.when(dremioApiController.login(any())).thenReturn(dremioAuthResponse);
        spyDremioHelperService.promoteFolderOrFile(s3PathResolver, "test");
    }

    @Test
    public void demoteFolderOrFileSuccessTest(){
        String testDirectoryPath = "testDirectoryPath";
        S3PathResolver s3PathResolver = new S3PathResolver(1L, 1L, 1L, "test", "test", S3_IMPORT_FILE_PATH);
        Mockito.when(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)).thenReturn(testDirectoryPath);
        DremioHelperServiceImpl spyDremioHelperService = Mockito.spy(dremioHelperService);
        Mockito.doReturn(true).when(spyDremioHelperService).checkFolderPromoted(any(), anyString());
        Mockito.doReturn("test").when(spyDremioHelperService).getFolderId(any(), anyString());
        spyDremioHelperService.demoteFolderOrFile(s3PathResolver, "test");
    }

    @Test(expected = DremioApiException.class)
    public void demoteFolderOrFileThrowsDremioApiException(){
        String testDirectoryPath = "testDirectoryPath";
        S3PathResolver s3PathResolver = new S3PathResolver(1L, 1L, 1L, "test", "test", S3_IMPORT_FILE_PATH);
        Mockito.when(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)).thenReturn(testDirectoryPath);
        Mockito.when(dremioApiController.getDirectoryItems(anyString(), anyString())).thenReturn(new DremioDirectoryItemsResponse());
        DremioHelperServiceImpl spyDremioHelperService = Mockito.spy(dremioHelperService);
        Mockito.doReturn(true).when(spyDremioHelperService).checkFolderPromoted(any(), anyString());
        Mockito.doReturn("test").when(spyDremioHelperService).getFolderId(any(), anyString());
        Mockito.when(feignException.status()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Mockito.doThrow(feignException).when(dremioApiController).demote(any(), anyString());
        spyDremioHelperService.demoteFolderOrFile(s3PathResolver, "test");
    }


    @Test
    public void demoteFolderOrFileInvalidTokenTestSuccess(){
        String testDirectoryPath = "testDirectoryPath";
        DremioAuthResponse dremioAuthResponse = new DremioAuthResponse();
        dremioAuthResponse.setToken("token");
        S3PathResolver s3PathResolver = new S3PathResolver(1L, 1L, 1L, "test", "test", S3_IMPORT_FILE_PATH);
        Mockito.when(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)).thenReturn(testDirectoryPath);
        Mockito.when(dremioApiController.getDirectoryItems(anyString(), anyString())).thenReturn(new DremioDirectoryItemsResponse());
        DremioHelperServiceImpl spyDremioHelperService = Mockito.spy(dremioHelperService);
        Mockito.doReturn(true).when(spyDremioHelperService).checkFolderPromoted(any(), anyString());
        Mockito.doReturn("test").when(spyDremioHelperService).getFolderId(any(), anyString());
        Mockito.when(feignException.status()).thenReturn(HttpStatus.UNAUTHORIZED.value());
        Mockito.doThrow(feignException).doNothing().when(dremioApiController).demote(any(), anyString());
        Mockito.when(dremioApiController.login(any())).thenReturn(dremioAuthResponse);
        spyDremioHelperService.demoteFolderOrFile(s3PathResolver, "test");
    }

    @Test(expected = DremioApiException.class)
    public void demoteFolderOrFileInvalidTokenTestThrowsDremioApiException(){
        String testDirectoryPath = "testDirectoryPath";
        DremioAuthResponse dremioAuthResponse = new DremioAuthResponse();
        dremioAuthResponse.setToken("token");
        S3PathResolver s3PathResolver = new S3PathResolver(1L, 1L, 1L, "test", "test", S3_IMPORT_FILE_PATH);
        Mockito.when(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)).thenReturn(testDirectoryPath);
        Mockito.when(dremioApiController.getDirectoryItems(anyString(), anyString())).thenReturn(new DremioDirectoryItemsResponse());
        DremioHelperServiceImpl spyDremioHelperService = Mockito.spy(dremioHelperService);
        Mockito.doReturn(true).when(spyDremioHelperService).checkFolderPromoted(any(), anyString());
        Mockito.doReturn("test").when(spyDremioHelperService).getFolderId(any(), anyString());
        Mockito.when(feignException.status()).thenReturn(HttpStatus.UNAUTHORIZED.value());
        Mockito.doThrow(feignException).doThrow(feignException).when(dremioApiController).demote(any(), anyString());
        Mockito.when(dremioApiController.login(any())).thenReturn(dremioAuthResponse);
        spyDremioHelperService.demoteFolderOrFile(s3PathResolver, "test");
    }


    @Test
    public void executeSqlStatementSuccessTest(){
        String sqlStatement = "sqlStatement";
        Mockito.when(dremioApiController.sqlQuery(any(), any())).thenReturn(new DremioSqlResponse());
        dremioHelperService.executeSqlStatement(sqlStatement);
    }

    @Test(expected = DremioApiException.class)
    public void executeSqlStatementThrowsDremioApiException(){
        String sqlStatement = "sqlStatement";
        Mockito.when(feignException.status()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Mockito.when(dremioApiController.sqlQuery(any(), any())).thenThrow(feignException);
        dremioHelperService.executeSqlStatement(sqlStatement);
    }


    @Test
    public void executeSqlStatementInvalidTokenTestSuccess(){
        DremioAuthResponse dremioAuthResponse = new DremioAuthResponse();
        dremioAuthResponse.setToken("token");
        String sqlStatement = "sqlStatement";
        Mockito.when(feignException.status()).thenReturn(HttpStatus.UNAUTHORIZED.value());
        Mockito.when(dremioApiController.sqlQuery(any(), any())).thenThrow(feignException).thenReturn(new DremioSqlResponse());
        Mockito.when(dremioApiController.login(any())).thenReturn(dremioAuthResponse);
        dremioHelperService.executeSqlStatement(sqlStatement);

    }

    @Test(expected = DremioApiException.class)
    public void executeSqlStatementInvalidTokenTestThrowsDremioApiException(){
        DremioAuthResponse dremioAuthResponse = new DremioAuthResponse();
        dremioAuthResponse.setToken("token");
        String sqlStatement = "sqlStatement";
        Mockito.when(feignException.status()).thenReturn(HttpStatus.UNAUTHORIZED.value());
        Mockito.when(dremioApiController.sqlQuery(any(), any())).thenThrow(feignException).thenThrow(feignException);
        Mockito.when(dremioApiController.login(any())).thenReturn(dremioAuthResponse);
        dremioHelperService.executeSqlStatement(sqlStatement);
    }




}
