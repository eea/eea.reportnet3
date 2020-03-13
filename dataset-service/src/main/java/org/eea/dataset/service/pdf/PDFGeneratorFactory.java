package org.eea.dataset.service.pdf;

public class PDFGeneratorFactory {

  private PDFGeneratorFactory() {}

  /**
   * Gets the generator.
   *
   * @param pdfTemplate the pdf template
   * @return the generator
   */
  public static PDFGenerator getGenerator(PDFTemplate template) {
    switch (template) {
      case RECEIPT:
        return new ReceiptPDFGenerator();
      default:
        return null;
    }
  }
}
