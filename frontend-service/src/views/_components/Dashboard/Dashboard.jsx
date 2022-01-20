import { Fragment, memo, useContext, useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';

import capitalize from 'lodash/capitalize';
import isUndefined from 'lodash/isUndefined';

import styles from './Dashboard.module.scss';

import colors from 'conf/colors.json';

import { Chart } from 'primereact/chart';
import { Spinner } from 'views/_components/Spinner';
import { StatusList } from 'views/_components/StatusList';

import { DatasetService } from 'services/DatasetService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useStatusFilter } from 'views/_components/StatusList/_hooks/useStatusFilter';

import { ErrorUtils } from 'views/_functions/Utils';

const Dashboard = memo(({ refresh, tableSchemaNames }) => {
  const { datasetId } = useParams();

  const resourcesContext = useContext(ResourcesContext);

  const [dashboardData, setDashboardData] = useState({});
  const [dashboardTitle, setDashboardTitle] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [levelErrorTypes, setLevelErrorTypes] = useState([]);

  const { updatedState, statusDispatcher } = useStatusFilter(dashboardData);

  const chartRef = useRef();

  const dashboardColors = {
    CORRECT: colors.correct,
    INFO: colors.info,
    WARNING: colors.warning,
    ERROR: colors.error,
    BLOCKER: colors.blocker
  };

  useEffect(() => {
    if (refresh) {
      onLoadStatistics();
    }
    return () => {
      setDashboardData([]);
    };
  }, [refresh, datasetId]);

  const getLevelErrorsOrdered = levelErrors => ErrorUtils.orderLevelErrors(levelErrors);

  const getLevelErrorPriority = levelError => ErrorUtils.getLevelErrorPriorityByLevelError(levelError);

  const getDashboardBarsByDatasetData = dataset => {
    const dashboardBars = [];
    const levelErrors = getLevelErrorsOrdered(dataset.levelErrorTypes);

    levelErrors.forEach(levelError => {
      const levelErrorIndex = getLevelErrorPriority(levelError);
      const bar = {
        label: capitalize(levelError),
        backgroundColor: !isUndefined(dashboardColors) ? dashboardColors[levelError] : colors.levelError,
        data: dataset.tableStatisticPercentages[levelErrorIndex],
        totalData: dataset.tableStatisticValues
      };
      dashboardBars.push(bar);
    });

    return dashboardBars;
  };

  const onLoadStatistics = async () => {
    setIsLoading(true);
    const dataset = await DatasetService.getStatistics(datasetId, tableSchemaNames);
    setLevelErrorTypes(dataset.levelErrorTypes);
    const tableNames = dataset.tables.map(table => table.tableSchemaName);
    setDashboardTitle(dataset.datasetSchemaName);
    setDashboardData({
      labels: tableNames,
      datasets: getDashboardBarsByDatasetData(dataset)
    });

    setIsLoading(false);
  };

  const dashboardOptions = {
    tooltips: {
      mode: 'index',
      callbacks: {
        label: (tooltipItems, data) =>
          `${data.datasets[tooltipItems.datasetIndex].totalData[tooltipItems['index']][tooltipItems.datasetIndex]} (${
            tooltipItems.yLabel
          }%)`
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
          gridLines: { display: false }
        }
      ],
      yAxes: [
        {
          stacked: true,
          scaleLabel: {
            display: true,
            labelString: resourcesContext.messages['percentage']
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

  const renderDashboard = () => {
    if (
      !isUndefined(dashboardData.datasets) &&
      dashboardData.datasets.length > 0 &&
      ![].concat.apply([], dashboardData.datasets[0].totalData).every(total => total === 0)
    ) {
      return (
        <Fragment>
          <span
            className={styles.dashboardWarning}
            dangerouslySetInnerHTML={{ __html: resourcesContext.messages['dashboardWarning'] }}></span>
          <div className={styles.chartDiv}>
            <StatusList
              filterDispatch={statusDispatcher}
              filteredStatusTypes={updatedState.filterStatus}
              statusTypes={levelErrorTypes}
            />
            <Chart
              data={updatedState.dashboardData}
              height="95%"
              options={dashboardOptions}
              ref={chartRef}
              type="bar"
            />
          </div>
        </Fragment>
      );
    } else {
      return (
        <div className={styles.dashboardWithoutData}>
          <div className={styles.noDashboard}>{resourcesContext.messages['noValidationDashboardData']}</div>
        </div>
      );
    }
  };

  if (isLoading) {
    return (
      <div className={styles.dashboardWithoutData}>
        <div className={styles.spinner}>
          <Spinner style={{ top: 0, left: 0 }} />
        </div>
      </div>
    );
  }

  return (
    <Fragment>
      {dashboardTitle && <h1 className={styles.dashboardTitle}>{dashboardTitle}</h1>}
      {renderDashboard()}
    </Fragment>
  );
});

export { Dashboard };
