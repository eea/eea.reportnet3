package org.eea.dataset.service.pdf;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.metabase.ReleaseReceiptVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The Class ReceiptPDFGenerator.
 */
@Service
public class ReceiptPDFGenerator {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ReceiptPDFGenerator.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Generate PDF.
   *
   * @param receipt the receipt
   * @param out the out
   */
  public void generatePDF(ReleaseReceiptVO receipt, OutputStream out) {
    if (out != null) {
      try {
        // Creating PDF document object
        PDDocument document = new PDDocument();

        // Create and add an A3 landscape page
        float POINTS_PER_INCH = 72;
        float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;
        PDPage page = new PDPage(new PDRectangle(210 * POINTS_PER_MM, 148 * POINTS_PER_MM));
        document.addPage(page);

        printContentPDF(receipt, document, page);

        // Save and close the PDF
        document.save(out);
        LOG.info("Receipt generated: representative={}, dataflowId={}, dataflowName={}",
            receipt.getProviderAssignation(), receipt.getIdDataflow(), receipt.getDataflowName());
        document.close();
      } catch (IOException e) {
        LOG_ERROR.error("Unexpected exception: ", e);
      }
    }
  }

  /**
   * Prints the content PDF.
   *
   * @param receipt the receipt
   * @param document the document
   * @param page the page
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void printContentPDF(ReleaseReceiptVO receipt, PDDocument document, PDPage page)
      throws IOException {

    String text;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    PDPageContentStream contentStream = new PDPageContentStream(document, page);
    PDType1Font font = PDType1Font.HELVETICA;
    PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;

    // Print background
    PDImageXObject pdImage = PDImageXObject.createFromFile("receipt_background.png", document);
    contentStream.drawImage(pdImage, 0, 0);

    // Print receipt information
    printLinePDF(contentStream, "CONFIRMATION RECEIPT", font, 25, 25, 345);
    printLinePDF(contentStream, "Dataset", fontBold, 12, 25, 295);
    printLinePDF(contentStream, "Date", fontBold, 12, 458, 295);
    printLinePDF(contentStream, "Date: " + dateFormatter.format(new Date()), font, 12, 25, 386);
    text = "Representative: " + receipt.getProviderAssignation();
    printLinePDF(contentStream, text, font, 12, 570 - font.getStringWidth(text) / 1000 * 12, 386);
    printLinePDF(contentStream, receipt.getDataflowName(), font, 12, 25, 325);

    // Print schemas information
    float y = 295 - 20;
    contentStream.setNonStrokingColor(Color.DARK_GRAY);
    for (ReportingDatasetVO dataset : receipt.getDatasets()) {
      printLinePDF(contentStream, dataset.getNameDatasetSchema(), font, 12, 25, y);
      text = dateFormatter.format(dataset.getDateReleased());
      printLinePDF(contentStream, text, font, 12, 570 - font.getStringWidth(text) / 1000 * 12, y);
      contentStream.addRect(25, y - 5.5f, 545, 0.5f);
      contentStream.fill();
      y -= 20;
    }

    contentStream.close();
  }

  /**
   * Prints the line PDF.
   *
   * @param contentStream the content stream
   * @param text the text
   * @param font the font
   * @param fontSize the font size
   * @param x the x
   * @param y the y
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void printLinePDF(PDPageContentStream contentStream, String text, PDType1Font font,
      float fontSize, float x, float y) throws IOException {
    contentStream.beginText();
    contentStream.newLineAtOffset(x, y);
    contentStream.setFont(font, fontSize);
    contentStream.showText(text);
    contentStream.endText();
  }
}
