import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './DatasetDesigner.module.scss';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { MainLayout } from 'ui/views/_components/Layout';
import { ManageUniqueConstraint } from './_components/ManageUniqueConstraint';
import { Snapshots } from 'ui/views/_components/Snapshots';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsDesigner } from './_components/TabsDesigner';
import { TabsValidations } from './_components/TabsValidations';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { UniqueConstraints } from './_components/UniqueConstraints';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import { designerReducer } from './_functions/Reducers/designerReducer';

import { useDatasetDesigner } from 'ui/views/_components/Snapshots/_hooks/useDatasetDesigner';

import { DatasetDesignerUtils } from './_functions/Utils/DatasetDesignerUtils';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { MetadataUtils } from 'ui/views/_functions/Utils';

export const DatasetDesigner = withRouter(({ history, match }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const validationContext = useContext(ValidationContext);

  const [designerState, designerDispatch] = useReducer(designerReducer, {
    dataflowName: '',
    datasetDescription: '',
    datasetHasData: false,
    datasetSchemaAllTables: [],
    datasetSchemaId: '',
    datasetSchemaName: '',
    datasetSchemas: [],
    hasWritePermissions: false,
    initialDatasetDescription: '',
    isLoading: true,
    isPreviewModeOn: DatasetDesignerUtils.getUrlParamValue('design'),
    isManageUniqueConstraintDialogVisible: false,
    metaData: {},
    uniqueConstraintListDialogVisible: false,
    validateDialogVisible: false,
    validationListDialogVisible: false,
    refresh: false
  });

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

  useEffect(() => {
    onLoadSchema();
  }, []);

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      designerDispatch({
        type: 'LOAD_PERMISSIONS',
        payload: {
          permissions: UserService.hasPermission(
            user,
            [config.permissions.PROVIDER],
            `${config.permissions.DATASET}${datasetId}`
          )
        }
      });
    }
  }, [user]);

  useEffect(() => {
    breadCrumbContext.add([
      {
        command: () => history.push(getUrl(routes.DATAFLOWS)),
        href: getUrl(routes.DATAFLOWS),
        icon: 'home',
        label: resources.messages['dataflows']
      },
      {
        command: () => history.push(getUrl(routes.DATAFLOW, { dataflowId }, true)),
        href: getUrl(routes.DATAFLOW, { dataflowId }, true),
        icon: 'archive',
        label: resources.messages['dataflow']
      },
      { label: resources.messages['datasetDesigner'], icon: 'pencilRuler' }
    ]);
    leftSideBarContext.removeModels();
    callSetMetaData();
  }, []);

  useEffect(() => {
    if (validationContext.opener == 'validationsListDialog' && validationContext.reOpenOpener)
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

  const callSetMetaData = async () => {
    const metaData = await getMetadata({ datasetId, dataflowId });
    designerDispatch({
      type: 'GET_METADATA',
      payload: { metaData, dataflowName: metaData.dataflow.name, schemaName: metaData.dataset.name }
    });
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

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } });
    } finally {
      isLoading(false);
    }
  };

  const isLoading = value => designerDispatch({ type: 'IS_LOADING', payload: { value } });

  const manageDialogs = (dialog, value, secondDialog, secondValue) => {
    designerDispatch({ type: 'MANAGE_DIALOGS', payload: { dialog, value, secondDialog, secondValue } });
  };

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

  const onHideValidationsDialog = () => {
    if (validationContext.opener == 'validationsListDialog' && validationContext.reOpenOpener) {
      validationContext.onResetOpener();
    }
    manageDialogs('validationListDialogVisible', false);
  };

  const onKeyChange = event => {
    if (event.key === 'Escape') {
      designerDispatch({ type: 'ON_UPDATE_DESCRIPTION', payload: { value: designerState.initialDatasetDescription } });
    } else if (event.key == 'Enter') {
      event.preventDefault();
      onBlurDescription(event.target.value);
    }
  };

  const onLoadSchema = () => {
    try {
      const getDatasetSchemaId = async () => {
        const dataset = await DatasetService.schemaById(datasetId);
        designerDispatch({
          type: 'GET_DATASET_DATA',
          payload: {
            description: dataset.datasetSchemaDescription,
            schemaId: dataset.datasetSchemaId,
            tables: dataset.tables
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

  const onUpdateDescription = async description => {
    try {
      await DatasetService.updateDatasetDescriptionDesign(datasetId, description);
    } catch (error) {
      console.error('Error during datasetSchema Description update: ', error);
    } finally {
    }
  };

  const onUpdateTable = tables => designerDispatch({ type: 'ON_UPDATE_TABLES', payload: { tables } });

  const renderActionButtonsValidationDialog = (
    <Fragment>
      <Button
        className="p-button-primary p-button-animated-blink"
        icon={'plus'}
        label={resources.messages['create']}
        onClick={() => {
          validationContext.onOpenModalFronOpener('validationsListDialog');
          onHideValidationsDialog();
        }}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => onHideValidationsDialog()}
      />
    </Fragment>
  );

  const renderSwitchView = () => (
    <div className={styles.switchDivInput}>
      <div className={styles.switchDiv}>
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
    <Dialog
      footer={renderUniqueConstraintsFooter}
      header={resources.messages['uniqueConstraints']}
      onHide={() => manageDialogs('uniqueConstraintListDialogVisible', false)}
      style={{ width: '70%' }}
      visible={designerState.uniqueConstraintListDialogVisible}>
      <UniqueConstraints
        datasetSchemaId={designerState.datasetSchemaId}
        tableData={designerState.datasetSchemaAllTables}
      />
    </Dialog>
  );

  const renderUniqueConstraintsFooter = (
    <Fragment>
      <div className="p-toolbar-group-left">
        <Button
          className="p-button-primary p-button-animated-blink"
          icon={'plus'}
          label={resources.messages['add']}
          onClick={() =>
            manageDialogs('uniqueConstraintListDialogVisible', false, 'isManageUniqueConstraintDialogVisible', true)
          }
        />
      </div>
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => manageDialogs('uniqueConstraintListDialogVisible', false)}
      />
    </Fragment>
  );

  const validationsListDialog = () => {
    if (designerState.validationListDialogVisible) {
      return (
        <Dialog
          className={styles.paginatorValidationViewer}
          dismissableMask={true}
          footer={renderActionButtonsValidationDialog}
          header={resources.messages['qcRules']}
          onHide={() => onHideValidationsDialog()}
          style={{ width: '90%' }}
          visible={designerState.validationListDialogVisible}>
          <TabsValidations
            dataset={designerState.metaData.dataset}
            datasetSchemaAllTables={designerState.datasetSchemaAllTables}
            datasetSchemaId={designerState.datasetSchemaId}
            onHideValidationsDialog={onHideValidationsDialog}
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
            className={styles.datasetDescription}
            collapsedHeight={40}
            expandableOnClick={true}
            key="datasetDescription"
            onBlur={e => onBlurDescription(e.target.value)}
            onChange={e => designerDispatch({ type: 'ON_UPDATE_DESCRIPTION', payload: { value: e.target.value } })}
            onFocus={e => designerDispatch({ type: 'INITIAL_DATASET_DESCRIPTION', payload: { value: e.target.value } })}
            onKeyDown={e => onKeyChange(e)}
            placeholder={resources.messages['newDatasetSchemaDescriptionPlaceHolder']}
            value={designerState.datasetDescription || ''}
          />

          <Toolbar>
            <div className="p-toolbar-group-right">
              {/* <Button
              className={`p-button-rounded p-button-secondary-transparent`}
              disabled={true}
              icon={'clock'}
              label={resources.messages['events']}
              onClick={null}
            /> */}
              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  designerState.datasetHasData ? ' p-button-animated-blink' : null
                }`}
                disabled={!designerState.datasetHasData}
                icon={'validate'}
                iconClasses={null}
                label={resources.messages['validate']}
                onClick={() => manageDialogs('validateDialogVisible', true)}
                ownButtonClasses={null}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
                disabled={false}
                icon={'list'}
                iconClasses={null}
                label={resources.messages['qcRules']}
                onClick={() => manageDialogs('validationListDialogVisible', true)}
                ownButtonClasses={null}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
                icon={'list'}
                label={resources.messages['uniqueConstraints']}
                onClick={() => manageDialogs('uniqueConstraintListDialogVisible', true)}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent`}
                disabled={true}
                icon={'dashboard'}
                label={resources.messages['dashboards']}
                onClick={() => null}
              />
              <Button
                className={`p-button-rounded p-button-secondary-transparent ${
                  !designerState.hasWritePermissions ? 'p-button-animated-blink' : null
                }`}
                disabled={designerState.hasWritePermissions}
                icon={'camera'}
                label={resources.messages['snapshots']}
                onClick={() => setIsSnapshotsBarVisible(!isSnapshotsBarVisible)}
              />
              <Button
                className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
                icon={'refresh'}
                label={resources.messages['refresh']}
                onClick={() => onLoadSchema()}
              />
            </div>
          </Toolbar>
        </div>
        {renderSwitchView()}
        <TabsDesigner
          datasetSchemas={designerState.datasetSchemas}
          editable={true}
          history={history}
          isPreviewModeOn={designerState.isPreviewModeOn}
          onChangeReference={onChangeReference}
          onLoadTableData={onLoadTableData}
          onUpdateTable={onUpdateTable}
        />
        <Snapshots
          isLoadingSnapshotListData={isLoadingSnapshotListData}
          isSnapshotDialogVisible={isSnapshotDialogVisible}
          setIsSnapshotDialogVisible={setIsSnapshotDialogVisible}
          snapshotListData={snapshotListData}
        />
        {validationsListDialog()}
        {renderUniqueConstraintsDialog()}

        <ManageUniqueConstraint designerState={designerState} manageDialogs={manageDialogs} />

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
      </div>
    </SnapshotContext.Provider>
  );
});
