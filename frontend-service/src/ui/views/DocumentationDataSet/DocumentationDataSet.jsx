/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined, isEmpty, sortBy } from 'lodash';

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
import { DatasetService } from 'core/services/DataSet';
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
  const [sortFieldDocuments, setSortFieldDocuments] = useState();
  const [sortOrderDocuments, setSortOrderDocuments] = useState();
  const [sortFieldWeblinks, setSortFieldWeblinks] = useState();
  const [sortOrderWeblinks, setSortOrderWeblinks] = useState();
  const [webLinks, setWebLinks] = useState([]);
  const [designDatasets, setDesignDatasets] = useState([]);

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
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow'],
        icon: 'archive',
        href: getUrl(
          routes.DATAFLOW,
          {
            dataflowId: match.params.dataflowId
          },
          true
        ),
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
      { label: resources.messages['dataflowHelp'], icon: 'info' }
    ]);
  }, [history, match.params.dataflowId, resources.messages]);

  useEffect(() => {
    setIsLoading(true);
    try {
      getDataflowName();
      onLoadDocuments();
      onLoadWebLinks();
      onLoadDesignDatasets();
    } catch (error) {
      console.error(error.response);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const getDataflowName = async () => {
    const dataflowData = await DataflowService.dataflowDetails(match.params.dataflowId);
    setDataflowName(dataflowData.name);
  };

  const onLoadWebLinks = async () => {
    try {
      let loadedWebLinks = await WebLinkService.all(match.params.dataflowId);
      loadedWebLinks = sortBy(loadedWebLinks, ['WebLink', 'id']);
      setWebLinks(loadedWebLinks);
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        console.error('error', error.response);
      }
    }
  };

  const onLoadDocuments = async () => {
    try {
      let loadedDocuments = await DocumentService.all(`${match.params.dataflowId}`);
      loadedDocuments = sortBy(loadedDocuments, ['Document', 'id']);
      setDocuments(loadedDocuments);
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS));
        console.error('error', error.response);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const onLoadDesignDatasets = async () => {
    try {
      const dataflow = await DataflowService.reporting(match.params.dataflowId);
      if (!isEmpty(dataflow.designDatasets)) {
        const datasetSchemas = dataflow.designDatasets.map(async designDataset => {
          return await onLoadDatasetDesignSchema(designDataset.datasetId);
        });
        Promise.all(datasetSchemas).then(completed => {
          setDesignDatasets(completed);
        });
      }
    } catch (error) {
      // if (error.response.status === 401 || error.response.status === 403) {
      //   history.push(getUrl(routes.DATAFLOWS));
      // }
    } finally {
      setIsLoading(false);
    }
  };

  const onLoadDatasetDesignSchema = async datasetId => {
    try {
      const datasetSchema = await DatasetService.schemaById(datasetId);
      if (!isEmpty(datasetSchema)) {
        const datasetMetaData = await DatasetService.getMetaData(datasetId);
        datasetSchema.datasetSchemaName = datasetMetaData.datasetSchemaName;
        return datasetSchema;
      }
    } catch (error) {
      // if (error.response.status === 401 || error.response.status === 403) {
      //   history.push(getUrl(routes.DATAFLOWS));
      // }
    } finally {
    }
  };

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} />
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
        <Title title={`${resources.messages['dataflowHelp']} `} subtitle={dataflowName} icon="info" iconSize="3.5rem" />
        <TabView activeIndex={0}>
          <TabPanel header={resources.messages['supportingDocuments']}>
            <Documents
              onLoadDocuments={onLoadDocuments}
              match={match}
              documents={documents}
              isCustodian={isCustodian}
              sortFieldDocuments={sortFieldDocuments}
              setSortFieldDocuments={setSortFieldDocuments}
              sortOrderDocuments={sortOrderDocuments}
              setSortOrderDocuments={setSortOrderDocuments}
            />
          </TabPanel>
          <TabPanel header={resources.messages['webLinks']}>
            <WebLinks
              onLoadWebLinks={onLoadWebLinks}
              isCustodian={isCustodian}
              dataflowId={match.params.dataflowId}
              webLinks={webLinks}
              sortFieldWeblinks={sortFieldWeblinks}
              setSortFieldWeblinks={setSortFieldWeblinks}
              sortOrderWeblinks={sortOrderWeblinks}
              setSortOrderWeblinks={setSortOrderWeblinks}
            />
          </TabPanel>
          <TabPanel header={resources.messages['datasetSchemas']}>
            <DatasetSchemas designDatasets={designDatasets} onLoadDesignDatasets={onLoadDesignDatasets} />
          </TabPanel>
        </TabView>
      </React.Fragment>
    );
  } else {
    return <></>;
  }
});
