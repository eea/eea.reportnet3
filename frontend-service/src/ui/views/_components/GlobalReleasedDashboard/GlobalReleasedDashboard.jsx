import React, { useEffect, useContext, useState } from 'react';

import { isEmpty, isUndefined } from 'lodash';

import styles from './GlobalReleasedDashboard.module.css';

import { Chart } from 'primereact/chart';
import { ColorPicker } from 'ui/views/_components/ColorPicker';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/DataFlow';

const SEVERITY_CODE = {
  RELEASED: 'RELEASED',
  UNRELEASED: 'UNRELEASED'
};

const GlobalReleasedDashboard = dataflowId => {
  const resources = useContext(ResourcesContext);
  const [dashboardColors, setDashboardColors] = useState({
    RELEASED: '#339900',
    UNRELEASED: '#D0D0CE'
  });
  const [isLoading, setLoading] = useState(true);
  const [releasedDashboardData, setReleasedDashboardData] = useState([]);
  const [releasedData, setReleasedData] = useState();

  useEffect(() => {
    onLoadDashboard();
  }, []);

  const onLoadDashboard = async () => {
    try {
      const releasedData = await DataflowService.datasetsReleasedStatus(dataflowId.dataflowId);
      setReleasedDashboardData(buildReleasedDashboardObject(releasedData));
      setReleasedData(releasedData);
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
  };

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
  }, [dashboardColors]);

  if (isLoading) {
    return <Spinner className={styles.positioning} />;
  }

  if (!isEmpty(releasedDashboardData.datasets) && isEmpty(!releasedDashboardData.labels)) {
    if (releasedDashboardData.datasets.length > 0 && releasedDashboardData.labels.length > 0) {
      return (
        <div className={`rep-row ${styles.chart_released}`}>
          <fieldset className={styles.colorPickerWrap}>
            <legend>{resources.messages['chooseChartColor']}</legend>
            {Object.keys(SEVERITY_CODE).map((type, i) => {
              return (
                <React.Fragment key={i}>
                  <span key={`label_${type}`}>{`  ${type.charAt(0).toUpperCase()}${type
                    .slice(1)
                    .toLowerCase()}: `}</span>
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

export { GlobalReleasedDashboard };
