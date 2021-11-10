import { useState } from 'react';

import styles from './SystemNotificationFieldEditor.module.scss';

import { InputText } from 'views/_components/InputText';

export const SystemNotificationFieldEditor = ({
  initialValue,
  keyfilter = '',
  onSaveField,
  required,
  systemNotifications
}) => {
  const [fieldValue, setFieldValue] = useState(initialValue);
  console.log({ systemNotifications });
  const onChange = value => setFieldValue(value);

  return (
    <InputText
      className={required && fieldValue === '' ? styles.required : ''}
      keyfilter={keyfilter}
      onBlur={() => onSaveField(systemNotifications, fieldValue, true)}
      onChange={e => {
        if (e.target.value === '' && required) {
          onSaveField(systemNotifications, '', true);
        }
        onChange(e.target.value);
      }}
      type="text"
      value={fieldValue}
    />
  );
};
