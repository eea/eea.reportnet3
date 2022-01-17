import { useState } from 'react';

import styles from './QCFieldEditor.module.scss';

import { InputText } from 'views/_components/InputText';

export const QCFieldEditor = ({ field, keyfilter = '', onSaveField, required, rowData }) => {
  const [fieldValue, setFieldValue] = useState(() => rowData[field]);

  const onChange = value => setFieldValue(value);

  return (
    <InputText
      className={required && fieldValue === '' ? styles.required : ''}
      keyfilter={keyfilter}
      onBlur={() => onSaveField(rowData, field, fieldValue, true)}
      onChange={e => {
        if (e.target.value === '' && required) {
          onSaveField(rowData, field, '', true);
        }
        onChange(e.target.value);
      }}
      type="text"
      value={fieldValue}
    />
  );
};
