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

export const ManageReferenceDataflow = ({ isVisible, manageDialogs, onCreate }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [description, setDescription] = useState('');
  const [name, setName] = useState('');

  const inputRef = useRef(null);

  useInputTextFocus(isVisible, inputRef);

  const onCreateReferenceDataflow = async () => {
    try {
      const { status } = await ReferenceDataflowService.create(name, description, 'REFERENCE');

      if (status >= 200 && status <= 299) {
        onCreate();
      }
    } catch (error) {
      console.log('error :>> ', error);
    }
  };

  const renderDialogFooter = () => (
    <Fragment>
      <Button
        className="p-button-primary p-button-animated-blink"
        disabled={isEmpty(name) || isEmpty(description)}
        icon={'save'}
        label={resources.messages['save']}
        onClick={() => onCreateReferenceDataflow()}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => manageDialogs('isReferencedDataflowDialogVisible', false)}
      />
    </Fragment>
  );

  return (
    <Dialog
      footer={renderDialogFooter()}
      header={'Reference dataflow'}
      onHide={() => manageDialogs('isReferencedDataflowDialogVisible', false)}
      visible={isVisible}>
      <InputText
        onChange={event => setName(event.target.value)}
        placeholder={resources.messages['createDataflowName']}
        ref={inputRef}
        value={name}
      />
      <InputTextarea
        className={styles.inputTextArea}
        onChange={event => setDescription(event.target.value)}
        placeholder={resources.messages['createDataflowDescription']}
        rows={10}
        value={description}
      />
    </Dialog>
  );
};
