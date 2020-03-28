package org.eea.rod.persistence.domain;

import com.google.common.base.Objects;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Obligation that = (Obligation) o;
    return Objects.equal(oblTitle, that.oblTitle) &&
        Objects.equal(description, that.description) &&
        Objects.equal(obligationId, that.obligationId) &&
        Objects.equal(eeaPrimary, that.eeaPrimary) &&
        Objects.equal(overlapUrl, that.overlapUrl) &&
        Objects.equal(eeaCore, that.eeaCore) &&
        Objects.equal(flagged, that.flagged) &&
        Objects.equal(coordinator, that.coordinator) &&
        Objects.equal(coordinatorUrl, that.coordinatorUrl) &&
        Objects.equal(coordinatorRole, that.coordinatorRole) &&
        Objects.equal(coordinatorRoleSuf, that.coordinatorRoleSuf) &&
        Objects.equal(nationalContact, that.nationalContact) &&
        Objects.equal(nationalContactUrl, that.nationalContactUrl) &&
        Objects.equal(responsibleRole, that.responsibleRole) &&
        Objects.equal(responsibleRoleSuf, that.responsibleRoleSuf) &&
        Objects.equal(terminate, that.terminate) &&
        Objects.equal(reportFreqMonths, that.reportFreqMonths) &&
        Objects.equal(nextDeadline, that.nextDeadline) &&
        Objects.equal(nextDeadline2, that.nextDeadline2) &&
        Objects.equal(nextReporting, that.nextReporting) &&
        Objects.equal(firstReporting, that.firstReporting) &&
        Objects.equal(continousReporting, that.continousReporting) &&
        Objects.equal(dateComments, that.dateComments) &&
        Objects.equal(formatName, that.formatName) &&
        Objects.equal(reportFormatUrl, that.reportFormatUrl) &&
        Objects.equal(reportingFormat, that.reportingFormat) &&
        Objects.equal(locationPtr, that.locationPtr) &&
        Objects.equal(locationInfo, that.locationInfo) &&
        Objects.equal(dataUsedFor, that.dataUsedFor) &&
        Objects.equal(dataUsedForUrl, that.dataUsedForUrl) &&
        Objects.equal(validSince, that.validSince) &&
        Objects.equal(validTo, that.validTo) &&
        Objects.equal(authority, that.authority) &&
        Objects.equal(comment, that.comment) &&
        Objects.equal(parameters, that.parameters) &&
        Objects.equal(hasDelivery, that.hasDelivery) &&
        Objects.equal(reportFreqDetail, that.reportFreqDetail) &&
        Objects.equal(lastUpdate, that.lastUpdate) &&
        Objects.equal(reportFreq, that.reportFreq) &&
        Objects.equal(lastHarvested, that.lastHarvested) &&
        Objects.equal(coordRoleId, that.coordRoleId) &&
        Objects.equal(coordRoleUrl, that.coordRoleUrl) &&
        Objects.equal(coordRoleName, that.coordRoleName) &&
        Objects.equal(respRoleId, that.respRoleId) &&
        Objects.equal(respRoleName, that.respRoleName) &&
        Objects.equal(clientLnkFKClientId, that.clientLnkFKClientId) &&
        Objects.equal(clientLnkFKObjectId, that.clientLnkFKObjectId) &&
        Objects.equal(clientLnkStatus, that.clientLnkStatus) &&
        Objects.equal(clientLnkType, that.clientLnkType) &&
        Objects.equal(clientId, that.clientId) &&
        Objects.equal(clientName, that.clientName) &&
        Objects.equal(sourceId, that.sourceId) &&
        Objects.equal(sourceTitle, that.sourceTitle) &&
        Objects.equal(sourceAlias, that.sourceAlias) &&
        Objects.equal(selectedClients, that.selectedClients) &&
        Objects.equal(selectedFormalCountries, that.selectedFormalCountries) &&
        Objects
            .equal(selectedVoluntaryCountries, that.selectedVoluntaryCountries) &&
        Objects.equal(selectedIssues, that.selectedIssues) &&
        Objects.equal(issueId, that.issueId) &&
        Objects.equal(spatialId, that.spatialId) &&
        Objects.equal(voluntary, that.voluntary) &&
        Objects.equal(deadlineId, that.deadlineId) &&
        Objects.equal(delObligations, that.delObligations) &&
        Objects.equal(relObligationId, that.relObligationId) &&
        Objects.equal(oblRelationId, that.oblRelationId) &&
        Objects.equal(oblRelationTitle, that.oblRelationTitle) &&
        Objects.equal(nextDeadlineFrom, that.nextDeadlineFrom) &&
        Objects.equal(nextDeadlineTo, that.nextDeadlineTo) &&
        Objects.equal(deliveryCountryId, that.deliveryCountryId) &&
        Objects.equal(deliveryCountryName, that.deliveryCountryName) &&
        Objects.equal(anmode, that.anmode);
  }

  @Override
  public int hashCode() {
    return Objects
        .hashCode(oblTitle, description, obligationId, eeaPrimary, overlapUrl, eeaCore, flagged,
            coordinator, coordinatorUrl, coordinatorRole, coordinatorRoleSuf, nationalContact,
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
