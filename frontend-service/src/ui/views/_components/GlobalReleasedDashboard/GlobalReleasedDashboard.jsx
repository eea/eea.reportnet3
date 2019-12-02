import React, { useContext, useEffect, useReducer, useState } from 'react';

import { isEmpty } from 'lodash';

import styles from './GlobalReleasedDashboard.module.css';

import colors from 'conf/colors.json';

import { Chart } from 'primereact/chart';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { StatusList } from 'ui/views/_components/StatusList';

import { useStatusFilter } from 'ui/views/_components/StatusList/_hooks/useStatusFilter';

import { DataflowService } from 'core/services/DataFlow';

export const GlobalReleasedDashboard = dataflowId => {
  const resources = useContext(ResourcesContext);
  const [isLoading, setLoading] = useState(true);
  const [maxValue, setMaxValue] = useState();
  const [releasedDashboardData, setReleasedDashboardData] = useState([]);

  const { updatedState, statusDispatcher } = useStatusFilter(releasedDashboardData);

  useEffect(() => {
    onLoadDashboard();
  }, []);

  useEffect(() => {
    statusDispatcher({ type: 'INIT_DATA', payload: releasedDashboardData });
  }, [releasedDashboardData]);

  const onLoadDashboard = async () => {
    try {
      const releasedData = await DataflowService.datasetsReleasedStatus(dataflowId.dataflowId);
      setReleasedDashboardData(buildReleasedDashboardObject(releasedData));
      setLoading(false);
    } catch (error) {
      onErrorLoadingDashboard(error);
    } finally {
      setLoading(false);
    }
  };

  const onErrorLoadingDashboard = error => {
    console.error('Released dashboard error: ', error);
    const errorResponse = error.response;
    console.error('Released dashboard errorResponse: ', errorResponse);
  };

  const getMaxOfArrays = (releasedNumArr, unReleasedNumArr) => {
    const maxReleased = Math.max.apply(null, releasedNumArr);
    const maxUnReleased = Math.max.apply(null, unReleasedNumArr);
    return Math.max(maxReleased, maxUnReleased);
  };

  const buildReleasedDashboardObject = data => {
    setMaxValue(getMaxOfArrays(data.releasedData, data.unReleasedData));
    return {
      labels: data.labels,
      datasets: [
        {
          label: resources.messages['released'],
          backgroundColor: colors.green400,
          data: data.releasedData
        },
        {
          label: resources.messages['unreleased'],
          backgroundColor: colors.gray25,
          data: data.unReleasedData
        }
      ]
    };
  };

  const releasedOptionsObject = {
    tooltips: {
      mode: 'point',
      intersect: true
      // callbacks: {
      //   label: (tooltipItems, data) =>
      //     `${tooltipItems.yLabel} ${data.datasets[tooltipItems.datasetIndex].label}: ${
      //       data.datasets[tooltipItems.datasetIndex]
      //     }
      //     `
      // }
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
            beginAtZero: true,
            max: maxValue,
            callback: value => {
              if (Number.isInteger(value)) {
                return value;
              }
            },
            stepSize: 1
          }
        }
      ]
    }
  };

  if (isLoading) {
    return <Spinner className={styles.positioning} />;
  }

  if (!isEmpty(releasedDashboardData.datasets) && isEmpty(!releasedDashboardData.labels)) {
    if (releasedDashboardData.datasets.length > 0 && releasedDashboardData.labels.length > 0) {
      return (
        <div className={`${styles.chart_released}`}>
          {!isEmpty(updatedState.dashboardData) ? (
            <>
              <StatusList
                filterDispatch={statusDispatcher}
                filteredStatusTypes={updatedState.filterStatus}
                statusTypes={['RELEASED', 'UNRELEASED']}
              />
              <Chart
                type="bar"
                data={updatedState.dashboardData}
                options={releasedOptionsObject}
                width="100%"
                height="25%"
              />
            </>
          ) : (
            <>
              <Chart type="bar" options={releasedOptionsObject} width="100%" height="25%" />
            </>
          )}
        </div>
      );
    }
  }
  return (
    <div>
      <h2>{resources.messages['emptyReleasedDashboard']}</h2>
    </div>
  );
};
