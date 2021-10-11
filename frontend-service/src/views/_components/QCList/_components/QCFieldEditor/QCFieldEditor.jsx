import { useState } from 'react';

import styles from './QCFieldEditor.module.scss';

import { InputText } from 'views/_components/InputText';

export const QCFieldEditor = ({ initialValue, keyfilter = '', onSaveField, qcs, required }) => {
  const [fieldValue, setFieldValue] = useState(initialValue);

  const onChange = value => setFieldValue(value);

  return (
    <InputText
      className={required && fieldValue === '' ? styles.required : ''}
      keyfilter={keyfilter}
      onBlur={() => onSaveField(qcs, fieldValue)}
      onChange={e => {
        if (e.target.value === '' && required) {
          onSaveField(qcs, '');
        }
        onChange(e.target.value);
      }}
      type="text"
      value={fieldValue}
    />
  );
};
