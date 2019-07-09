package org.eea.dataset.service.file;

import static org.junit.Assert.assertNotNull;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExcelReaderStrategyTest {

  @InjectMocks
  private ExcelReaderStrategy excelReaderStrategy;

  @Mock
  private ParseCommon parseCommon;

  private FileInputStream fileIn;

  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
    String filename = "testExcelFile.xlsx";
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("BWQ_2006_SPAIN_2018_V1_shortter");

    XSSFRow rowhead = sheet.createRow(0);
    rowhead.createCell(0).setCellValue("BWID");
    rowhead.createCell(1).setCellValue("GroupID");
    rowhead.createCell(2).setCellValue("StartDateS");
    rowhead.createCell(3).setCellValue("EndDateS");

    XSSFRow row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue("ES511M270688");
    row1.createCell(1).setCellValue("na");
    row1.createCell(2).setCellValue("16/07/2018");
    row1.createCell(3).setCellValue("19/07/2018");

    XSSFRow row2 = sheet.createRow(2);
    row2.createCell(0).setCellValue("ES512M118746");
    row2.createCell(1).setCellValue("na");
    row2.createCell(2).setCellValue("16/07/2018");

    XSSFRow row3 = sheet.createRow(3);
    row3.createCell(0).setCellValue("ES522M085993");
    row3.createCell(1).setCellValue("na");
    row3.createCell(2).setCellValue("31/07/2018");
    row3.createCell(3).setCellValue("02/08/2018");

    FileOutputStream fileOut = new FileOutputStream(filename);

    workbook.write(fileOut);
    fileOut.close();
    workbook.close();

    fileIn = new FileInputStream(filename);
  }

  @Test
  public void testParseFile()
      throws InvalidFileException, EncryptedDocumentException, InvalidFormatException, IOException {
    Mockito.when(parseCommon.getDataSetSchema(Mockito.any())).thenReturn(new DataSetSchemaVO());
    assertNotNull("is null", excelReaderStrategy.parseFile(fileIn, 1L, 1L, ""));
  }

  @Test
  public void testParseFile2()
      throws InvalidFileException, EncryptedDocumentException, InvalidFormatException, IOException {
    assertNotNull("is null", excelReaderStrategy.parseFile(fileIn, 1L, 1L, ""));
  }

  @Test(expected = InvalidFileException.class)
  public void testParseFileException() throws IOException, InvalidFileException {
    String filename = "testExcelFileException.xlsx";
    XSSFWorkbook workbook = new XSSFWorkbook();

    FileOutputStream fileOut = new FileOutputStream(filename);

    workbook.write(fileOut);
    fileOut.close();
    workbook.close();

    FileInputStream fileInAux = new FileInputStream(fileOut.getFD());

    excelReaderStrategy.parseFile(fileInAux, 1L, 1L, "");
  }

}
