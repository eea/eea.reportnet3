/* eslint-disable react-hooks/exhaustive-deps */
import { Fragment, useContext, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';
import uniqBy from 'lodash/uniqBy';

import { config } from 'conf';
import { DataflowHelpReporterHelpConfig } from 'conf/help/dataflowHelp/reporter';
import { DataflowHelpRequesterHelpConfig } from 'conf/help/dataflowHelp/requester';
import { routes } from 'conf/routes';

import { DatasetSchemas } from './_components/DatasetSchemas';
import { Documents } from './_components/Documents';
import { MainLayout } from 'views/_components/Layout';
import { TabPanel } from 'views/_components/TabView/_components/TabPanel';
import { TabView } from 'views/_components/TabView';
import { Title } from 'views/_components/Title';
import { WebLinks } from './_components/WebLinks';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';
import { DocumentService } from 'services/DocumentService';
import { WebLinkService } from 'services/WebLinkService';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

import { CurrentPage } from 'views/_functions/Utils';
import { getUrl } from 'repositories/_utils/UrlUtils';

export const DataflowHelp = () => {
  const navigate = useNavigate();
  const { dataflowId } = useParams();

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dataflowName, setDataflowName] = useState();
  const [dataflowType, setDataflowType] = useState('');
  const [datasetsSchemas, setDatasetsSchemas] = useState();
  const [documents, setDocuments] = useState([]);
  const [hasCustodianPermissions, setHasCustodianPermissions] = useState(false);
  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [isDeletingDocument, setIsDeletingDocument] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingDocuments, setIsLoadingDocuments] = useState(true);
  const [isLoadingSchemas, setIsLoadingSchemas] = useState(true);
  const [isLoadingWebLinks, setIsLoadingWeblinks] = useState(false);
  const [isToolbarVisible, setIsToolbarVisible] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [sortFieldDocuments, setSortFieldDocuments] = useState();
  const [sortFieldWebLinks, setSortFieldWebLinks] = useState();
  const [sortOrderDocuments, setSortOrderDocuments] = useState();
  const [sortOrderWebLinks, setSortOrderWebLinks] = useState();
  const [webLinks, setWebLinks] = useState([]);

  useEffect(() => {
    leftSideBarContext.removeModels();
  }, []);

  useEffect(() => {
    if (!isUndefined(userContext.contextRoles)) {
      const userRoles = userContext.getUserRole(`${config.permissions.prefixes.DATAFLOW}${dataflowId}`);
      setHasCustodianPermissions(
        userRoles.includes(config.permissions.roles.CUSTODIAN.key) ||
          userRoles.includes(config.permissions.roles.STEWARD.key) ||
          userRoles.includes(config.permissions.roles.EDITOR_WRITE.key) ||
          userRoles.includes(config.permissions.roles.EDITOR_READ.key)
      );

      setIsToolbarVisible(
        userRoles.includes(config.permissions.roles.CUSTODIAN.key) ||
          userRoles.includes(config.permissions.roles.STEWARD.key) ||
          userRoles.includes(config.permissions.roles.STEWARD_SUPPORT.key)
      );
    }
  }, [userContext]);

  useBreadCrumbs({ currentPage: CurrentPage.DATAFLOW_HELP, dataflowId, dataflowType, isLoading });

  useEffect(() => {
    leftSideBarContext.addHelpSteps(
      hasCustodianPermissions ? DataflowHelpRequesterHelpConfig : DataflowHelpReporterHelpConfig,
      'dataflowHelpHelp'
    );
  }, [documents, webLinks, datasetsSchemas, selectedIndex]);

  useEffect(() => {
    fetchDocumentsData();
  }, [isDataUpdated]);

  useEffect(() => {
    onLoadDatasetsSchemas();
  }, [hasCustodianPermissions]);

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
      const data = await DataflowService.getDetails(dataflowId);
      setDataflowName(data.name);
    } catch (error) {
      console.error('DataflowHelp - getDataflowName.', error);
      notificationContext.add({ type: 'DATAFLOW_DETAILS_ERROR', content: {} }, true);
    }
  };

  const onLoadDatasetSchema = async datasetId => {
    try {
      const datasetSchema = await DatasetService.getSchema(dataflowId, datasetId);

      if (!isEmpty(datasetSchema)) {
        if (hasCustodianPermissions) {
          const datasetMetadata = await DatasetService.getMetadata(datasetId);
          datasetSchema.datasetSchemaName = datasetMetadata.datasetSchemaName;
        }
        return datasetSchema;
      }
    } catch (error) {
      console.error('DataflowHelp - onLoadDatasetSchema.', error);
      notificationContext.add({ type: 'IMPORT_DESIGN_FAILED_EVENT' }, true);
    }
  };

  const onLoadDatasetsSchemas = async () => {
    try {
      const data = await DataflowService.get(dataflowId);
      setDataflowType(data.type);
      setIsLoading(false);
      if (!hasCustodianPermissions) {
        if (!isEmpty(data.datasets)) {
          const datasetSchemas = data.datasets.map(async datasetSchema => {
            return await onLoadDatasetSchema(datasetSchema.datasetId);
          });
          Promise.all(datasetSchemas).then(completed => {
            completed.forEach(datasetSchema => {
              datasetSchema.datasetId = data.datasets.find(
                dataset => dataset.datasetSchemaId === datasetSchema.datasetSchemaId
              ).datasetId;
            });

            setDatasetsSchemas(uniqBy(completed, 'datasetSchemaId'));
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
            completed.forEach(datasetSchema => {
              datasetSchema.datasetId = data.designDatasets.find(
                designDataset => designDataset.datasetSchemaId === datasetSchema.datasetSchemaId
              ).datasetId;
            });
            setDatasetsSchemas(uniqBy(completed, 'datasetSchemaId'));
          });
        } else {
          setIsLoadingSchemas(false);
        }
      }
    } catch (error) {
      console.error('DataflowHelp - onLoadDatasetsSchemas.', error);
      notificationContext.add({ type: 'LOAD_DATASETS_ERROR', content: {} }, true);
    }
  };

  const sortByProperty = propertyName => (a, b) => a[propertyName]?.localeCompare(b[propertyName]);

  const onLoadDocuments = async () => {
    setIsLoadingDocuments(true);
    try {
      const documents = await DocumentService.getAll(`${dataflowId}`);
      const loadedDocuments = documents.sort(sortByProperty('description'));
      setDocuments(loadedDocuments);
    } catch (error) {
      console.error('DataflowHelp - onLoadDocuments.', error);
      notificationContext.add({ type: 'LOAD_DOCUMENTS_ERROR', content: {} }, true);
      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        navigate(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setIsLoadingDocuments(false);
    }
  };

  const onLoadWebLinks = async () => {
    setIsLoadingWeblinks(true);
    try {
      const webLinks = await WebLinkService.getAll(dataflowId);
      const loadedWebLinks = webLinks.sort(sortByProperty('description'));
      setWebLinks(loadedWebLinks);
    } catch (error) {
      console.error('DataflowHelp - onLoadWebLinks.', error);
      notificationContext.add({ type: 'LOAD_WEB_LINKS_ERROR', content: {} }, true);
    } finally {
      setIsLoadingWeblinks(false);
    }
  };

  const renderLayout = children => (
    <MainLayout>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  if (documents) {
    return renderLayout(
      <Fragment>
        <Title
          icon="info"
          iconSize="3.5rem"
          subtitle={dataflowName}
          title={`${resourcesContext.messages['dataflowHelp']} `}
        />
        <TabView activeIndex={0} hasQueryString={false} name="DataflowHelp" onTabClick={e => setSelectedIndex(e)}>
          <TabPanel
            header={resourcesContext.messages['supportingDocuments']}
            headerClassName="dataflowHelp-documents-help-step">
            <Documents
              dataflowId={dataflowId}
              documents={documents}
              isDeletingDocument={isDeletingDocument}
              isLoading={isLoadingDocuments}
              isToolbarVisible={isToolbarVisible}
              onLoadDocuments={onLoadDocuments}
              setIsDeletingDocument={setIsDeletingDocument}
              setSortFieldDocuments={setSortFieldDocuments}
              setSortOrderDocuments={setSortOrderDocuments}
              sortFieldDocuments={sortFieldDocuments}
              sortOrderDocuments={sortOrderDocuments}
            />
          </TabPanel>
          <TabPanel header={resourcesContext.messages['webLinks']} headerClassName="dataflowHelp-webLinks-help-step">
            <WebLinks
              dataflowId={dataflowId}
              isLoading={isLoadingWebLinks}
              isToolbarVisible={isToolbarVisible}
              onLoadWebLinks={onLoadWebLinks}
              setSortFieldWebLinks={setSortFieldWebLinks}
              setSortOrderWebLinks={setSortOrderWebLinks}
              sortFieldWebLinks={sortFieldWebLinks}
              sortOrderWebLinks={sortOrderWebLinks}
              webLinks={webLinks}
            />
          </TabPanel>
          <TabPanel
            disabled={isEmpty(datasetsSchemas)}
            header={resourcesContext.messages['datasetSchemas']}
            headerClassName="dataflowHelp-schemas-help-step"
            rightIcon={isEmpty(datasetsSchemas) && isLoadingSchemas ? config.icons['spinnerAnimate'] : null}>
            <DatasetSchemas
              dataflowId={dataflowId}
              dataflowName={dataflowName}
              datasetsSchemas={datasetsSchemas}
              hasCustodianPermissions={hasCustodianPermissions}
              onLoadDatasetsSchemas={onLoadDatasetsSchemas}
            />
          </TabPanel>
        </TabView>
      </Fragment>
    );
  } else {
    return null;
  }
};
