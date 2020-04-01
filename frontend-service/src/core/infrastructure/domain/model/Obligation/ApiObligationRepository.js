import isNil from 'lodash/isNil';
import moment from 'moment';

import { apiObligation } from 'core/infrastructure/api/domain/model/Obligation';

import { LegalInstrument } from 'core/domain/model/Obligation/LegalInstrument/LegalInstrument';
import { Obligation } from 'core/domain/model/Obligation/Obligation';
import { Organization } from 'core/domain/model/Obligation/Organization/Organization';

const parseClient = clientDTO => {
  if (!isNil(clientDTO)) {
    return new Organization({
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
