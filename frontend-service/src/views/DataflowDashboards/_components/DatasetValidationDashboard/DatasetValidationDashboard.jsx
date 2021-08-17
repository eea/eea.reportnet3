import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import styles from './DatasetValidationDashboard.module.css';

import colors from 'conf/colors.json';

import { Chart } from 'primereact/chart';
import { FilterList } from './_components/FilterList';
import { Spinner } from 'views/_components/Spinner';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { filterReducer } from './_functions/filterReducer';

import { DataflowService } from 'services/DataflowService';
import { ErrorUtils } from 'views/_functions/Utils';

export const DatasetValidationDashboard = ({ dataflowId, datasetSchemaId, datasetSchemaName, isVisible }) => {
  const resources = useContext(ResourcesContext);

  const initialFiltersState = { data: {}, originalData: {}, reporterFilter: [], statusFilter: [], tableFilter: [] };

  const [filterState, filterDispatch] = useReducer(filterReducer, initialFiltersState);

  const [dashboardColors] = useState({
    BLOCKER: colors.blocker,
    CORRECT: colors.correct,
    ERROR: colors.error,
    INFO: colors.info,
    WARNING: colors.warning
  });

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

  const onLoadDashboard = async () => {
    try {
      const data = await DataflowService.getDatasetsValidationStatistics(dataflowId, datasetSchemaId);
      setLevelErrorTypes(data.levelErrors);

      if (!isUndefined(data.datasetId) && !isNull(data.datasetId)) {
        setLevelErrorTypes(data.levelErrors);

        setValidationDashboardData(buildDatasetDashboardObject(data, data.levelErrors));
      }
    } catch (error) {
      console.error('DatasetValidationDashboard - onLoadDashboard.', error);
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
    datasets: { bar: { maxBarThickness: 100 } },
    scales: {
      xAxes: [
        {
          stacked: true,
          gridLines: { display: false }
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
            callback: value => `${value}%`
          },
          gridLines: { display: false }
        }
      ]
    }
  };

  if (isLoading) return <Spinner className={styles.positioning} />;

  return (
    isVisible && (
      <div className={`rep-row ${styles.chart_released}`}>
        <h3 className={styles.dashboardName}>{datasetSchemaName}</h3>
        {filterState.data ? (
          <Fragment>
            <FilterList
              color={dashboardColors}
              datasetSchemaId={datasetSchemaId}
              filterDispatch={filterDispatch}
              levelErrors={levelErrorTypes}
              originalData={filterState.originalData}
              reporterFilters={filterState.reporterFilter}
              statusFilters={filterState.statusFilter}
              tableFilters={filterState.tableFilter}
            />
            {!isEmpty(filterState.originalData.datasets) ? '' : onLoadStamp(resources.messages['empty'])}
            <Chart
              data={filterState.data}
              height="30%"
              options={datasetOptionsObject}
              ref={chartRef}
              style={{ marginTop: '3rem' }}
              type="bar"
              width="100%"
            />
          </Fragment>
        ) : (
          <Fragment>
            <FilterList levelErrors={[]} originalData={{ labels: {}, datasets: {} }} />
            {onLoadStamp(resources.messages['empty'])}
            <Chart className={styles.emptyChart} height="30%" options={datasetOptionsObject} type="bar" width="100%" />
          </Fragment>
        )}
      </div>
    )
  );
};
