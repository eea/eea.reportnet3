import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import uniq from 'lodash/uniq';

import styles from './DatasetDesigner.module.scss';

import { config } from 'conf';
import { DatasetSchemaRequesterEmptyHelpConfig } from 'conf/help/datasetSchema/requester/empty';
import { DatasetSchemaRequesterWithTabsHelpConfig } from 'conf/help/datasetSchema/requester/withTabs';
import { DatasetSchemaReporterHelpConfig } from 'conf/help/datasetSchema/reporter';
import { DatasetConfig } from 'conf/domain/model/Dataset';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { Dashboard } from 'ui/views/_components/Dashboard';
import { Dialog } from 'ui/views/_components/Dialog';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { Integrations } from './_components/Integrations';
import { MainLayout } from 'ui/views/_components/Layout';
import { ManageUniqueConstraint } from './_components/ManageUniqueConstraint';
import { Menu } from 'primereact/menu';
import { Snapshots } from 'ui/views/_components/Snapshots';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsDesigner } from './_components/TabsDesigner';
import { TabsValidations } from 'ui/views/_components/TabsValidations';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { UniqueConstraints } from './_components/UniqueConstraints';
import { ValidationViewer } from 'ui/views/_components/ValidationViewer';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { IntegrationService } from 'core/services/Integration';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import { designerReducer } from './_functions/Reducers/designerReducer';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';
import { useDatasetDesigner } from 'ui/views/_components/Snapshots/_hooks/useDatasetDesigner';

import { DatasetDesignerUtils } from './_functions/Utils/DatasetDesignerUtils';
import { CurrentPage, ExtensionUtils, MetadataUtils } from 'ui/views/_functions/Utils';
import { getUrl, TextUtils } from 'core/infrastructure/CoreUtils';

export const DatasetDesigner = withRouter(({ history, match }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const validationContext = useContext(ValidationContext);
  const [hasValidations, setHasValidations] = useState();

  const [designerState, designerDispatch] = useReducer(designerReducer, {
    areLoadedSchemas: false,
    areUpdatingTables: false,
    dashDialogVisible: false,
    dataflowName: '',
    datasetDescription: '',
    datasetHasData: false,
    datasetSchema: {},
    datasetSchemaAllTables: [],
    datasetSchemaId: '',
    datasetSchemaName: '',
    datasetSchemas: [],
    datasetStatistics: [],
    dataViewerOptions: { activeIndex: 0, isValidationSelected: false, recordPositionId: -1, selectedRecordErrorId: -1 },
    exportButtonsList: [],
    exportDatasetData: null,
    exportDatasetDataName: '',
    extensionsOperationsList: { export: [], import: [] },
    hasWritePermissions: false,
    initialDatasetDescription: '',
    isDataUpdated: false,
    isDuplicatedToManageUnique: false,
    isImportDatasetDialogVisible: false,
    isIntegrationListDialogVisible: false,
    isIntegrationManageDialogVisible: false,
    isLoading: true,
    isLoadingFile: false,
    isManageUniqueConstraintDialogVisible: false,
    isPreviewModeOn: DatasetDesignerUtils.getUrlParamValue('design'),
    isRefreshHighlighted: false,
    isUniqueConstraintsListDialogVisible: false,
    isValidationViewerVisible: false,
    levelErrorTypes: [],
    manageUniqueConstraintData: {
      fieldData: [],
      isTableCreationMode: false,
      tableSchemaId: null,
      tableSchemaName: '',
      uniqueId: null
    },
    metaData: {},
    refresh: false,
    tableSchemaNames: [],
    uniqueConstraintsList: [],
    validateDialogVisible: false,
    validationListDialogVisible: false
  });

  const exportMenuRef = useRef();

  const {
    isLoadingSnapshotListData,
    isSnapshotDialogVisible,
    isSnapshotsBarVisible,
    setIsSnapshotDialogVisible,
    setIsSnapshotsBarVisible,
    snapshotDispatch,
    snapshotListData,
    snapshotState
  } = useDatasetDesigner(dataflowId, datasetId, designerState.datasetSchemaId);

  useBreadCrumbs({ currentPage: CurrentPage.DATASET_DESIGNER, dataflowId, history });

  useEffect(() => {
    leftSideBarContext.removeModels();
    onLoadSchema();
    callSetMetaData();
  }, []);

  useEffect(() => {
    if (!isUndefined(userContext.contextRoles)) {
      designerDispatch({
        type: 'LOAD_PERMISSIONS',
        payload: {
          permissions: userContext.hasPermission(
            [config.permissions.LEAD_REPORTER],
            `${config.permissions.DATASET}${datasetId}`
          )
        }
      });
    }
  }, [userContext]);

  useEffect(() => {
    if (!isUndefined(userContext.contextRoles)) {
      if (userContext.accessRole[0] === 'DATA_CUSTODIAN') {
        if (designerState.datasetSchemaAllTables.length > 1) {
          leftSideBarContext.addHelpSteps(
            DatasetSchemaRequesterWithTabsHelpConfig,
            'datasetSchemaRequesterWithTabsHelpConfig'
          );
        } else {
          leftSideBarContext.addHelpSteps(
            DatasetSchemaRequesterEmptyHelpConfig,
            'datasetSchemaRequesterEmptyHelpConfig'
          );
        }
      }
    }
  }, [userContext, designerState, designerState.areLoadingSchemas, designerState.areUpdatingTables]);

  useEffect(() => {
    if (validationContext.opener === 'validationsListDialog' && validationContext.reOpenOpener)
      manageDialogs('validationListDialogVisible', true);
  }, [validationContext]);

  useEffect(() => {
    if (designerState.validationListDialogVisible) {
      validationContext.resetReOpenOpener();
    }
  }, [designerState.validationListDialogVisible]);

  useEffect(() => {
    if (window.location.search !== '') {
      changeUrl();
    }
  }, [designerState.isPreviewModeOn]);

  useEffect(() => {
    if (designerState.datasetSchemaId) getFileExtensions();
  }, [designerState.datasetSchemaId, designerState.isImportDatasetDialogVisible, designerState.isDataUpdated]);

  useEffect(() => {
    getExportList();
  }, [designerState.datasetSchemaName, designerState.extensionsOperationsList]);

  useEffect(() => {
    if (!isNil(designerState.exportDatasetData)) {
      DownloadFile(designerState.exportDatasetData, designerState.exportDatasetDataName);
    }
  }, [designerState.exportDatasetData]);

  const callSetMetaData = async () => {
    const metaData = await getMetadata({ datasetId, dataflowId });
    designerDispatch({
      type: 'GET_METADATA',
      payload: { metaData, dataflowName: metaData.dataflow.name, schemaName: metaData.dataset.name }
    });
  };

  const changeMode = previewMode => {
    designerDispatch({ type: 'IS_PREVIEW_MODE_ON', payload: { value: previewMode } });
  };

  const changeUrl = () => {
    window.history.replaceState(
      null,
      null,
      `?tab=${DatasetDesignerUtils.getUrlParamValue('tab')}${
        !isUndefined(designerState.isPreviewModeOn) ? `&design=${designerState.isPreviewModeOn}` : ''
      }`
    );
  };

  const createFileName = (fileName, fileType) => `${fileName}.${fileType}`;

  const filterActiveIndex = index => {
    if (!isNil(index) && isNaN(index)) {
      const filteredTable = designerState.datasetSchema.tables.filter(table => table.tableSchemaId === index);
      if (!isEmpty(filteredTable) && !isNil(filteredTable[0])) {
        return filteredTable[0].index;
      }
    } else {
      return index;
    }
  };

  const getExportList = () => {
    const { extensionsOperationsList } = designerState;

    const internalExtensionList = config.exportTypes.exportDatasetTypes.map(type => ({
      command: () => onExportData(type.code),
      icon: config.icons['archive'],
      label: type.text
    }));

    const externalExtensions = [
      {
        label: resources.messages['externalExtensions'],
        items: extensionsOperationsList.export.map(type => ({
          command: () => onExportData(type.fileExtension.toUpperCase()),
          icon: config.icons['archive'],
          label: `${type.fileExtension.toUpperCase()} (.${type.fileExtension.toLowerCase()})`
        }))
      }
    ];

    designerDispatch({
      type: 'GET_EXPORT_LIST',
      payload: {
        exportList: internalExtensionList.concat(!isEmpty(extensionsOperationsList.export) ? externalExtensions : [])
      }
    });
  };

  const getFileExtensions = async () => {
    try {
      const response = await IntegrationService.allExtensionsOperations(designerState.datasetSchemaId);
      const externalExtension = ExtensionUtils.groupOperations('operation', response);
      designerDispatch({
        type: 'LOAD_EXTERNAL_EXTENSIONS',
        payload: { export: externalExtension.export, import: externalExtension.import }
      });
    } catch (error) {
      notificationContext.add({ type: 'LOADING_FILE_EXTENSIONS_ERROR' });
    }
  };

  const getImportExtensions = designerState.extensionsOperationsList.import
    .map(file => `.${file.fileExtension}`)
    .join(', ')
    .toLowerCase();

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } });
    } finally {
      isLoading(false);
    }
  };

  const getPosition = e => {
    const exportButton = e.currentTarget;
    const left = `${exportButton.offsetLeft}px`;
    const topValue = exportButton.offsetHeight + exportButton.offsetTop + 3;
    const top = `${topValue}px `;
    const menu = exportButton.nextElementSibling;
    menu.style.top = top;
    menu.style.left = left;
  };

  const getStatisticsById = async (datasetId, tableSchemaNames) => {
    try {
      return await DatasetService.errorStatisticsById(datasetId, tableSchemaNames);
    } catch (error) {
      console.error(error);
      throw new Error('ERROR_STATISTICS_BY_ID_ERROR');
    }
  };

  const getUniqueConstraintsList = data => designerDispatch({ type: 'GET_UNIQUES', payload: { data } });

  const infoExtensionsTooltip = `${resources.messages['supportedFileExtensionsTooltip']} ${uniq(
    getImportExtensions.split(', ')
  ).join(', ')}`;

  const isLoading = value => designerDispatch({ type: 'IS_LOADING', payload: { value } });

  const isLoadingFile = value => designerDispatch({ type: 'IS_LOADING_FILE', payload: { value } });

  const manageDialogs = (dialog, value, secondDialog, secondValue) => {
    designerDispatch({ type: 'MANAGE_DIALOGS', payload: { dialog, value, secondDialog, secondValue } });
  };

  const manageUniqueConstraint = data => designerDispatch({ type: 'MANAGE_UNIQUE_CONSTRAINT_DATA', payload: { data } });

  const onBlurDescription = description => {
    if (description !== designerState.initialDatasetDescription) {
      onUpdateDescription(description);
    }
  };

  const onChangeReference = (tabs, datasetSchemaId) => {
    const inmDatasetSchemas = [...designerState.datasetSchemas];
    const datasetSchemaIndex = DatasetDesignerUtils.getIndexById(datasetSchemaId, inmDatasetSchemas);
    inmDatasetSchemas[datasetSchemaIndex].tables = tabs;
    if (!isNil(inmDatasetSchemas)) {
      inmDatasetSchemas.forEach(datasetSchema =>
        datasetSchema.tables.forEach(table => {
          if (!table.addTab && !isUndefined(table.records)) {
            table.records.forEach(record =>
              record.fields.forEach(field => {
                if (!isNil(field) && field.pk) {
                  if (DatasetDesignerUtils.getCountPKUseInAllSchemas(field.fieldId, inmDatasetSchemas) > 0) {
                    table.hasPKReferenced = true;
                    field.pkReferenced = true;
                  } else {
                    table.hasPKReferenced = false;
                    field.pkReferenced = false;
                  }
                }
              })
            );
          }
        })
      );
    }
    designerDispatch({ type: 'LOAD_DATASET_SCHEMAS', payload: { schemas: inmDatasetSchemas } });
  };

  const onConfirmValidate = async () => {
    manageDialogs('validateDialogVisible', false);
    try {
      await DatasetService.validateDataById(datasetId);
      notificationContext.add({
        type: 'VALIDATE_DATA_INIT',
        content: {
          dataflowId,
          dataflowName: designerState.dataflowName,
          datasetId,
          datasetName: designerState.datasetSchemaName
        }
      });
    } catch (error) {
      notificationContext.add({
        type: 'VALIDATE_DATA_BY_ID_ERROR',
        content: {
          dataflowId,
          dataflowName: designerState.dataflowName,
          datasetId,
          datasetName: designerState.datasetSchemaName
        }
      });
    }
  };

  const onExportData = async fileType => {
    isLoadingFile(true);
    try {
      const datasetName = createFileName(designerState.datasetSchemaName, fileType);
      const datasetData = await DatasetService.exportDataById(datasetId, fileType);

      designerDispatch({ type: 'ON_EXPORT_DATA', payload: { data: datasetData, name: datasetName } });
    } catch (error) {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'EXPORT_DATA_BY_ID_ERROR',
        content: { dataflowId, datasetId, dataflowName, datasetName }
      });
    } finally {
      isLoadingFile(false);
    }
  };

  const onHighlightRefresh = value => designerDispatch({ type: 'HIGHLIGHT_REFRESH', payload: { value } });

  useCheckNotifications(['VALIDATION_FINISHED_EVENT'], onHighlightRefresh, true);

  const onHideValidationsDialog = () => {
    if (validationContext.opener === 'validationsListDialog' && validationContext.reOpenOpener) {
      validationContext.onResetOpener();
    }
    manageDialogs('validationListDialogVisible', false);
  };

  const onKeyChange = event => {
    if (event.key === 'Escape') {
      designerDispatch({ type: 'ON_UPDATE_DESCRIPTION', payload: { value: designerState.initialDatasetDescription } });
    } else if (event.key === 'Enter') {
      event.preventDefault();
      onBlurDescription(event.target.value);
    }
  };

  const onLoadSchema = () => {
    onHighlightRefresh(false);

    try {
      isLoading(true);
      const getDatasetSchemaId = async () => {
        const dataset = await DatasetService.schemaById(datasetId);
        const tableSchemaNamesList = [];
        dataset.tables.forEach(table => tableSchemaNamesList.push(table.tableSchemaName));

        const datasetStatisticsDTO = await getStatisticsById(
          datasetId,
          dataset.tables.map(tableSchema => tableSchema.tableSchemaName)
        );

        isLoading(false);
        designerDispatch({
          type: 'GET_DATASET_DATA',
          payload: {
            datasetSchema: dataset,
            datasetStatistics: datasetStatisticsDTO,
            description: dataset.datasetSchemaDescription,
            levelErrorTypes: dataset.levelErrorTypes,
            schemaId: dataset.datasetSchemaId,
            tables: dataset.tables,
            tableSchemaNames: tableSchemaNamesList
          }
        });
      };
      const getDatasetSchemas = async () => {
        designerDispatch({
          type: 'LOAD_DATASET_SCHEMAS',
          payload: { schemas: await DataflowService.getAllSchemas(dataflowId) }
        });
      };

      getDatasetSchemaId();
      getDatasetSchemas();
    } catch (error) {
      console.error(`Error while loading schema: ${error}`);
    }
  };

  const onLoadTableData = hasData => designerDispatch({ type: 'SET_DATASET_HAS_DATA', payload: { hasData } });

  const onSelectValidation = (tableSchemaId, posIdRecord, selectedRecordErrorId) => {
    designerDispatch({
      type: 'SET_DATAVIEWER_OPTIONS',
      payload: {
        activeIndex: tableSchemaId,
        isValidationSelected: true,
        recordPositionId: posIdRecord,
        selectedRecordErrorId
      }
    });
  };

  const onTabChange = tableSchemaId => {
    designerDispatch({
      type: 'SET_DATAVIEWER_OPTIONS',
      payload: { ...designerState.dataViewerOptions, activeIndex: tableSchemaId.index }
    });
  };

  const onUpdateData = () => {
    designerDispatch({ type: 'ON_UPDATE_DATA', payload: { isUpdated: !designerState.isDataUpdated } });
  };

  const onUpdateDescription = async description => {
    try {
      await DatasetService.updateDatasetDescriptionDesign(datasetId, description);
    } catch (error) {
      console.error('Error during datasetSchema Description update: ', error);
    } finally {
    }
  };

  const onUpdateTable = tables => designerDispatch({ type: 'ON_UPDATE_TABLES', payload: { tables } });

  const onUpload = async () => {
    manageDialogs('isImportDatasetDialogVisible', false);

    const {
      dataflow: { name: dataflowName },
      dataset: { name: datasetName }
    } = await MetadataUtils.getMetadata({ dataflowId, datasetId });

    notificationContext.add({
      type: 'DATASET_DATA_LOADING_INIT',
      content: {
        dataflowName,
        datasetLoading: resources.messages['datasetLoading'],
        datasetLoadingMessage: resources.messages['datasetLoadingMessage'],
        datasetName,
        title: TextUtils.ellipsis(datasetName, config.notifications.STRING_LENGTH_MAX)
      }
    });
  };

  const renderActionButtonsValidationDialog = (
    <Fragment>
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'plus'}
        label={resources.messages['createFieldValidationBtn']}
        onClick={() => {
          validationContext.onOpenModalFromOpener('field', 'validationsListDialog');
        }}
        style={{ float: 'left' }}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'plus'}
        label={resources.messages['createRowValidationBtn']}
        onClick={() => {
          validationContext.onOpenModalFromOpener('row', 'validationsListDialog');
        }}
        style={{ float: 'left' }}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'plus'}
        label={resources.messages['createDatasetValidationBtn']}
        onClick={() => {
          validationContext.onOpenModalFromOpener('dataset', 'validationsListDialog');
        }}
        style={{ float: 'left' }}
      />

      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => onHideValidationsDialog()}
      />
    </Fragment>
  );

  const renderCustomFileUploadFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => manageDialogs('isImportDatasetDialogVisible', false)}
    />
  );

  const renderSwitchView = () => (
    <div className={styles.switchDivInput}>
      <div className={`${styles.switchDiv} datasetSchema-switchDesignToData-help-step`}>
        <span className={styles.switchTextInput}>{resources.messages['design']}</span>
        <InputSwitch
          checked={designerState.isPreviewModeOn}
          // disabled={true}
          // disabled={!isUndefined(fields) ? (fields.length === 0 ? true : false) : false}
          // onChange={e => setIsPreviewModeOn(e.value)}
          onChange={event => designerDispatch({ type: 'IS_PREVIEW_MODE_ON', payload: { value: event.value } })}
        />
        <span className={styles.switchTextInput}>{resources.messages['preview']}</span>
      </div>
    </div>
  );

  const renderUniqueConstraintsDialog = () => (
    <Fragment>
      {designerState.isUniqueConstraintsListDialogVisible && (
        <Dialog
          footer={renderUniqueConstraintsFooter}
          header={resources.messages['uniqueConstraints']}
          onHide={() => manageDialogs('isUniqueConstraintsListDialogVisible', false)}
          style={{ width: '70%' }}
          visible={designerState.isUniqueConstraintsListDialogVisible}>
          <UniqueConstraints
            dataflowId={dataflowId}
            designerState={designerState}
            getManageUniqueConstraint={manageUniqueConstraint}
            getUniques={getUniqueConstraintsList}
            setIsDuplicatedToManageUnique={setIsDuplicatedToManageUnique}
            manageDialogs={manageDialogs}
          />
        </Dialog>
      )}
    </Fragment>
  );

  const renderUniqueConstraintsFooter = (
    <Fragment>
      <div className="p-toolbar-group-left">
        <Button
          className="p-button-secondary p-button-animated-blink"
          icon={'plus'}
          label={resources.messages['addUniqueConstraint']}
          onClick={() => manageDialogs('isManageUniqueConstraintDialogVisible', true)}
        />
      </div>
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => manageDialogs('isUniqueConstraintsListDialogVisible', false)}
      />
    </Fragment>
  );

  const setIsDuplicatedToManageUnique = value =>
    designerDispatch({ type: 'UPDATED_IS_DUPLICATED', payload: { value } });

  const validationsListDialog = () => {
    if (designerState.validationListDialogVisible) {
      return (
        <Dialog
          className={hasValidations ? styles.qcRulesDialog : styles.qcRulesDialogEmpty}
          dismissableMask={true}
          footer={renderActionButtonsValidationDialog}
          header={resources.messages['qcRules']}
          onHide={() => onHideValidationsDialog()}
          visible={designerState.validationListDialogVisible}>
          <TabsValidations
            dataset={designerState.metaData.dataset}
            datasetSchemaAllTables={designerState.datasetSchemaAllTables}
            datasetSchemaId={designerState.datasetSchemaId}
            onHideValidationsDialog={onHideValidationsDialog}
            setHasValidations={setHasValidations}
          />
        </Dialog>
      );
    }
  };

  const layout = children => (
    <MainLayout>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  if (designerState.isLoading) return layout(<Spinner />);

  return layout(
    <SnapshotContext.Provider
      value={{
        isSnapshotsBarVisible: isSnapshotsBarVisible,
        setIsSnapshotsBarVisible: setIsSnapshotsBarVisible,
        snapshotDispatch: snapshotDispatch,
        snapshotState: snapshotState
      }}>
      <div className={styles.noScrollDatasetDesigner}>
        <Title
          icon="pencilRuler"
          iconSize="3.4rem"
          subtitle={designerState.dataflowName}
          title={`${resources.messages['datasetSchema']}: ${designerState.datasetSchemaName}`}
        />
        <h4 className={styles.descriptionLabel}>{resources.messages['newDatasetSchemaDescriptionPlaceHolder']}</h4>
        <div className={styles.ButtonsBar}>
          <InputTextarea
            className={`${styles.datasetDescription} datasetSchema-metadata-help-step`}
            collapsedHeight={55}
            expandableOnClick={true}
            id="datasetDescription"
            key="datasetDescription"
            onBlur={e => onBlurDescription(e.target.value)}
            onChange={e => designerDispatch({ type: 'ON_UPDATE_DESCRIPTION', payload: { value: e.target.value } })}
            onFocus={e => designerDispatch({ type: 'INITIAL_DATASET_DESCRIPTION', payload: { value: e.target.value } })}
            onKeyDown={e => onKeyChange(e)}
            placeholder={resources.messages['newDatasetSchemaDescriptionPlaceHolder']}
            value={designerState.datasetDescription || ''}
          />
          <Toolbar>
            <div className="p-toolbar-group-left">
              {!isEmpty(designerState.extensionsOperationsList.import) && (
                <Button
                  className={`p-button-rounded p-button-secondary p-button-animated-blink`}
                  icon={'import'}
                  label={resources.messages['importDataset']}
                  onClick={() => manageDialogs('isImportDatasetDialogVisible', true)}
                />
              )}
              <Button
                className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
                icon={designerState.isLoadingFile ? 'spinnerAnimate' : 'export'}
                id="buttonExportDataset"
                label={resources.messages['exportDataset']}
                onClick={event => exportMenuRef.current.show(event)}
              />
              <Menu
                id="exportDataSetMenu"
                model={designerState.exportButtonsList}
                onShow={e => getPosition(e)}
                popup={true}
                ref={exportMenuRef}
              />
            </div>
            <div className="p-toolbar-group-right">
              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  designerState.datasetHasData && designerState.isPreviewModeOn ? ' p-button-animated-blink' : null
                }`}
                disabled={!designerState.datasetHasData}
                icon={'validate'}
                iconClasses={null}
                label={resources.messages['validate']}
                onClick={() => manageDialogs('validateDialogVisible', true)}
                ownButtonClasses={null}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  designerState.datasetStatistics.datasetErrors && designerState.isPreviewModeOn
                    ? 'p-button-animated-blink'
                    : null
                }`}
                disabled={!designerState.datasetStatistics.datasetErrors}
                icon={'warning'}
                label={resources.messages['showValidations']}
                onClick={() => designerDispatch({ type: 'TOGGLE_VALIDATION_VIEWER_VISIBILITY', payload: true })}
                ownButtonClasses={null}
                iconClasses={designerState.datasetStatistics.datasetErrors ? 'warning' : ''}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink datasetSchema-qcRules-help-step`}
                disabled={false}
                icon={'horizontalSliders'}
                iconClasses={null}
                label={resources.messages['qcRules']}
                onClick={() => manageDialogs('validationListDialogVisible', true)}
                ownButtonClasses={null}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
                icon={'key'}
                label={resources.messages['uniqueConstraints']}
                onClick={() => manageDialogs('isUniqueConstraintsListDialogVisible', true)}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent ${styles.integrationsButton}`}
                icon={'export'}
                iconClasses={styles.integrationsButtonIcon}
                label={resources.messages['externalIntegrations']}
                onClick={() => manageDialogs('isIntegrationListDialogVisible', true)}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  designerState.datasetHasData && 'p-button-animated-blink'
                }`}
                disabled={!designerState.datasetHasData}
                icon={'dashboard'}
                label={resources.messages['dashboards']}
                onClick={() => designerDispatch({ type: 'TOGGLE_DASHBOARD_VISIBILITY', payload: true })}
              />
              <Button
                className={`p-button-rounded p-button-secondary-transparent datasetSchema-manageCopies-help-step ${
                  !designerState.hasWritePermissions ? 'p-button-animated-blink' : null
                }`}
                disabled={designerState.hasWritePermissions}
                icon={'camera'}
                label={resources.messages['snapshots']}
                onClick={() => setIsSnapshotsBarVisible(!isSnapshotsBarVisible)}
              />

              <Button
                className={`p-button-rounded p-button-${
                  designerState.isRefreshHighlighted ? 'primary' : 'secondary-transparent'
                }  p-button-animated-blink`}
                icon={'refresh'}
                label={resources.messages['refresh']}
                onClick={() => onLoadSchema()}
              />
            </div>
          </Toolbar>
        </div>
        {renderSwitchView()}
        <TabsDesigner
          activeIndex={filterActiveIndex(designerState.dataViewerOptions.activeIndex)}
          changeMode={changeMode}
          datasetSchemaDTO={designerState.datasetSchema}
          datasetSchemas={designerState.datasetSchemas}
          datasetStatistics={designerState.datasetStatistics}
          editable={true}
          history={history}
          isPreviewModeOn={designerState.isPreviewModeOn}
          isValidationSelected={designerState.dataViewerOptions.isValidationSelected}
          manageDialogs={manageDialogs}
          manageUniqueConstraint={manageUniqueConstraint}
          onChangeReference={onChangeReference}
          onLoadTableData={onLoadTableData}
          onTabChange={onTabChange}
          onUpdateTable={onUpdateTable}
          recordPositionId={designerState.dataViewerOptions.recordPositionId}
          selectedRecordErrorId={designerState.dataViewerOptions.selectedRecordErrorId}
          setActiveIndex={index =>
            designerDispatch({
              type: 'SET_DATAVIEWER_OPTIONS',
              payload: { ...designerState.dataViewerOptions, activeIndex: index }
            })
          }
          setIsValidationSelected={isVisible =>
            designerDispatch({ type: 'SET_IS_VALIDATION_SELECTED', payload: isVisible })
          }
        />
        <Snapshots
          isLoadingSnapshotListData={isLoadingSnapshotListData}
          isSnapshotDialogVisible={isSnapshotDialogVisible}
          setIsSnapshotDialogVisible={setIsSnapshotDialogVisible}
          snapshotListData={snapshotListData}
        />
        {validationsListDialog()}
        {renderUniqueConstraintsDialog()}

        <Integrations
          dataflowId={dataflowId}
          datasetId={datasetId}
          designerState={designerState}
          manageDialogs={manageDialogs}
          onUpdateData={onUpdateData}
        />

        <ManageUniqueConstraint
          dataflowId={dataflowId}
          designerState={designerState}
          manageDialogs={manageDialogs}
          resetUniques={manageUniqueConstraint}
        />

        {designerState.validateDialogVisible && (
          <ConfirmDialog
            header={resources.messages['validateDataset']}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            maximizable={false}
            onConfirm={onConfirmValidate}
            onHide={() => manageDialogs('validateDialogVisible', false)}
            visible={designerState.validateDialogVisible}>
            {resources.messages['validateDatasetConfirm']}
          </ConfirmDialog>
        )}
        {designerState.dashDialogVisible && (
          <Dialog
            dismissableMask={true}
            header={resources.messages['titleDashboard']}
            onHide={() => designerDispatch({ type: 'TOGGLE_DASHBOARD_VISIBILITY', payload: false })}
            style={{ width: '70vw' }}
            visible={designerState.dashDialogVisible}>
            <Dashboard
              refresh={designerState.dashDialogVisible}
              levelErrorTypes={designerState.levelErrorTypes}
              tableSchemaNames={designerState.tableSchemaNames}
            />
          </Dialog>
        )}
        {designerState.isValidationViewerVisible && (
          <Dialog
            className={styles.paginatorValidationViewer}
            dismissableMask={true}
            header={resources.messages['titleValidations']}
            onHide={() => designerDispatch({ type: 'TOGGLE_VALIDATION_VIEWER_VISIBILITY', payload: false })}
            style={{ width: '80%' }}
            visible={designerState.isValidationViewerVisible}>
            <ValidationViewer
              datasetId={datasetId}
              datasetName={designerState.datasetSchemaName}
              hasWritePermissions={designerState.hasWritePermissions}
              levelErrorTypes={designerState.datasetSchema.levelErrorTypes}
              onSelectValidation={onSelectValidation}
              tableSchemaNames={designerState.tableSchemaNames}
              visible={designerState.isValidationViewerVisible}
            />
          </Dialog>
        )}

        {designerState.isImportDatasetDialogVisible && (
          <Dialog
            className={styles.Dialog}
            dismissableMask={false}
            footer={renderCustomFileUploadFooter}
            header={`${resources.messages['uploadDataset']}${designerState.datasetSchemaName}`}
            onHide={() => manageDialogs('isImportDatasetDialogVisible', false)}
            visible={designerState.isImportDatasetDialogVisible}>
            <CustomFileUpload
              accept={getImportExtensions}
              chooseLabel={resources.messages['selectFile']}
              className={styles.FileUpload}
              fileLimit={1}
              infoTooltip={infoExtensionsTooltip}
              invalidExtensionMessage={resources.messages['invalidExtensionFile']}
              mode="advanced"
              multiple={false}
              name="file"
              onUpload={onUpload}
              url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.importDatasetData, {
                datasetId: datasetId
              })}`}
            />
          </Dialog>
        )}
      </div>
    </SnapshotContext.Provider>
  );
});
