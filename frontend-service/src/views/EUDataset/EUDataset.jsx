import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';
import { withRouter } from 'react-router-dom';

import isUndefined from 'lodash/isUndefined';

import styles from './EUDataset.module.scss';

import { config } from 'conf';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { routes } from 'conf/routes';

import { Button } from 'views/_components/Button';
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

import { euDatasetReducer } from './_functions/Reducers/euDatasetReducer';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

import { CurrentPage } from 'views/_functions/Utils';
import { MetadataUtils } from 'views/_functions/Utils';

export const EUDataset = withRouter(({ history, match }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [euDatasetState, euDatasetDispatch] = useReducer(euDatasetReducer, {
    dataflowName: '',
    datasetHasData: false,
    datasetHasErrors: false,
    datasetName: '',
    datasetSchemaAllTables: [],
    datasetSchemaId: null,
    datasetSchemaName: '',
    dataViewerOptions: { activeIndex: null },
    exportExtensionsList: [],
    isBusinessDataflow: false,
    isDataUpdated: false,
    isLoading: true,
    isRefreshHighlighted: false,
    isGroupedValidationSelected: false,
    levelErrorTypes: [],
    metaData: {},
    tableSchema: undefined,
    tableSchemaColumns: undefined,
    tableSchemaId: undefined,
    tableSchemaNames: []
  });

  const {
    dataflowName,
    datasetName,
    dataViewerOptions,
    isBusinessDataflow,
    isLoading,
    isGroupedValidationSelected,
    levelErrorTypes,
    metaData,
    tableSchema,
    tableSchemaColumns
  } = euDatasetState;

  let exportMenuRef = useRef();

  useEffect(() => {
    leftSideBarContext.removeModels();
    callSetMetaData();
    getDataflowDetails();
  }, []);

  useEffect(() => {
    onLoadDatasetSchema();
  }, [euDatasetState.isDataUpdated]);

  useEffect(() => {
    getExportExtensionsList();
  }, []);

  useEffect(() => {
    if (notificationContext.hidden.some(notification => notification.key === 'EXPORT_DATASET_FAILED_EVENT')) {
      setIsLoadingFile(false);
    }
  }, [notificationContext.hidden]);

  useBreadCrumbs({ currentPage: CurrentPage.EU_DATASET, dataflowId, history, isBusinessDataflow, isLoading, metaData });

  const callSetMetaData = async () => {
    euDatasetDispatch({
      type: 'GET_METADATA',
      payload: { metadata: await getMetadata({ dataflowId, datasetId }), isBusinessDataflow: false }
    }); // TODO WITH REAL DATA
  };

  const getDataflowDetails = async () => {
    try {
      const data = await DataflowService.getDataflowDetails(match.params.dataflowId);
      euDatasetDispatch({ type: 'GET_DATAFLOW_DETAILS', payload: { name: data.name, isBusinessDataflow: false } }); // TODO WITH REAL DATA
    } catch (error) {
      console.error('EUDataset - getDataflowName.', error);
      notificationContext.add({ type: 'DATAFLOW_DETAILS_ERROR', content: {} });
    }
  };

  const getDataSchema = async () => {
    try {
      const datasetSchema = await DatasetService.getSchema(datasetId);
      euDatasetDispatch({
        type: 'GET_DATA_SCHEMA',
        payload: {
          allTables: datasetSchema.tables,
          errorTypes: datasetSchema.levelErrorTypes,
          schemaId: datasetSchema.datasetSchemaId,
          schemaName: datasetSchema.datasetSchemaName
        }
      });
      return datasetSchema;
    } catch (error) {
      throw new Error('SCHEMA_BY_ID_ERROR');
    }
  };

  const internalExtensionsList = config.exportTypes.exportDatasetTypes.filter(
    exportType => exportType.code !== 'xlsx+validations'
  );

  const getExportExtensionsList = () => {
    const internalExtensionList = internalExtensionsList.map(type => {
      const extensionsTypes = type.code.split('+');
      return {
        command: () => onExportDataInternalExtension(type.code),
        icon: extensionsTypes[0],
        label: type.text
      };
    });

    euDatasetDispatch({
      type: 'GET_EXPORT_EXTENSIONS_LIST',
      payload: { internalExtensionList }
    });
  };

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      console.error('EUDataset - getMetadata.', error);
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } });
    }
  };

  const getStatisticsById = async (datasetId, tableSchemaNames) => {
    try {
      return await DatasetService.getStatistics(datasetId, tableSchemaNames);
    } catch (error) {
      throw new Error('ERROR_STATISTICS_BY_ID_ERROR');
    }
  };

  const setIsLoading = value => euDatasetDispatch({ type: 'IS_LOADING', payload: { value } });

  const onExportDataInternalExtension = async fileType => {
    setIsLoadingFile(true);
    notificationContext.add({ type: 'EXPORT_DATASET_DATA' });

    try {
      await DatasetService.exportDatasetData(datasetId, fileType);
    } catch (error) {
      console.error('EUDataset - onExportDataInternalExtension.', error);
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

  const onHighlightRefresh = value => euDatasetDispatch({ type: 'ON_HIGHLIGHT_REFRESH', payload: { value } });

  useCheckNotifications(['VALIDATION_FINISHED_EVENT'], onHighlightRefresh, true);

  const onLoadDatasetSchema = async () => {
    setIsLoading(true);
    onHighlightRefresh(false);

    try {
      const datasetSchema = await getDataSchema();
      const datasetStatistics = await getStatisticsById(
        datasetId,
        datasetSchema.tables.map(tableSchema => tableSchema.tableSchemaName)
      );
      const tableSchemaNamesList = [];

      const tableSchema = datasetSchema.tables.map(tableSchema => {
        tableSchemaNamesList.push(tableSchema.tableSchemaName);

        return {
          hasErrors: {
            ...datasetStatistics.tables.filter(table => table['tableSchemaId'] === tableSchema['tableSchemaId'])[0]
          }.hasErrors,
          id: tableSchema['tableSchemaId'],
          name: tableSchema['tableSchemaName'],
          readOnly: tableSchema['tableSchemaReadOnly']
        };
      });

      const tableSchemaColumns = datasetSchema.tables.map(table => {
        return table.records[0].fields.map(field => ({
          codelistItems: field['codelistItems'],
          description: field['description'],
          field: field['fieldId'],
          header: field['name'],
          maxSize: field['maxSize'],
          pkHasMultipleValues: field['pkHasMultipleValues'],
          recordId: field['recordId'],
          referencedField: field['referencedField'],
          table: table['tableSchemaName'],
          type: field['type'],
          validExtensions: field['validExtensions']
        }));
      });

      euDatasetDispatch({
        type: 'ON_LOAD_DATASET_SCHEMA',
        payload: {
          datasetErrors: datasetStatistics.datasetErrors,
          datasetName: datasetStatistics.datasetSchemaName,
          schemaId: datasetSchema.tables[0].tableSchemaId,
          tableSchema,
          tableSchemaColumns,
          tableSchemaNames: tableSchemaNamesList
        }
      });
    } catch (error) {
      console.error('EUDataset - onLoadDatasetSchema.', error);
      notificationContext.add({ type: 'ERROR_LOADING_EU_DATASET_SCHEMA' });
      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        history.push(getUrl(routes.DATAFLOW, { dataflowId }));
      }
    } finally {
      setIsLoading(false);
    }
  };

  const onLoadTableData = hasData => euDatasetDispatch({ type: 'ON_LOAD_TABLE_DATA', payload: { hasData } });

  const onTabChange = table =>
    euDatasetDispatch({ type: 'ON_TAB_CHANGE', payload: { tableSchemaId: table.tableSchemaId } });

  const renderLayout = children => (
    <MainLayout>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  const setIsLoadingFile = value => euDatasetDispatch({ type: 'SET_IS_LOADING_FILE', payload: { value } });

  useCheckNotifications(
    ['DOWNLOAD_EXPORT_DATASET_FILE_ERROR', 'EXPORT_DATA_BY_ID_ERROR', 'EXPORT_DATASET_FILE_AUTOMATICALLY_DOWNLOAD'],
    setIsLoadingFile,
    false
  );

  const renderTabsSchema = () => (
    <TabsSchema
      hasCountryCode={true}
      hasWritePermissions={false}
      isBusinessDataflow={euDatasetState.isBusinessDataflow}
      isExportable={false}
      isFilterable={false}
      isGroupedValidationSelected={isGroupedValidationSelected}
      levelErrorTypes={levelErrorTypes}
      onLoadTableData={onLoadTableData}
      onTabChange={table => onTabChange(table)}
      showWriteButtons={false}
      tableSchemaColumns={tableSchemaColumns}
      tableSchemaId={dataViewerOptions.tableSchemaId}
      tables={tableSchema}
    />
  );

  if (euDatasetState.isLoading) return renderLayout(<Spinner />);

  return renderLayout(
    <Fragment>
      <Title icon="euDataset" iconSize="3.5rem" subtitle={dataflowName} title={datasetName} />
      <div className={styles.ButtonsBar}>
        <Toolbar>
          <div className="p-toolbar-group-left">
            <Button
              className="p-button-rounded p-button-secondary-transparent p-button-animated-blink"
              icon={euDatasetState.isLoadingFile ? 'spinnerAnimate' : 'export'}
              id="buttonExportDataset"
              label={resourcesContext.messages['exportDataset']}
              onClick={event => exportMenuRef.current.show(event)}
            />
            <Menu
              className={styles.exportSubmenu}
              id="exportDataSetMenu"
              model={euDatasetState.exportExtensionsList}
              popup={true}
              ref={exportMenuRef}
            />
          </div>
        </Toolbar>
      </div>
      {renderTabsSchema()}
    </Fragment>
  );
});
