import dayjs from 'dayjs';
import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import orderBy from 'lodash/orderBy';
import sortBy from 'lodash/sortBy';

import utc from 'dayjs/plugin/utc';

import { DataflowRepository } from 'repositories/DataflowRepository';

import { DataflowUtils } from 'services/_utils/DataflowUtils';
import { DatasetUtils } from 'services/_utils/DatasetUtils';

import { Dataset } from 'entities/Dataset';
import { DatasetTable } from 'entities/DatasetTable';
import { DatasetTableField } from 'entities/DatasetTableField';
import { DatasetTableRecord } from 'entities/DatasetTableRecord';

import { CoreUtils } from 'repositories/_utils/CoreUtils';
import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';
import { ServiceUtils } from 'services/_utils/ServiceUtils';

export const DataflowService = {
  countByType: async () => {
    const dataflowsCountDTO = await DataflowRepository.countByType();
    return DataflowUtils.parseDataflowCount(dataflowsCountDTO.data);
  },

  getAll: async ({ accessRoles, contextRoles, filterBy, numberRows, pageNum, sortBy }) => {
    const { isAsc, sortByHeader } = DataflowUtils.parseRequestSortBy(sortBy);
    const filteredFilterBy = DataflowUtils.parseRequestFilterBy(filterBy);

    const dataflowsDTO = await DataflowRepository.getAll({
      filterBy: filteredFilterBy,
      isAsc,
      numberRows,
      pageNum,
      sortBy: sortByHeader
    });

    const dataflows = dataflowsDTO.data.dataflows.map(dataflowDTO => {
      dataflowDTO.userRole = UserRoleUtils.getUserRoleByDataflow(dataflowDTO.id, accessRoles, contextRoles);
      return dataflowDTO;
    });

    return { ...dataflowsDTO.data, dataflows: DataflowUtils.parseDataflowListDTO(dataflows) };
  },

  getCloneableDataflows: async () => {
    dayjs.extend(utc);

    const dataflowsDTO = await DataflowRepository.getCloneableDataflows();
    return dataflowsDTO.data.map(dataflow => {
      dataflow.expirationDate =
        dataflow.deadlineDate > 0 ? dayjs(dataflow.deadlineDate).utc().format('YYYY-MM-DD') : '-';
      dataflow.obligationTitle = dataflow.obligation?.oblTitle;
      dataflow.legalInstrument = dataflow.obligation?.legalInstrument?.sourceAlias;
      return dataflow;
    });
  },

  create: async (name, description, obligationId, type, bigData) =>
    await DataflowRepository.create(name, description, obligationId, type, bigData),

  cloneSchemas: async (sourceDataflowId, targetDataflowId) =>
    await DataflowRepository.cloneSchemas(sourceDataflowId, targetDataflowId),

  downloadAllSchemasInfo: async (dataflowId, fileName) =>
    await DataflowRepository.downloadAllSchemasInfo(dataflowId, fileName),

  downloadPublicAllSchemasInfoFile: async dataflowId =>
    await DataflowRepository.downloadPublicAllSchemasInfoFile(dataflowId),

  downloadUsersListFile: async (dataflowId, fileName) =>
    await DataflowRepository.downloadUsersListFile(dataflowId, fileName),

  generateAllSchemasInfoFile: async dataflowId => await DataflowRepository.generateAllSchemasInfoFile(dataflowId),

  generateUsersByCountryFile: async (dataflowId, dataProviderId) =>
    await DataflowRepository.generateUsersByCountryFile(dataflowId, dataProviderId),

  getDatasetsValidationStatistics: async (dataflowId, datasetSchemaId) => {
    const datasetsDashboardsDataDTO = await DataflowRepository.getDatasetsValidationStatistics(
      dataflowId,
      datasetSchemaId
    );

    datasetsDashboardsDataDTO.data.sort((a, b) => {
      const datasetName_A = a.nameDataSetSchema;
      const datasetName_B = b.nameDataSetSchema;
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
              CoreUtils.getPercentageOfValue(
                table.totalRecords -
                  (table.totalRecordsWithBlockers +
                    table.totalRecordsWithErrors +
                    table.totalRecordsWithWarnings +
                    table.totalRecordsWithInfos),
                table.totalRecords
              )
            ],
            [CoreUtils.getPercentageOfValue(table.totalRecordsWithInfos, table.totalRecords)],
            [CoreUtils.getPercentageOfValue(table.totalRecordsWithWarnings, table.totalRecords)],
            [CoreUtils.getPercentageOfValue(table.totalRecordsWithErrors, table.totalRecords)],
            [CoreUtils.getPercentageOfValue(table.totalRecordsWithBlockers, table.totalRecords)]
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
            CoreUtils.getPercentageOfValue(
              table.totalRecords -
                (table.totalRecordsWithBlockers +
                  table.totalRecordsWithErrors +
                  table.totalRecordsWithWarnings +
                  table.totalRecordsWithInfos),
              table.totalRecords
            )
          );

          tableById.tableStatisticPercentages[1].push(
            CoreUtils.getPercentageOfValue(table.totalRecordsWithInfos, table.totalRecords)
          );

          tableById.tableStatisticPercentages[2].push(
            CoreUtils.getPercentageOfValue(table.totalRecordsWithWarnings, table.totalRecords)
          );

          tableById.tableStatisticPercentages[3].push(
            CoreUtils.getPercentageOfValue(table.totalRecordsWithErrors, table.totalRecords)
          );

          tableById.tableStatisticPercentages[4].push(
            CoreUtils.getPercentageOfValue(table.totalRecordsWithBlockers, table.totalRecords)
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
    const datasetsFinalFeedbackDTO = await DataflowRepository.getDatasetsFinalFeedbackAndReleasedStatus(dataflowId);
    return datasetsFinalFeedbackDTO.data.map(dataset => ({
      dataProviderName: dataset.dataSetName,
      datasetName: dataset.nameDatasetSchema,
      datasetId: dataset.id,
      isReleased: dataset.isReleased ?? false,
      feedbackStatus: !isNil(dataset.status) && capitalize(dataset.status.split('_').join(' '))
    }));
  },

  getDatasetsReleasedStatus: async dataflowId => {
    const datasetsReleasedStatusDTO = await DataflowRepository.getDatasetsFinalFeedbackAndReleasedStatus(dataflowId);
    datasetsReleasedStatusDTO.data.sort((a, b) => {
      const datasetName_A = a.dataSetName;
      const datasetName_B = b.dataSetName;
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

  getDetails: async dataflowId => {
    const dataflowDetails = await DataflowRepository.getDetails(dataflowId);
    return DataflowUtils.parseDataflowDTO(dataflowDetails.data);
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
                ? dataTableRecordDTO.fieldSchema.map((DataTableFieldDTO, index) => {
                    const datasetTable = new DatasetTableField({
                      codelistItems: DataTableFieldDTO.codelistItems,
                      description: DataTableFieldDTO.description,
                      fieldId: DataTableFieldDTO.id,
                      pk: !isNull(DataTableFieldDTO.pk) ? DataTableFieldDTO.pk : false,
                      pkHasMultipleValues: !isNull(DataTableFieldDTO.pkHasMultipleValues)
                        ? DataTableFieldDTO.pkHasMultipleValues
                        : false,
                      ignoreCaseInLinks: !isNull(DataTableFieldDTO.ignoreCaseInLinks)
                        ? DataTableFieldDTO.ignoreCaseInLinks
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
                    return datasetTable;
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
          dataAreManuallyEditable: datasetTableDTO.dataAreManuallyEditable,
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

  getPublicDataflowsByCountryCode: async ({ countryCode, sortOrder, pageNum, numberRows, sortField, filterBy }) => {
    const parsedFilterBy = DataflowUtils.parseRequestPublicCountryFilterBy(filterBy);
    const parsedSortField = DataflowUtils.parseRequestPublicCountrySortField(sortField);

    const publicDataflowsByCountryCodeResponse = await DataflowRepository.getPublicDataflowsByCountryCode({
      countryCode,
      sortOrder: ServiceUtils.getSortOrder(sortOrder),
      pageNum,
      numberRows,
      sortField: parsedSortField,
      filterBy: parsedFilterBy
    });

    publicDataflowsByCountryCodeResponse.data.dataflows = DataflowUtils.parsePublicDataflowListDTO(
      publicDataflowsByCountryCodeResponse.data.dataflows
    );

    return publicDataflowsByCountryCodeResponse.data;
  },

  getPublicDataflowData: async dataflowId => {
    const publicDataflowDataDTO = await DataflowRepository.getPublicDataflowData(dataflowId);
    const publicDataflowData = DataflowUtils.parsePublicDataflowDTO(publicDataflowDataDTO.data);
    publicDataflowData.datasets = orderBy(publicDataflowData.datasets, 'datasetSchemaName');
    return publicDataflowData;
  },

  createApiKey: async (dataflowId, dataProviderId, isCustodian) =>
    await DataflowRepository.createApiKey(dataflowId, dataProviderId, isCustodian),

  getAllDataflowsUserList: async () => {
    const usersListDTO = await DataflowRepository.getAllDataflowsUserList();
    const usersList = DataflowUtils.parseAllDataflowsUserList(usersListDTO.data);
    return sortBy(usersList, ['dataflowName', 'role']);
  },

  getRepresentativesUsersList: async dataflowId => {
    const response = await DataflowRepository.getRepresentativesUsersList(dataflowId);
    const usersList = DataflowUtils.parseDataProvidersUserList(response.data);
    return sortBy(usersList, 'dataProviderName');
  },

  getUserList: async (dataflowId, representativeId) => {
    const response = await DataflowRepository.getUserList(dataflowId, representativeId);
    const usersList = DataflowUtils.parseUsersList(response.data);
    return sortBy(usersList, 'role');
  },

  createEmptyDatasetSchema: async (dataflowId, datasetSchemaName) =>
    await DataflowRepository.createEmptyDatasetSchema(dataflowId, datasetSchemaName),

  getPublicData: async ({ filterBy, numberRows, pageNum, sortByOptions }) => {
    const { isAsc, sortByHeader } = DataflowUtils.parseRequestSortBy(sortByOptions);
    const filteredFilterBy = DataflowUtils.parseRequestFilterBy(filterBy);

    const publicDataflows = await DataflowRepository.getPublicData({
      filterBy: filteredFilterBy,
      isAsc,
      numberRows,
      pageNum,
      sortByHeader
    });
    const parsedPublicDataflows = DataflowUtils.parsePublicDataflowListDTO(publicDataflows.data.dataflows);

    return { ...publicDataflows.data, dataflows: parsedPublicDataflows };
  },

  get: async dataflowId => {
    const reportingDataflowDTO = await DataflowRepository.get(dataflowId);
    const dataflow = DataflowUtils.parseDataflowDTO(reportingDataflowDTO.data);
    dataflow.testDatasets.sort(DatasetUtils.sortDatasetTypeByName);
    dataflow.datasets.sort(DatasetUtils.sortDatasetTypeByName);
    dataflow.designDatasets.sort(DatasetUtils.sortDatasetTypeByName);
    dataflow.referenceDatasets.sort(DatasetUtils.sortDatasetTypeByName);
    return dataflow;
  },

  getIcebergTables: async ({ dataflowId, providerId }) => {
    const icebergTables = await DataflowRepository.getIcebergTables({ dataflowId, providerId });

    return icebergTables;
  },

  getSchemasValidation: async dataflowId => await DataflowRepository.getSchemasValidation(dataflowId),

  update: async (dataflowId, name, description, obligationId, isReleasable, showPublicInfo, bigData) =>
    await DataflowRepository.update(dataflowId, name, description, obligationId, isReleasable, showPublicInfo, bigData),

  updateAutomaticDelete: async (dataflowId, isAutomaticReportingDeletion) =>
    await DataflowRepository.updateAutomaticDelete(dataflowId, isAutomaticReportingDeletion),

  getDatasetsInfo: async dataflowId => {
    const datasetsInfoDTO = await DataflowRepository.getDatasetsInfo(dataflowId);
    const parsedDatasetsInfoDTO = DataflowUtils.parseDatasetsInfoDTO(datasetsInfoDTO.data);
    return sortBy(parsedDatasetsInfoDTO, 'id');
  },

  validateAllDataflowsUsers: async () => await DataflowRepository.validateAllDataflowsUsers()
};
