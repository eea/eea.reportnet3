package org.eea.dataset.service.pdf;

import static org.mockito.Mockito.times;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.metabase.ReleaseReceiptVO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * The Class ReceiptPDFGeneratorTest.
 */
public class ReceiptPDFGeneratorTest {

  /** The receipt PDF generator. */
  @InjectMocks
  private ReceiptPDFGenerator receiptPDFGenerator;

  /** The out. */
  @Mock
  OutputStream out;

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
   * Test generate PDF.
   */
  @Test
  public void testGeneratePDF() {
    List<ReportingDatasetVO> datasets = new ArrayList<>();
    ReportingDatasetVO dataset = new ReportingDatasetVO();
    dataset.setNameDatasetSchema("");
    datasets.add(dataset);
    dataset.setDateReleased(new Date());
    ReleaseReceiptVO receipt = new ReleaseReceiptVO();
    receipt.setDataflowName("word ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
    receipt.setDatasets(datasets);
    receipt.setProviderAssignation("");
    receipt.setObligationId(1);
    receipt.setObligationTitle("");
    ReceiptPDFGenerator spyClass = Mockito.spy(receiptPDFGenerator);
    spyClass.generatePDF(receipt, out);
    Mockito.verify(spyClass, times(1)).generatePDF(Mockito.any(), Mockito.any());
  }

}
