import { forwardRef, useContext, useEffect, useImperativeHandle, useRef, useState } from 'react';

import isNil from 'lodash/isNil';

import styles from './ManageReportingDataflowForm.module.scss';

import { Button } from 'views/_components/Button';
import { ErrorMessage } from 'views/_components/ErrorMessage';

import { DataflowService } from 'services/DataflowService';
import { UserService } from 'services/UserService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

const ManageReportingDataflowForm = forwardRef(
  ({ data, dataflowId, getData, isEditForm, onCreate, onEdit, onResetData, onSearch, onSubmit, refresh }, ref) => {
    const notificationContext = useContext(NotificationContext);
    const resources = useContext(ResourcesContext);
    const userContext = useContext(UserContext);

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
    }, [refresh]);

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

    const onConfirm = async pinned => {
      checkIsCorrectInputValue(data.obligation.title, 'obligation');
      checkIsCorrectInputValue(name, 'name');
      checkIsCorrectInputValue(description, 'description');

      if (!errors.obligation.hasErrors && !errors.name.hasErrors && !errors.description.hasErrors) {
        onSubmit(true);

        try {
          if (isEditForm) {
            await DataflowService.update(
              dataflowId,
              name,
              description,
              data.obligation.id,
              data.isReleasable,
              data.showPublicInfo
            );
            onEdit(name, description, data.obligation.id);
          } else {
            const creationResponse = await DataflowService.create(name, description, data.obligation.id);
            if (pinned) {
              const inmUserProperties = { ...userContext.userProps };
              inmUserProperties.pinnedDataflows.push(creationResponse.data.toString());
              await UserService.updateConfiguration(inmUserProperties);
              userContext.onChangePinnedDataflows(inmUserProperties.pinnedDataflows);
            }
            onCreate('isAddDialogVisible');
            onResetData();
          }
        } catch (error) {
          console.error('ManageReportingDataflowForm - onConfirm.', error);

          if (error?.response?.data === 'Dataflow name already exists') {
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
        <fieldset className={styles.fieldset}>
          <div className={`formField ${errors.name.hasErrors ? 'error' : ''}`}>
            <input
              autoComplete="off"
              id="dataflowName"
              name="name"
              onBlur={() => checkIsCorrectInputValue(name, 'name')}
              onChange={event => {
                getData({ ...data, name: event.target.value });
                setName(event.target.value);
              }}
              onFocus={() => {
                setErrors(previousErrors => {
                  return { ...previousErrors, name: { message: '', hasErrors: false } };
                });
              }}
              onKeyPress={e => {
                if (e.key === 'Enter' && !checkIsCorrectInputValue(name, 'name')) onConfirm();
              }}
              placeholder={resources.messages['createDataflowName']}
              ref={inputRef}
              type="text"
              value={name}
            />
            <label className="srOnly" htmlFor="dataflowName">
              {resources.messages['createDataflowName']}
            </label>
            {errors.name.message !== '' && <ErrorMessage message={errors.name.message} />}
          </div>

          <div className={`formField ${errors.description.hasErrors ? 'error' : ''}`}>
            <textarea
              autoComplete="off"
              component="textarea"
              id="dataflowDescription"
              name="description"
              onBlur={() => checkIsCorrectInputValue(description, 'description')}
              onChange={event => {
                getData({ ...data, description: event.target.value });
                setDescription(event.target.value);
              }}
              onFocus={() => {
                setErrors(previousErrors => {
                  return { ...previousErrors, description: { message: '', hasErrors: false } };
                });
              }}
              placeholder={resources.messages['createDataflowDescription']}
              rows={10}
              value={description}
            />
            <label className="srOnly" htmlFor="dataflowDescription">
              {resources.messages['createDataflowDescription']}
            </label>
            {errors.description.message !== '' && <ErrorMessage message={errors.description.message} />}
          </div>

          <div className={`${styles.search}`}>
            <Button icon="search" label={resources.messages['searchObligations']} onClick={onSearch} />
            <input
              className={`${styles.searchInput} ${errors.obligation.hasErrors ? styles.searchErrors : ''}`}
              id="searchObligation"
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
            <label className="srOnly" htmlFor="searchObligation">
              {resources.messages['searchObligations']}
            </label>
          </div>
        </fieldset>
      </form>
    );
  }
);

export { ManageReportingDataflowForm };
