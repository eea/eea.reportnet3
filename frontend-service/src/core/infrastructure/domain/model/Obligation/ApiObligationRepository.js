import isNil from 'lodash/isNil';
import moment from 'moment';

import { apiObligation } from 'core/infrastructure/api/domain/model/Obligation';

import { Country } from 'core/domain/model/Obligation/Country/Country';
import { Issue } from 'core/domain/model/Obligation/Issue/Issue';
import { LegalInstrument } from 'core/domain/model/Obligation/LegalInstrument/LegalInstrument';
import { Obligation } from 'core/domain/model/Obligation/Obligation';
import { Organization } from 'core/domain/model/Obligation/Organization/Organization';

const getCountries = async () => {
  const countriesDTO = await apiObligation.getCountries();
  return parseCountryList(countriesDTO);
};

const getIssues = async () => {
  const issuesDTO = await apiObligation.getIssues();
  return parseIssueList(issuesDTO);
};

const getOrganizations = async () => {
  const clientsDTO = await apiObligation.getOrganizations();
  return parseOrganizationList(clientsDTO);
};

const obligationById = async obligationId => {
  const obligationByIdDTO = await apiObligation.obligationById(obligationId);
  return parseObligation(obligationByIdDTO);
};

const opened = async () => {
  const openedObligationsDTO = await apiObligation.openedObligations();
  return parseObligationList(openedObligationsDTO);
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

const parseIssue = issueDTO => new Issue({ id: issueDTO.id, name: issueDTO.name });

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
    expirationDate: !isNil(obligationDTO.nextDeadline)
      ? moment.unix(obligationDTO.nextDeadline / 1000).format('YYYY-MM-DD')
      : moment(obligationDTO.nextDeadline).format('YYYY-MM-DD'),
    issues: obligationDTO.issues,
    legalInstruments: parseLegalInstrument(obligationDTO.legalInstrument),
    obligationId: obligationDTO.obligationId,
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

const parseOrganization = organizationDTO =>
  new Organization({
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

const parseOrganizationList = organizationsDTO => {
  if (!isNil(organizationsDTO)) {
    const organizations = [];
    organizationsDTO.forEach(organizationDTO => organizations.push(parseOrganization(organizationDTO)));
    return organizations;
  }
  return;
};

export const ApiObligationRepository = {
  getCountries,
  getIssues,
  getOrganizations,
  obligationById,
  opened
};
