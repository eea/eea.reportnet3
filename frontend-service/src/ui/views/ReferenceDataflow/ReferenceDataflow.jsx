/* eslint-disable jsx-a11y/anchor-is-valid */
import { useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';

import styles from './ReferenceDataflow.module.scss';

import { config } from 'conf';

import { ApiKeyDialog } from 'ui/views/_components/ApiKeyDialog';
import { BigButtonListReference } from './_components/BigButtonListReference';
import { Button } from 'ui/views/_components/Button';
import { MainLayout } from 'ui/views/_components/Layout';
import { ReferencingDataflows } from './_components/ReferencingDataflows';
import { routes } from 'ui/routes';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from 'ui/views/_components/Title';
import { ShareRights } from 'ui/views/_components/ShareRights';

import { DatasetService } from 'core/services/Dataset';
import { ReferenceDataflowService } from 'core/services/ReferenceDataflow';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { dataflowReducer } from './_functions/Reducers/dataflowReducer';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';
import { useLeftSideBar } from './_functions/Hooks/useLeftSideBar';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { Dialog } from 'ui/views/_components/Dialog/';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { ManageReferenceDataflow } from 'ui/views/_components/ManageReferenceDataflow';

const ReferenceDataflow = withRouter(({ history, match }) => {
  const {
    params: { referenceDataflowId }
  } = match;

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const dataflowInitialState = {
    data: {},
    description: '',
    designDatasetSchemas: [],
    error: null,
    isApiKeyDialogVisible: false,
    isCreatingReferenceDatasets: false,
    isEditDialogVisible: false,
    isManageRequestersDialogVisible: false,
    isPropertiesDialogVisible: false,
    isReferencingDataflowsDialogVisible: false,
    isUserRightManagementDialogVisible: false,
    name: '',
    refresh: false,
    requestStatus: 'idle',
    status: '',
    updatedDatasetSchema: []
  };

  const [dataflowState, dataflowDispatch] = useReducer(dataflowReducer, dataflowInitialState);

  const setUpdatedDatasetSchema = updatedData =>
    dataflowDispatch({ type: 'SET_UPDATED_DATASET_SCHEMA', payload: { updatedData } });

  useEffect(() => {
    onLoadReferenceDataflow();
  }, [dataflowState.refresh]);

  useBreadCrumbs({
    currentPage: CurrentPage.REFERENCE_DATAFLOW,
    referenceDataflowId,
    history,
    matchParams: match.params
  });

  useLeftSideBar(dataflowState, getLeftSidebarButtonsVisibility, manageDialogs);

  useCheckNotifications(['REFERENCE_DATAFLOW_PROCESSED_EVENT', 'COPY_DATASET_SCHEMA_COMPLETED_EVENT'], refreshPage);
  useCheckNotifications(['REFERENCE_DATAFLOW_PROCESS_FAILED_EVENT'], setIsCreatingReferenceDatasets, false);

  function manageDialogs(dialog, value, secondDialog, secondValue) {
    dataflowDispatch({
      type: 'MANAGE_DIALOGS',
      payload: { dialog, value, secondDialog, secondValue, deleteInput: '' }
    });
  }

  const onEditDataflow = (name, description) => {
    dataflowDispatch({ type: 'ON_EDIT_DATAFLOW', payload: { description, name } });
  };

  function refreshPage() {
    dataflowDispatch({ type: 'REFRESH_PAGE' });
  }

  function setIsCreatingReferenceDatasets(isCreatingReferenceDatasets) {
    dataflowDispatch({ type: 'SET_IS_CREATING_REFERENCE_DATASETS', payload: { isCreatingReferenceDatasets } });
  }

  const setIsUserRightManagementDialogVisible = isVisible => {
    manageDialogs('isUserRightManagementDialogVisible', isVisible);
  };

  const onSaveDatasetName = async (value, index) => {
    try {
      await DatasetService.updateSchemaNameById(
        dataflowState.designDatasetSchemas[index].datasetId,
        encodeURIComponent(value)
      );
      const updatedTitles = [...dataflowState.updatedDatasetSchema];
      updatedTitles[index].schemaName = value;
      setUpdatedDatasetSchema(updatedTitles);
    } catch (error) {
      if (error?.response?.status === 400) {
        notificationContext.add({
          type: 'DATASET_SCHEMA_CREATION_ERROR_INVALID_NAME',
          content: { schemaName: value }
        });
      }
    }
  };

  const onLoadReferenceDataflow = async () => {
    dataflowDispatch({ type: 'LOADING_STARTED' });

    try {
      const referenceDataflowResponse = await ReferenceDataflowService.referenceDataflow(referenceDataflowId);
      const referenceDataflow = referenceDataflowResponse.data;

      dataflowDispatch({
        type: 'LOADING_SUCCESS',
        payload: {
          data: referenceDataflow,
          description: referenceDataflow.description,
          name: referenceDataflow.name,
          status: referenceDataflow.status
        }
      });

      if (!isEmpty(referenceDataflow.designDatasets)) {
        referenceDataflow.designDatasets.forEach((schema, idx) => {
          schema.index = idx;
        });

        dataflowDispatch({
          type: 'SET_DESIGN_DATASET_SCHEMAS',
          payload: { designDatasets: referenceDataflow.designDatasets }
        });

        const datasetSchemaInfo = [];
        referenceDataflow.designDatasets.forEach(schema => {
          datasetSchemaInfo.push({ schemaName: schema.datasetSchemaName, schemaIndex: schema.index });
        });

        setUpdatedDatasetSchema(datasetSchemaInfo);
      } else {
        dataflowDispatch({ type: 'SET_DESIGN_DATASET_SCHEMAS', payload: { designDatasets: [] } });
      }
    } catch (error) {
      notificationContext.add({ type: 'LOADING_REFERENCE_DATAFLOW_ERROR', error });
      history.push(getUrl(routes.DATAFLOWS));
    }
  };

  const referencingDataflowsDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => manageDialogs('isReferencingDataflowsDialogVisible', false)}
    />
  );
  const propertiesDataflowsDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => manageDialogs('isPropertiesDialogVisible', false)}
    />
  );

  const shareRightsFooterDialogFooter = (
    <div className={styles.buttonsRolesFooter}>
      <Button
        className={`p-button-secondary p-button-animated-blink p-button-left-aligned`}
        icon={'plus'}
        label={resources.messages['add']}
        onClick={() => manageDialogs('isUserRightManagementDialogVisible', true)}
      />
      <Button
        className={`p-button-secondary p-button-animated-blink p-button-right-aligned`}
        icon={'cancel'}
        label={resources.messages['cancel']}
        onClick={() => manageDialogs(`isManageRequestersDialogVisible`, false)}
      />
    </div>
  );

  const requesterRoleOptionsOpenStatus = [
    { label: config.permissions.roles.CUSTODIAN.label, role: config.permissions.roles.CUSTODIAN.key },
    { label: config.permissions.roles.STEWARD.label, role: config.permissions.roles.STEWARD.key }
  ];

  function getLeftSidebarButtonsVisibility() {
    return {
      apiKeyBtn: true,
      editBtn: dataflowState.status === config.dataflowStatus.DESIGN,
      manageRequestersBtn: dataflowState.status === config.dataflowStatus.DESIGN,
      propertiesBtn: true,
      reportingDataflows: dataflowState.status === config.dataflowStatus.OPEN
    };
  }

  const layout = children => (
    <MainLayout leftSideBarConfig={{ isCustodian: dataflowState.isCustodian, buttons: [] }}>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  if (dataflowState.requestStatus === 'pending') return layout(<Spinner />);

  return layout(
    <div className="rep-row">
      <div className={`rep-col-12 rep-col-sm-12`}>
        <Title
          icon="clone"
          iconSize="4rem"
          subtitle={resources.messages['referenceDataflowCrumbLabel']}
          title={dataflowState.name}
        />
      </div>
      <div className={`rep-col-12 rep-col-sm-12`}>
        <BigButtonListReference
          className="dataflow-big-buttons-help-step"
          dataflowId={referenceDataflowId}
          dataflowState={dataflowState}
          onSaveName={onSaveDatasetName}
          onUpdateData={refreshPage}
          setIsCreatingReferenceDatasets={setIsCreatingReferenceDatasets}
          setUpdatedDatasetSchema={setUpdatedDatasetSchema}
        />
      </div>

      {dataflowState.isPropertiesDialogVisible && (
        <Dialog
          footer={propertiesDataflowsDialogFooter}
          header={resources.messages['properties']}
          onHide={() => manageDialogs('isPropertiesDialogVisible', false)}
          visible={dataflowState.isPropertiesDialogVisible}>
          <h3>{resources.messages['propertiesModalTitle']}</h3>
          <p>
            {resources.messages['propertiesModalDataflowNameLabel']}: {dataflowState.name}
          </p>
          <p>
            {resources.messages['propertiesModalDataflowDescriptionLabel']}: {dataflowState.description}
          </p>
          <p>
            {resources.messages['propertiesModalDataflowStatusLabel']}: {dataflowState.status}
          </p>
        </Dialog>
      )}

      {dataflowState.isEditDialogVisible && (
        <ManageReferenceDataflow
          dataflowId={referenceDataflowId}
          history={history}
          isEditing
          isVisible={dataflowState.isEditDialogVisible}
          manageDialogs={manageDialogs}
          metadata={{ name: dataflowState.name, description: dataflowState.description, status: dataflowState.status }}
          onEditDataflow={onEditDataflow}
        />
      )}

      {dataflowState.isApiKeyDialogVisible && (
        <ApiKeyDialog
          dataflowId={referenceDataflowId}
          isApiKeyDialogVisible={dataflowState.isApiKeyDialogVisible}
          isCustodian={true}
          manageDialogs={manageDialogs}
          match={match}
        />
      )}

      {dataflowState.isReferencingDataflowsDialogVisible && (
        <Dialog
          footer={referencingDataflowsDialogFooter}
          header={resources.messages['referencingDataflowsDialogHeader']}
          onHide={() => manageDialogs('isReferencingDataflowsDialogVisible', false)}
          visible={dataflowState.isReferencingDataflowsDialogVisible}>
          <ReferencingDataflows referenceDataflowId={referenceDataflowId} />
        </Dialog>
      )}

      {dataflowState.isManageRequestersDialogVisible && (
        <Dialog
          footer={shareRightsFooterDialogFooter}
          header={resources.messages['manageRequestersRights']}
          onHide={() => manageDialogs('isManageRequestersDialogVisible', false)}
          visible={dataflowState.isManageRequestersDialogVisible}>
          <ShareRights
            addConfirmHeader={resources.messages[`addRequesterConfirmHeader`]}
            addErrorNotificationKey={'ADD_REQUESTER_ERROR'}
            columnHeader={resources.messages['requestersEmailColumn']}
            dataflowId={referenceDataflowId}
            deleteColumnHeader={resources.messages['deleteRequesterButtonTableHeader']}
            deleteConfirmHeader={resources.messages[`requestersRightsDialogConfirmDeleteHeader`]}
            deleteConfirmMessage={resources.messages[`requestersRightsDialogConfirmDeleteQuestion`]}
            deleteErrorNotificationKey={'DELETE_REQUESTER_ERROR'}
            editConfirmHeader={resources.messages[`editRequesterConfirmHeader`]}
            getErrorNotificationKey={'GET_REQUESTERS_ERROR'}
            isUserRightManagementDialogVisible={dataflowState.isUserRightManagementDialogVisible}
            placeholder={resources.messages['manageRolesRequesterDialogInputPlaceholder']}
            roleOptions={requesterRoleOptionsOpenStatus}
            setIsUserRightManagementDialogVisible={setIsUserRightManagementDialogVisible}
            updateErrorNotificationKey={'UPDATE_REQUESTER_ERROR'}
            userType={'requester'}
          />
        </Dialog>
      )}
    </div>
  );
});

export { ReferenceDataflow };
