import { useContext, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import styles from './DatasetsForm.module.scss';

import { Button } from 'views/_components/Button';
import { ErrorMessage } from 'views/_components/ErrorMessage';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const DatasetsForm = ({ getDatasetData }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [errors, setErrors] = useState({ datasetId: '', dataProviderId: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [datasetsFormError, setDatasetsFormError] = useState();
  const [dataProviderId, setDataProviderId] = useState('');
  const [datasetId, setDatasetId] = useState('');

  const checkIsEmptyDatasetId = () =>
    datasetId.length === 0
      ? setErrors(previousErrors => {
          return { ...previousErrors, datasetId: resourcesContext.messages['datasetsFormDatasetIdError'] };
        })
      : setErrors(previousErrors => {
          return { ...previousErrors, datasetId: '' };
        });

  const checkIsEmptyProviderId = () =>
    dataProviderId.length === 0
      ? setErrors(previousErrors => {
          return { ...previousErrors, dataProviderId: resourcesContext.messages['datasetsFormProviderIdError'] };
        })
      : setErrors(previousErrors => {
          return { ...previousErrors, dataProviderId: '' };
        });

  const checkIsNumericDatasetId = () =>
    /\D/.test(datasetId)
      ? setErrors(previousErrors => {
          return { ...previousErrors, datasetId: resourcesContext.messages['datasetsFormNonNumericDatasetIdError'] };
        })
      : setErrors(previousErrors => {
          return { ...previousErrors, datasetId: '' };
        });

  const checkIsNumericProviderId = () =>
    /\D/.test(dataProviderId)
      ? setErrors(previousErrors => {
          return {
            ...previousErrors,
            dataProviderId: resourcesContext.messages['datasetsFormNonNumericProviderIdError']
          };
        })
      : setErrors(previousErrors => {
          return { ...previousErrors, dataProviderId: '' };
        });

  const isValidDatasetId = () => {
    checkIsEmptyDatasetId();
    if (!(datasetId === '')) {
      checkIsNumericDatasetId();
    }
  };

  const isValidProviderId = () => {
    checkIsEmptyProviderId();
    if (!(dataProviderId === '')) {
      checkIsNumericProviderId();
    }
  };

  const checkInputs = () => {
    isValidDatasetId();
    isValidProviderId();
    return errors.datasetId === '' && errors.dataProviderId === '';
  };

  const onGetDatasetData = async () => {
    if (checkInputs()) {
      setIsSubmitting(true);
      try {
        await getDatasetData(datasetId, dataProviderId);
      } catch (error) {
        console.error('DatasetsForm - onGetDatasetData.', error);
        notificationContext.add(
          {
            type: 'DATASETS_FORM_ERROR',
            content: {}
          },
          true
        );
        const errorResponse = error.response;
        if (!isUndefined(errorResponse) && errorResponse.status === 500) {
          setDatasetsFormError(resourcesContext.messages['datasetsFormError']);
        }
      } finally {
        setIsSubmitting(false);
        setDatasetId('');
        setDataProviderId('');
      }
    }
  };

  const isFirefox = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;

  return (
    <div className={`${styles.boxContainer}`}>
      <div className={`${styles.box}`}>
        <div className={styles.header}>
          <h1>{resourcesContext.messages['searchForDataset']}</h1>
          {!isEmpty(datasetsFormError) && <div className={styles.error}>{datasetsFormError}</div>}
        </div>
        <form>
          <fieldset>
            <label htmlFor="datasetId">{resourcesContext.messages['datasetId']}</label>
            <input
              className={errors.datasetId !== '' ? styles.hasErrors : null}
              id="datasetId"
              name="datasetId"
              onBlur={isValidDatasetId}
              onChange={e => {
                setDatasetId(e.target.value);
                setDatasetsFormError('');
              }}
              onFocus={() =>
                setErrors(previousErrors => {
                  return { ...previousErrors, datasetId: '' };
                })
              }
              placeholder={resourcesContext.messages['datasetId']}
              type="text"
              value={datasetId}
            />
            {errors['datasetId'] !== '' && <ErrorMessage message={errors['datasetId']} />}
          </fieldset>

          <fieldset>
            <label htmlFor="dataProviderId">{resourcesContext.messages['providerId']}</label>
            <input
              autoComplete="dataProviderId"
              className={errors.dataProviderId !== '' ? styles.hasErrors : null}
              id="dataProviderId"
              name="dataProviderId"
              onBlur={isValidProviderId}
              onChange={e => {
                setDataProviderId(e.target.value);
                setDatasetsFormError('');
              }}
              onFocus={() =>
                setErrors(previousErrors => {
                  return { ...previousErrors, dataProviderId: '' };
                })
              }
              placeholder={resourcesContext.messages['providerId']}
              type="text"
              value={dataProviderId}
            />
            {errors['dataProviderId'] !== '' && <ErrorMessage message={errors['dataProviderId']} />}
          </fieldset>

          <fieldset className={`${styles.buttonHolder}`}>
            <Button
              className="rp-btn primary"
              disabled={isSubmitting || errors['dataProviderId'] !== '' || errors['datasetId'] !== ''}
              id="buttonDatasetForm"
              label={resourcesContext.messages['findDataset']}
              onClick={onGetDatasetData}
              type={isFirefox ? 'button' : 'submit'}
            />
          </fieldset>
        </form>
      </div>
    </div>
  );
};
