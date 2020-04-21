import React, { useContext, useEffect, useState } from 'react';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { Spinner } from 'ui/views/_components/Spinner';

import styles from './ApiKeyDialog.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'core/services/Dataflow';

const ApiKeyDialog = ({ dataflowId, dataProviderId, isVisibleApiKeyDialog, setIsVisibleApiKeyDialog }) => {
  const [apiKey, setApiKey] = useState('');
  const [isGenerating, setIsGenerating] = useState(false);

  const resources = useContext(ResourcesContext);

  useEffect(() => {
    onGetApiKey();
    return () => {
      setIsGenerating(false);
    };
  }, []);

  const onCloseDialog = () => {
    setIsVisibleApiKeyDialog(false);
  };

  const onCopy = () => {
    // TODO Copy to clipboard functionality
    // TODO message on growl confirming Copy to clipboard
    //
    console.log('COPY TO CLIPBOARD');
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
        label={resources.messages['generateKey']}
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
      blockScroll={false}
      closeOnEscape={true}
      footer={footer}
      header={resources.messages['copyRestAPIKey']}
      modal={true}
      onHide={() => onCloseDialog()}
      visible={isVisibleApiKeyDialog}
      zIndex={3003}>
      <div className={styles.container}>
        {isGenerating || apiKey === '' ? <Spinner /> : <p> {apiKey}</p>}
        <div>
          <Button
            icon={'copy'}
            className="p-button-secondary"
            label={resources.messages['copy']}
            onClick={() => onCopy()}
          />
        </div>
      </div>
    </Dialog>
  );
};

export { ApiKeyDialog };
