import React, { useState, useEffect, useContext } from 'react';
import { withRouter } from 'react-router-dom';

import isUndefined from 'lodash/isUndefined';

import styles from './Dashboard.module.css';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { DatasetService } from 'core/services/DataSet';

import { Chart } from 'primereact/chart';

const Dashboard = withRouter(
  React.memo(({ refresh, match: { params: { datasetId } } }) => {
    const [dashboardData, setDashboardData] = useState({});
    const [dashboardOptions, setDashboardOptions] = useState({});
    const [dashboardTitle, setDashboardTitle] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const resources = useContext(ResourcesContext);

    useEffect(() => {
      if (refresh) {
        onLoadStatistics();
      }
      return () => {
        setDashboardData([]);
      };
    }, [refresh, datasetId]);

    const onLoadStatistics = async () => {
      setIsLoading(true);
      const dataset = await DatasetService.errorStatisticsById(datasetId);
      const tableStatisticValues = dataset.tableStatisticValues;
      const tableNames = dataset.tables.map(table => table.tableSchemaName);

      setDashboardTitle(dataset.datasetSchemaName);
      setDashboardData({
        labels: tableNames,
        datasets: [
          {
            label: 'Correct',
            backgroundColor: '#004494',
            data: dataset.tableStatisticPercentages[0],
            totalData: tableStatisticValues
          },
          {
            label: 'Warning',
            backgroundColor: '#ffd617',
            data: dataset.tableStatisticPercentages[1],
            totalData: tableStatisticValues
          },
          {
            label: 'Error',
            backgroundColor: '#DA2131',
            data: dataset.tableStatisticPercentages[2],
            totalData: tableStatisticValues
          }
        ]
      });

      setDashboardOptions({
        tooltips: {
          mode: 'index',
          callbacks: {
            label: (tooltipItems, data) =>
              `${
                data.datasets[tooltipItems.datasetIndex].totalData[tooltipItems['index']][tooltipItems.datasetIndex]
              } (${tooltipItems.yLabel} %)`
          }
        },
        responsive: true,
        scales: {
          xAxes: [
            {
              stacked: true,
              scaleLabel: {
                display: true,
                labelString: 'Tables'
              }
            }
          ],
          yAxes: [
            {
              stacked: true,
              scaleLabel: {
                display: true,
                labelString: 'Percentage'
              },
              ticks: {
                // Include a % sign in the ticks
                callback: (value, index, values) => `${value} %`
              }
            }
          ]
        }
      });
      setIsLoading(false);
    };

    const renderDashboard = () => {
      if (isLoading) {
        return <Spinner className={styles.dashBoardSpinner} />;
      } else {
        if (
          !isUndefined(dashboardData.datasets) &&
          dashboardData.datasets.length > 0 &&
          ![].concat.apply([], dashboardData.datasets[0].totalData).every(total => total === 0)
        ) {
          return <Chart type="bar" data={dashboardData} options={dashboardOptions} />;
        } else {
          return <div className={styles.NoErrorData}>{resources.messages['noErrorData']}</div>;
        }
      }
    };

    return (
      <React.Fragment>
        <h1>{dashboardTitle}</h1>
        {renderDashboard()}
      </React.Fragment>
    );
  })
);

export { Dashboard };
