import React, { useContext, useEffect, useRef, useState } from 'react';

import { isEmpty, isNull, isUndefined } from 'lodash';
import { PDFDownloadLink } from '@react-pdf/renderer';

import styles from './BigButtonList.module.css';

import { BigButton } from './_components/BigButton';

import { ConfirmationReceiptService } from 'core/services/ConfirmationReceipt';
import { DownloadFile } from 'ui/views/_components/DownloadFile';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useBigButtonList } from './_functions/Hooks/useBigButtonList';

export const BigButtonList = ({
  dataflowData,
  dataflowId,
  dataProviderId,
  handleRedirect,
  hasWritePermissions,
  isCustodian,
  receiptDispatch,
  receiptState,
  representative,
  showReleaseSnapshotDialog
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const [fileToDownload, setFileToDownload] = useState(undefined);

  const receiptBtnRef = useRef(null);

  useEffect(() => {
    const response = notificationContext.toShow.find(notification => notification.key === 'LOAD_RECEIPT_DATA_ERROR');
    if (response) {
      receiptDispatch({
        type: 'ON_DOWNLOAD',
        payload: { isLoading: false }
      });
    }
  }, [notificationContext]);

  useEffect(() => {
    if (!isUndefined(fileToDownload)) {
      DownloadFile(fileToDownload, 'Receipt.pdf');

      const url = window.URL.createObjectURL(new Blob([fileToDownload]));

      const link = document.createElement('a');

      document.body.appendChild(link);

      link.click();

      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    }
  }, [fileToDownload]);
  console.log('object', fileToDownload);
  // useEffect(() => {
  //   setTimeout(() => {
  //     if (!isEmpty(receiptState.receiptPdf)) {
  //       onDownloadReceipt();
  //     }
  //   }, 1000);
  // }, [receiptState.receiptPdf]);

  const onDownloadReceipt = () => {
    if (!isNull(receiptBtnRef.current) && !isEmpty(receiptState.receiptPdf)) {
      receiptBtnRef.current.click();

      receiptDispatch({
        type: 'ON_CLEAN_UP',
        payload: { isLoading: false, isOutdated: false, receiptPdf: {} }
      });
    }
  };

  const onLoadReceiptData = async () => {
    try {
      receiptDispatch({
        type: 'ON_DOWNLOAD',
        payload: { isLoading: true }
      });
      const response = await ConfirmationReceiptService.get(dataflowId, dataProviderId);
      setFileToDownload(response);
    } catch (error) {
      console.error('error', error);
      notificationContext.add({
        type: 'LOAD_RECEIPT_DATA_ERROR'
      });
      receiptDispatch({
        type: 'ON_DOWNLOAD',
        payload: { isLoading: false }
      });
    } finally {
      receiptDispatch({
        type: 'ON_DOWNLOAD',
        payload: { isLoading: false }
      });
    }
  };

  return (
    <>
      <div className={styles.buttonsWrapper}>
        <div className={styles.splitButtonWrapper}>
          <div className={styles.datasetItem}>
            {useBigButtonList({
              dataflowData: dataflowData,
              dataflowId: dataflowId,
              handleRedirect: handleRedirect,
              hasWritePermissions: hasWritePermissions,
              isCustodian: isCustodian,
              onLoadReceiptData: onLoadReceiptData,
              receiptState: receiptState,
              representative,
              showReleaseSnapshotDialog: showReleaseSnapshotDialog
            }).map((button, i) => (button.visibility ? <BigButton key={i} {...button} /> : <></>))}
          </div>
        </div>
      </div>

      {({ loading }) => !loading && <button ref={receiptBtnRef} style={{ display: 'none' }} />}
      {/* <PDFDownloadLink
        document={<ConfirmationReceipt receiptPdf={receiptState.receiptPdf} resources={resources} />}
        fileName={`${dataflowData.name}_${Date.now()}.pdf`}>
      </PDFDownloadLink> */}
    </>
  );
};
