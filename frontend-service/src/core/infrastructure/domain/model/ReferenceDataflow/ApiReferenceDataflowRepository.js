import dayjs from 'dayjs';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { apiReferenceDataflow } from 'core/infrastructure/api/domain/model/ReferenceDataflow';

import { Dataset } from 'core/domain/model/Dataset/Dataset';
import { ReferenceDataflow } from 'core/domain/model/ReferenceDataflow/ReferenceDataflow';

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
  new ReferenceDataflow({
    anySchemaAvailableInPublic: dataflowDTO.anySchemaAvailableInPublic,
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
    showPublicInfo: dataflowDTO.showPublicInfo,
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
  const dataflowsDTO = await apiReferenceDataflow.all();
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

const create = async (name, description, type) => apiReferenceDataflow.create(name, description, type);

const deleteReferenceDataflow = async referenceDataflowId =>
  apiReferenceDataflow.deleteReferenceDataflow(referenceDataflowId);

const edit = async (dataflowId, description, name, type) =>
  apiReferenceDataflow.edit(dataflowId, description, name, type);

const getReferencingDataflows = async referenceDataflowId => {
  const referenceDataflowDTO = await apiReferenceDataflow.getReferencingDataflows(referenceDataflowId);

  return referenceDataflowDTO;
};

const sortDatasetTypeByName = (a, b) => {
  let datasetName_A = a.datasetSchemaName;
  let datasetName_B = b.datasetSchemaName;
  return datasetName_A < datasetName_B ? -1 : datasetName_A > datasetName_B ? 1 : 0;
};

const referenceDataflow = async referenceDataflowId => {
  const referenceDataflowDTO = await apiReferenceDataflow.referenceDataflow(referenceDataflowId);
  const dataflow = parseDataflowDTO(referenceDataflowDTO.data);
  dataflow.datasets.sort(sortDatasetTypeByName);
  dataflow.designDatasets.sort(sortDatasetTypeByName);
  referenceDataflowDTO.data = dataflow;

  return referenceDataflowDTO;
};

export const ApiReferenceDataflowRepository = {
  all,
  create,
  deleteReferenceDataflow,
  edit,
  getReferencingDataflows,
  referenceDataflow
};
