import { forwardRef, useContext, useEffect, useImperativeHandle, useRef, useState } from 'react';

import styles from './ManageDataflowForm.module.scss';

import { config } from 'conf';

import { Button } from 'views/_components/Button';
import { CharacterCounter } from 'views/_components/CharacterCounter';
import { ErrorMessage } from 'views/_components/ErrorMessage';
import { InputText } from 'views/_components/InputText';
import { InputTextarea } from 'views/_components/InputTextarea/InputTextarea';

import { useInputTextFocus } from 'views/_functions/Hooks/useInputTextFocus';

import { DataflowService } from 'services/DataflowService';
import { CitizenScienceDataflowService } from 'services/CitizenScienceDataflowService';
import { RepresentativeService } from 'services/RepresentativeService';
import { UserService } from 'services/UserService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { TextUtils } from 'repositories/_utils/TextUtils';
import { Dropdown } from 'views/_components/Dropdown';

export const ManageDataflowForm = forwardRef(
  (
    {
      dataflowId,
      dataProviderGroup,
      dialogName,
      getData,
      isAdmin,
      isCitizenScienceDataflow,
      isDataflowOpen,
      isEditing,
      metadata,
      onChangeDate,
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

    const isCustodian = userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowId, [
      config.permissions.roles.CUSTODIAN.key
    ]);
    const isSteward = userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowId, [
      config.permissions.roles.STEWARD.key
    ]);
    const isLeadDesigner = isSteward || isCustodian;

    const isDesign = TextUtils.areEquals(metadata?.dataflowStatus, config.dataflowStatus.DESIGN);

    const [description, setDescription] = useState(metadata.description);
    const [errors, setErrors] = useState({
      description: { message: '', hasErrors: false },
      name: { message: '', hasErrors: false },
      obligation: { message: '', hasErrors: false }
    });
    const [groupOfCompanies, setGroupOfCompanies] = useState([]);
    const [name, setName] = useState(metadata.name);
    const [selectedGroup, setSelectedGroup] = useState();

    const form = useRef(null);
    const inputRef = useRef(null);

    useImperativeHandle(ref, () => ({
      handleSubmit: onConfirm
    }));

    useInputTextFocus(refresh, inputRef);

    const getDropdownsOptions = async () => {
      try {
        const responseGroupOfCompanies = await RepresentativeService.getGroupOrganizations();
        setGroupOfCompanies(responseGroupOfCompanies.data);
      } catch (error) {
        console.error('ManageDataflowForm - getDropdownsOptions.', error);
      }
    };

    useEffect(() => {
      getDropdownsOptions();
    }, []);

    const handleErrors = ({ field, hasErrors, message }) => {
      setErrors(prevState => ({ ...prevState, [field]: { message, hasErrors } }));
    };

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
      checkIsCorrectInputValue(metadata.obligation.title, 'obligation');
      checkIsCorrectInputValue(name, 'name');
      checkIsCorrectInputValue(description, 'description');

      if (!errors.obligation.hasErrors && !errors.name.hasErrors && !errors.description.hasErrors) {
        onSubmit(true);

        try {
          if (isEditing) {
            isCitizenScienceDataflow
              ? await CitizenScienceDataflowService.update(
                  dataflowId,
                  name,
                  description,
                  metadata.obligation.id,
                  metadata.isReleasable,
                  metadata.showPublicInfo,
                  selectedGroup.dataProviderGroupId
                )
              : await DataflowService.update(
                  dataflowId,
                  name,
                  description,
                  metadata.obligation.id,
                  metadata.isReleasable,
                  metadata.showPublicInfo
                );

            onEdit(name, description, metadata.obligation.id);
          } else {
            const creationResponse = isCitizenScienceDataflow
              ? await CitizenScienceDataflowService.create(
                  name,
                  description,
                  metadata.obligation.id,
                  selectedGroup.dataProviderGroupId
                )
              : await DataflowService.create(name, description, metadata.obligation.id);

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
            const notification = isEditing
              ? { type: 'DATAFLOW_UPDATING_ERROR', content: { dataflowId: metadata.id, dataflowName: name } }
              : { type: 'DATAFLOW_CREATION_ERROR', content: { dataflowName: name } };

            notificationContext.add(notification, true);
          }
        } finally {
          onSubmit(false);
        }
      }
    };

    const onSelectGroup = group => {
      setSelectedGroup(group);
      getData({ ...metadata, providerGroup: group });
    };

    return (
      <form ref={form}>
        <fieldset className={styles.fieldset}>
          <div className={`formField ${errors.name.hasErrors ? 'error' : ''}`}>
            <InputText
              autoComplete="off"
              className={styles.dataflowName}
              hasMaxCharCounter={true}
              id="dataflowName"
              maxLength={config.INPUT_MAX_LENGTH}
              name="name"
              onBlur={() => checkIsCorrectInputValue(name, 'name')}
              onChange={event => {
                getData({ ...metadata, name: event.target.value });
                setName(event.target.value);
              }}
              onFocus={() => {
                setErrors(previousErrors => {
                  return { ...previousErrors, name: { message: '', hasErrors: true } };
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
            <InputTextarea
              className={styles.inputTextArea}
              disabled={isEditing && !isLeadDesigner && !isAdmin}
              id="dataflowDescription"
              name="description"
              onBlur={() => checkIsCorrectInputValue(description, 'description')}
              onChange={event => {
                getData({ ...metadata, description: event.target.value });
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

          {isCitizenScienceDataflow && (
            <div className={styles.dropdownsWrapper}>
              <Dropdown
                appendTo={document.body}
                ariaLabel="groupOfCompanies"
                className={styles.groupOfCompaniesWrapper}
                disabled={isEditing && !isDesign}
                name="groupOfCompanies"
                onChange={event => onSelectGroup(event.target.value)}
                onFocus={() => handleErrors({ field: 'groupOfCompanies', hasErrors: false, message: '' })}
                optionLabel="label"
                options={groupOfCompanies}
                placeholder={resourcesContext.messages['selectGroupOfCompanies']}
                tooltip={isDesign ? resourcesContext.messages['groupOfCompaniesDisabledTooltip'] : ''}
                value={selectedGroup ? selectedGroup : dataProviderGroup}
              />
            </div>
          )}

          {isEditing && isDataflowOpen && (
            <div className={`${styles.deliveryDate}`}>
              <Button
                disabled={isEditing && !isLeadDesigner && !isAdmin}
                icon="calendar"
                label={resourcesContext.messages['deliveryDate']}
                onClick={onChangeDate}
              />
            </div>
          )}

          <div className={`${styles.search}`}>
            <Button
              disabled={isEditing && !isLeadDesigner && !isAdmin}
              icon="search"
              label={resourcesContext.messages['searchObligations']}
              onClick={onSearch}
            />
            <input
              className={`${styles.searchInput} ${errors.obligation.hasErrors ? styles.searchErrors : ''}`}
              id="searchObligation"
              name="obligation.title"
              onBlur={() => checkIsCorrectInputValue(metadata.obligation.title, 'obligation')}
              onKeyPress={e => {
                if (e.key === 'Enter' && !checkIsCorrectInputValue(metadata.obligation.title, 'obligation'))
                  onConfirm();
              }}
              placeholder={resourcesContext.messages['associatedObligation']}
              readOnly={true}
              type="text"
              value={metadata.obligation.title}
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
