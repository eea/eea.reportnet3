import React, { useEffect, useContext, useReducer, useState, useRef } from 'react';

import { isEmpty, isNull, isUndefined } from 'lodash';

import styles from './DatasetValidationDashboard.module.css';

import colors from 'conf/colors.json';

import { Chart } from 'primereact/chart';
import { FilterList } from './_components/FilterList';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { filterReducer } from './_functions/filterReducer';

import { DataflowService } from 'core/services/Dataflow';
import { ErrorUtils } from 'ui/views/_functions/Utils';

/* const SEVERITY_CODE = {
  CORRECT: colors.dashboardCorrect,
  INFO: colors.dashboardInfo,
  WARNING: colors.dashboardWarning,
  ERROR: colors.dashboardError,
  BLOCKER: colors.dashboardBlocker
}; */

export const DatasetValidationDashboard = ({ datasetSchemaId, isVisible, datasetSchemaName }) => {
  const resources = useContext(ResourcesContext);
  const initialFiltersState = {
    reporterFilter: [],
    tableFilter: [],
    statusFilter: [],
    originalData: {},
    data: {}
  };
  const [dashboardColors] = useState({
    CORRECT: colors.correct,
    INFO: colors.info,
    WARNING: colors.warning,
    ERROR: colors.error,
    BLOCKER: colors.blocker
  });
  const [filterState, filterDispatch] = useReducer(filterReducer, initialFiltersState);
  const [isLoading, setLoading] = useState(true);
  const [levelErrorTypes, setLevelErrorTypes] = useState([]);
  const [validationDashboardData, setValidationDashboardData] = useState();
  const chartRef = useRef();

  useEffect(() => {
    onLoadDashboard();
  }, []);

  useEffect(() => {
    filterDispatch({ type: 'INIT_DATA', payload: validationDashboardData });
  }, [validationDashboardData]);

  // const onChangeColor = (color, type) => {
  //   setDashboardColors({ ...dashboardColors, [SEVERITY_CODE[type]]: `#${color}` });
  //   const filteredDatasets = filterState.originalData.datasets.filter(dataset => dataset.label === SEVERITY_CODE[type]);

  //   const filteredDatasetsCurrent = chartRef.current.chart.data.datasets.filter(
  //     dataset => dataset.label === SEVERITY_CODE[type]
  //   );
  //   filteredDatasets.forEach(dataset => {
  //     dataset.backgroundColor = `#${color}`;
  //   });
  //   filteredDatasetsCurrent.forEach(dataset => {
  //     dataset.backgroundColor = `#${color}`;
  //   });

  //   chartRef.current.refresh();
  // };

  const onErrorLoadingDashboard = error => {
    console.error('Dashboard error: ', error);
    const errorResponse = error.response;
    console.error('Dashboard errorResponse: ', errorResponse);
  };

  const onLoadDashboard = async () => {
    try {
      const datasetsValidationStatistics = await DataflowService.datasetsValidationStatistics(datasetSchemaId);
      setLevelErrorTypes(datasetsValidationStatistics.levelErrors);
      if (!isUndefined(datasetsValidationStatistics.datasetId) && !isNull(datasetsValidationStatistics.datasetId)) {
        setLevelErrorTypes(datasetsValidationStatistics.levelErrors);
        setValidationDashboardData(
          buildDatasetDashboardObject(datasetsValidationStatistics, datasetsValidationStatistics.levelErrors)
        );
      }
    } catch (error) {
      onErrorLoadingDashboard(error);
    } finally {
      setLoading(false);
    }
  };

  const onLoadStamp = message => {
    return <span className={`${styles.stamp} ${styles.emptySchema}`}>{message}</span>;
  };

  const getDatasetsByErrorAndStatistics = (datasets, levelErrors) => {
    let allDatasets = getDashboardBarsByDataset(datasets, levelErrors);
    return allDatasets.flat();
  };

  const getLevelErrorPriority = levelError => {
    return ErrorUtils.getLevelErrorPriorityByLevelError(levelError);
  };

  const getDashboardBarsByDataset = (datasets, levelErrors) => {
    let allDatasets = [];
    datasets.tables.forEach((table, z) => {
      let allLevelErrorBars = [];
      levelErrors.forEach((levelError, i) => {
        let levelErrorIndex = getLevelErrorPriority(levelError);
        const errorBar = {
          label: levelError,
          tableName: table.tableName,
          tableId: table.tableId,
          backgroundColor: !isUndefined(dashboardColors) ? dashboardColors[levelError] : colors.levelErrorIndex,
          data: table.tableStatisticPercentages[levelErrorIndex],
          totalData: table.tableStatisticValues[levelErrorIndex],
          stack: table.tableName
        };
        allLevelErrorBars.push(errorBar);
      });
      allDatasets.push(allLevelErrorBars);
    });
    return allDatasets;
  };

  const buildDatasetDashboardObject = (datasets, levelErrors) => {
    let dashboards = [];
    if (!isUndefined(datasets)) {
      dashboards = getDatasetsByErrorAndStatistics(datasets, levelErrors);
    }
    const labels = datasets.datasetReporters.map(reporterData => reporterData.reporterName);
    const datasetDataObject = {
      labels: labels,
      datasets: dashboards
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
          scaleLabel: {
            display: true,
            labelString: resources.messages['percentage']
          },
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
  return (
    <>
      {isVisible ? (
        <div className={`rep-row ${styles.chart_released}`}>
          <h3 className={styles.dashboardName}>{datasetSchemaName}</h3>
          {filterState.data ? (
            <>
              <FilterList
                datasetSchemaId={datasetSchemaId}
                color={dashboardColors}
                filterDispatch={filterDispatch}
                levelErrors={levelErrorTypes}
                originalData={filterState.originalData}
                reporterFilters={filterState.reporterFilter}
                statusFilters={filterState.statusFilter}
                tableFilters={filterState.tableFilter}
              />
              {!isEmpty(filterState.originalData.datasets) ? '' : onLoadStamp(resources.messages['empty'])}
              <Chart
                ref={chartRef}
                type="bar"
                data={filterState.data}
                options={datasetOptionsObject}
                width="100%"
                height="30%"
              />
            </>
          ) : (
            //   <fieldset className={styles.colorPickerWrap}>
            //   <legend>{resources.messages['chooseChartColor']}</legend>
            //   <div className={styles.fieldsetContent}>
            //     {Object.keys(SEVERITY_CODE).map((type, i) => {
            //       return (
            //         <div className={styles.colorPickerItem} key={i}>
            //           <span key={`label_${type}`}>{`  ${type.charAt(0).toUpperCase()}${type
            //             .slice(1)
            //             .toLowerCase()}: `}</span>
            //           <ColorPicker
            //             className={styles.colorPicker}
            //             //key={type}
            //             value={!isUndefined(dashboardColors) ? dashboardColors[type] : ''}
            //             onChange={e => {
            //               e.preventDefault();
            //               onChangeColor(e.value, SEVERITY_CODE[type]);
            //             }}
            //           />
            //         </div>
            //       );
            //     })}
            //   </div>
            // </fieldset>
            <>
              <FilterList levelErrors={[]} originalData={{ labels: {}, datasets: {} }} />
              {onLoadStamp(resources.messages['empty'])}
              <Chart
                className={styles.emptyChart}
                type="bar"
                options={datasetOptionsObject}
                width="100%"
                height="30%"
              />
            </>
          )}
        </div>
      ) : (
        <></>
      )}
    </>
  );
};
