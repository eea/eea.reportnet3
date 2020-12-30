package org.eea.interfaces.vo.pams;

import java.util.List;
import lombok.Data;


/**
 * Instantiates a new single paMs VO.
 */
@Data
public class SinglePaMVO {

  /** The id. */
  private String id;

  /** The paM name. */
  private String paMName;

  /** The is policy measure envisaged. */
  private String isPolicyMeasureEnvisaged;

  /** The sectors. */
  private List<SectorVO> sectors;

  /** The status implementation. */
  private String statusImplementation;

  /** The implementation period start. */
  private String implementationPeriodStart;

  /** The implementation period finish. */
  private String implementationPeriodFinish;

  /** The implementation period comment. */
  private String implementationPeriodComment;

  /** The projections scenario. */
  private String projectionsScenario;

  /** The union policy list. */
  private List<String> unionPolicyList;

  /** The entities. */
  private List<EntityPaMVO> entities;

  /** The Policy impacting. */
  private String policyImpacting;

  /** The type policy instrument. */
  private List<String> typePolicyInstrument;

  /** The ghg affected. */
  private List<String> ghgAffected;

  /** The other union policy. */
  private String otherUnionPolicy;

  /** The other policy instrument. */
  private String otherPolicyInstrument;


}
