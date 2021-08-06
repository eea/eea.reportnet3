import dayjs from 'dayjs';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { ReferenceDataflowRepository } from 'repositories/ReferenceDataflowRepository';

import { Dataset } from 'entities/Dataset';
import { ReferenceDataflow } from 'entities/ReferenceDataflow';

import { config } from 'conf';

import { CoreUtils } from 'repositories/_utils/CoreUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';
import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

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

const sortDatasetTypeByName = (a, b) => {
  let datasetName_A = a.datasetSchemaName;
  let datasetName_B = b.datasetSchemaName;
  return datasetName_A < datasetName_B ? -1 : datasetName_A > datasetName_B ? 1 : 0;
};

export const ReferenceDataflowService = {
  getAll: async (userData = []) => {
    const dataflowsDTO = await ReferenceDataflowRepository.getAll();
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
  },

  create: async (name, description, type) => ReferenceDataflowRepository.create(name, description, type),

  delete: async referenceDataflowId => ReferenceDataflowRepository.delete(referenceDataflowId),

  update: async (dataflowId, description, name, type) =>
    ReferenceDataflowRepository.update(dataflowId, description, name, type),

  getReferencingDataflows: async referenceDataflowId =>
    await ReferenceDataflowRepository.getReferencingDataflows(referenceDataflowId),

  getReferenceDataflow: async referenceDataflowId => {
    const referenceDataflowDTO = await ReferenceDataflowRepository.referenceDataflow(referenceDataflowId);
    const dataflow = parseDataflowDTO(referenceDataflowDTO.data);
    dataflow.datasets.sort(sortDatasetTypeByName);
    dataflow.designDatasets.sort(sortDatasetTypeByName);
    referenceDataflowDTO.data = dataflow;

    return referenceDataflowDTO;
  }
};
