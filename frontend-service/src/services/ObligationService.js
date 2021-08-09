import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { ObligationRepository } from 'repositories/ObligationRepository';

import { Country } from 'entities/Country';
import { Issue } from 'entities/Issue';
import { LegalInstrument } from 'entities/LegalInstrument';
import { Obligation } from 'entities/Obligation';
import { Organization } from 'entities/Organization';

const parseObligationList = (obligationsDTO = []) =>
  obligationsDTO.map(obligationDTO => parseObligation(obligationDTO));

const parseCountryList = (countriesDTO = []) => countriesDTO.map(countryDTO => parseCountry(countryDTO));

const parseIssueList = (issuesDTO = []) => issuesDTO.map(issueDTO => parseIssue(issueDTO));

const parseOrganizationList = (organizationsDTO = []) =>
  organizationsDTO.map(organizationDTO => parseOrganization(organizationDTO));

const parseLegalInstrument = legalInstrumentDTO => {
  if (isNil(legalInstrumentDTO)) return;

  return new LegalInstrument({
    alias: legalInstrumentDTO.sourceAlias,
    id: legalInstrumentDTO.sourceId,
    title: legalInstrumentDTO.sourceTitle
  });
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

const parseCountry = countryDTO =>
  new Country({
    countryCode: countryDTO.twoLetter,
    countryMember: countryDTO.memberCommunity,
    id: countryDTO.spatialId,
    name: countryDTO.name,
    type: countryDTO.type
  });

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

const parseIssue = issueDTO => new Issue({ id: issueDTO.issueId, name: issueDTO.issueName });

export const ObligationService = {
  getCountries: async () => {
    const countriesDTO = await ObligationRepository.getCountries();
    return parseCountryList(countriesDTO.data);
  },

  getIssues: async () => {
    const issuesDTO = await ObligationRepository.getIssues();
    return parseIssueList(issuesDTO.data);
  },

  getOrganizations: async () => {
    const clientsDTO = await ObligationRepository.getOrganizations();
    return parseOrganizationList(clientsDTO.data);
  },

  get: async obligationId => {
    const obligationByIdDTO = await ObligationRepository.get(obligationId);
    return parseObligation(obligationByIdDTO.data);
  },

  getOpen: async filterData => {
    if (!isEmpty(filterData)) {
      const countryId = !isNil(filterData.countries) ? filterData.countries.value : '';
      const dateFrom = filterData.expirationDate[0] ? filterData.expirationDate[0].getTime() : '';
      const dateTo = filterData.expirationDate[1] ? filterData.expirationDate[1].getTime() : '';
      const issueId = !isNil(filterData.issues) ? filterData.issues.value : '';
      const organizationId = !isNil(filterData.organizations) ? filterData.organizations.value : '';
      const openedObligationsDTO = await ObligationRepository.getOpen(
        countryId,
        dateFrom,
        dateTo,
        issueId,
        organizationId
      );
      return parseObligationList(openedObligationsDTO.data);
    } else {
      const openedObligationsDTO = await ObligationRepository.getOpen();
      return parseObligationList(openedObligationsDTO.data);
    }
  },

  parseObligation
};
