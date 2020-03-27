package org.eea.rod.persistence.domain;

import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString
public class Obligation {

  private String oblTitle;
  private String description;

  private Integer obligationId;
  private Integer eeaPrimary;
  private String overlapUrl;
  private Integer eeaCore;
  private Integer flagged;
  private String coordinator;
  private String coordinatorUrl;
  private String coordinatorRole;
  private String coordinatorRoleSuf;

  private String nationalContact;
  private String nationalContactUrl;
  private String responsibleRole;
  private String responsibleRoleSuf;

  private String terminate;

  private String reportFreqMonths;
  private Date nextDeadline;
  private Date nextDeadline2;
  private String nextReporting;
  private Date firstReporting;
  private String continousReporting;
  private String dateComments;
  private String formatName;
  private String reportFormatUrl;
  private String reportingFormat;
  private String locationPtr;
  private String locationInfo;
  private String dataUsedFor;
  private String dataUsedForUrl;
  private Date validSince;
  private Date validTo;
  private String authority;
  private String comment;
  private String parameters;
  private String hasDelivery;

  private String reportFreqDetail;
  private String lastUpdate;
  private String reportFreq;
  private Date lastHarvested;

  //Fields from t_role table
  private String coordRoleId;
  private String coordRoleUrl;
  private String coordRoleName;

  private String respRoleId;
  private String respRoleName;

  //Fields from T_CLIENT_LNK table
  private String clientLnkFKClientId;
  private String clientLnkFKObjectId;
  private String clientLnkStatus;
  private String clientLnkType;

  //Fields from T_CLIENT table
  private String clientId;
  private String clientName;

  //Fields from T_SOURCE table
  private String sourceId;
  private String sourceTitle;
  private String sourceAlias;

  private List<String> selectedClients;
  private List<String> selectedFormalCountries;
  private List<String> selectedVoluntaryCountries;
  private List<String> selectedIssues;

  //Fields from T_ISSUE table to search
  private String issueId;

  //Fields from T_SPATIAL table to search
  private String spatialId;

  //Fields from T_RASPATIAL_LNK table
  String voluntary;

  //field to deadline search
  private String deadlineId;

  private String delObligations;

  //Obligations relations table T_OBLIGATION_RELATION
  private Integer relObligationId;
  private String oblRelationId;

  private String oblRelationTitle;

  //advanded Search
  private String nextDeadlineFrom;
  private String nextDeadlineTo;
  private String deliveryCountryId;
  private String deliveryCountryName;
  private String anmode;
}
