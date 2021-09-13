import { Fragment, useContext, useEffect, useReducer } from 'react';
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
import { routes } from 'conf/routes';
import { RepresentativeConfig } from 'repositories/config/RepresentativeConfig';

import { ApiKeyDialog } from 'views/_components/ApiKeyDialog';
import { BigButtonList } from './_components/BigButtonList';
import { BigButtonListRepresentative } from './_components/BigButtonListRepresentative';
import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { ManageDataflow } from 'views/_components/ManageDataflow';
import { Dialog } from 'views/_components/Dialog';
import { DownloadFile } from 'views/_components/DownloadFile';
import { MainLayout } from 'views/_components/Layout';
import { PropertiesDialog } from './_components/PropertiesDialog';
import { ReportingObligations } from 'views/_components/ReportingObligations';
import { RepresentativesList } from './_components/RepresentativesList';
import { ShareRights } from 'views/_components/ShareRights';
import { Spinner } from 'views/_components/Spinner';
import { Title } from 'views/_components/Title';
import { UserList } from 'views/_components/UserList';
import { ManageBusinessDataflow } from 'views/_components/ManageBusinessDataflow';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';
import { RepresentativeService } from 'services/RepresentativeService';
import { SnapshotService } from 'services/SnapshotService';
import { UserService } from 'services/UserService';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { dataflowDataReducer } from './_functions/Reducers/dataflowDataReducer';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';
import { useLeftSideBar } from './_functions/Hooks/useLeftSideBar';
import { useReportingObligations } from 'views/_components/ReportingObligations/_functions/Hooks/useReportingObligations';

import { CurrentPage } from 'views/_functions/Utils';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

const Dataflow = withRouter(({ history, match }) => {
  const {
    params: { dataflowId, representativeId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
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
    isAdmin: false,
    isAdminAssignedBusinessDataflow: false,
    isApiKeyDialogVisible: false,
    isBusinessDataflowDialogVisible: false,
    isCopyDataCollectionToEUDatasetLoading: false,
    isCustodian: false,
    isDataSchemaCorrect: [],
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isExportDialogVisible: false,
    isExportEUDatasetLoading: false,
    isExporting: false,
    isFetchingData: false,
    isImportLeadReportersVisible: false,
    isManageReportersDialogVisible: false,
    isManageRequestersDialogVisible: false,
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
    isReportingDataflowDialogVisible: false,
    isShowPublicInfoDialogVisible: false,
    isSnapshotDialogVisible: false,
    isUserListVisible: false,
    isUserRightManagementDialogVisible: false,
    name: '',
    obligations: {},
    representativesImport: false,
    restrictFromPublic: false,
    showPublicInfo: false,
    status: '',
    updatedDatasetSchema: [],
    userRoles: []
  };

  const [dataflowState, dataflowDispatch] = useReducer(dataflowDataReducer, dataflowInitialState);

  useEffect(() => {
    if (!isEmpty(dataflowState.data)) {
      userContext.setCurrentDataflowType(dataflowState.data.type);
    }
  }, [dataflowState.data]);

  const {
    obligation,
    setCheckedObligation,
    setObligation,
    setObligationToPrevious,
    setPreviousObligation,
    setToCheckedObligation
  } = useReportingObligations();

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
    dataflowType: dataflowState.dataflowType,
    history,
    matchParams: match.params,
    representativeId
  });

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) onLoadPermission();
  }, [userContext, dataflowState.data]);

  useEffect(() => {
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
        editBusinessBtn: false,
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
      editBtn:
        isDesign &&
        isLeadDesigner &&
        !dataflowState.isAdmin &&
        !TextUtils.areEquals(dataflowState.dataflowType, config.dataflowType.BUSINESS.value),
      editBusinessBtn:
        (dataflowState.isAdmin || dataflowState.isCustodian) &&
        TextUtils.areEquals(dataflowState.dataflowType, config.dataflowType.BUSINESS.value),
      exportBtn: isLeadDesigner && dataflowState.designDatasetSchemas.length > 0,
      manageReportersBtn: isLeadReporterOfCountry,
      manageRequestersBtn: dataflowState.isAdmin || dataflowState.isCustodian,
      propertiesBtn: true,
      releaseableBtn: !isDesign && isLeadDesigner,
      showPublicInfoBtn:
        !isDesign &&
        isLeadDesigner &&
        !TextUtils.areEquals(dataflowState.dataflowType, config.dataflowType.BUSINESS.value),
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
        {resourcesContext.messages['restrictFromPublic']}
      </label>
    </div>
  );

  const handleRedirect = target => history.push(target);

  const setIsAdminAssignedBusinessDataflow = value => {
    dataflowDispatch({
      type: 'SET_IS_ADMIN_ASSIGNED_BUSINESS_DATAFLOW',
      payload: { isAdminAssignedBusinessDataflow: value }
    });
  };

  const setIsUserRightManagementDialogVisible = isVisible => {
    manageDialogs('isUserRightManagementDialogVisible', isVisible);
  };

  const shareRightsFooterDialogFooter = userType => (
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
        label={resourcesContext.messages['close']}
        onClick={() => {
          manageDialogs(`isManage${userType}DialogVisible`, false);
          if (dataflowState.isAdminAssignedBusinessDataflow) {
            onLoadReportingDataflow();
            setIsPageLoading(true);
            onRefreshToken();
          }
        }}
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

  const setIsCopyDataCollectionToEUDatasetLoading = value =>
    dataflowDispatch({ type: 'SET_IS_COPY_DATA_COLLECTION_TO_EU_DATASET_LOADING', payload: { isLoading: value } });

  const setIsExportEUDatasetLoading = value =>
    dataflowDispatch({ type: 'SET_IS_EXPORT_EU_DATASET', payload: { isExportEUDatasetLoading: value } });

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
      payload: {
        name: newName,
        description: newDescription,
        isReportingDataflowDialogVisible: false,
        isExportDialogVisible: false
      }
    });
    onLoadReportingDataflow();
  };

  const resetObligations = () => {
    setCheckedObligation({ id: dataflowState.obligations.obligationId, title: dataflowState.obligations.title });
    setObligation({ id: dataflowState.obligations.obligationId, title: dataflowState.obligations.title });
    setPreviousObligation({ id: dataflowState.obligations.obligationId, title: dataflowState.obligations.title });
  };

  const onExportLeadReporters = async () => {
    try {
      const { data } = await RepresentativeService.exportFile(dataflowId);
      if (!isNil(data)) {
        DownloadFile(
          data,
          `${TextUtils.ellipsis(dataflowState.name, config.notifications.STRING_LENGTH_MAX)}_Lead_Reporters.csv`
        );
      }
    } catch (error) {
      console.error('Dataflow - onExportLeadReporters.', error);
      notificationContext.add({ type: 'EXPORT_DATAFLOW_LEAD_REPORTERS_FAILED_EVENT' });
    }
  };

  const getUsersListDialogHeader = () => {
    switch (dataflowState.dataflowType) {
      case config.dataflowType.BUSINESS.value:
        return resourcesContext.messages['dataflowUsersByCompanyList'];

      case config.dataflowType.CITIZEN_SCIENCE.value:
        return resourcesContext.messages['dataflowUsersByOrganizationList'];

      default:
        return resourcesContext.messages['dataflowUsersByCountryList'];
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
        label={resourcesContext.messages['importLeadReporters']}
        onClick={() => manageDialogs('isImportLeadReportersVisible', true)}
      />
      <Button
        className={`${styles.manageLeadReportersButton} p-button-secondary p-button-animated-blink`}
        icon={'export'}
        label={resourcesContext.messages['exportLeadReporters']}
        onClick={onExportLeadReporters}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={'cancel'}
        label={resourcesContext.messages['close']}
        onClick={() => manageDialogs('isManageRolesDialogVisible', false)}
      />
    </Fragment>
  );

  const renderDataflowUsersListFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resourcesContext.messages['close']}
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

    const isAdmin = userContext.accessRole.some(role => role === config.permissions.roles.ADMIN.key);

    dataflowDispatch({
      type: 'LOAD_PERMISSIONS',
      payload: { hasWritePermissions, isCustodian, isNationalCoordinator, isObserver, isAdmin, userRoles }
    });
  };

  const onLoadReportingDataflow = async () => {
    try {
      const dataflow = await DataflowService.get(dataflowId);
      dataflowDispatch({ type: 'SET_IS_FETCHING_DATA', payload: { isFetchingData: false } });
      dataflowDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          anySchemaAvailableInPublic: dataflow.anySchemaAvailableInPublic,
          data: dataflow,
          dataflowType: dataflow.type,
          description: dataflow.description,
          isReleasable: dataflow.isReleasable,
          name: dataflow.name,
          obligations: dataflow.obligation,
          showPublicInfo: dataflow.showPublicInfo,
          status: dataflow.status
        }
      });

      setCheckedObligation({ id: dataflow.obligation.obligationId, title: dataflow.obligation.title });
      setObligation({ id: dataflow.obligation.obligationId, title: dataflow.obligation.title });
      setPreviousObligation({ id: dataflow.obligation.obligationId, title: dataflow.obligation.title });

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
      console.error('Dataflow - onLoadReportingDataflow.', error);
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
      console.error('Dataflow - onUploadLeadReporters.', error);
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
  useCheckNotifications(['DELETE_DATAFLOW_COMPLETED_EVENT'], goToDataflowsPage);

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
    const validationResult = await DataflowService.getSchemasValidation(dataflowId);
    dataflowDispatch({ type: 'SET_IS_DATA_SCHEMA_CORRECT', payload: { validationResult: validationResult.data } });
  };

  function goToDataflowsPage() {
    history.push(getUrl(routes.DATAFLOWS));
  }

  const onSaveName = async (value, index) => {
    try {
      await DatasetService.updateDatasetNameDesign(
        dataflowState.designDatasetSchemas[index].datasetId,
        encodeURIComponent(value)
      );
      const updatedTitles = [...dataflowState.updatedDatasetSchema];
      updatedTitles[index].schemaName = value;
      setUpdatedDatasetSchema(updatedTitles);
    } catch (error) {
      console.error('Dataflow - onSaveName.', error);
      if (error?.response?.status === 400) {
        notificationContext.add({ type: 'DATASET_SCHEMA_CREATION_ERROR_INVALID_NAME', content: { schemaName: value } });
      }
    }
  };

  const onShowManageReportersDialog = () => manageDialogs('isManageRolesDialogVisible', true);

  const onOpenReleaseConfirmDialog = () => manageDialogs('isReleaseDialogVisible', true);

  const onConfirmExport = async () => {
    try {
      dataflowDispatch({ type: 'SET_IS_EXPORTING', payload: true });
      const { data } = await DataflowService.exportSchemas(dataflowId);
      if (!isNil(data)) {
        DownloadFile(data, `${dataflowState.data.name}_${new Date(Date.now()).toDateString().replace(' ', '_')}.zip`);
      }
    } catch (error) {
      console.error('Dataflow - onConfirmExport.', error);
      notificationContext.add({ type: 'EXPORT_DATASET_SCHEMA_FAILED_EVENT' });
    } finally {
      manageDialogs('isExportDialogVisible', false);
      dataflowDispatch({ type: 'SET_IS_EXPORTING', payload: false });
    }
  };

  const onConfirmRelease = async () => {
    try {
      notificationContext.add({ type: 'RELEASE_START_EVENT' });
      await SnapshotService.release(dataflowId, dataProviderId, dataflowState.restrictFromPublic);

      dataflowState.data.datasets
        .filter(dataset => dataset.dataProviderId === dataProviderId)
        .forEach(dataset => (dataset.isReleasing = true));
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'RELEASE_BLOCKED_EVENT' });
      } else {
        console.error('Dataflow - onConfirmRelease.', error);
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
      console.error('Dataflow - onRefreshToken.', error);
      notificationContext.add({ key: 'TOKEN_REFRESH_ERROR', content: {} });
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

  const infoExtensionsTooltip = `${resourcesContext.messages['supportedFileExtensionsTooltip']} ${uniq(
    getImportExtensions.split(', ')
  ).join(', ')}`;

  const onHideObligationDialog = () => {
    manageDialogs('isReportingObligationsDialogVisible', false);
    setObligationToPrevious();
  };

  const renderObligationFooter = () => (
    <Fragment>
      <Button
        icon="check"
        label={resourcesContext.messages['ok']}
        onClick={() => {
          manageDialogs('isReportingObligationsDialogVisible', false);
          setToCheckedObligation();
        }}
      />
      <Button
        className="p-button-secondary button-right-aligned p-button-animated-blink"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={() => {
          manageDialogs('isReportingObligationsDialogVisible', false);
          setObligationToPrevious();
        }}
      />
    </Fragment>
  );

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
      console.error('Dataflow - onConfirmUpdateIsReleaseable.', error);
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
      console.error('Dataflow - onConfirmUpdateShowPublicInfo.', error);
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
          dataflowType={dataflowState.dataflowType}
          handleRedirect={handleRedirect}
          isLeadReporterOfCountry={isLeadReporterOfCountry}
          onCleanUpReceipt={onCleanUpReceipt}
          onOpenReleaseConfirmDialog={onOpenReleaseConfirmDialog}
          onSaveName={onSaveName}
          onShowManageReportersDialog={onShowManageReportersDialog}
          onUpdateData={setIsDataUpdated}
          setIsCopyDataCollectionToEUDatasetLoading={setIsCopyDataCollectionToEUDatasetLoading}
          setIsExportEUDatasetLoading={setIsExportEUDatasetLoading}
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
              ? `${resourcesContext.messages['dataflow']} - ${country}`
              : resourcesContext.messages['dataflow']
          }
          title={dataflowState.name}
        />

        {getBigButtonList()}

        {dataflowState.isReleaseDialogVisible && (
          <ConfirmDialog
            footerAddon={
              dataflowState.anySchemaAvailableInPublic &&
              !TextUtils.areEquals(dataflowState.dataflowType, config.dataflowType.BUSINESS.value) &&
              checkRestrictFromPublic
            }
            header={resourcesContext.messages['confirmReleaseHeader']}
            labelCancel={resourcesContext.messages['no']}
            labelConfirm={resourcesContext.messages['yes']}
            onConfirm={onConfirmRelease}
            onHide={() => {
              manageDialogs('isReleaseDialogVisible', false);
              if (dataflowState.restrictFromPublic) {
                dataflowDispatch({ type: 'SET_RESTRICT_FROM_PUBLIC', payload: false });
              }
            }}
            visible={dataflowState.isReleaseDialogVisible}>
            {resourcesContext.messages['confirmReleaseQuestion']}
          </ConfirmDialog>
        )}

        {dataflowState.isCustodian && dataflowState.isManageRolesDialogVisible && (
          <Dialog
            contentStyle={{ maxHeight: '60vh' }}
            footer={manageRoleDialogFooter}
            header={resourcesContext.messages['manageRolesDialogTitle']}
            onHide={() => manageDialogs('isManageRolesDialogVisible', false)}
            visible={dataflowState.isManageRolesDialogVisible}>
            <div className={styles.dialog}>
              <RepresentativesList
                dataflowId={dataflowId}
                dataflowType={dataflowState.dataflowType}
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
            header={resourcesContext.messages['manageRequestersRights']}
            onHide={() => {
              manageDialogs('isManageRequestersDialogVisible', false);
              if (dataflowState.isAdminAssignedBusinessDataflow) {
                onLoadReportingDataflow();
                setIsPageLoading(true);
                onRefreshToken();
              }
            }}
            visible={dataflowState.isManageRequestersDialogVisible}>
            <ShareRights
              addConfirmHeader={resourcesContext.messages[`addRequesterConfirmHeader`]}
              addErrorNotificationKey={'ADD_REQUESTER_ERROR'}
              columnHeader={resourcesContext.messages['requestersEmailColumn']}
              dataProviderId={dataProviderId}
              dataflowId={dataflowId}
              deleteColumnHeader={resourcesContext.messages['deleteRequesterButtonTableHeader']}
              deleteConfirmHeader={resourcesContext.messages[`requestersRightsDialogConfirmDeleteHeader`]}
              deleteConfirmMessage={resourcesContext.messages[`requestersRightsDialogConfirmDeleteQuestion`]}
              deleteErrorNotificationKey={'DELETE_REQUESTER_ERROR'}
              editConfirmHeader={resourcesContext.messages[`editRequesterConfirmHeader`]}
              getErrorNotificationKey={'GET_REQUESTERS_ERROR'}
              isUserRightManagementDialogVisible={dataflowState.isUserRightManagementDialogVisible}
              placeholder={resourcesContext.messages['manageRolesRequesterDialogInputPlaceholder']}
              representativeId={representativeId}
              roleOptions={isOpenStatus ? requesterRoleOptionsOpenStatus : requesterRoleOptions}
              setIsAdminAssignedBusinessDataflow={setIsAdminAssignedBusinessDataflow}
              setIsUserRightManagementDialogVisible={setIsUserRightManagementDialogVisible}
              updateErrorNotificationKey={'UPDATE_REQUESTER_ERROR'}
              userType={'requester'}
            />
          </Dialog>
        )}

        {dataflowState.isManageReportersDialogVisible && (
          <Dialog
            footer={shareRightsFooterDialogFooter('Reporters')}
            header={resourcesContext.messages['manageReportersRights']}
            onHide={() => manageDialogs('isManageReportersDialogVisible', false)}
            visible={dataflowState.isManageReportersDialogVisible}>
            <ShareRights
              addConfirmHeader={resourcesContext.messages[`addReporterConfirmHeader`]}
              addErrorNotificationKey={'ADD_REPORTER_ERROR'}
              columnHeader={resourcesContext.messages['reportersEmailColumn']}
              dataProviderId={dataProviderId}
              dataflowId={dataflowId}
              deleteColumnHeader={resourcesContext.messages['deleteReporterButtonTableHeader']}
              deleteConfirmHeader={resourcesContext.messages[`reportersRightsDialogConfirmDeleteHeader`]}
              deleteConfirmMessage={resourcesContext.messages[`reportersRightsDialogConfirmDeleteQuestion`]}
              deleteErrorNotificationKey={'DELETE_REPORTER_ERROR'}
              editConfirmHeader={resourcesContext.messages[`editReporterConfirmHeader`]}
              getErrorNotificationKey={'GET_REPORTERS_ERROR'}
              isUserRightManagementDialogVisible={dataflowState.isUserRightManagementDialogVisible}
              placeholder={resourcesContext.messages['manageRolesReporterDialogInputPlaceholder']}
              representativeId={representativeId}
              roleOptions={reporterRoleOptions}
              setIsAdminAssignedBusinessDataflow={setIsAdminAssignedBusinessDataflow}
              setIsUserRightManagementDialogVisible={setIsUserRightManagementDialogVisible}
              updateErrorNotificationKey={'UPDATE_REPORTER_ERROR'}
              userType={'reporter'}
            />
          </Dialog>
        )}

        {dataflowState.isExportDialogVisible && (
          <ConfirmDialog
            disabledConfirm={dataflowState.isExporting}
            header={resourcesContext.messages['exportSchema']}
            labelCancel={resourcesContext.messages['no']}
            labelConfirm={resourcesContext.messages['yes']}
            onConfirm={onConfirmExport}
            onHide={() => manageDialogs('isExportDialogVisible', false)}
            visible={dataflowState.isExportDialogVisible}>
            {resourcesContext.messages['confirmExportSchema']}
          </ConfirmDialog>
        )}

        {dataflowState.isReleaseableDialogVisible && (
          <ConfirmDialog
            disabledConfirm={
              dataflowState.data.isReleasable === dataflowState.isReleasable || dataflowState.isFetchingData
            }
            header={resourcesContext.messages['isReleasableDataflowDialogHeader']}
            iconConfirm={dataflowState.isFetchingData && 'spinnerAnimate'}
            labelCancel={resourcesContext.messages['cancel']}
            labelConfirm={resourcesContext.messages['save']}
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
              <span className={styles.pointer} onClick={() => setIsReleaseable(!dataflowState.isReleasable)}>
                {resourcesContext.messages['isReleasableDataflowCheckboxLabel']}
              </span>
            </label>
          </ConfirmDialog>
        )}

        {dataflowState.isShowPublicInfoDialogVisible && (
          <ConfirmDialog
            disabledConfirm={
              dataflowState.data.showPublicInfo === dataflowState.showPublicInfo || dataflowState.isFetchingData
            }
            header={resourcesContext.messages['showPublicInfoDataflowDialogHeader']}
            iconConfirm={dataflowState.isFetchingData && 'spinnerAnimate'}
            labelCancel={resourcesContext.messages['cancel']}
            labelConfirm={resourcesContext.messages['save']}
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
              <span
                className={styles.pointer}
                onClick={() =>
                  dataflowDispatch({
                    type: 'SET_SHOW_PUBLIC_INFO',
                    payload: { showPublicInfo: !dataflowState.showPublicInfo }
                  })
                }>
                {resourcesContext.messages['showPublicInfoDataflowCheckboxLabel']}
              </span>
            </label>
          </ConfirmDialog>
        )}

        {dataflowState.isImportLeadReportersVisible && (
          <CustomFileUpload
            accept={getImportExtensions}
            chooseLabel={resourcesContext.messages['selectFile']}
            dialogHeader={`${resourcesContext.messages['importLeadReporters']}`}
            dialogOnHide={() => manageDialogs('isImportLeadReportersVisible', false)}
            dialogVisible={dataflowState.isImportLeadReportersVisible}
            infoTooltip={infoExtensionsTooltip}
            invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
            isDialog={true}
            name="file"
            onUpload={onUploadLeadReporters}
            url={`${window.env.REACT_APP_BACKEND}${getUrl(RepresentativeConfig.importFile, {
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
                ? getUsersListDialogHeader()
                : resourcesContext.messages['dataflowUsersList']
            }
            onHide={() => manageDialogs('isUserListVisible', false)}
            visible={dataflowState.isUserListVisible}>
            <UserList
              dataflowId={dataflowId}
              dataflowType={dataflowState.dataflowType}
              representativeId={dataflowState.isObserver ? representativeId : dataProviderId}
            />
          </Dialog>
        )}

        <PropertiesDialog dataflowState={dataflowState} manageDialogs={manageDialogs} />

        {dataflowState.isReportingDataflowDialogVisible && (
          <ManageDataflow
            dataflowId={dataflowId}
            isEditForm
            isVisible={dataflowState.isReportingDataflowDialogVisible}
            manageDialogs={manageDialogs}
            obligation={obligation}
            onEditDataflow={onEditDataflow}
            resetObligations={resetObligations}
            setCheckedObligation={setCheckedObligation}
            state={dataflowState}
          />
        )}

        {dataflowState.isBusinessDataflowDialogVisible && (
          <ManageBusinessDataflow
            dataflowId={dataflowId}
            hasRepresentatives={dataflowState.data.representatives.length !== 0}
            isAdmin={dataflowState.isAdmin}
            isEditing
            isVisible={dataflowState.isBusinessDataflowDialogVisible}
            manageDialogs={manageDialogs}
            obligation={obligation}
            onEditDataflow={onEditDataflow}
            onLoadReportingDataflow={onLoadReportingDataflow}
            resetObligations={resetObligations}
            state={{
              name: dataflowState.name,
              description: dataflowState.description,
              status: dataflowState.status,
              fmeUserId: dataflowState.data.fmeUserId,
              fmeUserName: dataflowState.data.fmeUserName,
              dataProviderGroupId: dataflowState.data.dataProviderGroupId,
              dataProviderGroupName: dataflowState.data.dataProviderGroupName
            }}
          />
        )}

        {dataflowState.isReportingObligationsDialogVisible && (
          <Dialog
            footer={renderObligationFooter()}
            header={resourcesContext.messages['reportingObligations']}
            onHide={onHideObligationDialog}
            style={{ width: '95%' }}
            visible={dataflowState.isReportingObligationsDialogVisible}>
            <ReportingObligations obligationChecked={obligation} setCheckedObligation={setCheckedObligation} />
          </Dialog>
        )}

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
