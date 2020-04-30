import React, { useContext, useEffect, useState } from 'react';

import styles from './ApiKeyDialog.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { Spinner } from 'ui/views/_components/Spinner';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'core/services/Dataflow';

const ApiKeyDialog = ({ dataflowId, dataProviderId, isApiKeyDialogVisible, onManageDialogs }) => {
  const resources = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [apiKey, setApiKey] = useState('');
  const [isKeyLoading, setIsKeyLoading] = useState(false);
  const [textAreaRef, setTextAreaRef] = useState(null);

  useEffect(() => {
    onGetApiKey();
  }, []);

  const onCloseDialog = () => onManageDialogs('isApiKeyDialogVisible', false);

  const onCopyToClipboard = () => {
    const textArea = textAreaRef;
    textArea.select();
    document.execCommand('copy');
    window.getSelection().removeAllRanges();
  };

  const onGetApiKey = async () => {
    setIsKeyLoading(true);
    try {
      const responseApiKey = await DataflowService.getApiKey(dataflowId, dataProviderId);
      setApiKey(responseApiKey);
    } catch (error) {
      console.error('Error on getting Api key:', error);
    } finally {
      setIsKeyLoading(false);
    }
  };

  const onGenerateApiKey = async () => {
    setIsKeyLoading(true);

    try {
      const responseApiKey = await DataflowService.generateApiKey(dataflowId, dataProviderId);
      setApiKey(responseApiKey);
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
        className="p-button-secondary"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => onCloseDialog()}
      />
    </>
  );

  return (
    <Dialog
      blockScroll={false}
      closeOnEscape={true}
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
            <Spinner style={{ top: 0, left: 0, width: '50px', height: '50px' }} />
          ) : (
            <p>{resources.messages['noApiKey']}</p>
          )
        ) : (
          <>
            <label>{resources.messages['apiKeyDialogLabel']}</label>
            <textarea
              className={styles.textarea}
              readOnly
              ref={textRef => setTextAreaRef(textRef)}
              rows={1}
              value={apiKey}
            />
            <Button
              tooltip={resources.messages['copyToClipboardSuccess']}
              tooltipOptions={{ event: 'focus', hideDelay: 750, position: 'top' }}
              showDelay="3000"
              className={`p-button-primary ${styles.copyBtn}`}
              icon={'copy'}
              onClick={() => onCopyToClipboard()}
            />
          </>
        )}
      </div>
    </Dialog>
  );
};

export { ApiKeyDialog };
