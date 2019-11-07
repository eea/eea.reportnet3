import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Title } from 'ui/views/_components/Title';

import { DataflowService } from 'core/services/DataFlow';
import { GlobalReleasedDashboard } from 'ui/views/_components/GlobalReleasedDashboard/';
import { GlobalValidationDashboard } from 'ui/views/_components/GlobalValidationDashboard/';
import { getUrl } from 'core/infrastructure/api/getUrl';

export const DataCustodianDashboards = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataflowName, setDataflowName] = useState('');

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
        command: () =>
          history.push(
            getUrl(
              routes.DATAFLOW,
              {
                dataflowId: match.params.dataflowId
              },
              true
            )
          )
      },
      {
        label: resources.messages.dashboards
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
    const dataflowData = await DataflowService.dataflowDetails(match.params.dataflowId);
    setDataflowName(dataflowData.name);
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
          {resources.messages['dataflow']}: {dataflowName}
        </h1>
      </div>
    );
  };

  return layout(
    <>
      <Title title={`${resources.messages['dataflow']}: ${dataflowName}`} icon="barChart" />
      <GlobalValidationDashboard dataflowId={match.params.dataflowId} />
      <GlobalReleasedDashboard dataflowId={match.params.dataflowId} />
    </>
  );
});
