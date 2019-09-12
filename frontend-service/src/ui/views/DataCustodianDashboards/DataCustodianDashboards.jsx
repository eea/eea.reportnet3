import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isEmpty } from 'lodash';

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

  function getDatasetsDashboardData() {
    const dashboardStatus = dashboardsData.dataSetCountries.map(status => {
      return {
        error: status.error.length,
        warning: status.warning.length,
        correct: status.correct.length
      };
    });

    const datasetDataObject = {
      labels: dashboardsData.dataSetCountries.map(countryData => countryData.countryName),
      datasets: [
        {
          label: 'Correct',
          backgroundColor: '#004494',
          data: dashboardStatus.map(cor => cor.correct)
        },
        {
          label: 'Warning',
          backgroundColor: '#ffd617',
          data: dashboardStatus.map(war => war.warning)
        },
        {
          label: 'Error',
          backgroundColor: '#DA2131',
          data: dashboardStatus.map(err => err.error)
        }
      ]
    };
    return datasetDataObject;
  }
  function getDatasetsDashboardOptions() {
    const datasetOptionsObject = {
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
          backgroundColor: '#32CD32',
          data: dashboardsData.dataSetCountries.map(released => released.isDataSetReleased)
        },
        {
          label: 'Unreleased',
          backgroundColor: '#8FBC8F',
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
