package org.eea.rod.persistence.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class Obligation.
 */
@Getter
@Setter
@ToString
public class Obligation implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 4187963258165306794L;

  /** The obl title. */
  private String oblTitle;

  /** The description. */
  private String description;

  /** The obligation id. */
  private Integer obligationId;

  /** The eea primary. */
  private Integer eeaPrimary;

  /** The overlap url. */
  private String overlapUrl;

  /** The eea core. */
  private Integer eeaCore;

  /** The flagged. */
  private Integer flagged;

  /** The coordinator. */
  private String coordinator;

  /** The coordinator url. */
  private String coordinatorUrl;

  /** The coordinator role. */
  private String coordinatorRole;

  /** The coordinator role suf. */
  private String coordinatorRoleSuf;

  /** The national contact. */
  private String nationalContact;

  /** The national contact url. */
  private String nationalContactUrl;

  /** The responsible role. */
  private String responsibleRole;

  /** The responsible role suf. */
  private String responsibleRoleSuf;

  /** The terminate. */
  private String terminate;

  /** The report freq months. */
  private String reportFreqMonths;

  /** The next deadline. */
  private Date nextDeadline;

  /** The next deadline 2. */
  private Date nextDeadline2;

  /** The next reporting. */
  private String nextReporting;

  /** The first reporting. */
  private Date firstReporting;

  /** The continous reporting. */
  private String continousReporting;

  /** The date comments. */
  private String dateComments;

  /** The format name. */
  private String formatName;

  /** The report format url. */
  private String reportFormatUrl;

  /** The reporting format. */
  private String reportingFormat;

  /** The location ptr. */
  private String locationPtr;

  /** The location info. */
  private String locationInfo;

  /** The data used for. */
  private String dataUsedFor;

  /** The data used for url. */
  private String dataUsedForUrl;

  /** The valid since. */
  private Date validSince;

  /** The valid to. */
  private Date validTo;

  /** The authority. */
  private String authority;

  /** The comment. */
  private String comment;

  /** The parameters. */
  private String parameters;

  /** The has delivery. */
  private String hasDelivery;

  /** The report freq detail. */
  private String reportFreqDetail;

  /** The last update. */
  private String lastUpdate;

  /** The report freq. */
  private String reportFreq;

  /** The last harvested. */
  private Date lastHarvested;

  /** The coord role id. */
  // Fields from t_role table
  private String coordRoleId;

  /** The coord role url. */
  private String coordRoleUrl;

  /** The coord role name. */
  private String coordRoleName;

  /** The resp role id. */
  private String respRoleId;

  /** The resp role name. */
  private String respRoleName;

  /** The client lnk FK client id. */
  // Fields from T_CLIENT_LNK table
  private String clientLnkFKClientId;

  /** The client lnk FK object id. */
  private String clientLnkFKObjectId;

  /** The client lnk status. */
  private String clientLnkStatus;

  /** The client lnk type. */
  private String clientLnkType;

  /** The client id. */
  // Fields from T_CLIENT table
  private String clientId;

  /** The client name. */
  private String clientName;

  /** The source id. */
  // Fields from T_SOURCE table
  private String sourceId;

  /** The source title. */
  private String sourceTitle;

  /** The source alias. */
  private String sourceAlias;

  /** The selected clients. */
  private List<String> selectedClients;

  /** The selected formal countries. */
  private List<String> selectedFormalCountries;

  /** The selected voluntary countries. */
  private List<String> selectedVoluntaryCountries;

  /** The selected issues. */
  private List<String> selectedIssues;

  /** The issue id. */
  // Fields from T_ISSUE table to search
  private String issueId;

  /** The spatial id. */
  // Fields from T_SPATIAL table to search
  private String spatialId;

  /** The voluntary. */
  // Fields from T_RASPATIAL_LNK table
  String voluntary;

  /** The deadline id. */
  // field to deadline search
  private String deadlineId;

  /** The del obligations. */
  private String delObligations;

  /** The rel obligation id. */
  // Obligations relations table T_OBLIGATION_RELATION
  private Integer relObligationId;

  /** The obl relation id. */
  private String oblRelationId;

  /** The obl relation title. */
  private String oblRelationTitle;

  /** The next deadline from. */
  // advanded Search
  private String nextDeadlineFrom;

  /** The next deadline to. */
  private String nextDeadlineTo;

  /** The delivery country id. */
  private String deliveryCountryId;

  /** The delivery country name. */
  private String deliveryCountryName;

  /** The anmode. */
  private String anmode;

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Obligation that = (Obligation) o;
    return Objects.equal(oblTitle, that.oblTitle) && Objects.equal(description, that.description)
        && Objects.equal(obligationId, that.obligationId)
        && Objects.equal(eeaPrimary, that.eeaPrimary) && Objects.equal(overlapUrl, that.overlapUrl)
        && Objects.equal(eeaCore, that.eeaCore) && Objects.equal(flagged, that.flagged)
        && Objects.equal(coordinator, that.coordinator)
        && Objects.equal(coordinatorUrl, that.coordinatorUrl)
        && Objects.equal(coordinatorRole, that.coordinatorRole)
        && Objects.equal(coordinatorRoleSuf, that.coordinatorRoleSuf)
        && Objects.equal(nationalContact, that.nationalContact)
        && Objects.equal(nationalContactUrl, that.nationalContactUrl)
        && Objects.equal(responsibleRole, that.responsibleRole)
        && Objects.equal(responsibleRoleSuf, that.responsibleRoleSuf)
        && Objects.equal(terminate, that.terminate)
        && Objects.equal(reportFreqMonths, that.reportFreqMonths)
        && Objects.equal(nextDeadline, that.nextDeadline)
        && Objects.equal(nextDeadline2, that.nextDeadline2)
        && Objects.equal(nextReporting, that.nextReporting)
        && Objects.equal(firstReporting, that.firstReporting)
        && Objects.equal(continousReporting, that.continousReporting)
        && Objects.equal(dateComments, that.dateComments)
        && Objects.equal(formatName, that.formatName)
        && Objects.equal(reportFormatUrl, that.reportFormatUrl)
        && Objects.equal(reportingFormat, that.reportingFormat)
        && Objects.equal(locationPtr, that.locationPtr)
        && Objects.equal(locationInfo, that.locationInfo)
        && Objects.equal(dataUsedFor, that.dataUsedFor)
        && Objects.equal(dataUsedForUrl, that.dataUsedForUrl)
        && Objects.equal(validSince, that.validSince) && Objects.equal(validTo, that.validTo)
        && Objects.equal(authority, that.authority) && Objects.equal(comment, that.comment)
        && Objects.equal(parameters, that.parameters)
        && Objects.equal(hasDelivery, that.hasDelivery)
        && Objects.equal(reportFreqDetail, that.reportFreqDetail)
        && Objects.equal(lastUpdate, that.lastUpdate) && Objects.equal(reportFreq, that.reportFreq)
        && Objects.equal(lastHarvested, that.lastHarvested)
        && Objects.equal(coordRoleId, that.coordRoleId)
        && Objects.equal(coordRoleUrl, that.coordRoleUrl)
        && Objects.equal(coordRoleName, that.coordRoleName)
        && Objects.equal(respRoleId, that.respRoleId)
        && Objects.equal(respRoleName, that.respRoleName)
        && Objects.equal(clientLnkFKClientId, that.clientLnkFKClientId)
        && Objects.equal(clientLnkFKObjectId, that.clientLnkFKObjectId)
        && Objects.equal(clientLnkStatus, that.clientLnkStatus)
        && Objects.equal(clientLnkType, that.clientLnkType)
        && Objects.equal(clientId, that.clientId) && Objects.equal(clientName, that.clientName)
        && Objects.equal(sourceId, that.sourceId) && Objects.equal(sourceTitle, that.sourceTitle)
        && Objects.equal(sourceAlias, that.sourceAlias)
        && Objects.equal(selectedClients, that.selectedClients)
        && Objects.equal(selectedFormalCountries, that.selectedFormalCountries)
        && Objects.equal(selectedVoluntaryCountries, that.selectedVoluntaryCountries)
        && Objects.equal(selectedIssues, that.selectedIssues)
        && Objects.equal(issueId, that.issueId) && Objects.equal(spatialId, that.spatialId)
        && Objects.equal(voluntary, that.voluntary) && Objects.equal(deadlineId, that.deadlineId)
        && Objects.equal(delObligations, that.delObligations)
        && Objects.equal(relObligationId, that.relObligationId)
        && Objects.equal(oblRelationId, that.oblRelationId)
        && Objects.equal(oblRelationTitle, that.oblRelationTitle)
        && Objects.equal(nextDeadlineFrom, that.nextDeadlineFrom)
        && Objects.equal(nextDeadlineTo, that.nextDeadlineTo)
        && Objects.equal(deliveryCountryId, that.deliveryCountryId)
        && Objects.equal(deliveryCountryName, that.deliveryCountryName)
        && Objects.equal(anmode, that.anmode);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(oblTitle, description, obligationId, eeaPrimary, overlapUrl, eeaCore,
        flagged, coordinator, coordinatorUrl, coordinatorRole, coordinatorRoleSuf, nationalContact,
        nationalContactUrl, responsibleRole, responsibleRoleSuf, terminate, reportFreqMonths,
        nextDeadline, nextDeadline2, nextReporting, firstReporting, continousReporting,
        dateComments, formatName, reportFormatUrl, reportingFormat, locationPtr, locationInfo,
        dataUsedFor, dataUsedForUrl, validSince, validTo, authority, comment, parameters,
        hasDelivery, reportFreqDetail, lastUpdate, reportFreq, lastHarvested, coordRoleId,
        coordRoleUrl, coordRoleName, respRoleId, respRoleName, clientLnkFKClientId,
        clientLnkFKObjectId, clientLnkStatus, clientLnkType, clientId, clientName, sourceId,
        sourceTitle, sourceAlias, selectedClients, selectedFormalCountries,
        selectedVoluntaryCountries, selectedIssues, issueId, spatialId, voluntary, deadlineId,
        delObligations, relObligationId, oblRelationId, oblRelationTitle, nextDeadlineFrom,
        nextDeadlineTo, deliveryCountryId, deliveryCountryName, anmode);
  }
}
