import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';

const getMetadata = async ({ dataflowId, datasetId }) => {
  const metadata = {};
  if (dataflowId) {
    const dataflowMetadata = await DataflowService.getDetails(dataflowId);

    metadata.dataflow = {
      dataflowId,
      name: dataflowMetadata.name || '',
      description: dataflowMetadata.description || '',
      type: dataflowMetadata.type,
      status: dataflowMetadata.status || ''
    };
  }

  if (datasetId) {
    const datasetMetadata = await DatasetService.getMetadata(datasetId);
    metadata.dataset = {
      dataProviderId: datasetMetadata.dataProviderId,
      datasetSchemaId: datasetMetadata.datasetSchemaId,
      datasetId,
      datasetFeedbackStatus: datasetMetadata.datasetFeedbackStatus,
      name: datasetMetadata.datasetSchemaName || ''
    };
  }
  return metadata;
};
export const MetadataUtils = { getMetadata };
