package org.eea.dataset.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.eea.dataset.service.file.FileParserFactory;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.kafka.io.KafkaSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class DatasetServiceTest {

	@InjectMocks
	DatasetServiceImpl datasetService;

	@Mock
	IFileParseContext context;

	@Mock
	FileParserFactory fileParserFactory;

//	@Mock
//	DatasetRepository datasetRepository;

	@Mock
	KafkaSender kafkaSender;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = EEAException.class)
	public void testProcessFileThrowExceptionFileNull() throws Exception {
		datasetService.processFile("1", null);
	}

	@Test(expected = EEAException.class)
	public void testProcessFileThrowException() throws Exception {
		MockMultipartFile fileNoExtension = new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
		datasetService.processFile(null, fileNoExtension);
	}

	@Test
	public void testProcessFileThrowException2() throws Exception {
		MockMultipartFile fileNoExtension = new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());

		datasetService.processFile("1", fileNoExtension);
	}

	@Test
	public void testProcessFileThrowException4() throws Exception {
		MockMultipartFile fileNoType = new MockMultipartFile("file", "fileOriginal.csv", null, "content".getBytes());

		datasetService.processFile("1", fileNoType);
	}

	// @Test
	public void testProcessFileThrowException5() throws Exception {
		MockMultipartFile fileNoType = new MockMultipartFile("file", "fileOriginal.csv", null, "content".getBytes());
		doThrow(new EEAException()).when(context).parse(Mockito.any(InputStream.class));
		datasetService.processFile("1", fileNoType);
	}

	@Test
	public void testProcessFileEmptyDataset() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
		when(fileParserFactory.createContext(Mockito.anyString())).thenReturn(context);
		when(context.parse(Mockito.any(InputStream.class))).thenReturn(null);

		datasetService.processFile("1", file);
	}

	@Test
	public void testProcessFileSuccess() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
		when(fileParserFactory.createContext(Mockito.anyString())).thenReturn(context);
		when(context.parse(Mockito.any(InputStream.class))).thenReturn(new DataSetVO());
//		when(datasetRepository.save(Mockito.any())).thenReturn(new DataSetVO());
		doNothing().when(kafkaSender).sendMessage(Mockito.any());
		datasetService.processFile("1", file);
	}

}
