import { useContext, useState } from 'react';

import { config } from 'conf';

import styles from './SystemNotificationsCreateForm.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { LevelError } from 'views/_components/LevelError';
import { TooltipButton } from 'views/_components/TooltipButton';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const SystemNotificationsCreateForm = ({ isVisible, onCreateSystemNotification, onToggleVisibility }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [systemNotification, setSystemNotification] = useState({});

  const onChange = (property, value) => {
    const inmSystemNotification = { ...systemNotification };
    inmSystemNotification[property] = value;
    setSystemNotification(inmSystemNotification);
  };

  const notificationLevelTemplate = rowData => {
    console.log(rowData);
    return (
      <div className={styles.notificationLevelTemplateWrapper}>
        <LevelError type={rowData.label.toLowerCase()} />
      </div>
    );
  };

  const systemNotificationsCreateFormFooter = (
    <div>
      <Button
        // className={!isSaving && !records.isSaveDisabled && 'p-button-animated-blink'}
        icon="add"
        id="createSystemNotificationCreateForm"
        label={resourcesContext.messages['save']}
        onClick={() => onCreateSystemNotification(systemNotification)}
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
            <label>{`${resourcesContext.messages['type']} (${resourcesContext.messages['key']})`}</label>
            <TooltipButton
              message={resourcesContext.messages['systemNotificationsKey']}
              uniqueIdentifier="systemNotificationKey"
            />
          </div>
          <div>
            <InputText
              id="systemNotificationKey"
              // keyfilter={RecordUtils.getFilter(type)}
              // maxLength={getMaxCharactersByType(type)}
              name="systemNotificationKey"
              onChange={e => onChange('key', e.target.value.replaceAll(' ', '_').toUpperCase())}
              type="text"
              value={systemNotification.key}
            />
          </div>
        </div>
        <div>
          <div>
            <label>{resourcesContext.messages['message']}</label>
          </div>
          <div>
            <InputText
              id="systemNotificationMessage"
              // keyfilter={RecordUtils.getFilter(type)}
              // maxLength={getMaxCharactersByType(type)}
              name="systemNotificationMessage"
              onChange={e => onChange('message', e.target.value)}
              type="text"
              value={systemNotification.message}
            />
          </div>
        </div>
        <div>
          <div>
            <label>{resourcesContext.messages['levelError']}</label>
          </div>
          <div>
            <Dropdown
              appendTo={document.body}
              filterPlaceholder={resourcesContext.messages['errorTypePlaceholder']}
              id="errorType"
              itemTemplate={rowData => notificationLevelTemplate(rowData, true)}
              onChange={e => onChange('levelError', e.target.value.value)}
              optionLabel="label"
              optionValue="value"
              options={config.validations.errorLevels}
              placeholder={resourcesContext.messages['errorTypePlaceholder']}
              value={{ label: systemNotification.levelError, value: systemNotification.levelError }}
            />
          </div>
        </div>
      </div>
    </Dialog>
  );
};
