import React, { useEffect, useContext, useReducer, useState } from 'react';

import { isEmpty, isUndefined } from 'lodash';

import styles from './GlobalValidationDashboard.module.css';

import { Chart } from 'primereact/chart';
import { ColorPicker } from 'ui/views/_components/ColorPicker';
import { FilterList } from 'ui/views/DataCustodianDashboards/_components/FilterList';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { filterReducer } from './_components/_context/filterReducer';

import { DataflowService } from 'core/services/DataFlow';

const SEVERITY_CODE = {
  CORRECT: 'CORRECT',
  WARNING: 'WARNING',
  ERROR: 'ERROR'
};

const GlobalValidationDashboard = dataflowId => {
  const resources = useContext(ResourcesContext);
  const initialFiltersState = {
    reporterFilter: [],
    tableFilter: [],
    statusFilter: [],
    originalData: {},
    data: {}
  };
  const [dashboardColors, setDashboardColors] = useState({
    CORRECT: '#99CC33',
    WARNING: '#ffCC00',
    ERROR: '#CC3300'
  });
  const [filterState, filterDispatch] = useReducer(filterReducer, initialFiltersState);
  const [isLoading, setLoading] = useState(true);
  const [validationDashboardData, setValidationDashboardData] = useState();

  useEffect(() => {
    onLoadDashboard();
  }, []);

  const onLoadDashboard = async () => {
    try {
      const datasetsValidationStatistics = await DataflowService.datasetsValidationStatistics(dataflowId.dataflowId);
      setValidationDashboardData(buildDatasetDashboardObject(datasetsValidationStatistics));
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

  function buildDatasetDashboardObject(datasetsDashboardsData) {
    let datasets = [];
    console.log(datasetsDashboardsData);
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
    console.log(filterState);
    if (!isUndefined(filterState.data)) {
      const {
        originalData: { labels, datasets }
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

  useEffect(() => {
    filterDispatch({ type: 'INIT_DATA', payload: validationDashboardData });
    console.log(filterState, validationDashboardData);
    if (
      !isEmpty(filterState.data) &&
      (filterState.data.datasets.length !== validationDashboardData.datasets.length ||
        !isEmpty(filterState.reporterFilter) ||
        !isEmpty(filterState.statusFilter) ||
        !isEmpty(filterState.tableFilter))
    ) {
      console.log(filterState, filterState.data.datasets, validationDashboardData.datasets);
      filterDispatch({
        type: 'APPLY_FILTERS',
        payload: filterState
      });
    }
  }, [validationDashboardData]);

  if (isLoading) {
    return <Spinner className={styles.positioning} />;
  }

  if (!isEmpty(filterState.data)) {
    return (
      <div className={`rep-row ${styles.chart_released}`}>
        <FilterList
          color={dashboardColors}
          originalData={filterState.originalData}
          filterDispatch={filterDispatch}></FilterList>
        <Chart type="bar" data={filterState.data} options={datasetOptionsObject} width="100%" height="30%" />
        <fieldset className={styles.colorPickerWrap}>
          <legend>{resources.messages['chooseChartColor']}</legend>
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

export { GlobalValidationDashboard };
