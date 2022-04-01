import { useContext, useEffect, useReducer } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ReferenceDataflow.module.scss';

import { config } from 'conf';

import { ApiKeyDialog } from 'views/_components/ApiKeyDialog';
import { BigButtonListReference } from './_components/BigButtonListReference';
import { Button } from 'views/_components/Button';
import { DatasetsInfo } from 'views/_components/DatasetsInfo';
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
import { useFilters } from 'views/_functions/Hooks/useFilters';
import { useLeftSideBar } from './_functions/Hooks/useLeftSideBar';

import { CurrentPage } from 'views/_functions/Utils';
import { Dialog } from 'views/_components/Dialog';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { ManageReferenceDataflow } from 'views/_components/ManageReferenceDataflow';

export const ReferenceDataflow = () => {
  const navigate = useNavigate();
  const { referenceDataflowId } = useParams();

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const dataflowInitialState = {
    data: {},
    dataflowType: '',
    description: '',
    designDatasetSchemas: [],
    error: null,
    isAdmin: false,
    isRightPermissionsChanged: false,
    isApiKeyDialogVisible: false,
    isCustodianUser: false,
    isCreatingReferenceDatasets: false,
    isCustodian: false,
    isEditDialogVisible: false,
    isDatasetsInfoDialogVisible: false,
    isImportingDataflow: false,
    isLoading: false,
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
    onLoadReferenceDataflow();
  }, [dataflowState.refresh]);

  useEffect(() => {
    if (!isNil(userContext)) {
      onLoadPermissions();
    }
  }, [userContext]);

  const { resetFiltersState: resetDatasetInfoFiltersState } = useFilters('datasetInfo');
  const { resetFiltersState: resetReferencingDataflowsFiltersState } = useFilters('referencingDataflows');

  useBreadCrumbs({ currentPage: CurrentPage.REFERENCE_DATAFLOW, referenceDataflowId });

  const getLeftSidebarButtonsVisibility = () => ({
    apiKeyBtn: dataflowState.isCustodian,
    datasetsInfoBtn: dataflowState.isAdmin,
    editBtn:
      (dataflowState.status === config.dataflowStatus.DESIGN && dataflowState.isCustodian) || dataflowState.isAdmin,
    manageRequestersBtn: dataflowState.isAdmin || dataflowState.isCustodian,
    propertiesBtn: true,
    reportingDataflowsBtn:
      dataflowState.status !== config.dataflowStatus.DESIGN &&
      (dataflowState.isCustodian || dataflowState.isCustodianUser)
  });

  const onImportSchemaIsCompleted = () => {
    onRefreshToken();
    refreshPage();
  };

  useLeftSideBar(dataflowState, getLeftSidebarButtonsVisibility, manageDialogs);

  useCheckNotifications(['REFERENCE_DATAFLOW_PROCESSED_EVENT', 'COPY_DATASET_SCHEMA_COMPLETED_EVENT'], refreshPage);
  useCheckNotifications(['REFERENCE_DATAFLOW_PROCESS_FAILED_EVENT'], setIsCreatingReferenceDatasets, false);
  useCheckNotifications(['DELETE_DATAFLOW_COMPLETED_EVENT'], goToDataflowsPage);
  useCheckNotifications(['IMPORT_DATASET_SCHEMA_COMPLETED_EVENT'], onImportSchemaIsCompleted);

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
    navigate(getUrl(routes.DATAFLOWS));
  }

  const onRefreshToken = async () => {
    try {
      const userObject = await UserService.refreshToken();
      userContext.onTokenRefresh(userObject);
    } catch (error) {
      console.error('ReferenceDataflow - onRefreshToken.', error);
      notificationContext.add(
        {
          type: 'TOKEN_REFRESH_ERROR',
          content: {}
        },
        true
      );
      await UserService.logout();
      userContext.onLogout();
    } finally {
      setIsLoading(false);
    }
  };

  function setIsCreatingReferenceDatasets(isCreatingReferenceDatasets) {
    dataflowDispatch({ type: 'SET_IS_CREATING_REFERENCE_DATASETS', payload: { isCreatingReferenceDatasets } });
  }

  function setIsImportingDataflow(isImportingDataflow) {
    dataflowDispatch({ type: 'SET_IS_IMPORTING_DATAFLOW', payload: { isImportingDataflow } });
  }

  const setRightPermissionsChange = isRightPermissionsChanged => {
    dataflowDispatch({
      type: 'SET_IS_RIGHT_PERMISSIONS_CHANGED',
      payload: { isRightPermissionsChanged }
    });
  };

  const setIsLoading = isLoading => dataflowDispatch({ type: 'SET_IS_LOADING', payload: { isLoading } });

  const setIsUserRightManagementDialogVisible = isVisible => {
    manageDialogs('isUserRightManagementDialogVisible', isVisible);
  };

  const onCloseShareRightsDialog = () => {
    manageDialogs('isManageRequestersDialogVisible', false);
    if (dataflowState.isRightPermissionsChanged) {
      setIsLoading(true);
      onRefreshToken();
      onLoadReferenceDataflow();
      setRightPermissionsChange(false);
    }
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
        notificationContext.add(
          {
            type: 'DATASET_SCHEMA_CREATION_ERROR_INVALID_NAME',
            content: { customContent: { schemaName: value } }
          },
          true
        );
      }
    }
  };

  const onLoadPermissions = () => {
    const isAdmin = userContext.accessRole?.some(role => role === config.permissions.roles.ADMIN.key);
    const isCustodianUser = userContext.accessRole?.some(role => role === config.permissions.roles.CUSTODIAN.key);
    const isStewardUser = userContext.accessRole?.some(role => role === config.permissions.roles.CUSTODIAN.key);

    const isCustodian = userContext.hasContextAccessPermission(
      config.permissions.prefixes.DATAFLOW,
      referenceDataflowId,
      [config.permissions.roles.CUSTODIAN.key]
    );
    const isSteward = userContext.hasContextAccessPermission(
      config.permissions.prefixes.DATAFLOW,
      referenceDataflowId,
      [config.permissions.roles.STEWARD.key]
    );

    dataflowDispatch({
      type: 'LOAD_PERMISSIONS',
      payload: { isAdmin, isCustodian: isSteward || isCustodian, isCustodianUser: isCustodianUser || isStewardUser }
    });
  };

  const onLoadReferenceDataflow = async () => {
    dataflowDispatch({ type: 'LOADING_STARTED' });

    try {
      const referenceDataflow = await ReferenceDataflowService.get(referenceDataflowId);

      dataflowDispatch({
        type: 'LOADING_SUCCESS',
        payload: {
          data: referenceDataflow,
          dataflowType: referenceDataflow.type,
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
      notificationContext.add({ type: 'LOADING_REFERENCE_DATAFLOW_ERROR', error }, true);
      navigate(getUrl(routes.DATAFLOWS));
    } finally {
      setIsLoading(false);
    }
  };

  const renderDialogFooterCloseBtn = modalType => (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon="cancel"
      label={resourcesContext.messages['close']}
      onClick={() => {
        manageDialogs(modalType, false);
        resetDatasetInfoFiltersState();
        resetReferencingDataflowsFiltersState();
      }}
    />
  );

  const shareRightsFooterDialogFooter = (
    <div className={styles.buttonsRolesFooter}>
      <Button
        className="p-button-animated-blink p-button-left-aligned"
        icon="plus"
        label={resourcesContext.messages['add']}
        onClick={() => manageDialogs('isUserRightManagementDialogVisible', true)}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={() => onCloseShareRightsDialog()}
      />
    </div>
  );

  const requesterRoleOptions = [
    { label: config.permissions.roles.CUSTODIAN.label, role: config.permissions.roles.CUSTODIAN.key },
    { label: config.permissions.roles.STEWARD.label, role: config.permissions.roles.STEWARD.key }
  ];

  const layout = children => (
    <MainLayout leftSideBarConfig={{ isCustodian: dataflowState.isCustodian, buttons: [] }}>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  const parseStatus = status => {
    if (status === config.dataflowStatus.OPEN) {
      return resourcesContext.messages['published'].toUpperCase();
    } else {
      return resourcesContext.messages['design'].toUpperCase();
    }
  };

  if (dataflowState.requestStatus === 'pending' || dataflowState.isLoading) {
    return layout(<Spinner />);
  }

  return layout(
    <div className="rep-row">
      <div className="rep-col-12 rep-col-sm-12">
        <Title
          icon="clone"
          iconSize="4rem"
          subtitle={resourcesContext.messages['referenceDataflowCrumbLabel']}
          title={dataflowState.name}
        />
      </div>
      <div className="rep-col-12 rep-col-sm-12">
        <BigButtonListReference
          className="dataflow-big-buttons-help-step"
          dataflowId={referenceDataflowId}
          dataflowState={dataflowState}
          onSaveName={onSaveDatasetName}
          onUpdateData={refreshPage}
          setIsCreatingReferenceDatasets={setIsCreatingReferenceDatasets}
          setIsImportingDataflow={setIsImportingDataflow}
          setUpdatedDatasetSchema={setUpdatedDatasetSchema}
        />
      </div>

      {dataflowState.isPropertiesDialogVisible && (
        <Dialog
          footer={renderDialogFooterCloseBtn('isPropertiesDialogVisible')}
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
            {resourcesContext.messages['propertiesModalDataflowStatusLabel']}: {parseStatus(dataflowState.status)}
          </p>
        </Dialog>
      )}

      {dataflowState.isEditDialogVisible && (
        <ManageReferenceDataflow
          dataflowId={referenceDataflowId}
          isEditing={true}
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
        />
      )}

      {dataflowState.isReferencingDataflowsDialogVisible && (
        <Dialog
          footer={renderDialogFooterCloseBtn('isReferencingDataflowsDialogVisible')}
          header={resourcesContext.messages['referencingDataflowsDialogHeader']}
          onHide={() => {
            manageDialogs('isReferencingDataflowsDialogVisible', false);
            resetReferencingDataflowsFiltersState();
          }}
          visible={dataflowState.isReferencingDataflowsDialogVisible}>
          <ReferencingDataflows referenceDataflowId={referenceDataflowId} />
        </Dialog>
      )}

      {dataflowState.isManageRequestersDialogVisible && (
        <Dialog
          footer={shareRightsFooterDialogFooter}
          header={resourcesContext.messages['manageRequestersRights']}
          onHide={() => onCloseShareRightsDialog()}
          visible={dataflowState.isManageRequestersDialogVisible}>
          <ShareRights
            addConfirmHeader={resourcesContext.messages['addRequesterConfirmHeader']}
            addErrorNotificationKey={'ADD_REQUESTER_ERROR'}
            columnHeader={resourcesContext.messages['requestersEmailColumn']}
            dataflowId={referenceDataflowId}
            deleteConfirmHeader={resourcesContext.messages['requestersRightsDialogConfirmDeleteHeader']}
            deleteConfirmMessage={resourcesContext.messages['requestersRightsDialogConfirmDeleteQuestion']}
            deleteErrorNotificationKey={'DELETE_REQUESTER_ERROR'}
            editConfirmHeader={resourcesContext.messages['editRequesterConfirmHeader']}
            getErrorNotificationKey={'GET_REQUESTERS_ERROR'}
            isUserRightManagementDialogVisible={dataflowState.isUserRightManagementDialogVisible}
            placeholder={resourcesContext.messages['manageRolesRequesterDialogInputPlaceholder']}
            roleOptions={requesterRoleOptions}
            setIsUserRightManagementDialogVisible={setIsUserRightManagementDialogVisible}
            setRightPermissionsChange={setRightPermissionsChange}
            updateErrorNotificationKey={'UPDATE_REQUESTER_ERROR'}
            userType={'requester'}
          />
        </Dialog>
      )}

      {dataflowState.isDatasetsInfoDialogVisible && (
        <Dialog
          footer={renderDialogFooterCloseBtn('isDatasetsInfoDialogVisible')}
          header={`${resourcesContext.messages['datasetsInfo']} - ${resourcesContext.messages['dataflowId']}: ${dataflowState.data.id}`}
          onHide={() => {
            manageDialogs('isDatasetsInfoDialogVisible', false);
            resetDatasetInfoFiltersState();
          }}
          visible={dataflowState.isDatasetsInfoDialogVisible}>
          <DatasetsInfo dataflowId={referenceDataflowId} dataflowType={dataflowState.dataflowType} />
        </Dialog>
      )}
    </div>
  );
};
