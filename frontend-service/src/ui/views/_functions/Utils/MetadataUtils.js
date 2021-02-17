import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

const getDataflowMetadata = async dataflowId => {
  try {
    return await DataflowService.dataflowDetails(dataflowId);
  } catch (error) {
    console.error('dataflowDetails error', error);
    return {};
  }
};
const getDatasetMetadata = async datasetId => {
  try {
    return await DatasetService.getMetaData(datasetId);
  } catch (error) {
    console.error('DatasetService.getMetaData', error);
    return {};
  }
};

const getMetadata = async ({ dataflowId, datasetId }) => {
  const metadata = {};
  if (dataflowId) {
    const dataflowMetadata = await getDataflowMetadata(dataflowId);

    metadata.dataflow = {
      dataflowId,
      name: dataflowMetadata.name || '',
      description: dataflowMetadata.description || '',
      status: dataflowMetadata.status || '',
      showPublicInfo: dataflowMetadata.showPublicInfo || false
    };
  }

  if (datasetId) {
    const datasetMetadata = await getDatasetMetadata(datasetId);
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
