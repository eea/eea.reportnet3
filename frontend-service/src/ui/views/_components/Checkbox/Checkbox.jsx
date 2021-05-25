import { Checkbox as PrimeCheckbox } from 'primereact/checkbox';

import './Checkbox.scss';

const Checkbox = ({ disabled, id, inputId, isChecked, onChange }) => {
  return <PrimeCheckbox checked={isChecked} disabled={disabled} id={id} inputId={inputId} onChange={onChange} />;
};

export { Checkbox };
