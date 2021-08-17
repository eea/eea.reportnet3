import { EUDataset } from 'entities/EUDataset';

const parseEUDatasetListDTO = euDatasetsDTO => euDatasetsDTO?.map(euDatasetDTO => parseEUDatasetDTO(euDatasetDTO));

const parseEUDatasetDTO = euDatasetDTO => {
  return new EUDataset({
    creationDate: euDatasetDTO.creationDate,
    euDatasetId: euDatasetDTO.id,
    euDatasetName: euDatasetDTO.dataSetName,
    dataflowId: euDatasetDTO.idDataflow,
    datasetSchemaId: euDatasetDTO.datasetSchema,
    expirationDate: euDatasetDTO.dueDate,
    status: euDatasetDTO.status
  });
};

export const EUDatasetUtils = {
  parseEUDatasetListDTO
};
