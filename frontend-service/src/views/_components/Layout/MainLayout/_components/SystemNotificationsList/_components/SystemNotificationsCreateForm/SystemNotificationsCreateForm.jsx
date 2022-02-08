import { useContext, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';

import styles from './SystemNotificationsCreateForm.module.scss';

import { Button } from 'views/_components/Button';
import { CharacterCounter } from 'views/_components/CharacterCounter';
import { Checkbox } from 'views/_components/Checkbox';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';
import { Growl } from 'views/_components/Growl';
import { GrowlMessage } from 'views/_components/Growl/_components/GrowlMessage';
import { InputText } from 'views/_components/InputText';
import { LevelError } from 'views/_components/LevelError';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const SystemNotificationsCreateForm = ({
  formType = '',
  isCreating = false,
  isVisible,
  notification = {},
  onCreateSystemNotification,
  onToggleVisibility,
  onUpdateSystemNotification
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const growlRef = useRef();

  const [systemNotification, setSystemNotification] = useState(
    formType === 'EDIT' ? notification : { message: '', enabled: true, level: 'INFO' }
  );

  const hasErrors = () => isEmpty(systemNotification.message) || isEmpty(systemNotification.level);

  const onChange = (property, value) => {
    const inmSystemNotification = { ...systemNotification };
    inmSystemNotification[property] = value;
    setSystemNotification(inmSystemNotification);
  };

  const notificationLevelTemplate = rowData => (
    <div>
      <LevelError type={rowData.label.toLowerCase()} />
    </div>
  );

  const systemNotificationsCreateFormFooter = (
    <div>
      <Button
        className={!hasErrors() && 'p-button-animated-blink'}
        disabled={hasErrors() || isCreating}
        icon={isCreating ? 'spinnerAnimate' : 'add'}
        id="createSystemNotificationCreateForm"
        label={resourcesContext.messages[formType === 'EDIT' ? 'update' : 'save']}
        onClick={() => {
          if (formType === 'EDIT') {
            onUpdateSystemNotification(systemNotification);
          } else {
            onCreateSystemNotification(systemNotification);
          }
        }}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        id="cancelCreateSystemNotificationCreateForm"
        label={resourcesContext.messages['cancel']}
        onClick={() => onToggleVisibility(false)}
      />
    </div>
  );

  const renderSystemNotificationPreview = () => {
    return (
      <div className={styles.previewSystemNotification}>
        <GrowlMessage
          closableOnClick={false}
          message={{
            detail: systemNotification.message,
            preview: true,
            severity: systemNotification.level.toLowerCase(),
            summary: systemNotification.level.toUpperCase(),
            system: true
          }}
        />
      </div>
    );
  };

  return (
    <Dialog
      blockScroll={false}
      className="edit-table"
      contentStyle={{ height: '50%', maxHeight: '80%', overflow: 'auto' }}
      footer={systemNotificationsCreateFormFooter}
      header={resourcesContext.messages['addSystemNotification']}
      modal={true}
      onHide={() => onToggleVisibility(false)}
      style={{ width: '40vw' }}
      visible={isVisible}
      zIndex={3200}>
      <div className={styles.systemNotificationFormWrapper}>
        <div className={styles.formElementWrapper}>
          <div>
            <label>{resourcesContext.messages['message']}</label>
          </div>
          <div>
            <InputText
              id="systemNotificationMessage"
              maxLength={config.SYSTEM_NOTIFICATION_MAX_LENGTH}
              name="systemNotificationMessage"
              onChange={e => onChange('message', e.target.value)}
              type="text"
              value={systemNotification.message}
            />
            <CharacterCounter
              currentLength={systemNotification.message.length}
              maxLength={config.SYSTEM_NOTIFICATION_MAX_LENGTH}
              style={{ position: 'relative', top: '0.25rem', marginBottom: '0' }}
            />
          </div>
        </div>
        <div className={`${styles.systemNotificationColumnGroup} ${styles.formElementWrapper}`}>
          <div className={styles.systemNotificationColumn}>
            <label>{resourcesContext.messages['notificationLevel']}</label>
            <Dropdown
              appendTo={document.body}
              className={styles.systemNotificationLevelDropdown}
              filterPlaceholder={resourcesContext.messages['systemNotificationLevel']}
              id="errorLevel"
              itemTemplate={rowData => notificationLevelTemplate(rowData, true)}
              onChange={e => onChange('level', e.target.value.value)}
              optionLabel="label"
              options={config.systemNotifications.levels}
              optionValue="value"
              placeholder={resourcesContext.messages['systemNotificationLevel']}
              style={{ width: '15vw' }}
              value={{ label: systemNotification.level, value: systemNotification.level }}
            />
          </div>
          <div className={styles.systemNotificationColumn}>
            <label>{resourcesContext.messages['ruleEnabled']}</label>
            <Checkbox
              checked={systemNotification.enabled}
              className={styles.enabledCheckbox}
              id="systemNotification_Enabled"
              inputId="systemNotification_Enabled"
              onChange={e => onChange('enabled', e.checked)}
              role="checkbox"
            />
          </div>
        </div>
        <div className={styles.previewElementWrapper}>
          <div>
            <label>{resourcesContext.messages['previewNotification']}</label>
            <Growl ref={growlRef} />
          </div>
          {renderSystemNotificationPreview()}
          <div className={styles.previewButtonWrapper}>
            <Button
              className="p-button-animated-blink"
              icon="bell"
              id="previewSystemNotification"
              label={resourcesContext.messages['previewNotification']}
              onClick={() =>
                growlRef.current.show({
                  detail: systemNotification.message,
                  severity: systemNotification.level.toLowerCase(),
                  summary: systemNotification.level.toUpperCase(),
                  system: true
                })
              }
              tooltip={resourcesContext.messages['previewNotificationMessage']}
              tooltipOptions={{ position: 'top' }}
            />
          </div>
        </div>
      </div>
    </Dialog>
  );
};
