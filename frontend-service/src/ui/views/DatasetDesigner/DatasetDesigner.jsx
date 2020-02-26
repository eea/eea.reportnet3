import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';
import { isUndefined } from 'lodash';

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

import { useDatasetDesigner } from 'ui/views/_components/Snapshots/_hooks/useDatasetDesigner';

import { getUrl } from 'core/infrastructure/CoreUtils';

export const DatasetDesigner = withRouter(({ match, history }) => {
  const {
    params: { datasetId }
  } = match;

  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [dataflowName, setDataflowName] = useState('');
  const [datasetDescription, setDatasetDescription] = useState('');
  const [datasetSchemaId, setDatasetSchemaId] = useState('');
  const [datasetSchemaName, setDatasetSchemaName] = useState('');
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [initialDatasetDescription, setInitialDatasetDescription] = useState();
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [ruleData, setRuleData] = useState({});
  const [validationListDialogVisible, setValidationListDialogVisible] = useState(false);

  const {
    isLoadingSnapshotListData,
    isSnapshotsBarVisible,
    setIsSnapshotsBarVisible,
    isSnapshotDialogVisible,
    setIsSnapshotDialogVisible,
    snapshotDispatch,
    snapshotListData,
    snapshotState
  } = useDatasetDesigner(match.params.dataflowId, datasetId, datasetSchemaId);

  useEffect(() => {
    try {
      setIsLoading(true);
      const getDatasetSchemaId = async () => {
        const dataset = await DatasetService.schemaById(datasetId);
        setDatasetDescription(dataset.datasetSchemaDescription);
        setDatasetSchemaId(dataset.datasetSchemaId);
      };
      getDatasetSchemaId();
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
        label: resources.messages['dataflowList'],
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
            dataflowId: match.params.dataflowId
          },
          true
        ),
        command: () =>
          history.push(
            getUrl(
              routes.DATAFLOW,
              {
                dataflowId: match.params.dataflowId
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
  }, []);

  const getDataflowName = async () => {
    const dataflowData = await DataflowService.dataflowDetails(match.params.dataflowId);
    setDataflowName(dataflowData.name);
  };

  const onBlurDescription = description => {
    if (description !== initialDatasetDescription) {
      onUpdateDescription(description);
    }
  };

  const onDeleteValidation = async () => {
    try {
      await ValidationService.deleteById(datasetSchemaId, ruleData.ruleId);
    } catch (error) {
      notificationContext.add({
        type: 'DELETE_RULE_ERROR'
      });
    } finally {
      onHideDeleteDialog();
    }
  };

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

  const onHideDeleteDialog = () => {
    setIsDeleteDialogVisible(false);
    setValidationListDialogVisible(true);
    setRuleData({});
  };

  const onHideValidationsDialog = () => {
    setValidationListDialogVisible(false);
  };

  const onShowDeleteDialog = () => {
    setIsDeleteDialogVisible(true);
    setValidationListDialogVisible(false);
  };

  const actionButtonsValidationDialog = (
    <>
      <Button
        className="p-button-primary"
        icon={'plus'}
        label={resources.messages['create']}
        onClick={() => onHideValidationsDialog()}
      />
      <Button
        className="p-button-secondary"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => onHideValidationsDialog()}
      />
    </>
  );

  const renderDeleteConfirmDialog = () => {
    return (
      <ConfirmDialog
        header={resources.messages['deleteTabHeader']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        onConfirm={() => onDeleteValidation()}
        onHide={() => onHideDeleteDialog()}
        visible={isDeleteDialogVisible}>
        {resources.messages['deleteTabConfirm']}
      </ConfirmDialog>
    );
  };

  const ValidationsListDialog = () => {
    if (validationListDialogVisible) {
      return (
        <Dialog
          className={styles.paginatorValidationViewer}
          dismissableMask={true}
          footer={actionButtonsValidationDialog}
          header={resources.messages['titleValidations']}
          maximizable
          onHide={() => onHideValidationsDialog()}
          style={{ width: '80%' }}
          visible={validationListDialogVisible}>
          <TabsValidations
            datasetSchemaId={datasetSchemaId}
            onShowDeleteDialog={onShowDeleteDialog}
            setRuleData={setRuleData}
          />
        </Dialog>
      );
    }
    return <></>;
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
        snapshotState: snapshotState,
        snapshotDispatch: snapshotDispatch,
        isSnapshotsBarVisible: isSnapshotsBarVisible,
        setIsSnapshotsBarVisible: setIsSnapshotsBarVisible
      }}>
      <Title
        title={`${resources.messages['datasetSchema']}: ${datasetSchemaName}`}
        subtitle={dataflowName}
        icon="pencilRuler"
        iconSize="3.4rem"
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
              className={`p-button-rounded p-button-secondary-transparent`}
              disabled={true}
              icon={'validate'}
              label={resources.messages['validate']}
              onClick={() => null}
              ownButtonClasses={null}
              iconClasses={null}
            />

            <Button
              className={`p-button-rounded p-button-secondary-transparent`}
              disabled={false}
              icon={'list'}
              label={resources.messages['qcRules']}
              onClick={() => setValidationListDialogVisible(true)}
              ownButtonClasses={null}
              iconClasses={null}
            />

            <Button
              className={`p-button-rounded p-button-secondary-transparent`}
              disabled={true}
              icon={'dashboard'}
              label={resources.messages['dashboards']}
              onClick={() => null}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent`}
              disabled={hasWritePermissions}
              icon={'camera'}
              label={resources.messages['snapshots']}
              onClick={() => setIsSnapshotsBarVisible(!isSnapshotsBarVisible)}
            />
          </div>
        </Toolbar>
      </div>
      <TabsDesigner editable={true} />
      <Snapshots
        isLoadingSnapshotListData={isLoadingSnapshotListData}
        isSnapshotDialogVisible={isSnapshotDialogVisible}
        setIsSnapshotDialogVisible={setIsSnapshotDialogVisible}
        snapshotListData={snapshotListData}
      />
      <ValidationsListDialog />
      {renderDeleteConfirmDialog()}
      {/* <Dialog
        className={styles.paginatorValidationViewer}
        dismissableMask={true}
        header={resources.messages['titleValidations']}
        maximizable
        onHide={() => setValidationListDialogVisible(false)}
        style={{ width: '80%' }}
        visible={validationListDialogVisible}>
        {/* <ValidationViewer
          datasetId={datasetId}
          datasetName={datasetName}
          hasWritePermissions={hasWritePermissions}
          levelErrorTypes={levelErrorTypes}
          onSelectValidation={onSelectValidation}
          tableSchemaNames={tableSchemaNames}
          visible={validationsVisible}
        /> */}
      {/* <TabsValidations datasetSchemaId={datasetSchemaId} />
      </Dialog>       */}
    </SnapshotContext.Provider>
  );
});
