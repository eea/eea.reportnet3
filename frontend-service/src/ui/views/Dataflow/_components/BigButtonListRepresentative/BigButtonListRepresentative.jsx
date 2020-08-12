import React, { Fragment, useContext, useEffect, useRef } from 'react';

import isNil from 'lodash/isNil';

import styles from '../BigButtonList/BigButtonList.module.css';

import { BigButton } from '../BigButton';

import { ConfirmationReceiptService } from 'core/services/ConfirmationReceipt';
import { DownloadFile } from 'ui/views/_components/DownloadFile';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { useBigButtonList } from './_functions/Hooks/useBigButtonList';

export const BigButtonListRepresentative = ({
  dataflowState,
  handleRedirect,
  match,
  onCleanUpReceipt,
  onShowSnapshotDialog,
  setIsReceiptLoading
}) => {
  const notificationContext = useContext(NotificationContext);

  const receiptBtnRef = useRef(null);

  useEffect(() => {
    const response = notificationContext.toShow.find(notification => notification.key === 'LOAD_RECEIPT_DATA_ERROR');
    if (response) {
      setIsReceiptLoading(false);
    }
  }, [notificationContext]);

  const downloadPdf = response => {
    if (!isNil(response)) {
      DownloadFile(response, `${dataflowState.data.name}_${Date.now()}.pdf`);
    }
  };

  const onLoadReceiptData = async () => {
    try {
      setIsReceiptLoading(true);
      const response = await ConfirmationReceiptService.get(dataflowState.id, match.params.representativeId);
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

  return (
    <>
      <div className={styles.buttonsWrapper}>
        <div className={styles.splitButtonWrapper}>
          <div className={styles.datasetItem}>
            {useBigButtonList({
              dataflowState,
              handleRedirect,
              match,
              onLoadReceiptData,
              onShowSnapshotDialog
            }).map((button, i) => (button.visibility ? <BigButton key={i} {...button} /> : <Fragment key={i} />))}
          </div>
        </div>
      </div>

      <button ref={receiptBtnRef} style={{ display: 'none' }} />
    </>
  );
};
