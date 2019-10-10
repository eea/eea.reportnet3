import React, { useState, useEffect, useContext } from 'react';
import { withRouter } from 'react-router-dom';

import isUndefined from 'lodash/isUndefined';

import styles from './Dashboard.module.css';

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
    const [datasetData, setDatasetData] = useState();
    const [isLoading, setIsLoading] = useState(false);

    const resources = useContext(ResourcesContext);

    useEffect(() => {
      setDashboardColors({
        CORRECT: '#004494',
        WARNING: '#ffd617',
        ERROR: '#DA2131'
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

    useEffect(() => {
      if (!isUndefined(datasetData)) {
        setDashboardData({
          labels: datasetData.tables.map(table => table.tableSchemaName),
          datasets: [
            {
              label: 'Correct',
              backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.CORRECT : '#004494',
              data: datasetData.tableStatisticPercentages[0],
              totalData: datasetData.tableStatisticValues
            },
            {
              label: 'Warning',
              backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.WARNING : '#ffd617',
              data: datasetData.tableStatisticPercentages[1],
              totalData: datasetData.tableStatisticValues
            },
            {
              label: 'Error',
              backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.ERROR : '#DA2131',
              data: datasetData.tableStatisticPercentages[2],
              totalData: datasetData.tableStatisticValues
            }
          ]
        });
      }
    }, [dashboardColors]);

    const onChangeColor = (color, type) => {
      const inmDashboardColors = { ...dashboardColors };
      inmDashboardColors[Object.keys(SEVERITY_CODE)[type - 1]] = `#${color}`;
      setDashboardColors(inmDashboardColors);
    };

    const onLoadStatistics = async () => {
      setIsLoading(true);
      const dataset = await DatasetService.errorStatisticsById(datasetId);
      const tableStatisticValues = dataset.tableStatisticValues;
      const tableNames = dataset.tables.map(table => table.tableSchemaName);
      setDatasetData(dataset);
      setDashboardTitle(dataset.datasetSchemaName);
      setDashboardData({
        labels: tableNames,
        datasets: [
          {
            label: 'Correct',
            backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.CORRECT : '#004494',
            data: dataset.tableStatisticPercentages[0],
            totalData: tableStatisticValues
          },
          {
            label: 'Warning',
            backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.WARNING : '#ffd617',
            data: dataset.tableStatisticPercentages[1],
            totalData: tableStatisticValues
          },
          {
            label: 'Error',
            backgroundColor: !isUndefined(dashboardColors) ? dashboardColors.ERROR : '#DA2131',
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
        <div className={styles.dashboardWraper}>
          <fieldset className={styles.colorPickerWrap}>
            <legend>{resources.messages['colorPickerMessage']}</legend>
            {Object.keys(SEVERITY_CODE).map((type, i) => {
              return (
                <React.Fragment key={i}>
                  <span key={`label_${type}`}>{`  ${type.charAt(0).toUpperCase()}${type
                    .slice(1)
                    .toLowerCase()}: `}</span>
                  <ColorPicker
                    key={type}
                    value={!isUndefined(dashboardColors) ? dashboardColors[type] : '#004494'}
                    onChange={e => {
                      e.preventDefault();
                      onChangeColor(e.value, SEVERITY_CODE[type]);
                    }}
                  />
                </React.Fragment>
              );
            })}
          </fieldset>
        </div>
      </React.Fragment>
    );
  })
);

export { Dashboard };
