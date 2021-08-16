import { apiBusinessDataflow } from 'core/infrastructure/api/domain/model/BusinessDataflow';

import { BusinessDataflow } from 'core/domain/model/BusinessDataflow/BusinessDataflow';

import { config } from 'conf';

import { CoreUtils, TextUtils, UserRoleUtils } from 'core/infrastructure/CoreUtils';

const all = async userData => {
  const dataflowsDTO = await apiBusinessDataflow.all();
  const dataflows = !userData ? dataflowsDTO.data : [];

  if (userData) {
    const userRoles = [];
    const dataflowsRoles = userData.filter(role => role.includes(config.permissions.prefixes.DATAFLOW));
    dataflowsRoles.map((item, i) => {
      const role = TextUtils.reduceString(item, `${item.replace(/\D/g, '')}-`);

      return (userRoles[i] = { id: parseInt(item.replace(/\D/g, '')), userRole: UserRoleUtils.getUserRoleLabel(role) });
    });

    for (let index = 0; index < dataflowsDTO.data.length; index++) {
      const dataflow = dataflowsDTO.data[index];
      const isDuplicated = CoreUtils.isDuplicatedInObject(userRoles, 'id');
      const isOpen = dataflow.status === config.dataflowStatus.OPEN;

      if (isOpen) {
        dataflow.releasable ? (dataflow.status = 'OPEN') : (dataflow.status = 'CLOSED');
      }

      dataflows.push({
        ...dataflow,
        ...(isDuplicated ? UserRoleUtils.getUserRoles(userRoles) : userRoles).find(item => item.id === dataflow.id)
      });
    }
  }

  dataflowsDTO.data = parseDataflowDTOs(dataflows);

  return dataflowsDTO;
};

const create = async (name, description, obligationId, type, groupCompaniesId, fmeUserId) =>
  apiBusinessDataflow.create(name, description, obligationId, type, groupCompaniesId, fmeUserId);

const edit = async (dataflowId, description, name, type, groupCompaniesId, fmeUserId) =>
  apiBusinessDataflow.edit(dataflowId, description, name, type, groupCompaniesId, fmeUserId);

const parseDataflowDTO = dataflowDTO =>
  new BusinessDataflow({
    creationDate: dataflowDTO.creationDate,
    description: dataflowDTO.description,
    id: dataflowDTO.id,
    isReleasable: dataflowDTO.releasable,
    name: dataflowDTO.name,
    reportingDatasetsStatus: dataflowDTO.reportingStatus,
    requestId: dataflowDTO.requestId,
    status: dataflowDTO.status,
    userRole: dataflowDTO.userRole
  });

const parseDataflowDTOs = dataflowDTOs => {
  const dataflows = dataflowDTOs.map(dataflowDTO => parseDataflowDTO(dataflowDTO));
  dataflows.sort((a, b) => {
    const deadline_1 = a.expirationDate;
    const deadline_2 = b.expirationDate;
    return deadline_1 < deadline_2 ? -1 : deadline_1 > deadline_2 ? 1 : 0;
  });
  return dataflows;
};

export const ApiBusinessDataflowRepository = {
  all,
  create,
  edit
};
