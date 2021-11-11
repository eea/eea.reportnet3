import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import isNil from 'lodash/isNil';

import styles from '../BigButtonList/BigButtonList.module.scss';

import { BigButton } from 'views/_components/BigButton';
import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { DownloadFile } from 'views/_components/DownloadFile';
import { HistoricReleases } from 'views/Dataflow/_components/HistoricReleases';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { ConfirmationReceiptService } from 'services/ConfirmationReceiptService';

import { bigButtonListRepresentativeReducer } from './_functions/Reducers/bigButtonListRepresentativeReducer';

import { useBigButtonListRepresentative } from './_functions/Hooks/useBigButtonListRepresentative';

export const BigButtonListRepresentative = ({
  dataflowState,
  dataProviderId,
  handleRedirect,
  isLeadReporterOfCountry,
  manageDialogs,
  match,
  onCleanUpReceipt,
  onOpenReleaseConfirmDialog,
  setIsReceiptLoading,
  uniqRepresentatives
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

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
      notificationContext.add({ type: 'LOAD_RECEIPT_DATA_ERROR' }, true);
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
      label={resourcesContext.messages['close']}
      onClick={() => onCloseHistoricReleasesDialogVisible(false)}
    />
  );

  return (
    <Fragment>
      <div className={styles.buttonsWrapper}>
        <div className={styles.splitButtonWrapper}>
          <div className={styles.datasetItem}>
            {useBigButtonListRepresentative({
              dataflowState,
              dataProviderId,
              getDataHistoricReleases,
              handleRedirect,
              isLeadReporterOfCountry,
              match,
              onLoadReceiptData,
              onOpenReleaseConfirmDialog,
              onShowHistoricReleases,
              uniqRepresentatives
            })
              .filter(button => button.visibility)
              .map(button => (
                <BigButton key={button.caption} manageDialogs={manageDialogs} {...button} />
              ))}
          </div>
        </div>
      </div>

      <button ref={receiptBtnRef} style={{ display: 'none' }} />

      {bigButtonListRepresentativeState.isHistoricReleasesDialogVisible && (
        <Dialog
          className={styles.dialog}
          footer={renderDialogFooter}
          header={`${resourcesContext.messages['historicReleases']} ${bigButtonListRepresentativeState.historicReleasesDialogHeader}`}
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
