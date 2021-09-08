/* eslint-disable jsx-a11y/anchor-is-valid */
import { useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ReferenceDataflow.module.scss';

import { config } from 'conf';

import { ApiKeyDialog } from 'views/_components/ApiKeyDialog';
import { BigButtonListReference } from './_components/BigButtonListReference';
import { Button } from 'views/_components/Button';
import { MainLayout } from 'views/_components/Layout';
import { ReferencingDataflows } from './_components/ReferencingDataflows';
import { routes } from 'conf/routes';
import { Spinner } from 'views/_components/Spinner';
import { Title } from 'views/_components/Title';
import { ShareRights } from 'views/_components/ShareRights';

import { DatasetService } from 'services/DatasetService';
import { ReferenceDataflowService } from 'services/ReferenceDataflowService';
import { UserService } from 'services/UserService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { dataflowReducer } from './_functions/Reducers/dataflowReducer';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';
import { useLeftSideBar } from './_functions/Hooks/useLeftSideBar';

import { CurrentPage } from 'views/_functions/Utils';
import { Dialog } from 'views/_components/Dialog';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { ManageReferenceDataflow } from 'views/_components/ManageReferenceDataflow';

const ReferenceDataflow = withRouter(({ history, match }) => {
  const {
    params: { referenceDataflowId }
  } = match;

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const dataflowInitialState = {
    data: {},
    description: '',
    designDatasetSchemas: [],
    error: null,
    isCustodian: false,
    isApiKeyDialogVisible: false,
    isCreatingReferenceDatasets: false,
    isEditDialogVisible: false,
    isManageRequestersDialogVisible: false,
    isPropertiesDialogVisible: false,
    isReferencingDataflowsDialogVisible: false,
    isReferenceStateDialogVisible: false,
    isUserRightManagementDialogVisible: false,
    name: '',
    refresh: false,
    requestStatus: 'idle',
    status: '',
    updatedDatasetSchema: [],
    updatable: false
  };

  const [dataflowState, dataflowDispatch] = useReducer(dataflowReducer, dataflowInitialState);

  const setUpdatedDatasetSchema = updatedData =>
    dataflowDispatch({ type: 'SET_UPDATED_DATASET_SCHEMA', payload: { updatedData } });

  useEffect(() => {
    userContext.setCurrentDataflowType(config.dataflowType.REFERENCE.value);
  }, []);

  useEffect(() => {
    onLoadReferenceDataflow();
  }, [dataflowState.refresh]);

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) onLoadPermissions();
  }, [userContext]);

  useBreadCrumbs({
    currentPage: CurrentPage.REFERENCE_DATAFLOW,
    referenceDataflowId,
    history,
    matchParams: match.params
  });

  useLeftSideBar(dataflowState, getLeftSidebarButtonsVisibility, manageDialogs);

  useCheckNotifications(['REFERENCE_DATAFLOW_PROCESSED_EVENT', 'COPY_DATASET_SCHEMA_COMPLETED_EVENT'], refreshPage);
  useCheckNotifications(['REFERENCE_DATAFLOW_PROCESS_FAILED_EVENT'], setIsCreatingReferenceDatasets, false);
  useCheckNotifications(['DELETE_DATAFLOW_COMPLETED_EVENT'], goToDataflowsPage);

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
    onRefreshToken();
  }

  function goToDataflowsPage() {
    history.push(getUrl(routes.DATAFLOWS));
  }

  const onRefreshToken = async () => {
    try {
      const userObject = await UserService.refreshToken();
      userContext.onTokenRefresh(userObject);
    } catch (error) {
      console.error('ReferenceDataflow - onRefreshToken.', error);
      notificationContext.add({
        key: 'TOKEN_REFRESH_ERROR',
        content: {}
      });
      await UserService.logout();
      userContext.onLogout();
    }
  };

  function setIsCreatingReferenceDatasets(isCreatingReferenceDatasets) {
    dataflowDispatch({ type: 'SET_IS_CREATING_REFERENCE_DATASETS', payload: { isCreatingReferenceDatasets } });
  }

  const setIsUserRightManagementDialogVisible = isVisible => {
    manageDialogs('isUserRightManagementDialogVisible', isVisible);
  };

  const onSaveDatasetName = async (value, index) => {
    try {
      await DatasetService.updateDatasetNameDesign(
        dataflowState.designDatasetSchemas[index].datasetId,
        encodeURIComponent(value)
      );
      const updatedTitles = [...dataflowState.updatedDatasetSchema];
      updatedTitles[index].schemaName = value;
      setUpdatedDatasetSchema(updatedTitles);
    } catch (error) {
      console.error('ReferenceDataflow - onSaveDatasetName.', error);
      if (error?.response?.status === 400) {
        notificationContext.add({
          type: 'DATASET_SCHEMA_CREATION_ERROR_INVALID_NAME',
          content: { schemaName: value }
        });
      }
    }
  };

  const onLoadPermissions = () => {
    const isCustodian = userContext.hasContextAccessPermission(
      config.permissions.prefixes.DATAFLOW,
      referenceDataflowId,
      [config.permissions.roles.CUSTODIAN.key, config.permissions.roles.STEWARD.key]
    );

    dataflowDispatch({ type: 'LOAD_PERMISSIONS', payload: { isCustodian } });
  };

  const onLoadReferenceDataflow = async () => {
    dataflowDispatch({ type: 'LOADING_STARTED' });

    try {
      const referenceDataflow = await ReferenceDataflowService.get(referenceDataflowId);

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
      console.error('ReferenceDataflow - onLoadReferenceDataflow.', error);
      notificationContext.add({ type: 'LOADING_REFERENCE_DATAFLOW_ERROR', error });
      history.push(getUrl(routes.DATAFLOWS));
    }
  };

  const referencingDataflowsDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resourcesContext.messages['close']}
      onClick={() => manageDialogs('isReferencingDataflowsDialogVisible', false)}
    />
  );

  const propertiesDataflowsDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resourcesContext.messages['close']}
      onClick={() => manageDialogs('isPropertiesDialogVisible', false)}
    />
  );

  const shareRightsFooterDialogFooter = (
    <div className={styles.buttonsRolesFooter}>
      <Button
        className={`p-button-secondary p-button-animated-blink p-button-left-aligned`}
        icon={'plus'}
        label={resourcesContext.messages['add']}
        onClick={() => manageDialogs('isUserRightManagementDialogVisible', true)}
      />
      <Button
        className={`p-button-secondary p-button-animated-blink p-button-right-aligned`}
        icon={'cancel'}
        label={resourcesContext.messages['cancel']}
        onClick={() => manageDialogs(`isManageRequestersDialogVisible`, false)}
      />
    </div>
  );

  const requesterRoleOptions = [
    { label: config.permissions.roles.CUSTODIAN.label, role: config.permissions.roles.CUSTODIAN.key },
    { label: config.permissions.roles.STEWARD.label, role: config.permissions.roles.STEWARD.key }
  ];

  function getLeftSidebarButtonsVisibility() {
    return {
      apiKeyBtn: true,
      editBtn: dataflowState.status === config.dataflowStatus.DESIGN,
      manageRequestersBtn: dataflowState.isCustodian,
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
          subtitle={resourcesContext.messages['referenceDataflowCrumbLabel']}
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
          header={resourcesContext.messages['properties']}
          onHide={() => manageDialogs('isPropertiesDialogVisible', false)}
          visible={dataflowState.isPropertiesDialogVisible}>
          <h3>{resourcesContext.messages['propertiesModalTitle']}</h3>
          <p>
            {resourcesContext.messages['propertiesModalDataflowNameLabel']}: {dataflowState.name}
          </p>
          <p>
            {resourcesContext.messages['propertiesModalDataflowDescriptionLabel']}: {dataflowState.description}
          </p>
          <p>
            {resourcesContext.messages['propertiesModalDataflowStatusLabel']}: {dataflowState.status}
          </p>
        </Dialog>
      )}

      {dataflowState.isEditDialogVisible && (
        <ManageReferenceDataflow
          dataflowId={referenceDataflowId}
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
          header={resourcesContext.messages['referencingDataflowsDialogHeader']}
          onHide={() => manageDialogs('isReferencingDataflowsDialogVisible', false)}
          visible={dataflowState.isReferencingDataflowsDialogVisible}>
          <ReferencingDataflows referenceDataflowId={referenceDataflowId} />
        </Dialog>
      )}

      {dataflowState.isManageRequestersDialogVisible && (
        <Dialog
          footer={shareRightsFooterDialogFooter}
          header={resourcesContext.messages['manageRequestersRights']}
          onHide={() => manageDialogs('isManageRequestersDialogVisible', false)}
          visible={dataflowState.isManageRequestersDialogVisible}>
          <ShareRights
            addConfirmHeader={resourcesContext.messages[`addRequesterConfirmHeader`]}
            addErrorNotificationKey={'ADD_REQUESTER_ERROR'}
            columnHeader={resourcesContext.messages['requestersEmailColumn']}
            dataflowId={referenceDataflowId}
            deleteColumnHeader={resourcesContext.messages['deleteRequesterButtonTableHeader']}
            deleteConfirmHeader={resourcesContext.messages[`requestersRightsDialogConfirmDeleteHeader`]}
            deleteConfirmMessage={resourcesContext.messages[`requestersRightsDialogConfirmDeleteQuestion`]}
            deleteErrorNotificationKey={'DELETE_REQUESTER_ERROR'}
            editConfirmHeader={resourcesContext.messages[`editRequesterConfirmHeader`]}
            getErrorNotificationKey={'GET_REQUESTERS_ERROR'}
            isUserRightManagementDialogVisible={dataflowState.isUserRightManagementDialogVisible}
            placeholder={resourcesContext.messages['manageRolesRequesterDialogInputPlaceholder']}
            roleOptions={requesterRoleOptions}
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
