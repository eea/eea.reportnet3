import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './DataCustodianDashboards.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataFlowService } from 'core/services/DataFlow';
import { UserContext } from '../_components/_context/UserContext';

import { Chart } from 'primereact/chart';

export const DataCustodianDashboards = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const userData = useContext(UserContext);
  const [loading, setLoading] = useState(true);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);

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
        label: resources.messages.dataCustodianDashboards
      }
    ]);
  }, [history, match.params.dataFlowId, resources.messages]);

  useEffect(() => {
    setLoading(true);
    setDatasetsDashboardData({
      labels: [
        'Austria',
        'Belgium',
        'Bulgaria',
        'Croatia',
        'Cyprus',
        'Denmark',
        'Estonia',
        'Finland',
        'France',
        'Germany',
        'Greece',
        'Hungary',
        'Ireland',
        'Italy',
        'Latvia',
        'Lithuania',
        'Luxembourg',
        'Malta',
        'Netherlands',
        'Poland',
        'Portugal',
        'Romania',
        'Slovakia',
        'Slovenia',
        'Spain',
        'Sweeden',
        'United Kingdom'
      ],
      datasets: [
        {
          label: 'Correct',
          backgroundColor: '#004494',
          data: [
            50,
            25,
            70,
            95,
            90,
            100,
            5,
            33,
            50,
            25,
            70,
            95,
            90,
            100,
            5,
            33,
            50,
            25,
            70,
            95,
            90,
            100,
            5,
            33,
            100,
            85,
            0
          ]
        },
        {
          label: 'Warning',
          backgroundColor: '#ffd617',
          data: [25, 25, 20, 5, 5, 0, 10, 34, 25, 25, 20, 5, 5, 0, 10, 34, 25, 25, 20, 5, 5, 0, 10, 34, 0, 15, 1]
        },
        {
          label: 'Error',
          backgroundColor: '#DA2131',
          data: [25, 50, 10, 0, 5, 0, 85, 33, 25, 50, 10, 0, 5, 0, 85, 33, 25, 50, 10, 0, 5, 0, 85, 33, 0, 0, 99]
        }
      ]
    });

    setDatasetsDashboardOptions({
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
              callback: (value, index, values) => `${value} %`
            }
          }
        ]
      }
    });

    setReleasedDashboardData({
      labels: [
        'Austria',
        'Belgium',
        'Bulgaria',
        'Croatia',
        'Cyprus',
        'Denmark',
        'Estonia',
        'Finland',
        'France',
        'Germany',
        'Greece',
        'Hungary',
        'Ireland',
        'Italy',
        'Latvia',
        'Lithuania',
        'Luxembourg',
        'Malta',
        'Netherlands',
        'Poland',
        'Portugal',
        'Romania',
        'Slovakia',
        'Slovenia',
        'Spain',
        'Sweeden',
        'United Kingdom'
      ],
      datasets: [
        {
          label: 'Released',
          backgroundColor: '#32CD32',
          data: [
            50,
            75,
            25,
            100,
            0,
            30,
            20,
            45,
            35,
            80,
            50,
            75,
            25,
            100,
            0,
            30,
            20,
            45,
            35,
            80,
            50,
            75,
            25,
            0,
            100,
            30,
            20
          ]
        },
        {
          label: 'Unreleased',
          backgroundColor: '#8FBC8F',
          data: [
            50,
            25,
            75,
            0,
            100,
            70,
            80,
            55,
            65,
            20,
            50,
            25,
            75,
            0,
            100,
            70,
            80,
            55,
            65,
            20,
            50,
            25,
            75,
            100,
            0,
            70,
            80
          ]
        }
      ]
    });

    setReleasedDashboardOptions({
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
              labelString: 'Released'
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
              callback: (value, index, values) => `${value} %`
            }
          }
        ]
      }
    });

    setLoading(false);
  }, []);

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
        <h1>Title Hardcoded</h1>
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
