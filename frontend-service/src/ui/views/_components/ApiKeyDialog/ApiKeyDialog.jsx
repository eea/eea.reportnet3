import { Fragment, useContext, useEffect, useState } from 'react';

import styles from './ApiKeyDialog.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { Spinner } from 'ui/views/_components/Spinner';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'core/services/Dataflow';
import { TooltipUtils } from 'ui/views/_functions/Utils/TooltipUtils';

const ApiKeyDialog = ({
  dataflowId,
  dataProviderId,
  isApiKeyDialogVisible,
  isCustodian,
  manageDialogs,
  match: {
    params: { representativeId }
  }
}) => {
  const resources = useContext(ResourcesContext);

  const [apiKey, setApiKey] = useState('');
  const [isKeyLoading, setIsKeyLoading] = useState(false);
  const [textAreaRef, setTextAreaRef] = useState(null);

  useEffect(() => {
    onGetApiKey();
  }, []);

  const onCloseDialog = () => manageDialogs('isApiKeyDialogVisible', false);

  const onCopyToClipboard = () => {
    const textArea = textAreaRef;
    textArea.select();
    document.execCommand('copy');
    window.getSelection().removeAllRanges();

    setTimeout(() => {
      TooltipUtils.removeElementsByClass('p-tooltip');
    }, 750);
  };

  const onGetApiKey = async () => {
    setIsKeyLoading(true);
    try {
      const { data } = await DataflowService.getApiKey(dataflowId, dataProviderId, isCustodian);
      setApiKey(data);
    } catch (error) {
      console.error('ApiKeyDialog - onGetApiKey.', error);
    } finally {
      setIsKeyLoading(false);
    }
  };

  const onGenerateApiKey = async () => {
    setIsKeyLoading(true);
    try {
      const { data } = await DataflowService.generateApiKey(dataflowId, dataProviderId, isCustodian);
      setApiKey(data);
    } catch (error) {
      console.error('ApiKeyDialog - onGenerateApiKey.', error);
    } finally {
      setIsKeyLoading(false);
    }
  };

  const footer = (
    <Fragment>
      <Button
        className="p-button-primary"
        disabled={isKeyLoading}
        icon={isKeyLoading ? 'spinnerAnimate' : 'key'}
        label={resources.messages['generateApiKey']}
        onClick={() => onGenerateApiKey()}
      />
      <Button
        className="p-button-secondary p-button-right-aligned"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => onCloseDialog()}
      />
    </Fragment>
  );

  return (
    <Dialog
      blockScroll={false}
      footer={footer}
      header={resources.messages['apiKeyDialogHead']}
      modal={true}
      onHide={() => onCloseDialog()}
      style={{ width: '80%', maxWidth: '650px' }}
      visible={isApiKeyDialogVisible}
      zIndex={3003}>
      <div className={styles.container}>
        {apiKey === '' ? (
          isKeyLoading ? (
            <div className={styles.row}>
              <Spinner style={{ top: 0, left: 0, width: '50px', height: '50px' }} />
            </div>
          ) : (
            <div className={styles.row}>
              <p>{resources.messages['noApiKey']}</p>
            </div>
          )
        ) : (
          <div className={styles.row}>
            <label className={styles.label}>{resources.messages['apiKeyDialogLabel']}</label>

            <div className={styles.input_api}>
              <div className={styles.flex}>
                <textarea
                  className={styles.textarea}
                  id="apiKey"
                  readOnly
                  ref={textRef => setTextAreaRef(textRef)}
                  rows={1}
                  value={apiKey}
                />
                <label className="srOnly" htmlFor="apiKey">
                  {apiKey}
                </label>
                <Button
                  className={`p-button-primary ${styles.copyBtn}`}
                  icon={'copy'}
                  onClick={() => onCopyToClipboard()}
                  showDelay="3000"
                  tooltip={resources.messages['copyToClipboardSuccess']}
                  tooltipOptions={{ event: 'focus', hideDelay: 750, position: 'top' }}
                />
              </div>
              <p className={styles.ids_info}>
                <span className={styles.ids_label}>
                  {resources.messages['dataflow']}: <strong>{dataflowId} </strong>
                </span>
                <span className={styles.ids_label} style={{ display: isCustodian ? 'none' : '' }}>
                  {resources.messages['apiKeyDataProviderIdLabel']}:{' '}
                  <strong>{representativeId ? representativeId : dataProviderId} </strong>
                </span>
              </p>
            </div>
          </div>
        )}
      </div>
    </Dialog>
  );
};

export { ApiKeyDialog };
