import React, { useEffect, useContext, useState, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './DataCustodianDashboards.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Chart } from 'primereact/chart';
import { FilterList } from './_components/FilterList/FilterList';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/DataFlow';
import { UserContext } from '../_components/_context/UserContext';

import { getUrl } from 'core/infrastructure/api/getUrl';

export const DataCustodianDashboards = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const userData = useContext(UserContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [releasedDashboardData, setReleasedDashboardData] = useState([]);

  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(config.DATAFLOWS.url))
  };

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        command: () => history.push(getUrl(config.DATAFLOWS.url))
      },
      {
        label: resources.messages.dataflow,
        command: () => history.push(`/dataflow/${match.params.dataflowId}`)
      },
      {
        label: resources.messages.dataCustodianDashboards
      }
    ]);
  }, []);

  useEffect(() => {
    loadDashboards();
  }, []);

  const loadDashboards = async () => {
    try {
      setReleasedDashboardData(await DataflowService.datasetReleasedStatus(match.params.dataflowId));

      const datasetsDashboardsData = await DataflowService.datasetStatisticsStatus(match.params.dataflowId);

      filterDispatch({ type: 'INIT_DATA', payload: datasetsDashboardsData });
    } catch (error) {
      console.error(error.response);
    } finally {
      setLoading(false);
    }
  };

  const datasetOptionsObject = {
    tooltips: {
      model: 'index',
      intersect: false,
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
          stacked: true
        }
      ],
      yAxes: [
        {
          stacked: true,
          ticks: {
            // Include a % sign in the ticks
            callback: (value, index, values) => `${value}%`
          }
        }
      ]
    }
  };

  const releasedOptionsObject = {
    tooltips: {
      mode: 'index',
      intersect: false
    },
    responsive: true,
    scales: {
      xAxes: [
        {
          stacked: true
        }
      ],
      yAxes: [
        {
          stacked: true,
          display: false
        }
      ]
    }
  };

  const onFilteringData = (originalData, datasetsIdsArr, reportersLabelsArr, msgStatusTypesArr) => {
    let tablesData = originalData.datasets.filter(table => showArrayItem(datasetsIdsArr, table.tableId));

    const labels = originalData.labels.filter(label => showArrayItem(reportersLabelsArr, label));

    const labelsPositionsInFilteredLabelsArray = reportersLabelsArr.map(label => getLabelIndex(originalData, label));

    tablesData = cleanOutFilteredTableData(tablesData, labelsPositionsInFilteredLabelsArray);

    tablesData = tablesData.filter(table => showArrayItem(msgStatusTypesArr, table.label));

    return { labels: labels, datasets: tablesData };
  };

  const initialFiltersState = {
    reporterFilter: [],
    tableFilter: [],
    statusFilter: [],
    originalData: {},
    data: {}
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

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <>
      <div className="rep-row">
        <h1>{resources.messages['dataflow']}</h1>
      </div>
      <div className="rep-row">
        <FilterList originalData={filterState.originalData} filterDispatch={filterDispatch}></FilterList>
        <Chart type="bar" data={filterState.data} options={datasetOptionsObject} width="100%" height="30%" />
      </div>
      <div className={`rep-row ${styles.chart_released}`}>
        <Chart type="bar" data={releasedDashboardData} options={releasedOptionsObject} width="100%" height="25%" />
      </div>
    </>
  );
});

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
