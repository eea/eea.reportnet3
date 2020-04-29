import React, { useContext, useEffect, useState } from 'react';

import styles from './ApiKeyDialog.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { Spinner } from 'ui/views/_components/Spinner';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'core/services/Dataflow';

const ApiKeyDialog = ({ dataflowId, dataProviderId, isApiKeyDialogVisible, manageDialogs }) => {
  const resources = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

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
    notificationContext.add({
      type: 'COPY_TO_CLIPBOARD_SUCCESS'
    });
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
        icon={'key'}
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
      {!isKeyLoading ? (
        <div className={styles.container}>
          {apiKey === '' ? (
            <p>{resources.messages['noApiKey']}</p>
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
                className={`p-button-primary ${styles.copyBtn}`}
                icon={'copy'}
                onClick={() => onCopyToClipboard()}
              />
            </>
          )}
        </div>
      ) : (
        <div className={styles.container}>
          <Spinner style={{ top: 0, left: 0, width: '50px', height: '50px' }} />
        </div>
      )}
    </Dialog>
  );
};

export { ApiKeyDialog };
