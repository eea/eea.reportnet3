import React, { useState, useEffect, useContext, useRef } from 'react';
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

import { DatasetDesignerUtils } from './Utils/DatasetDesignerUtils';

import { useDatasetDesigner } from 'ui/views/_components/Snapshots/_hooks/useDatasetDesigner';

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

  const getUrlParamValue = param => {
    let value = '';
    let queryString = window.location.search;
    const params = queryString.substring(1, queryString.length).split('&');
    params.forEach(parameter => {
      if (parameter.includes(param)) {
        value = parameter.split('=')[1];
      }
    });
    return param === 'tab' ? Number(value) : value === 'true';
  };

  const [dataflowName, setDataflowName] = useState('');
  const [datasetDescription, setDatasetDescription] = useState('');
  const [datasetHasData, setDatasetHasData] = useState(false);
  const [datasetSchemaAllTables, setDatasetSchemaAllTables] = useState([]);
  const [datasetSchemaId, setDatasetSchemaId] = useState('');
  const [datasetSchemaName, setDatasetSchemaName] = useState('');
  const [datasetSchemas, setDatasetSchemas] = useState([]);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [initialDatasetDescription, setInitialDatasetDescription] = useState();
  const [isLoading, setIsLoading] = useState(false);
  const [isPreviewModeOn, setIsPreviewModeOn] = useState(getUrlParamValue('design'));
  const [metaData, setMetaData] = useState({});
  const [uniqueConstraintDialogVisible, setUniqueConstraintDialogVisible] = useState(false);
  const [uniqueConstraintListDialogVisible, setUniqueConstraintListDialogVisible] = useState(false);
  const [validateDialogVisible, setValidateDialogVisible] = useState(false);
  const [validationListDialogVisible, setValidationListDialogVisible] = useState(false);

  const {
    isLoadingSnapshotListData,
    isSnapshotDialogVisible,
    isSnapshotsBarVisible,
    setIsSnapshotDialogVisible,
    setIsSnapshotsBarVisible,
    snapshotDispatch,
    snapshotListData,
    snapshotState
  } = useDatasetDesigner(dataflowId, datasetId, datasetSchemaId);

  useEffect(() => {
    try {
      setIsLoading(true);
      const getDatasetSchemaId = async () => {
        const dataset = await DatasetService.schemaById(datasetId);
        setDatasetDescription(dataset.datasetSchemaDescription);
        setDatasetSchemaId(dataset.datasetSchemaId);
        setDatasetSchemaAllTables(dataset.tables);
      };
      const getDatasetSchemas = async () => {
        const datasetSchemasDTO = await DataflowService.getAllSchemas(dataflowId);
        setDatasetSchemas(datasetSchemasDTO);
      };
      getDatasetSchemaId();
      getDatasetSchemas();
    } catch (error) {
      console.error(`Error while loading schema: ${error}`);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setHasWritePermissions(
        UserService.hasPermission(user, [config.permissions.PROVIDER], `${config.permissions.DATASET}${datasetId}`)
      );
    }
  }, [user]);

  useEffect(() => {
    breadCrumbContext.add([
      {
        label: resources.messages['dataflows'],
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow'],
        icon: 'archive',
        href: getUrl(
          routes.DATAFLOW,
          {
            dataflowId
          },
          true
        ),
        command: () =>
          history.push(
            getUrl(
              routes.DATAFLOW,
              {
                dataflowId
              },
              true
            )
          )
      },
      { label: resources.messages['datasetDesigner'], icon: 'pencilRuler' }
    ]);
    leftSideBarContext.removeModels();
    getDataflowName();
    onLoadDatasetSchemaName();
    callSetMetaData();
  }, []);

  useEffect(() => {
    if (validationContext.opener == 'uniqueConstraintListDialog' && validationContext.reOpenOpener)
      setUniqueConstraintListDialogVisible(true);
  }, [validationContext]);

  useEffect(() => {
    if (validationContext.opener == 'validationsListDialog' && validationContext.reOpenOpener)
      setValidationListDialogVisible(true);
  }, [validationContext]);

  useEffect(() => {
    if (validationListDialogVisible) {
      validationContext.resetReOpenOpener();
    }
  }, [validationListDialogVisible]);

  useEffect(() => {
    if (window.location.search !== '') {
      changeUrl();
    }
  }, [isPreviewModeOn]);

  const callSetMetaData = async () => {
    setMetaData(await getMetadata({ datasetId, dataflowId }));
  };

  const changeUrl = () => {
    window.history.replaceState(
      null,
      null,
      `?tab=${getUrlParamValue('tab')}${!isUndefined(isPreviewModeOn) ? `&design=${isPreviewModeOn}` : ''}`
    );
  };

  const getDataflowName = async () => {
    const dataflowData = await DataflowService.dataflowDetails(dataflowId);
    setDataflowName(dataflowData.name);
  };

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      notificationContext.add({
        type: 'GET_METADATA_ERROR',
        content: {
          dataflowId,
          datasetId
        }
      });
    }
  };

  const renderSwitchView = () => (
    <div className={styles.switchDivInput}>
      <div className={styles.switchDiv}>
        <span className={styles.switchTextInput}>{resources.messages['design']}</span>
        <InputSwitch
          checked={isPreviewModeOn}
          // disabled={true}
          // disabled={!isUndefined(fields) ? (fields.length === 0 ? true : false) : false}
          onChange={e => setIsPreviewModeOn(e.value)}
        />
        <span className={styles.switchTextInput}>{resources.messages['preview']}</span>
      </div>
    </div>
  );

  const onBlurDescription = description => {
    if (description !== initialDatasetDescription) {
      onUpdateDescription(description);
    }
  };

  const onChangeReference = (tabs, datasetSchemaId) => {
    const inmDatasetSchemas = [...datasetSchemas];
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
    setDatasetSchemas(inmDatasetSchemas);
  };

  const onConfirmValidate = async () => {
    try {
      setValidateDialogVisible(false);
      await DatasetService.validateDataById(datasetId);
      notificationContext.add({
        type: 'VALIDATE_DATA_INIT',
        content: {
          dataflowId,
          datasetId,
          dataflowName,
          datasetName: datasetSchemaName
        }
      });
    } catch (error) {
      notificationContext.add({
        type: 'VALIDATE_DATA_BY_ID_ERROR',
        content: {
          dataflowId,
          datasetId,
          dataflowName,
          datasetName: datasetSchemaName
        }
      });
    }
  };

  const onLoadTableData = hasData => setDatasetHasData(hasData);

  const onUpdateTable = tables => setDatasetSchemaAllTables(tables);

  const onKeyChange = event => {
    if (event.key === 'Escape') {
      setDatasetDescription(initialDatasetDescription);
    } else if (event.key == 'Enter') {
      event.preventDefault();
      onBlurDescription(event.target.value);
    }
  };

  const onLoadDatasetSchemaName = async () => {
    setIsLoading(true);
    try {
      const dataset = await DatasetService.getMetaData(datasetId);
      setDatasetSchemaName(dataset.datasetSchemaName);
    } catch (error) {
      console.error(`Error while getting datasetSchemaName: ${error}`);
    } finally {
      setIsLoading(false);
    }
  };

  const onUpdateDescription = async description => {
    try {
      const response = await DatasetService.updateDatasetDescriptionDesign(datasetId, description);
      if (response.status < 200 || response.status > 299) {
        console.error('Error during datasetSchema Description update');
      }
    } catch (error) {
      console.error('Error during datasetSchema Description update: ', error);
    } finally {
    }
  };

  const onHideUniqueConstraintsDialog = () => {
    if (validationContext.opener == 'uniqueConstraintListDialog' && validationContext.reOpenOpener) {
      validationContext.onResetOpener();
    }
    setUniqueConstraintListDialogVisible(false);
  };

  const onHideValidationsDialog = () => {
    if (validationContext.opener == 'validationsListDialog' && validationContext.reOpenOpener) {
      validationContext.onResetOpener();
    }
    setValidationListDialogVisible(false);
  };

  const actionButtonsUniqueConstraintDialog = (
    <>
      <Button
        className="p-button-primary p-button-animated-blink"
        icon={'plus'}
        label={resources.messages['create']}
        onClick={() => {
          validationContext.onOpenModalFronOpener('uniqueConstraintsListDialog');
          onHideUniqueConstraintsDialog();
        }}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => onHideUniqueConstraintsDialog()}
      />
    </>
  );

  const actionButtonsValidationDialog = (
    <>
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
    </>
  );

  const uniqueConstraintsListDialog = () => {
    if (uniqueConstraintListDialogVisible) {
      return (
        <Dialog
          className={styles.paginatorValidationViewer}
          dismissableMask={true}
          footer={actionButtonsUniqueConstraintDialog}
          header={resources.messages['uniqueConstraints']}
          onHide={() => {
            onHideUniqueConstraintsDialog();
          }}
          style={{ width: '90%' }}
          visible={uniqueConstraintListDialogVisible}>
          <UniqueConstraints />
        </Dialog>
      );
    }
  };

  const validationsListDialog = () => {
    if (validationListDialogVisible) {
      return (
        <Dialog
          className={styles.paginatorValidationViewer}
          dismissableMask={true}
          footer={actionButtonsValidationDialog}
          header={resources.messages['qcRules']}
          onHide={() => {
            onHideValidationsDialog();
          }}
          style={{ width: '90%' }}
          visible={validationListDialogVisible}>
          <TabsValidations
            dataset={metaData.dataset}
            datasetSchemaAllTables={datasetSchemaAllTables}
            datasetSchemaId={datasetSchemaId}
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

  if (isLoading) {
    return layout(<Spinner />);
  }

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
          subtitle={dataflowName}
          title={`${resources.messages['datasetSchema']}: ${datasetSchemaName}`}
        />
        <h4 className={styles.descriptionLabel}>{resources.messages['newDatasetSchemaDescriptionPlaceHolder']}</h4>
        <div className={styles.ButtonsBar}>
          <InputTextarea
            className={styles.datasetDescription}
            collapsedHeight={40}
            expandableOnClick={true}
            key="datasetDescription"
            onBlur={e => onBlurDescription(e.target.value)}
            onChange={e => setDatasetDescription(e.target.value)}
            onFocus={e => {
              setInitialDatasetDescription(e.target.value);
            }}
            onKeyDown={e => onKeyChange(e)}
            placeholder={resources.messages['newDatasetSchemaDescriptionPlaceHolder']}
            value={datasetDescription || ''}
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
                  datasetHasData ? ' p-button-animated-blink' : null
                }`}
                disabled={!datasetHasData}
                icon={'validate'}
                iconClasses={null}
                label={resources.messages['validate']}
                onClick={() => setValidateDialogVisible(true)}
                ownButtonClasses={null}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
                disabled={false}
                icon={'list'}
                iconClasses={null}
                label={resources.messages['qcRules']}
                onClick={() => setValidationListDialogVisible(true)}
                ownButtonClasses={null}
              />

              <Button
                className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
                disabled={false}
                icon={'list'}
                iconClasses={null}
                label={resources.messages['uniqueConstraints']}
                onClick={() => setUniqueConstraintListDialogVisible(true)}
                ownButtonClasses={null}
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
                  !hasWritePermissions ? 'p-button-animated-blink' : null
                }`}
                disabled={hasWritePermissions}
                icon={'camera'}
                label={resources.messages['snapshots']}
                onClick={() => setIsSnapshotsBarVisible(!isSnapshotsBarVisible)}
              />
            </div>
          </Toolbar>
        </div>
        {renderSwitchView()}
        <TabsDesigner
          datasetSchemas={datasetSchemas}
          editable={true}
          history={history}
          isPreviewModeOn={isPreviewModeOn}
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
        {uniqueConstraintsListDialog()}

        <ConfirmDialog
          header={resources.messages['validateDataset']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          maximizable={false}
          onConfirm={onConfirmValidate}
          onHide={() => setValidateDialogVisible(false)}
          visible={validateDialogVisible}>
          {resources.messages['validateDatasetConfirm']}
        </ConfirmDialog>
      </div>
    </SnapshotContext.Provider>
  );
});
