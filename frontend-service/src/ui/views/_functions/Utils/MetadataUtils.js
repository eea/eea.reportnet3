import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

const getDataflowMetadata = async dataflowId => await DataflowService.dataflowDetails(dataflowId);
const getDatasetMetadata = async datasetId => await DatasetService.getMetaData(datasetId);

const getMetadata = ({ dataflowId, datasetId }) => {
  const metadata = {};
  if (dataflowId) {
    const dataflowMedatada = getDataflowMetadata();
    metadata.dataflow = {
      dataflowId,
      name: dataflowMedatada.name,
      description: dataflowMedatada.description
    };
  }

  if (datasetId) {
    const datasetMetadata = getDatasetMetadata();
    metadata.dataset = {
      datasetId,
      name: datasetMetadata.datasetSchemaName
    };
  }
  return metadata;
};
export const MetadataUtils = {
  getDataflowMetadata,
  getDatasetMetadata,
  getMetadata
};
