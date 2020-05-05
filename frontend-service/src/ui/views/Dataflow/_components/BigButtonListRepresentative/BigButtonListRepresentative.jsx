import React, { useContext, useEffect, useRef } from 'react';

import isUndefined from 'lodash/isUndefined';

import styles from './BigButtonListRepresentative.module.css';

import { BigButton } from './_components/BigButton';

import { ConfirmationReceiptService } from 'core/services/ConfirmationReceipt';
import { DownloadFile } from 'ui/views/_components/DownloadFile';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { useBigButtonList } from './_functions/Hooks/useBigButtonList';
import { dataflowActionCreators } from '../../_functions/dataflowActionCreators';

export const BigButtonListRepresentative = ({
  dataflowDispatch,
  dataflowState,
  handleRedirect,
  onShowSnapshotDialog,
  match
}) => {
  const notificationContext = useContext(NotificationContext);

  const { setIsReceiptLoading, onCleanUpReceipt } = dataflowActionCreators(dataflowDispatch);

  const receiptBtnRef = useRef(null);

  useEffect(() => {
    const response = notificationContext.toShow.find(notification => notification.key === 'LOAD_RECEIPT_DATA_ERROR');
    if (response) {
      setIsReceiptLoading(false);
    }
  }, [notificationContext]);

  const downloadPdf = response => {
    if (!isUndefined(response)) {
      DownloadFile(response, `${dataflowState.data.name}_${Date.now()}.pdf`);

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
      setIsReceiptLoading(true);
      const response = await ConfirmationReceiptService.get(dataflowState.id, dataflowState.dataProviderId);
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
              dataflowDispatch,
              handleRedirect,
              match,
              onLoadReceiptData,
              onShowSnapshotDialog
            }).map((button, i) => (button.visibility ? <BigButton key={i} {...button} /> : <></>))}
          </div>
        </div>
      </div>

      {({ loading }) => !loading && <button ref={receiptBtnRef} style={{ display: 'none' }} />}
    </>
  );
};
