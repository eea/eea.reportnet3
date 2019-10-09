import React, { useEffect, useContext, useState, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import { isEmpty, isUndefined } from 'lodash';

import styles from './DataCustodianDashboards.module.scss';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Chart } from 'primereact/chart';
import { FilterList } from './_components/FilterList/FilterList';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/DataFlow';
import { GlobalReleasedDashboard } from 'ui/views/_components/GlobalReleasedDashboard/';
import { GlobalValidationDashboard } from 'ui/views/_components/GlobalValidationDashboard/';
import { getUrl } from 'core/infrastructure/api/getUrl';

export const DataCustodianDashboards = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataflowName, setDataflowName] = useState({});

  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(routes.DATAFLOWS))
  };

  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages.dataflow,
        command: () => history.push(`/dataflow/${match.params.dataflowId}`)
      },
      {
        label: resources.messages.dataCustodianDashboards
      }
    ]);
  }, []);

  useEffect(() => {
    try {
      getDataflowName();
    } catch (error) {
      console.error(error.response);
    }
  }, []);

  const getDataflowName = async () => {
    const dataflowName = await DataflowService.dataflowDetails(match.params.dataflowId);
    setDataflowName(dataflowName);
  };

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  const DataflowTitle = () => {
    return (
      <div className="rep-row">
        <h1>
          {resources.messages['dataflow']}: {dataflowName.name}
        </h1>
      </div>
    );
  };

  return layout(
    <>
      <DataflowTitle />
      <GlobalValidationDashboard dataflowId={match.params.dataflowId} />
      <GlobalReleasedDashboard dataflowId={match.params.dataflowId} />
    </>
  );
});
