/* eslint-disable react-hooks/exhaustive-deps */
import React, { Fragment, useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';
import { DataflowHelpReporterHelpConfig } from 'conf/help/dataflowHelp/reporter';
import { DataflowHelpRequesterHelpConfig } from 'conf/help/dataflowHelp/requester';
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
import { WebLinkService } from 'core/services/WebLink';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { getUrl } from 'core/infrastructure/CoreUtils';

export const DataflowHelp = withRouter(({ match, history }) => {
  const {
    params: { dataflowId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dataflowName, setDataflowName] = useState();
  const [datasetsSchemas, setDatasetsSchemas] = useState();
  const [documents, setDocuments] = useState([]);
  const [isCustodian, setIsCustodian] = useState(false);
  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [isDeletingDocument, setIsDeletingDocument] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingSchemas, setIsLoadingSchemas] = useState(true);
  const [isToolbarVisible, setIsToolbarVisible] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [sortFieldDocuments, setSortFieldDocuments] = useState();
  const [sortFieldWeblinks, setSortFieldWeblinks] = useState();
  const [sortOrderDocuments, setSortOrderDocuments] = useState();
  const [sortOrderWeblinks, setSortOrderWeblinks] = useState();
  const [webLinks, setWebLinks] = useState([]);

  useEffect(() => {
    leftSideBarContext.removeModels();
  }, []);

  useEffect(() => {
    if (!isUndefined(userContext.contextRoles)) {
      const userRoles = userContext.getUserRole(`${config.permissions.DATAFLOW}${dataflowId}`);
      setIsCustodian(
        userRoles.includes(config.permissions['DATA_CUSTODIAN']) ||
          userRoles.includes(config.permissions['DATA_STEWARD']) ||
          userRoles.includes(config.permissions['EDITOR_WRITE']) ||
          userRoles.includes(config.permissions['EDITOR_READ'])
      );

      setIsToolbarVisible(
        userRoles.includes(config.permissions['DATA_CUSTODIAN']) ||
          userRoles.includes(config.permissions['DATA_STEWARD'])
      );
    }
  }, [userContext]);

  useBreadCrumbs({ currentPage: CurrentPage.DATAFLOW_HELP, dataflowId, history });

  useEffect(() => {
    leftSideBarContext.addHelpSteps(
      isCustodian ? DataflowHelpRequesterHelpConfig : DataflowHelpReporterHelpConfig,
      'dataflowHelpHelp'
    );
  }, [documents, webLinks, datasetsSchemas, selectedIndex]);

  useEffect(() => {
    setIsLoading(true);
    fetchDocumentsData();
    setIsLoading(false);
  }, [isDataUpdated]);

  useEffect(() => {
    onLoadDatasetsSchemas();
  }, [isCustodian]);

  useCheckNotifications(
    ['DELETE_DOCUMENT_FAILED_EVENT', 'DELETE_DOCUMENT_COMPLETED_EVENT'],
    setIsDeletingDocument,
    false
  );

  useCheckNotifications(
    ['UPLOAD_DOCUMENT_COMPLETED_EVENT', 'UPDATED_DOCUMENT_COMPLETED_EVENT', 'DELETE_DOCUMENT_COMPLETED_EVENT'],
    setIsDataUpdated,
    !isDataUpdated
  );

  const fetchDocumentsData = () => {
    getDataflowName();
    onLoadDocuments();
    onLoadWebLinks();
  };

  const getDataflowName = async () => {
    try {
      const { data } = await DataflowService.dataflowDetails(dataflowId);
      setDataflowName(data.name);
    } catch (error) {
      notificationContext.add({ type: 'DATAFLOW_DETAILS_ERROR', content: {} });
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
        return datasetSchema;
      }
    } catch (error) {
      notificationContext.add({
        type: 'IMPORT_DESIGN_FAILED_EVENT',
        content: {
          datasetId
        }
      });
    } finally {
    }
  };

  const onLoadDatasetsSchemas = async () => {
    try {
      const { data } = await DataflowService.reporting(dataflowId);

      if (!isCustodian) {
        if (!isEmpty(data.datasets)) {
          const uniqueDatasetSchemas = data.datasets.filter((dataset, pos, arr) => {
            return arr.map(dataset => dataset.datasetSchemaId).indexOf(dataset.datasetSchemaId) === pos;
          });
          const datasetSchemas = uniqueDatasetSchemas.map(async datasetSchema => {
            return await onLoadDatasetSchema(datasetSchema.datasetId);
          });
          Promise.all(datasetSchemas).then(completed => {
            setDatasetsSchemas(completed);
          });
        } else {
          setIsLoadingSchemas(false);
        }
      } else {
        if (!isEmpty(data.designDatasets)) {
          const datasetSchemas = data.designDatasets.map(async designDataset => {
            return await onLoadDatasetSchema(designDataset.datasetId);
          });
          Promise.all(datasetSchemas).then(completed => {
            setDatasetsSchemas(completed);
          });
        } else {
          setIsLoadingSchemas(false);
        }
      }
    } catch (error) {
      notificationContext.add({ type: 'LOAD_DATASETS_ERROR', content: {} });
    } finally {
      setIsLoading(false);
    }
  };

  const sortByProperty = propertyName => (a, b) => a[propertyName].localeCompare(b[propertyName]);

  const onLoadDocuments = async () => {
    try {
      let loadedDocuments = await DocumentService.all(`${dataflowId}`);

      loadedDocuments = loadedDocuments.sort(sortByProperty('description'));

      setDocuments(loadedDocuments);
    } catch (error) {
      notificationContext.add({
        type: 'LOAD_DOCUMENTS_ERROR',
        content: {}
      });
      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setIsLoading(false);
    }
  };

  const onLoadWebLinks = async () => {
    try {
      let loadedWebLinks = await WebLinkService.all(dataflowId);
      loadedWebLinks = loadedWebLinks.sort(sortByProperty('description'));
      setWebLinks(loadedWebLinks);
    } catch (error) {
      notificationContext.add({
        type: 'LOAD_WEB_LINKS_ERROR',
        content: {}
      });
      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
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
      <Fragment>
        <Title title={`${resources.messages['dataflowHelp']} `} subtitle={dataflowName} icon="info" iconSize="3.5rem" />
        <TabView activeIndex={0} hasQueryString={false} onTabClick={e => setSelectedIndex(e)}>
          <TabPanel
            headerClassName="dataflowHelp-documents-help-step"
            header={resources.messages['supportingDocuments']}>
            <Documents
              dataflowId={dataflowId}
              documents={documents}
              isDeletingDocument={isDeletingDocument}
              isToolbarVisible={isToolbarVisible}
              onLoadDocuments={onLoadDocuments}
              setIsDeletingDocument={setIsDeletingDocument}
              setSortFieldDocuments={setSortFieldDocuments}
              setSortOrderDocuments={setSortOrderDocuments}
              sortFieldDocuments={sortFieldDocuments}
              sortOrderDocuments={sortOrderDocuments}
            />
          </TabPanel>
          <TabPanel headerClassName="dataflowHelp-weblinks-help-step" header={resources.messages['webLinks']}>
            <WebLinks
              dataflowId={dataflowId}
              isToolbarVisible={isToolbarVisible}
              onLoadWebLinks={onLoadWebLinks}
              setSortFieldWeblinks={setSortFieldWeblinks}
              setSortOrderWeblinks={setSortOrderWeblinks}
              sortFieldWeblinks={sortFieldWeblinks}
              sortOrderWeblinks={sortOrderWeblinks}
              webLinks={webLinks}
            />
          </TabPanel>
          <TabPanel
            disabled={isEmpty(datasetsSchemas)}
            headerClassName="dataflowHelp-schemas-help-step"
            header={resources.messages['datasetSchemas']}
            rightIcon={isEmpty(datasetsSchemas) && isLoadingSchemas ? config.icons['spinnerAnimate'] : null}>
            <DatasetSchemas
              dataflowId={dataflowId}
              datasetsSchemas={datasetsSchemas}
              isCustodian={isCustodian}
              onLoadDatasetsSchemas={onLoadDatasetsSchemas}
            />
          </TabPanel>
        </TabView>
      </Fragment>
    );
  } else {
    return <Fragment />;
  }
});
