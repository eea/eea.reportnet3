package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
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

  /** The account email. */
  private String account;

  /** The data provider group id. */
  private Long dataProviderGroupId;

  /** The receipt downloaded. */
  private Boolean receiptDownloaded;

  /** The receipt outdated. */
  private Boolean receiptOutdated;

  /** The has datasets. */
  private Boolean hasDatasets;

  /** The role. */
  private SecurityRoleEnum role;

  /** The write permission. */
  private Boolean permission;
}
