import { isNull, isUndefined } from 'lodash';

import moment from 'moment';

import { apiDataflow } from 'core/infrastructure/api/domain/model/Dataflow';
import { DataCollection } from 'core/domain/model/DataCollection/DataCollection';
import { Dataflow } from 'core/domain/model/Dataflow/Dataflow';
import { Dataset } from 'core/domain/model/Dataset/Dataset';
import { DatasetTable } from 'core/domain/model/Dataset/DatasetTable/DatasetTable';
import { DatasetTableField } from 'core/domain/model/Dataset/DatasetTable/DatasetRecord/DatasetTableField/DatasetTableField';
import { DatasetTableRecord } from 'core/domain/model/Dataset/DatasetTable/DatasetRecord/DatasetTableRecord';
import { Representative } from 'core/domain/model/Representative/Representative';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

import { CoreUtils } from 'core/infrastructure/CoreUtils';

const accept = async dataflowId => {
  const status = await apiDataflow.accept(dataflowId);
  return status;
};

const accepted = async () => {
  const acceptedDataflowsDTO = await apiDataflow.accepted();
  return parseDataflowDTOs(acceptedDataflowsDTO.filter(item => item.userRequestStatus === 'ACCEPTED'));
};

const all = async () => {
  const pendingDataflowsDTO = await apiDataflow.all();
  return {
    pending: parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'PENDING')),
    accepted: parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'ACCEPTED')),
    completed: parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'COMPLETED'))
  };
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

const getAllSchemas = async dataflowId => {
  const datasetSchemasDTO = await apiDataflow.allSchemas(dataflowId);
  // const datasetSchemasDTO = [
  //   {
  //     idDataSetSchema: '5e662c48c9b42f00018d4cbc',
  //     description: null,
  //     nameDatasetSchema: 'schema dos',
  //     tableSchemas: [
  //       {
  //         idTableSchema: '5e65f90b7c84f327b83722d9',
  //         description: null,
  //         nameTableSchema: 'Tsabla uno',
  //         recordSchema: {
  //           idRecordSchema: '5e65f90b7c84f327b83722da',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9197c84f327b83722db',
  //               description: null,
  //               idRecord: '5e65f90b7c84f327b83722da',
  //               name: 'campo uno',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637684f327b83722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: false,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84fz27b83722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e63f9687c84f327b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327bg3722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327b8h722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f322b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84v327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327b8g722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327d83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84fs27b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327b8372ade',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b8h722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327b13722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327b8f722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f32ab83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327b83722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b8j722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327183722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327bk3722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327e83722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c8jf327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327b837x2de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327bj3722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327bj1722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: false,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327j83722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: false,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e65f95a7c84f327b83722dc',
  //         description: null,
  //         nameTableSchema: 'TAbla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e65f95a7c84f327b83722dd',
  //           fieldSchema: [
  //             {
  //               id: '5e65f9637c84f327283722de',
  //               description: null,
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo uno bis',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e65f9687c84f327b83722df',
  //               description: '',
  //               idRecord: '5e65f95a7c84f327b83722dd',
  //               name: 'campo dos',
  //               type: 'NUMBER',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       }
  //     ]
  //   },
  //   {
  //     idDataSetSchema: '5e6627461cf2e754ac519f9a',
  //     description: null,
  //     nameDatasetSchema: 'schema uno',
  //     tableSchemas: [
  //       {
  //         idTableSchema: '5e66274f1cf2eg54ac519f9c',
  //         description: null,
  //         nameTableSchema: 'tabla tres',
  //         recordSchema: {
  //           idRecordSchema: '5e66274f1cf2e754ac519f3d',
  //           fieldSchema: [
  //             {
  //               id: '5e6627551cf2e754ac519f93',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac519f3d',
  //               name: 'campito zero',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e6627551cf2e754ac519f9e',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac519f3d',
  //               name: 'campito one',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e66274f1cf2e754ac519f9c',
  //         description: null,
  //         nameTableSchema: 'tabla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e66274f1cf2e754ac519f9d',
  //           fieldSchema: [
  //             {
  //               id: '5e6627551cf2e754ac513f93',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac519f9h',
  //               name: 'campito zero',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e6627551cf2e754ac519f9e',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac519f9d',
  //               name: 'campito one',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       }
  //     ]
  //   },
  //   {
  //     idDataSetSchema: '5e6627461cf2e754ac519f9a',
  //     description: null,
  //     nameDatasetSchema: 'schema tres',
  //     tableSchemas: [
  //       {
  //         idTableSchema: '5e66274f1cf2e7g4ac519g9c',
  //         description: null,
  //         nameTableSchema: 'tabla tres2',
  //         recordSchema: {
  //           idRecordSchema: '5e66274f1cf2e754ac51933d',
  //           fieldSchema: [
  //             {
  //               id: '5e6627551cf2e754a1519f33',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac51933d',
  //               name: 'campito zero',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             },
  //             {
  //               id: '6e6627551cf2e754ac519f91',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac51933d',
  //               name: 'campito one',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e66274f1cf2e754ac519f9c',
  //         description: null,
  //         nameTableSchema: 'tabla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e66274f1cf2e754ac519f9d',
  //           fieldSchema: [
  //             {
  //               id: '5e6627551cf2e75fac519f93',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac519f9h',
  //               name: 'campito zero',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e6627551cf22744ac519f9e',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac519f9d',
  //               name: 'campito one',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       }
  //     ]
  //   },
  //   {
  //     idDataSetSchema: '5e6627461cf2e754ac519f9a',
  //     description: null,
  //     nameDatasetSchema: 'schema cuatro',
  //     tableSchemas: [
  //       {
  //         idTableSchema: '5e66274f1cf2e7g4ac519g9c',
  //         description: null,
  //         nameTableSchema: 'tabla tres2',
  //         recordSchema: {
  //           idRecordSchema: '5e66274f1cf2e754ac51933d',
  //           fieldSchema: [
  //             {
  //               id: '5e6627551cf2e754a1519f33',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac51933d',
  //               name: 'campito zero',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e6627551cf2e752ac519f91',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac51933d',
  //               name: 'campito one',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e66274f1cf2e754ac519f9c',
  //         description: null,
  //         nameTableSchema: 'tabla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e66274f1cf2e754ac519f9d',
  //           fieldSchema: [
  //             {
  //               id: '5e6627551cf2275fac519f93',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac519f9h',
  //               name: 'campito zero',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e6627551cf1e744ac519f9e',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac519f9d',
  //               name: 'campito one',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       }
  //     ]
  //   },
  //   {
  //     idDataSetSchema: '5e6627461cf2e754ac519f9a',
  //     description: null,
  //     nameDatasetSchema: 'schema cinco',
  //     tableSchemas: [
  //       {
  //         idTableSchema: '5e66274f1cf2e7g4ac519g9c',
  //         description: null,
  //         nameTableSchema: 'tabla tres2',
  //         recordSchema: {
  //           idRecordSchema: '5e66274f1cf2e754ac51933d',
  //           fieldSchema: [
  //             {
  //               id: '5e6627551cf2e754a1519f33',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac51933d',
  //               name: 'campito zero',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e66275513f2e754ac519f91',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac51933d',
  //               name: 'campito one',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       },
  //       {
  //         idTableSchema: '5e66274f1cf2e754ac519f9c',
  //         description: null,
  //         nameTableSchema: 'tabla dos',
  //         recordSchema: {
  //           idRecordSchema: '5e66274f1cf2e754ac519f9d',
  //           fieldSchema: [
  //             {
  //               id: '5e6627551cf2e75fac519f93',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac519f9h',
  //               name: 'campito zero',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: null,
  //               referencedField: null
  //             },
  //             {
  //               id: '5e6627551c42e744ac519f9e',
  //               description: null,
  //               idRecord: '5e66274f1cf2e754ac519f9d',
  //               name: 'campito one',
  //               type: 'TEXT',
  //               codelistItems: null,
  //               required: false,
  //               pk: true,
  //               referencedField: null
  //             }
  //           ]
  //         }
  //       }
  //     ]
  //   }
  // ];
  // console.log({ datasetSchemasDTO });
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
                    pkReferenced: !isNull(DataTableFieldDTO.pkReferenced) ? DataTableFieldDTO.pkReferenced : false,
                    name: DataTableFieldDTO.name,
                    recordId: DataTableFieldDTO.idRecord,
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
        tableSchemaId: datasetTableDTO.idTableSchema,
        tableSchemaDescription: datasetTableDTO.description,
        tableSchemaName: datasetTableDTO.nameTableSchema,
        records: records,
        recordSchemaId: !isNull(datasetTableDTO.recordSchema) ? datasetTableDTO.recordSchema.idRecordSchema : null
      });
    });

    dataset.tables = tables;
    return dataset;
  });

  console.log({ datasetSchemas });
  datasetSchemas.sort((a, b) => {
    console.log(a.datasetSchemaName, b.datasetSchemaName);
    const textA = a.datasetSchemaName.toUpperCase();
    const textB = b.datasetSchemaName.toUpperCase();
    return textA < textB ? -1 : textA > textB ? 1 : 0;
  });
  console.log({ datasetSchemas });
  return datasetSchemas;
};

const getPercentageOfValue = (val, total) => {
  return total === 0 ? '0.00' : ((val / total) * 100).toFixed(2);
};

const newEmptyDatasetSchema = async (dataflowId, datasetSchemaName) => {
  const newEmptyDatasetSchemaResponse = await apiDataflow.newEmptyDatasetSchema(dataflowId, datasetSchemaName);
  return newEmptyDatasetSchemaResponse;
};

const parseDataflowDTO = dataflowDTO =>
  new Dataflow({
    creationDate: dataflowDTO.creationDate,
    dataCollections: parseDataCollectionListDTO(dataflowDTO.dataCollections),
    datasets: parseDatasetListDTO(dataflowDTO.reportingDatasets),
    deadlineDate: moment(dataflowDTO.deadlineDate).format('YYYY-MM-DD'),
    description: dataflowDTO.description,
    designDatasets: parseDatasetListDTO(dataflowDTO.designDatasets),
    documents: parseDocumentListDTO(dataflowDTO.documents),
    id: dataflowDTO.id,
    name: dataflowDTO.name,
    representatives: parseRepresentativeListDTO(dataflowDTO.representatives),
    requestId: dataflowDTO.requestId,
    status: dataflowDTO.status,
    userRequestStatus: dataflowDTO.userRequestStatus,
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
    providerAccount: representativeDTO.provideraccount
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

const pending = async () => {
  const pendingDataflowsDTO = await apiDataflow.pending();
  return parseDataflowDTOs(pendingDataflowsDTO.filter(item => item.userRequestStatus === 'PENDING'));
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

const update = async (dataflowId, name, description) => {
  const updatedDataflow = await apiDataflow.update(dataflowId, name, description);
  return updatedDataflow;
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
  getAllSchemas,
  newEmptyDatasetSchema,
  pending,
  reject,
  reporting,
  schemasValidation,
  update
};
