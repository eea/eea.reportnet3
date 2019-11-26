import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';

import { capitalize, isUndefined } from 'lodash';

import styles from './Dashboard.module.css';

import colors from 'conf/colors.json';

import { Chart } from 'primereact/chart';
import { ColorPicker } from 'ui/views/_components/ColorPicker';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DatasetService } from 'core/services/DataSet';
import { ViewUtils } from 'ui/ViewUtils';

const SEVERITY_CODE = {
  CORRECT: 0,
  INFO: 1,
  WARNING: 2,
  ERROR: 3,
  BLOCKER: 4
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
      setDashboardColors({
        CORRECT: colors.correct,
        INFO: colors.info,
        WARNING: colors.warning,
        ERROR: colors.error,
        BLOCKER: colors.blocker
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

    const getLevelErrorsOrdered = levelErrors => {
      return ViewUtils.orderLevelErrors(levelErrors);
    };

    const getLevelErrorPriority = levelError => {
      return ViewUtils.getLevelErrorPriorityByLevelError(levelError);
    };

    const getDashboardBarsByDatasetData = dataset => {
      const dashboardBars = [];
      let levelErrors = getLevelErrorsOrdered(dataset.levelErrorTypes);
      levelErrors.forEach(levelError => {
        let levelErrorIndex = getLevelErrorPriority(levelError);
        let bar = {
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
      const dataset = await DatasetService.errorStatisticsById(datasetId);
      const tableNames = dataset.tables.map(table => table.tableSchemaName);
      setDashboardTitle(dataset.datasetSchemaName);
      setDashboardData({
        labels: tableNames,
        datasets: getDashboardBarsByDatasetData(dataset)
      });

      setDashboardOptions({
        tooltips: {
          mode: 'index',
          callbacks: {
            label: (tooltipItems, data) =>
              `${
                data.datasets[tooltipItems.datasetIndex].totalData[tooltipItems['index']][tooltipItems.datasetIndex]
              } (${tooltipItems.yLabel}%)`
          }
        },
        responsive: true,
        scales: {
          xAxes: [
            {
              stacked: true,
              scaleLabel: {
                display: true,
                labelString: resources.messages['tables']
              }
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
                      <span key={`label_${type}`}>{`  ${capitalize(type)}`}</span>
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
          return (
            <div className={styles.chartDiv}>
              <Chart ref={chartRef} type="bar" data={dashboardData} options={dashboardOptions} />
            </div>
          );
        } else {
          return <div className={styles.NoErrorData}>{resources.messages['noErrorData']}</div>;
        }
      }
    };

    return (
      <React.Fragment>
        <h1 className={styles.dashboardTitle}>{dashboardTitle}</h1>
        {renderDashboard()}
        {/* {renderColorPicker()} */}
      </React.Fragment>
    );
  })
);

export { Dashboard };
