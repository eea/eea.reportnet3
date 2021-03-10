import React, { forwardRef, useContext, useEffect, useImperativeHandle, useRef, useState } from 'react';

import isNil from 'lodash/isNil';

import styles from './DataflowManagementForm.module.scss';

import DataflowConf from 'conf/dataflow.config.json';

import { Button } from 'ui/views/_components/Button';
import { ErrorMessage } from 'ui/views/_components/ErrorMessage';

import { DataflowService } from 'core/services/Dataflow';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DataflowManagementForm = forwardRef(
  ({ data, dataflowId, getData, isEditForm, onCreate, onEdit, onSearch, onSubmit, refresh }, ref) => {
    const notificationContext = useContext(NotificationContext);
    const resources = useContext(ResourcesContext);

    const [description, setDescription] = useState(data.description);
    const [errors, setErrors] = useState({
      description: { message: '', hasErrors: false },
      name: { message: '', hasErrors: false },
      obligation: { message: '', hasErrors: false }
    });
    const [name, setName] = useState(data.name);

    const form = useRef(null);
    const inputRef = useRef(null);

    useEffect(() => {
      if (!isNil(inputRef) && refresh) inputRef.current.focus();
    }, [inputRef.current, refresh]);

    useImperativeHandle(ref, () => ({
      handleSubmit: onConfirm
    }));

    const checkIsCorrectLength = inputValue => inputValue.length <= 255;

    const checkIsEmptyInput = inputValue => inputValue.trim() === '';

    const checkIsCorrectInputValue = (inputValue, inputName) => {
      let hasErrors = false;
      let message = '';

      if (checkIsEmptyInput(inputValue)) {
        message = '';
        hasErrors = true;
      } else if (inputName === 'description' && !checkIsCorrectLength(inputValue)) {
        message = resources.messages['dataflowDescriptionValidationMax'];
        hasErrors = true;
      } else if (inputName === 'name' && !checkIsCorrectLength(inputValue)) {
        message = resources.messages['dataflowNameValidationMax'];
        hasErrors = true;
      }

      setErrors(previousErrors => {
        return { ...previousErrors, [inputName]: { message, hasErrors } };
      });

      return hasErrors;
    };

    const onConfirm = async () => {
      if (
        !checkIsCorrectInputValue(data.obligation.title, 'obligation') &&
        !checkIsCorrectInputValue(name, 'name') &&
        !checkIsCorrectInputValue(description, 'description')
      ) {
        onSubmit(true);

        try {
          if (isEditForm) {
            await DataflowService.update(dataflowId, name, description, data.obligation.id, data.isReleasable);
            onEdit(name, description, data.obligation.id);
          } else {
            await DataflowService.create(name, description, data.obligation.id);
            onCreate();
          }
        } catch (error) {
          console.log('error', error);
          if (error?.response?.data === DataflowConf.errorTypes['dataflowExists']) {
            setErrors(previousErrors => {
              return {
                ...previousErrors,
                name: { message: resources.messages['dataflowNameExists'], hasErrors: true }
              };
            });
            notificationContext.add({ type: 'DATAFLOW_NAME_EXISTS' });
          } else {
            const notification = isEditForm
              ? { type: 'DATAFLOW_UPDATING_ERROR', content: { dataflowId: data.id, dataflowName: name } }
              : { type: 'DATAFLOW_CREATION_ERROR', content: { dataflowName: name } };

            notificationContext.add(notification);
          }
        } finally {
          onSubmit(false);
        }
      }
    };

    return (
      <form ref={form}>
        <fieldset>
          <div className={`formField ${errors.name.hasErrors ? 'error' : ''}`}>
            <input
              autoComplete="off"
              id="dataflowName"
              ref={inputRef}
              onBlur={() => checkIsCorrectInputValue(name, 'name')}
              onKeyPress={e => {
                if (e.key === 'Enter' && !checkIsCorrectInputValue(name, 'name')) onConfirm();
              }}
              name="name"
              placeholder={resources.messages['createDataflowName']}
              onChange={event => {
                getData({ ...data, name: event.target.value });
                setName(event.target.value);
              }}
              type="text"
              value={name}
            />
            <label htmlFor="dataflowName" className="srOnly">
              {resources.messages['createDataflowName']}
            </label>
            {errors.name.message !== '' && <ErrorMessage message={errors.name.message} />}
          </div>

          <div className={`formField ${errors.description.hasErrors ? 'error' : ''}`}>
            <textarea
              autoComplete="off"
              id="dataflowDescription"
              name="description"
              onBlur={() => checkIsCorrectInputValue(description, 'description')}
              component="textarea"
              rows={10}
              onChange={event => {
                getData({ ...data, description: event.target.value });
                setDescription(event.target.value);
              }}
              placeholder={resources.messages['createDataflowDescription']}
              value={description}
            />
            <label htmlFor="dataflowDescription" className="srOnly">
              {resources.messages['createDataflowDescription']}
            </label>
            {errors.description.message !== '' && <ErrorMessage message={errors.description.message} />}
          </div>

          <div className={`${styles.search}`}>
            <Button icon="search" label={resources.messages['searchObligations']} onMouseDown={onSearch} />
            <input
              id="searchObligation"
              className={`${styles.searchInput} ${errors.obligation.hasErrors ? styles.searchErrors : ''}`}
              name="obligation.title"
              onBlur={() => checkIsCorrectInputValue(data.obligation.title, 'obligation')}
              onKeyPress={e => {
                if (e.key === 'Enter' && !checkIsCorrectInputValue(data.obligation.title, 'obligation')) onConfirm();
              }}
              placeholder={resources.messages['associatedObligation']}
              readOnly={true}
              type="text"
              value={data.obligation.title}
            />
            <label htmlFor="searchObligation" className="srOnly">
              {resources.messages['searchObligations']}
            </label>
          </div>
        </fieldset>
      </form>
    );
  }
);

export { DataflowManagementForm };
