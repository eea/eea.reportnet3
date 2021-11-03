import { forwardRef, useContext, useImperativeHandle, useRef, useState } from 'react';

import styles from './ManageDataflowForm.module.scss';

import { config } from 'conf';

import { Button } from 'views/_components/Button';
import { CharacterCounter } from 'views/_components/CharacterCounter';
import { ErrorMessage } from 'views/_components/ErrorMessage';
import { InputText } from 'views/_components/InputText';

import { useInputTextFocus } from 'views/_functions/Hooks/useInputTextFocus';

import { DataflowService } from 'services/DataflowService';
import { CitizenScienceDataflowService } from 'services/CitizenScienceDataflowService';
import { UserService } from 'services/UserService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

const ManageDataflowForm = forwardRef(
  (
    {
      data,
      dataflowId,
      dialogName,
      getData,
      isCitizenScienceDataflow,
      isEditForm,
      onCreate,
      onEdit,
      onResetData,
      onSearch,
      onSubmit,
      refresh
    },
    ref
  ) => {
    const notificationContext = useContext(NotificationContext);
    const resourcesContext = useContext(ResourcesContext);
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

    useImperativeHandle(ref, () => ({
      handleSubmit: onConfirm
    }));

    useInputTextFocus(refresh, inputRef);

    const checkIsCorrectLength = inputValue => inputValue.length <= config.INPUT_MAX_LENGTH;

    const checkIsEmptyInput = inputValue => inputValue.trim() === '';

    const checkIsCorrectInputValue = (inputValue, inputName) => {
      let hasErrors = false;
      let message = '';

      if (checkIsEmptyInput(inputValue)) {
        message = '';
        hasErrors = true;
      } else if (inputName === 'description' && !checkIsCorrectLength(inputValue)) {
        message = `${resourcesContext.messages['dataflowDescriptionValidationMax']} (${resourcesContext.messages['maxAllowedCharacters']} ${config.INPUT_MAX_LENGTH})`;
        hasErrors = true;
      } else if (inputName === 'name' && !checkIsCorrectLength(inputValue)) {
        message = `${resourcesContext.messages['dataflowNameValidationMax']} (${resourcesContext.messages['maxAllowedCharacters']} ${config.INPUT_MAX_LENGTH})`;
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
          const service = isCitizenScienceDataflow ? CitizenScienceDataflowService : DataflowService;

          if (isEditForm) {
            await service.update(
              dataflowId,
              name,
              description,
              data.obligation.id,
              data.isReleasable,
              data.showPublicInfo
            );

            onEdit(name, description, data.obligation.id);
          } else {
            const creationResponse = await service.create(name, description, data.obligation.id);

            if (pinned) {
              const inmUserProperties = { ...userContext.userProps };
              inmUserProperties.pinnedDataflows.push(creationResponse.data.toString());
              await UserService.updateConfiguration(inmUserProperties);
              userContext.onChangePinnedDataflows(inmUserProperties.pinnedDataflows);
            }
            onCreate(dialogName);
            onResetData();
          }
        } catch (error) {
          console.error('ManageDataflowForm - onConfirm.', error);

          if (error?.response?.data === 'Dataflow name already exists') {
            setErrors(previousErrors => {
              return {
                ...previousErrors,
                name: { message: resourcesContext.messages['dataflowNameExists'], hasErrors: true }
              };
            });
            notificationContext.add({ type: 'DATAFLOW_NAME_EXISTS' }, true);
          } else {
            const notification = isEditForm
              ? { type: 'DATAFLOW_UPDATING_ERROR', content: { dataflowId: data.id, dataflowName: name } }
              : { type: 'DATAFLOW_CREATION_ERROR', content: { dataflowName: name } };

            notificationContext.add(notification, true);
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
            <InputText
              autoComplete="off"
              hasMaxCharCounter={true}
              id="dataflowName"
              maxLength={config.INPUT_MAX_LENGTH}
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
              placeholder={resourcesContext.messages['createDataflowName']}
              ref={inputRef}
              type="text"
              value={name}
            />
            <label className="srOnly" htmlFor="dataflowName">
              {resourcesContext.messages['createDataflowName']}
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
              placeholder={resourcesContext.messages['createDataflowDescription']}
              rows={10}
              value={description}
            />

            <label className="srOnly" htmlFor="dataflowDescription">
              {resourcesContext.messages['createDataflowDescription']}
            </label>

            <div className={styles.errorAndCounterWrapper}>
              <CharacterCounter
                currentLength={description.length}
                maxLength={config.INPUT_MAX_LENGTH}
                style={{ marginTop: '0.25rem' }}
              />
              {errors.description.message !== '' && <ErrorMessage message={errors.description.message} />}
            </div>
          </div>
          <div className={`${styles.search}`}>
            <Button icon="search" label={resourcesContext.messages['searchObligations']} onClick={onSearch} />
            <input
              className={`${styles.searchInput} ${errors.obligation.hasErrors ? styles.searchErrors : ''}`}
              id="searchObligation"
              name="obligation.title"
              onBlur={() => checkIsCorrectInputValue(data.obligation.title, 'obligation')}
              onKeyPress={e => {
                if (e.key === 'Enter' && !checkIsCorrectInputValue(data.obligation.title, 'obligation')) onConfirm();
              }}
              placeholder={resourcesContext.messages['associatedObligation']}
              readOnly={true}
              type="text"
              value={data.obligation.title}
            />
            <label className="srOnly" htmlFor="searchObligation">
              {resourcesContext.messages['searchObligations']}
            </label>
          </div>
        </fieldset>
      </form>
    );
  }
);

export { ManageDataflowForm };
