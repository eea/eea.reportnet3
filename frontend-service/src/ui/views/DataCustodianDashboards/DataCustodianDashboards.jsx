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

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataFlowTask'],
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
          label: tableNames[0],
          backgroundColor: '#004494',
          data: tableOnePercentages[0],
          totalData: tableOneValues[0],
          stack: tableNames[0]
        },
        {
          label: tableNames[0],
          backgroundColor: '#ffd617',
          data: tableOnePercentages[1],
          totalData: tableOneValues[1],
          stack: tableNames[0]
        },
        {
          label: tableNames[0],
          backgroundColor: '#DA2131',
          data: tableOnePercentages[2],
          totalData: tableOneValues[2],
          stack: tableNames[0]
        },
        {
          label: tableNames[1],
          backgroundColor: '#004494',
          data: tableTwoPercentages[0],
          totalData: tableTwoValues[0],
          stack: tableNames[1]
        },
        {
          label: tableNames[1],
          backgroundColor: '#ffd617',
          data: tableTwoPercentages[1],
          totalData: tableTwoValues[1],
          stack: tableNames[1]
        },
        {
          label: tableNames[1],
          backgroundColor: '#DA2131',
          data: tableTwoPercentages[2],
          totalData: tableTwoValues[2],
          stack: tableNames[1]
        },
        {
          label: tableNames[2],
          backgroundColor: '#004494',
          data: tableThirdPercentages[0],
          totalData: tableThirdValues[0],
          stack: tableNames[2]
        },
        {
          label: tableNames[2],
          backgroundColor: '#ffd617',
          data: tableThirdPercentages[1],
          totalData: tableThirdValues[1],
          stack: tableNames[2]
        },
        {
          label: tableNames[2],
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
            `${data.datasets[tooltipItems.datasetIndex].label}: ${
              data.datasets[tooltipItems.datasetIndex].totalData[tooltipItems.index]
            } (${tooltipItems.yLabel}%)`
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
              labelString: 'Datasets'
            }
          }
        ],
        yAxes: [
          {
            stacked: true,
            /*  scaleLabel: {
              display: true,
              labelString: 'Percentage'
            }, */
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
          backgroundColor: 'rgba(50, 205, 50, 0.7)',
          borderColor: 'rgba(50, 205, 50, 1)',
          data: dashboardsData.dataSetCountries.map(released => released.isDataSetReleased)
        },
        {
          label: 'Unreleased',
          backgroundColor: '#8FBC8F',
          backgroundColor: 'rgba(143, 188, 143, 0.7)',
          borderColor: 'rgba(143, 188, 143, 1)',
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
            stacked: true,
            scaleLabel: {
              display: true,
              labelString: 'Datasets'
            }
          }
        ],
        yAxes: [
          {
            stacked: true,
            /*  scaleLabel: {
              display: true,
              labelString: 'Percentage'
            }, */
            ticks: {
              // Include a % sign in the ticks
              callback: (value, index, values) => `${value}`
            }
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
        <Chart type="bar" data={releasedDashboardData} options={releasedDashboardOptions} width="100%" height="35%" />
      </div>
    </>
  );
});
