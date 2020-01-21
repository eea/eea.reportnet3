import { isNull, isUndefined } from 'lodash';

import moment from 'moment';

import { apiDataflow } from 'core/infrastructure/api/domain/model/Dataflow';
import { DataCollection } from 'core/domain/model/DataCollection/DataCollection';
import { Dataflow } from 'core/domain/model/Dataflow/Dataflow';
import { Dataset } from 'core/domain/model/Dataset/Dataset';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

import { CoreUtils } from 'core/infrastructure/CoreUtils';

const parseDataflowDTO = dataflowDTO => {
  const dataflow = new Dataflow();
  dataflow.creationDate = dataflowDTO.creationDate;
  dataflow.dataCollections = parseDataCollectionListDTO(dataflowDTO.dataCollections);
  dataflow.datasets = parseDatasetListDTO(dataflowDTO.reportingDatasets);
  dataflow.designDatasets = parseDatasetListDTO(dataflowDTO.designDatasets);
  dataflow.deadlineDate = moment(dataflowDTO.deadlineDate).format('YYYY-MM-DD');
  dataflow.description = dataflowDTO.description;
  dataflow.documents = parseDocumentListDTO(dataflowDTO.documents);
  dataflow.id = dataflowDTO.id;
  dataflow.name = dataflowDTO.name;
  dataflow.status = dataflowDTO.status;
  dataflow.userRequestStatus = dataflowDTO.userRequestStatus;
  dataflow.weblinks = parseWebLinkListDTO(dataflowDTO.weblinks);
  dataflow.requestId = dataflowDTO.requestId;
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
  return new DataCollection(
    dataCollectionDTO.id,
    dataCollectionDTO.dataSetName,
    dataCollectionDTO.idDataflow,
    dataCollectionDTO.datasetSchema,
    dataCollectionDTO.creationDate,
    dataCollectionDTO.dueDate,
    dataCollectionDTO.status
  );
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

const parseDatasetDTO = datasetDTO => {
  return new Dataset(
    null,
    datasetDTO.id,
    datasetDTO.datasetSchema,
    datasetDTO.dataSetName,
    null,
    null,
    null,
    null,
    null,
    null,
    datasetDTO.isReleased,
    null,
    null,
    datasetDTO.nameDatasetSchema
  );
};

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
  return new Document(
    documentDTO.category,
    documentDTO.dataflowId,
    documentDTO.description,
    documentDTO.id,
    documentDTO.language,
    documentDTO.name
  );
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

const parseWebLinkDTO = webLinkDTO => {
  return new WebLink(webLinkDTO.description, webLinkDTO.url);
};

const parseDataflowDTOs = dataflowDTOs => {
  let dataflows = dataflowDTOs.map(dataflowDTO => {
    return parseDataflowDTO(dataflowDTO);
  });

  dataflows.sort((a, b) => {
    let deadline_1 = a.deadlineDate;
    let deadline_2 = b.deadlineDate;
    return deadline_1 < deadline_2 ? -1 : deadline_1 > deadline_2 ? 1 : 0;
  });

  return dataflows;
};

const all = async () => {
  const pendingDataflowsDTO = await apiDataflow.all();
  return {
    pending: parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'PENDING')),
    accepted: parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'ACCEPTED')),
    completed: parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'COMPLETED'))
  };
};

const accepted = async () => {
  const acceptedDataflowsDTO = await apiDataflow.accepted();
  return parseDataflowDTOs(acceptedDataflowsDTO.filter(item => item.userRequestStatus === 'ACCEPTED'));
};

const create = async (name, description) => {
  const createdDataflow = await apiDataflow.create(name, description);
  return createdDataflow;
};

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

const newEmptyDatasetSchema = async (dataflowId, datasetSchemaName) => {
  const newEmptyDatasetSchemaResponse = await apiDataflow.newEmptyDatasetSchema(dataflowId, datasetSchemaName);
  return newEmptyDatasetSchemaResponse;
};

const pending = async () => {
  const pendingDataflowsDTO = await apiDataflow.pending();
  return parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'PENDING'));
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

const update = async (dataflowId, name, description) => {
  const updatedDataflow = await apiDataflow.update(dataflowId, name, description);
  return updatedDataflow;
};

const accept = async dataflowId => {
  const status = await apiDataflow.accept(dataflowId);
  return status;
};

const reject = async dataflowId => {
  const status = await apiDataflow.reject(dataflowId);
  return status;
};

const getPercentageOfValue = (val, total) => {
  return total === 0 ? '0.00' : ((val / total) * 100).toFixed(2);
};

export const ApiDataflowRepository = {
  all,
  accept,
  accepted,
  create,
  completed,
  dataflowDetails,
  datasetsValidationStatistics,
  datasetsReleasedStatus,
  deleteById,
  newEmptyDatasetSchema,
  pending,
  reject,
  reporting,
  update
};
