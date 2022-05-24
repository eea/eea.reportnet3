import { DataCollection } from 'entities/DataCollection';

const parseDataCollectionListDTO = dataCollectionsDTO =>
  dataCollectionsDTO?.map(dataCollectionDTO => parseDataCollectionDTO(dataCollectionDTO));

const parseDataCollectionDTO = dataCollectionDTO =>
  new DataCollection({
    creationDate: dataCollectionDTO.creationDate,
    dataCollectionId: dataCollectionDTO.id,
    dataCollectionName: dataCollectionDTO.dataSetName,
    dataflowId: dataCollectionDTO.idDataflow,
    datasetSchemaId: dataCollectionDTO.datasetSchema,
    expirationDate: dataCollectionDTO.dueDate,
    status: dataCollectionDTO.status
  });

export const DataCollectionUtils = {
  parseDataCollectionListDTO
};
