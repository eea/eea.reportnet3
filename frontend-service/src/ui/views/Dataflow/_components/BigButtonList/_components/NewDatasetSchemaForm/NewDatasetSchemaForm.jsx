import { useContext, useEffect, useRef, useState } from 'react';

import isNil from 'lodash/isNil';

import styles from './NewDatasetSchemaForm.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ErrorMessage } from 'ui/views/_components/ErrorMessage';

import { DataflowService } from 'core/services/Dataflow';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { MetadataUtils, TextUtils } from 'ui/views/_functions/Utils';

const NewDatasetSchemaForm = ({ dataflowId, datasetSchemaInfo, onCreate, onUpdateData, setNewDatasetDialog }) => {
  const { hideLoading, showLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const validCharsRegex = new RegExp(/[a-zA-Z0-9_-\s()]/);
  const invalidCharsRegex = new RegExp(/[^a-zA-Z0-9_-\s()]/);

  const [datasetSchemaName, setDatasetSchemaName] = useState('');
  const [errorMessage, setErrorMessage] = useState({ datasetSchemaName: '' });
  const [hasErrors, setHasErrors] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const inputRef = useRef(null);

  useEffect(() => {
    if (!isNil(inputRef.current)) inputRef.current.focus();
  }, []);

  const checkIsInvalidName = () => {
    if (invalidCharsRegex.test(inputRef.current.value)) {
      setErrorMessage({ datasetSchemaName: resources.messages['invalidCharactersSchemaError'] });
      return true;
    }
    return false;
  };

  const checkIsDuplicateSchemaName = () => {
    const isDuplicatedName = datasetSchemaInfo.some(schema =>
      TextUtils.areEquals(schema.schemaName, datasetSchemaName)
    );

    if (isDuplicatedName) {
      setErrorMessage({ datasetSchemaName: resources.messages['duplicateSchemaError'] });
    }
    return isDuplicatedName;
  };

  const checkIsEmptyInput = () => {
    if (datasetSchemaName.trim() === '') {
      setErrorMessage({ datasetSchemaName: '' });
      return true;
    }
    return false;
  };

  const checkInput = () => {
    setHasErrors(checkIsDuplicateSchemaName() || checkIsEmptyInput() || checkIsInvalidName());
    return !checkIsDuplicateSchemaName() && !checkIsEmptyInput() && !checkIsInvalidName();
  };

  const onConfirm = async event => {
    event.preventDefault();

    if (checkInput()) {
      setIsSubmitting(true);
      showLoading();
      try {
        const response = await DataflowService.newEmptyDatasetSchema(dataflowId, encodeURIComponent(datasetSchemaName));
        if (response.status >= 200 && response.status <= 299) {
          onUpdateData();
          setIsSubmitting(false);
        } else {
          throw new Error('Schema creation error');
        }
        onCreate();
      } catch (error) {
        const metadata = await MetadataUtils.getMetadata({ dataflowId });
        const {
          dataflow: { name: dataflowName }
        } = metadata;

        if (error.response?.data?.message?.includes('duplicated')) {
          notificationContext.add({
            type: 'DATASET_SCHEMA_CREATION_ERROR_DUPLICATED',
            content: { schemaName: datasetSchemaName }
          });
        } else {
          notificationContext.add({
            type: 'DATASET_SCHEMA_CREATION_ERROR',
            content: {
              dataflowId,
              dataflowName
            }
          });
          onCreate();
        }
      } finally {
        setIsSubmitting(false);
        hideLoading();
      }
    }
  };

  return (
    <form>
      <fieldset>
        <input
          className={`formField ${hasErrors ? styles.hasErrors : ''}`}
          id={'datasetSchemaName'}
          maxLength={250}
          name="datasetSchemaName"
          onBlur={() => checkInput()}
          onChange={e => setDatasetSchemaName(e.target.value)}
          onKeyPress={e => {
            if (e.key === 'Enter') onConfirm(e);
            else if (!validCharsRegex.test(e.key) || e.key === 'Dead') {
              e.preventDefault();
              return false;
            }
          }}
          placeholder={resources.messages['createDatasetSchemaName']}
          ref={inputRef}
          type="text"
          value={datasetSchemaName}
        />
        <label htmlFor="datasetSchemaName" className="srOnly">
          {resources.messages['createDatasetSchemaName']}
        </label>
        {errorMessage['datasetSchemaName'] !== '' && <ErrorMessage message={errorMessage['datasetSchemaName']} />}
      </fieldset>

      <fieldset>
        <div className={`${styles.buttonWrap} ui-dialog-buttonpane p-clearfix`}>
          <Button
            className="p-button-primary"
            disabled={isSubmitting}
            icon="add"
            label={resources.messages['create']}
            onClick={e => onConfirm(e)}
            type="subscribe"
          />
          <Button
            className={`${styles.cancelButton} p-button-secondary button-right-aligned`}
            icon="cancel"
            label={resources.messages['cancel']}
            onClick={() => setNewDatasetDialog(false)}
          />
        </div>
      </fieldset>
    </form>
  );
};

export { NewDatasetSchemaForm };
