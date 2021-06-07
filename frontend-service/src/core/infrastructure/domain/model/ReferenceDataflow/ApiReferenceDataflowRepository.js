import dayjs from 'dayjs';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';

import { apiReferenceDataflow } from 'core/infrastructure/api/domain/model/ReferenceDataflow';

import { Dataset } from 'core/domain/model/Dataset/Dataset';
import { ReferenceDataflow } from 'core/domain/model/ReferenceDataflow/ReferenceDataflow';

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

const all = async userData => {
  const dataflowsDTO = await apiReferenceDataflow.all(userData);

  dataflowsDTO.data.forEach(dataflow => {
    if (dataflow.status === config.dataflowStatus.OPEN) {
      dataflow.releasable ? (dataflow.status = 'OPEN') : (dataflow.status = 'CLOSED');
    }
  });

  return dataflowsDTO;
};

const create = async (name, description, type) => apiReferenceDataflow.create(name, description, type);

const edit = async (dataflowId, description, name) => apiReferenceDataflow.edit(dataflowId, description, name);

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

export const ApiReferenceDataflowRepository = { all, create, edit, getReferencingDataflows, referenceDataflow };
