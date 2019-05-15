package org.eea.dataset.service;

import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.eea.dataset.service.file.FileParserFactory;
import org.eea.dataset.service.file.interfaces.FileParseContext;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.interfaces.vo.dataset.DataSetVO;
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
	FileParseContext context;
	
	@Mock	
	FileParserFactory fileParserFactory;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = Exception.class)
	public void testProcessFileThrowException() throws Exception {
		datasetService.processFile(null);
	}

	@Test(expected = Exception.class)
	public void testProcessFileThrowException2() throws Exception {
		MockMultipartFile fileNoExtension = new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());

		datasetService.processFile(fileNoExtension);
	}

	@Test(expected = Exception.class)
	public void testProcessFileEmptyDataset() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
		when(fileParserFactory.createContext(Mockito.anyString())).thenReturn(context);
		when(context.parse(Mockito.any(InputStream.class))).thenReturn(null);
		
		datasetService.processFile(file);
	}
	
	@Test
	public void testProcessFileSuccess() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
		when(fileParserFactory.createContext(Mockito.anyString())).thenReturn(context);
		when(context.parse(Mockito.any(InputStream.class))).thenReturn(new DataSetVO());
		
		datasetService.processFile(file);
	}
	
}
