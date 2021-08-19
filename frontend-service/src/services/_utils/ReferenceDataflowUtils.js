import { ReferenceDataflow } from 'entities/ReferenceDataflow';

import { DatasetUtils } from 'services/_utils/DatasetUtils';

const parseReferenceDataflowDTO = referenceDataflowDTO =>
  new ReferenceDataflow({
    anySchemaAvailableInPublic: referenceDataflowDTO.anySchemaAvailableInPublic,
    creationDate: referenceDataflowDTO.creationDate,
    datasets: DatasetUtils.parseDatasetListDTO(referenceDataflowDTO.reportingDatasets),
    description: referenceDataflowDTO.description,
    designDatasets: DatasetUtils.parseDatasetListDTO(referenceDataflowDTO.designDatasets),
    id: referenceDataflowDTO.id,
    isReleasable: referenceDataflowDTO.releasable,
    name: referenceDataflowDTO.name,
    referenceDatasets: DatasetUtils.parseDatasetListDTO(referenceDataflowDTO.referenceDatasets),
    reportingDatasetsStatus: referenceDataflowDTO.reportingStatus,
    requestId: referenceDataflowDTO.requestId,
    showPublicInfo: referenceDataflowDTO.showPublicInfo,
    status: referenceDataflowDTO.status,
    testDatasets: DatasetUtils.parseDatasetListDTO(referenceDataflowDTO.testDatasets),
    userRole: referenceDataflowDTO.userRole
  });

export const ReferenceDataflowUtils = {
  parseReferenceDataflowDTO
};
