import React, { Fragment, useContext, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';
import { routes } from 'ui/routes';

import styles from './ManageReferenceDataflow.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { ErrorMessage } from 'ui/views/_components/ErrorMessage';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';

import { ReferenceDataflowService } from 'core/services/ReferenceDataflow';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useInputTextFocus } from 'ui/views/_functions/Hooks/useInputTextFocus';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

export const ManageReferenceDataflow = ({
  dataflowId,
  history,
  isEditing = false,
  isVisible,
  manageDialogs,
  metadata,
  onManage
}) => {
  const dialogName = isEditing ? 'isEditDialogVisible' : 'isReferencedDataflowDialogVisible';
  const INPUT_MAX_LENGTH = 255;
  const isDesign = TextUtils.areEquals(metadata?.status, config.dataflowStatus.DESIGN);

  const { hideLoading, showLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [deleteInput, setDeleteInput] = useState('');
  const [description, setDescription] = useState(isEditing ? metadata.description : '');
  const [errors, setErrors] = useState({
    description: { hasErrors: false, message: '' },
    name: { hasErrors: false, message: '' }
  });
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [name, setName] = useState(isEditing ? metadata.name : '');

  const deleteInputRef = useRef(null);
  const inputRef = useRef(null);

  useInputTextFocus(isVisible, inputRef);
  useInputTextFocus(isDeleteDialogVisible, deleteInputRef);

  const checkErrors = () => {
    let hasErrors = false;
    if (description.length > INPUT_MAX_LENGTH) {
      handleErrors({
        field: 'description',
        hasErrors: true,
        message: resources.messages['dataflowDescriptionValidationMax']
      });
      hasErrors = true;
    }

    if (name.length > INPUT_MAX_LENGTH) {
      handleErrors({ field: 'name', hasErrors: true, message: resources.messages['dataflowNameValidationMax'] });
      hasErrors = true;
    }

    return hasErrors;
  };

  const handleErrors = ({ field, hasErrors, message }) => {
    setErrors(prevState => ({ ...prevState, [field]: { message, hasErrors } }));
  };

  const onDeleteDataflow = async () => {
    setIsDeleteDialogVisible(false);
    showLoading();
    try {
      const response = await ReferenceDataflowService.deleteReferenceDataflow(dataflowId);
      if (response.status >= 200 && response.status <= 299) {
        history.push(getUrl(routes.DATAFLOWS));
        notificationContext.add({ type: 'DATAFLOW_DELETE_SUCCESS' });
      }
    } catch (error) {
      notificationContext.add({ type: 'DATAFLOW_DELETE_BY_ID_ERROR', content: { dataflowId } });
    } finally {
      hideLoading();
    }
  };

  const onManageReferenceDataflow = async () => {
    if (checkErrors()) return;

    try {
      setIsSending(true);
      if (isEditing) {
        const { status } = await ReferenceDataflowService.edit(dataflowId, description, name, 'REFERENCE');

        if (status >= 200 && status <= 299) manageDialogs(dialogName, false);
      } else {
        const { status } = await ReferenceDataflowService.create(name, description, 'REFERENCE');

        if (status >= 200 && status <= 299) onManage();
      }
    } catch (error) {
      if (TextUtils.areEquals(error?.response?.data, 'Dataflow name already exists')) {
        handleErrors({ field: 'name', hasErrors: true, message: resources.messages['dataflowNameExists'] });
        notificationContext.add({ type: 'DATAFLOW_NAME_EXISTS' });
      } else {
        const notification = isEditing
          ? { type: 'DATAFLOW_UPDATING_ERROR', content: { dataflowId, dataflowName: name } }
          : { type: 'DATAFLOW_CREATION_ERROR', content: { dataflowName: name } };

        notificationContext.add(notification);
      }
    } finally {
      setIsSending(false);
    }
  };

  const renderDialogFooter = () => (
    <Fragment>
      <div className="p-toolbar-group-left">
        {/* {isEditing && state.isCustodian && state.status === config.dataflowStatus.DESIGN && ( */}
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
        className="p-button-primary p-button-animated-blink"
        disabled={isEmpty(name) || isEmpty(description) || isSending}
        icon={isSending ? 'spinnerAnimate' : isEditing ? 'check' : 'plus'}
        label={isEditing ? resources.messages['save'] : resources.messages['create']}
        onClick={() => onManageReferenceDataflow()}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
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
          disabledConfirm={!TextUtils.areEquals(deleteInput, metadata.name)}
          header={resources.messages['deleteReferenceDataflowDialogHeader']}
          labelCancel={resources.messages['close']}
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
              maxLength={255}
              onChange={event => setDeleteInput(event.target.value)}
              ref={deleteInputRef}
              value={deleteInput}
            />
            <label className="srOnly" htmlFor="deleteDataflow">
              {resources.messages['deleteDataflowButton']}
            </label>
          </p>
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
