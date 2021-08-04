import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { obligationRepository } from 'repositories/ObligationRepository';

import { Country } from 'entities/Country';
import { Issue } from 'entities/Issue';
import { LegalInstrument } from 'entities/LegalInstrument';
import { Obligation } from 'entities/Obligation';
import { Organization } from 'entities/Organization';

const getCountries = async () => {
  const countriesDTO = await obligationRepository.getCountries();
  return parseCountryList(countriesDTO.data);
};

const getIssues = async () => {
  const issuesDTO = await obligationRepository.getIssues();
  return parseIssueList(issuesDTO.data);
};

const getOrganizations = async () => {
  const clientsDTO = await obligationRepository.getOrganizations();
  return parseOrganizationList(clientsDTO.data);
};

const obligationById = async obligationId => {
  const obligationByIdDTO = await obligationRepository.obligationById(obligationId);
  return parseObligation(obligationByIdDTO.data);
};

const opened = async filterData => {
  if (!isEmpty(filterData)) {
    const countryId = !isNil(filterData.countries) ? filterData.countries.value : '';
    const dateFrom = filterData.expirationDate[0] ? filterData.expirationDate[0].getTime() : '';
    const dateTo = filterData.expirationDate[1] ? filterData.expirationDate[1].getTime() : '';
    const issueId = !isNil(filterData.issues) ? filterData.issues.value : '';
    const organizationId = !isNil(filterData.organizations) ? filterData.organizations.value : '';
    const openedObligationsDTO = await obligationRepository.openedObligations(
      countryId,
      dateFrom,
      dateTo,
      issueId,
      organizationId
    );
    return parseObligationList(openedObligationsDTO.data);
  } else {
    const openedObligationsDTO = await obligationRepository.openedObligations();
    return parseObligationList(openedObligationsDTO.data);
  }
};

const parseCountry = countryDTO =>
  new Country({
    countryCode: countryDTO.twoLetter,
    countryMember: countryDTO.memberCommunity,
    id: countryDTO.spatialId,
    name: countryDTO.name,
    type: countryDTO.type
  });

const parseCountryList = countriesDTO => {
  if (!isNil(countriesDTO)) {
    const countries = [];
    countriesDTO.forEach(countryDTO => countries.push(parseCountry(countryDTO)));
    return countries;
  }
  return;
};

const parseIssue = issueDTO => new Issue({ id: issueDTO.issueId, name: issueDTO.issueName });

const parseIssueList = issuesDTO => {
  if (!isNil(issuesDTO)) {
    const issues = [];
    issuesDTO.forEach(issueDTO => issues.push(parseIssue(issueDTO)));
    return issues;
  }
};

const parseLegalInstrument = legalInstrumentDTO => {
  if (!isNil(legalInstrumentDTO)) {
    return new LegalInstrument({
      alias: legalInstrumentDTO.sourceAlias,
      id: legalInstrumentDTO.sourceId,
      title: legalInstrumentDTO.sourceTitle
    });
  }
  return;
};

const parseObligation = obligationDTO =>
  new Obligation({
    comment: obligationDTO.comment,
    countries: obligationDTO.countries,
    description: obligationDTO.description,
    expirationDate: obligationDTO.nextDeadline > 0 ? dayjs(obligationDTO.nextDeadline).format('YYYY-MM-DD') : null,
    issues: obligationDTO.issues,
    legalInstruments: parseLegalInstrument(obligationDTO.legalInstrument),
    obligationId: obligationDTO.obligationId,
    reportingFrequency: obligationDTO.reportFreq,
    reportingFrequencyDetail: obligationDTO.reportFreqDetail,
    organization: parseOrganization(obligationDTO.client),
    title: obligationDTO.oblTitle,
    validSince: obligationDTO.validSince,
    validTo: obligationDTO.validTo
  });

const parseObligationList = obligationsDTO => {
  if (!isNil(obligationsDTO)) {
    const obligations = [];
    obligationsDTO.forEach(obligationDTO => obligations.push(parseObligation(obligationDTO)));
    return obligations;
  }
  return;
};

const parseOrganization = organizationDTO => {
  if (!isNil(organizationDTO)) {
    return new Organization({
      acronym: organizationDTO.acronym,
      address: organizationDTO.address,
      city: organizationDTO.city,
      country: organizationDTO.country,
      description: organizationDTO.description,
      email: organizationDTO.email,
      id: organizationDTO.clientId,
      name: organizationDTO.name,
      postalCode: organizationDTO.postalCode,
      shortName: organizationDTO.shortName,
      url: organizationDTO.url
    });
  }
  return;
};

const parseOrganizationList = organizationsDTO => {
  if (!isNil(organizationsDTO)) {
    const organizations = [];
    organizationsDTO.forEach(organizationDTO => organizations.push(parseOrganization(organizationDTO)));
    return organizations;
  }
  return;
};

export const ObligationService = {
  getCountries,
  getIssues,
  getOrganizations,
  obligationById,
  opened
};
