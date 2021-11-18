import dayjs from 'dayjs';

import isNil from 'lodash/isNil';

import { Country } from 'entities/Country';
import { Issue } from 'entities/Issue';
import { LegalInstrument } from 'entities/LegalInstrument';
import { Obligation } from 'entities/Obligation';
import { Organization } from 'entities/Organization';

const parseObligationList = obligationsDTO => obligationsDTO?.map(obligationDTO => parseObligation(obligationDTO));

const parseCountryList = countriesDTO => countriesDTO?.map(countryDTO => parseCountry(countryDTO));

const parseIssueList = issuesDTO => issuesDTO?.map(issueDTO => parseIssue(issueDTO));

const parseOrganizationList = organizationsDTO =>
  organizationsDTO?.map(organizationDTO => parseOrganization(organizationDTO));

const parseLegalInstrument = legalInstrumentDTO => {
  if (isNil(legalInstrumentDTO)) return;

  return new LegalInstrument({
    alias: legalInstrumentDTO.sourceAlias,
    id: legalInstrumentDTO.sourceId,
    title: legalInstrumentDTO.sourceTitle
  });
};

const parseObligation = obligationDTO => {
  if (isNil(obligationDTO)) return;

  return new Obligation({
    comment: obligationDTO.comment,
    countries: obligationDTO.countries,
    description: obligationDTO.description,
    expirationDate: obligationDTO.nextDeadline > 0 ? dayjs(obligationDTO.nextDeadline).format('YYYY-MM-DD') : null,
    issues: obligationDTO.issues,
    legalInstrument: parseLegalInstrument(obligationDTO.legalInstrument),
    obligationId: obligationDTO.obligationId,
    reportingFrequency: obligationDTO.reportFreq,
    reportingFrequencyDetail: obligationDTO.reportFreqDetail,
    organization: parseOrganization(obligationDTO.client),
    title: obligationDTO.oblTitle,
    validSince: obligationDTO.validSince,
    validTo: obligationDTO.validTo
  });
};

const parseCountry = countryDTO => {
  if (isNil(countryDTO)) return;

  return new Country({
    countryCode: countryDTO.twoLetter,
    countryMember: countryDTO.memberCommunity,
    id: countryDTO.spatialId,
    name: countryDTO.name,
    type: countryDTO.type
  });
};

const parseOrganization = organizationDTO => {
  if (isNil(organizationDTO)) return;

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
};

const parseIssue = issueDTO => {
  if (isNil(issueDTO)) return;

  return new Issue({ id: issueDTO.issueId, name: issueDTO.issueName });
};

export const ObligationUtils = {
  parseLegalInstrument,
  parseObligationList,
  parseObligation,
  parseCountryList,
  parseIssueList,
  parseOrganizationList
};
