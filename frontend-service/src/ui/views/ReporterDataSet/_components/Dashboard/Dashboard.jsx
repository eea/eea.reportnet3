import React, { useState, useEffect, useContext } from 'react';
import { withRouter } from 'react-router-dom';

import isUndefined from 'lodash/isUndefined';

import styles from './Dashboard.module.css';

import { Chart } from 'primereact/chart';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { DataSetService } from 'core/services/DataSet';

const Dashboard = withRouter(
  React.memo(({ refresh, match: { params: { dataSetId } } }) => {
    const [dashBoardData, setDashBoardData] = useState({});
    const [dashBoardOptions, setDashBoardOptions] = useState({});
    const [dashBoardTitle, setDashBoardTitle] = useState('');
    const resources = useContext(ResourcesContext);

    useEffect(() => {
      if (refresh) {
        onLoadStatistics();
      }
    }, [refresh]);

    const onLoadStatistics = async dataSetId => {
      const dataSet = await DataSetService.errorStatisticsById(dataSetId);
      const tableStatisticValues = dataSet.tableStatisticValues;
      const tableNames = dataSet.tables.map(table => table.tableSchemaName);

      setDashBoardTitle(dataSet.dataSetSchemaName);
      setDashBoardData({
        labels: tableNames,
        datasets: [
          {
            label: 'Correct',
            backgroundColor: '#004494',
            data: dataSet.tableStatisticPercentages[0],
            totalData: tableStatisticValues
          },
          {
            label: 'Warning',
            backgroundColor: '#ffd617',
            data: dataSet.tableStatisticPercentages[1],
            totalData: tableStatisticValues
          },
          {
            label: 'Error',
            backgroundColor: '#DA2131',
            data: dataSet.tableStatisticPercentages[2],
            totalData: tableStatisticValues
          }
        ]
      });

      setDashBoardOptions({
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
    };

    return (
      <React.Fragment>
        {!isUndefined(dashBoardData.datasets) &&
        dashBoardData.datasets.length > 0 &&
        ![].concat.apply([], dashBoardData.datasets[0].totalData).every(t => t === 0) ? (
          <React.Fragment>
            <h1>{dashBoardTitle}</h1>
            <Chart type="bar" data={dashBoardData} options={dashBoardOptions} />
          </React.Fragment>
        ) : (
          <div className={styles.NoErrorData}>{resources.messages['noErrorData']}</div>
        )}
      </React.Fragment>
    );
  })
);

export { Dashboard };
