import React, { useContext, useEffect, useRef } from 'react';

import isUndefined from 'lodash/isUndefined';

import styles from './BigButtonList.module.css';

import { BigButton } from './_components/BigButton';

import { ConfirmationReceiptService } from 'core/services/ConfirmationReceipt';
import { DownloadFile } from 'ui/views/_components/DownloadFile';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

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
  onShowSnapshotDialog,
  manageDialogs
}) => {
  const notificationContext = useContext(NotificationContext);

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

  const downloadPdf = response => {
    if (!isUndefined(response)) {
      DownloadFile(response, `${dataflowData.name}_${Date.now()}.pdf`);

      const url = window.URL.createObjectURL(new Blob([response]));

      const link = document.createElement('a');

      document.body.appendChild(link);

      link.click();

      document.body.removeChild(link);

      window.URL.revokeObjectURL(url);
    }
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

  const removeNew = () => {
    receiptDispatch({
      type: 'ON_CLEAN_UP',
      payload: { isLoading: false, isOutdated: false }
    });
  };

  return (
    <>
      <div className={styles.buttonsWrapper}>
        <div className={styles.splitButtonWrapper}>
          <div className={styles.datasetItem}>
            {useBigButtonList({
              dataflowData,
              manageDialogs,
              dataflowId,
              handleRedirect,
              hasWritePermissions,
              isCustodian,
              onLoadReceiptData: onLoadReceiptData,
              receiptState,
              representative,
              onShowSnapshotDialog
            }).map((button, i) => (button.visibility ? <BigButton key={i} {...button} /> : <></>))}
          </div>
        </div>
      </div>

      {({ loading }) => !loading && <button ref={receiptBtnRef} style={{ display: 'none' }} />}
    </>
  );
};
