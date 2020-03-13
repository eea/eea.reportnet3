package org.eea.dataset.service.pdf;

import java.io.OutputStream;

public interface PDFGenerator {

  void generatePDF(OutputStream out);

  void configure(PDFData data);
}
