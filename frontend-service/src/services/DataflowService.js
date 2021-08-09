import dayjs from 'dayjs';
import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import orderBy from 'lodash/orderBy';
import sortBy from 'lodash/sortBy';

import { config } from 'conf';

import { DataflowRepository } from 'repositories/DataflowRepository';

import { DataCollection } from 'entities/DataCollection';
import { Dataflow } from 'entities/Dataflow';
import { Dataset } from 'entities/Dataset';
import { DatasetTable } from 'entities/DatasetTable';
import { DatasetTableField } from 'entities/DatasetTableField';
import { DatasetTableRecord } from 'entities/DatasetTableRecord';
import { EUDataset } from 'entities/EUDataset';
import { LegalInstrument } from 'entities/LegalInstrument';
import { Obligation } from 'entities/Obligation';
import { Representative } from 'entities/Representative';
import { WebLink } from 'entities/WebLink';

import { CoreUtils } from 'repositories/_utils/CoreUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';
import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

const sortDatasetTypeByName = (a, b) => {
  let datasetName_A = a.datasetSchemaName;
  let datasetName_B = b.datasetSchemaName;
  return datasetName_A < datasetName_B ? -1 : datasetName_A > datasetName_B ? 1 : 0;
};

const getPercentageOfValue = (val, total) => (total === 0 ? '0.00' : ((val / total) * 100).toFixed(2));

const parseDataflowListDTO = dataflowsDTO => {
  if (!isNull(dataflowsDTO) && !isUndefined(dataflowsDTO)) {
    const dataflows = [];
    dataflowsDTO.forEach(dataflowDTO => {
      dataflows.push(parseDataflowDTO(dataflowDTO));
    });
    return dataflows;
  }
  return;
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
    anySchemaAvailableInPublic: dataflowDTO.anySchemaAvailableInPublic,
    creationDate: dataflowDTO.creationDate,
    dataCollections: parseDataCollectionListDTO(dataflowDTO.dataCollections),
    datasets: parseDatasetListDTO(dataflowDTO.reportingDatasets),
    description: dataflowDTO.description,
    designDatasets: parseDatasetListDTO(dataflowDTO.designDatasets),
    documents: parseDocumentListDTO(dataflowDTO.documents),
    euDatasets: parseEUDatasetListDTO(dataflowDTO.euDatasets),
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
    showPublicInfo: dataflowDTO.showPublicInfo,
    status: dataflowDTO.status,
    testDatasets: parseDatasetListDTO(dataflowDTO.testDatasets),
    type: dataflowDTO.type,
    userRole: dataflowDTO.userRole,
    webLinks: parseWebLinkListDTO(dataflowDTO.weblinks),
    fmeUserId: dataflowDTO.fmeUserId,
    dataProviderGroupId: dataflowDTO.dataProviderGroupId
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

const parseEUDatasetListDTO = euDatasetsDTO => {
  if (!isNull(euDatasetsDTO) && !isUndefined(euDatasetsDTO)) {
    const euDatasets = [];
    euDatasetsDTO.forEach(euDatasetDTO => {
      euDatasets.push(parseEUDatasetDTO(euDatasetDTO));
    });
    return euDatasets;
  }
  return;
};

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

const parseAllDataflowsUserList = allDataflowsUserListDTO => {
  allDataflowsUserListDTO.forEach((dataflow, dataflowIndex) => {
    dataflow.users.forEach((user, usersIndex) => {
      user.roles.forEach((role, roleIndex) => {
        allDataflowsUserListDTO[dataflowIndex].users[usersIndex].roles[roleIndex] =
          UserRoleUtils.getUserRoleLabel(role);
      });
    });
  });
  const usersList = [];
  allDataflowsUserListDTO.forEach(dataflow => {
    const { dataflowId, dataflowName } = dataflow;
    dataflow.users.forEach(parsedUser => {
      const { email, roles } = parsedUser;
      roles.forEach(role => {
        usersList.push({ dataflowId, dataflowName, email, role });
      });
    });
  });
  return usersList;
};

const parseCountriesUserList = usersListDTO => {
  usersListDTO.forEach((user, usersIndex) => {
    user.roles.forEach((role, roleIndex) => {
      usersListDTO[usersIndex].roles[roleIndex] = UserRoleUtils.getUserRoleLabel(role);
    });
  });
  const usersList = [];
  usersListDTO.forEach(parsedUser => {
    const { country, email, roles } = parsedUser;
    roles.forEach(role => {
      usersList.push({ country, email, role });
    });
  });
  usersList.forEach(user => {
    if (isNil(user.country)) {
      user.country = '';
    }
  });
  return usersList;
};

const parseUsersList = usersListDTO => {
  usersListDTO.forEach((user, usersIndex) => {
    user.roles.forEach((role, roleIndex) => {
      usersListDTO[usersIndex].roles[roleIndex] = UserRoleUtils.getUserRoleLabel(role);
    });
  });
  const usersList = [];
  usersListDTO.forEach(parsedUser => {
    const { email, roles } = parsedUser;
    roles.forEach(role => {
      usersList.push({ email, role });
    });
  });
  return usersList;
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

export const DataflowService = {
  getAll: async userData => {
    const dataflowsDTO = await DataflowRepository.getAll();
    const dataflows = !userData ? dataflowsDTO.data : [];

    if (userData) {
      const userRoles = [];
      const dataflowsRoles = userData.filter(role => role.includes(config.permissions.prefixes.DATAFLOW));
      dataflowsRoles.map((item, i) => {
        const role = TextUtils.reduceString(item, `${item.replace(/\D/g, '')}-`);

        return (userRoles[i] = {
          id: parseInt(item.replace(/\D/g, '')),
          userRole: UserRoleUtils.getUserRoleLabel(role)
        });
      });

      for (let index = 0; index < dataflowsDTO.data.length; index++) {
        const dataflow = dataflowsDTO.data[index];
        const isDuplicated = CoreUtils.isDuplicatedInObject(userRoles, 'id');
        const isOpen = dataflow.status === config.dataflowStatus.OPEN;

        if (isOpen) {
          dataflow.releasable ? (dataflow.status = 'OPEN') : (dataflow.status = 'CLOSED');
        }

        dataflows.push({
          ...dataflow,
          ...(isDuplicated ? UserRoleUtils.getUserRoles(userRoles) : userRoles).find(item => item.id === dataflow.id)
        });
      }
    }

    return parseDataflowDTOs(dataflows);
  },

  create: async (name, description, obligationId, type) =>
    await DataflowRepository.create(name, description, obligationId, type),

  cloneSchemas: async (sourceDataflowId, targetDataflowId) =>
    await DataflowRepository.cloneSchemas(sourceDataflowId, targetDataflowId),

  getDatasetsValidationStatistics: async (dataflowId, datasetSchemaId) => {
    const datasetsDashboardsDataDTO = await DataflowRepository.getDatasetsValidationStatistics(
      dataflowId,
      datasetSchemaId
    );

    datasetsDashboardsDataDTO.data.sort((a, b) => {
      let datasetName_A = a.nameDataSetSchema;
      let datasetName_B = b.nameDataSetSchema;
      return datasetName_A < datasetName_B ? -1 : datasetName_A > datasetName_B ? 1 : 0;
    });

    const datasetsDashboardsData = {};
    datasetsDashboardsData.datasetId = datasetsDashboardsDataDTO.data.idDataSetSchema;

    const datasetReporters = [];
    const tables = [];
    let tablePercentages = [];
    let tableValues = [];
    let levelErrors = [];
    const allDatasetLevelErrors = [];
    datasetsDashboardsDataDTO.data.forEach(dataset => {
      datasetsDashboardsData.datasetId = dataset.idDataSetSchema;
      datasetReporters.push({ reporterName: dataset.nameDataSetSchema });
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
  },

  getDatasetsFinalFeedback: async dataflowId => {
    const datasetsFinalFeedbackDTO = await DataflowRepository.getDatasetsFinalFeedback(dataflowId);
    return datasetsFinalFeedbackDTO.data.map(dataset => {
      return {
        dataProviderName: dataset.dataSetName,
        datasetName: dataset.nameDatasetSchema,
        datasetId: dataset.id,
        isReleased: dataset.isReleased ?? false,
        feedbackStatus: !isNil(dataset.status) && capitalize(dataset.status.split('_').join(' '))
      };
    });
  },

  getDatasetsReleasedStatus: async dataflowId => {
    const datasetsReleasedStatusDTO = await DataflowRepository.getDatasetsReleasedStatus(dataflowId);
    datasetsReleasedStatusDTO.data.sort((a, b) => {
      let datasetName_A = a.dataSetName;
      let datasetName_B = b.dataSetName;
      return datasetName_A < datasetName_B ? -1 : datasetName_A > datasetName_B ? 1 : 0;
    });

    const reporters = datasetsReleasedStatusDTO.data.map(dataset => dataset.dataSetName);

    const groupByReporter = CoreUtils.onGroupBy('dataSetName');

    const isReleased = new Array(Object.values(groupByReporter(datasetsReleasedStatusDTO.data)).length).fill(0);
    const isNotReleased = [...isReleased];

    Object.values(groupByReporter(datasetsReleasedStatusDTO.data)).forEach((reporter, i) => {
      reporter.forEach(dataset => {
        dataset.isReleased ? (isReleased[i] += 1) : (isNotReleased[i] += 1);
      });
    });

    return {
      labels: Array.from(new Set(reporters)),
      releasedData: isReleased,
      unReleasedData: isNotReleased
    };
  },

  getDataflowDetails: async dataflowId => {
    const dataflowDetails = await DataflowRepository.getDataflowDetails(dataflowId);
    return parseDataflowDTO(dataflowDetails.data);
  },

  delete: async dataflowId => await DataflowRepository.delete(dataflowId),

  exportSchemas: async dataflowId => await DataflowRepository.exportSchemas(dataflowId),

  getSchemas: async dataflowId => {
    const datasetSchemasDTO = await DataflowRepository.getSchemas(dataflowId);
    const datasetSchemas = datasetSchemasDTO.data.map(datasetSchemaDTO => {
      const dataset = new Dataset({
        datasetSchemaDescription: datasetSchemaDTO.description,
        datasetSchemaId: datasetSchemaDTO.idDataSetSchema,
        datasetSchemaName: datasetSchemaDTO.nameDatasetSchema,
        referenceDataset: datasetSchemaDTO.referenceDataset
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
          records: records,
          recordSchemaId: !isNull(datasetTableDTO.recordSchema) ? datasetTableDTO.recordSchema.idRecordSchema : null,
          tableSchemaDescription: datasetTableDTO.description,
          tableSchemaFixedNumber: datasetTableDTO.fixedNumber,
          tableSchemaId: datasetTableDTO.idTableSchema,
          tableSchemaName: datasetTableDTO.nameTableSchema,
          tableSchemaNotEmpty: datasetTableDTO.notEmpty,
          tableSchemaReadOnly: datasetTableDTO.readOnly,
          tableSchemaToPrefill: datasetTableDTO.toPrefill
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
  },

  getApiKey: async (dataflowId, dataProviderId, isCustodian) =>
    await DataflowRepository.getApiKey(dataflowId, dataProviderId, isCustodian),

  getPublicDataflowsByCountryCode: async (countryCode, sortOrder, pageNum, numberRows, sortField) => {
    const publicDataflowsByCountryCodeResponse = await DataflowRepository.getPublicDataflowsByCountryCode(
      countryCode,
      sortOrder,
      pageNum,
      numberRows,
      sortField
    );

    const publicDataflowsByCountryCodeData = parseDataflowListDTO(
      publicDataflowsByCountryCodeResponse.data.publicDataflows
    );
    publicDataflowsByCountryCodeResponse.data.publicDataflows = publicDataflowsByCountryCodeData;

    return publicDataflowsByCountryCodeResponse.data;
  },

  getPublicDataflowData: async dataflowId => {
    const publicDataflowDataDTO = await DataflowRepository.getPublicDataflowData(dataflowId);
    const publicDataflowData = parseDataflowDTO(publicDataflowDataDTO.data);
    publicDataflowData.datasets = orderBy(publicDataflowData.datasets, 'datasetSchemaName');
    return publicDataflowData;
  },

  createApiKey: async (dataflowId, dataProviderId, isCustodian) =>
    await DataflowRepository.createApiKey(dataflowId, dataProviderId, isCustodian),

  getAllDataflowsUserList: async () => {
    const usersListDTO = await DataflowRepository.getAllDataflowsUserList();
    const usersList = parseAllDataflowsUserList(usersListDTO.data);
    return sortBy(usersList, ['dataflowName', 'role']);
  },

  getRepresentativesUsersList: async dataflowId => {
    const response = await DataflowRepository.getRepresentativesUsersList(dataflowId);
    const usersList = parseCountriesUserList(response.data);
    return sortBy(usersList, 'country');
  },

  getUserList: async (dataflowId, representativeId) => {
    const response = await DataflowRepository.getUserList(dataflowId, representativeId);
    const usersList = parseUsersList(response.data);
    return sortBy(usersList, 'role');
  },

  createEmptyDatasetSchema: async (dataflowId, datasetSchemaName) =>
    await DataflowRepository.createEmptyDatasetSchema(dataflowId, datasetSchemaName),

  getPublicData: async () => {
    const publicDataflows = await DataflowRepository.getPublicData();
    return publicDataflows.data.map(
      publicDataflow =>
        new Dataflow({
          description: publicDataflow.description,
          expirationDate:
            publicDataflow.deadlineDate > 0 ? dayjs(publicDataflow.deadlineDate).format('YYYY-MM-DD') : '-',
          id: publicDataflow.id,
          isReleasable: publicDataflow.releasable,
          name: publicDataflow.name,
          obligation: parseObligationDTO(publicDataflow.obligation),
          status: publicDataflow.status
        })
    );
  },

  getReportingDatasets: async dataflowId => {
    const reportingDataflowDTO = await DataflowRepository.getReportingDatasets(dataflowId);
    const dataflow = parseDataflowDTO(reportingDataflowDTO.data);
    dataflow.testDatasets.sort(sortDatasetTypeByName);
    dataflow.datasets.sort(sortDatasetTypeByName);
    dataflow.designDatasets.sort(sortDatasetTypeByName);
    dataflow.referenceDatasets.sort(sortDatasetTypeByName);
    return dataflow;
  },

  getSchemasValidation: async dataflowId => await DataflowRepository.getSchemasValidation(dataflowId),

  update: async (dataflowId, name, description, obligationId, isReleasable, showPublicInfo) =>
    await DataflowRepository.update(dataflowId, name, description, obligationId, isReleasable, showPublicInfo)
};
