package org.eea.interfaces.vo.rod;


import feign.Client;
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

  private Integer obligationId;
  private String oblTitle;
  private String description;
  private Date validSince;
  private Date validTo;
  private String comment;
  private Date nextDeadline;
  private List<LegalInstrumentVO> legalInstruments;
  private ClientVO client;
  private List<CountryVO> countries;
  private List<IssueVO> issues;
}
