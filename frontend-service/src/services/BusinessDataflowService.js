import dayjs from 'dayjs';
import isNil from 'lodash/isNil';

import { businessDataflowRepository } from 'repositories/BusinessDataflowRepository';

import { BusinessDataflow } from 'entities/BusinessDataflow';
import { LegalInstrument } from 'entities/LegalInstrument';
import { Obligation } from 'entities/Obligation';

const create = async (name, description, obligationId, dataProviderGroupId, fmeUserId) =>
  businessDataflowRepository.create(name, description, obligationId, dataProviderGroupId, fmeUserId);

const edit = async (dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId) =>
  businessDataflowRepository.edit(dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId);

const getAll = async userData => {
  const businessDataflowsDTO = await businessDataflowRepository.all(userData);

  businessDataflowsDTO.data = parseDataflowDTOs(businessDataflowsDTO.data);
  return businessDataflowsDTO;
};

const parseDataflowDTOs = dataflowDTOs => {
  const dataflows = dataflowDTOs.map(dataflowDTO => parseDataflowDTO(dataflowDTO));
  dataflows.sort((a, b) => {
    const deadline_1 = a.expirationDate;
    const deadline_2 = b.expirationDate;
    return deadline_1 < deadline_2 ? -1 : deadline_1 > deadline_2 ? 1 : 0;
  });
  return dataflows;
};

const parseDataflowDTO = dataflowDTO => {
  const dataflow = new BusinessDataflow({
    creationDate: dataflowDTO.creationDate,
    description: dataflowDTO.description,
    expirationDate: dataflowDTO.deadlineDate > 0 ? dayjs(dataflowDTO.deadlineDate).format('YYYY-MM-DD') : '-',
    id: dataflowDTO.id,
    isReleasable: dataflowDTO.releasable,
    name: dataflowDTO.name,
    obligation: parseObligationDTO(dataflowDTO.obligation),
    status: dataflowDTO.status,
    type: dataflowDTO.type,
    userRole: dataflowDTO.userRole
  });

  return dataflow;
};

const parseObligationDTO = obligationDTO => {
  if (!isNil(obligationDTO)) {
    return new Obligation({
      comment: obligationDTO.comment,
      countries: obligationDTO.countries,
      description: obligationDTO.description,
      expirationDate: !isNil(obligationDTO.nextDeadline)
        ? dayjs(obligationDTO.nextDeadline).format('YYYY-MM-DD')
        : null,
      issues: obligationDTO.issues,
      legalInstruments: parseLegalInstrument(obligationDTO.legalInstrument),
      obligationId: obligationDTO.obligationId,
      reportingFrequency: obligationDTO.reportFreq,
      reportingFrequencyDetail: obligationDTO.reportFreqDetail,
      title: obligationDTO.oblTitle,
      validSince: obligationDTO.validSince,
      validTo: obligationDTO.validTo
    });
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

export const BusinessDataflowService = {
  create,
  edit,
  getAll
};
