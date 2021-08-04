import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import isNil from 'lodash/isNil';

import styles from '../BigButtonList/BigButtonList.module.scss';

import { BigButton } from 'ui/views/_components/BigButton';
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
  dataProviderId,
  handleRedirect,
  match,
  onCleanUpReceipt,
  onOpenReleaseConfirmDialog,
  setIsReceiptLoading,
  uniqRepresentatives
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
      const response = await ConfirmationReceiptService.download(dataflowState.id, match.params.representativeId);
      downloadPdf(response.data);
      onCleanUpReceipt();
    } catch (error) {
      console.error('BigButtonListRepresentative - onLoadReceiptData.', error);
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
    <Button
      className="p-button-secondary p-button-animated-blink p-button-right-aligned"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => onCloseHistoricReleasesDialogVisible(false)}
    />
  );

  return (
    <Fragment>
      <div className={styles.buttonsWrapper}>
        <div className={styles.splitButtonWrapper}>
          <div className={styles.datasetItem}>
            {useBigButtonList({
              dataflowState,
              dataProviderId,
              getDataHistoricReleases,
              handleRedirect,
              match,
              onLoadReceiptData,
              onOpenReleaseConfirmDialog,
              onShowHistoricReleases,
              uniqRepresentatives
            })
              .filter(button => button.visibility)
              .map(button => (
                <BigButton key={button.caption} {...button} />
              ))}
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
          visible={bigButtonListRepresentativeState.isHistoricReleasesDialogVisible}>
          <HistoricReleases
            dataProviderId={bigButtonListRepresentativeState.dataProviderId}
            datasetId={bigButtonListRepresentativeState.datasetId}
            historicReleasesView={bigButtonListRepresentativeState.historicReleasesView}
          />
        </Dialog>
      )}
    </Fragment>
  );
};
