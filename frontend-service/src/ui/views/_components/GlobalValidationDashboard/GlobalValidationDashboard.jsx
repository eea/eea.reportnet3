import React, { useEffect, useContext, useReducer, useState } from 'react';

import { isEmpty, isUndefined } from 'lodash';

import styles from './GlobalValidationDashboard.module.css';

import { Chart } from 'primereact/chart';
import { FilterList } from 'ui/views/DataCustodianDashboards/_components/FilterList';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/DataFlow';

const GlobalValidationDashboard = dataflowId => {
  const resources = useContext(ResourcesContext);
  const initialFiltersState = {
    reporterFilter: [],
    tableFilter: [],
    statusFilter: [],
    originalData: {},
    data: {}
  };

  const onFilteringData = (originalData, datasetsIdsArr, reportersLabelsArr, msgStatusTypesArr) => {
    if (isEmpty(originalData)) {
      return;
    }

    let tablesData = originalData.datasets.filter(table => showArrayItem(datasetsIdsArr, table.tableId));

    const labels = originalData.labels.filter(label => showArrayItem(reportersLabelsArr, label));

    const labelsPositionsInFilteredLabelsArray = reportersLabelsArr.map(label => getLabelIndex(originalData, label));

    tablesData = cleanOutFilteredTableData(tablesData, labelsPositionsInFilteredLabelsArray).filter(table =>
      showArrayItem(msgStatusTypesArr, table.label)
    );

    return { labels: labels, datasets: tablesData };
  };

  const filterReducer = (state, { type, payload }) => {
    let reportersLabelsArr = [];
    let tablesIdsArray = [];
    let msgStatusTypesArray = [];
    let filteredTableData;
    switch (type) {
      case 'INIT_DATA':
        return {
          ...state,
          originalData: payload,
          data: payload
        };

      case 'TABLE_CHECKBOX_ON':
        tablesIdsArray = state.tableFilter.filter(table => table !== payload.tableId);
        filteredTableData = onFilteringData(
          state.originalData,
          tablesIdsArray,
          state.reporterFilter,
          state.statusFilter
        );

        return {
          ...state,
          tableFilter: tablesIdsArray,
          data: filteredTableData
        };

      case 'TABLE_CHECKBOX_OFF':
        tablesIdsArray = [...state.tableFilter, payload.tableId];

        filteredTableData = onFilteringData(
          state.originalData,
          tablesIdsArray,
          state.reporterFilter,
          state.statusFilter
        );

        return {
          ...state,
          tableFilter: tablesIdsArray,
          data: filteredTableData
        };

      case 'REPORTER_CHECKBOX_ON':
        reportersLabelsArr = state.reporterFilter.filter(label => label !== payload.label);

        filteredTableData = onFilteringData(
          state.originalData,
          state.tableFilter,
          reportersLabelsArr,
          state.statusFilter
        );

        return {
          ...state,
          reporterFilter: reportersLabelsArr,
          data: filteredTableData
        };

      case 'REPORTER_CHECKBOX_OFF':
        reportersLabelsArr = [...state.reporterFilter, payload.label];

        filteredTableData = onFilteringData(
          state.originalData,
          state.tableFilter,
          reportersLabelsArr,
          state.statusFilter
        );
        return {
          ...state,
          reporterFilter: reportersLabelsArr,
          data: filteredTableData
        };
      case 'STATUS_FILTER_ON':
        msgStatusTypesArray = state.statusFilter.filter(status => status !== payload.msg);

        filteredTableData = onFilteringData(
          state.originalData,
          state.tableFilter,
          state.reporterFilter,
          msgStatusTypesArray
        );

        return {
          ...state,
          statusFilter: msgStatusTypesArray,
          data: filteredTableData
        };
      case 'STATUS_FILTER_OFF':
        msgStatusTypesArray = [...state.statusFilter, payload.msg];

        filteredTableData = onFilteringData(
          state.originalData,
          state.tableFilter,
          state.reporterFilter,
          msgStatusTypesArray
        );

        return {
          ...state,
          statusFilter: msgStatusTypesArray,
          data: filteredTableData
        };

      default:
        return state;
    }
  };

  const [filterState, filterDispatch] = useReducer(filterReducer, initialFiltersState);
  const [isLoading, setLoading] = useState(true);

  useEffect(() => {
    onLoadDashboard();
  }, []);

  const onLoadDashboard = async () => {
    try {
      const datasetsValidationStatistics = await DataflowService.datasetsValidationStatistics(dataflowId.dataflowId);
      filterDispatch({ type: 'INIT_DATA', payload: buildDatasetDashboardObject(datasetsValidationStatistics) });
    } catch (error) {
      onErrorLoadingDashboard(error);
    } finally {
      setLoading(false);
    }
  };

  const onErrorLoadingDashboard = error => {
    console.error('Dashboard error: ', error);
    const errorResponse = error.response;
    console.error('Dashboard errorResponse: ', errorResponse);
  };

  const datasetOptionsObject = {
    hover: {
      mode: 'point',
      intersect: false
    },
    tooltips: {
      mode: 'point',
      intersect: true,
      callbacks: {
        label: (tooltipItems, data) =>
          `${data.datasets[tooltipItems.datasetIndex].tableName}: ${
            data.datasets[tooltipItems.datasetIndex].totalData[tooltipItems.index]
          } (${tooltipItems.yLabel}%)`
      }
    },

    legend: {
      display: false
    },
    responsive: true,
    scales: {
      xAxes: [
        {
          stacked: true,
          maxBarThickness: 100
        }
      ],
      yAxes: [
        {
          stacked: true,
          ticks: {
            callback: (value, index, values) => `${value}%`
          }
        }
      ]
    }
  };

  if (isLoading) {
    return <Spinner className={styles.positioning} />;
  }

  if (!isEmpty(filterState.data)) {
    return (
      <div className="rep-row">
        <FilterList originalData={filterState.originalData} filterDispatch={filterDispatch}></FilterList>
        <Chart type="bar" data={filterState.data} options={datasetOptionsObject} width="100%" height="30%" />
      </div>
    );
  } else {
    return (
      <div>
        <h2>{resources.messages['emptyErrorsDashboard']}</h2>
      </div>
    );
  }
};

function buildDatasetDashboardObject(datasetsDashboardsData) {
  let datasets = [];
  if (!isUndefined(datasetsDashboardsData.tables)) {
    datasets = datasetsDashboardsData.tables
      .map(table => [
        {
          label: `CORRECT`,
          tableName: table.tableName,
          tableId: table.tableId,
          backgroundColor: 'rgba(153, 204, 51, 1)',
          data: table.tableStatisticPercentages[0],
          totalData: table.tableStatisticValues[0],
          stack: table.tableName
        },
        {
          label: `WARNINGS`,
          tableName: table.tableName,
          tableId: table.tableId,
          backgroundColor: 'rgba(255, 204, 0, 1)',
          data: table.tableStatisticPercentages[1],
          totalData: table.tableStatisticValues[1],
          stack: table.tableName
        },
        {
          label: `ERRORS`,
          tableName: table.tableName,
          tableId: table.tableId,
          backgroundColor: 'rgba(204, 51, 0, 1)',
          data: table.tableStatisticPercentages[2],
          totalData: table.tableStatisticValues[2],
          stack: table.tableName
        }
      ])
      .flat();
  }
  const labels = datasetsDashboardsData.datasetReporters.map(reporterData => reporterData.reporterName);
  const datasetDataObject = {
    labels: labels,
    datasets: datasets
  };
  return datasetDataObject;
}

function cleanOutFilteredTableData(tablesData, labelsPositionsInFilteredLabelsArray) {
  return tablesData.map(table => ({
    ...table,
    data: table.data.filter((d, i) => !labelsPositionsInFilteredLabelsArray.includes(i)),
    totalData: table.totalData.filter((td, i) => !labelsPositionsInFilteredLabelsArray.includes(i))
  }));
}

function getLabelIndex(originalData, label) {
  return originalData.labels.indexOf(label);
}

function showArrayItem(array, item) {
  return !array.includes(item);
}

export { GlobalValidationDashboard };
