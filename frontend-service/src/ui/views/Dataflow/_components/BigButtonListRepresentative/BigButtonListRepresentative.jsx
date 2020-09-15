import React, { Fragment, useContext, useEffect, useRef, useState } from 'react';

import isNil from 'lodash/isNil';

import styles from '../BigButtonList/BigButtonList.module.css';

import { BigButton } from '../BigButton';
import { Button } from 'ui/views/_components/Button';
import { ConfirmationReceiptService } from 'core/services/ConfirmationReceipt';
import { Dialog } from 'ui/views/_components/Dialog';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { HistoricReleases } from 'ui/views/Dataflow/_components/HistoricReleases';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

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
  const resources = useContext(ResourcesContext);

  const [datasetId, setDatasetId] = useState(null);
  const [historicReleasesDialogHeader, setHistoricReleasesDialogHeader] = useState([]);
  const [historicReleasesView, setHistoricReleasesView] = useState('');
  const [isHistoricReleasesDialogVisible, setIsHistoricReleasesDialogVisible] = useState(false);

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

  const getDataHistoricReleases = (datasetId, value) => {
    setDatasetId(datasetId);
    setHistoricReleasesDialogHeader(value);
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

  const onShowHistoricReleases = typeView => {
    setIsHistoricReleasesDialogVisible(true);
    setHistoricReleasesView(typeView);
  };

  const renderDialogFooter = (
    <Fragment>
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => setIsHistoricReleasesDialogVisible(false)}
      />
    </Fragment>
  );

  return (
    <>
      <div className={styles.buttonsWrapper}>
        <div className={styles.splitButtonWrapper}>
          <div className={styles.datasetItem}>
            {useBigButtonList({
              dataflowState,
              getDataHistoricReleases,
              handleRedirect,
              match,
              onLoadReceiptData,
              onShowHistoricReleases,
              onShowSnapshotDialog
            }).map((button, i) => (button.visibility ? <BigButton key={i} {...button} /> : <Fragment key={i} />))}
          </div>
        </div>
      </div>

      <button ref={receiptBtnRef} style={{ display: 'none' }} />

      {isHistoricReleasesDialogVisible && (
        <Dialog
          className={styles.dialog}
          footer={renderDialogFooter}
          header={`${resources.messages['historicReleases']} ${historicReleasesDialogHeader}`}
          onHide={() => setIsHistoricReleasesDialogVisible(false)}
          // style={{ width: '80%' }}
          visible={isHistoricReleasesDialogVisible}>
          <HistoricReleases datasetId={datasetId} historicReleasesView={historicReleasesView} />
        </Dialog>
      )}
    </>
  );
};
