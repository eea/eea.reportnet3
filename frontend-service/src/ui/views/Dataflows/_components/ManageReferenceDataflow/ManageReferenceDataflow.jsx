import React, { Fragment, useContext, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ManageReferenceDataflow.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';

import { ReferenceDataflowService } from 'core/services/ReferenceDataflow';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useInputTextFocus } from 'ui/views/_functions/Hooks/useInputTextFocus';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog/ConfirmDialog';
import { routes } from 'ui/routes';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';
import { config } from 'conf';

export const ManageReferenceDataflow = ({
  dataflowId,
  history,
  isEditing,
  isVisible,
  manageDialogs,
  metadata,
  onManage
}) => {
  const dialogName = isEditing ? 'isEditDialogVisible' : 'isReferencedDataflowDialogVisible';
  const isDesign = TextUtils.areEquals(metadata?.status, config.dataflowStatus.DESIGN);

  const { hideLoading, showLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [deleteInput, setDeleteInput] = useState('');
  const [description, setDescription] = useState(isEditing ? metadata.description : '');
  const [hasErrors, setHasErrors] = useState({ description: false, name: false });
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [name, setName] = useState(isEditing ? metadata.name : '');

  const deleteInputRef = useRef(null);
  const inputRef = useRef(null);

  useInputTextFocus(isVisible, inputRef);
  useInputTextFocus(isDeleteDialogVisible, deleteInputRef);

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
    setIsSending(true);
    try {
      if (isEditing) {
        const { status } = await ReferenceDataflowService.edit(dataflowId, description, name);

        if (status >= 200 && status <= 299) manageDialogs(dialogName, false);
      } else {
        const { status } = await ReferenceDataflowService.create(name, description, 'REFERENCE');

        if (status >= 200 && status <= 299) onManage();
      }
    } catch (error) {
      console.log('error :>> ', error);
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
        icon={isSending ? 'spinnerAnimate' : 'save'}
        label={resources.messages['save']}
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
        header={'Reference dataflow'}
        onHide={() => manageDialogs(dialogName, false)}
        visible={isVisible}>
        <div className={`formField ${hasErrors.name ? 'error' : ''}`}>
          <InputText
            onChange={event => setName(event.target.value)}
            onFocus={() => setHasErrors({ ...hasErrors, name: false })}
            placeholder={resources.messages['createDataflowName']}
            ref={inputRef}
            value={name}
          />
        </div>
        <div className={`formField ${hasErrors.description ? 'error' : ''}`}>
          <InputTextarea
            className={styles.inputTextArea}
            onChange={event => setDescription(event.target.value)}
            onFocus={() => setHasErrors({ ...hasErrors, description: false })}
            placeholder={resources.messages['createDataflowDescription']}
            rows={100}
            value={description}
          />
        </div>
      </Dialog>

      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={!TextUtils.areEquals(deleteInput, metadata.name)}
          header={resources.messages['updateDataCollectionHeader']}
          labelCancel={resources.messages['close']}
          labelConfirm={resources.messages['create']}
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
