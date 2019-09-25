import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isEmpty, isArray } from 'lodash';

import styles from './DataCustodianDashboards.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Chart } from 'primereact/chart';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataFlowService } from 'core/services/DataFlow';
import { UserContext } from '../_components/_context/UserContext';

export const DataCustodianDashboards = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const userData = useContext(UserContext);
  const [loading, setLoading] = useState(true);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);

  const [dashboardsData, setDashboardsData] = useState({});
  const [datasetsDashboardData, setDatasetsDashboardData] = useState({});
  const [datasetsDashboardOptions, setDatasetsDashboardOptions] = useState({});
  const [releasedDashboardData, setReleasedDashboardData] = useState({});
  const [releasedDashboardOptions, setReleasedDashboardOptions] = useState({});

  const home = {
    icon: config.icons['home'],
    command: () => history.push('/')
  };

  const onNewLegend = function(e, legendItem) {
    let datasetIndex = legendItem.datasetIndex;

    let currentChart = this.chart;

    let arrLenght = currentChart.data.datasets.length;

    let arrLegendSameTypePositions = [];

    for (let i = datasetIndex; i >= 0; ) {
      arrLegendSameTypePositions.push(i);
      i -= 3;
    }
    for (let i = datasetIndex; i < arrLenght; ) {
      if (!arrLegendSameTypePositions.includes(i)) {
        arrLegendSameTypePositions.push(i);
      }
      i += 3;
    }

    arrLegendSameTypePositions
      .map(index => currentChart.getDatasetMeta(index))
      .forEach(meta => {
        meta.hidden = meta.hidden === null ? !currentChart.data.datasets[datasetIndex].hidden : null;
      });
    currentChart.update();
  };

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataFlowList'],
        command: () => history.push('/data-flow-task')
      },
      {
        label: resources.messages.reportingDataFlow,
        command: () => history.push(`/reporting-data-flow/${match.params.dataFlowId}`)
      },
      {
        label: resources.messages.dataCustodianDashboards
      }
    ]);
  }, []);

  const loadDashboards = async () => {
    try {
      setDashboardsData(await DataFlowService.dashboards(match.params.dataFlowId));
    } catch (error) {
      console.error(error.response);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboards();
  }, []);

  const tableNames = isArray(dashboardsData.tables) && dashboardsData.tables.map(table => table.tableName);

  const tablePercentages =
    isArray(dashboardsData.tables) && dashboardsData.tables.map(table => table.tableStatisticPercentages);
  const tableOnePercentages = tablePercentages[0];
  const tableTwoPercentages = tablePercentages[1];
  const tableThirdPercentages = tablePercentages[2];

  const tableValues =
    isArray(dashboardsData.tables) && dashboardsData.tables.map(values => values.tableStatisticValues);
  const tableOneValues = tableValues[0];
  const tableTwoValues = tableValues[1];
  const tableThirdValues = tableValues[2];

  function getDatasetsDashboardData() {
    const datasetDataObject = {
      labels: dashboardsData.dataSetCountries.map(countryData => countryData.countryName),
      datasets: [
        {
          label: `CORRECT`,
          tableName: tableNames[0],
          backgroundColor: '#004494',
          data: tableOnePercentages[0],
          totalData: tableOneValues[0],
          stack: tableNames[0]
        },
        {
          label: `WARNINGS`,
          tableName: tableNames[0],
          backgroundColor: '#ffd617',
          data: tableOnePercentages[1],
          totalData: tableOneValues[1],
          stack: tableNames[0]
        },
        {
          label: `ERRORS`,
          tableName: tableNames[0],
          backgroundColor: '#DA2131',
          data: tableOnePercentages[2],
          totalData: tableOneValues[2],
          stack: tableNames[0]
        },
        {
          label: `CORRECT`,
          tableName: tableNames[1],
          backgroundColor: '#004494',
          data: tableTwoPercentages[0],
          totalData: tableTwoValues[0],
          stack: tableNames[1]
        },
        {
          label: `WARNINGS`,
          tableName: tableNames[1],
          backgroundColor: '#ffd617',
          data: tableTwoPercentages[1],
          totalData: tableTwoValues[1],
          stack: tableNames[1]
        },
        {
          label: `ERRORS`,
          tableName: tableNames[1],
          backgroundColor: '#DA2131',
          data: tableTwoPercentages[2],
          totalData: tableTwoValues[2],
          stack: tableNames[1]
        },
        {
          label: `CORRECT`,
          tableName: tableNames[2],
          backgroundColor: '#004494',
          data: tableThirdPercentages[0],
          totalData: tableThirdValues[0],
          stack: tableNames[2]
        },
        {
          label: `WARNINGS`,
          tableName: tableNames[2],
          backgroundColor: '#ffd617',
          data: tableThirdPercentages[1],
          totalData: tableThirdValues[1],
          stack: tableNames[2]
        },
        {
          label: `ERRORS`,
          tableName: tableNames[2],
          backgroundColor: '#DA2131',
          data: tableThirdPercentages[2],
          totalData: tableThirdValues[2],
          stack: tableNames[2]
        }
      ]
    };
    return datasetDataObject;
  }

  function getDatasetsDashboardOptions() {
    const datasetOptionsObject = {
      tooltips: {
        model: 'index',
        intersect: false,
        callbacks: {
          label: (tooltipItems, data) =>
            `${data.datasets[tooltipItems.datasetIndex].tableName}: ${
              data.datasets[tooltipItems.datasetIndex].totalData[tooltipItems.index]
            } (${tooltipItems.yLabel}%)`
        }
      },
      legend: {
        onClick: onNewLegend,

        display: true,
        labels: {
          filter: legendItem =>
            legendItem.datasetIndex == 0 || legendItem.datasetIndex == 1 || legendItem.datasetIndex == 2
        }
      },
      responsive: true,
      scales: {
        xAxes: [
          {
            stacked: true
          }
        ],
        yAxes: [
          {
            stacked: true,
            ticks: {
              // Include a % sign in the ticks
              callback: (value, index, values) => `${value}%`
            }
          }
        ]
      }
    };
    return datasetOptionsObject;
  }
  function getReleasedDashboardData() {
    const releasedDataObject = {
      labels: dashboardsData.dataSetCountries.map(countryData => countryData.countryName),
      datasets: [
        {
          label: 'Released',
          backgroundColor: 'rgb(50, 205, 50)',

          data: dashboardsData.dataSetCountries.map(released => released.isDataSetReleased)
        },
        {
          label: 'Unreleased',
          backgroundColor: 'rgb(255, 99, 132)',
          data: dashboardsData.dataSetCountries.map(released => !released.isDataSetReleased)
        }
      ]
    };
    return releasedDataObject;
  }
  function getReleasedDashboardOptions() {
    const releasedOptionsObject = {
      tooltips: {
        mode: 'index',
        intersect: false
      },
      responsive: true,
      scales: {
        xAxes: [
          {
            stacked: true
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
    return releasedOptionsObject;
  }

  const onPageLoad = () => {
    setDatasetsDashboardData(getDatasetsDashboardData());
    setDatasetsDashboardOptions(getDatasetsDashboardOptions());
    setReleasedDashboardData(getReleasedDashboardData());
    setReleasedDashboardOptions(getReleasedDashboardOptions());
  };

  useEffect(() => {
    if (!isEmpty(dashboardsData)) {
      onPageLoad();
    }
  }, [dashboardsData]);

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <>
      <div className="rep-row">
        <h1>{resources.messages['dataFlow']}</h1>
      </div>
      <div className="rep-row">
        <Chart type="bar" data={datasetsDashboardData} options={datasetsDashboardOptions} width="100%" height="35%" />
      </div>
      <div className={`rep-row ${styles.chart_released}`}>
        <Chart type="bar" data={releasedDashboardData} options={releasedDashboardOptions} width="100%" height="25%" />
      </div>
    </>
  );
});
