package org.eea.dataset.service.file;

import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class ExcelReaderStrategyTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExcelReaderStrategyTest {

  /** The excel reader strategy. */
  @InjectMocks
  private ExcelReaderStrategy excelReaderStrategy;

  /** The parse common. */
  @Mock
  private FileCommonUtils fileCommon;

  /** The file in. */
  private ByteArrayInputStream fileIn;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("BWQ_2006_SPAIN_2018_V1_shortter");

    XSSFRow rowhead = sheet.createRow(0);
    rowhead.createCell(0).setCellValue("BWID");
    rowhead.createCell(1).setCellValue("GroupID");
    rowhead.createCell(2).setCellValue("StartDateS");
    rowhead.createCell(3).setCellValue("EndDateS");
    rowhead.createCell(4).setCellValue("DummiColumn");
    rowhead.createCell(5).setCellValue("");

    XSSFRow row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue("ES511M270688");
    row1.createCell(1).setCellValue("na");
    row1.createCell(2).setCellValue("16/07/2018");
    row1.createCell(3).setCellValue("19/07/2018");
    row1.createCell(4).setCellValue("DummiCell");
    row1.createCell(5).setCellValue("ThisValueShouldNotBeReaden");

    XSSFRow row2 = sheet.createRow(2);
    row2.createCell(0).setCellValue("ES512M118746");
    row2.createCell(1).setCellValue("na");
    row2.createCell(2).setCellValue("16/07/2018");
    row2.createCell(4).setCellValue("DummiCell");

    XSSFRow row3 = sheet.createRow(3);
    row3.createCell(0).setCellValue("");
    row3.createCell(1).setCellValue("");
    row3.createCell(2).setCellValue("");
    row3.createCell(3).setCellValue("");
    row3.createCell(4).setCellValue("");

    XSSFRow row4 = sheet.createRow(4);
    row4.createCell(0).setCellValue("ES522M085993");
    row4.createCell(1).setCellValue("na");
    row4.createCell(2).setCellValue("31/07/2018");
    row4.createCell(3).setCellValue("02/08/2018");
    row4.createCell(4).setCellValue("DummiCell");

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    workbook.write(outStream);
    workbook.close();
    outStream.close();


    fileIn = new ByteArrayInputStream(outStream.toByteArray());
  }

  /**
   * Test parse file.
   *
   * @throws EncryptedDocumentException the encrypted document exception
   * @throws InvalidFormatException the invalid format exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException
   */
  @Test
  public void testParseFile()
      throws EncryptedDocumentException, InvalidFormatException, IOException, EEAException {
    DataSetSchemaVO dataset = new DataSetSchemaVO();
    Mockito.when(fileCommon.getDataSetSchema(Mockito.any(), Mockito.any())).thenReturn(dataset);
    assertNotNull("is null", excelReaderStrategy.parseFile(fileIn, 1L, 1L, ""));
  }

  @Test
  public void testParseFileNotNull()
      throws EncryptedDocumentException, InvalidFormatException, IOException, EEAException {
    DataSetSchemaVO dataset = new DataSetSchemaVO();
    Mockito.when(fileCommon.getDataSetSchema(Mockito.any(), Mockito.any())).thenReturn(dataset);
    Mockito.when(fileCommon.findIdFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new FieldSchemaVO());
    assertNotNull("is null", excelReaderStrategy.parseFile(fileIn, 1L, 1L, ""));
  }

  /**
   * Test parse file 2.
   *
   * @throws EncryptedDocumentException the encrypted document exception
   * @throws InvalidFormatException the invalid format exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException
   */
  @Test
  public void testParseFile2()
      throws EncryptedDocumentException, InvalidFormatException, IOException, EEAException {
    assertNotNull("is null", excelReaderStrategy.parseFile(fileIn, 1L, 1L, ""));
  }

  @Test
  public void testParseAllPages() throws EEAException {
    assertNotNull("is null", excelReaderStrategy.parseFile(fileIn, 1L, 1L, null));
  }

  /**
   * Test parse file exception.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException
   */
  @Test(expected = InvalidFileException.class)
  public void testParseFileException() throws IOException, EEAException {
    XSSFWorkbook workbook = new XSSFWorkbook();

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    workbook.write(outStream);
    workbook.close();
    outStream.close();

    ByteArrayInputStream fileInAux = new ByteArrayInputStream(outStream.toByteArray());

    excelReaderStrategy.parseFile(fileInAux, 1L, 1L, "");
  }

}
