import { useContext, useEffect, useState } from 'react';

import styles from './ApiKeyDialog.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { Spinner } from 'ui/views/_components/Spinner';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'core/services/Dataflow';

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

  const removeElementsByClass = className => {
    const elements = document.getElementsByClassName(className);
    while (elements.length > 0) {
      elements[0].parentNode.removeChild(elements[0]);
    }
  };

  const onCopyToClipboard = () => {
    const textArea = textAreaRef;
    textArea.select();
    document.execCommand('copy');
    window.getSelection().removeAllRanges();

    setTimeout(() => {
      removeElementsByClass('p-tooltip');
    }, 750);
  };

  const onGetApiKey = async () => {
    setIsKeyLoading(true);
    try {
      const { data } = await DataflowService.getApiKey(dataflowId, dataProviderId, isCustodian);
      setApiKey(data);
    } catch (error) {
      console.error('Error on getting Api key:', error);
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
      console.error('Error on generating Api key:', error);
    } finally {
      setIsKeyLoading(false);
    }
  };

  const footer = (
    <>
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
    </>
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
          <>
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
                  <label htmlFor="apiKey" className="srOnly">
                    {apiKey}
                  </label>
                  <Button
                    tooltip={resources.messages['copyToClipboardSuccess']}
                    tooltipOptions={{ event: 'focus', hideDelay: 750, position: 'top' }}
                    showDelay="3000"
                    className={`p-button-primary ${styles.copyBtn}`}
                    icon={'copy'}
                    onClick={() => onCopyToClipboard()}
                  />
                </div>
                <p className={styles.ids_info}>
                  <span className={styles.ids_label}>
                    {resources.messages['dataflow']}: <b>{dataflowId} </b>
                  </span>
                  <span className={styles.ids_label} style={{ display: isCustodian ? 'none' : '' }}>
                    {resources.messages['apiKeyDataProviderIdLabel']}:{' '}
                    <b>{representativeId ? representativeId : dataProviderId} </b>
                  </span>
                </p>
              </div>
            </div>
          </>
        )}
      </div>
    </Dialog>
  );
};

export { ApiKeyDialog };
