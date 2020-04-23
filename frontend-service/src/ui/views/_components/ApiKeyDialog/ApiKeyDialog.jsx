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
  const [isGenerating, setIsGenerating] = useState(false);
  const [textAreaRef, setTextAreaRef] = useState(null);

  useEffect(() => {
    onGetApiKey();
    return () => {
      setIsGenerating(false);
    };
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
    try {
      const responseApiKey = await DataflowService.getApiKey(dataflowId, dataProviderId);
      setApiKey(responseApiKey);
    } catch (error) {
      console.error('Error on getting Api key:', error);
    }
  };

  const onGenerateApiKey = async () => {
    setIsGenerating(true);

    try {
      const responseApiKey = await DataflowService.generateApiKey(dataflowId, dataProviderId);
      setApiKey(responseApiKey);
    } catch (error) {
      console.error('Error on generating Api key:', error);
    } finally {
      setIsGenerating(false);
    }
  };

  const footer = (
    <>
      <Button
        icon={'key'}
        className="p-button-primary"
        label={resources.messages['generateApiKey']}
        onClick={() => onGenerateApiKey()}
        disabled={isGenerating}
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
      style={{ width: '80%', maxWidth: '650px' }}
      blockScroll={false}
      closeOnEscape={true}
      footer={footer}
      header={resources.messages['apiKeyDialogHead']}
      modal={true}
      onHide={() => onCloseDialog()}
      visible={isApiKeyDialogVisible}
      zIndex={3003}>
      {!isGenerating ? (
        <div className={styles.container}>
          {apiKey === '' ? (
            <p>{resources.messages['noApiKey']}</p>
          ) : (
            <>
              <textarea
                className={styles.textarea}
                ref={thisEl => setTextAreaRef(thisEl)}
                value={apiKey}
                rows={1}
                readOnly
              />
              <div>
                <Button
                  icon={'copy'}
                  className={`p-button-secondary ${styles.copyBtn}`}
                  label={resources.messages['copyApiKeyBtn']}
                  onClick={() => onCopyToClipboard()}
                />
              </div>
            </>
          )}
        </div>
      ) : (
        <div className={styles.container}>
          <Spinner style={{ top: 0, left: 0 }} />
        </div>
      )}
    </Dialog>
  );
};

export { ApiKeyDialog };
