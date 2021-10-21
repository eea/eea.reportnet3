import { useContext, useState } from 'react';

import styles from './SystemNotificationsCreateForm.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { InputText } from 'views/_components/InputText';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const SystemNotificationsCreateForm = ({
  isSystemNotificationVisible,
  onCreateSystemNotification,
  setIsSystemNotificationVisible
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const [systemNotification, setSystemNotification] = useState({});

  const onChange = (property, value) => {
    const inmSystemNotification = { ...systemNotification };
    inmSystemNotification[property] = value;
    setSystemNotification(inmSystemNotification);
  };

  const systemNotificationsCreateFormFooter = (
    <Button
      id="createSystemNotificationCreateForm"
      icon="add"
      label={resourcesContext.messages['save']}
      onClick={() => onCreateSystemNotification(systemNotification)}
    />
  );

  return (
    <Dialog
      blockScroll={false}
      className="edit-table"
      contentStyle={{ height: '50%', maxHeight: '80%', overflow: 'auto' }}
      footer={systemNotificationsCreateFormFooter}
      header={resourcesContext.messages['add']}
      modal={true}
      onHide={() => setIsSystemNotificationVisible(false)}
      style={{ width: '80%' }}
      visible={isSystemNotificationVisible}
      zIndex={3100}>
      <div className={styles.systemNotificationFormWrapper}>
        <div>
          <label>{resourcesContext.messages['type']}</label>
          <InputText
            id="systemNotificationType"
            // keyfilter={RecordUtils.getFilter(type)}
            // maxLength={getMaxCharactersByType(type)}
            name="systemNotificationType"
            onChange={e => onChange('type', e.target.value)}
            type="text"
            value={systemNotification.type}
          />
        </div>
      </div>
    </Dialog>
  );
};
