package org.eea.dataset.service.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.util.IOUtils;
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

  /** The Constant BACKGROUND. */
  private static final String BACKGROUND = "pdf/receipt_background.png";

  /** The Constant HELVETICA: {@value}. */
  private static final String HELVETICA = "pdf/Helvetica.ttf";

  /** The Constant HELVETICA_BOLD: {@value}. */
  private static final String HELVETICA_BOLD = "pdf/Helvetica-Bold.ttf";

  /** The Constant POINTS_PER_INCH. */
  private static final float POINTS_PER_INCH = 300;

  /** The Constant POINTS_PER_MM. */
  private static final float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;

  /** A rectangle the size of A5 Paper (landscape). */
  public static final PDRectangle A5_LS = new PDRectangle(210 * POINTS_PER_MM, 148 * POINTS_PER_MM);

  /** A rectangle the size of A5 Paper. */
  public static final PDRectangle A4 = new PDRectangle(210 * POINTS_PER_MM, 297 * POINTS_PER_MM);

  /**
   * Generate PDF.
   *
   * @param receipt the receipt
   * @param out the out
   */
  public void generatePDF(ReleaseReceiptVO receipt, OutputStream out) {
    if (out != null) {
      try (PDDocument document = new PDDocument();) {
        // Create and add an A4 page
        PDPage page = new PDPage(A4);
        document.addPage(page);

        printContentPDF(receipt, document, page);

        // Save and close the PDF
        document.save(out);
        LOG.info("Receipt generated: representative={}, dataflowId={}, dataflowName={}",
            receipt.getProviderAssignation(), receipt.getIdDataflow(), receipt.getDataflowName());
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

    float x;
    float y;
    float spaceBetweenLines;
    float fontSize;
    String text;
    ZoneId timeZone = ZoneId.of("CET");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    PDPageContentStream contentStream = new PDPageContentStream(document, page);
    PDType0Font font;
    PDType0Font fontBold;
    PDImageXObject pdImage;

    try (InputStream helvetica = getClass().getClassLoader().getResourceAsStream(HELVETICA);
        InputStream helveticaBold = getClass().getClassLoader().getResourceAsStream(HELVETICA_BOLD);
        InputStream bg = getClass().getClassLoader().getResourceAsStream(BACKGROUND)) {
      font = PDType0Font.load(document, helvetica);
      fontBold = PDType0Font.load(document, helveticaBold);
      pdImage = PDImageXObject.createFromByteArray(document, IOUtils.toByteArray(bg), BACKGROUND);
    }

    // Print background
    contentStream.drawImage(pdImage, 0, 0);

    // Print receipt left headers
    x = 133f;
    y = 3334f;
    fontSize = 40f;
    spaceBetweenLines = 20f;
    printLinePDF(contentStream, "European Environment Agency", fontBold, fontSize, x, y);
    y -= spaceBetweenLines + fontSize;
    printLinePDF(contentStream, "Kongens Nytorv 6", fontBold, fontSize, x, y);
    y -= spaceBetweenLines + fontSize;
    printLinePDF(contentStream, "Dk 1050 Copenhagen K", fontBold, fontSize, x, y);

    // Print receipt right headers
    text = ZonedDateTime.now(timeZone).format(dateFormatter);
    x = 2346 - font.getStringWidth(text) / 1000 * fontSize;
    y = 3334f;
    fontSize = 40f;
    printLinePDF(contentStream, text, font, fontSize, x, y);
    text = "Receipt date: ";
    x -= fontBold.getStringWidth(text) / 1000 * fontSize;
    printLinePDF(contentStream, text, fontBold, fontSize, x, y);
    text = receipt.getProviderAssignation();
    x = 2346 - font.getStringWidth(text) / 1000 * fontSize;
    y -= spaceBetweenLines + fontSize;
    printLinePDF(contentStream, text, font, fontSize, x, y);
    text = "Representative: ";
    x -= fontBold.getStringWidth(text) / 1000 * fontSize;
    printLinePDF(contentStream, text, fontBold, fontSize, x, y);

    // Print concern information
    fontSize = 90f;
    x = 133f;
    y = 2900f;
    text = "To Whom It May Concern";
    printLinePDF(contentStream, text, fontBold, fontSize, x, y);
    y -= spaceBetweenLines * 2 + fontSize;
    fontSize = 58f;
    text = "This is a confirmation of receipt for national data submission under";
    printLinePDF(contentStream, text, font, fontSize, x, y);
    y -= spaceBetweenLines + fontSize;
    text = "the reporting obligation";
    printLinePDF(contentStream, text, font, fontSize, x, y);

    // Print dataflow name
    fontSize = 90f;
    y -= spaceBetweenLines * 2 + fontSize + 20f;
    for (String line : splitInDifferentLines(receipt.getDataflowName(), 2213f, fontBold,
        fontSize)) {
      printLinePDF(contentStream, line, fontBold, fontSize, x, y);
      y -= spaceBetweenLines + fontSize;
    }
    fontSize = 58f;

    // Print obligation information
    y -= 18f;
    text = "Obligation: ";
    printLinePDF(contentStream, text, fontBold, fontSize, x, y);
    x += fontBold.getStringWidth(text) / 1000 * fontSize;
    for (String line : splitInDifferentLines(receipt.getObligationTitle(), 2213 - x, font,
        fontSize)) {
      printLinePDF(contentStream, line, font, fontSize, x, y);
      y -= spaceBetweenLines + fontSize;
    }
    text = "https://rod.eionet.europa.eu/obligations/" + receipt.getObligationId();
    printLinePDF(contentStream, text, font, fontSize, x, y);

    // Print dataset list
    y -= spaceBetweenLines * 2 + fontSize;
    printLinePDF(contentStream, "Datasets", fontBold, fontSize, 133f, y);
    printLinePDF(contentStream, "Release date", fontBold, fontSize, 1672f, y);
    spaceBetweenLines = 40f;
    for (ReportingDatasetVO dataset : receipt.getDatasets()) {
      y -= spaceBetweenLines + fontSize;
      printLinePDF(contentStream, dataset.getNameDatasetSchema(), font, fontSize, 133f, y);
      text = dateTimeFormatter
          .format(ZonedDateTime.ofInstant(dataset.getDateReleased().toInstant(), timeZone));
      printLinePDF(contentStream, text, font, fontSize, 1672f, y);
      contentStream.addRect(133f, y - spaceBetweenLines / 2, 2213, 1f);
      contentStream.fill();
    }

    // Print user
    fontSize = 40f;
    x = 133f;
    y -= spaceBetweenLines * 4 - fontSize;
    text = "Submitted by user: " + receipt.getEmail();
    printLinePDF(contentStream, text, font, fontSize, x, y);
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
  private void printLinePDF(PDPageContentStream contentStream, String text, PDType0Font font,
      float fontSize, float x, float y) throws IOException {
    contentStream.beginText();
    contentStream.newLineAtOffset(x, y);
    contentStream.setFont(font, fontSize);
    contentStream.showText(text);
    contentStream.endText();
  }

  /**
   * Split in different lines.
   *
   * @param text the text
   * @param maxLineWidth the max line width
   * @param font the font
   * @param fontSize the font size
   * @return the string[]
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private String[] splitInDifferentLines(String text, float maxLineWidth, PDType0Font font,
      float fontSize) throws IOException {

    List<String> lines = new ArrayList<>();
    String[] textArray = text.split(" ");
    int index = 0;

    while (index < textArray.length) {

      StringBuilder line = new StringBuilder();
      boolean isLineFull = false;

      while (!isLineFull && index < textArray.length) {

        String word = textArray[index];
        float wordWidth = font.getStringWidth(word) / 1000 * fontSize;
        float newLineWidth = font.getStringWidth(line + " " + word) / 1000 * fontSize;

        if (wordWidth >= maxLineWidth) {
          int cutIndex = cutWordToFitInLine(word, line.toString(), maxLineWidth, font, fontSize);
          textArray[index] = word.substring(cutIndex);
          line.append(" ").append(word.substring(0, cutIndex));
          isLineFull = true;
        } else if (newLineWidth <= maxLineWidth) {
          line.append(" ").append(word);
          index++;
        } else {
          isLineFull = true;
        }
      }

      lines.add(line.toString().trim());
    }

    return lines.toArray(new String[lines.size()]);
  }

  /**
   * Cut word to fit in line.
   *
   * @param word the word
   * @param line the line
   * @param maxLineWidth the max line width
   * @param font the font
   * @param fontSize the font size
   * @return the int
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private int cutWordToFitInLine(String word, String line, float maxLineWidth, PDType0Font font,
      float fontSize) throws IOException {

    float wordWidth = font.getStringWidth(word) / 1000 * fontSize;
    float emptyWidth = maxLineWidth - font.getStringWidth(line + " ") / 1000 * fontSize;
    int cutIndex = (int) (word.length() * emptyWidth / wordWidth);
    float currentWidth =
        font.getStringWidth(line + " " + word.substring(0, cutIndex)) / 1000 * fontSize;

    boolean betterWidthFound = false;
    while (!betterWidthFound) {
      if (currentWidth <= maxLineWidth) {
        currentWidth =
            font.getStringWidth(line + " " + word.substring(0, cutIndex + 1)) / 1000 * fontSize;
        if (currentWidth <= maxLineWidth) {
          cutIndex++;
        } else {
          betterWidthFound = true;
        }
      } else {
        currentWidth =
            font.getStringWidth(line + " " + word.substring(0, cutIndex - 1)) / 1000 * fontSize;
        if (currentWidth >= maxLineWidth) {
          cutIndex--;
        } else {
          betterWidthFound = true;
        }
      }
    }

    return cutIndex;
  }
}
