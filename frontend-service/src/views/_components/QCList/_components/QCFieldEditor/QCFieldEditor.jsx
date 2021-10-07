import { useState } from 'react';

import { InputText } from 'views/_components/InputText';

export const QCFieldEditor = ({ initialValue, onSaveField, qcs }) => {
  const [fieldValue, setFieldValue] = useState(initialValue);

  const onChange = value => setFieldValue(value);

  return (
    <InputText
      onBlur={() => onSaveField(qcs, fieldValue)}
      onChange={e => onChange(e.target.value)}
      type="text"
      value={fieldValue}
    />
  );
};
