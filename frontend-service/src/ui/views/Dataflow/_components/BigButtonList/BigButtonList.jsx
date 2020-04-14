import React, { useContext, useEffect, useRef, useState } from 'react';

import isNil from 'lodash/isNil';
import remove from 'lodash/remove';
import moment from 'moment';

import styles from './BigButtonList.module.css';

import { BigButton } from './_components/BigButton';
import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar/Calendar';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { NewDatasetSchemaForm } from './_components/NewDatasetSchemaForm';

import { ConfirmationReceiptService } from 'core/services/ConfirmationReceipt';
import { DatasetService } from 'core/services/Dataset';
import { DataCollectionService } from 'core/services/DataCollection';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { useBigButtonList } from './_functions/Hooks/useBigButtonList';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

import { MetadataUtils } from 'ui/views/_functions/Utils';
import { TextUtils } from 'ui/views/_functions/Utils';

export const BigButtonList = ({
  dataflowData,
  dataflowDataState,
  dataflowId,
  dataProviderId,
  designDatasetSchemas,
  handleRedirect,
  hasWritePermissions,
  isCustodian,
  isDataSchemaCorrect,
  onSaveName,
  onUpdateData,
  receiptDispatch,
  receiptState,
  setUpdatedDatasetSchema,
  showReleaseSnapshotDialog,
  updatedDatasetSchema
}) => {
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [dataCollectionDialog, setDataCollectionDialog] = useState(false);
  const [dataCollectionDueDate, setDataCollectionDueDate] = useState(null);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [deleteSchemaIndex, setDeleteSchemaIndex] = useState();
  const [errorDialogVisible, setErrorDialogVisible] = useState(false);
  const [isActiveButton, setIsActiveButton] = useState(true);
  const [isDuplicated, setIsDuplicated] = useState(false);
  const [isFormReset, setIsFormReset] = useState(true);
  const [isUpdateDatacollectionDialogVisible, setIsUpdateDatacollectionDialogVisible] = useState(false);
  const [newDatasetDialog, setNewDatasetDialog] = useState(false);
  const hasExpirationDate = new Date(dataflowDataState.obligations.expirationDate) > new Date();

  const receiptBtnRef = useRef(null);

  useCheckNotifications(['ADD_DATACOLLECTION_FAILED_EVENT'], setIsActiveButton, true);
  useCheckNotifications(['UPDATE_DATACOLLECTION_COMPLETED_EVENT'], onUpdateData);
  useCheckNotifications(['UPDATE_DATACOLLECTION_FAILED_EVENT'], setIsActiveButton, true);

  useEffect(() => {
    const response = notificationContext.toShow.find(notification => notification.key === 'LOAD_RECEIPT_DATA_ERROR');

    if (response) {
      receiptDispatch({ type: 'ON_DOWNLOAD', payload: { isLoading: false } });
    }
  }, [notificationContext]);

  useEffect(() => {
    getExpirationDate();
  }, [dataflowDataState.obligations.expirationDate]);

  const downloadPdf = response => {
    if (!isNil(response)) {
      DownloadFile(response, `${dataflowData.name}_${Date.now()}.pdf`);

      const url = window.URL.createObjectURL(new Blob([response]));

      const link = document.createElement('a');

      document.body.appendChild(link);

      link.click();

      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    }
  };

  const removeNew = () => {
    receiptDispatch({
      type: 'ON_CLEAN_UP',
      payload: { isLoading: false, isOutdated: false }
    });
  };

  const errorDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['ok']}
        icon="check"
        onClick={() => {
          setErrorDialogVisible(false);
          setIsDuplicated(false);
        }}
      />
    </div>
  );

  const getDeleteSchemaIndex = index => {
    setDeleteSchemaIndex(index);
    setDeleteDialogVisible(true);
  };

  const getExpirationDate = () => {
    setDataCollectionDueDate(
      !isNil(dataflowDataState.obligations.expirationDate) &&
        new Date(dataflowDataState.obligations.expirationDate) > new Date()
        ? new Date(dataflowDataState.obligations.expirationDate)
        : null
    );
  };

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      notificationContext.add({
        type: 'GET_METADATA_ERROR',
        content: {
          dataflowId
        }
      });
    }
  };

  const onCreateDatasetSchema = () => {
    setNewDatasetDialog(false);
  };

  const onCreateDataCollection = async date => {
    setDataCollectionDialog(false);

    notificationContext.add({
      type: 'CREATE_DATA_COLLECTION_INIT',
      content: {}
    });

    setIsActiveButton(false);

    try {
      return await DataCollectionService.create(dataflowId, date);
    } catch (error) {
      console.error(error);
      const {
        dataflow: { name: dataflowName }
      } = await getMetadata({ dataflowId });
      notificationContext.add({
        type: 'CREATE_DATA_COLLECTION_ERROR',
        content: {
          dataflowId,
          dataflowName
        }
      });

      setIsActiveButton(true);
    }
  };

  const onUpdateDataCollection = async () => {
    setIsUpdateDatacollectionDialogVisible(false);

    setIsActiveButton(false);

    try {
      const result = await DataCollectionService.update(dataflowId);
      return result;
    } catch (error) {
      console.error(error);
    }
  };

  const onDatasetSchemaNameError = () => {
    setErrorDialogVisible(true);
  };

  const onDeleteDatasetSchema = async index => {
    setDeleteDialogVisible(false);

    showLoading();
    try {
      const response = await DatasetService.deleteSchemaById(designDatasetSchemas[index].datasetId);
      if (response >= 200 && response <= 299) {
        onUpdateData();
        setUpdatedDatasetSchema(remove(updatedDatasetSchema, event => event.schemaIndex != index));
      }
    } catch (error) {
      console.error(error.response);
      if (error.response.status === 401) {
        notificationContext.add({
          type: 'DELETE_DATASET_SCHEMA_LINK_ERROR'
        });
      }
    } finally {
      hideLoading();
    }
  };

  const onDuplicateName = () => {
    setIsDuplicated(true);
  };

  const onHideErrorDialog = () => {
    setErrorDialogVisible(false);
    setIsDuplicated(false);
  };

  const onLoadReceiptData = async () => {
    try {
      receiptDispatch({
        type: 'ON_DOWNLOAD',
        payload: { isLoading: true }
      });
      const response = await ConfirmationReceiptService.get(dataflowId, dataProviderId);

      downloadPdf(response);
      removeNew();
    } catch (error) {
      console.error(error);
      notificationContext.add({
        type: 'LOAD_RECEIPT_DATA_ERROR'
      });
    } finally {
      receiptDispatch({
        type: 'ON_DOWNLOAD',
        payload: { isLoading: false }
      });
    }
  };

  const onShowNewSchemaDialog = () => {
    setNewDatasetDialog(true);
    setIsFormReset(true);
  };

  const onShowDataCollectionModal = () => {
    setDataCollectionDialog(true);
  };

  const onShowUpdateDataCollectionModal = () => {
    setIsUpdateDatacollectionDialogVisible(true);
  };

  const bigButtonList = useBigButtonList({
    dataflowData,
    dataflowDataState,
    dataflowId,
    getDeleteSchemaIndex,
    handleRedirect,
    hasWritePermissions,
    isActiveButton,
    isCustodian,
    isDataSchemaCorrect,
    onDatasetSchemaNameError,
    onDuplicateName,
    onLoadReceiptData,
    onSaveName,
    onShowDataCollectionModal,
    onShowNewSchemaDialog,
    onShowUpdateDataCollectionModal,
    receiptState,
    showReleaseSnapshotDialog,
    updatedDatasetSchema
  })
    .filter(button => button.visibility)
    .map((button, i) => <BigButton key={i} {...button} />);

  return (
    <>
      <div className={styles.buttonsWrapper}>
        <div className={styles.splitButtonWrapper}>
          <div className={styles.datasetItem}>{bigButtonList}</div>
        </div>
      </div>

      <Dialog
        header={resources.messages['newDatasetSchema']}
        visible={newDatasetDialog}
        className={styles.dialog}
        dismissableMask={false}
        onHide={() => {
          setNewDatasetDialog(false);
          setIsFormReset(false);
        }}>
        <NewDatasetSchemaForm
          dataflowId={dataflowId}
          datasetSchemaInfo={updatedDatasetSchema}
          isFormReset={isFormReset}
          onCreate={onCreateDatasetSchema}
          onUpdateData={onUpdateData}
          setNewDatasetDialog={setNewDatasetDialog}
        />
      </Dialog>

      <Dialog
        footer={errorDialogFooter}
        header={resources.messages['error'].toUpperCase()}
        onHide={onHideErrorDialog}
        visible={isDuplicated}>
        <div className="p-grid p-fluid">{resources.messages['duplicateSchemaError']}</div>
      </Dialog>

      <Dialog
        footer={errorDialogFooter}
        header={resources.messages['error'].toUpperCase()}
        onHide={onHideErrorDialog}
        visible={errorDialogVisible}>
        <div className="p-grid p-fluid">{resources.messages['emptyDatasetSchema']}</div>
      </Dialog>

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

      <ConfirmDialog
        header={resources.messages['updateDataCollectionHeader']}
        labelCancel={resources.messages['close']}
        labelConfirm={resources.messages['create']}
        onConfirm={() => onUpdateDataCollection()}
        onHide={() => setIsUpdateDatacollectionDialogVisible(false)}
        visible={isUpdateDatacollectionDialogVisible}>
        <p>{resources.messages['updateDataCollectionMessage']}</p>
      </ConfirmDialog>

      <ConfirmDialog
        header={resources.messages['createDataCollection']}
        disabledConfirm={isNil(dataCollectionDueDate)}
        labelCancel={resources.messages['close']}
        labelConfirm={resources.messages['create']}
        onConfirm={() =>
          onCreateDataCollection(new Date(moment(dataCollectionDueDate).endOf('day').format()).getTime() / 1000)
        }
        onHide={() => setDataCollectionDialog(false)}
        visible={dataCollectionDialog}>
        {hasExpirationDate ? (
          <p
            dangerouslySetInnerHTML={{
              __html: TextUtils.parseText(resources.messages['dataCollectionExpirationDate'], {
                expirationData: moment(dataflowDataState.obligations.expirationDate).format(user.userProps.dateFormat)
              })
            }}></p>
        ) : (
          <p>{`${resources.messages['chooseExpirationDate']}: `}</p>
        )}
        <Calendar
          className={styles.calendar}
          disabledDates={[new Date()]}
          inline={true}
          monthNavigator={true}
          minDate={new Date()}
          onChange={event => setDataCollectionDueDate(event.target.value)}
          showWeek={true}
          value={dataCollectionDueDate}
          yearNavigator={true}
          yearRange="2020:2030"
        />
      </ConfirmDialog>

      {({ loading }) => !loading && <button ref={receiptBtnRef} style={{ display: 'none' }} />}
    </>
  );
};
