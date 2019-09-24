import React, { useEffect, useContext, useState, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import { isEmpty, isArray } from 'lodash';

import styles from './DataCustodianDashboards.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Chart } from 'primereact/chart';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { FilterList } from './_components/FilterList/FilterList';

import { DataFlowService } from 'core/services/DataFlow';
import { UserContext } from '../_components/_context/UserContext';

export const DataCustodianDashboards = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const userData = useContext(UserContext);
  const [loading, setLoading] = useState(true);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);

  const [dashboardsData, setDashboardsData] = useState({});
  const [datasetsDashboardData, setDatasetsDashboardData] = useState({});
  const [datasetsDashboardOptions, setDatasetsDashboardOptions] = useState({});
  const [releasedDashboardData, setReleasedDashboardData] = useState({});
  const [releasedDashboardOptions, setReleasedDashboardOptions] = useState({});

  const home = {
    icon: config.icons['home'],
    command: () => history.push('/')
  };

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataFlowList'],
        command: () => history.push('/data-flow-task')
      },
      {
        label: resources.messages.reportingDataFlow,
        command: () => history.push(`/reporting-data-flow/${match.params.dataFlowId}`)
      },
      {
        label: resources.messages.dataCustodianDashboards
      }
    ]);
  }, []);

  const tableNames = isArray(dashboardsData.tables) && dashboardsData.tables.map(table => table.tableName);

  const tablePercentages =
    isArray(dashboardsData.tables) && dashboardsData.tables.map(table => table.tableStatisticPercentages);

  const tableOnePercentages = tablePercentages[0];
  const tableTwoPercentages = tablePercentages[1];
  const tableThirdPercentages = tablePercentages[2];

  const tableValues =
    isArray(dashboardsData.tables) && dashboardsData.tables.map(values => values.tableStatisticValues);

  const tableOneValues = tableValues[0];
  const tableTwoValues = tableValues[1];
  const tableThirdValues = tableValues[2];

  function getDatasetsDashboardData() {
    const datasetDataObject = {
      labels: dashboardsData.dataSetCountries.map(countryData => countryData.countryName),
      datasets: [
        {
          label: `CORRECT`,
          tableName: tableNames[0],
          tableId: 'a1111',
          backgroundColor: '#004494',
          data: tableOnePercentages[0],
          totalData: tableOneValues[0],
          stack: tableNames[0]
        },
        {
          label: `WARNINGS`,
          tableName: tableNames[0],
          tableId: 'a1111',
          backgroundColor: '#ffd617',
          data: tableOnePercentages[1],
          totalData: tableOneValues[1],
          stack: tableNames[0]
        },
        {
          label: `ERRORS`,
          tableName: tableNames[0],
          tableId: 'a1111',
          backgroundColor: '#DA2131',
          data: tableOnePercentages[2],
          totalData: tableOneValues[2],
          stack: tableNames[0]
        },
        {
          label: `CORRECT`,
          tableName: tableNames[1],
          tableId: 'b2222',
          backgroundColor: '#004494',
          data: tableTwoPercentages[0],
          totalData: tableTwoValues[0],
          stack: tableNames[1]
        },
        {
          label: `WARNINGS`,
          tableName: tableNames[1],
          tableId: 'b2222',
          backgroundColor: '#ffd617',
          data: tableTwoPercentages[1],
          totalData: tableTwoValues[1],
          stack: tableNames[1]
        },
        {
          label: `ERRORS`,
          tableName: tableNames[1],
          tableId: 'b2222',
          backgroundColor: '#DA2131',
          data: tableTwoPercentages[2],
          totalData: tableTwoValues[2],
          stack: tableNames[1]
        },
        {
          label: `CORRECT`,
          tableName: tableNames[2],
          tableId: 'c3333',
          backgroundColor: '#004494',
          data: tableThirdPercentages[0],
          totalData: tableThirdValues[0],
          stack: tableNames[2]
        },
        {
          label: `WARNINGS`,
          tableName: tableNames[2],
          tableId: 'c3333',
          backgroundColor: '#ffd617',
          data: tableThirdPercentages[1],
          totalData: tableThirdValues[1],
          stack: tableNames[2]
        },
        {
          label: `ERRORS`,
          tableName: tableNames[2],
          tableId: 'c3333',
          backgroundColor: '#DA2131',
          data: tableThirdPercentages[2],
          totalData: tableThirdValues[2],
          stack: tableNames[2]
        }
      ]
    };
    filterDispatch({ type: 'INIT_DATA', payload: datasetDataObject });
    return datasetDataObject;
  }

  function getDatasetsDashboardOptions() {
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
    return datasetOptionsObject;
  }

  function getReleasedDashboardData() {
    const releasedDataObject = {
      labels: dashboardsData.dataSetCountries.map(countryData => countryData.countryName),
      datasets: [
        {
          label: 'Released',
          backgroundColor: 'rgb(50, 205, 50)',

          data: dashboardsData.dataSetCountries.map(released => released.isDataSetReleased)
        },
        {
          label: 'Unreleased',
          backgroundColor: 'rgb(255, 99, 132)',
          data: dashboardsData.dataSetCountries.map(released => !released.isDataSetReleased)
        }
      ]
    };
    return releasedDataObject;
  }

  function getReleasedDashboardOptions() {
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
    return releasedOptionsObject;
  }

  const onPageLoad = () => {
    getDatasetsDashboardData();

    setDatasetsDashboardOptions(getDatasetsDashboardOptions());

    setReleasedDashboardData(getReleasedDashboardData());

    setReleasedDashboardOptions(getReleasedDashboardOptions());
  };

  const onFilteringData = (originalData, datasetsIdsArr, countriesLabelsArr, msgStatusTypesArr) => {
    let tablesData = originalData.datasets.filter(table => !datasetsIdsArr.includes(table.tableId));

    const labels = originalData.labels.filter(label => !countriesLabelsArr.includes(label));

    const labelPositionsInArray = countriesLabelsArr.map(label => originalData.labels.indexOf(label));

    tablesData = tablesData.map(table => ({
      ...table,
      data: table.data.filter((d, i) => !labelPositionsInArray.includes(i)),
      totalData: table.totalData.filter((td, i) => !labelPositionsInArray.includes(i))
    }));

    tablesData = tablesData.filter(table => !msgStatusTypesArr.includes(table.label));

    return { labels: labels, datasets: tablesData };
  };

  useEffect(() => {
    if (!isEmpty(dashboardsData)) {
      onPageLoad();
    }
  }, [dashboardsData]);

  const initialFiltersState = {
    countryFilter: [],
    tableFilter: [],
    statusFilter: [],
    originalData: {},
    data: {}
  };

  const filterReducer = (state, { type, payload }) => {
    let countriesLabelsArr = [];
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
          state.countryFilter,
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
          state.countryFilter,
          state.statusFilter
        );

        return {
          ...state,
          tableFilter: tablesIdsArray,
          data: filteredTableData
        };

      case 'COUNTRY_CHECKBOX_ON':
        countriesLabelsArr = state.countryFilter.filter(label => label !== payload.label);

        filteredTableData = onFilteringData(
          state.originalData,
          state.tableFilter,
          countriesLabelsArr,
          state.statusFilter
        );

        return {
          ...state,
          countryFilter: countriesLabelsArr,
          data: filteredTableData
        };

      case 'COUNTRY_CHECKBOX_OFF':
        countriesLabelsArr = [...state.countryFilter, payload.label];

        filteredTableData = onFilteringData(
          state.originalData,
          state.tableFilter,
          countriesLabelsArr,
          state.statusFilter
        );
        return {
          ...state,
          countryFilter: countriesLabelsArr,
          data: filteredTableData
        };
      case 'STATUS_FILTER_ON':
        msgStatusTypesArray = state.statusFilter.filter(status => status !== payload.msg);

        filteredTableData = onFilteringData(
          state.originalData,
          state.tableFilter,
          state.countryFilter,
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
          state.countryFilter,
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

  const loadDashboards = async () => {
    try {
      const dashboardData = await DataFlowService.dashboards(match.params.dataFlowId);
      setDashboardsData(dashboardData);
    } catch (error) {
      console.error(error.response);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboards();
  }, []);

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
        <h1>{resources.messages['dataFlow']}</h1>
      </div>
      <div className="rep-row">
        <FilterList originalData={filterState.originalData} filterDispatch={filterDispatch}></FilterList>
        <Chart type="bar" data={filterState.data} options={datasetsDashboardOptions} width="100%" height="35%" />
      </div>
      <div className={`rep-row ${styles.chart_released}`}>
        <Chart type="bar" data={releasedDashboardData} options={releasedDashboardOptions} width="100%" height="25%" />
      </div>
    </>
  );
});
