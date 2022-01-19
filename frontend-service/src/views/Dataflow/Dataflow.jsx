import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

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
import { RepresentativeConfig } from 'repositories/config/RepresentativeConfig';
import { routes } from 'conf/routes';

import { ApiKeyDialog } from 'views/_components/ApiKeyDialog';
import { BigButtonList } from './_components/BigButtonList';
import { BigButtonListRepresentative } from './_components/BigButtonListRepresentative';
import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { DatasetsInfo } from 'views/_components/DatasetsInfo';
import { Dialog } from 'views/_components/Dialog';
import { DownloadFile } from 'views/_components/DownloadFile';
import { MainLayout } from 'views/_components/Layout';
import { ManageBusinessDataflow } from 'views/_components/ManageBusinessDataflow';
import { ManageDataflow } from 'views/_components/ManageDataflow';
import { Menu } from 'views/_components/Menu';
import { PropertiesDialog } from './_components/PropertiesDialog';
import { ReportingObligations } from 'views/_components/ReportingObligations';
import { RepresentativesList } from './_components/RepresentativesList';
import { ShareRights } from 'views/_components/ShareRights';
import { Spinner } from 'views/_components/Spinner';
import { Title } from 'views/_components/Title';
import { UserList } from 'views/_components/UserList';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';
import { RepresentativeService } from 'services/RepresentativeService';
import { SnapshotService } from 'services/SnapshotService';
import { UserRightService } from 'services/UserRightService';
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
import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

const Dataflow = () => {
  const navigate = useNavigate();
  const { dataflowId, representativeId } = useParams();

  const exportImportMenuRef = useRef(null);

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const dataflowInitialState = {
    anySchemaAvailableInPublic: false,
    currentUrl: '',
    data: {},
    dataflowType: '',
    dataProviderId: [],
    dataProviderSelected: {},
    deleteInput: '',
    description: '',
    designDatasetSchemas: [],
    formHasRepresentatives: false,
    hasCustodianPermissions: false,
    hasReporters: false,
    hasRepresentativesWithoutDatasets: false,
    hasWritePermissions: false,
    id: dataflowId,
    isApiKeyDialogVisible: false,
    isBusinessDataflowDialogVisible: false,
    isCopyDataCollectionToEUDatasetLoading: false,
    isDataSchemaCorrect: [],
    isDatasetsInfoDialogVisible: false,
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isDownloadingUsers: false,
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
    isRightPermissionsChanged: false,
    isShowPublicInfoDialogVisible: false,
    isShowPublicInfoUpdating: false,
    isSnapshotDialogVisible: false,
    isUpdatingPermissions: false,
    isUserListVisible: false,
    isUserRightManagementDialogVisible: false,
    isValidateLeadReportersDialogVisible: false,
    isValidateReportersDialogVisible: false,
    name: '',
    obligations: {},
    representative: {},
    representativesImport: false,
    restrictFromPublic: false,
    restrictFromPublicIsUpdating: {},
    showPublicInfo: false,
    status: '',
    updatedDatasetSchema: [],
    userRoles: []
  };

  const [dataflowState, dataflowDispatch] = useReducer(dataflowDataReducer, dataflowInitialState);

  const usersTypes = { REPORTERS: 'Reporters', REQUESTERS: 'Requesters' };

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

  const isAdmin = userContext.hasPermission([config.permissions.roles.ADMIN.key]);

  const isSteward = userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowState.id, [
    config.permissions.roles.STEWARD.key
  ]);

  const isCustodian = userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowState.id, [
    config.permissions.roles.CUSTODIAN.key
  ]);

  const isLeadDesigner = isSteward || isCustodian;

  const isStewardSupport = userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowId, [
    config.permissions.roles.STEWARD_SUPPORT.key
  ]);

  const hasCustodianPermissions = isStewardSupport || isLeadDesigner;

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

  const isBusinessDataflow = TextUtils.areEquals(dataflowState.dataflowType, config.dataflowType.BUSINESS.value);

  const getCountry = () => {
    if (uniqDataProviders.length === 1) {
      return uniq(map(dataflowState.data.datasets, 'datasetSchemaName'));
    } else if (isNil(representativeId)) {
      return null;
    } else {
      return uniq(
        map(
          dataflowState.data?.datasets?.filter(d => d.dataProviderId?.toString() === representativeId),
          'datasetSchemaName'
        )
      );
    }
  };

  const country = getCountry();

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

  const isReleased =
    !isNil(dataflowState.data.datasets) &&
    dataflowState.data.datasets.some(dataset => dataset.isReleased && dataset.dataProviderId === dataProviderId);

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
    representativeId
  });

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) onLoadPermission();
  }, [userContext, dataflowState.data]);

  useEffect(() => {
    if (dataflowState.hasCustodianPermissions) {
      if (isOpenStatus) {
        leftSideBarContext.addHelpSteps(DataflowDraftRequesterHelpConfig, 'dataflowRequesterDraftHelp');
      } else {
        leftSideBarContext.addHelpSteps(DataflowRequesterHelpConfig, 'dataflowRequesterDesignHelp');
      }
    } else {
      leftSideBarContext.addHelpSteps(DataflowReporterHelpConfig, 'dataflowReporterHelp');
    }
  }, [userContext, dataflowState]);

  useEffect(() => {
    if (notificationContext.hidden.some(notification => notification.key === 'EXPORT_USERS_BY_COUNTRY_FAILED_EVENT')) {
      setIsDownloadingUsers(false);
    }
  }, [notificationContext.hidden]);

  const exportImportMenuItems = [
    {
      command: () => onExportLeadReportersTemplate(),
      disabled: isEmpty(dataflowState.dataProviderSelected),
      icon: 'download',
      label: resourcesContext.messages['exportLeadReportersTemplate'],
      tooltip: `${resourcesContext.messages['exportLeadReportersTemplateTooltip']} ${dataflowState.dataProviderSelected?.label}`
    },
    {
      command: () => onExportLeadReporters(),
      icon: 'download',
      label: resourcesContext.messages['exportLeadReporters'],
      tooltip: resourcesContext.messages['exportLeadReportersTooltip']
    },
    {
      command: () => manageDialogs('isImportLeadReportersVisible', true),
      disabled: isEmpty(dataflowState.dataProviderSelected),
      icon: 'upload',
      label: resourcesContext.messages['importLeadReporters'],
      tooltip: resourcesContext.messages['importLeadReportersTooltip']
    }
  ];

  const manageDialogs = (dialog, value, secondDialog, secondValue) =>
    dataflowDispatch({
      type: 'MANAGE_DIALOGS',
      payload: { dialog, value, secondDialog, secondValue, deleteInput: '' }
    });

  const getLeftSidebarButtonsVisibility = () => {
    if (isEmpty(dataflowState.data)) {
      return {
        apiKeyBtn: false,
        datasetsInfoBtn: false,
        editBtn: false,
        editBusinessBtn: false,
        exportBtn: false,
        manageReportersBtn: false,
        manageRequestersBtn: false,
        propertiesBtn: false,
        releaseableBtn: false,
        restrictFromPublicBtn: false,
        showPublicInfoBtn: false,
        usersListBtn: false
      };
    }

    return {
      apiKeyBtn: isLeadDesigner || isLeadReporterOfCountry,
      datasetsInfoBtn: isAdmin && isNil(dataProviderId),
      editBtn: isDesign && isLeadDesigner && !isBusinessDataflow,
      editBusinessBtn: (isAdmin || isLeadDesigner) && isBusinessDataflow,
      exportBtn: isLeadDesigner && dataflowState.designDatasetSchemas.length > 0,
      manageReportersBtn: isLeadReporterOfCountry,
      manageRequestersBtn: isAdmin || (isBusinessDataflow && isSteward) || (!isBusinessDataflow && isLeadDesigner),
      propertiesBtn: true,
      releaseableBtn: !isDesign && isLeadDesigner,
      restrictFromPublicBtn:
        isLeadReporterOfCountry && dataflowState.showPublicInfo && isReleased && !isBusinessDataflow,
      showPublicInfoBtn: !isDesign && isLeadDesigner,
      usersListBtn:
        hasCustodianPermissions ||
        isLeadReporterOfCountry ||
        isNationalCoordinatorOfCountry ||
        isReporterOfCountry ||
        isObserver
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
    <div style={{ float: 'left', marginTop: '5px' }}>
      <Checkbox
        checked={dataflowState.restrictFromPublic}
        id="restrict_from_public_checkbox"
        inputId="restrict_from_public_checkbox"
        onChange={() => setRestrictFromPublic(!dataflowState.restrictFromPublic)}
        role="checkbox"
      />
      <label
        className={styles.restrictFromPublic}
        onClick={() => setRestrictFromPublic(!dataflowState.restrictFromPublic)}
        style={{ cursor: 'pointer', fontWeight: 'bold' }}>
        {resourcesContext.messages['restrictFromPublicCheckboxLabel']}
      </label>
    </div>
  );

  const handleRedirect = target => navigate(target);

  const setRightPermissionsChange = isRightPermissionsChanged => {
    dataflowDispatch({
      type: 'SET_IS_RIGHT_PERMISSIONS_CHANGED',
      payload: { isRightPermissionsChanged }
    });
  };

  const setHasReporters = hasReporters => {
    dataflowDispatch({
      type: 'SET_HAS_REPORTERS',
      payload: { hasReporters }
    });
  };

  const setIsUserRightManagementDialogVisible = isVisible => {
    manageDialogs('isUserRightManagementDialogVisible', isVisible);
  };

  const renderValidateReportersButton = usersType => {
    if (usersType === usersTypes.REPORTERS) {
      return (
        <Button
          className={`${styles.buttonLeft} p-button-secondary p-button-animated-blink ${
            dataflowState.isUpdatingPermissions || !dataflowState.hasReporters ? 'p-button-animated-spin' : ''
          }`}
          disabled={dataflowState.isUpdatingPermissions || !dataflowState.hasReporters}
          icon={dataflowState.isUpdatingPermissions ? 'spinnerAnimate' : 'refresh'}
          label={resourcesContext.messages['updateUsersPermissionsButton']}
          onClick={() => manageDialogs('isValidateReportersDialogVisible', true)}
        />
      );
    }
  };

  const shareRightsFooterDialogFooter = usersType => {
    const isAddButtonHidden = isBusinessDataflow && !isAdmin && !isSteward;

    return (
      <div className={styles.buttonsRolesFooter}>
        {isAddButtonHidden ? null : (
          <Button
            className={`${styles.buttonLeft} p-button-secondary p-button-animated-blink`}
            icon="plus"
            label={resourcesContext.messages['add']}
            onClick={() => manageDialogs('isUserRightManagementDialogVisible', true)}
          />
        )}

        {renderValidateReportersButton(usersType)}

        <Button
          className={`p-button-secondary p-button-animated-blink`}
          icon="cancel"
          label={resourcesContext.messages['close']}
          onClick={() => {
            manageDialogs(`isManage${usersType}DialogVisible`, false);
            if (dataflowState.isRightPermissionsChanged) {
              onLoadReportingDataflow();
              setIsPageLoading(true);
              onRefreshToken();
            }
          }}
        />
      </div>
    );
  };

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

  const setIsShowPublicInfoUpdating = isShowPublicInfoUpdating =>
    dataflowDispatch({ type: 'SHOW_PUBLIC_INFO_IS_UPDATING', payload: { isShowPublicInfoUpdating } });

  const setRepresentative = representative => dataflowDispatch({ type: 'SET_REPRESENTATIVE', payload: representative });

  const setRestrictFromPublic = restrictFromPublicValue =>
    dataflowDispatch({ type: 'SET_RESTRICT_FROM_PUBLIC', payload: restrictFromPublicValue });

  const setRestrictFromPublicIsUpdating = (value, dataProviderId) => {
    dataflowDispatch({
      type: 'RESTRICT_FROM_PUBLIC_IS_UPDATING',
      payload: { value: value, dataProviderId: dataProviderId }
    });
  };

  const setIsUpdatingPermissions = isUpdatingPermissions =>
    dataflowDispatch({ type: 'SET_IS_UPDATING_PERMISSIONS', payload: { isUpdatingPermissions } });

  useCheckNotifications(
    [
      'VALIDATE_LEAD_REPORTERS_COMPLETED_EVENT',
      'VALIDATE_LEAD_REPORTERS_FAILED_EVENT',
      'VALIDATE_REPORTERS_COMPLETED_EVENT',
      'VALIDATE_REPORTERS_FAILED_EVENT'
    ],
    setIsUpdatingPermissions,
    false
  );

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
      notificationContext.add({ type: 'EXPORT_DATAFLOW_LEAD_REPORTERS_FAILED_EVENT' }, true);
    }
  };

  const onExportLeadReportersTemplate = async () => {
    try {
      const { data } = await RepresentativeService.exportTemplateFile(
        dataflowState.dataProviderSelected?.dataProviderGroupId
      );
      if (!isNil(data)) {
        DownloadFile(data, `GroupId_${dataflowState.dataProviderSelected?.dataProviderGroupId}_Template.csv`);
      }
    } catch (error) {
      console.error('Dataflow - onExportLeadReportersTemplate.', error);
      notificationContext.add({ type: 'EXPORT_DATAFLOW_LEAD_REPORTERS_TEMPLATE_FAILED_EVENT' }, true);
    }
  };

  const onConfirmValidateReporters = async () => {
    manageDialogs('isValidateReportersDialogVisible', false);
    setIsUpdatingPermissions(true);

    try {
      await UserRightService.validateReporters(dataflowId, dataProviderId);
    } catch (error) {
      console.error('Dataflow - onConfirmReassign.', error);
      notificationContext.add({ type: 'VALIDATE_REPORTERS_FAILED_EVENT' }, true);
      setIsUpdatingPermissions(false);
    }
  };

  const onConfirmValidateLeadReporters = async () => {
    manageDialogs('isValidateLeadReportersDialogVisible', false);
    setIsUpdatingPermissions(true);

    try {
      await RepresentativeService.validateLeadReporters(dataflowId);
    } catch (error) {
      console.error('Dataflow - onConfirmValidateLeadReporters.', error);
      notificationContext.add({ type: 'VALIDATE_LEAD_REPORTERS_FAILED_EVENT' }, true);
      setIsUpdatingPermissions(false);
    }
  };

  const manageRoleDialogFooter = (
    <Fragment>
      <Button
        className={`${styles.buttonLeft} p-button-secondary ${
          !isEmpty(dataflowState.dataProviderSelected) ? 'p-button-animated-blink' : ''
        }`}
        disabled={isEmpty(dataflowState.dataProviderSelected)}
        icon="sortAlt"
        id="buttonExportImport"
        label={resourcesContext.messages['exportImport']}
        onClick={event => {
          exportImportMenuRef.current.show(event);
        }}
      />
      <Menu
        className={styles.exportImportMenu}
        id="exportImportMenu"
        model={exportImportMenuItems}
        popup={true}
        ref={exportImportMenuRef}
      />
      <Button
        className={`${styles.buttonLeft} p-button-secondary ${
          !isEmpty(dataflowState.dataProviderSelected) ? 'p-button-animated-blink' : ''
        } ${dataflowState.isUpdatingPermissions ? 'p-button-animated-spin' : ''}`}
        disabled={dataflowState.isUpdatingPermissions || isEmpty(dataflowState.dataProviderSelected)}
        icon={dataflowState.isUpdatingPermissions ? 'spinnerAnimate' : 'refresh'}
        label={resourcesContext.messages['updateUsersPermissionsButton']}
        onClick={() => manageDialogs('isValidateLeadReportersDialogVisible', true)}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={() => manageDialogs('isManageRolesDialogVisible', false)}
      />
    </Fragment>
  );

  const renderDialogFooterCloseBtn = modalType => (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon="cancel"
      label={resourcesContext.messages['close']}
      onClick={() => manageDialogs(modalType, false)}
    />
  );

  const onDownloadUsersByCountry = async () => {
    setIsDownloadingUsers(true);

    try {
      await DataflowService.generateUsersByCountryFile(dataflowId);
      notificationContext.add({ type: 'DOWNLOAD_USERS_BY_COUNTRY_START' });
    } catch (error) {
      console.error('Dataflow - onDownloadUsersByCountry.', error);
      if (error.response?.status === 400) {
        notificationContext.add({ type: 'DOWNLOAD_FILE_BAD_REQUEST_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'GENERATE_USERS_LIST_FILE_ERROR' }, true);
      }
      setIsDownloadingUsers(false);
    }
  };

  useCheckNotifications(
    ['AUTOMATICALLY_DOWNLOAD_USERS_LIST_FILE', 'DOWNLOAD_USERS_LIST_FILE_ERROR', 'DOWNLOAD_FILE_BAD_REQUEST_ERROR'],
    setIsDownloadingUsers,
    false
  );

  function setIsDownloadingUsers(isDownloadingUsers) {
    dataflowDispatch({ type: 'SET_IS_DOWNLOADING_USERS', payload: isDownloadingUsers });
  }

  const renderUserListDialogFooter = () => (
    <div className={styles.buttonsRolesFooter}>
      <Button
        className={`p-button-secondary p-button-animated-blink ${styles.buttonLeft}`}
        disabled={dataflowState.isDownloadingUsers}
        icon={dataflowState.isDownloadingUsers ? 'spinnerAnimate' : 'export'}
        label={resourcesContext.messages['downloadUsersListButtonLabel']}
        onClick={() => onDownloadUsersByCountry()}
      />
      <Button
        className={`p-button-secondary p-button-animated-blink ${styles.closeButton}`}
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={() => manageDialogs('isUserListVisible', false)}
      />
    </div>
  );

  const getCurrentDatasetId = () => {
    if (isEmpty(dataflowState.data)) {
      return null;
    }

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

    const entity =
      isNil(representativeId) || representativeId !== 0
        ? `${config.permissions.prefixes.DATAFLOW}${dataflowId}`
        : `${config.permissions.prefixes.DATASET}${currentDatasetId}`;

    const userRoles = userContext.getUserRole(entity);

    dataflowDispatch({
      type: 'LOAD_PERMISSIONS',
      payload: {
        hasWritePermissions,
        hasCustodianPermissions,
        isNationalCoordinator,
        isObserver,
        isAdmin,
        userRoles
      }
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

        if (!isEmpty(dataflow.representatives)) {
          const representative = dataflow.representatives.find(
            representative => representative.dataProviderId === parseInt(representativeId)
          );

          if (!isEmpty(representative)) {
            setRepresentativeAndRestrictFromPublic(representative, representative.restrictFromPublic);
          }
        }
      } else {
        if (!isEmpty(dataflow.representatives)) {
          const isReceiptOutdated = dataflow.representatives.map(representative => representative.isReceiptOutdated);

          if (isReceiptOutdated.length === 1) {
            setIsReceiptOutdated(isReceiptOutdated[0]);
          }

          if (isLeadReporter) {
            const representative = dataflow.representatives[0];
            if (!isEmpty(representative)) {
              setRepresentativeAndRestrictFromPublic(representative, representative.restrictFromPublic);
            }
          }
        }
      }
    } catch (error) {
      console.error('Dataflow - onLoadReportingDataflow.', error);
      notificationContext.add({ type: 'LOAD_DATAFLOW_DATA_ERROR' }, true);
      navigate(getUrl(routes.DATAFLOWS));
    } finally {
      setIsPageLoading(false);
      setIsShowPublicInfoUpdating(false);
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
      notificationContext.add({ type: 'IMPORT_DATAFLOW_LEAD_REPORTERS_FAILED_EVENT' }, true);
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
    navigate(getUrl(routes.DATAFLOWS));
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
        notificationContext.add(
          { type: 'DATASET_SCHEMA_CREATION_ERROR_INVALID_NAME', content: { customContent: { schemaName: value } } },
          true
        );
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
      notificationContext.add({ type: 'EXPORT_DATASET_SCHEMA_FAILED_EVENT' }, true);
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
        notificationContext.add({ type: 'RELEASE_BLOCKED_EVENT' }, true);
      } else {
        console.error('Dataflow - onConfirmRelease.', error);
        notificationContext.add({ type: 'RELEASE_FAILED_EVENT', content: {} }, true);
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
      notificationContext.add({ key: 'TOKEN_REFRESH_ERROR', content: {} }, true);
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

  useCheckNotifications(
    ['UPDATE_RELEASABLE_FAILED_EVENT', 'UPDATE_RESTRICT_FROM_PUBLIC_FAILED_EVENT', 'UPDATE_PUBLIC_STATUS_FAILED_EVENT'],
    setIsDataUpdated
  );

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

  const onConfirmRestrictFromPublic = async () => {
    manageDialogs('isRestrictFromPublicDialogVisible', false);
    try {
      const selectedDataProviderId = dataProviderId || dataflowState.representative.dataProviderId;
      setRestrictFromPublicIsUpdating(true, selectedDataProviderId);
      dataflowDispatch({ type: 'SET_IS_FETCHING_DATA', payload: { isFetchingData: true } });
      await RepresentativeService.updateRestrictFromPublic(
        dataflowId,
        selectedDataProviderId,
        dataflowState.restrictFromPublic
      );
      onLoadReportingDataflow();
    } catch (error) {
      console.error('Dataflow - onConfirmRestrictFromPublic.', error);
      notificationContext.add({ type: 'UPDATE_RESTRICT_FROM_PUBLIC_FAILED_EVENT', content: { dataflowId } }, true);
      setRestrictFromPublic(!dataflowState.restrictFromPublic);
    }
  };

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
      notificationContext.add({ type: 'UPDATE_RELEASABLE_FAILED_EVENT', content: { dataflowId } }, true);
    }
  };

  const onConfirmUpdateShowPublicInfo = async () => {
    manageDialogs('isShowPublicInfoDialogVisible', false);
    try {
      setIsShowPublicInfoUpdating(true);
      dataflowDispatch({ type: 'SET_IS_FETCHING_DATA', payload: { isFetchingData: true } });
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
      notificationContext.add({ type: 'UPDATE_PUBLIC_STATUS_FAILED_EVENT', content: { dataflowId } }, true);
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
    { label: config.permissions.roles.STEWARD.label, role: config.permissions.roles.STEWARD.key },
    { label: config.permissions.roles.CUSTODIAN.label, role: config.permissions.roles.CUSTODIAN.key },
    { label: config.permissions.roles.STEWARD_SUPPORT.label, role: config.permissions.roles.STEWARD_SUPPORT.key },
    { label: config.permissions.roles.OBSERVER.label, role: config.permissions.roles.OBSERVER.key }
  ];

  const requesterRoleOptions = [
    ...requesterRoleOptionsOpenStatus,
    { label: config.permissions.roles.EDITOR_WRITE.label, role: config.permissions.roles.EDITOR_WRITE.key },
    { label: config.permissions.roles.EDITOR_READ.label, role: config.permissions.roles.EDITOR_READ.key }
  ];

  const setSelectedRepresentative = dataProviderId => {
    if (isEmpty(dataflowState.representative) || dataProviderId !== dataflowState.representative.dataProviderId) {
      const representative = dataflowState.data.representatives.find(
        representative => representative.dataProviderId === dataProviderId
      );

      if (!isEmpty(representative)) {
        setRepresentativeAndRestrictFromPublic(representative, representative.restrictFromPublic);
      }
    }
  };

  const setRepresentativeAndRestrictFromPublic = (representative, restrictFromPublicValue) => {
    setRepresentative(representative);
    setRestrictFromPublic(restrictFromPublicValue);
    setRestrictFromPublicIsUpdating(false, representative.dataProviderId);
  };

  const getBigButtonList = () => {
    if (isNil(representativeId)) {
      return (
        <BigButtonList
          className="dataflow-big-buttons-help-step"
          dataflowState={dataflowState}
          dataflowType={dataflowState.dataflowType}
          dataProviderId={dataProviderId}
          handleRedirect={handleRedirect}
          isLeadReporter={isLeadReporter}
          isLeadReporterOfCountry={isLeadReporterOfCountry}
          manageDialogs={manageDialogs}
          onCleanUpReceipt={onCleanUpReceipt}
          onOpenReleaseConfirmDialog={onOpenReleaseConfirmDialog}
          onSaveName={onSaveName}
          onShowManageReportersDialog={onShowManageReportersDialog}
          onUpdateData={setIsDataUpdated}
          setIsCopyDataCollectionToEUDatasetLoading={setIsCopyDataCollectionToEUDatasetLoading}
          setIsExportEUDatasetLoading={setIsExportEUDatasetLoading}
          setIsReceiptLoading={setIsReceiptLoading}
          setSelectedRepresentative={setSelectedRepresentative}
          setUpdatedDatasetSchema={setUpdatedDatasetSchema}
        />
      );
    } else {
      return (
        <BigButtonListRepresentative
          dataflowState={dataflowState}
          dataProviderId={dataProviderId}
          handleRedirect={handleRedirect}
          isLeadReporterOfCountry={isLeadReporterOfCountry}
          manageDialogs={manageDialogs}
          onCleanUpReceipt={onCleanUpReceipt}
          onOpenReleaseConfirmDialog={onOpenReleaseConfirmDialog}
          representativeId={representativeId}
          setIsReceiptLoading={setIsReceiptLoading}
        />
      );
    }
  };

  const layout = children => (
    <MainLayout leftSideBarConfig={{ hasCustodianPermissions: dataflowState.hasCustodianPermissions, buttons: [] }}>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  if (dataflowState.isPageLoading || isNil(dataflowState.data)) return layout(<Spinner />);

  const getSubtitle = () => {
    if (parseInt(representativeId) === 0) {
      return dataflowState.data.name;
    } else {
      if (isInsideACountry && !isNil(country) && country.length > 0) {
        return dataflowState.data.name;
      } else {
        return resourcesContext.messages['dataflow'];
      }
    }
  };

  const getTitle = () => {
    if (parseInt(representativeId) !== 0) {
      if (isInsideACountry && !isNil(country) && country.length > 0) {
        return `${resourcesContext.messages['dataflow']} - ${country}`;
      } else {
        return dataflowState.data.name;
      }
    } else {
      return resourcesContext.messages['testDataset'];
    }
  };

  return layout(
    <div className="rep-row">
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-12`}>
        <Title icon="clone" iconSize="4rem" subtitle={getSubtitle()} title={getTitle()} />

        {getBigButtonList()}

        {dataflowState.isReleaseDialogVisible && (
          <ConfirmDialog
            footerAddon={dataflowState.anySchemaAvailableInPublic && !isBusinessDataflow && checkRestrictFromPublic}
            header={resourcesContext.messages['confirmReleaseHeader']}
            labelCancel={resourcesContext.messages['no']}
            labelConfirm={resourcesContext.messages['yes']}
            onConfirm={onConfirmRelease}
            onHide={() => {
              manageDialogs('isReleaseDialogVisible', false);
              if (dataflowState.representative.restrictFromPublic !== dataflowState.restrictFromPublic) {
                setRestrictFromPublic(dataflowState.representative.restrictFromPublic);
              }
            }}
            visible={dataflowState.isReleaseDialogVisible}>
            {resourcesContext.messages['confirmReleaseQuestion']}
          </ConfirmDialog>
        )}

        {hasCustodianPermissions && dataflowState.isManageRolesDialogVisible && (
          <Dialog
            className="responsiveDialog"
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
                selectedDataProviderGroup={{
                  dataProviderGroupId: dataflowState.data.dataProviderGroupId,
                  label: dataflowState.data.dataProviderGroupName
                }}
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
            className="responsiveDialog"
            footer={shareRightsFooterDialogFooter(usersTypes.REQUESTERS)}
            header={resourcesContext.messages['manageRequestersRights']}
            onHide={() => {
              manageDialogs('isManageRequestersDialogVisible', false);
              if (dataflowState.isRightPermissionsChanged) {
                onLoadReportingDataflow();
                setIsPageLoading(true);
                onRefreshToken();
              }
            }}
            visible={dataflowState.isManageRequestersDialogVisible}>
            <ShareRights
              addConfirmHeader={resourcesContext.messages['addRequesterConfirmHeader']}
              addErrorNotificationKey={'ADD_REQUESTER_ERROR'}
              columnHeader={resourcesContext.messages['requestersEmailColumn']}
              dataflowId={dataflowId}
              dataProviderId={dataProviderId}
              deleteConfirmHeader={resourcesContext.messages['requestersRightsDialogConfirmDeleteHeader']}
              deleteConfirmMessage={resourcesContext.messages['requestersRightsDialogConfirmDeleteQuestion']}
              deleteErrorNotificationKey={'DELETE_REQUESTER_ERROR'}
              editConfirmHeader={resourcesContext.messages['editRequesterConfirmHeader']}
              getErrorNotificationKey={'GET_REQUESTERS_ERROR'}
              isAdmin={isAdmin}
              isUserRightManagementDialogVisible={dataflowState.isUserRightManagementDialogVisible}
              placeholder={resourcesContext.messages['manageRolesRequesterDialogInputPlaceholder']}
              representativeId={representativeId}
              roleOptions={isOpenStatus ? requesterRoleOptionsOpenStatus : requesterRoleOptions}
              saveErrorNotificationKey={'IMPOSSIBLE_REQUESTER_ROLE_ERROR'}
              setIsUserRightManagementDialogVisible={setIsUserRightManagementDialogVisible}
              setRightPermissionsChange={setRightPermissionsChange}
              updateErrorNotificationKey={'UPDATE_REQUESTER_ERROR'}
              userType="requester"
            />
          </Dialog>
        )}

        {dataflowState.isManageReportersDialogVisible && (
          <Dialog
            className="responsiveDialog"
            footer={shareRightsFooterDialogFooter(usersTypes.REPORTERS)}
            header={resourcesContext.messages['manageReportersRights']}
            onHide={() => manageDialogs('isManageReportersDialogVisible', false)}
            visible={dataflowState.isManageReportersDialogVisible}>
            <ShareRights
              addConfirmHeader={resourcesContext.messages['addReporterConfirmHeader']}
              addErrorNotificationKey={'ADD_REPORTER_ERROR'}
              columnHeader={resourcesContext.messages['reportersEmailColumn']}
              dataflowId={dataflowId}
              dataProviderId={dataProviderId}
              deleteConfirmHeader={resourcesContext.messages['reportersRightsDialogConfirmDeleteHeader']}
              deleteConfirmMessage={resourcesContext.messages['reportersRightsDialogConfirmDeleteQuestion']}
              deleteErrorNotificationKey={'DELETE_REPORTER_ERROR'}
              editConfirmHeader={resourcesContext.messages['editReporterConfirmHeader']}
              getErrorNotificationKey={'GET_REPORTERS_ERROR'}
              isUserRightManagementDialogVisible={dataflowState.isUserRightManagementDialogVisible}
              placeholder={resourcesContext.messages['manageRolesReporterDialogInputPlaceholder']}
              representativeId={representativeId}
              roleOptions={reporterRoleOptions}
              saveErrorNotificationKey={'IMPOSSIBLE_REPORTER_ROLE_ERROR'}
              setHasReporters={setHasReporters}
              setIsUserRightManagementDialogVisible={setIsUserRightManagementDialogVisible}
              setRightPermissionsChange={setRightPermissionsChange}
              updateErrorNotificationKey={'UPDATE_REPORTER_ERROR'}
              userType="reporter"
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

        {dataflowState.isValidateLeadReportersDialogVisible && (
          <ConfirmDialog
            header={resourcesContext.messages['updateUsersPermissionsDialogHeader']}
            labelCancel={resourcesContext.messages['no']}
            labelConfirm={resourcesContext.messages['yes']}
            onConfirm={onConfirmValidateLeadReporters}
            onHide={() => manageDialogs('isValidateLeadReportersDialogVisible', false)}
            visible={dataflowState.isValidateLeadReportersDialogVisible}>
            {resourcesContext.messages['updateUsersPermissionsDialogMessage']}
          </ConfirmDialog>
        )}

        {dataflowState.isValidateReportersDialogVisible && (
          <ConfirmDialog
            header={resourcesContext.messages['updateUsersPermissionsDialogHeader']}
            labelCancel={resourcesContext.messages['no']}
            labelConfirm={resourcesContext.messages['yes']}
            onConfirm={onConfirmValidateReporters}
            onHide={() => manageDialogs('isValidateReportersDialogVisible', false)}
            visible={dataflowState.isValidateReportersDialogVisible}>
            {resourcesContext.messages['updateUsersPermissionsDialogMessage']}
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

        {dataflowState.isRestrictFromPublicDialogVisible && (
          <ConfirmDialog
            confirmTooltip={resourcesContext.messages['restrictFromPublicTooltip']}
            disabledConfirm={
              dataflowState.restrictFromPublic === dataflowState.representative.restrictFromPublic ||
              dataflowState.isFetchingData
            }
            header={resourcesContext.messages['restrictFromPublicDialogHeader']}
            iconConfirm={dataflowState.isFetchingData && 'spinnerAnimate'}
            labelCancel={resourcesContext.messages['cancel']}
            labelConfirm={resourcesContext.messages['save']}
            onConfirm={onConfirmRestrictFromPublic}
            onHide={() => {
              manageDialogs('isRestrictFromPublicDialogVisible', false);
              if (dataflowState.representative.restrictFromPublic !== dataflowState.restrictFromPublic) {
                setRestrictFromPublic(dataflowState.representative.restrictFromPublic);
              }
            }}
            visible={dataflowState.isRestrictFromPublicDialogVisible}>
            <Checkbox
              checked={dataflowState.restrictFromPublic}
              disabled={!dataflowState.representative.restrictFromPublic}
              id="restrictFromPublicCheckbox"
              inputId="restrictFromPublicCheckbox"
              onChange={() => setRestrictFromPublic(!dataflowState.restrictFromPublic)}
              role="checkbox"
            />
            <label className={styles.restrictFromPublic} htmlFor="restrictFromPublicCheckbox">
              <span
                className={dataflowState.representative.restrictFromPublic ? styles.pointer : styles.disabledLabel}
                onClick={() => {
                  if (dataflowState.representative.restrictFromPublic) {
                    setRestrictFromPublic(!dataflowState.restrictFromPublic);
                  }
                }}>
                {resourcesContext.messages['restrictFromPublicCheckboxLabel']}
              </span>
            </label>
            {!dataflowState.representative.restrictFromPublic && (
              <div className={styles.restrictFromPublicNote}>
                {resourcesContext.messages['restrictFromPublicDisabledLabel']}
              </div>
            )}
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
            className="responsiveDialog"
            footer={
              ((isNil(dataProviderId) && isLeadDesigner) || (isNil(representativeId) && isObserver)) &&
              dataflowState.status === config.dataflowStatus.OPEN
                ? renderUserListDialogFooter()
                : renderDialogFooterCloseBtn('isUserListVisible')
            }
            header={
              ((isNil(dataProviderId) && isLeadDesigner) || (isNil(representativeId) && isObserver)) &&
              dataflowState.status === config.dataflowStatus.OPEN
                ? TextByDataflowTypeUtils.getLabelByDataflowType(
                    resourcesContext.messages,
                    dataflowState.dataflowType,
                    'userListDialogHeader'
                  )
                : resourcesContext.messages['dataflowUsersList']
            }
            onHide={() => manageDialogs('isUserListVisible', false)}
            visible={dataflowState.isUserListVisible}>
            <UserList
              dataflowId={dataflowId}
              dataflowType={dataflowState.dataflowType}
              representativeId={isObserver ? representativeId : dataProviderId}
            />
          </Dialog>
        )}

        <PropertiesDialog dataflowState={dataflowState} manageDialogs={manageDialogs} />

        {dataflowState.isReportingDataflowDialogVisible && (
          <ManageDataflow
            dataflowId={dataflowId}
            isCustodian={isLeadDesigner}
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
            className="responsiveDialog"
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
            dataflowId={dataflowId}
            dataProviderId={dataProviderId}
            isApiKeyDialogVisible={dataflowState.isApiKeyDialogVisible}
            isCustodian={isLeadDesigner}
            manageDialogs={manageDialogs}
          />
        )}

        {dataflowState.isDatasetsInfoDialogVisible && (
          <Dialog
            footer={renderDialogFooterCloseBtn('isDatasetsInfoDialogVisible')}
            header={`${resourcesContext.messages['datasetsInfo']} - ${resourcesContext.messages['dataflowId']}: ${dataflowState.id}`}
            onHide={() => manageDialogs('isDatasetsInfoDialogVisible', false)}
            visible={dataflowState.isDatasetsInfoDialogVisible}>
            <DatasetsInfo dataflowId={dataflowId} dataflowType={dataflowState.dataflowType} />
          </Dialog>
        )}
      </div>
    </div>
  );
};

export { Dataflow };
