import dayjs from 'dayjs';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import { BusinessDataflowRepository } from 'repositories/BusinessDataflowRepository';

import { BusinessDataflow } from 'entities/BusinessDataflow';
import { LegalInstrument } from 'entities/LegalInstrument';
import { Obligation } from 'entities/Obligation';

import { CoreUtils } from 'repositories/_utils/CoreUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';
import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

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
  getAll: async (accessRole, contextRoles) => {
    const businessDataflowsDTO = await BusinessDataflowRepository.getAll();

    const businessDataflows = !accessRole ? businessDataflowsDTO.data : [];

    const userRoles = [];
    if (contextRoles) {
      const dataflowsRoles = contextRoles.filter(role => role.includes(config.permissions.prefixes.DATAFLOW));
      dataflowsRoles.map((item, i) => {
        const role = TextUtils.reduceString(item, `${item.replace(/\D/g, '')}-`);

        return (userRoles[i] = {
          id: parseInt(item.replace(/\D/g, '')),
          userRole: UserRoleUtils.getUserRoleLabel(role)
        });
      });
    }

    for (let index = 0; index < businessDataflowsDTO.data.length; index++) {
      const businessDataflow = businessDataflowsDTO.data[index];

      const isOpen = businessDataflow.status === config.dataflowStatus.OPEN;

      if (isOpen) {
        businessDataflow.releasable ? (businessDataflow.status = 'OPEN') : (businessDataflow.status = 'CLOSED');
      }

      if (contextRoles.length === 0) {
        businessDataflow.userRole =
          accessRole.some(role => role === config.permissions.roles.ADMIN.key) && config.permissions.roles.ADMIN.key; // TODO WITH TWO ROLES
        businessDataflows.push({
          ...businessDataflow
        });
      } else {
        const isDuplicated = CoreUtils.isDuplicatedInObject(userRoles, 'id');
        businessDataflows.push({
          ...businessDataflow,
          ...(isDuplicated ? UserRoleUtils.getUserRoles(userRoles) : userRoles).find(
            item => item.id === businessDataflow.id
          )
        });
      }
    }

    businessDataflowsDTO.data = parseDataflowDTOs(businessDataflowsDTO.data);
    return businessDataflowsDTO;
  },

  create: async (name, description, obligationId, dataProviderGroupId, fmeUserId) =>
    BusinessDataflowRepository.create(name, description, obligationId, dataProviderGroupId, fmeUserId),

  update: async (dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId) =>
    BusinessDataflowRepository.update(dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId)
};
