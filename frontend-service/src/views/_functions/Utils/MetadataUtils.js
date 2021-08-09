import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';

const getMetadata = async ({ dataflowId, datasetId }) => {
  const metadata = {};
  if (dataflowId) {
    const dataflowMetadata = await DataflowService.getDataflowDetails(dataflowId);

    metadata.dataflow = {
      dataflowId,
      name: dataflowMetadata.name || '',
      description: dataflowMetadata.description || '',
      type: dataflowMetadata.type, // TODO TEST WITH REAL DATA
      status: dataflowMetadata.status || ''
    };
  }

  if (datasetId) {
    const datasetMetadata = await DatasetService.getMetaData(datasetId);
    metadata.dataset = {
      dataProviderId: datasetMetadata.dataProviderId,
      datasetId,
      name: datasetMetadata.datasetSchemaName || ''
    };
  }
  return metadata;
};
export const MetadataUtils = { getMetadata };
