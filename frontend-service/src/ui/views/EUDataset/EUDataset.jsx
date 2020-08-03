import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dashboard } from 'ui/views/_components/Dashboard';
import { Dialog } from 'ui/views/_components/Dialog';
import { EUDatasetToolbar } from './_components/EUDatasetToolbar';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from 'ui/views/_components/TabsSchema';
import { TabsValidations } from 'ui/views/_components/TabsValidations';
import { Title } from 'ui/views/_components/Title';
import { ValidationViewer } from 'ui/views/_components/ValidationViewer';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

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
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [euDatasetState, euDatasetDispatch] = useReducer(euDatasetReducer, {
    dataflowName: '',
    datasetHasData: false,
    datasetHasErrors: false,
    datasetName: '',
    datasetSchemaAllTables: [],
    datasetSchemaId: null,
    datasetSchemaName: '',
    dataViewerOptions: { activeIndex: null, recordPositionId: -1, selectedRecordErrorId: -1 },
    hasWritePermissions: false,
    isDataDeleted: false,
    isDataUpdated: false,
    isDialogVisible: {
      dashboard: false,
      deleteData: false,
      importData: false,
      qcRules: false,
      validate: false,
      validationList: false
    },
    isLoading: true,
    isRefreshHighlighted: false,
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
    datasetSchemaName,
    dataViewerOptions,
    hasWritePermissions,
    isDataDeleted,
    isDialogVisible,
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
  }, [euDatasetState.isDataUpdated, isDataDeleted]);

  useEffect(() => {
    getWritePermissions();
  }, [userContext]);

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

  const getWritePermissions = () => {
    if (!isUndefined(userContext.contextRoles)) {
      const userRoles = userContext.getUserRole(`${config.permissions['DATASET']}${datasetId}`);
      const hasWritePermissions = userRoles.map(roles =>
        roles.includes(config.permissions['DATA_CUSTODIAN'] || config.permissions['DATA_STEWARD'])
      );

      euDatasetDispatch({ type: 'HAS_WRITE_PERMISSIONS', payload: { hasWritePermissions } });
    }
  };

  const handleDialogs = (dialog, value) => euDatasetDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, value } });

  const isLoading = value => euDatasetDispatch({ type: 'IS_LOADING', payload: { value } });

  const onConfirmDelete = async () => {
    handleDialogs('deleteData', false);

    try {
      const response = await DatasetService.deleteDataById(datasetId);
      if (response) onUpdateData(!euDatasetState.isDataUpdated);
    } catch (error) {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'DATASET_SERVICE_DELETE_DATA_BY_ID_ERROR',
        content: { dataflowId, datasetId, dataflowName, datasetName }
      });
    }
  };

  const onConfirmValidate = async () => {
    handleDialogs('validate', false);

    try {
      await DatasetService.validateDataById(datasetId);
      notificationContext.add({
        type: 'VALIDATE_DATA_INIT',
        content: { dataflowId, dataflowName, datasetId, datasetName }
      });
    } catch (error) {
      notificationContext.add({
        type: 'VALIDATE_DATA_BY_ID_ERROR',
        content: { dataflowId, dataflowName, datasetId, datasetName }
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
          pkHasMultipleValues: field['pkHasMultipleValues'],
          recordId: field['recordId'],
          referencedField: field['referencedField'],
          table: table['tableSchemaName'],
          type: field['type']
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
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await getMetadata({ dataflowId, datasetId });
      //   setDatasetName(datasetName);
      const datasetError = { type: error.message, content: { dataflowName, datasetId, datasetName } };
      // notificationContext.add(datasetError);
      notificationContext.add({ type: 'ERROR_LOADING_EU_DATASET_SCHEMA' });

      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        history.push(getUrl(routes.DATAFLOW, { dataflowId }));
      }
    } finally {
      isLoading(false);
    }
  };

  const onLoadTableData = hasData => euDatasetDispatch({ type: 'ON_LOAD_TABLE_DATA', payload: { hasData } });

  const onTabChange = tableSchemaId => {
    euDatasetDispatch({ type: 'ON_TAB_CHANGE', payload: { activeIndex: tableSchemaId.index } });
  };

  const onSelectValidation = (tableSchemaId, posIdRecord, selectedRecordErrorId) => {
    euDatasetDispatch({
      type: 'ON_SELECT_VALIDATION',
      payload: {
        activeIndex: tableSchemaId,
        isValidationSelected: false,
        recordPositionId: posIdRecord,
        selectedRecordErrorId: selectedRecordErrorId
      }
    });

    handleDialogs('validationList', false);
  };

  const onSetIsValidationSelected = value => euDatasetDispatch({ type: 'IS_VALIDATION_SELECTED', payload: { value } });

  const onUpdateData = value => euDatasetDispatch({ type: 'IS_DATA_UPDATED', payload: { value } });

  const renderConfirmDialogLayout = (onConfirm, option) => {
    const confirmClassName = { deleteData: 'p-button-danger', validate: '' };
    const dialogContent = { deleteData: 'deleteDatasetConfirm', validate: 'validateDatasetConfirm' };

    return (
      isDialogVisible[option] && (
        <ConfirmDialog
          classNameConfirm={confirmClassName[option]}
          header={resources.messages[`${option}EuDatasetHeader`]}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onConfirm()}
          onHide={() => handleDialogs(option, false)}
          visible={isDialogVisible[option]}>
          {resources.messages[dialogContent[option]]}
        </ConfirmDialog>
      )
    );
  };

  const renderDialogLayout = (children, option) => {
    const dialogFooter = (
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => handleDialogs(option, false)}
      />
    );

    return (
      isDialogVisible[option] && (
        <Dialog
          header={resources.messages[`${option}`]}
          footer={dialogFooter}
          onHide={() => handleDialogs(option, false)}
          style={{ width: '80%' }}
          visible={isDialogVisible[option]}>
          {children}
        </Dialog>
      )
    );
  };

  const renderLayout = children => (
    <MainLayout>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  const renderTabsSchema = () => (
    <TabsSchema
      activeIndex={dataViewerOptions.activeIndex}
      hasWritePermissions={false}
      isDatasetDeleted={isDataDeleted}
      isEUDataset={true}
      isValidationSelected={isValidationSelected}
      levelErrorTypes={levelErrorTypes}
      onLoadTableData={onLoadTableData}
      onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
      recordPositionId={dataViewerOptions.recordPositionId}
      selectedRecordErrorId={dataViewerOptions.selectedRecordErrorId}
      setIsValidationSelected={onSetIsValidationSelected}
      tables={tableSchema}
      tableSchemaColumns={tableSchemaColumns}
    />
  );

  if (euDatasetState.isLoading) return renderLayout(<Spinner />);

  return renderLayout(
    <Fragment>
      <Title icon="euDataset" iconSize="3.5rem" subtitle={dataflowName} title={datasetName} />
      <EUDatasetToolbar
        datasetHasData={euDatasetState.datasetHasData}
        datasetHasErrors={euDatasetState.datasetHasErrors}
        handleDialogs={handleDialogs}
        isRefreshHighlighted={euDatasetState.isRefreshHighlighted}
        onRefresh={onLoadDatasetSchema}
      />
      {renderTabsSchema()}

      {renderDialogLayout(
        <ValidationViewer
          datasetId={datasetId}
          datasetName={datasetName}
          hasWritePermissions={hasWritePermissions}
          levelErrorTypes={levelErrorTypes}
          onSelectValidation={onSelectValidation}
          tableSchemaNames={euDatasetState.tableSchemaNames}
          visible={isDialogVisible.validationList}
        />,
        'validationList'
      )}
      {renderDialogLayout(
        <Dashboard
          levelErrorTypes={levelErrorTypes}
          refresh={isDialogVisible.dashboard}
          tableSchemaNames={euDatasetState.tableSchemaNames}
        />,
        'dashboard'
      )}
      {renderDialogLayout(
        <TabsValidations
          dataset={{ datasetId: datasetId, name: datasetSchemaName }}
          datasetSchemaAllTables={euDatasetState.datasetSchemaAllTables}
          datasetSchemaId={euDatasetState.datasetSchemaId}
          reporting={true}
        />,
        'qcRules'
      )}

      {renderConfirmDialogLayout(() => onConfirmDelete(), 'deleteData')}
      {renderConfirmDialogLayout(() => onConfirmValidate(), 'validate')}
    </Fragment>
  );
});
