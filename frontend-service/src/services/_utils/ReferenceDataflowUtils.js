import { ReferenceDataflow } from 'entities/ReferenceDataflow';

import { DatasetUtils } from 'services/_utils/DatasetUtils';

const parseReferenceDataflowDTO = referenceDataflowDTO =>
  new ReferenceDataflow({
    anySchemaAvailableInPublic: referenceDataflowDTO.anySchemaAvailableInPublic,
    bigData: referenceDataflowDTO.bigData,
    creationDate: referenceDataflowDTO.creationDate,
    datasets: DatasetUtils.parseDatasetListDTO(referenceDataflowDTO.reportingDatasets),
    deleted: referenceDataflowDTO.deleted,
    deletedAt: referenceDataflowDTO.deletedAt,
    description: referenceDataflowDTO.description,
    designDatasets: DatasetUtils.parseDatasetListDTO(referenceDataflowDTO.designDatasets),
    id: referenceDataflowDTO.id,
    isReleasable: referenceDataflowDTO.releasable,
    name: referenceDataflowDTO.name,
    officialReporting: referenceDataflowDTO.officialReporting,
    referenceDatasets: DatasetUtils.parseDatasetListDTO(referenceDataflowDTO.referenceDatasets),
    reportingDatasetsStatus: referenceDataflowDTO.reportingStatus,
    requestId: referenceDataflowDTO.requestId,
    showPublicInfo: referenceDataflowDTO.showPublicInfo,
    sncData: referenceDataflowDTO.sncData,
    status: referenceDataflowDTO.status,
    testDatasets: DatasetUtils.parseDatasetListDTO(referenceDataflowDTO.testDatasets),
    type: referenceDataflowDTO.type,
    userRole: referenceDataflowDTO.userRole,
    useViews: referenceDataflowDTO.useViews
  });

export const ReferenceDataflowUtils = {
  parseReferenceDataflowDTO
};
