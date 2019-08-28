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

  const [dashboardData, setDashboardData] = useState({});
  const [dashboardOptions, setDashboardOptions] = useState({});

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
    setDashboardData({
      labels: ['FIRST', 'SECOND', 'FIRD', 'FOURTH', 'FIFTH', 'SIXTH', 'SEVENTH'],
      datasets: [
        {
          label: 'Correct',
          backgroundColor: '#004494',
          data: [50, 25, 12, 48, 90, 76, 85]
        },
        {
          label: 'Warning',
          backgroundColor: '#ffd617',
          data: [21, 84, 24, 75, 37, 65, 85]
        },
        {
          label: 'Error',
          backgroundColor: '#DA2131',
          data: [41, 52, 24, 74, 23, 21, 85]
        }
      ]
    });

    setDashboardOptions({
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
            stacked: true
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
    <div className="rep-row">
      <React.Fragment>
        <h1>Title Hardcoded</h1>
        <Chart type="bar" data={dashboardData} options={dashboardOptions} width="50%" height="35%" />
      </React.Fragment>
    </div>
  );
});
