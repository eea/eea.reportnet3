import React, { useState, useEffect, useContext } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './Dashboard.module.css';

import { config } from 'assets/conf';

import { Chart } from 'primereact/chart';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const Dashboard = withRouter(
  React.memo(({ refresh, match: { params: { dataSetId } } }) => {
    const [dashBoardData, setDashBoardData] = useState({});
    const [dashBoardOptions, setDashBoardOptions] = useState({});
    const [dashBoardTitle, setDashBoardTitle] = useState('');
    const resources = useContext(ResourcesContext);

    useEffect(() => {
      if (refresh) {
        const dataPromise = HTTPRequester.get({
          // url: `${config.loadStatisticsAPI.url}${dataSetId}`,
          url: '/jsons/error-statistics.json',
          queryString: {}
        });

        //Parse JSON to array statistic values.
        dataPromise
          .then(res => {
            if (res.data.tables !== null) {
              const tabStatisticNames = [];
              const tabStatisticValues = [];
              setDashBoardTitle(res.data.nameDataSetSchema);
              res.data.tables.forEach(t => {
                tabStatisticNames.push(t.nameTableSchema);
                tabStatisticValues.push([
                  t.totalRecords - (t.totalRecordsWithErrors + t.totalRecordsWithWarnings),
                  t.totalRecordsWithWarnings,
                  t.totalRecordsWithErrors
                ]);
              });
              //Transpose value matrix and delete undefined elements to fit Chart data structure
              const transposedValues = Object.keys(tabStatisticValues)
                .map(c => tabStatisticValues.map(r => r[c]))
                .filter(t => t[0] !== undefined);

              setDashBoardData({
                labels: tabStatisticNames,
                datasets: [
                  {
                    label: 'Correct',
                    backgroundColor: '#004494',
                    data: getPercentage(transposedValues)[0],
                    totalData: tabStatisticValues
                  },
                  {
                    label: 'Warning',
                    backgroundColor: '#ffd617',
                    data: getPercentage(transposedValues)[1],
                    totalData: tabStatisticValues
                  },
                  {
                    label: 'Error',
                    backgroundColor: '#DA2131',
                    data: getPercentage(transposedValues)[2],
                    totalData: tabStatisticValues
                  }
                ]
              });

              setDashBoardOptions({
                tooltips: {
                  mode: 'index',
                  callbacks: {
                    label: (tooltipItems, data) =>
                      `${
                        data.datasets[tooltipItems.datasetIndex].totalData[tooltipItems['index']][
                          tooltipItems.datasetIndex
                        ]
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
            }
          })
          .catch(error => {
            console.log(error);
            return error;
          });
      }
    }, [refresh]);

    const getPercentage = valArr => {
      let total = valArr.reduce((arr1, arr2) => arr1.map((v, i) => v + arr2[i]));
      return valArr.map(val => val.map((v, i) => ((v / total[i]) * 100).toFixed(2)));
    };

    return (
      <React.Fragment>
        {dashBoardData.datasets &&
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
