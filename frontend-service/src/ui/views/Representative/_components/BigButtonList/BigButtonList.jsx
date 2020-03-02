import React, { useContext, useEffect, useRef } from 'react';

import { isEmpty, isNull } from 'lodash';
import { PDFDownloadLink } from '@react-pdf/renderer';

import styles from './BigButtonList.module.css';

import { BigButton } from './_components/BigButton';
import { ConfirmationReceipt } from 'ui/views/_components/ConfirmationReceipt';

import { ConfirmationReceiptService } from 'core/services/ConfirmationReceipt';

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
    setTimeout(() => {
      if (!isEmpty(receiptState.receiptData)) {
        onDownloadReceipt();
      }
    }, 1000);
  }, [receiptState.receiptData]);

  const onDownloadReceipt = () => {
    if (!isNull(receiptBtnRef.current) && !isEmpty(receiptState.receiptData)) {
      receiptBtnRef.current.click();
      receiptDispatch({
        type: 'ON_CLEAN_UP',
        payload: { isLoading: false, isOutdated: false }
      });
    }
  };

  const onLoadReceiptData = async () => {
    try {
      const response = await ConfirmationReceiptService.get(dataflowId, dataProviderId);
      receiptDispatch({
        type: 'ON_DOWNLOAD',
        payload: { isLoading: true, receiptData: response }
      });
    } catch (error) {
      console.error('error', error);
      notificationContext.add({
        type: 'LOAD_RECEIPT_DATA_ERROR'
      });
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

      <PDFDownloadLink
        document={<ConfirmationReceipt receiptData={receiptState.receiptData} resources={resources} />}
        fileName={`${dataflowData.name}_${Date.now()}.pdf`}>
        {({ loading }) => !loading && <button ref={receiptBtnRef} style={{ display: 'none' }} />}
      </PDFDownloadLink>
    </>
  );
};
