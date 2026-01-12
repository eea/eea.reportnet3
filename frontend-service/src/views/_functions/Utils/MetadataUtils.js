import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';

const getMetadata = async ({ dataflowId, datasetId }) => {
  const metadata = {};
  if (dataflowId) {
    const dataflowMetadata = await DataflowService.getDetails(dataflowId);

    metadata.dataflow = {
      bigData: dataflowMetadata.bigData,
      dataflowId,
      deleted: dataflowMetadata.deleted,
      deletedAt: dataflowMetadata.deletedAt,
      name: dataflowMetadata.name || '',
      description: dataflowMetadata.description || '',
      type: dataflowMetadata.type,
      sncData: dataflowMetadata.sncData,
      status: dataflowMetadata.status || '',
      representatives: dataflowMetadata.representatives || ''
    };
  }

  if (datasetId) {
    const datasetMetadata = await DatasetService.getMetadata(datasetId);
    metadata.dataset = {
      dataProviderId: datasetMetadata.dataProviderId,
      datasetSchemaId: datasetMetadata.datasetSchemaId,
      datasetId,
      datasetFeedbackStatus: datasetMetadata.datasetFeedbackStatus,
      datasetRunningStatus: datasetMetadata.datasetRunningStatus,
      datasetType: datasetMetadata.datasetType,
      name: datasetMetadata.datasetSchemaName || ''
    };
  }
  return metadata;
};
export const MetadataUtils = { getMetadata };
