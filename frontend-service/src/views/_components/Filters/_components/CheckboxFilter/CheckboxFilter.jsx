import { useContext } from 'react';

import styles from '../../Filters.module.scss';

import { Checkbox } from 'views/_components/Checkbox';

import { filterByKeyCheckboxStore } from 'views/_components/Filters/_functions/Stores/filterKeysStore';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useFilters } from 'views/_components/Filters/_functions/Hooks/useFilters';

export const CheckboxFilter = ({ hasCustomSort, isLoading, onFilterData, option, recoilId }) => {
  const resourcesContext = useContext(ResourcesContext);

  const { filterBy, onFilter } = useFilters({
    hasCustomSort,
    keyStore: filterByKeyCheckboxStore,
    onFilterData,
    option,
    recoilId
  });

  return (
    <div className={styles.block} key={option.key}>
      <div className={styles.labelCheckbox}>{option.label}</div>
      <div className={styles.checkbox}>
        <Checkbox
          ariaLabel={resourcesContext.messages[option.key]}
          checked={filterBy[option.key] || false}
          disabled={isLoading}
          id={option.key}
          inputId={option.key}
          label={option.key}
          onChange={event => onFilter(event.checked)}
        />
        <label className="srOnly" htmlFor={option.key}>
          {resourcesContext.messages[option.key]}
        </label>
      </div>
    </div>
  );
};
