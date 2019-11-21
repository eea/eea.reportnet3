/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined } from 'lodash';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { DatasetSchemas } from './_components/DatasetSchemas';
import { Documents } from './_components/Documents';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView } from 'ui/views/_components/TabView';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel';
import { Title } from 'ui/views/_components/Title';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';
import { WebLinks } from './_components/WebLinks';

import { DataflowService } from 'core/services/DataFlow';
import { DocumentService } from 'core/services/Document';
import { WebLinkService } from 'core/services/WebLink';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { routes } from 'ui/routes';

export const DocumentationDataset = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);

  const [dataflowName, setDataflowName] = useState();
  const [documents, setDocuments] = useState([]);
  const [isCustodian, setIsCustodian] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [webLinks, setWebLinks] = useState();

  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(routes.DATAFLOWS))
  };

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setIsCustodian(
        UserService.hasPermission(
          user,
          [config.permissions.CUSTODIAN],
          `${config.permissions.DATA_FLOW}${match.params.dataflowId}`
        )
      );
    }
  }, [user]);

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow'],
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
      { label: resources.messages['dataflowHelp'] }
    ]);
  }, [history, match.params.dataflowId, resources.messages]);

  useEffect(() => {
    try {
      getDataflowName();
      onLoadDocumentsAndWebLinks();
    } catch (error) {
      console.error(error.response);
    }
  }, []);

  const getDataflowName = async () => {
    const dataflowData = await DataflowService.dataflowDetails(match.params.dataflowId);
    setDataflowName(dataflowData.name);
  };

  const onLoadDocumentsAndWebLinks = async () => {
    setIsLoading(true);
    try {
      setDocuments(await DocumentService.all(`${match.params.dataflowId}`));
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setIsLoading(false);
    }
  };

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (isLoading) {
    return layout(<Spinner />);
  }

  if (documents) {
    return layout(
      <React.Fragment>
        <Title title={`${resources.messages['dataflowHelp']} `} subtitle={dataflowName} icon="info" iconSize="3rem" />
        <TabView>
          <TabPanel header={resources.messages['supportingDocuments']}>
            <Documents
              onLoadDocumentsAndWebLinks={onLoadDocumentsAndWebLinks}
              match={match}
              documents={documents}
              isCustodian={isCustodian}
            />
          </TabPanel>
          <TabPanel header={resources.messages['webLinks']}>
            <WebLinks isCustodian={isCustodian} dataflowId={match.params.dataflowId} />
          </TabPanel>
          <TabPanel header={resources.messages['datasetSchemas']}>
            <DatasetSchemas dataflowId={match.params.dataflowId} />
          </TabPanel>
        </TabView>
      </React.Fragment>
    );
  } else {
    return <></>;
  }
});
