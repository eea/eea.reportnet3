/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './DocumentationDataSet.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Documents } from './_components/Documents';
import { Growl } from 'primereact/growl';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView } from 'primereact/tabview';
import { TabPanel } from 'primereact/tabview';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';
import { WebLinks } from './_components/WebLinks';

import { DocumentService } from 'core/services/Document';
import { WebLinkService } from 'core/services/WebLink';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { routes } from 'ui/routes';

export const DocumentationDataset = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);

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

  useEffect(() => {
    onLoadDocumentsAndWebLinks();
  }, []);

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow'],
        command: () => history.push(`/dataflow/${match.params.dataflowId}`)
      },
      { label: resources.messages['documents'] }
    ]);
  }, [history, match.params.dataflowId, resources.messages]);

  const onLoadDocumentsAndWebLinks = async () => {
    setIsLoading(true);
    try {
      setWebLinks(await WebLinkService.all(`${match.params.dataflowId}`));
      setDocuments(await DocumentService.all(`${match.params.dataflowId}`));
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setIsLoading(false);
    }
  };

  const onGrowlAlert = message => {
    growlRef.current.show(message);
  };

  let growlRef = useRef();

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
        <Growl ref={growlRef} />
      </MainLayout>
    );
  };

  if (isLoading) {
    return layout(<Spinner />);
  }

  if (documents) {
    return layout(
      <TabView>
        <TabPanel header={resources.messages['documents']}>
          <Documents
            onLoadDocumentsAndWebLinks={onLoadDocumentsAndWebLinks}
            match={match}
            documents={documents}
            isCustodian={isCustodian}
          />
        </TabPanel>
        <TabPanel header={resources.messages['webLinks']}>
          <WebLinks
            onLoadDocumentsAndWebLinks={onLoadDocumentsAndWebLinks}
            webLinks={webLinks}
            isCustodian={isCustodian}
          />
        </TabPanel>
      </TabView>
    );
  } else {
    return <></>;
  }
});
