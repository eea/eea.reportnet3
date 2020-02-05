/* eslint-disable react-hooks/exhaustive-deps */
import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isEmpty, isUndefined, sortBy } from 'lodash';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { DatasetSchemas } from './_components/DatasetSchemas';
import { Documents } from './_components/Documents';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel';
import { TabView } from 'ui/views/_components/TabView';
import { Title } from 'ui/views/_components/Title';
import { WebLinks } from './_components/WebLinks';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { DocumentService } from 'core/services/Document';
import { UserService } from 'core/services/User';
import { WebLinkService } from 'core/services/WebLink';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

import { getUrl } from 'core/infrastructure/CoreUtils';

export const DataflowHelp = withRouter(({ match, history }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [dataflowName, setDataflowName] = useState();
  const [datasetsSchemas, setDatasetsSchemas] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [isCustodian, setIsCustodian] = useState(false);
  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [isDeletingDocument, setIsDeletingDocument] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [sortFieldDocuments, setSortFieldDocuments] = useState();
  const [sortFieldWeblinks, setSortFieldWeblinks] = useState();
  const [sortOrderDocuments, setSortOrderDocuments] = useState();
  const [sortOrderWeblinks, setSortOrderWeblinks] = useState();
  const [webLinks, setWebLinks] = useState([]);

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setIsCustodian(
        UserService.hasPermission(
          user,
          [config.permissions.CUSTODIAN],
          `${config.permissions.DATAFLOW}${match.params.dataflowId}`
        )
      );
    }
  }, [user]);

  //Bread Crumbs settings
  useEffect(() => {
    breadCrumbContext.add([
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
    leftSideBarContext.addModels([]);
  }, []);

  useEffect(() => {
    setIsLoading(true);
    fetchDocumentsData();
    setIsLoading(false);
  }, [isCustodian, isDataUpdated]);

  useCheckNotifications(
    ['DELETE_DOCUMENT_FAILED_EVENT', 'DELETE_DOCUMENT_COMPLETED_EVENT'],
    setIsDeletingDocument,
    false
  );
  useCheckNotifications(
    ['UPLOAD_DOCUMENT_COMPLETED_EVENT', 'DELETE_DOCUMENT_COMPLETED_EVENT'],
    setIsDataUpdated,
    !isDataUpdated
  );

  const fetchDocumentsData = () => {
    getDataflowName();
    onLoadDocuments();
    onLoadWebLinks();
    onLoadDatasetsSchemas();
  };

  const getDataflowName = async () => {
    try {
      const dataflowData = await DataflowService.dataflowDetails(match.params.dataflowId);
      setDataflowName(dataflowData.name);
    } catch (error) {
      notificationContext.add({
        type: 'DATAFLOW_DETAILS_ERROR',
        content: {}
      });
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
      notificationContext.add({
        type: 'LOAD_SCHEMA_FAILED_EVENT',
        content: {
          datasetId
        }
      });
    } finally {
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
      notificationContext.add({
        type: 'LOAD_DATASETS_ERROR',
        content: {}
      });
    } finally {
      setIsLoading(false);
    }
  };

  const onLoadDocuments = async () => {
    try {
      let loadedDocuments = await DocumentService.all(`${match.params.dataflowId}`);
      loadedDocuments = sortBy(loadedDocuments, ['Document', 'id']);
      setDocuments(loadedDocuments);
    } catch (error) {
      notificationContext.add({
        type: 'LOAD_DOCUMENTS_ERROR',
        content: {}
      });
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setIsLoading(false);
    }
  };

  const onLoadWebLinks = async () => {
    try {
      let loadedWebLinks = await WebLinkService.all(match.params.dataflowId);
      loadedWebLinks = sortBy(loadedWebLinks, ['WebLink', 'id']);
      setWebLinks(loadedWebLinks);
    } catch (error) {
      notificationContext.add({
        type: 'LOAD_WEB_LINKS_ERROR',
        content: {}
      });
      if (error.response.status === 401 || error.response.status === 403) {
        console.error('error', error.response);
      }
    }
  };

  const layout = children => {
    return (
      <MainLayout>
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
              dataflowId={match.params.dataflowId}
              documents={documents}
              isCustodian={isCustodian}
              isDeletingDocument={isDeletingDocument}
              onLoadDocuments={onLoadDocuments}
              setIsDeletingDocument={setIsDeletingDocument}
              setSortFieldDocuments={setSortFieldDocuments}
              setSortOrderDocuments={setSortOrderDocuments}
              sortFieldDocuments={sortFieldDocuments}
              sortOrderDocuments={sortOrderDocuments}
            />
          </TabPanel>
          <TabPanel header={resources.messages['webLinks']}>
            <WebLinks
              dataflowId={match.params.dataflowId}
              isCustodian={isCustodian}
              onLoadWebLinks={onLoadWebLinks}
              setSortFieldWeblinks={setSortFieldWeblinks}
              setSortOrderWeblinks={setSortOrderWeblinks}
              sortFieldWeblinks={sortFieldWeblinks}
              sortOrderWeblinks={sortOrderWeblinks}
              webLinks={webLinks}
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
