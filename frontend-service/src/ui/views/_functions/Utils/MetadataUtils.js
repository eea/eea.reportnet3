import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

const getDataflowMetadata = async dataflowId => {
  try {
    const dataflowDetails = await DataflowService.dataflowDetails(dataflowId);
    return dataflowDetails.data;
  } catch (error) {
    console.error('MetadataUtils - getDataflowMetadata', error);
    return {};
  }
};
const getDatasetMetadata = async datasetId => {
  try {
    const datasetDetails = await DatasetService.getMetaData(datasetId);
    return datasetDetails.data;
  } catch (error) {
    console.error('MetadataUtils - getDatasetMetadata', error);
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
      status: dataflowMetadata.status || ''
    };
  }

  if (datasetId) {
    const datasetMetadata = await getDatasetMetadata(datasetId);
    metadata.dataset = {
      dataProviderId: datasetMetadata.dataProviderId,
      datasetId,
      name: datasetMetadata.datasetSchemaName || ''
    };
  }
  return metadata;
};
export const MetadataUtils = { getDataflowMetadata, getDatasetMetadata, getMetadata };
