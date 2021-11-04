import { Fragment, useContext, useEffect, useRef, useState } from 'react';

import dayjs from 'dayjs';
import isNil from 'lodash/isNil';
import remove from 'lodash/remove';

import { config } from 'conf';
import { DataflowConfig } from 'repositories/config/DataflowConfig';

import styles from './BigButtonList.module.scss';

import { BigButton } from 'views/_components/BigButton';
import { Button } from 'views/_components/Button';
import { Calendar } from 'views/_components/Calendar/Calendar';
import { Checkbox } from 'views/_components/Checkbox';
import { CloneSchemas } from 'views/Dataflow/_components/CloneSchemas';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { Dialog } from 'views/_components/Dialog';
import { DownloadFile } from 'views/_components/DownloadFile';
import { HistoricReleases } from 'views/Dataflow/_components/HistoricReleases';
import { ManageManualAcceptanceDataset } from 'views/Dataflow/_components/ManageManualAcceptanceDataset';
import { ManualAcceptanceDatasets } from 'views/Dataflow/_components/ManualAcceptanceDatasets';
import { NewDatasetSchemaForm } from 'views/_components/NewDatasetSchemaForm';
import { TooltipButton } from 'views/_components/TooltipButton';

import { ConfirmationReceiptService } from 'services/ConfirmationReceiptService';
import { DataCollectionService } from 'services/DataCollectionService';
import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';
import { EUDatasetService } from 'services/EUDatasetService';
import { IntegrationService } from 'services/IntegrationService';
import { ManageIntegrations } from 'views/_components/ManageIntegrations/ManageIntegrations';

import { LoadingContext } from 'views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { RadioButton } from 'views/_components/RadioButton';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { useBigButtonList } from './_functions/Hooks/useBigButtonList';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { IntegrationsUtils } from 'views/DatasetDesigner/_components/Integrations/_functions/Utils/IntegrationsUtils';
import { MetadataUtils } from 'views/_functions/Utils';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const BigButtonList = ({
  dataflowState,
  dataflowType,
  dataProviderId,
  handleRedirect,
  isLeadReporterOfCountry,
  manageDialogs,
  onCleanUpReceipt,
  onOpenReleaseConfirmDialog,
  onSaveName,
  onShowManageReportersDialog,
  onUpdateData,
  setIsCopyDataCollectionToEUDatasetLoading,
  setIsExportEUDatasetLoading,
  setIsReceiptLoading,
  setUpdatedDatasetSchema
}) => {
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [errorDialogData, setErrorDialogData] = useState({ isVisible: false, message: '' });

  const [cloneDataflow, setCloneDataflow] = useState({});
  const [cloneDialogVisible, setCloneDialogVisible] = useState(false);
  const [dataCollectionDialog, setDataCollectionDialog] = useState(false);
  const [dataCollectionDueDate, setDataCollectionDueDate] = useState(null);
  const [datasetFeedbackStatusToEdit, setDatasetFeedbackStatusToEdit] = useState({});
  const [datasetId, setDatasetId] = useState(null);
  const [datasetSchemaId, setDatasetSchemaId] = useState(null);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [deleteSchemaIndex, setDeleteSchemaIndex] = useState();
  const [exportEUDatasetIntegration, setExportEUDatasetIntegration] = useState({});
  const [historicReleasesDialogHeader, setHistoricReleasesDialogHeader] = useState([]);
  const [historicReleasesView, setHistoricReleasesView] = useState('');
  const [isActiveButton, setIsActiveButton] = useState(true);
  const [isCloningDataflow, setIsCloningDataflow] = useState(false);
  const [isConfirmCollectionDialog, setIsConfirmCollectionDialog] = useState(false);
  const [isCopyDataCollectionToEUDatasetDialogVisible, setIsCopyDataCollectionToEUDatasetDialogVisible] =
    useState(false);
  const [isExportEUDatasetDialogVisible, setIsExportEUDatasetDialogVisible] = useState(false);
  const [isHistoricReleasesDialogVisible, setIsHistoricReleasesDialogVisible] = useState(false);
  const [isImportingDataflow, setIsImportingDataflow] = useState(false);
  const [isIntegrationManageDialogVisible, setIsIntegrationManageDialogVisible] = useState(false);
  const [isManageManualAcceptanceDatasetDialogVisible, setIsManageManualAcceptanceDatasetDialogVisible] =
    useState(false);
  const [isManualTechnicalAcceptance, setIsManualTechnicalAcceptance] = useState(null);
  const [isManualTechnicalAcceptanceDialogVisible, setIsManualTechnicalAcceptanceDialogVisible] = useState(false);
  const [isUpdatedManualAcceptanceDatasets, setIsUpdatedManualAcceptanceDatasets] = useState(false);
  const [isUpdateDataCollectionDialogVisible, setIsUpdateDataCollectionDialogVisible] = useState(false);
  const [manualTechnicalAcceptanceOptions, setManualTechnicalAcceptanceOptions] = useState({
    Yes: false,
    No: false
  });
  const [newDatasetDialog, setNewDatasetDialog] = useState(false);
  const [isQCsNotValidWarningVisible, setIsQCsNotValidWarningVisible] = useState(false);
  const [isImportSchemaVisible, setIsImportSchemaVisible] = useState(false);
  const [invalidAndDisabledRulesAmount, setInvalidAndDisabledRulesAmount] = useState({
    invalidRules: 0,
    disabledRules: 0
  });

  const [providerId, setProviderId] = useState(null);
  const [showPublicInfo, setShowPublicInfo] = useState(true);
  const hasExpirationDate = new Date(dataflowState.obligations?.expirationDate) > new Date();
  const receiptBtnRef = useRef(null);

  const dataflowId = dataflowState.id;
  const dataflowName = dataflowState.name;
  const dataflowData = dataflowState.data;
  const isBusinessDataflow = dataflowType === config.dataflowType.BUSINESS.value;

  useCheckNotifications(['ADD_DATACOLLECTION_FAILED_EVENT'], setIsActiveButton, true);
  useCheckNotifications(['UPDATE_DATACOLLECTION_COMPLETED_EVENT'], onUpdateData);
  useCheckNotifications(['UPDATE_DATACOLLECTION_FAILED_EVENT'], setIsActiveButton, true);
  useCheckNotifications(
    ['COPY_DATA_TO_EUDATASET_COMPLETED_EVENT', 'COPY_DATA_TO_EUDATASET_FAILED_EVENT'],
    setIsCopyDataCollectionToEUDatasetLoading,
    false
  );
  useCheckNotifications(
    ['EXTERNAL_EXPORT_EUDATASET_COMPLETED_EVENT', 'EXTERNAL_EXPORT_EUDATASET_FAILED_EVENT'],
    setIsExportEUDatasetLoading,
    false
  );
  useCheckNotifications(
    ['COPY_DATASET_SCHEMA_COMPLETED_EVENT', 'COPY_DATASET_SCHEMA_FAILED_EVENT', 'COPY_DATASET_SCHEMA_NOT_FOUND_EVENT'],
    setIsCloningDataflow,
    false
  );

  useCheckNotifications(
    ['IMPORT_DATASET_SCHEMA_COMPLETED_EVENT', 'IMPORT_DATASET_SCHEMA_FAILED_EVENT'],
    setIsImportingDataflow,
    false
  );

  useEffect(() => {
    const response = notificationContext.toShow.find(notification => notification.key === 'LOAD_RECEIPT_DATA_ERROR');

    if (response) {
      setIsReceiptLoading(false);
    }
  }, [notificationContext]);

  useEffect(() => {
    getExpirationDate();
  }, [dataflowState.obligations?.expirationDate]);

  const checkShowPublicInfo = (
    <div style={{ float: 'left' }}>
      <Checkbox
        ariaLabelledBy="show_public_info_label"
        checked={showPublicInfo}
        id="show_public_info_checkbox"
        inputId="show_public_info_checkbox"
        onChange={e => setShowPublicInfo(e.checked)}
        role="checkbox"
      />
      <label
        id="show_public_info_label"
        onClick={() => setShowPublicInfo(!showPublicInfo)}
        style={{ cursor: 'pointer', fontWeight: 'bold', marginLeft: '3px' }}>
        {resourcesContext.messages['showPublicInfo']}
      </label>

      {isBusinessDataflow && (
        <TooltipButton
          message={resourcesContext.messages['showPublicBusinessInfoCheckboxTooltip']}
          uniqueIdentifier={'show_public_info_label'}
        />
      )}
    </div>
  );

  const cloneDatasetSchemas = async () => {
    setCloneDialogVisible(false);

    notificationContext.add(
      {
        type: 'CLONE_DATASET_SCHEMAS_INIT',
        content: { customContent: { sourceDataflowName: cloneDataflow.name, targetDataflowName: dataflowName } }
      },
      true
    );
    setIsCloningDataflow(true);

    try {
      await DataflowService.cloneSchemas(cloneDataflow.id, dataflowId);
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('BigButtonList - cloneDatasetSchemas.', error);
        notificationContext.add({ type: 'CLONE_NEW_SCHEMA_ERROR' }, true);
      }
    }
  };

  const downloadPdf = response => {
    if (!isNil(response)) {
      DownloadFile(response, `${dataflowState.data.name}_${Date.now()}.pdf`);
    }
  };

  const errorDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button icon="check" label={resourcesContext.messages['ok']} onClick={() => onHideErrorDialog()} />
    </div>
  );

  const getCloneDataflow = value => setCloneDataflow(value);

  const getDatasetData = (datasetId, datasetSchemaId) => {
    setDatasetSchemaId(datasetSchemaId);
    setDatasetId(datasetId);
  };

  const getDataHistoricReleases = (datasetId, headerTitle) => {
    setDatasetId(datasetId);
    setHistoricReleasesDialogHeader(headerTitle);
    setProviderId(null);
  };

  const getDataHistoricReleasesByRepresentatives = (headerTitle, providerId) => {
    setDatasetId(null);
    setHistoricReleasesDialogHeader(headerTitle);
    setProviderId(providerId);
  };

  const getDeleteSchemaIndex = index => {
    setDeleteSchemaIndex(index);
    setDeleteDialogVisible(true);
  };

  const getExpirationDate = () => {
    setDataCollectionDueDate(
      !isNil(dataflowState.obligations?.expirationDate) &&
        new Date(dataflowState.obligations.expirationDate) > new Date()
        ? new Date(dataflowState.obligations.expirationDate)
        : null
    );
  };

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      console.error('BigButtonList - getMetadata.', error);
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId } }, true);
    }
  };

  const handleExportEUDataset = value => setIsIntegrationManageDialogVisible(value);

  const manageManualAcceptanceDatasetDialog = value => setIsManageManualAcceptanceDatasetDialogVisible(value);

  const onCloneDataflow = async () => {
    setCloneDialogVisible(true);
  };

  const onCreateDatasetSchema = () => setNewDatasetDialog(false);

  const onCreateDataCollections = async () => {
    setIsConfirmCollectionDialog(false);

    notificationContext.add({ type: 'CREATE_DATA_COLLECTION_INIT', content: {} });

    setIsActiveButton(false);

    try {
      return await DataCollectionService.create(
        dataflowId,
        getDate(),
        isManualTechnicalAcceptance,
        true,
        showPublicInfo
      );
    } catch (error) {
      console.error('BigButtonList - onCreateDataCollections.', error);
      const {
        dataflow: { name: dataflowName }
      } = await getMetadata({ dataflowId });

      notificationContext.add({ type: 'CREATE_DATA_COLLECTION_ERROR', content: { dataflowId, dataflowName } }, true);
      setIsActiveButton(true);
    } finally {
      setDataCollectionDialog(false);
    }
  };

  const onImportSchema = () => {
    setIsImportSchemaVisible(true);
  };

  const onImportSchemaError = async ({ xhr }) => {
    if (xhr.status === 423) {
      notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
    } else {
      notificationContext.add({ type: 'IMPORT_DATASET_SCHEMA_FAILED_EVENT' }, true);
    }
  };

  const onShowManualTechnicalAcceptanceDialog = () => setIsManualTechnicalAcceptanceDialogVisible(true);

  useEffect(() => {
    const response = notificationContext.hidden.find(notification => notification.key === 'DISABLE_RULES_ERROR_EVENT');
    if (response) {
      const {
        content: { invalidRules, disabledRules }
      } = response;
      setInvalidAndDisabledRulesAmount({ invalidRules, disabledRules });
      setIsQCsNotValidWarningVisible(true);
      setIsActiveButton(true);
    }
  }, [notificationContext]);

  const onShowHistoricReleases = typeView => {
    setIsHistoricReleasesDialogVisible(true);
    setHistoricReleasesView(typeView);
  };

  const onUpload = async () => {
    setIsImportSchemaVisible(false);
    setIsImportingDataflow(true);
    notificationContext.add({
      type: 'IMPORT_DATASET_SCHEMA_INIT',
      content: { dataflowName }
    });
  };

  const onLoadEUDatasetIntegration = async datasetSchemaId => {
    try {
      const euDatasetExportIntegration = await IntegrationService.getEUDatasetIntegration(dataflowId, datasetSchemaId);
      setExportEUDatasetIntegration(IntegrationsUtils.parseIntegration(euDatasetExportIntegration));
    } catch (error) {
      console.error('BigButtonList - onLoadEUDatasetIntegration.', error);
      notificationContext.add({ type: 'LOAD_INTEGRATIONS_ERROR' }, true);
    }
  };

  const onUpdateDataCollection = async () => {
    setIsUpdateDataCollectionDialogVisible(false);

    setIsActiveButton(false);

    try {
      return await DataCollectionService.update(dataflowId);
    } catch (error) {
      console.error('BigButtonList - onUpdateDataCollection.', error);
    }
  };

  const onDeleteDatasetSchema = async index => {
    setDeleteDialogVisible(false);

    showLoading();
    try {
      await DatasetService.deleteSchema(dataflowState.designDatasetSchemas[index].datasetId);
      onUpdateData();
      setUpdatedDatasetSchema(remove(dataflowState.updatedDatasetSchema, event => event.schemaIndex !== index));
    } catch (error) {
      console.error('BigButtonList - onDeleteDatasetSchema.', error);
      if (error.response.status === 401) {
        notificationContext.add({ type: 'DELETE_DATASET_SCHEMA_LINK_ERROR' }, true);
      }
    } finally {
      hideLoading();
    }
  };

  const onHideErrorDialog = () => {
    setErrorDialogData({ isVisible: false, message: '' });
  };

  const onCopyDataCollectionToEUDataset = async () => {
    setIsCopyDataCollectionToEUDatasetDialogVisible(false);
    setIsCopyDataCollectionToEUDatasetLoading(true);

    try {
      await EUDatasetService.copyFromDataCollection(dataflowId);
      notificationContext.add({ type: 'COPY_TO_EU_DATASET_INIT' });
    } catch (error) {
      setIsCopyDataCollectionToEUDatasetLoading(false);

      if (error.response.status === 423) {
        notificationContext.add({ type: 'DATA_COLLECTION_LOCKED_ERROR' }, true);
      } else {
        console.error('BigButtonList - onCopyDataCollectionToEUDataset.', error);
        notificationContext.add({ type: 'COPY_DATA_COLLECTION_EU_DATASET_ERROR' }, true);
      }
    }
  };

  const onExportEUDataset = async () => {
    setIsExportEUDatasetDialogVisible(false);
    setIsExportEUDatasetLoading(true);

    try {
      await EUDatasetService.export(dataflowId);
      notificationContext.add({ type: 'EXPORT_EU_DATASET_INIT' });
    } catch (error) {
      setIsExportEUDatasetLoading(false);

      if (error.response.status === 423) {
        notificationContext.add({ type: 'EU_DATASET_LOCKED_ERROR' }, true);
      } else {
        console.error('BigButtonList - onExportEUDataset.', error);
        notificationContext.add({ type: 'EXPORT_EU_DATASET_ERROR' }, true);
      }
    }
  };

  const onLoadReceiptData = async () => {
    try {
      setIsReceiptLoading(true);
      const response = await ConfirmationReceiptService.download(dataflowId, dataProviderId);

      downloadPdf(response.data);
      onCleanUpReceipt();
    } catch (error) {
      console.error('BigButtonList - onLoadReceiptData.', error);
      notificationContext.add({ type: 'LOAD_RECEIPT_DATA_ERROR' }, true);
    } finally {
      setIsReceiptLoading(false);
    }
  };

  const onShowCopyDataCollectionToEUDatasetModal = () => setIsCopyDataCollectionToEUDatasetDialogVisible(true);

  const onShowDataCollectionModal = () => setDataCollectionDialog(true);

  const onShowExportEUDatasetModal = () => setIsExportEUDatasetDialogVisible(true);

  const onShowNewSchemaDialog = () => setNewDatasetDialog(true);

  const onShowUpdateDataCollectionModal = () => setIsUpdateDataCollectionDialogVisible(true);

  const getDate = () => {
    return new Date(dayjs(dataCollectionDueDate).endOf('day').format()).getTime();
  };

  const onCreateDataCollectionsWithNotValids = async () => {
    setIsActiveButton(false);
    try {
      notificationContext.removeHiddenByKey('DISABLE_RULES_ERROR_EVENT');
      await DataCollectionService.create(dataflowId, getDate(), isManualTechnicalAcceptance, false);
    } catch (error) {
      console.error('BigButtonList - onCreateDataCollectionsWithNotValids.', error);
      const {
        dataflow: { name: dataflowName }
      } = await getMetadata({ dataflowId });

      notificationContext.add({ type: 'CREATE_DATA_COLLECTION_ERROR', content: { dataflowId, dataflowName } }, true);

      setIsActiveButton(true);
    } finally {
      setIsQCsNotValidWarningVisible(false);
    }
  };

  const renderDialogFooter =
    isHistoricReleasesDialogVisible || isManualTechnicalAcceptanceDialogVisible ? (
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={'cancel'}
        label={resourcesContext.messages['close']}
        onClick={() => {
          setIsHistoricReleasesDialogVisible(false);
          setIsManualTechnicalAcceptanceDialogVisible(false);
        }}
      />
    ) : (
      <Fragment>
        <Button
          className="p-button-primary p-button-animated-blink"
          disabled={isNil(cloneDataflow.id)}
          icon={'plus'}
          label={resourcesContext.messages['cloneSelectedDataflow']}
          onClick={() => cloneDatasetSchemas()}
        />
        <Button
          className="p-button-secondary p-button-animated-blink p-button-right-aligned"
          icon={'cancel'}
          label={resourcesContext.messages['close']}
          onClick={() => setCloneDialogVisible(false)}
        />
      </Fragment>
    );

  const onResetRadioButtonOptions = () => {
    setIsManualTechnicalAcceptance(null);
    setManualTechnicalAcceptanceOptions({
      Yes: false,
      No: false
    });
  };

  const onChangeRadioButton = value => {
    const options = { ...manualTechnicalAcceptanceOptions };
    Object.keys(options).forEach(option => {
      options[option] = false;
      options[value] = true;
    });

    setIsManualTechnicalAcceptance(value.toString() === 'Yes');
    setManualTechnicalAcceptanceOptions(options);
  };

  const refreshManualAcceptanceDatasets = value => setIsUpdatedManualAcceptanceDatasets(value);

  const renderRadioButtonsCreateDC = () => {
    return Object.keys(manualTechnicalAcceptanceOptions).map(value => (
      <div className={styles.radioButton} key={`technicalAcceptance${value}`}>
        <RadioButton
          checked={manualTechnicalAcceptanceOptions[value]}
          className={styles.button}
          inputId={`technicalAcceptance${value}`}
          onChange={event => onChangeRadioButton(event.target.value)}
          value={value}
        />
        <label className={styles.label} htmlFor={`technicalAcceptance${value}`}>
          {value}
        </label>
      </div>
    ));
  };

  const bigButtonList = useBigButtonList({
    dataflowId,
    dataflowState,
    dataProviderId,
    getDataHistoricReleases,
    getDataHistoricReleasesByRepresentatives,
    getDatasetData,
    getDeleteSchemaIndex,
    handleExportEUDataset,
    handleRedirect,
    isActiveButton,
    isCloningDataflow,
    isImportingDataflow,
    isLeadReporterOfCountry,
    onCloneDataflow,
    onImportSchema,
    onLoadEUDatasetIntegration,
    onLoadReceiptData,
    onOpenReleaseConfirmDialog,
    onSaveName,
    onShowCopyDataCollectionToEUDatasetModal,
    onShowDataCollectionModal,
    onShowExportEUDatasetModal,
    onShowHistoricReleases,
    onShowManageReportersDialog,
    onShowManualTechnicalAcceptanceDialog,
    onShowNewSchemaDialog,
    onShowUpdateDataCollectionModal,
    setErrorDialogData
  })
    .filter(button => button.visibility)
    .map(button => <BigButton key={button.caption} manageDialogs={manageDialogs} {...button} />);

  const getManageAcceptanceDataset = data => setDatasetFeedbackStatusToEdit(data);

  return (
    <Fragment>
      <div className={styles.buttonsWrapper}>
        <div className={`${styles.splitButtonWrapper} dataflow-big-buttons-help-step`}>
          <div className={styles.datasetItem}>{bigButtonList}</div>
        </div>
      </div>

      {isIntegrationManageDialogVisible && (
        <ManageIntegrations
          dataflowId={dataflowId}
          datasetId={datasetId}
          datasetType={'dataflow'}
          manageDialogs={handleExportEUDataset}
          state={{ datasetSchemaId, isIntegrationManageDialogVisible }}
          updatedData={exportEUDatasetIntegration}
        />
      )}

      {newDatasetDialog && (
        <Dialog
          className={styles.dialog}
          header={resourcesContext.messages['newDatasetSchema']}
          onHide={() => setNewDatasetDialog(false)}
          visible={newDatasetDialog}>
          <NewDatasetSchemaForm
            dataflowId={dataflowId}
            datasetSchemaInfo={dataflowState.updatedDatasetSchema}
            onCreate={onCreateDatasetSchema}
            onUpdateData={onUpdateData}
            setNewDatasetDialog={setNewDatasetDialog}
          />
        </Dialog>
      )}

      {cloneDialogVisible && (
        <Dialog
          className={styles.dialog}
          footer={renderDialogFooter}
          header={resourcesContext.messages['dataflowsList']}
          onHide={() => setCloneDialogVisible(false)}
          style={{ width: '95%' }}
          visible={cloneDialogVisible}>
          <CloneSchemas dataflowId={dataflowId} getCloneDataflow={getCloneDataflow} />
        </Dialog>
      )}

      {errorDialogData.isVisible && (
        <Dialog
          footer={errorDialogFooter}
          header={resourcesContext.messages['error'].toUpperCase()}
          onHide={onHideErrorDialog}
          visible={errorDialogData.isVisible}>
          <div className="p-grid p-fluid">{errorDialogData.message}</div>
        </Dialog>
      )}

      {deleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resourcesContext.messages['delete'].toUpperCase()}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => onDeleteDatasetSchema(deleteSchemaIndex)}
          onHide={() => setDeleteDialogVisible(false)}
          visible={deleteDialogVisible}>
          {resourcesContext.messages['deleteDatasetSchema']}
        </ConfirmDialog>
      )}

      {isHistoricReleasesDialogVisible && (
        <Dialog
          className={styles.dialog}
          footer={renderDialogFooter}
          header={`${resourcesContext.messages['historicReleasesContextMenu']} ${historicReleasesDialogHeader}`}
          onHide={() => setIsHistoricReleasesDialogVisible(false)}
          visible={isHistoricReleasesDialogVisible}>
          <HistoricReleases
            dataProviderId={providerId}
            dataflowId={dataflowId}
            dataflowType={dataflowType}
            datasetId={datasetId}
            historicReleasesView={historicReleasesView}
          />
        </Dialog>
      )}

      {isManualTechnicalAcceptanceDialogVisible && (
        <Dialog
          className={styles.dialog}
          footer={renderDialogFooter}
          header={`${resourcesContext.messages['manualTechnicalAcceptanceHeader']} ${dataflowName}`}
          onHide={() => setIsManualTechnicalAcceptanceDialogVisible(false)}
          style={{ width: '80%' }}
          visible={isManualTechnicalAcceptanceDialogVisible}>
          <ManualAcceptanceDatasets
            dataflowId={dataflowData.id}
            dataflowType={dataflowType}
            getManageAcceptanceDataset={getManageAcceptanceDataset}
            isUpdatedManualAcceptanceDatasets={isUpdatedManualAcceptanceDatasets}
            manageDialogs={manageManualAcceptanceDatasetDialog}
            refreshManualAcceptanceDatasets={refreshManualAcceptanceDatasets}
          />
        </Dialog>
      )}

      {isManageManualAcceptanceDatasetDialogVisible && (
        <ManageManualAcceptanceDataset
          dataflowId={dataflowId}
          dataset={datasetFeedbackStatusToEdit}
          isManageManualAcceptanceDatasetDialogVisible={isManageManualAcceptanceDatasetDialogVisible}
          manageDialogs={manageManualAcceptanceDatasetDialog}
          refreshManualAcceptanceDatasets={refreshManualAcceptanceDatasets}
        />
      )}

      {isUpdateDataCollectionDialogVisible && (
        <ConfirmDialog
          header={resourcesContext.messages['updateDataCollectionHeader']}
          labelCancel={resourcesContext.messages['close']}
          labelConfirm={resourcesContext.messages['create']}
          onConfirm={() => onUpdateDataCollection()}
          onHide={() => setIsUpdateDataCollectionDialogVisible(false)}
          visible={isUpdateDataCollectionDialogVisible}>
          <p>{resourcesContext.messages['updateDataCollectionMessage']}</p>
        </ConfirmDialog>
      )}

      {dataCollectionDialog && (
        <ConfirmDialog
          className={styles.calendarConfirm}
          disabledConfirm={isNil(dataCollectionDueDate)}
          footerAddon={checkShowPublicInfo}
          header={resourcesContext.messages['createDataCollection']}
          labelCancel={resourcesContext.messages['close']}
          labelConfirm={resourcesContext.messages['create']}
          onConfirm={() => setIsConfirmCollectionDialog(true)}
          onHide={() => setDataCollectionDialog(false)}
          visible={dataCollectionDialog}>
          {hasExpirationDate ? (
            <p
              dangerouslySetInnerHTML={{
                __html: TextUtils.parseText(resourcesContext.messages['dataCollectionExpirationDate'], {
                  expirationData: dayjs(dataflowState.obligations.expirationDate).format(
                    userContext.userProps.dateFormat
                  )
                })
              }}></p>
          ) : (
            <p className={styles.dataCollectionDialogMessagesWrapper}>
              <span>{`${resourcesContext.messages['chooseExpirationDate']}`}</span>
              <span>{`${resourcesContext.messages['chooseExpirationDateSecondLine']}`}</span>
            </p>
          )}
          <Calendar
            className={styles.calendar}
            disabledDates={[new Date()]}
            inline={true}
            minDate={new Date()}
            monthNavigator={true}
            onChange={event => setDataCollectionDueDate(event.target.value)}
            showWeek={true}
            value={dataCollectionDueDate}
            yearNavigator={true}
            yearRange="2020:2030"
          />
        </ConfirmDialog>
      )}

      {isCopyDataCollectionToEUDatasetDialogVisible && (
        <ConfirmDialog
          header={resourcesContext.messages['copyDataCollectionToEUDatasetHeader']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => onCopyDataCollectionToEUDataset()}
          onHide={() => setIsCopyDataCollectionToEUDatasetDialogVisible(false)}
          visible={isCopyDataCollectionToEUDatasetDialogVisible}>
          <p>{resourcesContext.messages['copyDataCollectionToEUDatasetMessage']}</p>
        </ConfirmDialog>
      )}

      {isExportEUDatasetDialogVisible && (
        <ConfirmDialog
          header={resourcesContext.messages['exportEUDatasetHeader']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => onExportEUDataset()}
          onHide={() => setIsExportEUDatasetDialogVisible(false)}
          visible={isExportEUDatasetDialogVisible}>
          <p>{resourcesContext.messages['exportEUDatasetMessage']}</p>
        </ConfirmDialog>
      )}

      {isConfirmCollectionDialog && (
        <ConfirmDialog
          disabledConfirm={isNil(isManualTechnicalAcceptance)}
          header={resourcesContext.messages['createDataCollection']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => onCreateDataCollections()}
          onHide={() => {
            setIsConfirmCollectionDialog(false);
            onResetRadioButtonOptions();
          }}
          visible={isConfirmCollectionDialog}>
          <div>{resourcesContext.messages['createDataCollectionConfirmQuestion']}</div>
          <div>{resourcesContext.messages['createDataCollectionConfirm']}</div>
          <div className={styles.radioButtonDiv}>
            <label>{resourcesContext.messages['manualTechnicalAcceptanceTitle']}</label>
            {renderRadioButtonsCreateDC()}
          </div>
        </ConfirmDialog>
      )}

      {isQCsNotValidWarningVisible && (
        <ConfirmDialog
          header={resourcesContext.messages['notValidQCWarningTitle']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => onCreateDataCollectionsWithNotValids()}
          onHide={() => {
            notificationContext.removeHiddenByKey('DISABLE_RULES_ERROR_EVENT');
            setIsQCsNotValidWarningVisible(false);
          }}
          visible={isQCsNotValidWarningVisible}>
          {TextUtils.parseText(resourcesContext.messages['notValidQCWarningBody'], {
            disabled: invalidAndDisabledRulesAmount.disabledRules,
            invalid: invalidAndDisabledRulesAmount.invalidRules
          })}
        </ConfirmDialog>
      )}

      {isImportSchemaVisible && (
        <CustomFileUpload
          accept=".zip"
          chooseLabel={resourcesContext.messages['selectFile']}
          className={styles.FileUpload}
          dialogHeader={`${resourcesContext.messages['importSchema']}`}
          dialogOnHide={() => setIsImportSchemaVisible(false)} //allowTypes="/(\.|\/)(csv)$/"
          dialogVisible={isImportSchemaVisible}
          infoTooltip={`${resourcesContext.messages['supportedFileExtensionsTooltip']} .zip`}
          invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
          isDialog={true}
          name="file"
          onError={onImportSchemaError}
          onUpload={onUpload}
          url={`${window.env.REACT_APP_BACKEND}${getUrl(DataflowConfig.importSchema, { dataflowId })}`}
        />
      )}

      <button
        className="dataflow-big-buttons-confirmation-receipt-help-step"
        ref={receiptBtnRef}
        style={{ display: 'none' }}>
        <span className="srOnly">{resourcesContext.messages['confirmationReceipt']}</span>
      </button>
    </Fragment>
  );
};
