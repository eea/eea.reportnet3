import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import styles from './EUDataset.module.scss';

import { config } from 'conf';
import { DatasetConfig } from 'conf/domain/model/Dataset';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { EUDatasetToolbar } from './_components/EUDatasetToolbar';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from 'ui/views/_components/TabsSchema';
import { Title } from 'ui/views/_components/Title';
import { ValidationViewer } from 'ui/views/_components/ValidationViewer';
import { TabsValidations } from 'ui/views/_components/TabsValidations';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { euDatasetReducer } from './_functions/euDatasetReducer';

import { MetadataUtils } from 'ui/views/_functions/Utils';

export const EUDataset = withRouter(({ history, match }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [euDatasetState, euDatasetDispatch] = useReducer(euDatasetReducer, {
    // dialogVisibility: { validationList: false, dashboards: false, delete: false, input: false },
    dataflowName: '',
    datasetHasData: false,
    datasetHasErrors: false,
    datasetName: '',
    datasetSchemaAllTables: [],
    datasetSchemaId: null,
    datasetSchemaName: '',
    dataViewerOptions: { activeIndex: null, recordPositionId: -1, selectedRecordErrorId: -1 },
    exportButtonsList: [],
    exportDatasetData: undefined,
    exportDatasetDataName: '',
    exportExtensionsOperationsList: [],
    FMEExportExtensions: [],
    hasWritePermissions: false,
    isDashDialogVisible: false,
    isDataDeleted: false,
    isDatasetReleased: false,
    isDeleteDialogVisible: false,
    isDialogVisible: { dashboard: false, deleteData: false, importData: false, validationList: false, validate: false },
    isInputSwitchChecked: false,
    isLoading: true,
    isLoadingFile: false,
    isRefreshHighlighted: false,
    isValidateDialogVisible: false,
    isValidationListDialogVisible: false,
    isValidationSelected: false,
    isWebFormMMR: false,
    levelErrorTypes: [],
    metaData: {},
    tableSchema: undefined,
    tableSchemaColumns: undefined,
    tableSchemaId: undefined,
    tableSchemaNames: [],
    validationsVisible: false
  });

  console.log('euDatasetState', euDatasetState);

  const {
    dataflowName,
    datasetName,
    datasetSchemaName,
    dataViewerOptions,
    hasWritePermissions,
    isDataDeleted,
    isDatasetReleased,
    isDialogVisible,
    isValidationSelected,
    levelErrorTypes,
    metaData,
    tableSchema,
    tableSchemaColumns
  } = euDatasetState;

  useEffect(() => {
    callSetMetaData();
  }, []);

  useEffect(() => {
    // onLoadDatasetSchema();
  }, [isDataDeleted]);

  useEffect(() => {
    if (!isUndefined(metaData.dataset)) {
      const breadCrumbs = [
        {
          command: () => history.push(getUrl(routes.DATAFLOWS)),
          href: getUrl(routes.DATAFLOWS),
          icon: 'home',
          label: resources.messages['dataflows']
        },
        {
          command: () => history.goBack(),
          href: getUrl(routes.DATAFLOW, { dataflowId }, true),
          icon: 'clone',
          label: resources.messages['dataflow']
        }
      ];
      if (breadCrumbContext.model.find(model => model.icon === 'representative')) {
        breadCrumbs.push({
          command: () =>
            history.push(getUrl(routes.REPRESENTATIVE, { dataflowId, representative: metaData.dataset.name }, true)),
          href: getUrl(routes.REPRESENTATIVE, { dataflowId, representative: metaData.dataset.name }, true),
          icon: 'representative',
          label: !isUndefined(metaData.dataset) ? metaData.dataset.name : resources.messages['representative']
        });
      }

      breadCrumbs.push({ label: resources.messages['dataset'], icon: 'dataset' });
      breadCrumbContext.add(breadCrumbs);
      leftSideBarContext.removeModels();
    }
  }, [metaData]);

  const callSetMetaData = async () => {
    euDatasetDispatch({ type: 'GET_METADATA', payload: { metadata: await getMetadata({ dataflowId, datasetId }) } });
  };

  const getDataSchema = async () => {
    try {
      const datasetSchema = await DatasetService.schemaById(datasetId);
      euDatasetDispatch({
        type: 'GET_DATA_SCHEMA',
        payload: {
          allTables: datasetSchema.tables,
          errorTypes: datasetSchema.levelErrorTypes,
          schemaName: datasetSchema.datasetSchemaName
        }
      });
      return datasetSchema;
    } catch (error) {
      throw new Error('SCHEMA_BY_ID_ERROR');
    }
  };

  const getDatasetTitle = () => {
    const datasetReleasedTitle = `${datasetSchemaName} (${resources.messages['released'].toString().toLowerCase()})`;
    return isDatasetReleased ? datasetReleasedTitle : datasetSchemaName;
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

  const handleDialogs = (dialog, value) => euDatasetDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, value } });

  const isLoading = value => euDatasetDispatch({ type: 'IS_LOADING', payload: { value } });

  const isWebFormMMR = datasetName => {
    const mmrDatasetName = 'MMR_TEST';

    const value = datasetName.toString().toLowerCase() === mmrDatasetName.toString().toLowerCase();

    euDatasetDispatch({ type: 'IS_WEB_FORM_MMR', payload: { value } });
  };

  const onConfirmValidate = async () => {
    try {
      //   setValidateDialogVisible(false);
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

  const onLoadDataflow = async () => {
    try {
      const dataflow = await DataflowService.reporting(match.params.dataflowId);
      const dataset = dataflow.datasets.filter(datasets => datasets.datasetId == datasetId);
      //   setIsDatasetReleased(dataset[0].isReleased);
    } catch (error) {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'REPORTING_ERROR',
        content: { dataflowId, dataflowName, datasetId, datasetName }
      });
      if (error.response && (error.response.status === 401 || error.response.status === 403)) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      isLoading(false);
    }
  };

  const onLoadDatasetSchema = async () => {
    // onHighlightRefresh(false);

    try {
      isLoading(true);
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

      isWebFormMMR(datasetStatistics.datasetSchemaName);

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
      notificationContext.add(datasetError);

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

  const renderConfirmDialogLayout = (onConfirm, option) =>
    isDialogVisible[option] && (
      <ConfirmDialog
        header={resources.messages[`${option}`]}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        onConfirm={() => onConfirm()}
        onHide={() => handleDialogs(option, false)}
        visible={isDialogVisible[option]}>
        {resources.messages[`${option}`]}
      </ConfirmDialog>
    );

  const renderDialogLayout = (children, option) =>
    isDialogVisible[option] && (
      <Dialog
        header={resources.messages[`${option}`]}
        onHide={() => handleDialogs(option, false)}
        style={{ width: '80%' }}
        visible={isDialogVisible[option]}>
        {children}
      </Dialog>
    );

  const renderLayout = children => (
    <MainLayout>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  const renderTabsSchema = () => (
    <TabsSchema
      activeIndex={dataViewerOptions.activeIndex}
      hasWritePermissions={hasWritePermissions}
      isDataCollection={true}
      isWebFormMMR={false}
      levelErrorTypes={levelErrorTypes}
      onLoadTableData={onLoadTableData}
      onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
      recordPositionId={dataViewerOptions.recordPositionId}
      selectedRecordErrorId={dataViewerOptions.selectedRecordErrorId}
      tables={tableSchema}
      tableSchemaColumns={tableSchemaColumns}
    />
  );

  return renderLayout(
    <Fragment>
      <Title
        icon="dataset"
        iconSize="3.5rem"
        subtitle={`${dataflowName} - ${datasetName}`}
        title={`${getDatasetTitle()}`}
      />
      <EUDatasetToolbar handleDialogs={handleDialogs} />
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
        <TabsValidations
          dataset={{ datasetId: datasetId, name: datasetSchemaName }}
          datasetSchemaAllTables={euDatasetState.datasetSchemaAllTables}
          // datasetSchemaId={datasetSchemaId}
          onHideValidationsDialog={() => handleDialogs('validationList', false)}
        />,
        'validationList'
      )}

      {renderConfirmDialogLayout(() => {}, 'deleteData')}
      {renderConfirmDialogLayout(() => {}, 'validate')}
    </Fragment>
  );
});
