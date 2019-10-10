import React, { useEffect, useContext, useState, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import { isEmpty, isUndefined } from 'lodash';

import styles from './DataCustodianDashboards.module.scss';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Chart } from 'primereact/chart';
import { ColorPicker } from 'ui/views/_components/ColorPicker';
import { FilterList } from './_components/FilterList/FilterList';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/DataFlow';

import { filterReducer } from './_components/_context/filterReducer';
import { getUrl } from 'core/infrastructure/api/getUrl';

const SEVERITY_CODE = {
  CORRECT: 'CORRECT',
  WARNING: 'WARNING',
  ERROR: 'ERROR',
  RELEASED: 'RELEASED',
  UNRELEASED: 'UNRELEASED'
};

export const DataCustodianDashboards = withRouter(({ match, history }) => {
  const initialFiltersState = {
    reporterFilter: [],
    tableFilter: [],
    statusFilter: [],
    originalData: {},
    data: {}
  };

  const [filterState, filterDispatch] = useReducer(filterReducer, initialFiltersState);
  const resources = useContext(ResourcesContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dashboardColors, setDashboardColors] = useState({
    CORRECT: '#99CC33',
    WARNING: '#ffCC00',
    ERROR: '#CC3300',
    RELEASED: '#339900',
    UNRELEASED: '#D0D0CE'
  });
  const [dataflowMetadata, setDataflowMetadata] = useState({});
  const [isLoadingValidationData, setIsLoadingValidationData] = useState(true);
  const [isLoadingReleasedData, setIsLoadingReleasedData] = useState(true);
  const [releasedDashboardData, setReleasedDashboardData] = useState([]);
  const [releasedData, setReleasedData] = useState();
  const [validationDashboardData, setValidationDashboardData] = useState();

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
            backgroundColor: dashboardColors.CORRECT,
            data: table.tableStatisticPercentages[0],
            totalData: table.tableStatisticValues[0],
            stack: table.tableName
          },
          {
            label: `WARNINGS`,
            tableName: table.tableName,
            tableId: table.tableId,
            backgroundColor: dashboardColors.WARNING,
            data: table.tableStatisticPercentages[1],
            totalData: table.tableStatisticValues[1],
            stack: table.tableName
          },
          {
            label: `ERRORS`,
            tableName: table.tableName,
            tableId: table.tableId,
            backgroundColor: dashboardColors.ERROR,
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
    if (!isUndefined(filterState.data)) {
      const {
        data: { labels, datasets }
      } = filterState;
      if (labels && datasets) {
        setValidationDashboardData({
          labels: labels,
          datasets: datasets.map(dataset => {
            switch (dataset.label) {
              case 'CORRECT':
                dataset.backgroundColor = dashboardColors.CORRECT;
                break;
              case 'WARNINGS':
                dataset.backgroundColor = dashboardColors.WARNING;
                break;
              case 'ERRORS':
                dataset.backgroundColor = dashboardColors.ERROR;
                break;
              default:
                break;
            }
            return dataset;
          })
        });
      }
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
            backgroundColor: dashboardColors.RELEASED,
            data: releasedData.map(dataset => dataset.isReleased)
          },
          {
            label: resources.messages['unreleased'],
            backgroundColor: dashboardColors.UNRELEASED,
            data: releasedData.map(dataset => !dataset.isReleased)
          }
        ]
      });
    }
  }, []);

  const loadDashboards = async () => {
    const releasedData = await DataflowService.datasetsReleasedStatus(match.params.dataflowId);
    setReleasedDashboardData(buildReleasedDashboardObject(releasedData));
    setReleasedData(releasedData);
    setIsLoadingReleasedData(false);

    const datasetsDashboardsData = await DataflowService.datasetsValidationStatistics(match.params.dataflowId);
    setValidationDashboardData(buildDatasetDashboardObject(datasetsDashboardsData));
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
          <FilterList color={dashboardColors} filterDispatch={filterDispatch} originalData={filterState.originalData} />
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
                  setDashboardColors({ ...dashboardColors, [SEVERITY_CODE[type]]: `#${e.value}` });
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
