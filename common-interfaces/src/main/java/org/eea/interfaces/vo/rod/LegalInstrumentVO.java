package org.eea.interfaces.vo.rod;


import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Legal instrument vo.
 */
@Getter
@Setter
@ToString
public class LegalInstrumentVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 3470713570404074419L;

  /** The source id. */
  private String sourceId;

  /** The source title. */
  private String sourceTitle;

  /** The source alias. */
  private String sourceAlias;
}
