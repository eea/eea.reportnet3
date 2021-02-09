import React, { useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import first from 'lodash/first';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import map from 'lodash/map';
import uniq from 'lodash/uniq';

import styles from './Dataflow.module.scss';

import { config } from 'conf';
import { DataflowDraftRequesterHelpConfig } from 'conf/help/dataflow/requester/draft';
import { DataflowReporterHelpConfig } from 'conf/help/dataflow/reporter';
import { DataflowRequesterHelpConfig } from 'conf/help/dataflow/requester';
import { routes } from 'ui/routes';
import DataflowConf from 'conf/dataflow.config.json';

import { ApiKeyDialog } from 'ui/views/_components/ApiKeyDialog';
import { BigButtonList } from './_components/BigButtonList';
import { BigButtonListRepresentative } from './_components/BigButtonListRepresentative';
import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox/';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataflowManagement } from 'ui/views/_components/DataflowManagement';
import { Dialog } from 'ui/views/_components/Dialog';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { MainLayout } from 'ui/views/_components/Layout';
import { ManageRights } from './_components/ManageRights';
import { PropertiesDialog } from './_components/PropertiesDialog';
import { RepresentativesList } from './_components/RepresentativesList';
import { ShareRights } from './_components/ShareRights';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from 'ui/views/_components/Title';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotService } from 'core/services/Snapshot';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { dataflowDataReducer } from './_functions/Reducers/dataflowDataReducer';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { getUrl } from 'core/infrastructure/CoreUtils';

const Dataflow = withRouter(({ history, match }) => {
  const {
    params: { dataflowId, representativeId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const dataflowInitialState = {
    currentUrl: '',
    data: {},
    dataProviderId: [],
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
    isManageRightsDialogVisible: false,
    isManageRolesDialogVisible: false,
    isPageLoading: true,
    isPropertiesDialogVisible: false,
    isReceiptLoading: false,
    isReceiptOutdated: false,
    isReleasable: false,
    isReleaseableDialogVisible: false,
    isReleaseDialogVisible: false,
    isShareRightsDialogVisible: false,
    isSnapshotDialogVisible: false,
    name: '',
    obligations: {},
    status: '',
    updatedDatasetSchema: undefined,
    userRoles: []
  };

  const [dataflowState, dataflowDispatch] = useReducer(dataflowDataReducer, dataflowInitialState);

  const uniqDataProviders = uniq(map(dataflowState.data.datasets, 'dataProviderId'));
  const uniqRepresentatives = uniq(map(dataflowState.data.representatives, 'dataProviderId'));

  const isInsideACountry = !isNil(representativeId) || uniqDataProviders.length === 1;
  const isLeadReporter = userContext.hasContextAccessPermission(config.permissions.DATAFLOW, dataflowState.id, [
    config.permissions.LEAD_REPORTER
  ]);

  const isLeadReporterOfCountry =
    isLeadReporter &&
    isInsideACountry &&
    ((!isNil(representativeId) && uniqRepresentatives.includes(parseInt(representativeId))) ||
      (uniqDataProviders.length === 1 && uniqRepresentatives.includes(uniqDataProviders[0])));

  const dataProviderId = isInsideACountry
    ? !isNil(representativeId)
      ? parseInt(representativeId)
      : uniqDataProviders[0]
    : null;

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

  useEffect(() => {
    if (dataflowState.isCustodian) {
      if (dataflowState.status === DataflowConf.dataflowStatus['OPEN']) {
        leftSideBarContext.addHelpSteps(DataflowDraftRequesterHelpConfig, 'dataflowRequesterDraftHelp');
      } else {
        leftSideBarContext.addHelpSteps(DataflowRequesterHelpConfig, 'dataflowRequesterDesignHelp');
      }
    } else {
      leftSideBarContext.addHelpSteps(DataflowReporterHelpConfig, 'dataflowReporterHelp');
    }
  }, [userContext, dataflowState]);

  useEffect(() => {
    if (!isEmpty(dataflowState.userRoles)) {
      const buttonsVisibility = getLeftSidebarButtonsVisibility();

      const apiKeyBtn = {
        className: 'dataflow-api-key-help-step',
        icon: 'settings',
        isVisible: buttonsVisibility.apiKeyBtn,
        label: 'sidebarApiKeyBtn',
        onClick: () => manageDialogs('isApiKeyDialogVisible', true),
        title: 'sidebarApiKeyBtn'
      };

      const editBtn = {
        className: 'dataflow-edit-help-step',
        icon: 'edit',
        isVisible: buttonsVisibility.editBtn,
        label: 'edit',
        onClick: () => manageDialogs('isEditDialogVisible', true),
        title: 'edit'
      };

      const exportSchemaBtn = {
        className: 'dataflow-export-schema-help-step',
        icon: 'download',
        isVisible: buttonsVisibility.exportBtn,
        label: 'exportSchema',
        onClick: () => manageDialogs('isExportDialogVisible', true),
        title: 'exportSchema'
      };

      const manageEditorsBtn = {
        className: 'dataflow-manage-rights-help-step',
        icon: 'userConfig',
        isVisible: buttonsVisibility.manageEditorsBtn,
        label: 'manageEditorsRights',
        onClick: () => manageDialogs('isShareRightsDialogVisible', true),
        title: 'manageEditorsRights'
      };

      const manageReportersBtn = {
        className: 'dataflow-manage-rights-help-step',
        icon: 'userConfig',
        isVisible: buttonsVisibility.manageReportersBtn,
        label: 'manageReportersRights',
        onClick: () => manageDialogs('isShareRightsDialogVisible', true),
        title: 'manageReportersRights'
      };

      const propertiesBtn = {
        className: 'dataflow-properties-help-step',
        icon: 'infoCircle',
        isVisible: buttonsVisibility.propertiesBtn,
        label: 'properties',
        onClick: () => manageDialogs('isPropertiesDialogVisible', true),
        title: 'properties'
      };

      const releaseableBtn = {
        className: 'dataflow-releasable-help-step',
        icon: 'released',
        isVisible: buttonsVisibility.releaseableBtn,
        label: 'releasingLeftSideBarButton',
        onClick: () => manageDialogs('isReleaseableDialogVisible', true),
        title: 'releasingLeftSideBarButton'
      };

      const allButtons = [
        propertiesBtn,
        editBtn,
        releaseableBtn,
        exportSchemaBtn,
        apiKeyBtn,
        manageReportersBtn,
        manageEditorsBtn
      ];

      leftSideBarContext.addModels(allButtons.filter(button => button.isVisible));
    }
  }, [
    dataflowState.userRoles,
    dataflowState.status,
    representativeId,
    dataflowState.datasetId,
    dataflowState.designDatasetSchemas.length
  ]);

  useEffect(() => {
    if (!isEmpty(dataflowState.data.representatives)) {
      const representativesNoDatasets = dataflowState.data.representatives.filter(
        representative => !representative.hasDatasets
      );
      //set for the first load
      setHasRepresentativesWithoutDatasets(!isEmpty(representativesNoDatasets));
      setFormHasRepresentatives(!isEmpty(representativesNoDatasets));
    }
  }, [dataflowState.data.representatives]);

  useEffect(() => {
    setIsPageLoading(true);
    onLoadReportingDataflow();
    onLoadSchemasValidations();
  }, [dataflowId, dataflowState.isDataUpdated, representativeId]);

  const getLeftSidebarButtonsVisibility = () => {
    const { userRoles } = dataflowState;

    const isLeadDesigner = userRoles.includes(
      config.permissions['DATA_CUSTODIAN'] || config.permissions['DATA_STEWARD']
    );

    const isDesign = dataflowState.status === DataflowConf.dataflowStatus['DESIGN'];

    if (isEmpty(dataflowState.data)) {
      return {
        apiKeyBtn: false,
        editBtn: false,
        exportBtn: false,
        releaseableBtn: false,
        manageEditorsBtn: false,
        manageReportersBtn: false,
        propertiesBtn: false
      };
    }

    return {
      apiKeyBtn: isLeadDesigner || isLeadReporterOfCountry,
      editBtn: isDesign && isLeadDesigner,
      exportBtn: isLeadDesigner && dataflowState.designDatasetSchemas.length > 0,
      releaseableBtn: !isDesign && isLeadDesigner,
      manageEditorsBtn: isDesign && isLeadDesigner,
      manageReportersBtn: isLeadReporterOfCountry,
      propertiesBtn: true
    };
  };

  const handleRedirect = target => history.push(target);

  const manageRoleDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => manageDialogs('isManageRolesDialogVisible', false)}
    />
  );

  const manageRightsDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => manageDialogs('isShareRightsDialogVisible', false)}
    />
  );

  const manageDialogs = (dialog, value, secondDialog, secondValue) =>
    dataflowDispatch({
      type: 'MANAGE_DIALOGS',
      payload: { dialog, value, secondDialog, secondValue, deleteInput: '' }
    });

  const onConfirmDeleteDataflow = event =>
    dataflowDispatch({ type: 'ON_CONFIRM_DELETE_DATAFLOW', payload: { deleteInput: event.target.value } });

  const setFormHasRepresentatives = value =>
    dataflowDispatch({ type: 'SET_FORM_HAS_REPRESENTATIVES', payload: { formHasRepresentatives: value } });

  const setHasRepresentativesWithoutDatasets = value =>
    dataflowDispatch({
      type: 'SET_HAS_REPRESENTATIVES_WITHOUT_DATASETS',
      payload: { hasRepresentativesWithoutDatasets: value }
    });

  const setIsCopyDataCollectionToEuDatasetLoading = value =>
    dataflowDispatch({
      type: 'SET_IS_COPY_DATA_COLLECTION_TO_EU_DATASET_LOADING',
      payload: { isLoading: value }
    });

  const setIsExportEuDatasetLoading = value =>
    dataflowDispatch({
      type: 'SET_IS_EXPORT_EU_DATASET',
      payload: { isExportEuDatasetLoading: value }
    });

  const setIsReleaseable = isReleasable =>
    dataflowDispatch({
      type: 'SET_IS_RELEASABLE',
      payload: { isReleasable: isReleasable }
    });

  const setIsDataUpdated = () => dataflowDispatch({ type: 'SET_IS_DATA_UPDATED' });

  const setIsPageLoading = isPageLoading =>
    dataflowDispatch({ type: 'SET_IS_PAGE_LOADING', payload: { isPageLoading } });

  const setUpdatedDatasetSchema = updatedData =>
    dataflowDispatch({ type: 'SET_UPDATED_DATASET_SCHEMA', payload: { updatedData } });

  const setIsReceiptLoading = isReceiptLoading => {
    dataflowDispatch({
      type: 'SET_IS_RECEIPT_LOADING',
      payload: { isReceiptLoading }
    });
  };

  const setIsReceiptOutdated = isReceiptOutdated => {
    dataflowDispatch({
      type: 'SET_IS_RECEIPT_OUTDATED',
      payload: { isReceiptOutdated }
    });
  };

  const onCleanUpReceipt = () => {
    dataflowDispatch({
      type: 'ON_CLEAN_UP_RECEIPT',
      payload: { isReceiptLoading: false, isReceiptOutdated: false }
    });
  };

  const onEditDataflow = (newName, newDescription) => {
    dataflowDispatch({
      type: 'ON_EDIT_DATA',
      payload: { name: newName, description: newDescription, isEditDialogVisible: false, isExportDialogVisible: false }
    });
    onLoadReportingDataflow();
  };

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
      [config.permissions.LEAD_REPORTER],
      `${config.permissions.DATAFLOW}${dataflowId}`
    );

    const entity = isNil(representativeId)
      ? `${config.permissions['DATAFLOW']}${dataflowId}`
      : `${config.permissions['DATASET']}${currentDatasetId}`;

    const userRoles = userContext.getUserRole(entity);

    const isCustodian = userRoles.includes(config.permissions['DATA_CUSTODIAN'] || config.permissions['DATA_STEWARD']);

    dataflowDispatch({ type: 'LOAD_PERMISSIONS', payload: { hasWritePermissions, isCustodian, userRoles } });
  };

  const onLoadReportingDataflow = async () => {
    try {
      const dataflow = await DataflowService.reporting(dataflowId);

      dataflowDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          data: dataflow,
          description: dataflow.description,
          isReleasable: dataflow.isReleasable,
          name: dataflow.name,
          obligations: dataflow.obligation,
          status: dataflow.status
        }
      });

      if (!isEmpty(dataflow.designDatasets)) {
        dataflow.designDatasets.forEach((schema, idx) => {
          schema.index = idx;
        });

        dataflowDispatch({ type: 'SET_DESIGN_DATASET_SCHEMAS', payload: { designDatasets: dataflow.designDatasets } });

        const datasetSchemaInfo = [];
        dataflow.designDatasets.map(schema => {
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

      if (
        !isUndefined(error.response) &&
        (error.response.status === 401 || error.response.status === 403 || error.response.status === 500)
      ) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setIsPageLoading(false);
    }
  };

  const setIsReleasingDatasetsProviderId = isReleasingDatasetValue => {
    const [notification] = notificationContext.all.filter(
      notification =>
        notification.key === 'RELEASE_FAILED_EVENT' || notification.key === 'RELEASE_BLOCKERS_FAILED_EVENT'
    );

    dataflowState.data.datasets.forEach(dataset => {
      if (dataset.dataProviderId === notification.content.providerId) {
        dataset.isReleasing = isReleasingDatasetValue;
      }
    });
  };

  useCheckNotifications(['RELEASE_COMPLETED_EVENT'], onLoadReportingDataflow);

  useCheckNotifications(
    ['RELEASE_FAILED_EVENT', 'RELEASE_BLOCKERS_FAILED_EVENT'],
    setIsReleasingDatasetsProviderId,
    false
  );

  const onLoadSchemasValidations = async () => {
    const validationResult = await DataflowService.schemasValidation(dataflowId);

    dataflowDispatch({ type: 'SET_IS_DATA_SCHEMA_CORRECT', payload: { validationResult } });
  };

  const onSaveName = async (value, index) => {
    await DatasetService.updateSchemaNameById(
      dataflowState.designDatasetSchemas[index].datasetId,
      encodeURIComponent(value)
    );
    const updatedTitles = [...dataflowState.updatedDatasetSchema];
    updatedTitles[index].schemaName = value;
    setUpdatedDatasetSchema(updatedTitles);
  };

  const onShowManageReportersDialog = () => manageDialogs('isManageRolesDialogVisible', true);

  const onOpenReleaseConfirmDialog = () => {
    manageDialogs('isReleaseDialogVisible', true);
  };

  const onConfirmExport = async () => {
    try {
      const response = await DataflowService.downloadById(dataflowId);
      if (!isNil(response)) {
        DownloadFile(
          response,
          `${dataflowState.data.name}_${new Date(Date.now()).toDateString().replace(' ', '_')}.zip`
        );
      }
    } catch (error) {
      console.error(error);
      notificationContext.add({
        type: 'EXPORT_DATASET_SCHEMA_FAILED_EVENT'
      });
    } finally {
      manageDialogs('isExportDialogVisible', false);
    }
  };

  const onConfirmRelease = async () => {
    try {
      await SnapshotService.releaseDataflow(dataflowId, dataProviderId);

      dataflowState.data.datasets
        .filter(dataset => dataset.dataProviderId === dataProviderId)
        .forEach(dataset => (dataset.isReleasing = true));
    } catch (error) {
      notificationContext.add({ type: 'RELEASE_FAILED_EVENT', content: {} });
    } finally {
      manageDialogs('isReleaseDialogVisible', false);
    }
  };

  useCheckNotifications(
    [
      'ADD_DATACOLLECTION_COMPLETED_EVENT',
      'COPY_DATASET_SCHEMA_COMPLETED_EVENT',
      'IMPORT_DATASET_SCHEMA_COMPLETED_EVENT'
    ],
    setIsDataUpdated
  );

  const onConfirmUpdateIsReleaseable = async () => {
    manageDialogs('isReleaseableDialogVisible', false);
    try {
      await DataflowService.update(
        dataflowId,
        dataflowState.data.name,
        dataflowState.data.description,
        dataflowState.obligations.obligationId,
        dataflowState.isReleasable
      );

      onLoadReportingDataflow();
    } catch (error) {
      console.error(error);
    }
  };

  const onCloseIsReleaseableDialog = () => {
    manageDialogs('isReleaseableDialogVisible', false);
    if (dataflowState.data.isReleasable !== dataflowState.isReleasable) {
      dataflowDispatch({
        type: 'SET_IS_RELEASABLE',
        payload: { isReleasable: dataflowState.data.isReleasable }
      });
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
        <Title icon="clone" iconSize="4rem" subtitle={resources.messages['dataflow']} title={dataflowState.name} />

        {isNil(representativeId) ? (
          <BigButtonList
            className="dataflow-big-buttons-help-step"
            dataflowState={dataflowState}
            dataProviderId={dataProviderId}
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
        ) : (
          <BigButtonListRepresentative
            dataflowState={dataflowState}
            dataProviderId={dataProviderId}
            handleRedirect={handleRedirect}
            isLeadReporterOfCountry={isLeadReporterOfCountry}
            match={match}
            onCleanUpReceipt={onCleanUpReceipt}
            onOpenReleaseConfirmDialog={onOpenReleaseConfirmDialog}
            setIsReceiptLoading={setIsReceiptLoading}
          />
        )}

        {dataflowState.isReleaseDialogVisible && (
          <ConfirmDialog
            header={resources.messages['confirmReleaseHeader']}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={() => onConfirmRelease()}
            onHide={() => manageDialogs('isReleaseDialogVisible', false)}
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
                dataflowRepresentatives={dataflowState.data.representatives}
                dataflowId={dataflowId}
                isActiveManageRolesDialog={dataflowState.isManageRolesDialogVisible}
                setHasRepresentativesWithoutDatasets={setHasRepresentativesWithoutDatasets}
                setFormHasRepresentatives={setFormHasRepresentatives}
              />
            </div>
          </Dialog>
        )}

        {dataflowState.isManageRightsDialogVisible && (
          <Dialog
            contentStyle={{ maxHeight: '60vh' }}
            footer={manageRightsDialogFooter}
            header={
              dataflowState.isCustodian
                ? resources.messages['manageEditorsRights']
                : resources.messages['manageReportersRights']
            }
            onHide={() => manageDialogs('isManageRightsDialogVisible', false)}
            visible={dataflowState.isManageRightsDialogVisible}>
            <div className={styles.dialog}>
              <ManageRights
                dataflowId={dataflowId}
                dataflowState={dataflowState}
                dataProviderId={dataProviderId}
                isActiveManageRightsDialog={dataflowState.isManageRightsDialogVisible}
              />
            </div>
          </Dialog>
        )}

        {dataflowState.isShareRightsDialogVisible && (
          <Dialog
            footer={manageRightsDialogFooter}
            header={
              dataflowState.isCustodian
                ? resources.messages['manageEditorsRights']
                : resources.messages['manageReportersRights']
            }
            onHide={() => manageDialogs('isShareRightsDialogVisible', false)}
            visible={dataflowState.isShareRightsDialogVisible}>
            <ShareRights
              dataflowId={dataflowId}
              dataProviderId={dataProviderId}
              isCustodian={dataflowState.isCustodian}
              representativeId={representativeId}
            />
          </Dialog>
        )}

        {dataflowState.isExportDialogVisible && (
          <ConfirmDialog
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
            disabledConfirm={dataflowState.data.isReleasable === dataflowState.isReleasable}
            header={resources.messages['isReleasableDataflowDialogHeader']}
            labelCancel={resources.messages['cancel']}
            labelConfirm={resources.messages['save']}
            onConfirm={onConfirmUpdateIsReleaseable}
            onHide={() => onCloseIsReleaseableDialog()}
            visible={dataflowState.isReleaseableDialogVisible}>
            <Checkbox
              id="isReleaseableCheckbox"
              inputId="isReleaseableCheckbox"
              isChecked={dataflowState.isReleasable}
              onChange={() => setIsReleaseable(!dataflowState.isReleasable)}
              role="checkbox"
            />
            <label htmlFor="isReleaseableCheckbox" className={styles.isReleaseableLabel}>
              <a onClick={() => setIsReleaseable(!dataflowState.isReleasable)}>
                {resources.messages['isReleasableDataflowCheckboxLabel']}
              </a>
            </label>
          </ConfirmDialog>
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
            dataflowId={dataflowId}
            dataProviderId={dataProviderId}
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
