import React, { useContext, useLayoutEffect, useRef, useState } from 'react';

import { isNull, isUndefined, remove } from 'lodash';
import { PDFDownloadLink } from '@react-pdf/renderer';

import styles from './BigButtonList.module.css';

import { BigButton } from './_components/BigButton';
import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar/Calendar';
import { ConfirmationReceipt } from 'ui/views/_components/ConfirmationReceipt';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { NewDatasetSchemaForm } from './_components/NewDatasetSchemaForm';

import { ConfirmationReceiptService } from 'core/services/ConfirmationReceipt';
import { DatasetService } from 'core/services/Dataset';
import { DataCollectionService } from 'core/services/DataCollection';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useBigButtonList } from './_functions/Hooks/useBigButtonList';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

import { MetadataUtils } from 'ui/views/_functions/Utils';

export const BigButtonList = ({
  dataflowData,
  dataflowId,
  dataflowStatus,
  dataProviderId,
  designDatasetSchemas,
  handleRedirect,
  hasRepresentatives,
  hasWritePermissions,
  isCustodian,
  isDataSchemaCorrect,
  onUpdateData,
  onSaveName,
  showReleaseSnapshotDialog,
  updatedDatasetSchema,
  setUpdatedDatasetSchema
}) => {
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [dataCollectionDialog, setDataCollectionDialog] = useState(false);
  const [dataCollectionDueDate, setDataCollectionDueDate] = useState();
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [deleteSchemaIndex, setDeleteSchemaIndex] = useState();
  const [errorDialogVisible, setErrorDialogVisible] = useState(false);
  const [isCreateButtonActive, setIsCreateButtonActive] = useState(true);
  const [isDuplicated, setIsDuplicated] = useState(false);
  const [isFormReset, setIsFormReset] = useState(true);
  const [newDatasetDialog, setNewDatasetDialog] = useState(false);
  const [receiptData, setReceiptData] = useState();

  const receiptBtnRef = useRef(null);

  useLayoutEffect(() => {
    setTimeout(() => {
      if (!isUndefined(receiptData)) {
        onDownloadReceipt();
      }
    }, 1000);
  }, [receiptData]);

  useCheckNotifications(['ADD_DATACOLLECTION_FAILED_EVENT'], setIsCreateButtonActive, true);

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
    setIsCreateButtonActive(false);
    try {
      return await DataCollectionService.create(dataflowId, date);
    } catch (error) {
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
      setIsCreateButtonActive(true);
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
    } finally {
      hideLoading();
    }
  };

  const onDownloadReceipt = () => {
    if (!isNull(receiptBtnRef.current) && !isUndefined(receiptData)) {
      receiptBtnRef.current.click();
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
      const response = await ConfirmationReceiptService.get(dataflowId, dataProviderId);
      setReceiptData(response);
    } catch (error) {
      console.log('error', error);
      notificationContext.add({
        type: 'LOAD_RECEIPT_DATA_ERROR'
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

  const bigButtonList = useBigButtonList({
    dataflowData: dataflowData,
    dataflowId: dataflowId,
    dataflowStatus: dataflowStatus,
    getDeleteSchemaIndex: getDeleteSchemaIndex,
    handleRedirect: handleRedirect,
    hasRepresentatives: hasRepresentatives,
    hasWritePermissions: hasWritePermissions,
    isCreateButtonActive: isCreateButtonActive,
    isCustodian: isCustodian,
    isDataSchemaCorrect: isDataSchemaCorrect,
    onDatasetSchemaNameError: onDatasetSchemaNameError,
    onDuplicateName: onDuplicateName,
    onLoadReceiptData: onLoadReceiptData,
    onSaveName: onSaveName,
    onShowDataCollectionModal: onShowDataCollectionModal,
    onShowNewSchemaDialog: onShowNewSchemaDialog,
    showReleaseSnapshotDialog: showReleaseSnapshotDialog,
    updatedDatasetSchema: updatedDatasetSchema
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
        header={resources.messages['delete'].toUpperCase()}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        onConfirm={() => onDeleteDatasetSchema(deleteSchemaIndex)}
        onHide={() => setDeleteDialogVisible(false)}
        visible={deleteDialogVisible}>
        {resources.messages['deleteDatasetSchema']}
      </ConfirmDialog>

      <ConfirmDialog
        header={resources.messages['createDataCollection']}
        disabledConfirm={isUndefined(dataCollectionDueDate)}
        labelCancel={resources.messages['close']}
        labelConfirm={resources.messages['create']}
        onConfirm={() => onCreateDataCollection(new Date(dataCollectionDueDate).getTime() / 1000)}
        onHide={() => setDataCollectionDialog(false)}
        visible={dataCollectionDialog}>
        <p>{`${resources.messages['chooseExpirationDate']}: `}</p>
        <Calendar
          className={styles.calendar}
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

      <PDFDownloadLink
        document={<ConfirmationReceipt receiptData={receiptData} resources={resources} />}
        fileName={`${dataflowData.name}_${Date.now()}.pdf`}>
        {({ loading }) => !loading && <button ref={receiptBtnRef} style={{ display: 'none' }} />}
      </PDFDownloadLink>
    </>
  );
};
