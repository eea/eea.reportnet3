import { Fragment, useContext, useLayoutEffect, useRef, useState } from 'react';

import styles from './ApiKeyDialog.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { InputText } from 'views/_components/InputText';
import { Spinner } from 'views/_components/Spinner';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'services/DataflowService';
import { TooltipUtils } from 'views/_functions/Utils/TooltipUtils';

const ApiKeyDialog = ({ dataflowId, dataProviderId, isApiKeyDialogVisible, isCustodian, manageDialogs }) => {
  const resourcesContext = useContext(ResourcesContext);

  const inputTextRef = useRef(null);

  const [apiKey, setApiKey] = useState('');
  const [isKeyLoading, setIsKeyLoading] = useState(false);

  useLayoutEffect(() => {
    onGetApiKey();
  }, []);

  const onCloseDialog = () => manageDialogs('isApiKeyDialogVisible', false);

  const onCopyToClipboard = () => {
    navigator.clipboard.writeText(inputTextRef.current.value).then(
      () => console.log('Copied to clipboard.'),
      error => console.error('ApiKeyDialog - onCopyToClipboard.', error)
    );

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
      const { data } = await DataflowService.createApiKey(dataflowId, dataProviderId, isCustodian);
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
        label={resourcesContext.messages['generateApiKey']}
        onClick={onGenerateApiKey}
      />
      <Button
        className="p-button-secondary p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={onCloseDialog}
      />
    </Fragment>
  );

  const renderDialogContent = () => {
    if (isKeyLoading) return <Spinner style={{ top: 0, left: 0, width: '50px', height: '50px' }} />;

    if (apiKey === '') return <p>{resourcesContext.messages['noApiKey']}</p>;

    return (
      <Fragment>
        <label className={styles.label}>{resourcesContext.messages['apiKeyDialogLabel']}</label>

        <div className={styles.input_api}>
          <div className={styles.flex}>
            <InputText className={styles.inputText} id="apiKey" readOnly ref={inputTextRef} value={apiKey} />
            <label className="srOnly" htmlFor="apiKey">
              {apiKey}
            </label>
            <Button
              className={`p-button-primary ${styles.copyBtn}`}
              icon="copy"
              onClick={onCopyToClipboard}
              showDelay="3000"
              tooltip={resourcesContext.messages['copyToClipboardSuccess']}
              tooltipOptions={{ event: 'focus', hideDelay: 750, position: 'top' }}
            />
          </div>
          <p className={styles.ids_info}>
            <span className={styles.ids_label}>
              {resourcesContext.messages['dataflow']}: <strong>{dataflowId} </strong>
            </span>
            <span className={styles.ids_label} style={{ display: isCustodian ? 'none' : '' }}>
              {resourcesContext.messages['apiKeyDataProviderIdLabel']}: <strong>{dataProviderId}</strong>
            </span>
          </p>
        </div>
      </Fragment>
    );
  };

  return (
    <Dialog
      blockScroll={false}
      footer={footer}
      header={resourcesContext.messages['apiKeyDialogHead']}
      modal
      onHide={onCloseDialog}
      style={{ width: '80%', maxWidth: '650px' }}
      visible={isApiKeyDialogVisible}
      zIndex={3003}>
      <div className={styles.container}>
        <div className={styles.row}>{renderDialogContent()}</div>
      </div>
    </Dialog>
  );
};

export { ApiKeyDialog };
