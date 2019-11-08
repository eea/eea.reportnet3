package org.eea.ums.controller;


import static org.mockito.Mockito.times;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.eea.ums.service.BackupManagmentService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

public class BackupManagmentControlerImplTest {

  @InjectMocks
  private BackupManagmentControlerImpl backupManagmentControlerImpl;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Mock
  BackupManagmentService backupManagmentControlerService;

  @Test
  public void readExcelTest() throws IOException {
    MockMultipartFile file = new MockMultipartFile("files", "filename.txt", "text/plain",
        "hello".getBytes(StandardCharsets.UTF_8));

    backupManagmentControlerImpl.setUsers(file);
    Mockito.verify(backupManagmentControlerService, times(1))
        .readExcelDatatoKeyCloack(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void readExcelFailTest() throws IOException {
    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Not found");
  }
}
