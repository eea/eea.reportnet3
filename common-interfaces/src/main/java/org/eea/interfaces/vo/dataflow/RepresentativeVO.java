package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RepresentativeVO.
 */
@Getter
@Setter
@ToString
public class RepresentativeVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -6924509754041958192L;

  /** The id. */
  private Long id;

  /** The data provider id. */
  private Long dataProviderId;

  /** The provider account. */
  private List<String> providerAccounts;

  /** The data provider group id. */
  private Long dataProviderGroupId;

  /** The receipt downloaded. */
  private Boolean receiptDownloaded;

  /** The receipt outdated. */
  private Boolean receiptOutdated;

  /** The has datasets. */
  private Boolean hasDatasets;

  /** The restrict from public. */
  private boolean restrictFromPublic;

}
