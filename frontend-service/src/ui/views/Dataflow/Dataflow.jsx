/* eslint-disable jsx-a11y/anchor-is-valid */
import { Fragment, useContext, useEffect, useLayoutEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import first from 'lodash/first';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import map from 'lodash/map';
import uniq from 'lodash/uniq';

import styles from './Dataflow.module.scss';

import { config } from 'conf';
import { DataflowDraftRequesterHelpConfig } from 'conf/help/dataflow/requester/draft';
import { DataflowReporterHelpConfig } from 'conf/help/dataflow/reporter';
import { DataflowRequesterHelpConfig } from 'conf/help/dataflow/requester';
import { routes } from 'ui/routes';
import { RepresentativeConfig } from 'conf/domain/model/Representative';

import { ApiKeyDialog } from 'ui/views/_components/ApiKeyDialog';
import { BigButtonList } from './_components/BigButtonList';
import { BigButtonListRepresentative } from './_components/BigButtonListRepresentative';
import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox/';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { DataflowManagement } from 'ui/views/_components/DataflowManagement';
import { Dialog } from 'ui/views/_components/Dialog';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { MainLayout } from 'ui/views/_components/Layout';
import { PropertiesDialog } from './_components/PropertiesDialog';
import { RepresentativesList } from './_components/RepresentativesList';
import { ShareRights } from 'ui/views/_components/ShareRights';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from 'ui/views/_components/Title';
import { UserList } from 'ui/views/_components/UserList';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { RepresentativeService } from 'core/services/Representative';
import { UserService } from 'core/services/User';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotService } from 'core/services/Snapshot';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { dataflowDataReducer } from './_functions/Reducers/dataflowDataReducer';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';
import { useLeftSideBar } from './_functions/Hooks/useLeftSideBar';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

const Dataflow = withRouter(({ history, match }) => {
  const {
    params: { dataflowId, representativeId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const dataflowInitialState = {
    anySchemaAvailableInPublic: false,
    currentUrl: '',
    data: {},
    dataProviderId: [],
    dataProviderSelected: {},
    deleteInput: '',
    description: '',
    designDatasetSchemas: [],
    formHasRepresentatives: false,
    hasRepresentativesWithoutDatasets: false,
    hasWritePermissions: false,
    id: dataflowId,
    isApiKeyDialogVisible: false,
    isCopyDataCollectionToEuDatasetLoading: false,
    isCustodian: false,
    isDataSchemaCorrect: [],
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isEditDialogVisible: false,
    isExportDialogVisible: false,
    isExportEuDatasetLoading: false,
    isExporting: false,
    isFetchingData: false,
    isImportLeadReportersVisible: false,
    isManageRequestersDialogVisible: false,
    isManageReportersDialogVisible: false,
    isManageRolesDialogVisible: false,
    isNationalCoordinator: false,
    isObserver: false,
    isPageLoading: true,
    isPropertiesDialogVisible: false,
    isReceiptLoading: false,
    isReceiptOutdated: false,
    isReleasable: false,
    isReleaseableDialogVisible: false,
    isReleaseDialogVisible: false,
    isShowPublicInfoDialogVisible: false,
    isSnapshotDialogVisible: false,
    isUserListVisible: false,
    name: '',
    obligations: {},
    representativesImport: false,
    restrictFromPublic: false,
    showPublicInfo: false,
    status: '',
    updatedDatasetSchema: [],
    userRoles: [],
    isUserRightManagementDialogVisible: false
  };

  const [dataflowState, dataflowDispatch] = useReducer(dataflowDataReducer, dataflowInitialState);

  const uniqDataProviders = uniq(map(dataflowState.data.datasets, 'dataProviderId'));

  const uniqRepresentatives = uniq(map(dataflowState.data.representatives, 'dataProviderId'));

  const isLeadDesigner = dataflowState.userRoles.some(
    userRole => userRole === config.permissions.roles.CUSTODIAN.key || userRole === config.permissions.roles.STEWARD.key
  );

  const isObserver = dataflowState.userRoles.some(userRole => userRole === config.permissions.roles.OBSERVER.key);

  const isDesign = dataflowState.status === config.dataflowStatus.DESIGN;

  const isInsideACountry =
    !isNil(representativeId) || (uniqDataProviders.length === 1 && !isLeadDesigner && !isObserver);

  const isOpenStatus = dataflowState.status === config.dataflowStatus.OPEN;

  const isLeadReporter = userContext.hasContextAccessPermission(
    config.permissions.prefixes.DATAFLOW,
    dataflowState.id,
    [config.permissions.roles.LEAD_REPORTER.key]
  );

  const isReporter = userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowState.id, [
    config.permissions.roles.REPORTER_READ.key,
    config.permissions.roles.REPORTER_WRITE.key
  ]);

  const isNationalCoordinator = userContext.hasContextAccessPermission(
    config.permissions.prefixes.NATIONAL_COORDINATOR,
    null,
    [config.permissions.roles.NATIONAL_COORDINATOR.key]
  );

  const country =
    uniqDataProviders.length === 1
      ? uniq(map(dataflowState.data.datasets, 'datasetSchemaName'))
      : isNil(representativeId)
      ? null
      : uniq(
          map(
            dataflowState.data?.datasets?.filter(d => d.dataProviderId?.toString() === representativeId),
            'datasetSchemaName'
          )
        );

  const isLeadReporterOfCountry =
    isLeadReporter &&
    isInsideACountry &&
    ((!isNil(representativeId) && uniqRepresentatives.includes(parseInt(representativeId))) ||
      (uniqDataProviders.length === 1 && uniqRepresentatives.includes(uniqDataProviders[0])));

  const isNationalCoordinatorOfCountry = isNationalCoordinator && isInsideACountry;

  const isReporterOfCountry = isReporter && isInsideACountry;

  const dataProviderId = isInsideACountry
    ? !isNil(representativeId)
      ? parseInt(representativeId)
      : uniqDataProviders[0]
    : null;

  useEffect(() => {
    if (!Number(dataflowId)) {
      window.location.href = '/dataflows/error/loadDataflowData';
    }
  }, []);

  useBreadCrumbs({
    currentPage: CurrentPage.DATAFLOW,
    dataflowId,
    dataflowStateData: dataflowState.data,
    history,
    matchParams: match.params,
    representativeId
  });

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) onLoadPermission();
  }, [userContext, dataflowState.data]);

  useLayoutEffect(() => {
    if (dataflowState.isCustodian) {
      if (isOpenStatus) {
        leftSideBarContext.addHelpSteps(DataflowDraftRequesterHelpConfig, 'dataflowRequesterDraftHelp');
      } else {
        leftSideBarContext.addHelpSteps(DataflowRequesterHelpConfig, 'dataflowRequesterDesignHelp');
      }
    } else {
      leftSideBarContext.addHelpSteps(DataflowReporterHelpConfig, 'dataflowReporterHelp');
    }
  }, [userContext, dataflowState]);

  const manageDialogs = (dialog, value, secondDialog, secondValue) =>
    dataflowDispatch({
      type: 'MANAGE_DIALOGS',
      payload: { dialog, value, secondDialog, secondValue, deleteInput: '' }
    });

  const getLeftSidebarButtonsVisibility = () => {
    if (isEmpty(dataflowState.data)) {
      return {
        apiKeyBtn: false,
        editBtn: false,
        exportBtn: false,
        manageReportersBtn: false,
        manageRequestersBtn: false,
        propertiesBtn: false,
        releaseableBtn: false,
        usersListBtn: false
      };
    }

    return {
      apiKeyBtn: isLeadDesigner || isLeadReporterOfCountry,
      editBtn: isDesign && isLeadDesigner,
      exportBtn: isLeadDesigner && dataflowState.designDatasetSchemas.length > 0,
      manageReportersBtn: isLeadReporterOfCountry,
      manageRequestersBtn: dataflowState.isCustodian,
      propertiesBtn: true,
      releaseableBtn: !isDesign && isLeadDesigner,
      showPublicInfoBtn: !isDesign && isLeadDesigner,
      usersListBtn:
        isLeadReporterOfCountry ||
        isNationalCoordinatorOfCountry ||
        isReporterOfCountry ||
        dataflowState.isCustodian ||
        dataflowState.isObserver
    };
  };

  useLeftSideBar(dataflowState, dataProviderId, getLeftSidebarButtonsVisibility, manageDialogs, representativeId);

  useEffect(() => {
    if (!isEmpty(dataflowState.data.representatives)) {
      const representativesNoDatasets = dataflowState.data.representatives.filter(
        representative => !representative.hasDatasets
      );
      setHasRepresentativesWithoutDatasets(!isEmpty(representativesNoDatasets));
      setFormHasRepresentatives(!isEmpty(representativesNoDatasets));
    }
  }, [dataflowState.data.representatives]);

  useEffect(() => {
    setIsPageLoading(true);
    onLoadReportingDataflow();
    onLoadSchemasValidations();
  }, [dataflowId, dataflowState.isDataUpdated, representativeId]);

  const checkRestrictFromPublic = (
    <div style={{ float: 'left' }}>
      <Checkbox
        checked={dataflowState.restrictFromPublic}
        id="restrict_from_public_checkbox"
        inputId="restrict_from_public_checkbox"
        onChange={e => dataflowDispatch({ type: 'SET_RESTRICT_FROM_PUBLIC', payload: e.checked })}
        role="checkbox"
      />
      <label
        onClick={() =>
          dataflowDispatch({ type: 'SET_RESTRICT_FROM_PUBLIC', payload: !dataflowState.restrictFromPublic })
        }
        style={{ cursor: 'pointer', fontWeight: 'bold', marginLeft: '3px' }}>
        {resources.messages['restrictFromPublic']}
      </label>
    </div>
  );

  const handleRedirect = target => history.push(target);

  const setIsUserRightManagementDialogVisible = isVisible => {
    manageDialogs('isUserRightManagementDialogVisible', isVisible);
  };

  const shareRightsFooterDialogFooter = userType => (
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
        label={resources.messages['close']}
        onClick={() => manageDialogs(`isManage${userType}DialogVisible`, false)}
      />
    </div>
  );

  const setDataProviderSelected = value => dataflowDispatch({ type: 'SET_DATA_PROVIDER_SELECTED', payload: value });

  const setFormHasRepresentatives = value =>
    dataflowDispatch({ type: 'SET_FORM_HAS_REPRESENTATIVES', payload: { formHasRepresentatives: value } });

  const setHasRepresentativesWithoutDatasets = value =>
    dataflowDispatch({
      type: 'SET_HAS_REPRESENTATIVES_WITHOUT_DATASETS',
      payload: { hasRepresentativesWithoutDatasets: value }
    });

  const setIsCopyDataCollectionToEuDatasetLoading = value =>
    dataflowDispatch({ type: 'SET_IS_COPY_DATA_COLLECTION_TO_EU_DATASET_LOADING', payload: { isLoading: value } });

  const setIsExportEuDatasetLoading = value =>
    dataflowDispatch({ type: 'SET_IS_EXPORT_EU_DATASET', payload: { isExportEuDatasetLoading: value } });

  const setIsReleaseable = isReleasable =>
    dataflowDispatch({ type: 'SET_IS_RELEASABLE', payload: { isReleasable: isReleasable } });

  const setIsDataUpdated = () => dataflowDispatch({ type: 'SET_IS_DATA_UPDATED' });

  const setIsPageLoading = isPageLoading =>
    dataflowDispatch({ type: 'SET_IS_PAGE_LOADING', payload: { isPageLoading } });

  const setUpdatedDatasetSchema = updatedData =>
    dataflowDispatch({ type: 'SET_UPDATED_DATASET_SCHEMA', payload: { updatedData } });

  const setIsReceiptLoading = isReceiptLoading => {
    dataflowDispatch({ type: 'SET_IS_RECEIPT_LOADING', payload: { isReceiptLoading } });
  };

  const setIsReceiptOutdated = isReceiptOutdated => {
    dataflowDispatch({ type: 'SET_IS_RECEIPT_OUTDATED', payload: { isReceiptOutdated } });
  };

  const onCleanUpReceipt = () => {
    dataflowDispatch({ type: 'ON_CLEAN_UP_RECEIPT', payload: { isReceiptLoading: false, isReceiptOutdated: false } });
  };

  const onEditDataflow = (newName, newDescription) => {
    dataflowDispatch({
      type: 'ON_EDIT_DATA',
      payload: { name: newName, description: newDescription, isEditDialogVisible: false, isExportDialogVisible: false }
    });
    onLoadReportingDataflow();
  };

  const onConfirmDeleteDataflow = event =>
    dataflowDispatch({ type: 'ON_CONFIRM_DELETE_DATAFLOW', payload: { deleteInput: event.target.value } });

  const onExportLeadReporters = async () => {
    try {
      const { data } = await RepresentativeService.downloadById(dataflowId);
      if (!isNil(data)) {
        DownloadFile(
          data,
          `${TextUtils.ellipsis(dataflowState.name, config.notifications.STRING_LENGTH_MAX)}_Lead_Reporters.csv`
        );
      }
    } catch (error) {
      console.error(error);
      notificationContext.add({ type: 'EXPORT_DATAFLOW_LEAD_REPORTERS_FAILED_EVENT' });
    }
  };

  const manageRoleDialogFooter = (
    <Fragment>
      <Button
        className={`${styles.manageLeadReportersButton} p-button-secondary ${
          !isEmpty(dataflowState.dataProviderSelected) ? 'p-button-animated-blink' : ''
        }`}
        disabled={isEmpty(dataflowState.dataProviderSelected)}
        icon={'import'}
        label={resources.messages['importLeadReporters']}
        onClick={() => manageDialogs('isImportLeadReportersVisible', true)}
      />
      <Button
        className={`${styles.manageLeadReportersButton} p-button-secondary p-button-animated-blink`}
        icon={'export'}
        label={resources.messages['exportLeadReporters']}
        onClick={onExportLeadReporters}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => manageDialogs('isManageRolesDialogVisible', false)}
      />
    </Fragment>
  );

  const renderDataflowUsersListFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => manageDialogs('isUserListVisible', false)}
    />
  );

  const getCurrentDatasetId = () => {
    if (isEmpty(dataflowState.data)) return null;

    const { datasets } = dataflowState.data;

    return first(
      datasets
        .filter(dataset => dataset.dataProviderId === parseInt(representativeId))
        .map(dataset => dataset.datasetId)
    );
  };

  const onLoadPermission = () => {
    const currentDatasetId = getCurrentDatasetId();

    const hasWritePermissions = userContext.hasPermission(
      [config.permissions.roles.LEAD_REPORTER.key],
      `${config.permissions.prefixes.DATAFLOW}${dataflowId}`
    );

    const isNationalCoordinator = userContext.hasContextAccessPermission(
      config.permissions.prefixes.NATIONAL_COORDINATOR,
      null,
      [config.permissions.roles.NATIONAL_COORDINATOR.key]
    );

    const entity =
      isNil(representativeId) || representativeId !== 0
        ? `${config.permissions.prefixes.DATAFLOW}${dataflowId}`
        : `${config.permissions.prefixes.DATASET}${currentDatasetId}`;

    const userRoles = userContext.getUserRole(entity);

    const isCustodian = userRoles.some(
      userRole =>
        userRole === config.permissions.roles.CUSTODIAN.key || userRole === config.permissions.roles.STEWARD.key
    );

    const isObserver = userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowId, [
      config.permissions.roles.OBSERVER.key
    ]);

    dataflowDispatch({
      type: 'LOAD_PERMISSIONS',
      payload: {
        hasWritePermissions,
        isCustodian,
        isObserver,
        userRoles,
        isNationalCoordinator
      }
    });
  };

  const onLoadReportingDataflow = async () => {
    try {
      const dataflowResponse = await DataflowService.reporting(dataflowId);
      const dataflow = dataflowResponse.data;

      Promise.resolve(dataflow).then(res => {
        dataflowDispatch({ type: 'SET_IS_FETCHING_DATA', payload: { isFetchingData: false } });
      });

      dataflowDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          anySchemaAvailableInPublic: dataflow.anySchemaAvailableInPublic,
          data: dataflow,
          description: dataflow.description,
          isReleasable: dataflow.isReleasable,
          name: dataflow.name,
          obligations: dataflow.obligation,
          showPublicInfo: dataflow.showPublicInfo,
          status: dataflow.status
        }
      });

      if (!isEmpty(dataflow.designDatasets)) {
        dataflow.designDatasets.forEach((schema, idx) => {
          schema.index = idx;
        });

        dataflowDispatch({ type: 'SET_DESIGN_DATASET_SCHEMAS', payload: { designDatasets: dataflow.designDatasets } });

        const datasetSchemaInfo = [];
        dataflow.designDatasets.forEach(schema => {
          datasetSchemaInfo.push({ schemaName: schema.datasetSchemaName, schemaIndex: schema.index });
        });

        setUpdatedDatasetSchema(datasetSchemaInfo);
      } else {
        dataflowDispatch({ type: 'SET_DESIGN_DATASET_SCHEMAS', payload: { designDatasets: [] } });
      }

      if (!isNil(dataProviderId)) {
        dataflowDispatch({ type: 'SET_DATA_PROVIDER_ID', payload: { id: dataProviderId } });
      }

      if (representativeId) {
        if (!isEmpty(dataflow.representatives) && !isEmpty(dataflow.datasets)) {
          const isReceiptOutdated = dataflow.representatives
            .filter(representative => representative.dataProviderId === parseInt(representativeId))
            .map(representative => representative.isReceiptOutdated);

          if (isReceiptOutdated.length === 1) {
            setIsReceiptOutdated(isReceiptOutdated[0]);
          }
        }
      } else {
        if (!isEmpty(dataflow.representatives)) {
          const isReceiptOutdated = dataflow.representatives.map(representative => representative.isReceiptOutdated);

          if (isReceiptOutdated.length === 1) {
            setIsReceiptOutdated(isReceiptOutdated[0]);
          }
        }
      }
    } catch (error) {
      notificationContext.add({ type: 'LOAD_DATAFLOW_DATA_ERROR' });
      history.push(getUrl(routes.DATAFLOWS));
    } finally {
      setIsPageLoading(false);
    }
  };

  const onUploadLeadReporters = event => {
    manageDialogs('isImportLeadReportersVisible', false);
    try {
      if (!isNil(event.xhr) && !isNil(event.xhr.response)) {
        DownloadFile(
          event.xhr.response,
          `${TextUtils.ellipsis(dataflowState.name, config.notifications.STRING_LENGTH_MAX)}_Results.csv`
        );
        dataflowDispatch({ type: 'SET_REPRESENTATIVES_IMPORT', payload: true });
      }
    } catch (error) {
      console.error(`Error while downloading the file: ${error}`);
      notificationContext.add({ type: 'IMPORT_DATAFLOW_LEAD_REPORTERS_FAILED_EVENT' });
    }
  };

  const setIsReleasingDatasetsProviderId = isReleasingDatasetValue => {
    const [notification] = notificationContext.all.filter(
      notification =>
        notification.key === 'RELEASE_FAILED_EVENT' ||
        notification.key === 'RELEASE_BLOCKED_EVENT' ||
        notification.key === 'RELEASE_BLOCKERS_FAILED_EVENT' ||
        notification.key === 'ADD_DATASET_SNAPSHOT_FAILED_EVENT'
    );

    dataflowState.data.datasets.forEach(dataset => {
      if (dataset.dataProviderId === notification.content.providerId) {
        dataset.isReleasing = isReleasingDatasetValue;
      }
    });
  };

  useCheckNotifications(['RELEASE_COMPLETED_EVENT', 'RELEASE_PROVIDER_COMPLETED_EVENT'], onLoadReportingDataflow);

  useCheckNotifications(
    [
      'RELEASE_FAILED_EVENT',
      'RELEASE_BLOCKED_EVENT',
      'RELEASE_BLOCKERS_FAILED_EVENT',
      'ADD_DATASET_SNAPSHOT_FAILED_EVENT'
    ],
    setIsReleasingDatasetsProviderId,
    false
  );

  const onLoadSchemasValidations = async () => {
    const validationResult = await DataflowService.schemasValidation(dataflowId);

    dataflowDispatch({ type: 'SET_IS_DATA_SCHEMA_CORRECT', payload: { validationResult: validationResult.data } });
  };

  const onSaveName = async (value, index) => {
    try {
      await DatasetService.updateSchemaNameById(
        dataflowState.designDatasetSchemas[index].datasetId,
        encodeURIComponent(value)
      );
      const updatedTitles = [...dataflowState.updatedDatasetSchema];
      updatedTitles[index].schemaName = value;
      setUpdatedDatasetSchema(updatedTitles);
    } catch (error) {
      console.error('error', error);
      if (error?.response?.status === 400) {
        notificationContext.add({
          type: 'DATASET_SCHEMA_CREATION_ERROR_INVALID_NAME',
          content: { schemaName: value }
        });
      }
    }
  };

  const onShowManageReportersDialog = () => manageDialogs('isManageRolesDialogVisible', true);

  const onOpenReleaseConfirmDialog = () => manageDialogs('isReleaseDialogVisible', true);

  const onConfirmExport = async () => {
    try {
      dataflowDispatch({ type: 'SET_IS_EXPORTING', payload: true });
      const { data } = await DataflowService.downloadById(dataflowId);
      if (!isNil(data)) {
        DownloadFile(data, `${dataflowState.data.name}_${new Date(Date.now()).toDateString().replace(' ', '_')}.zip`);
      }
    } catch (error) {
      console.error(error);
      notificationContext.add({ type: 'EXPORT_DATASET_SCHEMA_FAILED_EVENT' });
    } finally {
      manageDialogs('isExportDialogVisible', false);
      dataflowDispatch({ type: 'SET_IS_EXPORTING', payload: false });
    }
  };

  const onConfirmRelease = async () => {
    try {
      notificationContext.add({ type: 'RELEASE_START_EVENT' });
      await SnapshotService.releaseDataflow(dataflowId, dataProviderId, dataflowState.restrictFromPublic);

      dataflowState.data.datasets
        .filter(dataset => dataset.dataProviderId === dataProviderId)
        .forEach(dataset => (dataset.isReleasing = true));
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'RELEASE_BLOCKED_EVENT' });
      } else {
        notificationContext.add({ type: 'RELEASE_FAILED_EVENT', content: {} });
      }
    } finally {
      manageDialogs('isReleaseDialogVisible', false);
    }
  };

  const onRefreshToken = async () => {
    try {
      const userObject = await UserService.refreshToken();
      userContext.onTokenRefresh(userObject);
    } catch (error) {
      notificationContext.add({
        key: 'TOKEN_REFRESH_ERROR',
        content: {}
      });
      await UserService.logout();
      userContext.onLogout();
    }
  };

  const onDataCollectionIsCompleted = () => {
    onRefreshToken();
    setIsDataUpdated();
  };

  useCheckNotifications(
    ['COPY_DATASET_SCHEMA_COMPLETED_EVENT', 'IMPORT_DATASET_SCHEMA_COMPLETED_EVENT'],
    setIsDataUpdated
  );

  useCheckNotifications(['ADD_DATACOLLECTION_COMPLETED_EVENT'], onDataCollectionIsCompleted);

  useCheckNotifications(['UPDATE_RELEASABLE_FAILED_EVENT'], setIsDataUpdated);

  const getImportExtensions = ['.csv'].join(', ').toLowerCase();

  const infoExtensionsTooltip = `${resources.messages['supportedFileExtensionsTooltip']} ${uniq(
    getImportExtensions.split(', ')
  ).join(', ')}`;

  const onConfirmUpdateIsReleaseable = async () => {
    manageDialogs('isReleaseableDialogVisible', false);
    try {
      dataflowDispatch({
        type: 'SET_IS_FETCHING_DATA',
        payload: { isFetchingData: true }
      });
      await DataflowService.update(
        dataflowId,
        dataflowState.data.name,
        dataflowState.data.description,
        dataflowState.obligations.obligationId,
        dataflowState.isReleasable,
        dataflowState.showPublicInfo
      );
      onLoadReportingDataflow();
    } catch (error) {
      notificationContext.add({ type: 'UPDATE_RELEASABLE_FAILED_EVENT', content: { dataflowId } });
      dataflowDispatch({
        type: 'ON_ERROR_UPDATE_IS_RELEASABLE',
        payload: { isReleasable: dataflowState.data.isReleasable, isFetchingData: false }
      });
    }
  };

  const onConfirmUpdateShowPublicInfo = async () => {
    manageDialogs('isShowPublicInfoDialogVisible', false);
    try {
      dataflowDispatch({
        type: 'SET_IS_FETCHING_DATA',
        payload: { isFetchingData: true }
      });
      await DataflowService.update(
        dataflowId,
        dataflowState.data.name,
        dataflowState.data.description,
        dataflowState.obligations.obligationId,
        dataflowState.isReleasable,
        dataflowState.showPublicInfo
      );
      onLoadReportingDataflow();
    } catch (error) {
      notificationContext.add({ type: 'UPDATE_RELEASABLE_FAILED_EVENT', content: { dataflowId } });
      dataflowDispatch({
        type: 'ON_ERROR_UPDATE_IS_RELEASABLE',
        payload: { showPublicInfo: dataflowState.data.showPublicInfo, isFetchingData: false }
      });
    }
  };

  const onCloseIsReleaseableDialog = () => {
    manageDialogs('isReleaseableDialogVisible', false);
    if (dataflowState.data.isReleasable !== dataflowState.isReleasable) {
      dataflowDispatch({ type: 'SET_IS_RELEASABLE', payload: { isReleasable: dataflowState.data.isReleasable } });
    }
  };

  const onCloseIsShowPublicInfoDialog = () => {
    manageDialogs('isShowPublicInfoDialogVisible', false);
    if (dataflowState.data.showPublicInfo !== dataflowState.showPublicInfo) {
      dataflowDispatch({
        type: 'SET_SHOW_PUBLIC_INFO',
        payload: { showPublicInfo: dataflowState.data.showPublicInfo }
      });
    }
  };

  const reporterRoleOptions = [
    { label: config.permissions.roles.REPORTER_WRITE.label, role: config.permissions.roles.REPORTER_WRITE.key },
    { label: config.permissions.roles.REPORTER_READ.label, role: config.permissions.roles.REPORTER_READ.key }
  ];

  const requesterRoleOptionsOpenStatus = [
    { label: config.permissions.roles.CUSTODIAN.label, role: config.permissions.roles.CUSTODIAN.key },
    { label: config.permissions.roles.STEWARD.label, role: config.permissions.roles.STEWARD.key },
    { label: config.permissions.roles.OBSERVER.label, role: config.permissions.roles.OBSERVER.key }
  ];

  const requesterRoleOptions = [
    ...requesterRoleOptionsOpenStatus,
    { label: config.permissions.roles.EDITOR_WRITE.label, role: config.permissions.roles.EDITOR_WRITE.key },
    { label: config.permissions.roles.EDITOR_READ.label, role: config.permissions.roles.EDITOR_READ.key }
  ];

  const getBigButtonList = () => {
    if (isNil(representativeId)) {
      return (
        <BigButtonList
          className="dataflow-big-buttons-help-step"
          dataProviderId={dataProviderId}
          dataflowState={dataflowState}
          handleRedirect={handleRedirect}
          isLeadReporterOfCountry={isLeadReporterOfCountry}
          onCleanUpReceipt={onCleanUpReceipt}
          onOpenReleaseConfirmDialog={onOpenReleaseConfirmDialog}
          onSaveName={onSaveName}
          onShowManageReportersDialog={onShowManageReportersDialog}
          onUpdateData={setIsDataUpdated}
          setIsCopyDataCollectionToEuDatasetLoading={setIsCopyDataCollectionToEuDatasetLoading}
          setIsExportEuDatasetLoading={setIsExportEuDatasetLoading}
          setIsReceiptLoading={setIsReceiptLoading}
          setUpdatedDatasetSchema={setUpdatedDatasetSchema}
        />
      );
    } else {
      return (
        <BigButtonListRepresentative
          dataProviderId={dataProviderId}
          dataflowState={dataflowState}
          handleRedirect={handleRedirect}
          match={match}
          onCleanUpReceipt={onCleanUpReceipt}
          onOpenReleaseConfirmDialog={onOpenReleaseConfirmDialog}
          setIsReceiptLoading={setIsReceiptLoading}
        />
      );
    }
  };

  const layout = children => (
    <MainLayout leftSideBarConfig={{ isCustodian: dataflowState.isCustodian, buttons: [] }}>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  if (dataflowState.isPageLoading || isNil(dataflowState.data)) return layout(<Spinner />);

  return layout(
    <div className="rep-row">
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-12`}>
        <Title
          icon="clone"
          iconSize="4rem"
          subtitle={
            isInsideACountry && !isNil(country) && country.length > 0
              ? `${resources.messages['dataflow']} - ${country}`
              : resources.messages['dataflow']
          }
          title={dataflowState.name}
        />

        {getBigButtonList()}

        {dataflowState.isReleaseDialogVisible && (
          <ConfirmDialog
            footerAddon={dataflowState.anySchemaAvailableInPublic && checkRestrictFromPublic}
            header={resources.messages['confirmReleaseHeader']}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={onConfirmRelease}
            onHide={() => {
              manageDialogs('isReleaseDialogVisible', false);
              if (dataflowState.restrictFromPublic) {
                dataflowDispatch({ type: 'SET_RESTRICT_FROM_PUBLIC', payload: false });
              }
            }}
            visible={dataflowState.isReleaseDialogVisible}>
            {resources.messages['confirmReleaseQuestion']}
          </ConfirmDialog>
        )}

        {dataflowState.isCustodian && dataflowState.isManageRolesDialogVisible && (
          <Dialog
            contentStyle={{ maxHeight: '60vh' }}
            footer={manageRoleDialogFooter}
            header={resources.messages['manageRolesDialogTitle']}
            onHide={() => manageDialogs('isManageRolesDialogVisible', false)}
            visible={dataflowState.isManageRolesDialogVisible}>
            <div className={styles.dialog}>
              <RepresentativesList
                dataflowId={dataflowId}
                representativesImport={dataflowState.representativesImport}
                setDataProviderSelected={setDataProviderSelected}
                setFormHasRepresentatives={setFormHasRepresentatives}
                setHasRepresentativesWithoutDatasets={setHasRepresentativesWithoutDatasets}
                setRepresentativeImport={isImport =>
                  dataflowDispatch({ type: 'SET_REPRESENTATIVES_IMPORT', payload: isImport })
                }
              />
            </div>
          </Dialog>
        )}

        {dataflowState.isManageRequestersDialogVisible && (
          <Dialog
            footer={shareRightsFooterDialogFooter('Requesters')}
            header={resources.messages['manageRequestersRights']}
            onHide={() => manageDialogs('isManageRequestersDialogVisible', false)}
            visible={dataflowState.isManageRequestersDialogVisible}>
            <ShareRights
              addConfirmHeader={resources.messages[`addRequesterConfirmHeader`]}
              addErrorNotificationKey={'ADD_REQUESTER_ERROR'}
              columnHeader={resources.messages['requestersEmailColumn']}
              dataProviderId={dataProviderId}
              dataflowId={dataflowId}
              deleteColumnHeader={resources.messages['deleteRequesterButtonTableHeader']}
              deleteConfirmHeader={resources.messages[`requestersRightsDialogConfirmDeleteHeader`]}
              deleteConfirmMessage={resources.messages[`requestersRightsDialogConfirmDeleteQuestion`]}
              deleteErrorNotificationKey={'DELETE_REQUESTER_ERROR'}
              editConfirmHeader={resources.messages[`editRequesterConfirmHeader`]}
              getErrorNotificationKey={'GET_REQUESTERS_ERROR'}
              isUserRightManagementDialogVisible={dataflowState.isUserRightManagementDialogVisible}
              placeholder={resources.messages['manageRolesRequesterDialogInputPlaceholder']}
              representativeId={representativeId}
              roleOptions={isOpenStatus ? requesterRoleOptionsOpenStatus : requesterRoleOptions}
              setIsUserRightManagementDialogVisible={setIsUserRightManagementDialogVisible}
              updateErrorNotificationKey={'UPDATE_REQUESTER_ERROR'}
              userType={'requester'}
            />
          </Dialog>
        )}

        {dataflowState.isManageReportersDialogVisible && (
          <Dialog
            footer={shareRightsFooterDialogFooter('Reporters')}
            header={resources.messages['manageReportersRights']}
            onHide={() => manageDialogs('isManageReportersDialogVisible', false)}
            visible={dataflowState.isManageReportersDialogVisible}>
            <ShareRights
              addConfirmHeader={resources.messages[`addReporterConfirmHeader`]}
              addErrorNotificationKey={'ADD_REPORTER_ERROR'}
              columnHeader={resources.messages['reportersEmailColumn']}
              dataProviderId={dataProviderId}
              dataflowId={dataflowId}
              deleteColumnHeader={resources.messages['deleteReporterButtonTableHeader']}
              deleteConfirmHeader={resources.messages[`reportersRightsDialogConfirmDeleteHeader`]}
              deleteConfirmMessage={resources.messages[`reportersRightsDialogConfirmDeleteQuestion`]}
              deleteErrorNotificationKey={'DELETE_REPORTER_ERROR'}
              editConfirmHeader={resources.messages[`editReporterConfirmHeader`]}
              getErrorNotificationKey={'GET_REPORTERS_ERROR'}
              isUserRightManagementDialogVisible={dataflowState.isUserRightManagementDialogVisible}
              placeholder={resources.messages['manageRolesReporterDialogInputPlaceholder']}
              representativeId={representativeId}
              roleOptions={reporterRoleOptions}
              setIsUserRightManagementDialogVisible={setIsUserRightManagementDialogVisible}
              updateErrorNotificationKey={'UPDATE_REPORTER_ERROR'}
              userType={'reporter'}
            />
          </Dialog>
        )}

        {dataflowState.isExportDialogVisible && (
          <ConfirmDialog
            disabledConfirm={dataflowState.isExporting}
            header={resources.messages['exportSchema']}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={onConfirmExport}
            onHide={() => manageDialogs('isExportDialogVisible', false)}
            visible={dataflowState.isExportDialogVisible}>
            {resources.messages['confirmExportSchema']}
          </ConfirmDialog>
        )}

        {dataflowState.isReleaseableDialogVisible && (
          <ConfirmDialog
            disabledConfirm={
              dataflowState.data.isReleasable === dataflowState.isReleasable || dataflowState.isFetchingData
            }
            header={resources.messages['isReleasableDataflowDialogHeader']}
            iconConfirm={dataflowState.isFetchingData && 'spinnerAnimate'}
            labelCancel={resources.messages['cancel']}
            labelConfirm={resources.messages['save']}
            onConfirm={onConfirmUpdateIsReleaseable}
            onHide={() => onCloseIsReleaseableDialog()}
            visible={dataflowState.isReleaseableDialogVisible}>
            <Checkbox
              checked={dataflowState.isReleasable}
              id="isReleasableCheckbox"
              inputId="isReleasableCheckbox"
              onChange={() => setIsReleaseable(!dataflowState.isReleasable)}
              role="checkbox"
            />
            <label className={styles.isReleasableLabel} htmlFor="isReleasableCheckbox">
              <a onClick={() => setIsReleaseable(!dataflowState.isReleasable)}>
                {resources.messages['isReleasableDataflowCheckboxLabel']}
              </a>
            </label>
          </ConfirmDialog>
        )}

        {dataflowState.isShowPublicInfoDialogVisible && (
          <ConfirmDialog
            disabledConfirm={
              dataflowState.data.showPublicInfo === dataflowState.showPublicInfo || dataflowState.isFetchingData
            }
            header={resources.messages['showPublicInfoDataflowDialogHeader']}
            iconConfirm={dataflowState.isFetchingData && 'spinnerAnimate'}
            labelCancel={resources.messages['cancel']}
            labelConfirm={resources.messages['save']}
            onConfirm={onConfirmUpdateShowPublicInfo}
            onHide={() => onCloseIsShowPublicInfoDialog()}
            visible={dataflowState.isShowPublicInfoDialogVisible}>
            <Checkbox
              checked={dataflowState.showPublicInfo}
              id="showPublicInfoCheckbox"
              inputId="showPublicInfoCheckbox"
              onChange={() =>
                dataflowDispatch({
                  type: 'SET_SHOW_PUBLIC_INFO',
                  payload: { showPublicInfo: !dataflowState.showPublicInfo }
                })
              }
              role="checkbox"
            />
            <label className={styles.showPublicInfo} htmlFor="showPublicInfoCheckbox">
              <a
                onClick={() =>
                  dataflowDispatch({
                    type: 'SET_SHOW_PUBLIC_INFO',
                    payload: { showPublicInfo: !dataflowState.showPublicInfo }
                  })
                }>
                {resources.messages['showPublicInfoDataflowCheckboxLabel']}
              </a>
            </label>
          </ConfirmDialog>
        )}

        {dataflowState.isImportLeadReportersVisible && (
          <CustomFileUpload
            accept={getImportExtensions}
            chooseLabel={resources.messages['selectFile']}
            dialogHeader={`${resources.messages['importLeadReporters']}`}
            dialogOnHide={() => manageDialogs('isImportLeadReportersVisible', false)}
            dialogVisible={dataflowState.isImportLeadReportersVisible}
            fileLimit={1}
            infoTooltip={infoExtensionsTooltip}
            invalidExtensionMessage={resources.messages['invalidExtensionFile']}
            isDialog={true}
            mode="advanced"
            multiple={false}
            name="file"
            onUpload={onUploadLeadReporters}
            url={`${window.env.REACT_APP_BACKEND}${getUrl(RepresentativeConfig.importLeadReporters, {
              dataflowId,
              dataProviderGroupId: dataflowState.dataProviderSelected.dataProviderGroupId
            })}`}
          />
        )}

        {dataflowState.isUserListVisible && (
          <Dialog
            footer={renderDataflowUsersListFooter}
            header={
              ((isNil(dataProviderId) && dataflowState.isCustodian) ||
                (isNil(representativeId) && dataflowState.isObserver)) &&
              dataflowState.status === config.dataflowStatus.OPEN
                ? resources.messages['dataflowUsersByCountryList']
                : resources.messages['dataflowUsersList']
            }
            onHide={() => manageDialogs('isUserListVisible', false)}
            visible={dataflowState.isUserListVisible}>
            <UserList
              dataflowId={dataflowId}
              representativeId={dataflowState.isObserver ? representativeId : dataProviderId}
            />
          </Dialog>
        )}

        <PropertiesDialog dataflowState={dataflowState} manageDialogs={manageDialogs} />

        <DataflowManagement
          dataflowId={dataflowId}
          history={history}
          isEditForm={true}
          manageDialogs={manageDialogs}
          onConfirmDeleteDataflow={onConfirmDeleteDataflow}
          onEditDataflow={onEditDataflow}
          state={dataflowState}
        />

        {dataflowState.isApiKeyDialogVisible && (
          <ApiKeyDialog
            dataProviderId={dataProviderId}
            dataflowId={dataflowId}
            isApiKeyDialogVisible={dataflowState.isApiKeyDialogVisible}
            isCustodian={dataflowState.isCustodian}
            manageDialogs={manageDialogs}
            match={match}
          />
        )}
      </div>
    </div>
  );
});

export { Dataflow };
