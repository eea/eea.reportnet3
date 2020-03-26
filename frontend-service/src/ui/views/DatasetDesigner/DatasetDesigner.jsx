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
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { MainLayout } from 'ui/views/_components/Layout';
import { Snapshots } from 'ui/views/_components/Snapshots';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsDesigner } from './_components/TabsDesigner';
import { TabsValidations } from './_components/TabsValidations';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';
import { ValidationService } from 'core/services/Validation';

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

  const [dataflowName, setDataflowName] = useState('');
  const [datasetDescription, setDatasetDescription] = useState('');
  const [datasetHasData, setDatasetHasData] = useState(false);
  const [datasetSchemaId, setDatasetSchemaId] = useState('');
  const [datasetSchemaName, setDatasetSchemaName] = useState('');
  const [datasetSchemas, setDatasetSchemas] = useState([]);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [initialDatasetDescription, setInitialDatasetDescription] = useState();
  const [isLoading, setIsLoading] = useState(false);
  const [metaData, setMetaData] = useState({});
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
    if (validationContext.opener == 'validationsListDialog' && validationContext.reOpenOpener)
      setValidationListDialogVisible(true);
  }, [validationContext]);

  useEffect(() => {
    if (validationListDialogVisible) {
      validationContext.resetReOpenOpener();
    }
  }, [validationListDialogVisible]);

  const callSetMetaData = async () => {
    setMetaData(await getMetadata({ datasetId, dataflowId }));
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

  // const onTableAdd = ()

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

  const onHideValidationsDialog = () => {
    if (validationContext.opener == 'validationsListDialog' && validationContext.reOpenOpener) {
      validationContext.onResetOpener();
    }
    setValidationListDialogVisible(false);
  };

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

  const validationsListDialog = () => {
    if (validationListDialogVisible) {
      return (
        <Dialog
          className={styles.paginatorValidationViewer}
          dismissableMask={true}
          footer={actionButtonsValidationDialog}
          header={resources.messages['qcRules']}
          maximizable
          onHide={() => {
            onHideValidationsDialog();
          }}
          style={{ width: '80%' }}
          visible={validationListDialogVisible}>
          <TabsValidations
            datasetSchemaId={datasetSchemaId}
            dataset={metaData.dataset}
            onHideValidationsDialog={onHideValidationsDialog}
          />
        </Dialog>
      );
    }
  };

  const layout = children => {
    return (
      <MainLayout>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

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
                  !datasetHasData ? ' p-button-animated-blink' : null
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
        <TabsDesigner
          datasetSchemas={datasetSchemas}
          editable={true}
          onChangeReference={onChangeReference}
          onLoadTableData={onLoadTableData}
        />
        <Snapshots
          isLoadingSnapshotListData={isLoadingSnapshotListData}
          isSnapshotDialogVisible={isSnapshotDialogVisible}
          setIsSnapshotDialogVisible={setIsSnapshotDialogVisible}
          snapshotListData={snapshotListData}
        />
        {validationsListDialog()}
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
