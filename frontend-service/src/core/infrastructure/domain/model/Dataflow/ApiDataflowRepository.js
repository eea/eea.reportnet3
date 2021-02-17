import capitalize from 'lodash/capitalize';
import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import dayjs from 'dayjs';

import { config } from 'conf';
import DataflowConf from 'conf/dataflow.config.json';

import { apiDataflow } from 'core/infrastructure/api/domain/model/Dataflow';

import { DataCollection } from 'core/domain/model/DataCollection/DataCollection';
import { Dataflow } from 'core/domain/model/Dataflow/Dataflow';
import { Dataset } from 'core/domain/model/Dataset/Dataset';
import { DatasetTable } from 'core/domain/model/Dataset/DatasetTable/DatasetTable';
import { DatasetTableField } from 'core/domain/model/Dataset/DatasetTable/DatasetRecord/DatasetTableField/DatasetTableField';
import { DatasetTableRecord } from 'core/domain/model/Dataset/DatasetTable/DatasetRecord/DatasetTableRecord';
import { EuDataset } from 'core/domain/model/EuDataset/EuDataset';
import { LegalInstrument } from 'core/domain/model/Obligation/LegalInstrument/LegalInstrument';
import { Obligation } from 'core/domain/model/Obligation/Obligation';
import { Representative } from 'core/domain/model/Representative/Representative';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

import { CoreUtils, TextUtils } from 'core/infrastructure/CoreUtils';

const accept = async dataflowId => {
  const status = await apiDataflow.accept(dataflowId);
  return status;
};

const accepted = async () => {
  const acceptedDataflowsDTO = await apiDataflow.accepted();
  return parseDataflowDTOs(acceptedDataflowsDTO.filter(item => item.userRequestStatus === 'ACCEPTED'));
};

const getUserRoles = userRoles => {
  const userRoleToDataflow = [];
  userRoles.filter(userRol => {
    return !userRol.duplicatedRoles && userRoleToDataflow.push(userRol);
  });

  const duplicatedRoles = userRoles.filter(userRol => userRol.duplicatedRoles);
  const dataflowDuplicatedRoles = [];
  for (const duplicatedRol of duplicatedRoles) {
    if (dataflowDuplicatedRoles[duplicatedRol.id]) {
      dataflowDuplicatedRoles[duplicatedRol.id].push(duplicatedRol);
    } else {
      dataflowDuplicatedRoles[duplicatedRol.id] = [duplicatedRol];
    }
  }

  const dataflowPermissionsConfig = {
    1: config.dataflowPermissions.DATA_CUSTODIAN,
    2: config.dataflowPermissions.EDITOR_WRITE,
    3: config.dataflowPermissions.EDITOR_READ,
    4: config.dataflowPermissions.LEAD_REPORTER,
    5: config.dataflowPermissions.NATIONAL_COORDINATOR,
    6: config.dataflowPermissions.REPORTER_WRITE,
    7: config.dataflowPermissions.REPORTER_READ
  };

  const dataflowPermissions = Object.values(dataflowPermissionsConfig);

  dataflowDuplicatedRoles.forEach(dataflowRoles => {
    let rol = null;

    dataflowPermissions.forEach(permission => {
      dataflowRoles.forEach(dataflowRol => {
        if (isNil(rol) && dataflowRol.userRole === permission) {
          rol = dataflowRol;
        }
      });
    });

    userRoleToDataflow.push(rol);
  });

  return userRoleToDataflow;
};

const all = async userData => {
  const pendingDataflowsDTO = await apiDataflow.all(userData);
  const dataflows = !userData ? pendingDataflowsDTO : [];
  const userRoles = [];

  if (userData) {
    const dataflowsRoles = userData.filter(role => role.includes(config.permissions['DATAFLOW']));
    dataflowsRoles.map((item, i) => {
      const role = TextUtils.reduceString(item, `${item.replace(/\D/g, '')}-`);
      return (userRoles[i] = { id: parseInt(item.replace(/\D/g, '')), userRole: config.permissions[role] });
    });

    for (let i = 0; i < pendingDataflowsDTO.length; i++) {
      const isDuplicated = CoreUtils.isDuplicatedInObject(userRoles, 'id');
      if (pendingDataflowsDTO[i].status === DataflowConf.dataflowStatus['OPEN'] && pendingDataflowsDTO[i].releasable) {
        pendingDataflowsDTO[i].status = 'OPEN';
      } else if (
        pendingDataflowsDTO[i].status === DataflowConf.dataflowStatus['OPEN'] &&
        !pendingDataflowsDTO[i].releasable
      ) {
        pendingDataflowsDTO[i].status = 'CLOSED';
      }
      dataflows.push({
        ...pendingDataflowsDTO[i],
        ...(isDuplicated ? getUserRoles(userRoles) : userRoles).find(item => item.id === pendingDataflowsDTO[i].id)
      });
    }
  }

  const groupByUserRequestStatus = CoreUtils.onGroupBy('userRequestStatus');

  const dataflowsData = groupByUserRequestStatus(dataflows);

  const allDataflows = cloneDeep(DataflowConf.userRequestStatus);
  Object.keys(dataflowsData).forEach(key => {
    allDataflows[key.toLowerCase()] = parseDataflowDTOs(dataflowsData[key]);
  });
  return allDataflows;
};

const create = async (name, description, obligationId) => await apiDataflow.create(name, description, obligationId);

const cloneDatasetSchemas = async (sourceDataflowId, targetDataflowId) =>
  await apiDataflow.cloneDatasetSchemas(sourceDataflowId, targetDataflowId);

const completed = async () => {
  const completedDataflowsDTO = await apiDataflow.completed();
  return parseDataflowDTOs(completedDataflowsDTO);
};

const datasetsValidationStatistics = async datasetSchemaId => {
  const datasetsDashboardsDataDTO = await apiDataflow.datasetsValidationStatistics(datasetSchemaId);
  datasetsDashboardsDataDTO.sort((a, b) => {
    let datasetName_A = a.nameDataSetSchema;
    let datasetName_B = b.nameDataSetSchema;
    return datasetName_A < datasetName_B ? -1 : datasetName_A > datasetName_B ? 1 : 0;
  });

  const datasetsDashboardsData = {};
  datasetsDashboardsData.datasetId = datasetsDashboardsDataDTO.idDataSetSchema;

  const datasetReporters = [];
  const tables = [];
  let tablePercentages = [];
  let tableValues = [];
  let levelErrors = [];
  const allDatasetLevelErrors = [];
  datasetsDashboardsDataDTO.forEach(dataset => {
    datasetsDashboardsData.datasetId = dataset.idDataSetSchema;
    datasetReporters.push({
      reporterName: dataset.nameDataSetSchema
    });
    allDatasetLevelErrors.push(CoreUtils.getDashboardLevelErrorByTable(dataset));
    dataset.tables.forEach((table, i) => {
      let index = tables.map(t => t.tableId).indexOf(table.idTableSchema);
      //Check if table has been already added
      if (index === -1) {
        tablePercentages.push(
          [
            getPercentageOfValue(
              table.totalRecords -
                (table.totalRecordsWithBlockers +
                  table.totalRecordsWithErrors +
                  table.totalRecordsWithWarnings +
                  table.totalRecordsWithInfos),
              table.totalRecords
            )
          ],
          [getPercentageOfValue(table.totalRecordsWithInfos, table.totalRecords)],
          [getPercentageOfValue(table.totalRecordsWithWarnings, table.totalRecords)],
          [getPercentageOfValue(table.totalRecordsWithErrors, table.totalRecords)],
          [getPercentageOfValue(table.totalRecordsWithBlockers, table.totalRecords)]
        );
        tableValues.push(
          [
            table.totalRecords -
              (table.totalRecordsWithBlockers +
                table.totalRecordsWithErrors +
                table.totalRecordsWithWarnings +
                table.totalRecordsWithInfos)
          ],
          [table.totalRecordsWithInfos],
          [table.totalRecordsWithWarnings],
          [table.totalRecordsWithErrors],
          [table.totalRecordsWithBlockers]
        );

        tables.push({
          tableId: table.idTableSchema,
          tableName: table.nameTableSchema,
          tableStatisticPercentages: tablePercentages,
          tableStatisticValues: tableValues
        });
        tablePercentages = [];
        tableValues = [];
      } else {
        const tableById = tables.filter(tab => tab.tableId === table.idTableSchema)[0];

        tableById.tableStatisticPercentages[0].push(
          getPercentageOfValue(
            table.totalRecords -
              (table.totalRecordsWithBlockers +
                table.totalRecordsWithErrors +
                table.totalRecordsWithWarnings +
                table.totalRecordsWithInfos),
            table.totalRecords
          )
        );

        tableById.tableStatisticPercentages[1].push(
          getPercentageOfValue(table.totalRecordsWithInfos, table.totalRecords)
        );

        tableById.tableStatisticPercentages[2].push(
          getPercentageOfValue(table.totalRecordsWithWarnings, table.totalRecords)
        );

        tableById.tableStatisticPercentages[3].push(
          getPercentageOfValue(table.totalRecordsWithErrors, table.totalRecords)
        );

        tableById.tableStatisticPercentages[4].push(
          getPercentageOfValue(table.totalRecordsWithBlockers, table.totalRecords)
        );

        tableById.tableStatisticPercentages = tableById.tableStatisticPercentages;
        tableById.tableStatisticValues[0].push(
          table.totalRecords -
            (table.totalRecordsWithBlockers +
              table.totalRecordsWithErrors +
              table.totalRecordsWithWarnings +
              table.totalRecordsWithInfos)
        );
        tableById.tableStatisticValues[1].push(table.totalRecordsWithInfos);
        tableById.tableStatisticValues[2].push(table.totalRecordsWithWarnings);
        tableById.tableStatisticValues[3].push(table.totalRecordsWithErrors);
        tableById.tableStatisticValues[4].push(table.totalRecordsWithBlockers);
        tables[index] = tableById;
      }
    });
  });
  levelErrors = [...new Set(CoreUtils.orderLevelErrors(allDatasetLevelErrors.flat()))];

  datasetsDashboardsData.datasetReporters = datasetReporters;
  datasetsDashboardsData.levelErrors = levelErrors;
  datasetsDashboardsData.tables = tables;
  return datasetsDashboardsData;
};

const datasetsFinalFeedback = async dataflowId => {
  const datasetsFinalFeedbackDTO = await apiDataflow.datasetsFinalFeedback(dataflowId);
  const datasetsFeedback = datasetsFinalFeedbackDTO.map(dataset => {
    return {
      dataProviderName: dataset.dataSetName,
      datasetName: dataset.nameDatasetSchema,
      datasetId: dataset.id,
      isReleased: dataset.isReleased,
      feedbackStatus: !isNil(dataset.status) && capitalize(dataset.status.split('_').join(' '))
    };
  });

  return datasetsFeedback;
};

const datasetsReleasedStatus = async dataflowId => {
  const datasetsReleasedStatusDTO = await apiDataflow.datasetsReleasedStatus(dataflowId);
  datasetsReleasedStatusDTO.sort((a, b) => {
    let datasetName_A = a.dataSetName;
    let datasetName_B = b.dataSetName;
    return datasetName_A < datasetName_B ? -1 : datasetName_A > datasetName_B ? 1 : 0;
  });

  const reporters = [];
  datasetsReleasedStatusDTO.map(dataset => {
    reporters.push(dataset.dataSetName);
  });

  const groupByReporter = CoreUtils.onGroupBy('dataSetName');

  const isReleased = new Array(Object.values(groupByReporter(datasetsReleasedStatusDTO)).length).fill(0);
  const isNotReleased = [...isReleased];

  Object.values(groupByReporter(datasetsReleasedStatusDTO)).forEach((reporter, i) => {
    reporter.forEach(dataset => {
      dataset.isReleased ? (isReleased[i] += 1) : (isNotReleased[i] += 1);
    });
  });

  const releasedStatusData = {
    releasedData: isReleased,
    unReleasedData: isNotReleased,
    labels: Array.from(new Set(reporters))
  };

  return releasedStatusData;
};

const dataflowDetails = async dataflowId => {
  const dataflowDetailsDTO = await apiDataflow.dataflowDetails(dataflowId);
  const dataflowDetails = parseDataflowDTO(dataflowDetailsDTO);
  return dataflowDetails;
};

const deleteById = async dataflowId => {
  return await apiDataflow.deleteById(dataflowId);
};

const downloadById = async dataflowId => {
  return await apiDataflow.downloadById(dataflowId);
};

const getAllSchemas = async dataflowId => {
  const datasetSchemasDTO = await apiDataflow.allSchemas(dataflowId);
  const datasetSchemas = datasetSchemasDTO.map(datasetSchemaDTO => {
    const dataset = new Dataset({
      datasetSchemaDescription: datasetSchemaDTO.description,
      datasetSchemaId: datasetSchemaDTO.idDataSetSchema,
      datasetSchemaName: datasetSchemaDTO.nameDatasetSchema
      // levelErrorTypes: !isUndefined(rulesDTO) && rulesDTO !== '' ? getAllLevelErrorsFromRuleValidations(rulesDTO) : []
    });

    const tables = datasetSchemaDTO.tableSchemas.map(datasetTableDTO => {
      const records = !isNull(datasetTableDTO.recordSchema)
        ? [datasetTableDTO.recordSchema].map(dataTableRecordDTO => {
            const fields = !isNull(dataTableRecordDTO.fieldSchema)
              ? dataTableRecordDTO.fieldSchema.map(DataTableFieldDTO => {
                  return new DatasetTableField({
                    codelistItems: DataTableFieldDTO.codelistItems,
                    description: DataTableFieldDTO.description,
                    fieldId: DataTableFieldDTO.id,
                    pk: !isNull(DataTableFieldDTO.pk) ? DataTableFieldDTO.pk : false,
                    pkHasMultipleValues: !isNull(DataTableFieldDTO.pkHasMultipleValues)
                      ? DataTableFieldDTO.pkHasMultipleValues
                      : false,
                    pkMustBeUsed: !isNull(DataTableFieldDTO.pkMustBeUsed) ? DataTableFieldDTO.pkMustBeUsed : false,
                    pkReferenced: !isNull(DataTableFieldDTO.pkReferenced) ? DataTableFieldDTO.pkReferenced : false,
                    name: DataTableFieldDTO.name,
                    readOnly: DataTableFieldDTO.readOnly,
                    recordId: DataTableFieldDTO.idRecord,
                    referencedField: DataTableFieldDTO.referencedField,
                    required: DataTableFieldDTO.required,
                    type: DataTableFieldDTO.type
                  });
                })
              : null;
            return new DatasetTableRecord({
              datasetPartitionId: dataTableRecordDTO.id,
              fields,
              recordSchemaId: dataTableRecordDTO.idRecordSchema
            });
          })
        : null;
      return new DatasetTable({
        hasPKReferenced: !isEmpty(
          records.filter(record => record.fields.filter(field => field.pkReferenced === true)[0])
        ),
        tableSchemaToPrefill: datasetTableDTO.toPrefill,
        tableSchemaId: datasetTableDTO.idTableSchema,
        tableSchemaDescription: datasetTableDTO.description,
        tableSchemaFixedNumber: datasetTableDTO.fixedNumber,
        tableSchemaName: datasetTableDTO.nameTableSchema,
        tableSchemaNotEmpty: datasetTableDTO.notEmpty,
        tableSchemaReadOnly: datasetTableDTO.readOnly,
        records: records,
        recordSchemaId: !isNull(datasetTableDTO.recordSchema) ? datasetTableDTO.recordSchema.idRecordSchema : null
      });
    });

    dataset.tables = tables;
    return dataset;
  });

  datasetSchemas.sort((a, b) => {
    const textA = a.datasetSchemaName.toUpperCase();
    const textB = b.datasetSchemaName.toUpperCase();
    return textA < textB ? -1 : textA > textB ? 1 : 0;
  });
  return datasetSchemas;
};

const getApiKey = async (dataflowId, dataProviderId, isCustodian) =>
  await apiDataflow.getApiKey(dataflowId, dataProviderId, isCustodian);

const generateApiKey = async (dataflowId, dataProviderId, isCustodian) =>
  await apiDataflow.generateApiKey(dataflowId, dataProviderId, isCustodian);

const getPercentageOfValue = (val, total) => {
  return total === 0 ? '0.00' : ((val / total) * 100).toFixed(2);
};

const newEmptyDatasetSchema = async (dataflowId, datasetSchemaName) => {
  const newEmptyDatasetSchemaResponse = await apiDataflow.newEmptyDatasetSchema(dataflowId, datasetSchemaName);
  return newEmptyDatasetSchemaResponse;
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

const parseDataflowDTO = dataflowDTO =>
  new Dataflow({
    creationDate: dataflowDTO.creationDate,
    dataCollections: parseDataCollectionListDTO(dataflowDTO.dataCollections),
    datasets: parseDatasetListDTO(dataflowDTO.reportingDatasets),
    description: dataflowDTO.description,
    designDatasets: parseDatasetListDTO(dataflowDTO.designDatasets),
    documents: parseDocumentListDTO(dataflowDTO.documents),
    euDatasets: parseEuDatasetListDTO(dataflowDTO.euDatasets),
    expirationDate: dataflowDTO.deadlineDate > 0 ? dayjs(dataflowDTO.deadlineDate * 1000).format('YYYY-MM-DD') : '-',
    id: dataflowDTO.id,
    isReleasable: dataflowDTO.releasable,
    manualAcceptance: dataflowDTO.manualAcceptance,
    name: dataflowDTO.name,
    obligation: parseObligationDTO(dataflowDTO.obligation),
    reportingDatasetsStatus: dataflowDTO.reportingStatus,
    representatives: parseRepresentativeListDTO(dataflowDTO.representatives),
    requestId: dataflowDTO.requestId,
    status: dataflowDTO.status,
    userRequestStatus: dataflowDTO.userRequestStatus,
    userRole: dataflowDTO.userRole,
    weblinks: parseWebLinkListDTO(dataflowDTO.weblinks)
  });
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
    datasetId: datasetDTO.id,
    datasetSchemaId: datasetDTO.datasetSchema,
    datasetSchemaName: datasetDTO.dataSetName,
    isReleased: datasetDTO.isReleased,
    isReleasing: datasetDTO.releasing,
    name: datasetDTO.nameDatasetSchema,
    dataProviderId: datasetDTO.dataProviderId
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
    id: representativeDTO.id,
    isReceiptDownloaded: representativeDTO.receiptDownloaded,
    isReceiptOutdated: representativeDTO.receiptOutdated,
    providerAccount: representativeDTO.providerAccount,
    hasDatasets: representativeDTO.hasDatasets
  });
};

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

const pending = async () => {
  const pendingDataflowsDTO = await apiDataflow.pending();
  return parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'PENDING'));
};

const publicData = async () => {
  const publicDataflows = await apiDataflow.publicData();
  return publicDataflows;
};

const reject = async dataflowId => {
  const status = await apiDataflow.reject(dataflowId);
  return status;
};

const reporting = async dataflowId => {
  const reportingDataflowDTO = await apiDataflow.reporting(dataflowId);
  const dataflow = parseDataflowDTO(reportingDataflowDTO);
  dataflow.datasets.sort((a, b) => {
    let datasetName_A = a.datasetSchemaName;
    let datasetName_B = b.datasetSchemaName;
    return datasetName_A < datasetName_B ? -1 : datasetName_A > datasetName_B ? 1 : 0;
  });
  return dataflow;
};

const schemasValidation = async dataflowId => {
  return await apiDataflow.schemasValidation(dataflowId);
};

const update = async (dataflowId, name, description, obligationId, isReleasable) =>
  await apiDataflow.update(dataflowId, name, description, obligationId, isReleasable);

export const ApiDataflowRepository = {
  accept,
  accepted,
  all,
  cloneDatasetSchemas,
  completed,
  create,
  dataflowDetails,
  datasetsFinalFeedback,
  datasetsReleasedStatus,
  datasetsValidationStatistics,
  deleteById,
  downloadById,
  generateApiKey,
  getAllSchemas,
  getApiKey,
  newEmptyDatasetSchema,
  pending,
  publicData,
  reject,
  reporting,
  schemasValidation,
  update
};
