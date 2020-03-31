import isNil from 'lodash/isNil';
import moment from 'moment';

import { apiObligation } from 'core/infrastructure/api/domain/model/Obligation';

import { Client } from 'core/domain/model/Obligation/Client/Client';
import { Country } from 'core/domain/model/Obligation/Country/Country';
import { Issue } from 'core/domain/model/Obligation/Issue/Issue';
import { LegalInstrument } from 'core/domain/model/Obligation/LegalInstrument/LegalInstrument';
import { Obligation } from 'core/domain/model/Obligation/Obligation';

const parseClient = clientDTO => {
  if (!isNil(clientDTO)) {
    new Client({
      acronym: clientDTO.acronym,
      address: clientDTO.address,
      city: clientDTO.city,
      country: clientDTO.country,
      description: clientDTO.description,
      email: clientDTO.email,
      id: clientDTO.clientId,
      name: clientDTO.name,
      postalCode: clientDTO.postalCode,
      shortName: clientDTO.shortName,
      url: clientDTO.url
    });
  }
  return;
};

const parseClientList = clientsDTO => {
  if (!isNil(clientsDTO)) {
    const clients = [];
    clientsDTO.forEach(clientDTO => clients.push(parseClient(clientDTO)));
    return clients;
  }
  return;
};

const parseCountry = countryDTO => {
  if (!isNil(countryDTO)) {
    new Country({
      memberCommunity: countryDTO.memberCommunity,
      name: countryDTO.name,
      spatialId: countryDTO.spatialId,
      type: countryDTO.type,
      twoLetter: countryDTO.twoLetter
    });
  }
  return;
};

const parseCountryList = countriesDTO => {
  if (!isNil(countriesDTO)) {
    const countries = [];
    countriesDTO.forEach(countryDTO => countries.push(parseCountry(countryDTO)));
    return countries;
  }
  return;
};

const parseIssue = issueDTO => {
  if (!isNil(issueDTO)) {
    new Issue({
      issueId: issueDTO.issueId,
      issueName: issueDTO.issueName
    });
  }
  return;
};

const parseIssueList = issuesDTO => {
  if (!isNil(issuesDTO)) {
    const issues = [];
    issuesDTO.forEach(issueDTO => issues.push(parseIssue(issueDTO)));
    return issues;
  }
  return;
};

const parseLegalInstrument = legalInstrumentDTO => {
  if (!isNil(legalInstrumentDTO)) {
    new LegalInstrument({
      alias: legalInstrumentDTO.sourceAlias,
      id: legalInstrumentDTO.sourceId,
      title: legalInstrumentDTO.sourceTitle
    });
  }
  return;
};

const parseObligation = obligationDTO =>
  new Obligation({
    client: parseClient(obligationDTO.client),
    comment: obligationDTO.comment,
    countries: obligationDTO.countries,
    description: obligationDTO.description,
    expirationDate: moment.unix(obligationDTO.nextDeadline).format('YYYY-MM-DD'),
    issues: obligationDTO.issues,
    legalInstruments: parseLegalInstrument(obligationDTO.legalInstrument),
    obligationId: obligationDTO.obligationId,
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

const getClients = async () => {
  const clientsDTO = await apiObligation.getClients();
  return parseClientList(clientsDTO);
};

const getCountries = async () => {
  const countriesDTO = await apiObligation.getCountries();
  return parseCountryList(countriesDTO);
};

const getIssues = async () => {
  const issuesDTO = await apiObligation.getIssues();
  return parseIssueList(issuesDTO);
};

const getObligationById = async obligationId => {
  const obligationByIdDTO = await apiObligation.getObligationByID(obligationId);
  const obligationById = new Obligation(obligationByIdDTO);

  return obligationById;
};

const opened = async () => {
  const openedObligationsDTO = await apiObligation.openedObligations();
  return parseObligationList(openedObligationsDTO);
};

export const ApiObligationRepository = {
  getClients,
  getCountries,
  getIssues,
  getObligationById,
  opened
};
