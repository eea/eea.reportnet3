package org.eea.interfaces.vo.rod;


import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Obligation vo.
 */
@Getter
@Setter
@ToString
public class ObligationVO {

  /** The obligation id. */
  private Integer obligationId;

  /** The obl title. */
  private String oblTitle;

  /** The description. */
  private String description;

  /** The valid since. */
  private Date validSince;

  /** The valid to. */
  private Date validTo;

  /** The comment. */
  private String comment;

  /** The next deadline. */
  private Date nextDeadline;

  /** The legal instrument. */
  private LegalInstrumentVO legalInstrument;

  /** The client. */
  private ClientVO client;

  /** The countries. */
  private List<CountryVO> countries;

  /** The issues. */
  private List<IssueVO> issues;

  /** The report freq. */
  private String reportFreq;

  /** The report freq detail. */
  private String reportFreqDetail;

}
