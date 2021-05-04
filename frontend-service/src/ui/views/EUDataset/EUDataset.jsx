import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';
import { withRouter } from 'react-router-dom';

import isUndefined from 'lodash/isUndefined';

import styles from './EUDataset.module.scss';

import { config } from 'conf';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { MainLayout } from 'ui/views/_components/Layout';
// import { Menu } from 'primereact/menu';
import { Menu } from 'ui/views/Dataflow/_components/BigButton/_components/Menu';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from 'ui/views/_components/TabsSchema';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { euDatasetReducer } from './_functions/Reducers/euDatasetReducer';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { MetadataUtils } from 'ui/views/_functions/Utils';

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
    dataViewerOptions: { activeIndex: null, recordPositionId: -1, selectedRecordErrorId: -1 },
    exportExtensionsList: [],
    isDataUpdated: false,
    isLoading: true,
    isRefreshHighlighted: false,
    isGroupedValidationSelected: false,
    isValidationSelected: false,
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
    isGroupedValidationSelected,
    isValidationSelected,
    levelErrorTypes,
    metaData,
    tableSchema,
    tableSchemaColumns
  } = euDatasetState;

  let exportMenuRef = useRef();

  useEffect(() => {
    leftSideBarContext.removeModels();
    callSetMetaData();
    getDataflowName();
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

  useBreadCrumbs({ currentPage: CurrentPage.EU_DATASET, dataflowId, history, metaData });

  const callSetMetaData = async () => {
    euDatasetDispatch({ type: 'GET_METADATA', payload: { metadata: await getMetadata({ dataflowId, datasetId }) } });
  };

  const getDataflowName = async () => {
    try {
      const { data } = await DataflowService.dataflowDetails(match.params.dataflowId);
      euDatasetDispatch({ type: 'GET_DATAFLOW_NAME', payload: { name: data.name } });
    } catch (error) {
      notificationContext.add({ type: 'DATAFLOW_DETAILS_ERROR', content: {} });
    }
  };

  const getDataSchema = async () => {
    try {
      const datasetSchema = await DatasetService.schemaById(datasetId);
      euDatasetDispatch({
        type: 'GET_DATA_SCHEMA',
        payload: {
          allTables: datasetSchema.data.tables,
          errorTypes: datasetSchema.data.levelErrorTypes,
          schemaId: datasetSchema.data.datasetSchemaId,
          schemaName: datasetSchema.data.datasetSchemaName
        }
      });
      return datasetSchema.data;
    } catch (error) {
      throw new Error('SCHEMA_BY_ID_ERROR');
    }
  };

  const getExportExtensionsList = () => {
    const internalExtensionList = config.exportTypes.exportDatasetTypes.map(type => {
      const extensionsTypes = type.code.split('+');
      return ({
      command: () => onExportDataInternalExtension(type.code),
      icon: extensionsTypes[extensionsTypes.length-1],
      label: type.text
    })}) 

    euDatasetDispatch({
      type: 'GET_EXPORT_EXTENSIONS_LIST',
      payload: { internalExtensionList }
    });
  };

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } });
    }
  };

  const getPosition = e => {
    const button = e.currentTarget;
    const left = `${button.offsetLeft}px`;
    const topValue = button.offsetHeight + button.offsetTop + 3;
    const top = `${topValue}px `;
    const menu = button.nextElementSibling;
    menu.style.top = top;
    menu.style.left = left;
  };

  const getStatisticsById = async (datasetId, tableSchemaNames) => {
    try {
      const statistics = await DatasetService.errorStatisticsById(datasetId, tableSchemaNames);
      return statistics.data;
    } catch (error) {
      throw new Error('ERROR_STATISTICS_BY_ID_ERROR');
    }
  };

  const isLoading = value => euDatasetDispatch({ type: 'IS_LOADING', payload: { value } });

  const onExportDataInternalExtension = async fileType => {
    setIsLoadingFile(true);
    notificationContext.add({ type: 'EXPORT_DATASET_DATA' });

    try {
      await DatasetService.exportDataById(datasetId, fileType);
    } catch (error) {
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
    isLoading(true);
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
      notificationContext.add({ type: 'ERROR_LOADING_EU_DATASET_SCHEMA' });

      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        history.push(getUrl(routes.DATAFLOW, { dataflowId }));
      }
    } finally {
      isLoading(false);
    }
  };

  const onLoadTableData = hasData => euDatasetDispatch({ type: 'ON_LOAD_TABLE_DATA', payload: { hasData } });

  const onTabChange = table =>
    euDatasetDispatch({ type: 'ON_TAB_CHANGE', payload: { tableSchemaId: table.tableSchemaId } });

  const onSetIsValidationSelected = value => euDatasetDispatch({ type: 'IS_VALIDATION_SELECTED', payload: { value } });

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
      hasWritePermissions={false}
      showWriteButtons={false}
      isExportable={false}
      isFilterable={false}
      hasCountryCode={true}
      isGroupedValidationSelected={isGroupedValidationSelected}
      isValidationSelected={isValidationSelected}
      levelErrorTypes={levelErrorTypes}
      onLoadTableData={onLoadTableData}
      onTabChange={table => onTabChange(table)}
      recordPositionId={dataViewerOptions.recordPositionId}
      selectedRecordErrorId={dataViewerOptions.selectedRecordErrorId}
      setIsValidationSelected={onSetIsValidationSelected}
      tables={tableSchema}
      tableSchemaColumns={tableSchemaColumns}
      tableSchemaId={dataViewerOptions.tableSchemaId}
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
              // onShow={e => getPosition(e)}
              show={e => {console.log('e :>> ', e) }}
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
