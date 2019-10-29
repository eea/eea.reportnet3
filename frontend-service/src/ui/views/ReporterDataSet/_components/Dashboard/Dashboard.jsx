import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';

import isUndefined from 'lodash/isUndefined';

import styles from './Dashboard.module.css';

import colors from 'conf/colors.json';

import { Chart } from 'primereact/chart';
import { ColorPicker } from 'ui/views/_components/ColorPicker';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { DatasetService } from 'core/services/DataSet';

const SEVERITY_CODE = {
  CORRECT: 1,
  WARNING: 2,
  ERROR: 3
};

const Dashboard = withRouter(
  React.memo(({ refresh, match: { params: { datasetId } } }) => {
    const [dashboardColors, setDashboardColors] = useState();
    const [dashboardData, setDashboardData] = useState({});
    const [dashboardOptions, setDashboardOptions] = useState({});
    const [dashboardTitle, setDashboardTitle] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const resources = useContext(ResourcesContext);

    const chartRef = useRef();

    useEffect(() => {
      console.log('colors', colors);
      setDashboardColors({
        CORRECT: colors.dashboardCorrect,
        WARNING: colors.dashboardWarning,
        ERROR: colors.dashboardError
      });
    }, []);

    useEffect(() => {
      if (refresh) {
        onLoadStatistics();
      }
      return () => {
        setDashboardData([]);
      };
    }, [refresh, datasetId]);

    const onChangeColor = (color, type) => {
      const inmDashboardColors = { ...dashboardColors };
      inmDashboardColors[Object.keys(SEVERITY_CODE)[type - 1]] = `#${color}`;
      setDashboardColors(inmDashboardColors);
      chartRef.current.chart.data.datasets[type - 1].backgroundColor = `#${color}`;
      chartRef.current.refresh();
    };

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
            backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.CORRECT : colors.dashboardCorrect,
            data: dataset.tableStatisticPercentages[0],
            totalData: tableStatisticValues
          },
          {
            label: 'Warning',
            backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.WARNING : colors.dashboardWarning,
            data: dataset.tableStatisticPercentages[1],
            totalData: tableStatisticValues
          },
          {
            label: 'Error',
            backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.ERROR : colors.dashboardError,
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
                callback: (value, index, values) => `${value} %`
              }
            }
          ]
        }
      });
      setIsLoading(false);
    };

    const renderColorPicker = () => {
      if (
        !isUndefined(dashboardData.datasets) &&
        dashboardData.datasets.length > 0 &&
        ![].concat.apply([], dashboardData.datasets[0].totalData).every(total => total === 0)
      ) {
        return (
          <div className={styles.dashboardWraper}>
            <fieldset className={styles.colorPickerWrap}>
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
                        onChange={e => {
                          e.preventDefault();
                          onChangeColor(e.value, SEVERITY_CODE[type]);
                        }}
                        value={!isUndefined(dashboardColors) ? dashboardColors[type] : colors.dashboardCorrect}
                      />
                    </div>
                  );
                })}
              </div>
            </fieldset>
          </div>
        );
      }
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
          return <Chart ref={chartRef} type="bar" data={dashboardData} options={dashboardOptions} />;
        } else {
          return <div className={styles.NoErrorData}>{resources.messages['noErrorData']}</div>;
        }
      }
    };

    return (
      <React.Fragment>
        <h1>{dashboardTitle}</h1>
        {renderDashboard()}
        {renderColorPicker()}
      </React.Fragment>
    );
  })
);

export { Dashboard };
