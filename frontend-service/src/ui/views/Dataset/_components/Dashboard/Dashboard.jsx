import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';

import { capitalize, isUndefined } from 'lodash';

import styles from './Dashboard.module.css';

import colors from 'conf/colors.json';

import { Chart } from 'primereact/chart';
import { ColorPicker } from 'ui/views/_components/ColorPicker';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { StatusList } from 'ui/views/_components/StatusList';

import { useStatusFilter } from 'ui/views/_components/StatusList/_hooks/useStatusFilter';

import { DatasetService } from 'core/services/Dataset';
import { ErrorUtils } from 'ui/views/_functions/Utils';

const SEVERITY_CODE = {
  CORRECT: 0,
  INFO: 1,
  WARNING: 2,
  ERROR: 3,
  BLOCKER: 4
};

const Dashboard = withRouter(
  React.memo(({ refresh, tableSchemaNames, match: { params: { datasetId } } }) => {
    const [dashboardColors, setDashboardColors] = useState();
    const [dashboardData, setDashboardData] = useState({});
    const [dashboardTitle, setDashboardTitle] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [levelErrorTypes, setLevelErrorTypes] = useState([]);

    const { updatedState, statusDispatcher } = useStatusFilter(dashboardData);

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
      return ErrorUtils.orderLevelErrors(levelErrors);
    };

    const getLevelErrorPriority = levelError => {
      return ErrorUtils.getLevelErrorPriorityByLevelError(levelError);
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
      const dataset = await DatasetService.errorStatisticsById(datasetId, tableSchemaNames);
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
              <StatusList
                filterDispatch={statusDispatcher}
                filteredStatusTypes={updatedState.filterStatus}
                statusTypes={levelErrorTypes}
              />
              <Chart ref={chartRef} type="bar" data={updatedState.dashboardData} options={dashboardOptions} />
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
