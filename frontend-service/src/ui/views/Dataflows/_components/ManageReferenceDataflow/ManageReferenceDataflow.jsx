import React, { Fragment, useContext, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ManageReferenceDataflow.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';

import { ReferenceDataflowService } from 'core/services/ReferenceDataflow';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useInputTextFocus } from 'ui/views/_functions/Hooks/useInputTextFocus';

export const ManageReferenceDataflow = ({ dataflowId, isEditing, isVisible, manageDialogs, metadata, onManage }) => {
  const dialogName = isEditing ? 'isEditDialogVisible' : 'isReferencedDataflowDialogVisible';

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [description, setDescription] = useState(isEditing ? metadata.description : '');
  const [hasErrors, setHasErrors] = useState({ description: true, name: false });
  const [isSending, setIsSending] = useState(false);
  const [name, setName] = useState(isEditing ? metadata.name : '');

  const inputRef = useRef(null);

  useInputTextFocus(isVisible, inputRef);

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
  );
};
