import { useRecoilState } from 'recoil';
import styles from '../../Filters.module.scss';

import { Dropdown } from 'views/_components/Dropdown';

import { filterByStore } from '../../_functions/Stores/filterStore';

export const DropdownFilter = ({ option, recoilId }) => {
  const [filterBy, setFilterBy] = useRecoilState(filterByStore(`${option.key}_${recoilId}`));

  return (
    <div className={`${styles.block}`} key={option.key}>
      <Dropdown
        ariaLabel={option.key}
        className={styles.dropdownFilter}
        filter={option.dropdownOptions.length > 10}
        filterPlaceholder={option.label}
        id={`${option.key}_dropdown`}
        inputClassName={`p-float-label ${styles.label}`}
        inputId={option.key}
        label={option.label}
        onChange={event => setFilterBy({ [option.key]: event.target.value })}
        onMouseDown={event => {
          event.preventDefault();
          event.stopPropagation();
        }}
        optionLabel="label"
        options={option.dropdownOptions}
        showClear={filterBy[option.key]}
        showFilterClear={true}
        value={filterBy[option.key]}
      />
    </div>
  );
};
