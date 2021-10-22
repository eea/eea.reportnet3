import { useContext, useState } from 'react';

import styles from './SystemNotificationsCreateForm.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'views/_components/Calendar';
import { Dialog } from 'views/_components/Dialog';
import { InputText } from 'views/_components/InputText';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const SystemNotificationsCreateForm = ({
  formType = '',
  isVisible,
  onCreateSystemNotification,
  onToggleVisibility
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const [systemNotification, setSystemNotification] = useState({});
  console.log({ isVisible });
  const onChange = (property, value) => {
    const inmSystemNotification = { ...systemNotification };
    inmSystemNotification[property] = value;
    setSystemNotification(inmSystemNotification);
  };

  const systemNotificationsCreateFormFooter = (
    <Button
      icon="add"
      id="createSystemNotificationCreateForm"
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
      onHide={() => onToggleVisibility(false)}
      style={{ width: '40vw' }}
      visible={isVisible}
      zIndex={3200}>
      <div className={styles.systemNotificationFormWrapper}>
        <div>
          <div>
            <label>{resourcesContext.messages['type']}</label>
          </div>
          <div>
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
            <label>{resourcesContext.messages['availablePeriod']}</label>
          </div>
        </div>
        <div className={styles.calendarWrapper}>
          <div>
            <div>
              <label>{resourcesContext.messages['from']}</label>
            </div>
            <div>
              <Calendar
                appendTo={document.body}
                baseZIndex={9999}
                // dateFormat={userContext.userProps.dateFormat.toLowerCase().replace('yyyy', 'yy')}
                // inputClassName={styles.inputFilter}
                inputId="systemNotificationFrom"
                monthNavigator={true}
                onChange={e => onChange('from', e.value)}
                // readOnlyInput={true}
                showTime={true}
                showWeek={true}
                // style={{ zoom: '0.95' }}
                value={systemNotification.from}
                yearNavigator={true}
                yearRange="2015:2030"
              />
            </div>
          </div>
          <div>
            <div>
              <label>{resourcesContext.messages['to']}</label>
            </div>
            <div>
              <Calendar
                appendTo={document.body}
                baseZIndex={9999}
                // dateFormat={userContext.userProps.dateFormat.toLowerCase().replace('yyyy', 'yy')}
                // inputClassName={styles.inputFilter}
                inputId="systemNotificationTo"
                monthNavigator={true}
                onChange={e => onChange('to', e.value)}
                // readOnlyInput={true}
                showTime={true}
                showWeek={true}
                // style={{ zoom: '0.95' }}
                value={systemNotification.to}
                yearNavigator={true}
                yearRange="2015:2030"
              />
            </div>
          </div>
        </div>
      </div>
    </Dialog>
  );
};
