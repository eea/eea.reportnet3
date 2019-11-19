import React, { useEffect, useContext, useState } from 'react';

import { isEmpty, isUndefined } from 'lodash';

import styles from './GlobalReleasedDashboard.module.css';

import colors from 'conf/colors.json';

import { Chart } from 'primereact/chart';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/DataFlow';

export const GlobalReleasedDashboard = dataflowId => {
  const resources = useContext(ResourcesContext);
  const [isLoading, setLoading] = useState(true);
  const [releasedDashboardData, setReleasedDashboardData] = useState([]);

  useEffect(() => {
    onLoadDashboard();
  }, []);

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

  const buildReleasedDashboardObject = releasedData => {
    return {
      labels: releasedData.map(dataset => dataset.dataSetName),
      datasets: [
        {
          label: resources.messages['released'],
          backgroundColor: colors.green400,
          data: releasedData.map(dataset => dataset.isReleased)
        },
        {
          label: resources.messages['unreleased'],
          backgroundColor: colors.gray25,
          data: releasedData.map(dataset => !dataset.isReleased)
        }
      ]
    };
  };

  if (isLoading) {
    return <Spinner className={styles.positioning} />;
  }

  if (!isEmpty(releasedDashboardData.datasets) && isEmpty(!releasedDashboardData.labels)) {
    if (releasedDashboardData.datasets.length > 0 && releasedDashboardData.labels.length > 0) {
      return (
        <div className={`${styles.chart_released}`}>
          <Chart type="bar" data={releasedDashboardData} options={releasedOptionsObject} width="100%" height="25%" />
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
