import { Fragment, useContext, useEffect, useRef, useState } from 'react';

import isNil from 'lodash/isNil';
import dayjs from 'dayjs';
import remove from 'lodash/remove';

import { DataflowConfig } from 'conf/domain/model/Dataflow';

import styles from './BigButtonList.module.scss';

import { BigButton } from 'ui/views/_components/BigButton';
import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar/Calendar';
import { Checkbox } from 'ui/views/_components/Checkbox';
import { CloneSchemas } from 'ui/views/Dataflow/_components/CloneSchemas';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { Dialog } from 'ui/views/_components/Dialog';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { HistoricReleases } from 'ui/views/Dataflow/_components/HistoricReleases';
import { ManageManualAcceptanceDataset } from 'ui/views/Dataflow/_components/ManageManualAcceptanceDataset';
import { ManualAcceptanceDatasets } from 'ui/views/Dataflow/_components/ManualAcceptanceDatasets';
import { NewDatasetSchemaForm } from 'ui/views/_components/NewDatasetSchemaForm';

import { ConfirmationReceiptService } from 'core/services/ConfirmationReceipt';
import { DataCollectionService } from 'core/services/DataCollection';
import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { EuDatasetService } from 'core/services/EuDataset';
import { IntegrationService } from 'core/services/Integration';
import { ManageIntegrations } from 'ui/views/_components/ManageIntegrations/ManageIntegrations';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { RadioButton } from 'ui/views/_components/RadioButton';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { useBigButtonList } from './_functions/Hooks/useBigButtonList';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { IntegrationsUtils } from 'ui/views/DatasetDesigner/_components/Integrations/_functions/Utils/IntegrationsUtils';
import { MetadataUtils } from 'ui/views/_functions/Utils';
import { TextUtils } from 'ui/views/_functions/Utils';

export const BigButtonList = ({
  dataflowState,
  dataProviderId,
  handleRedirect,
  isLeadReporterOfCountry,
  onCleanUpReceipt,
  onSaveName,
  onShowManageReportersDialog,
  onOpenReleaseConfirmDialog,
  onUpdateData,
  setIsCopyDataCollectionToEuDatasetLoading,
  setIsExportEuDatasetLoading,
  setIsReceiptLoading,
  setUpdatedDatasetSchema
}) => {
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
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
  const [euDatasetExportIntegration, setEuDatasetExportIntegration] = useState({});
  const [historicReleasesDialogHeader, setHistoricReleasesDialogHeader] = useState([]);
  const [historicReleasesView, setHistoricReleasesView] = useState('');
  const [isActiveButton, setIsActiveButton] = useState(true);
  const [isCloningDataflow, setIsCloningDataflow] = useState(false);
  const [isConfirmCollectionDialog, setIsConfirmCollectionDialog] = useState(false);
  const [isCopyDataCollectionToEuDatasetDialogVisible, setIsCopyDataCollectionToEuDatasetDialogVisible] = useState(
    false
  );
  const [isExportEuDatasetDialogVisible, setIsExportEuDatasetDialogVisible] = useState(false);
  const [isHistoricReleasesDialogVisible, setIsHistoricReleasesDialogVisible] = useState(false);
  const [isImportingDataflow, setIsImportingDataflow] = useState(false);
  const [isIntegrationManageDialogVisible, setIsIntegrationManageDialogVisible] = useState(false);
  const [isManageManualAcceptanceDatasetDialogVisible, setIsManageManualAcceptanceDatasetDialogVisible] = useState(
    false
  );
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

  useCheckNotifications(['ADD_DATACOLLECTION_FAILED_EVENT'], setIsActiveButton, true);
  useCheckNotifications(['UPDATE_DATACOLLECTION_COMPLETED_EVENT'], onUpdateData);
  useCheckNotifications(['UPDATE_DATACOLLECTION_FAILED_EVENT'], setIsActiveButton, true);
  useCheckNotifications(
    ['COPY_DATA_TO_EUDATASET_COMPLETED_EVENT', 'COPY_DATA_TO_EUDATASET_FAILED_EVENT'],
    setIsCopyDataCollectionToEuDatasetLoading,
    false
  );
  useCheckNotifications(
    ['EXTERNAL_EXPORT_EUDATASET_COMPLETED_EVENT', 'EXTERNAL_EXPORT_EUDATASET_FAILED_EVENT'],
    setIsExportEuDatasetLoading,
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
        {resources.messages['showPublicInfo']}
      </label>
    </div>
  );

  const cloneDatasetSchemas = async () => {
    setCloneDialogVisible(false);

    notificationContext.add({
      type: 'CLONE_DATASET_SCHEMAS_INIT',
      content: { sourceDataflowName: cloneDataflow.name, targetDataflowName: dataflowName }
    });
    setIsCloningDataflow(true);

    try {
      await DataflowService.cloneDatasetSchemas(cloneDataflow.id, dataflowId);
    } catch (error) {
      console.error(error);
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      } else {
        notificationContext.add({ type: 'CLONE_NEW_SCHEMA_ERROR' });
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
      <Button icon="check" label={resources.messages['ok']} onClick={() => onHideErrorDialog()} />
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
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId } });
    }
  };

  const handleExportEuDataset = value => setIsIntegrationManageDialogVisible(value);

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
      console.error(error);
      const {
        dataflow: { name: dataflowName }
      } = await getMetadata({ dataflowId });

      notificationContext.add({ type: 'CREATE_DATA_COLLECTION_ERROR', content: { dataflowId, dataflowName } });

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
      notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
    } else {
      notificationContext.add({ type: 'IMPORT_DATASET_SCHEMA_FAILED_EVENT' });
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

  const onLoadEuDatasetIntegration = async datasetSchemaId => {
    try {
      const euDatasetExportIntegration = await IntegrationService.findEUDatasetIntegration(datasetSchemaId);
      setEuDatasetExportIntegration(IntegrationsUtils.parseIntegration(euDatasetExportIntegration));
    } catch (error) {
      notificationContext.add({ type: 'LOAD_INTEGRATIONS_ERROR' });
    }
  };

  const onUpdateDataCollection = async () => {
    setIsUpdateDataCollectionDialogVisible(false);

    setIsActiveButton(false);

    try {
      return await DataCollectionService.update(dataflowId);
    } catch (error) {
      console.error(error);
    }
  };

  const onDeleteDatasetSchema = async index => {
    setDeleteDialogVisible(false);

    showLoading();
    try {
      const { status } = await DatasetService.deleteSchemaById(dataflowState.designDatasetSchemas[index].datasetId);
      if (status >= 200 && status <= 299) {
        onUpdateData();
        setUpdatedDatasetSchema(remove(dataflowState.updatedDatasetSchema, event => event.schemaIndex !== index));
      }
    } catch (error) {
      console.error(error.response);
      if (error.response.status === 401) {
        notificationContext.add({ type: 'DELETE_DATASET_SCHEMA_LINK_ERROR' });
      }
    } finally {
      hideLoading();
    }
  };

  const onHideErrorDialog = () => {
    setErrorDialogData({ isVisible: false, message: '' });
  };

  const onCopyDataCollectionToEuDataset = async () => {
    setIsCopyDataCollectionToEuDatasetDialogVisible(false);
    setIsCopyDataCollectionToEuDatasetLoading(true);

    try {
      const response = await EuDatasetService.copyDataCollection(dataflowId);
      if (response.status >= 200 && response.status <= 299) {
        notificationContext.add({ type: 'COPY_TO_EU_DATASET_INIT' });
      }
    } catch (error) {
      setIsCopyDataCollectionToEuDatasetLoading(false);

      if (error.response.status === 423) {
        notificationContext.add({ type: 'DATA_COLLECTION_LOCKED_ERROR' });
      } else {
        notificationContext.add({ type: 'COPY_DATA_COLLECTION_EU_DATASET_ERROR' });
      }
    }
  };

  const onExportEuDataset = async () => {
    setIsExportEuDatasetDialogVisible(false);
    setIsExportEuDatasetLoading(true);

    try {
      const response = await EuDatasetService.exportEuDataset(dataflowId);
      if (response.status >= 200 && response.status <= 299) {
        notificationContext.add({ type: 'EXPORT_EU_DATASET_INIT' });
      }
    } catch (error) {
      setIsExportEuDatasetLoading(false);

      if (error.response.status === 423) {
        notificationContext.add({ type: 'EU_DATASET_LOCKED_ERROR' });
      } else {
        notificationContext.add({ type: 'EXPORT_EU_DATASET_ERROR' });
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
      console.error(error);
      notificationContext.add({
        type: 'LOAD_RECEIPT_DATA_ERROR'
      });
    } finally {
      setIsReceiptLoading(false);
    }
  };

  const onShowCopyDataCollectionToEuDatasetModal = () => setIsCopyDataCollectionToEuDatasetDialogVisible(true);

  const onShowDataCollectionModal = () => setDataCollectionDialog(true);

  const onShowExportEuDatasetModal = () => setIsExportEuDatasetDialogVisible(true);

  const onShowNewSchemaDialog = () => setNewDatasetDialog(true);

  const onShowUpdateDataCollectionModal = () => setIsUpdateDataCollectionDialogVisible(true);

  const getDate = () => {
    return new Date(dayjs(dataCollectionDueDate).endOf('day').format()).getTime();
  };

  const onCreateDataCollectionsWithNotValids = async () => {
    setIsActiveButton(false);
    try {
      await DataCollectionService.create(dataflowId, getDate(), isManualTechnicalAcceptance, false);
    } catch (error) {
      console.error(error);
      const {
        dataflow: { name: dataflowName }
      } = await getMetadata({ dataflowId });

      notificationContext.add({ type: 'CREATE_DATA_COLLECTION_ERROR', content: { dataflowId, dataflowName } });

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
        label={resources.messages['close']}
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
          label={resources.messages['cloneSelectedDataflow']}
          onClick={() => cloneDatasetSchemas()}
        />
        <Button
          className="p-button-secondary p-button-animated-blink p-button-right-aligned"
          icon={'cancel'}
          label={resources.messages['close']}
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
    handleExportEuDataset,
    handleRedirect,
    isActiveButton,
    isCloningDataflow,
    isLeadReporterOfCountry,
    onCloneDataflow,
    onImportSchema,
    onLoadEuDatasetIntegration,
    onLoadReceiptData,
    onOpenReleaseConfirmDialog,
    onSaveName,
    onShowCopyDataCollectionToEuDatasetModal,
    onShowDataCollectionModal,
    onShowExportEuDatasetModal,
    onShowHistoricReleases,
    onShowManageReportersDialog,
    onShowManualTechnicalAcceptanceDialog,
    onShowNewSchemaDialog,
    onShowUpdateDataCollectionModal,
    setErrorDialogData
  })
    .filter(button => button.visibility)
    .map(button => <BigButton key={button.caption} {...button} />);

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
          manageDialogs={handleExportEuDataset}
          state={{ datasetSchemaId, isIntegrationManageDialogVisible }}
          updatedData={euDatasetExportIntegration}
        />
      )}

      {newDatasetDialog && (
        <Dialog
          className={styles.dialog}
          header={resources.messages['newDatasetSchema']}
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
          header={resources.messages['dataflowsList']}
          onHide={() => setCloneDialogVisible(false)}
          style={{ width: '95%' }}
          visible={cloneDialogVisible}>
          <CloneSchemas dataflowId={dataflowId} getCloneDataflow={getCloneDataflow} />
        </Dialog>
      )}

      {errorDialogData.isVisible && (
        <Dialog
          footer={errorDialogFooter}
          header={resources.messages['error'].toUpperCase()}
          onHide={onHideErrorDialog}
          visible={errorDialogData.isVisible}>
          <div className="p-grid p-fluid">{errorDialogData.message}</div>
        </Dialog>
      )}

      {deleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resources.messages['delete'].toUpperCase()}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteDatasetSchema(deleteSchemaIndex)}
          onHide={() => setDeleteDialogVisible(false)}
          visible={deleteDialogVisible}>
          {resources.messages['deleteDatasetSchema']}
        </ConfirmDialog>
      )}

      {isHistoricReleasesDialogVisible && (
        <Dialog
          className={styles.dialog}
          footer={renderDialogFooter}
          header={`${resources.messages['historicReleasesContextMenu']} ${historicReleasesDialogHeader}`}
          onHide={() => setIsHistoricReleasesDialogVisible(false)}
          visible={isHistoricReleasesDialogVisible}>
          <HistoricReleases
            dataProviderId={providerId}
            dataflowId={dataflowId}
            datasetId={datasetId}
            historicReleasesView={historicReleasesView}
          />
        </Dialog>
      )}

      {isManualTechnicalAcceptanceDialogVisible && (
        <Dialog
          className={styles.dialog}
          footer={renderDialogFooter}
          header={`${resources.messages['manualTechnicalAcceptanceHeader']} ${dataflowName}`}
          onHide={() => setIsManualTechnicalAcceptanceDialogVisible(false)}
          style={{ width: '80%' }}
          visible={isManualTechnicalAcceptanceDialogVisible}>
          <ManualAcceptanceDatasets
            dataflowId={dataflowData.id}
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
          header={resources.messages['updateDataCollectionHeader']}
          labelCancel={resources.messages['close']}
          labelConfirm={resources.messages['create']}
          onConfirm={() => onUpdateDataCollection()}
          onHide={() => setIsUpdateDataCollectionDialogVisible(false)}
          visible={isUpdateDataCollectionDialogVisible}>
          <p>{resources.messages['updateDataCollectionMessage']}</p>
        </ConfirmDialog>
      )}

      {dataCollectionDialog && (
        <ConfirmDialog
          className={styles.calendarConfirm}
          disabledConfirm={isNil(dataCollectionDueDate)}
          footerAddon={checkShowPublicInfo}
          header={resources.messages['createDataCollection']}
          labelCancel={resources.messages['close']}
          labelConfirm={resources.messages['create']}
          onConfirm={() => setIsConfirmCollectionDialog(true)}
          onHide={() => setDataCollectionDialog(false)}
          visible={dataCollectionDialog}>
          {hasExpirationDate ? (
            <p
              dangerouslySetInnerHTML={{
                __html: TextUtils.parseText(resources.messages['dataCollectionExpirationDate'], {
                  expirationData: dayjs(dataflowState.obligations.expirationDate).format(
                    userContext.userProps.dateFormat
                  )
                })
              }}></p>
          ) : (
            <p>
              <div>{`${resources.messages['chooseExpirationDate']} `}</div>
              <div>{`${resources.messages['chooseExpirationDateSecondLine']} `}</div>
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

      {isCopyDataCollectionToEuDatasetDialogVisible && (
        <ConfirmDialog
          header={resources.messages['copyDataCollectionToEuDatasetHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onCopyDataCollectionToEuDataset()}
          onHide={() => setIsCopyDataCollectionToEuDatasetDialogVisible(false)}
          visible={isCopyDataCollectionToEuDatasetDialogVisible}>
          <p>{resources.messages['copyDataCollectionToEuDatasetMessage']}</p>
        </ConfirmDialog>
      )}

      {isExportEuDatasetDialogVisible && (
        <ConfirmDialog
          header={resources.messages['exportEuDatasetHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onExportEuDataset()}
          onHide={() => setIsExportEuDatasetDialogVisible(false)}
          visible={isExportEuDatasetDialogVisible}>
          <p>{resources.messages['exportEuDatasetMessage']}</p>
        </ConfirmDialog>
      )}

      {isConfirmCollectionDialog && (
        <ConfirmDialog
          disabledConfirm={isNil(isManualTechnicalAcceptance)}
          header={resources.messages['createDataCollection']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onCreateDataCollections()}
          onHide={() => {
            setIsConfirmCollectionDialog(false);
            onResetRadioButtonOptions();
          }}
          visible={isConfirmCollectionDialog}>
          <div>{resources.messages['createDataCollectionConfirmQuestion']}</div>
          <div>{resources.messages['createDataCollectionConfirm']}</div>
          <div className={styles.radioButtonDiv}>
            <label>{resources.messages['manualTechnicalAcceptanceTitle']}</label>
            {renderRadioButtonsCreateDC()}
          </div>
        </ConfirmDialog>
      )}

      {isQCsNotValidWarningVisible && (
        <ConfirmDialog
          header={resources.messages['notValidQCWarningTitle']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onCreateDataCollectionsWithNotValids()}
          onHide={() => setIsQCsNotValidWarningVisible(false)}
          visible={isQCsNotValidWarningVisible}>
          {TextUtils.parseText(resources.messages['notValidQCWarningBody'], {
            disabled: invalidAndDisabledRulesAmount.disabledRules,
            invalid: invalidAndDisabledRulesAmount.invalidRules
          })}
        </ConfirmDialog>
      )}

      {isImportSchemaVisible && (
        <CustomFileUpload
          // dialogClassName={styles.Dialog}
          accept=".zip"
          chooseLabel={resources.messages['selectFile']}
          className={styles.FileUpload}
          dialogHeader={`${resources.messages['importSchema']}`}
          dialogOnHide={() => setIsImportSchemaVisible(false)} //allowTypes="/(\.|\/)(csv)$/"
          dialogVisible={isImportSchemaVisible}
          fileLimit={1}
          infoTooltip={`${resources.messages['supportedFileExtensionsTooltip']} .zip`}
          invalidExtensionMessage={resources.messages['invalidExtensionFile']}
          isDialog={true}
          mode="advanced"
          multiple={false}
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
        <span className="srOnly">{resources.messages['confirmationReceipt']}</span>
      </button>
    </Fragment>
  );
};
