import React, { useEffect, useContext, useState, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import { isEmpty, isUndefined } from 'lodash';

import styles from './DataCustodianDashboards.module.scss';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Chart } from 'primereact/chart';
import { ColorPicker } from 'primereact/colorpicker';
import { FilterList } from './_components/FilterList/FilterList';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/DataFlow';

import { getUrl } from 'core/infrastructure/api/getUrl';

const SEVERITY_CODE = {
  CORRECT: 1,
  WARNING: 2,
  ERROR: 3,
  RELEASED: 4,
  UNRELEASED: 5
};

export const DataCustodianDashboards = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dashboardColors, setDashboardColors] = useState();
  const [dataflowMetadata, setDataflowMetadata] = useState({});
  const [isLoadingValidationData, setIsLoadingValidationData] = useState(true);
  const [isLoadingReleasedData, setIsLoadingReleasedData] = useState(true);
  const [releasedDashboardData, setReleasedDashboardData] = useState([]);
  const [releasedData, setReleasedData] = useState();
  const [validationDashboardData, setValidationDashboardData] = useState();
  const [validationData, setValidationData] = useState();

  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(routes.DATAFLOWS))
  };

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        command: () => history.push(getUrl(routes.DATAFLOWS))
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
    setDashboardColors({
      CORRECT: '#99CC33',
      WARNING: '#ffCC00',
      ERROR: '#CC3300',
      RELEASED: '#339900',
      UNRELEASED: '#D0D0CE'
    });
  }, []);

  useEffect(() => {
    try {
      loadDataflowMetadata();
      loadDashboards();
    } catch (error) {
      console.error(error.response);
    }
  }, []);

  const loadDataflowMetadata = async () => {
    const dataflowMetadata = await DataflowService.metadata(match.params.dataflowId);
    setDataflowMetadata(dataflowMetadata);
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

  useEffect(() => {
    if (!isUndefined(validationData)) {
      setValidationDashboardData({
        labels: validationData.datasetReporters.map(reporterData => reporterData.reporterName),
        datasets: validationData.tables
          .map(table => [
            {
              label: `CORRECT`,
              tableName: table.tableName,
              tableId: table.tableId,
              backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.CORRECT : '#99CC33',
              data: table.tableStatisticPercentages[0],
              totalData: table.tableStatisticValues[0],
              stack: table.tableName
            },
            {
              label: `WARNINGS`,
              tableName: table.tableName,
              tableId: table.tableId,
              backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.WARNING : '#ffCC00',
              data: table.tableStatisticPercentages[1],
              totalData: table.tableStatisticValues[1],
              stack: table.tableName
            },
            {
              label: `ERRORS`,
              tableName: table.tableName,
              tableId: table.tableId,
              backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.ERROR : '#CC3300',
              data: table.tableStatisticPercentages[2],
              totalData: table.tableStatisticValues[2],
              stack: table.tableName
            }
          ])
          .flat()
      });
    }
  }, [dashboardColors]);

  function buildReleasedDashboardObject(releasedData) {
    return {
      labels: releasedData.map(dataset => dataset.dataSetName),
      datasets: [
        {
          label: resources.messages['released'],
          backgroundColor: '#339900',
          data: releasedData.map(dataset => dataset.isReleased)
        },
        {
          label: resources.messages['unreleased'],
          backgroundColor: '#D0D0CE',
          data: releasedData.map(dataset => !dataset.isReleased)
        }
      ]
    };
  }

  useEffect(() => {
    if (!isUndefined(releasedData)) {
      setReleasedDashboardData({
        labels: releasedData.map(dataset => dataset.dataSetName),
        datasets: [
          {
            label: resources.messages['released'],
            backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.RELEASED : '#339900',
            data: releasedData.map(dataset => dataset.isReleased)
          },
          {
            label: resources.messages['unreleased'],
            backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.UNRELEASED : '#D0D0CE',
            data: releasedData.map(dataset => !dataset.isReleased)
          }
        ]
      });
    }
  }, [dashboardColors]);

  const loadDashboards = async () => {
    const releasedData = await DataflowService.datasetsReleasedStatus(match.params.dataflowId);
    setReleasedDashboardData(buildReleasedDashboardObject(releasedData));
    setReleasedData(releasedData);
    setIsLoadingReleasedData(false);

    const datasetsDashboardsData = await DataflowService.datasetsValidationStatistics(match.params.dataflowId);
    setValidationDashboardData(buildDatasetDashboardObject(datasetsDashboardsData));
    setValidationData(datasetsDashboardsData);
    setIsLoadingValidationData(false);
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
            // Include a % sign in the ticks
            callback: (value, index, values) => `${value}%`
          }
        }
      ]
    }
  };

  const releasedOptionsObject = {
    tooltips: {
      enabled: false
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
          display: false
        }
      ]
    }
  };

  const onChangeColor = (color, type) => {
    const inmDashboardColors = { ...dashboardColors };
    inmDashboardColors[Object.keys(SEVERITY_CODE)[type - 1]] = `#${color}`;
    setDashboardColors(inmDashboardColors);
  };

  const onFilteringData = (originalData, datasetsIdsArr, reportersLabelsArr, msgStatusTypesArr) => {
    if (isEmpty(originalData)) {
      return;
    }

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

  useEffect(() => {
    filterDispatch({ type: 'INIT_DATA', payload: validationDashboardData });
  }, [validationDashboardData]);

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  const errorsDashboard = () => {
    if (!isEmpty(filterState.data)) {
      return (
        <div className="rep-row">
          <FilterList originalData={filterState.originalData} filterDispatch={filterDispatch}></FilterList>
          <Chart type="bar" data={filterState.data} options={datasetOptionsObject} width="100%" height="30%" />
        </div>
      );
    }
    return (
      <div>
        <h2>{resources.messages['emptyErrorsDashboard']}</h2>
      </div>
    );
  };

  const releasedDashboard = () => {
    if (!isEmpty(releasedDashboardData.datasets) && isEmpty(!releasedDashboardData.labels)) {
      if (releasedDashboardData.datasets.length > 0 && releasedDashboardData.labels.length > 0) {
        return (
          <>
            <div className={`rep-row ${styles.chart_released}`}>
              <Chart
                type="bar"
                data={releasedDashboardData}
                options={releasedOptionsObject}
                width="100%"
                height="25%"
              />
            </div>
          </>
        );
      }
    }
    return (
      <div>
        <h2>{resources.messages['emptyReleasedDashboard']}</h2>
      </div>
    );
  };

  return layout(
    <>
      <div className="rep-row">
        <h1>
          {resources.messages['dataflow']}: {dataflowMetadata.name}
        </h1>
      </div>
      <fieldset className={styles.colorPickerWrap}>
        <legend>Choose your dashboard color</legend>
        {Object.keys(SEVERITY_CODE).map((type, i) => {
          return (
            <React.Fragment key={i}>
              <span key={`label_${type}`}>{`  ${type.charAt(0).toUpperCase()}${type.slice(1).toLowerCase()}: `}</span>
              <ColorPicker
                key={type}
                value={!isUndefined(dashboardColors) ? dashboardColors[type] : ''}
                onChange={e => {
                  e.preventDefault();
                  onChangeColor(e.value, SEVERITY_CODE[type]);
                }}
              />
            </React.Fragment>
          );
        })}
      </fieldset>
      <div> {isLoadingValidationData ? <Spinner className={styles.positioning} /> : errorsDashboard()}</div>
      <div> {isLoadingReleasedData ? <Spinner className={styles.positioning} /> : releasedDashboard()}</div>
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
