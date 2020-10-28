import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';

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

import { bigButtonListRepresentativeReducer } from './_functions/Reducers/bigButtonListRepresentativeReducer';

import { useBigButtonList } from './_functions/Hooks/useBigButtonList';

export const BigButtonListRepresentative = ({
  dataflowState,
  handleRedirect,
  isLeadReporterOfCountry,
  match,
  onCleanUpReceipt,
  onShowSnapshotDialog,
  setIsReceiptLoading
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [bigButtonListRepresentativeState, bigButtonListRepresentativeDispatch] = useReducer(
    bigButtonListRepresentativeReducer,
    {
      datasetId: null,
      dataProviderId: null,
      historicReleasesDialogHeader: [],
      historicReleasesView: '',
      isHistoricReleasesDialogVisible: false
    }
  );

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

  const getDataHistoricReleases = (datasetId, value, dataProviderId) => {
    bigButtonListRepresentativeDispatch({
      type: 'GET_HISTORIC_RELEASE_DATASET_DATA',
      payload: { datasetId, value, dataProviderId }
    });
  };

  const onCloseHistoricReleasesDialogVisible = value => {
    bigButtonListRepresentativeDispatch({ type: 'ON_CLOSE_HISTORIC_RELEASE_DIALOG', payload: value });
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

  const onShowHistoricReleases = (typeView, value) => {
    bigButtonListRepresentativeDispatch({ type: 'ON_SHOW_HISTORIC_RELEASES', payload: { typeView, value } });
  };

  const renderDialogFooter = (
    <Fragment>
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => onCloseHistoricReleasesDialogVisible(false)}
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
              isLeadReporterOfCountry,
              match,
              onLoadReceiptData,
              onShowHistoricReleases,
              onShowSnapshotDialog
            }).map((button, i) => (button.visibility ? <BigButton key={i} {...button} /> : <Fragment key={i} />))}
          </div>
        </div>
      </div>

      <button ref={receiptBtnRef} style={{ display: 'none' }} />

      {bigButtonListRepresentativeState.isHistoricReleasesDialogVisible && (
        <Dialog
          className={styles.dialog}
          footer={renderDialogFooter}
          header={`${resources.messages['historicReleases']} ${bigButtonListRepresentativeState.historicReleasesDialogHeader}`}
          onHide={() => onCloseHistoricReleasesDialogVisible(false)}
          // style={{ width: '80%' }}
          visible={bigButtonListRepresentativeState.isHistoricReleasesDialogVisible}>
          <HistoricReleases
            datasetId={bigButtonListRepresentativeState.datasetId}
            dataProviderId={bigButtonListRepresentativeState.dataProviderId}
            historicReleasesView={bigButtonListRepresentativeState.historicReleasesView}
          />
        </Dialog>
      )}
    </>
  );
};
