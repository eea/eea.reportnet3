import dayjs from 'dayjs';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { businessDataflowRepository } from 'repositories/BusinessDataflowRepository';

import { DataCollection } from 'entities/DataCollection';
import { BusinessDataflow } from 'entities/BusinessDataflow';
import { Dataset } from 'entities/Dataset';
import { EuDataset } from 'entities/EuDataset';
import { LegalInstrument } from 'entities/LegalInstrument';
import { Obligation } from 'entities/Obligation';
import { Representative } from 'entities/Representative';
import { WebLink } from 'entities/WebLink';

const create = async (name, description, obligationId, dataProviderGroupId, fmeUserId) =>
  businessDataflowRepository.create(name, description, obligationId, dataProviderGroupId, fmeUserId);

const edit = async (dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId) =>
  businessDataflowRepository.edit(dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId);

const getAll = async userData => {
  const businessDataflowsDTO = await businessDataflowRepository.all(userData);

  businessDataflowsDTO.data = parseDataflowDTOs(businessDataflowsDTO.data);
  return businessDataflowsDTO;
};

const parseDataflowDTOs = dataflowDTOs => {
  const dataflows = dataflowDTOs.map(dataflowDTO => parseDataflowDTO(dataflowDTO));
  dataflows.sort((a, b) => {
    const deadline_1 = a.expirationDate;
    const deadline_2 = b.expirationDate;
    return deadline_1 < deadline_2 ? -1 : deadline_1 > deadline_2 ? 1 : 0;
  });
  return dataflows;
};

const parseDataflowDTO = dataflowDTO => {
  const dataflow = new BusinessDataflow({
    creationDate: dataflowDTO.creationDate,
    dataCollections: parseDataCollectionListDTO(dataflowDTO.dataCollections),
    datasets: parseDatasetListDTO(dataflowDTO.reportingDatasets),
    description: dataflowDTO.description,
    designDatasets: parseDatasetListDTO(dataflowDTO.designDatasets),
    documents: parseDocumentListDTO(dataflowDTO.documents),
    euDatasets: parseEuDatasetListDTO(dataflowDTO.euDatasets),
    expirationDate: dataflowDTO.deadlineDate > 0 ? dayjs(dataflowDTO.deadlineDate).format('YYYY-MM-DD') : '-',
    id: dataflowDTO.id,
    isReleasable: dataflowDTO.releasable,
    manualAcceptance: dataflowDTO.manualAcceptance,
    name: dataflowDTO.name,
    obligation: parseObligationDTO(dataflowDTO.obligation),
    referenceDatasets: parseDatasetListDTO(dataflowDTO.referenceDatasets),
    reportingDatasetsStatus: dataflowDTO.reportingStatus,
    representatives: parseRepresentativeListDTO(dataflowDTO.representatives),
    requestId: dataflowDTO.requestId,
    status: dataflowDTO.status,
    testDatasets: parseDatasetListDTO(dataflowDTO.testDatasets),
    type: dataflowDTO.type,
    userRole: dataflowDTO.userRole,
    weblinks: parseWebLinkListDTO(dataflowDTO.weblinks)
  });

  return dataflow;
};

const parseDataCollectionListDTO = dataCollectionsDTO => {
  if (!isNull(dataCollectionsDTO) && !isUndefined(dataCollectionsDTO)) {
    const dataCollections = [];
    dataCollectionsDTO.forEach(dataCollectionDTO => {
      dataCollections.push(parseDataCollectionDTO(dataCollectionDTO));
    });
    return dataCollections;
  }
  return;
};

const parseDataCollectionDTO = dataCollectionDTO => {
  return new DataCollection({
    creationDate: dataCollectionDTO.creationDate,
    dataCollectionId: dataCollectionDTO.id,
    dataCollectionName: dataCollectionDTO.dataSetName,
    dataflowId: dataCollectionDTO.idDataflow,
    datasetSchemaId: dataCollectionDTO.datasetSchema,
    expirationDate: dataCollectionDTO.dueDate,
    status: dataCollectionDTO.status
  });
};

const parseDatasetListDTO = datasetsDTO => {
  if (!isNull(datasetsDTO) && !isUndefined(datasetsDTO)) {
    const datasets = [];
    datasetsDTO.forEach(datasetDTO => {
      datasets.push(parseDatasetDTO(datasetDTO));
    });
    return datasets;
  }
  return;
};

const parseDatasetDTO = datasetDTO =>
  new Dataset({
    availableInPublic: datasetDTO.availableInPublic,
    datasetId: datasetDTO.id,
    datasetSchemaId: datasetDTO.datasetSchema,
    datasetSchemaName: datasetDTO.dataSetName,
    isReleased: datasetDTO.isReleased,
    isReleasing: datasetDTO.releasing,
    publicFileName: datasetDTO.publicFileName,
    referenceDataset: datasetDTO.referenceDataset,
    releaseDate: datasetDTO.dateReleased > 0 ? dayjs(datasetDTO.dateReleased).format('YYYY-MM-DD HH:mm') : '-',
    restrictFromPublic: datasetDTO.restrictFromPublic,
    name: datasetDTO.nameDatasetSchema,
    dataProviderId: datasetDTO.dataProviderId,
    updatable: datasetDTO.updatable
  });

const parseDocumentListDTO = documentsDTO => {
  if (!isNull(documentsDTO) && !isUndefined(documentsDTO)) {
    const documents = [];
    documentsDTO.forEach(documentDTO => {
      documents.push(parseDocumentDTO(documentDTO));
    });
    return documents;
  }
  return;
};

const parseDocumentDTO = documentDTO => {
  return new Document({
    category: documentDTO.category,
    description: documentDTO.description,
    id: documentDTO.id,
    language: documentDTO.language,
    title: documentDTO.name
  });
};

const parseEuDatasetListDTO = euDatasetsDTO => {
  if (!isNull(euDatasetsDTO) && !isUndefined(euDatasetsDTO)) {
    const euDatasets = [];
    euDatasetsDTO.forEach(euDatasetDTO => {
      euDatasets.push(parseEuDatasetDTO(euDatasetDTO));
    });
    return euDatasets;
  }
  return;
};

const parseEuDatasetDTO = euDatasetDTO => {
  return new EuDataset({
    creationDate: euDatasetDTO.creationDate,
    euDatasetId: euDatasetDTO.id,
    euDatasetName: euDatasetDTO.dataSetName,
    dataflowId: euDatasetDTO.idDataflow,
    datasetSchemaId: euDatasetDTO.datasetSchema,
    expirationDate: euDatasetDTO.dueDate,
    status: euDatasetDTO.status
  });
};

const parseObligationDTO = obligationDTO => {
  if (!isNil(obligationDTO)) {
    return new Obligation({
      comment: obligationDTO.comment,
      countries: obligationDTO.countries,
      description: obligationDTO.description,
      expirationDate: !isNil(obligationDTO.nextDeadline)
        ? dayjs(obligationDTO.nextDeadline).format('YYYY-MM-DD')
        : null,
      issues: obligationDTO.issues,
      legalInstruments: parseLegalInstrument(obligationDTO.legalInstrument),
      obligationId: obligationDTO.obligationId,
      reportingFrequency: obligationDTO.reportFreq,
      reportingFrequencyDetail: obligationDTO.reportFreqDetail,
      title: obligationDTO.oblTitle,
      validSince: obligationDTO.validSince,
      validTo: obligationDTO.validTo
    });
  }
};

const parseLegalInstrument = legalInstrumentDTO => {
  if (!isNil(legalInstrumentDTO)) {
    return new LegalInstrument({
      alias: legalInstrumentDTO.sourceAlias,
      id: legalInstrumentDTO.sourceId,
      title: legalInstrumentDTO.sourceTitle
    });
  }
  return;
};

const parseRepresentativeListDTO = representativesDTO => {
  if (!isNull(representativesDTO) && !isUndefined(representativesDTO)) {
    const representatives = [];
    representativesDTO.forEach(representativeDTO => {
      representatives.push(parseRepresentativeDTO(representativeDTO));
    });
    return representatives;
  }
  return;
};

const parseRepresentativeDTO = representativeDTO => {
  return new Representative({
    dataProviderGroupId: representativeDTO.dataProviderGroupId,
    dataProviderId: representativeDTO.dataProviderId,
    hasDatasets: representativeDTO.hasDatasets,
    id: representativeDTO.id,
    isReceiptDownloaded: representativeDTO.receiptDownloaded,
    isReceiptOutdated: representativeDTO.receiptOutdated,
    leadReporters: parseLeadReporters(representativeDTO.leadReporters)
  });
};

const parseLeadReporters = (leadReporters = []) =>
  leadReporters.map(leadReporter => ({
    account: leadReporter.email,
    id: leadReporter.id,
    representativeId: leadReporter.representativeId
  }));

const parseWebLinkListDTO = webLinksDTO => {
  if (!isNull(webLinksDTO) && !isUndefined(webLinksDTO)) {
    const webLinks = [];
    webLinksDTO.forEach(webLinkDTO => {
      webLinks.push(parseWebLinkDTO(webLinkDTO));
    });
    return webLinks;
  }
  return;
};

const parseWebLinkDTO = webLinkDTO => new WebLink(webLinkDTO);

export const BusinessDataflowService = {
  create,
  edit,
  getAll
};
