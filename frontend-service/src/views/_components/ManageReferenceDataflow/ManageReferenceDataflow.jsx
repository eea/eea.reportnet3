import React, { Fragment, useContext, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';

import styles from './ManageReferenceDataflow.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { Dialog } from 'views/_components/Dialog';
import { ErrorMessage } from 'views/_components/ErrorMessage';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputText } from 'views/_components/InputText';
import { InputTextarea } from 'views/_components/InputTextarea';
import ReactTooltip from 'react-tooltip';

import { DataflowService } from 'services/DataflowService';
import { ReferenceDataflowService } from 'services/ReferenceDataflowService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { useInputTextFocus } from 'views/_functions/Hooks/useInputTextFocus';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

import { UserService } from 'services/UserService';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const ManageReferenceDataflow = ({
  dataflowId,
  isEditing = false,
  isVisible,
  manageDialogs,
  metadata,
  onEditDataflow,
  onCreateDataflow
}) => {
  const dialogName = isEditing ? 'isEditDialogVisible' : 'isReferencedDataflowDialogVisible';

  const isDesign = TextUtils.areEquals(metadata?.status, config.dataflowStatus.DESIGN);

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [deleteInput, setDeleteInput] = useState('');
  const [description, setDescription] = useState(isEditing ? metadata.description : '');
  const [errors, setErrors] = useState({
    description: { hasErrors: false, message: '' },
    name: { hasErrors: false, message: '' }
  });
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [name, setName] = useState(isEditing ? metadata.name : '');
  const [pinDataflow, setPinDataflow] = useState(false);

  const deleteInputRef = useRef(null);
  const inputRef = useRef(null);

  useInputTextFocus(isVisible, inputRef);
  useInputTextFocus(isDeleteDialogVisible, deleteInputRef);

  useCheckNotifications(['DELETE_DATAFLOW_FAILED_EVENT'], setIsDeleting, false);

  const checkErrors = () => {
    let hasErrors = false;

    if (isEmpty(name.trim())) {
      handleErrors({
        field: 'name',
        hasErrors: true,
        message: resources.messages['emptyNameValidationError']
      });
      hasErrors = true;
    }

    if (isEmpty(description.trim())) {
      handleErrors({
        field: 'description',
        hasErrors: true,
        message: resources.messages['emptyDescriptionValidationError']
      });
      hasErrors = true;
    }

    if (description.length > config.INPUT_MAX_LENGTH) {
      handleErrors({
        field: 'description',
        hasErrors: true,
        message: resources.messages['dataflowDescriptionValidationMax']
      });
      hasErrors = true;
    }

    if (name.length > config.INPUT_MAX_LENGTH) {
      handleErrors({ field: 'name', hasErrors: true, message: resources.messages['dataflowNameValidationMax'] });
      hasErrors = true;
    }

    return hasErrors;
  };

  const handleErrors = ({ field, hasErrors, message }) => {
    setErrors(prevState => ({ ...prevState, [field]: { message, hasErrors } }));
  };

  const onDeleteDataflow = async () => {
    setIsDeleting(true);
    try {
      await DataflowService.delete(dataflowId);
    } catch (error) {
      console.error('ManageReferenceDataflow - onDeleteDataflow.', error);
      notificationContext.add({ type: 'DATAFLOW_DELETE_BY_ID_ERROR', content: { dataflowId } });
      setIsDeleting(false);
    }
  };

  const onManageReferenceDataflow = async () => {
    if (checkErrors()) return;

    try {
      setIsSending(true);
      if (isEditing) {
        await ReferenceDataflowService.update(dataflowId, description, name, 'REFERENCE');
        manageDialogs(dialogName, false);
        onEditDataflow(name, description);
      } else {
        const { data } = await ReferenceDataflowService.create(name, description, 'REFERENCE');
        if (pinDataflow) {
          const inmUserProperties = { ...userContext.userProps };
          inmUserProperties.pinnedDataflows.push(data.toString());
          await UserService.updateConfiguration(inmUserProperties);
          userContext.onChangePinnedDataflows(inmUserProperties.pinnedDataflows);
        }
        onCreateDataflow('isReferencedDataflowDialogVisible');
      }
    } catch (error) {
      if (TextUtils.areEquals(error?.response?.data, 'Dataflow name already exists')) {
        handleErrors({ field: 'name', hasErrors: true, message: resources.messages['dataflowNameExists'] });
        notificationContext.add({ type: 'DATAFLOW_NAME_EXISTS' });
      } else {
        console.error('ManageReferenceDataflows - onManageReferenceDataflow.', error);
        const notification = isEditing
          ? { type: 'REFERENCE_DATAFLOW_UPDATING_ERROR', content: { dataflowId, dataflowName: name } }
          : { type: 'REFERENCE_DATAFLOW_CREATION_ERROR', content: { dataflowName: name } };

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
              ariaLabel={resources.messages['pinDataflow']}
              checked={pinDataflow}
              id="replaceCheckbox"
              inputId="replaceCheckbox"
              onChange={() => setPinDataflow(!pinDataflow)}
              role="checkbox"
            />
            <label>
              <span onClick={() => setPinDataflow(!pinDataflow)}>{resources.messages['pinDataflow']}</span>
            </label>
            <FontAwesomeIcon
              aria-hidden={false}
              className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
              data-for="pinDataflow"
              data-tip
              icon={AwesomeIcons('infoCircle')}
            />
            <ReactTooltip border={true} className={styles.tooltip} effect="solid" id="pinDataflow" place="top">
              <span>{resources.messages['pinDataflowMessage']}</span>
            </ReactTooltip>
          </div>
        )}
        {isEditing && isDesign && (
          <Button
            className="p-button-danger p-button-animated-blink"
            icon="trash"
            label={resources.messages['deleteDataflowButton']}
            onClick={() => setIsDeleteDialogVisible(true)}
          />
        )}
      </div>
      <Button
        className={`p-button-primary ${
          !isEmpty(name) && !isEmpty(description) && !isSending && 'p-button-animated-blink'
        }`}
        disabled={isEmpty(name) || isEmpty(description) || isSending}
        icon={isSending ? 'spinnerAnimate' : isEditing ? 'check' : 'plus'}
        label={isEditing ? resources.messages['save'] : resources.messages['create']}
        onClick={() => onManageReferenceDataflow()}
      />
      <Button
        className={`p-button-secondary button-right-aligned p-button-animated-blink ${styles.cancelButton}`}
        icon={'cancel'}
        label={isEditing ? resources.messages['cancel'] : resources.messages['close']}
        onClick={() => manageDialogs(dialogName, false)}
      />
    </Fragment>
  );

  return (
    <Fragment>
      <Dialog
        footer={renderDialogFooter()}
        header={
          isEditing
            ? resources.messages['editReferenceDataflowDialogHeader']
            : resources.messages['createReferenceDataflowDialogHeader']
        }
        onHide={() => manageDialogs(dialogName, false)}
        visible={isVisible}>
        <div className={`formField ${errors.name.hasErrors ? 'error' : ''}`}>
          <InputText
            id="dataflowName"
            onChange={event => setName(event.target.value)}
            onFocus={() => handleErrors({ field: 'name', hasErrors: false, message: '' })}
            placeholder={resources.messages['createDataflowName']}
            ref={inputRef}
            value={name}
          />
          {!isEmpty(errors.name.message) && <ErrorMessage message={errors.name.message} />}
        </div>
        <div className={`formField ${errors.description.hasErrors ? 'error' : ''}`}>
          <InputTextarea
            className={styles.inputTextArea}
            id="dataflowDescription"
            onChange={event => setDescription(event.target.value)}
            onFocus={() => handleErrors({ field: 'description', hasErrors: false, message: '' })}
            placeholder={resources.messages['createDataflowDescription']}
            rows={10}
            value={description}
          />
          {!isEmpty(errors.description.message) && <ErrorMessage message={errors.description.message} />}
        </div>
      </Dialog>

      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={!TextUtils.areEquals(deleteInput, metadata.name) || isDeleting}
          header={resources.messages['deleteReferenceDataflowDialogHeader']}
          iconConfirm={isDeleting && 'spinnerAnimate'}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={onDeleteDataflow}
          onHide={() => setIsDeleteDialogVisible(false)}
          visible={isDeleteDialogVisible}>
          <p>{resources.messages['deleteDataflow']}</p>
          <p
            dangerouslySetInnerHTML={{
              __html: TextUtils.parseText(resources.messages['deleteDataflowConfirm'], {
                dataflowName: metadata.name
              })
            }}></p>
          <p>
            <InputText
              className={`${styles.inputText}`}
              id="deleteDataflow"
              maxLength={255}
              name={resources.messages['deleteDataflowButton']}
              onChange={event => setDeleteInput(event.target.value)}
              ref={deleteInputRef}
              value={deleteInput}
            />
          </p>
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
