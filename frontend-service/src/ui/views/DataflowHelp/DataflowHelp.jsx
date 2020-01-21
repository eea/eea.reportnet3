/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined, isEmpty, sortBy } from 'lodash';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { DatasetSchemas } from './_components/DatasetSchemas';
import { Documents } from './_components/Documents';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView } from 'ui/views/_components/TabView';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel';
import { Title } from 'ui/views/_components/Title';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';
import { WebLinks } from './_components/WebLinks';
import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { DocumentService } from 'core/services/Document';
import { WebLinkService } from 'core/services/WebLink';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

export const DataflowHelp = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const notificationContext = useContext(NotificationContext);
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
  const [datasetsSchemas, setDatasetsSchemas] = useState([]);

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

  const fetchDocumentsData = () => {
    setIsLoading(true);
    try {
      getDataflowName();
      onLoadDocuments();
      onLoadWebLinks();
      onLoadDatasetsSchemas();
    } catch (error) {
      console.error(error.response);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDocumentsData();
  }, [isCustodian]);

  useEffect(() => {
    const refresh = notificationContext.toShow.find(
      notification =>
        notification.key === 'UPLOAD_DOCUMENT_COMPLETED_EVENT' || notification.key === 'DELETE_DOCUMENT_COMPLETED_EVENT'
    );
    if (refresh) {
      fetchDocumentsData();
    }
  }, [notificationContext]);

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

  const onLoadDatasetsSchemas = async () => {
    try {
      const dataflow = await DataflowService.reporting(match.params.dataflowId);
      if (!isCustodian) {
        if (!isEmpty(dataflow.datasets)) {
          const uniqueDatasetSchemas = dataflow.datasets.filter((dataset, pos, arr) => {
            return arr.map(dataset => dataset.datasetSchemaId).indexOf(dataset.datasetSchemaId) === pos;
          });
          const datasetSchemas = uniqueDatasetSchemas.map(async datasetSchema => {
            return await onLoadDatasetSchema(datasetSchema.datasetId);
          });
          Promise.all(datasetSchemas).then(completed => {
            setDatasetsSchemas(completed);
          });
        }
      } else {
        if (!isEmpty(dataflow.designDatasets)) {
          const datasetSchemas = dataflow.designDatasets.map(async designDataset => {
            return await onLoadDatasetSchema(designDataset.datasetId);
          });
          Promise.all(datasetSchemas).then(completed => {
            setDatasetsSchemas(completed);
          });
        }
      }
    } catch (error) {
      // if (error.response.status === 401 || error.response.status === 403) {
      //   history.push(getUrl(routes.DATAFLOWS));
      // }
    } finally {
      setIsLoading(false);
    }
  };

  const onLoadDatasetSchema = async datasetId => {
    try {
      const datasetSchema = await DatasetService.schemaById(datasetId);
      if (!isEmpty(datasetSchema)) {
        if (isCustodian) {
          const datasetMetaData = await DatasetService.getMetaData(datasetId);
          datasetSchema.datasetSchemaName = datasetMetaData.datasetSchemaName;
        }
        // const datasetMetaData = await DatasetService.getMetaData(datasetId);
        // datasetSchema.datasetSchemaName = datasetMetaData.datasetSchemaName;
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
            <DatasetSchemas
              datasetsSchemas={datasetsSchemas}
              isCustodian={isCustodian}
              onLoadDatasetsSchemas={onLoadDatasetsSchemas}
            />
          </TabPanel>
        </TabView>
      </React.Fragment>
    );
  } else {
    return <></>;
  }
});
