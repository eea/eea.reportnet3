import { Fragment, useContext, useEffect, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import styles from './DataCollection.module.scss';

import { config } from 'conf';
import { DatasetConfig } from 'repositories/config/DatasetConfig';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { routes } from 'conf/routes';

import { Button } from 'views/_components/Button';
import { Growl } from 'views/_components/Growl';
import { MainLayout } from 'views/_components/Layout';
import { Menu } from 'views/_components/Menu';
import { Spinner } from 'views/_components/Spinner';
import { TabsSchema } from 'views/_components/TabsSchema';
import { Title } from 'views/_components/Title';
import { Toolbar } from 'views/_components/Toolbar';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

import { CurrentPage } from 'views/_functions/Utils';
import { MetadataUtils } from 'views/_functions/Utils';

export const DataCollection = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [dataCollectionName, setDataCollectionName] = useState();
  const [dataflowName, setDataflowName] = useState('');
  const [dataViewerOptions, setDataViewerOptions] = useState({ activeIndex: null });
  const [exportButtonsList, setExportButtonsList] = useState([]);
  const [isBusinessDataflow, setIsBusinessDataflow] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingFile, setIsLoadingFile] = useState(false);
  const [levelErrorTypes, setLevelErrorTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();

  let exportMenuRef = useRef();
  let growlRef = useRef();

  useBreadCrumbs({ currentPage: CurrentPage.DATA_COLLECTION, dataflowId, history, isBusinessDataflow, isLoading });

  useCheckNotifications(
    ['DOWNLOAD_EXPORT_DATASET_FILE_ERROR', 'EXPORT_DATA_BY_ID_ERROR', 'EXPORT_DATASET_FILE_AUTOMATICALLY_DOWNLOAD'],
    setIsLoadingFile,
    false
  );

  useEffect(() => {
    leftSideBarContext.removeModels();
    onLoadDatasetSchema();
  }, []);

  useEffect(() => {
    getDataflowName();
    onLoadDataflowData();
  }, []);

  useEffect(() => {
    setExportButtonsList(internalExtensions);
  }, []);

  useEffect(() => {
    if (notificationContext.hidden.some(notification => notification.key === 'EXPORT_DATASET_FAILED_EVENT')) {
      setIsLoadingFile(false);
    }
  }, [notificationContext.hidden]);

  const getDataflowName = async () => {
    try {
      const data = await DataflowService.getDataflowDetails(match.params.dataflowId);
      setDataflowName(data.name);
    } catch (error) {
      console.error('DataCollection - getDataflowName.', error);
      notificationContext.add({ type: 'DATAFLOW_DETAILS_ERROR', content: {} });
    }
  };

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      console.error('DataCollection - getMetadata.', error);
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } });
    }
  };

  const internalExtensionsList = config.exportTypes.exportDatasetTypes.filter(
    exportType => exportType.code !== 'xlsx+validations'
  );

  const internalExtensions = internalExtensionsList.map(type => {
    const extensionsTypes = type.code.split('+');
    return {
      label: type.text,
      icon: extensionsTypes[0],
      command: () => onExportDataInternalExtension(type.code)
    };
  });

  const onExportDataInternalExtension = async fileType => {
    setIsLoadingFile(true);
    notificationContext.add({ type: 'EXPORT_DATASET_DATA' });

    try {
      await DatasetService.exportDataById(datasetId, fileType);
    } catch (error) {
      console.error('DataCollection - onExportDataInternalExtension.', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await getMetadata({ dataflowId, datasetId });

      notificationContext.add({
        type: 'EXPORT_DATA_BY_ID_ERROR',
        content: { dataflowName: dataflowName, datasetName: datasetName }
      });
    }
  };

  const onLoadDataflowData = async () => {
    try {
      const { data } = await DataflowService.reporting(match.params.dataflowId);
      const dataCollection = data
        ? data.dataCollections.filter(dataset => dataset.dataCollectionId.toString() === datasetId)
        : [];
      const [firstDataCollection] = dataCollection;
      if (!isEmpty(firstDataCollection)) {
        setDataCollectionName(firstDataCollection.dataCollectionName);
      }
      setIsBusinessDataflow(false); // TODO WITH REAL DATA
      setIsLoading(false);
    } catch (error) {
      console.error('DataCollection - onLoadDataflowData.', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'REPORTING_ERROR',
        content: { dataflowId, datasetId, dataflowName, datasetName }
      });
      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setLoading(false);
    }
  };

  const onLoadDatasetSchema = async () => {
    try {
      setLoading(true);
      const datasetSchema = await DatasetService.schemaById(datasetId);
      setLevelErrorTypes(datasetSchema.data.levelErrorTypes);
      const tableSchemaNamesList = [];
      setTableSchema(
        datasetSchema.data.tables.map(tableSchema => {
          tableSchemaNamesList.push(tableSchema.tableSchemaName);
          return {
            id: tableSchema['tableSchemaId'],
            name: tableSchema['tableSchemaName'],
            readOnly: tableSchema['tableSchemaReadOnly']
          };
        })
      );
      setTableSchemaColumns(
        datasetSchema.data.tables.map(table => {
          return table.records[0].fields.map(field => {
            return {
              codelistItems: field['codelistItems'],
              description: field['description'],
              field: field['fieldId'],
              header: field['name'],
              pk: field['pk'],
              maxSize: field['maxSize'],
              pkHasMultipleValues: field['pkHasMultipleValues'],
              readOnly: field['readOnly'],
              recordId: field['recordId'],
              referencedField: field['referencedField'],
              table: table['tableSchemaName'],
              type: field['type'],
              validExtensions: field['validExtensions']
            };
          });
        })
      );
    } catch (error) {
      console.error('DataCollection - onLoadDatasetSchema.', error);
      const metadata = await getMetadata({ dataflowId, datasetId });
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = metadata;
      const {
        response,
        response: {
          data: { path }
        }
      } = error;
      const datasetError = { type: '', content: { dataflowId, datasetId, dataflowName, datasetName } };
      if (!isUndefined(path) && path.includes(getUrl(DatasetConfig.dataSchema, { datasetId }))) {
        datasetError.type = 'SCHEMA_BY_ID_ERROR';
      } else {
        datasetError.type = 'ERROR_STATISTICS_BY_ID_ERROR';
      }
      notificationContext.add(datasetError);
      if (!isUndefined(response) && (response.status === 401 || response.status === 403)) {
        history.push(getUrl(routes.DATAFLOW, { dataflowId }));
      }
    }
    setLoading(false);
  };

  const onRenderTabsSchema = (
    <TabsSchema
      hasCountryCode={true}
      hasWritePermissions={false}
      isBusinessDataflow={isBusinessDataflow}
      isExportable={false}
      isFilterable={false}
      levelErrorTypes={levelErrorTypes}
      onTabChange={table => onTabChange(table)}
      showWriteButtons={false}
      tableSchemaColumns={tableSchemaColumns}
      tableSchemaId={dataViewerOptions.tableSchemaId}
      tables={tableSchema}
    />
  );

  const onTabChange = table => setDataViewerOptions({ ...dataViewerOptions, tableSchemaId: table.tableSchemaId });

  const layout = children => {
    return (
      <MainLayout leftSideBarConfig={{ buttons: [] }}>
        <Growl ref={growlRef} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <Fragment>
      <Title icon="dataCollection" iconSize="3.5rem" subtitle={dataflowName} title={dataCollectionName} />
      <div className={styles.ButtonsBar}>
        <Toolbar>
          <div className="p-toolbar-group-left">
            <Button
              className="p-button-rounded p-button-secondary-transparent p-button-animated-blink"
              icon={isLoadingFile ? 'spinnerAnimate' : 'export'}
              id="buttonExportDataset"
              label={resourcesContext.messages['exportDataset']}
              onClick={event => exportMenuRef.current.show(event)}
            />
            <Menu
              className={styles.exportSubmenu}
              id="exportDataSetMenu"
              model={exportButtonsList}
              popup={true}
              ref={exportMenuRef}
            />
          </div>
        </Toolbar>
      </div>
      {onRenderTabsSchema}
    </Fragment>
  );
});
