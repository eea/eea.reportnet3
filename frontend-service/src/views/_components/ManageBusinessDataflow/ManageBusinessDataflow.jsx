import { Fragment, useContext, useLayoutEffect, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import styles from './ManageBusinessDataflow.module.scss';

import { Button } from 'views/_components/Button';
import { CharacterCounter } from 'views/_components/CharacterCounter';
import { Checkbox } from 'views/_components/Checkbox';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';
import { ErrorMessage } from 'views/_components/ErrorMessage';
import { InputText } from 'views/_components/InputText';
import { InputTextarea } from 'views/_components/InputTextarea';
import { Spinner } from 'views/_components/Spinner';
import { TooltipButton } from 'views/_components/TooltipButton';

import { BusinessDataflowService } from 'services/BusinessDataflowService';
import { DataflowService } from 'services/DataflowService';
import { RepresentativeService } from 'services/RepresentativeService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { useInputTextFocus } from 'views/_functions/Hooks/useInputTextFocus';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

import { UserService } from 'services/UserService';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const ManageBusinessDataflow = ({
  dataflowId,
  hasRepresentatives,
  isAdmin,
  isEditing = false,
  isVisible,
  manageDialogs,
  obligation,
  onCreateDataflow,
  onEditDataflow,
  onLoadReportingDataflow,
  resetObligations,
  state
}) => {
  const dialogName = 'isBusinessDataflowDialogVisible';
  const isDesign = TextUtils.areEquals(state?.status, config.dataflowStatus.DESIGN);

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [deleteInput, setDeleteInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState({});
  const [selectedFmeUser, setSelectedFmeUser] = useState({});
  const [description, setDescription] = useState(isEditing ? state.description : '');
  const [groupOfCompanies, setGroupOfCompanies] = useState([]);
  const [fmeUsers, setFmeUsers] = useState([]);
  const [errors, setErrors] = useState({
    description: { hasErrors: false, message: '' },
    name: { hasErrors: false, message: '' }
  });
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [name, setName] = useState(isEditing ? state.name : '');
  const [pinDataflow, setPinDataflow] = useState(false);

  const deleteInputRef = useRef(null);
  const inputRef = useRef(null);

  useInputTextFocus(isVisible, inputRef);
  useInputTextFocus(isDeleteDialogVisible, deleteInputRef);

  useLayoutEffect(() => {
    if (isEditing) {
      setSelectedFmeUser({ id: state.fmeUserId, username: state.fmeUserName });
    }
  }, [fmeUsers]);

  useLayoutEffect(() => {
    if (isEditing) {
      setSelectedGroup({ dataProviderGroupId: state.dataProviderGroupId, label: state.dataProviderGroupName });
    }
  }, [groupOfCompanies]);

  const getDropdownsOptions = async () => {
    setIsLoading(true);
    try {
      const responseGroupOfCompanies = await RepresentativeService.getGroupCompanies();
      setGroupOfCompanies(responseGroupOfCompanies.data);

      const responseFmeUsers = await RepresentativeService.getFmeUsers();
      setFmeUsers(responseFmeUsers.data);
    } catch (error) {
      console.error('ManageBusinessDataflow - getDropdownsOptions.', error);
    } finally {
      setIsLoading(false);
    }
  };

  useLayoutEffect(() => {
    if (isEditing) {
      onLoadReportingDataflow();
    }

    if (isAdmin) {
      getDropdownsOptions();
    }
  }, []);

  useCheckNotifications(['DELETE_DATAFLOW_FAILED_EVENT'], setIsDeleting, false);

  const checkErrors = () => {
    let hasErrors = false;

    if (isEmpty(name.trim())) {
      handleErrors({
        field: 'name',
        hasErrors: true,
        message: resourcesContext.messages['emptyNameValidationError']
      });
      hasErrors = true;
    }

    if (isEmpty(description.trim())) {
      handleErrors({
        field: 'description',
        hasErrors: true,
        message: resourcesContext.messages['emptyDescriptionValidationError']
      });
      hasErrors = true;
    }

    if (description.length > config.INPUT_MAX_LENGTH) {
      handleErrors({
        field: 'description',
        hasErrors: true,
        message: `${resourcesContext.messages['dataflowDescriptionValidationMax']} (${resourcesContext.messages['maxAllowedCharacters']} ${config.INPUT_MAX_LENGTH})`
      });
      hasErrors = true;
    }

    if (name.length > config.INPUT_MAX_LENGTH) {
      handleErrors({
        field: 'name',
        hasErrors: true,
        message: `${resourcesContext.messages['dataflowNameValidationMax']} (${resourcesContext.messages['maxAllowedCharacters']} ${config.INPUT_MAX_LENGTH})`
      });
      hasErrors = true;
    }

    return hasErrors;
  };

  const handleErrors = ({ field, hasErrors, message }) => {
    setErrors(prevState => ({ ...prevState, [field]: { message, hasErrors } }));
  };

  const onSelectGroup = group => setSelectedGroup(group);
  const onSelectFmeUser = fmeUser => setSelectedFmeUser(fmeUser);

  const onHideDataflowDialog = () => {
    resetObligations();
    manageDialogs(dialogName, false);
  };

  const onDeleteDataflow = async () => {
    setIsDeleting(true);
    try {
      await DataflowService.delete(dataflowId);
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      } else {
        console.error('ManageBusinessDataflow - onDeleteDataflow.', error);
        notificationContext.add({ type: 'DATAFLOW_DELETE_BY_ID_ERROR', content: { dataflowId } });
      }
      setIsDeleting(false);
    }
  };

  const onManageBusinessDataflow = async () => {
    if (checkErrors()) return;

    try {
      setIsSending(true);
      if (isEditing) {
        await BusinessDataflowService.update(
          dataflowId,
          description,
          obligation.id,
          name,
          selectedGroup.dataProviderGroupId,
          selectedFmeUser.id
        );
        manageDialogs(dialogName, false);
        onEditDataflow(name, description);
      } else {
        const { data } = await BusinessDataflowService.create(
          name,
          description,
          obligation.id,
          selectedGroup.dataProviderGroupId,
          selectedFmeUser.id
        );
        if (pinDataflow) {
          const inmUserProperties = { ...userContext.userProps };
          inmUserProperties.pinnedDataflows.push(data.toString());

          await UserService.updateConfiguration(inmUserProperties);
          userContext.onChangePinnedDataflows(inmUserProperties.pinnedDataflows);
        }
        onCreateDataflow('isBusinessDataflowDialogVisible');
      }
    } catch (error) {
      if (TextUtils.areEquals(error?.response?.data, 'Dataflow name already exists')) {
        handleErrors({ field: 'name', hasErrors: true, message: resourcesContext.messages['dataflowNameExists'] });
        notificationContext.add({ type: 'DATAFLOW_NAME_EXISTS' });
      } else {
        console.error('ManageBusinessDataflow - onManageBusinessDataflow.', error);
        const notification = isEditing
          ? { type: 'BUSINESS_DATAFLOW_UPDATING_ERROR', content: { dataflowId, dataflowName: name } }
          : { type: 'BUSINESS_DATAFLOW_CREATION_ERROR', content: { dataflowName: name } };

        notificationContext.add(notification);
      }
    } finally {
      setIsSending(false);
    }
  };

  const renderDialogFooter = () => (
    <Fragment>
      <div className="p-toolbar-group-left">
        {!isEditing && (
          <div className={styles.checkboxWrapper}>
            <Checkbox
              ariaLabel={resourcesContext.messages['pinDataflow']}
              checked={pinDataflow}
              id="replaceCheckbox"
              inputId="replaceCheckbox"
              onChange={() => setPinDataflow(!pinDataflow)}
              role="checkbox"
            />
            <label>
              <span onClick={() => setPinDataflow(!pinDataflow)}>{resourcesContext.messages['pinDataflow']}</span>
            </label>
            <TooltipButton
              message={resourcesContext.messages['pinDataflowMessage']}
              uniqueIdentifier="pinDataflow"></TooltipButton>
          </div>
        )}
        {isEditing && isDesign && isAdmin && (
          <Button
            className="p-button-danger p-button-animated-blink"
            icon="trash"
            label={resourcesContext.messages['deleteDataflowButton']}
            onClick={() => setIsDeleteDialogVisible(true)}
          />
        )}
      </div>
      <Button
        className={`p-button-primary ${
          !isEmpty(name) &&
          !isEmpty(description) &&
          !isNil(obligation.id) &&
          !isNil(selectedFmeUser?.id) &&
          !isNil(selectedGroup?.dataProviderGroupId) &&
          !isSending &&
          'p-button-animated-blink'
        }`}
        disabled={
          isEmpty(name) ||
          isEmpty(description) ||
          isNil(obligation.id) ||
          isNil(selectedFmeUser?.id) ||
          isNil(selectedGroup?.dataProviderGroupId) ||
          isSending
        }
        icon={isSending ? 'spinnerAnimate' : isEditing ? 'check' : 'plus'}
        label={isEditing ? resourcesContext.messages['save'] : resourcesContext.messages['create']}
        onClick={() => onManageBusinessDataflow()}
      />
      <Button
        className={`p-button-secondary button-right-aligned p-button-animated-blink ${styles.cancelButton}`}
        icon={'cancel'}
        label={isEditing ? resourcesContext.messages['cancel'] : resourcesContext.messages['close']}
        onClick={onHideDataflowDialog}
      />
    </Fragment>
  );

  return (
    <Fragment>
      <Dialog
        footer={renderDialogFooter()}
        header={
          isEditing
            ? resourcesContext.messages['editBusinessDataflowDialogHeader']
            : resourcesContext.messages['createBusinessDataflowDialogHeader']
        }
        onHide={onHideDataflowDialog}
        visible={isVisible}>
        <div className={styles.dialogContent}>
          {isLoading ? (
            <Spinner className={styles.spinnerCenter} />
          ) : (
            <Fragment>
              <div className={`formField ${errors.name.hasErrors ? 'error' : ''}`}>
                <InputText
                  hasMaxCharCounter={true}
                  id="dataflowName"
                  maxLength={config.INPUT_MAX_LENGTH}
                  onBlur={checkErrors}
                  onChange={event => setName(event.target.value)}
                  onFocus={() => handleErrors({ field: 'name', hasErrors: false, message: '' })}
                  placeholder={resourcesContext.messages['createDataflowName']}
                  ref={inputRef}
                  value={name}
                />
                {!isEmpty(errors.name.message) && <ErrorMessage message={errors.name.message} />}
              </div>

              <div className={`formField ${errors.description.hasErrors ? 'error' : ''}`}>
                <InputTextarea
                  className={styles.inputTextArea}
                  id="dataflowDescription"
                  onBlur={checkErrors}
                  onChange={event => setDescription(event.target.value)}
                  onFocus={() => handleErrors({ field: 'description', hasErrors: false, message: '' })}
                  placeholder={resourcesContext.messages['createDataflowDescription']}
                  rows={10}
                  value={description}
                />
                <div className={styles.errorAndCounterWrapper}>
                  <CharacterCounter
                    currentLength={description.length}
                    maxLength={config.INPUT_MAX_LENGTH}
                    style={{ marginTop: '0.25rem' }}
                  />
                  {!isEmpty(errors.description.message) && <ErrorMessage message={errors.description.message} />}
                </div>
              </div>
              <div className={styles.dropdownsWrapper}>
                <Dropdown
                  appendTo={document.body}
                  ariaLabel="groupOfCompanies"
                  className={styles.groupOfCompaniesWrapper}
                  disabled={!isAdmin || hasRepresentatives}
                  name="groupOfCompanies"
                  onChange={event => onSelectGroup(event.target.value)}
                  onFocus={() => handleErrors({ field: 'groupOfCompanies', hasErrors: false, message: '' })}
                  optionLabel="label"
                  options={!isAdmin ? [selectedGroup] : groupOfCompanies}
                  placeholder={resourcesContext.messages['selectGroupOfCompanies']}
                  tooltip={
                    isAdmin && hasRepresentatives ? resourcesContext.messages['groupOfCompaniesDisabledTooltip'] : ''
                  }
                  value={selectedGroup}
                />

                <Dropdown
                  appendTo={document.body}
                  ariaLabel="fmeUsers"
                  className={styles.fmeUsersWrapper}
                  disabled={!isAdmin}
                  name="fmeUsers"
                  onChange={event => onSelectFmeUser(event.target.value)}
                  onFocus={() => handleErrors({ field: 'fmeUsers', hasErrors: false, message: '' })}
                  optionLabel="username"
                  options={!isAdmin ? [selectedFmeUser] : fmeUsers}
                  placeholder={resourcesContext.messages['selectFmeUser']}
                  value={selectedFmeUser}
                />
              </div>
              <div className={`${styles.search}`}>
                <Button
                  icon="search"
                  label={resourcesContext.messages['searchObligations']}
                  onClick={() => manageDialogs('isReportingObligationsDialogVisible', true)}
                />
                <InputText
                  className={`${styles.searchInput} ${errors?.obligation?.hasErrors ? styles.searchErrors : ''}`}
                  id="obligation"
                  placeholder={resourcesContext.messages['associatedObligation']}
                  readOnly={true}
                  type="text"
                  value={obligation.title}
                />
                <label className="srOnly" htmlFor="obligation">
                  {resourcesContext.messages['searchObligations']}
                </label>
              </div>
            </Fragment>
          )}
        </div>
      </Dialog>

      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={!TextUtils.areEquals(deleteInput, state.name) || isDeleting}
          header={resourcesContext.messages['deleteBusinessDataflowDialogHeader']}
          iconConfirm={isDeleting && 'spinnerAnimate'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onDeleteDataflow}
          onHide={() => setIsDeleteDialogVisible(false)}
          visible={isDeleteDialogVisible}>
          <p>{resourcesContext.messages['deleteDataflow']}</p>
          <p
            dangerouslySetInnerHTML={{
              __html: TextUtils.parseText(resourcesContext.messages['deleteDataflowConfirm'], {
                dataflowName: state.name
              })
            }}></p>
          <InputText
            className={`${styles.inputText}`}
            id="deleteDataflow"
            maxLength={config.INPUT_MAX_LENGTH}
            name={resourcesContext.messages['deleteDataflowButton']}
            onChange={event => setDeleteInput(event.target.value)}
            ref={deleteInputRef}
            value={deleteInput}
          />
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
