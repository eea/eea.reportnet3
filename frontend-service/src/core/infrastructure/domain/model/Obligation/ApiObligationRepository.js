import isNil from 'lodash/isNil';
import moment from 'moment';

import { apiObligation } from 'core/infrastructure/api/domain/model/Obligation';

import { LegalInstrument } from 'core/domain/model/Obligation/LegalInstrument/LegalInstrument';
import { Obligation } from 'core/domain/model/Obligation/Obligation';
import { Organization } from 'core/domain/model/Obligation/Organization/Organization';

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
    expirationDate:
      obligationDTO.nextDeadline > 0 ? moment.unix(obligationDTO.nextDeadline / 1000).format('YYYY-MM-DD') : '-',
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

const opened = async () => {
  const openedObligationsDTO = await apiObligation.openedObligations();
  return parseObligationList(openedObligationsDTO);
};

const obligationById = async obligationId => {
  const obligationByIdDTO = await apiObligation.obligationById(obligationId);
  return parseObligation(obligationByIdDTO);
};

export const ApiObligationRepository = {
  obligationById,
  opened
};
