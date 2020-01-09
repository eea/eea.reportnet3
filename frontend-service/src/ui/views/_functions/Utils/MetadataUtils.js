import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

const getDataflowMetadata = async dataflowId => {
  try {
    return await DataflowService.dataflowDetails(dataflowId);
  } catch (error) {
    console.log('dataflowDetails error', error);
    return {};
  }
};
const getDatasetMetadata = async datasetId => {
  try {
    return await DatasetService.getMetaData(datasetId);
  } catch (error) {
    console.log('DatasetService.getMetaData', error);
    return {};
  }
};

const getMetadata = ({ dataflowId, datasetId }) => {
  const metadata = {};
  if (dataflowId) {
    const dataflowMedatada = getDataflowMetadata();
    metadata.dataflow = {
      dataflowId,
      name: dataflowMedatada.name || '',
      description: dataflowMedatada.description || ''
    };
  }

  if (datasetId) {
    const datasetMetadata = getDatasetMetadata();
    metadata.dataset = {
      datasetId,
      name: datasetMetadata.datasetSchemaName || ''
    };
  }
  return metadata;
};
export const MetadataUtils = {
  getDataflowMetadata,
  getDatasetMetadata,
  getMetadata
};
