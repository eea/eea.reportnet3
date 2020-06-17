import React, { useContext, useEffect, useRef, useState } from 'react';

import isNil from 'lodash/isNil';
import remove from 'lodash/remove';
import uniqBy from 'lodash/uniqBy';
import moment from 'moment';

import styles from './BigButtonList.module.css';

import { BigButton } from '../BigButton';
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
  dataflowState,
  handleRedirect,
  onCleanUpReceipt,
  onSaveName,
  onShowManageReportersDialog,
  onShowSnapshotDialog,
  onUpdateData,
  setIsReceiptLoading,
  setUpdatedDatasetSchema,
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
  const [isConfirmCollectionDialog, setIsConfirmCollectionDialog] = useState(false);
  const [isDuplicated, setIsDuplicated] = useState(false);
  const [isUpdateDataCollectionDialogVisible, setIsUpdateDataCollectionDialogVisible] = useState(false);
  const [newDatasetDialog, setNewDatasetDialog] = useState(false);

  const hasExpirationDate = new Date(dataflowState.obligations.expirationDate) > new Date();
  const receiptBtnRef = useRef(null);

  const dataflowId = dataflowState.id;

  useCheckNotifications(['ADD_DATACOLLECTION_FAILED_EVENT'], setIsActiveButton, true);
  useCheckNotifications(['UPDATE_DATACOLLECTION_COMPLETED_EVENT'], onUpdateData);
  useCheckNotifications(['UPDATE_DATACOLLECTION_FAILED_EVENT'], setIsActiveButton, true);

  useEffect(() => {
    const response = notificationContext.toShow.find(notification => notification.key === 'LOAD_RECEIPT_DATA_ERROR');

    if (response) {
      setIsReceiptLoading(false);
    }
  }, [notificationContext]);

  useEffect(() => {
    getExpirationDate();
  }, [dataflowState.obligations.expirationDate]);

  const downloadPdf = response => {
    if (!isNil(response)) {
      DownloadFile(response, `${dataflowState.data.name}_${Date.now()}.pdf`);

      const url = window.URL.createObjectURL(new Blob([response]));

      const link = document.createElement('a');

      document.body.appendChild(link);

      link.click();

      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    }
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

  // const exportDatatableSchema = async (datasetId, datasetName) => {
  //   const schema = await DatasetService.schemaById(datasetId);
  // console.log(datasetId, datasetName, schema);

  // let blob = new Blob([csv], {
  //   type: 'text/csv;charset=utf-8;'
  // });

  // if (window.navigator.msSaveOrOpenBlob) {
  //   navigator.msSaveOrOpenBlob(blob, this.props.exportFilename + '.csv');
  // } else {
  //   let link = document.createElement('a');
  //   link.style.display = 'none';
  //   document.body.appendChild(link);
  //   if (link.download !== undefined) {
  //     link.setAttribute('href', URL.createObjectURL(blob));
  //     link.setAttribute('download', this.props.exportFilename + '.csv');
  //     link.click();
  //   } else {
  //     csv = 'data:text/csv;charset=utf-8,' + csv;
  //     window.open(encodeURI(csv));
  //   }
  //   document.body.removeChild(link);
  // }
  // };

  const getDeleteSchemaIndex = index => {
    setDeleteSchemaIndex(index);
    setDeleteDialogVisible(true);
  };

  const getExpirationDate = () => {
    setDataCollectionDueDate(
      !isNil(dataflowState.obligations.expirationDate) &&
        new Date(dataflowState.obligations.expirationDate) > new Date()
        ? new Date(dataflowState.obligations.expirationDate)
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
    setIsConfirmCollectionDialog(false);
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
    setIsUpdateDataCollectionDialogVisible(false);

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
      const response = await DatasetService.deleteSchemaById(dataflowState.designDatasetSchemas[index].datasetId);
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
      setIsReceiptLoading(true);
      const response = await ConfirmationReceiptService.get(dataflowId, dataflowState.dataProviderId);

      downloadPdf(response);
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

  const onShowNewSchemaDialog = () => {
    setNewDatasetDialog(true);
  };

  const onShowDataCollectionModal = () => {
    setDataCollectionDialog(true);
  };

  const onShowUpdateDataCollectionModal = () => {
    setIsUpdateDataCollectionDialogVisible(true);
  };

  const bigButtonList = uniqBy(
    useBigButtonList({
      dataflowId,
      dataflowState,
      // exportDatatableSchema,
      getDeleteSchemaIndex,
      handleRedirect,
      isActiveButton,
      onDatasetSchemaNameError,
      onDuplicateName,
      onLoadReceiptData,
      onSaveName,
      onShowDataCollectionModal,
      onShowManageReportersDialog,
      onShowNewSchemaDialog,
      onShowSnapshotDialog,
      onShowUpdateDataCollectionModal,
      updatedDatasetSchema
    }),
    'caption'
  )
    .filter(button => button.visibility)
    .map((button, i) => <BigButton key={i} {...button} />);

  return (
    <>
      <div className={styles.buttonsWrapper}>
        <div className={styles.splitButtonWrapper}>
          <div className={styles.datasetItem}>{bigButtonList}</div>
        </div>
      </div>

      {newDatasetDialog && (
        <Dialog
          className={styles.dialog}
          dismissableMask={false}
          header={resources.messages['newDatasetSchema']}
          onHide={() => setNewDatasetDialog(false)}
          visible={newDatasetDialog}>
          <NewDatasetSchemaForm
            dataflowId={dataflowId}
            datasetSchemaInfo={updatedDatasetSchema}
            onCreate={onCreateDatasetSchema}
            onUpdateData={onUpdateData}
            setNewDatasetDialog={setNewDatasetDialog}
          />
        </Dialog>
      )}

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
        onHide={() => setIsUpdateDataCollectionDialogVisible(false)}
        visible={isUpdateDataCollectionDialogVisible}>
        <p>{resources.messages['updateDataCollectionMessage']}</p>
      </ConfirmDialog>

      <ConfirmDialog
        className={styles.calendarConfirm}
        disabledConfirm={isNil(dataCollectionDueDate)}
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
                expirationData: moment(dataflowState.obligations.expirationDate).format(user.userProps.dateFormat)
              })
            }}></p>
        ) : (
          <p>
            <div>{`${resources.messages['chooseExpirationDate']} `}</div>
            <div>{`${resources.messages['chooseExpirationDateSecond']} `}</div>
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

      {isConfirmCollectionDialog && (
        <ConfirmDialog
          header={resources.messages['createDataCollection']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() =>
            onCreateDataCollection(new Date(moment(dataCollectionDueDate).endOf('day').format()).getTime() / 1000)
          }
          onHide={() => setIsConfirmCollectionDialog(false)}
          visible={isConfirmCollectionDialog}>
          {resources.messages['createDataCollectionConfirm']}
        </ConfirmDialog>
      )}

      <button ref={receiptBtnRef} style={{ display: 'none' }} />
    </>
  );
};
