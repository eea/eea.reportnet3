import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import isUndefined from 'lodash/isUndefined';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from 'ui/views/_components/TabsSchema';
import { Title } from 'ui/views/_components/Title';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

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

  const [euDatasetState, euDatasetDispatch] = useReducer(euDatasetReducer, {
    dataflowName: '',
    datasetHasData: false,
    datasetHasErrors: false,
    datasetName: '',
    datasetSchemaAllTables: [],
    datasetSchemaId: null,
    datasetSchemaName: '',
    dataViewerOptions: { activeIndex: null, recordPositionId: -1, selectedRecordErrorId: -1 },
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

  useEffect(() => {
    leftSideBarContext.removeModels();
    callSetMetaData();
    getDataflowName();
  }, []);

  useEffect(() => {
    onLoadDatasetSchema();
  }, [euDatasetState.isDataUpdated]);

  useBreadCrumbs({ currentPage: CurrentPage.EU_DATASET, dataflowId, history, metaData });

  const callSetMetaData = async () => {
    euDatasetDispatch({ type: 'GET_METADATA', payload: { metadata: await getMetadata({ dataflowId, datasetId }) } });
  };

  const getDataflowName = async () => {
    try {
      const dataflowData = await DataflowService.dataflowDetails(match.params.dataflowId);
      euDatasetDispatch({ type: 'GET_DATAFLOW_NAME', payload: { name: dataflowData.name } });
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

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } });
    }
  };

  const getStatisticsById = async (datasetId, tableSchemaNames) => {
    try {
      return await DatasetService.errorStatisticsById(datasetId, tableSchemaNames);
    } catch (error) {
      throw new Error('ERROR_STATISTICS_BY_ID_ERROR');
    }
  };

  const isLoading = value => euDatasetDispatch({ type: 'IS_LOADING', payload: { value } });

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
      {renderTabsSchema()}
    </Fragment>
  );
});
