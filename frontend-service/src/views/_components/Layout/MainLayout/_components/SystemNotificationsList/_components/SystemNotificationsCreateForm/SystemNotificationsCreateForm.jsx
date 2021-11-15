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
  isVisible,
  notification = {},
  onCreateSystemNotification,
  onToggleVisibility,
  onUpdateSystemNotification
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const growlRef = useRef();

  console.log({ formType });
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
        disabled={hasErrors()}
        icon="add"
        id="createSystemNotificationCreateForm"
        label={resourcesContext.messages[formType === 'EDIT' ? 'update' : 'save']}
        onClick={() =>
          formType === 'EDIT'
            ? onUpdateSystemNotification(systemNotification)
            : onCreateSystemNotification(systemNotification)
        }
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
      header={resourcesContext.messages['add']}
      modal={true}
      onHide={() => onToggleVisibility(false)}
      style={{ width: '40vw' }}
      visible={isVisible}
      zIndex={3200}>
      <div className={styles.systemNotificationFormWrapper}>
        <div>
          <div>
            <label>{resourcesContext.messages['message']}</label>
          </div>
          <div>
            <InputText
              id="systemNotificationMessage"
              // keyfilter={RecordUtils.getFilter(type)}
              maxLength={config.INPUT_MAX_LENGTH}
              name="systemNotificationMessage"
              onChange={e => onChange('message', e.target.value)}
              type="text"
              value={systemNotification.message}
            />
            <CharacterCounter
              currentLength={systemNotification.message.length}
              maxLength={config.INPUT_MAX_LENGTH}
              style={{ position: 'relative', top: '0.25rem' }}
            />
          </div>
        </div>
        <div>
          <div>
            <label>{resourcesContext.messages['notificationLevel']}</label>
          </div>
          <div>
            {console.log({ label: systemNotification.level, value: systemNotification.level })}
            <Dropdown
              appendTo={document.body}
              filterPlaceholder={resourcesContext.messages['systemNotificationLevel']}
              id="errorLevel"
              itemTemplate={rowData => notificationLevelTemplate(rowData, true)}
              onChange={e => onChange('level', e.target.value.value)}
              optionLabel="label"
              optionValue="value"
              options={config.systemNotifications.levels}
              placeholder={resourcesContext.messages['systemNotificationLevel']}
              style={{ width: '15vw' }}
              value={{ label: systemNotification.level, value: systemNotification.level }}
            />
          </div>
        </div>
        <div>
          <div className={styles.enabledCheckbox}>
            <label onClick={e => onChange('enabled', !systemNotification.enabled)}>
              {resourcesContext.messages['ruleEnabled']}
            </label>
            <Checkbox
              checked={systemNotification.enabled}
              id="systemNotification_Enabled"
              inputId="systemNotification_Enabled"
              onChange={e => onChange('enabled', e.checked)}
              role="checkbox"
            />
          </div>
        </div>
        <div>
          <div>
            <h3>{resourcesContext.messages['previewNotification']}</h3>
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
