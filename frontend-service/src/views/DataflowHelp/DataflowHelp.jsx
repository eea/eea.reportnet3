/* eslint-disable react-hooks/exhaustive-deps */
import { Fragment, useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

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
import { TextUtils } from 'repositories/_utils/TextUtils';

export const DataflowHelp = withRouter(({ history, match }) => {
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
  const [isBusinessDataflow, setIsBusinessDataflow] = useState(false);
  const [isCustodian, setIsCustodian] = useState(false);
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
      setIsCustodian(
        userRoles.includes(config.permissions.roles.CUSTODIAN.key) ||
          userRoles.includes(config.permissions.roles.STEWARD.key) ||
          userRoles.includes(config.permissions.roles.EDITOR_WRITE.key) ||
          userRoles.includes(config.permissions.roles.EDITOR_READ.key)
      );

      setIsToolbarVisible(
        userRoles.includes(config.permissions.roles.CUSTODIAN.key) ||
          userRoles.includes(config.permissions.roles.STEWARD.key)
      );
    }
  }, [userContext]);

  useBreadCrumbs({ currentPage: CurrentPage.DATAFLOW_HELP, dataflowId, history, isBusinessDataflow, isLoading });

  useEffect(() => {
    leftSideBarContext.addHelpSteps(
      isCustodian ? DataflowHelpRequesterHelpConfig : DataflowHelpReporterHelpConfig,
      'dataflowHelpHelp'
    );
  }, [documents, webLinks, datasetsSchemas, selectedIndex]);

  useEffect(() => {
    fetchDocumentsData();
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
      const data = await DataflowService.getDataflowDetails(dataflowId);
      setDataflowName(data.name);
    } catch (error) {
      console.error('DataflowHelp - getDataflowName.', error);
      notificationContext.add({ type: 'DATAFLOW_DETAILS_ERROR', content: {} });
    }
  };

  const onLoadDatasetSchema = async datasetId => {
    try {
      const datasetSchema = await DatasetService.getSchema(datasetId);

      if (!isEmpty(datasetSchema)) {
        if (isCustodian) {
          const datasetMetaData = await DatasetService.getMetaData(datasetId);
          datasetSchema.datasetSchemaName = datasetMetaData.datasetSchemaName;
        }
        return datasetSchema;
      }
    } catch (error) {
      console.error('DataflowHelp - onLoadDatasetSchema.', error);
      notificationContext.add({ type: 'IMPORT_DESIGN_FAILED_EVENT' });
    }
  };

  const onLoadDatasetsSchemas = async () => {
    try {
      const data = await DataflowService.getReportingDatasets(dataflowId);
      setIsBusinessDataflow(TextUtils.areEquals(data.type, config.dataflowType.BUSINESS)); // TODO TEST WITH REAL DATA
      setIsLoading(false);
      if (!isCustodian) {
        if (!isEmpty(data.datasets)) {
          const allDatasets = [...data.referenceDatasets, ...data.datasets];
          const uniqueDatasetSchemas = allDatasets.filter((dataset, pos, arr) => {
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
      console.error('DataflowHelp - onLoadDatasetsSchemas.', error);
      notificationContext.add({ type: 'LOAD_DATASETS_ERROR', content: {} });
    }
  };

  const sortByProperty = propertyName => (a, b) => a[propertyName].localeCompare(b[propertyName]);

  const onLoadDocuments = async () => {
    setIsLoadingDocuments(true);
    try {
      const documents = await DocumentService.getAll(`${dataflowId}`);
      const loadedDocuments = documents.sort(sortByProperty('description'));
      setDocuments(loadedDocuments);
    } catch (error) {
      console.error('DataflowHelp - onLoadDocuments.', error);
      notificationContext.add({ type: 'LOAD_DOCUMENTS_ERROR', content: {} });
      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        history.push(getUrl(routes.DATAFLOWS));
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
      notificationContext.add({ type: 'LOAD_WEB_LINKS_ERROR', content: {} });
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
        <Title icon="info" iconSize="3.5rem" subtitle={dataflowName} title={`${resources.messages['dataflowHelp']} `} />
        <TabView activeIndex={0} hasQueryString={false} name="DataflowHelp" onTabClick={e => setSelectedIndex(e)}>
          <TabPanel
            header={resources.messages['supportingDocuments']}
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
          <TabPanel header={resources.messages['webLinks']} headerClassName="dataflowHelp-webLinks-help-step">
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
            header={resources.messages['datasetSchemas']}
            headerClassName="dataflowHelp-schemas-help-step"
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
    return null;
  }
});
