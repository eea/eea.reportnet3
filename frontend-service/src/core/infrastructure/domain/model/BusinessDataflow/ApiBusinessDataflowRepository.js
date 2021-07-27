import dayjs from 'dayjs';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { apiBusinessDataflow } from 'core/infrastructure/api/domain/model/BusinessDataflow';

import { Dataset } from 'core/domain/model/Dataset/Dataset';
import { BusinessDataflow } from 'core/domain/model/BusinessDataflow/BusinessDataflow';

import { config } from 'conf';

import { CoreUtils, TextUtils, UserRoleUtils } from 'core/infrastructure/CoreUtils';

const parseDatasetListDTO = datasetsDTO => {
  if (!isNull(datasetsDTO) && !isUndefined(datasetsDTO)) {
    const datasets = [];
    datasetsDTO.forEach(datasetDTO => {
      datasets.push(parseDatasetDTO(datasetDTO));
    });
    return datasets;
  }
  return;
};

const parseDatasetDTO = datasetDTO =>
  new Dataset({
    availableInPublic: datasetDTO.availableInPublic,
    datasetId: datasetDTO.id,
    datasetSchemaId: datasetDTO.datasetSchema,
    datasetSchemaName: datasetDTO.dataSetName,
    isReleased: datasetDTO.isReleased,
    isReleasing: datasetDTO.releasing,
    publicFileName: datasetDTO.publicFileName,
    referenceDataset: datasetDTO.referenceDataset,
    releaseDate: datasetDTO.dateReleased > 0 ? dayjs(datasetDTO.dateReleased).format('YYYY-MM-DD HH:mm') : '-',
    restrictFromPublic: datasetDTO.restrictFromPublic,
    name: datasetDTO.nameDatasetSchema,
    dataProviderId: datasetDTO.dataProviderId
  });

const parseDataflowDTO = dataflowDTO =>
  new BusinessDataflow({
    creationDate: dataflowDTO.creationDate,
    datasets: parseDatasetListDTO(dataflowDTO.reportingDatasets),
    description: dataflowDTO.description,
    designDatasets: parseDatasetListDTO(dataflowDTO.designDatasets),
    id: dataflowDTO.id,
    isReleasable: dataflowDTO.releasable,
    name: dataflowDTO.name,
    referenceDatasets: parseDatasetListDTO(dataflowDTO.referenceDatasets),
    reportingDatasetsStatus: dataflowDTO.reportingStatus,
    requestId: dataflowDTO.requestId,
    status: dataflowDTO.status,
    testDatasets: parseDatasetListDTO(dataflowDTO.testDatasets),
    userRole: dataflowDTO.userRole
  });

const sortDataflows = dataflowDTOs => {
  const dataflows = dataflowDTOs.map(dataflowDTO => parseDataflowDTO(dataflowDTO));
  dataflows.sort((a, b) => {
    const deadline_1 = a.expirationDate;
    const deadline_2 = b.expirationDate;
    return deadline_1 < deadline_2 ? -1 : deadline_1 > deadline_2 ? 1 : 0;
  });
  return dataflows;
};

const all = async (userData = []) => {
  const dataflowsDTO = await apiBusinessDataflow.all(userData);
  const userRoles = [];
  const dataflows = [];

  const dataflowsRoles = userData.filter(role => role.includes(config.permissions.prefixes.DATAFLOW));

  dataflowsRoles.map((item, index) => {
    const role = TextUtils.reduceString(item, `${item.replace(/\D/g, '')}-`);
    return (userRoles[index] = {
      id: parseInt(item.replace(/\D/g, '')),
      userRole: UserRoleUtils.getUserRoleLabel(role)
    });
  });

  dataflowsDTO.data.forEach(dataflow => {
    const isDuplicated = CoreUtils.isDuplicatedInObject(userRoles, 'id');
    const role = isDuplicated ? UserRoleUtils.getUserRoles(userRoles) : userRoles;

    dataflows.push({ ...dataflow, ...role.find(item => item.id === dataflow.id) });
  });

  dataflowsDTO.data = sortDataflows(dataflows);

  return dataflowsDTO;
};

const create = async (name, description, type) => apiBusinessDataflow.create(name, description, type);

const edit = async (dataflowId, description, name, type) =>
  apiBusinessDataflow.edit(dataflowId, description, name, type);

export const ApiBusinessDataflowRepository = {
  all,
  create,
  edit
};
