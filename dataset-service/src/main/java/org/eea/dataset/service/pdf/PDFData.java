package org.eea.dataset.service.pdf;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PDFData {
  private Long dataflowId;
  private Long dataProviderId;
}
