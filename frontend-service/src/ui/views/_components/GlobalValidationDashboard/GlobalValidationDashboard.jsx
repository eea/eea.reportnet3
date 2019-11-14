import React, { useEffect, useContext, useReducer, useState, useRef } from 'react';

import { isEmpty, isNull, isUndefined } from 'lodash';

import styles from './GlobalValidationDashboard.module.css';

import colors from 'conf/colors.json';

import { Chart } from 'primereact/chart';
import { ColorPicker } from 'ui/views/_components/ColorPicker';
import { FilterList } from 'ui/views/DataCustodianDashboards/_components/FilterList';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { filterReducer } from './_components/_context/filterReducer';

import { DataflowService } from 'core/services/DataFlow';

const SEVERITY_CODE = {
  CORRECT: colors.dashboardCorrect,
  INFO: colors.dashboardInfo,
  WARNING: colors.dashboardWarning,
  ERROR: colors.dashboardError,
  BLOCKER: colors.dashboardBlocker
};

const GlobalValidationDashboard = ({ datasetSchemaId }) => {
  const resources = useContext(ResourcesContext);
  const initialFiltersState = {
    reporterFilter: [],
    tableFilter: [],
    statusFilter: [],
    originalData: {},
    data: {}
  };
  const [dashboardColors, setDashboardColors] = useState({
    CORRECT: colors.dashboardCorrect,
    INFO: colors.dashboardInfo,
    WARNING: colors.dashboardWarning,
    ERROR: colors.dashboardError,
    BLOCKER: colors.dashboardBlocker
  });
  const [filterState, filterDispatch] = useReducer(filterReducer, initialFiltersState);
  const [isLoading, setLoading] = useState(true);
  const [validationDashboardData, setValidationDashboardData] = useState();
  const chartRef = useRef();

  useEffect(() => {
    onLoadDashboard();
  }, []);

  useEffect(() => {
    filterDispatch({ type: 'INIT_DATA', payload: validationDashboardData });
  }, [validationDashboardData]);

  const onChangeColor = (color, type) => {
    setDashboardColors({ ...dashboardColors, [SEVERITY_CODE[type]]: `#${color}` });
    const filteredDatasets = filterState.originalData.datasets.filter(dataset => dataset.label === SEVERITY_CODE[type]);

    const filteredDatasetsCurrent = chartRef.current.chart.data.datasets.filter(
      dataset => dataset.label === SEVERITY_CODE[type]
    );
    filteredDatasets.forEach(dataset => {
      dataset.backgroundColor = `#${color}`;
    });
    filteredDatasetsCurrent.forEach(dataset => {
      dataset.backgroundColor = `#${color}`;
    });

    chartRef.current.refresh();
  };

  const onErrorLoadingDashboard = error => {
    console.error('Dashboard error: ', error);
    const errorResponse = error.response;
    console.error('Dashboard errorResponse: ', errorResponse);
  };

  const onLoadDashboard = async () => {
    try {
      const datasetsValidationStatistics = await DataflowService.datasetsValidationStatistics(datasetSchemaId);
      if (!isUndefined(datasetsValidationStatistics.datasetId) && !isNull(datasetsValidationStatistics.datasetId)) {
        setValidationDashboardData(buildDatasetDashboardObject(datasetsValidationStatistics));
      }
    } catch (error) {
      onErrorLoadingDashboard(error);
    } finally {
      setLoading(false);
    }
  };

  const buildDatasetDashboardObject = datasetsDashboardsData => {
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
            label: `WARNING`,
            tableName: table.tableName,
            tableId: table.tableId,
            backgroundColor: dashboardColors.WARNING,
            data: table.tableStatisticPercentages[1],
            totalData: table.tableStatisticValues[1],
            stack: table.tableName
          },
          {
            label: `ERROR`,
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
            min: 0,
            max: 100,
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
      <div className={`rep-row ${styles.chart_released}`}>
        <FilterList
          color={dashboardColors}
          originalData={filterState.originalData}
          filterDispatch={filterDispatch}></FilterList>
        <Chart
          ref={chartRef}
          type="bar"
          data={filterState.data}
          options={datasetOptionsObject}
          width="100%"
          height="30%"
        />
        {/* <fieldset className={styles.colorPickerWrap}>
          <legend>{resources.messages['chooseChartColor']}</legend>
          <div className={styles.fieldsetContent}>
            {Object.keys(SEVERITY_CODE).map((type, i) => {
              return (
                <div className={styles.colorPickerItem} key={i}>
                  <span key={`label_${type}`}>{`  ${type.charAt(0).toUpperCase()}${type
                    .slice(1)
                    .toLowerCase()}: `}</span>
                  <ColorPicker
                    className={styles.colorPicker}
                    //key={type}
                    value={!isUndefined(dashboardColors) ? dashboardColors[type] : ''}
                    onChange={e => {
                      e.preventDefault();
                      onChangeColor(e.value, SEVERITY_CODE[type]);
                    }}
                  />
                </div>
              );
            })}
          </div>
        </fieldset> */}
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
