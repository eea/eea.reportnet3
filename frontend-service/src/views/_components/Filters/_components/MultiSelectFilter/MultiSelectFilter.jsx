import { useContext } from 'react';
import { useRecoilValue } from 'recoil';

import styles from './MultiSelectFilter.module.scss';

import { LevelError } from 'views/_components/LevelError';
import { MultiSelect } from 'views/_components/MultiSelect';
import { SortButton } from 'views/_components/Filters/_components/SortButton';

import { dataStore } from 'views/_components/Filters/_functions/Stores/filterStore';
import { filterByKeyMultiSelectStore } from 'views/_components/Filters/_functions/Stores/filterKeysStore';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useFilters } from 'views/_components/Filters/_functions/Hooks/useFilters';

import { MultiSelectFilterUtils } from './_functions/Utils/MultiSelectFilterUtils';

export const MultiSelectFilter = ({ isLoading, onFilterData, onSort, option, recoilId }) => {
  const resourcesContext = useContext(ResourcesContext);

  const data = useRecoilValue(dataStore(recoilId));

  const { filterBy, onFilter } = useFilters({ keyStore: filterByKeyMultiSelectStore, onFilterData, option, recoilId });

  const renderTemplate = (template, type) => {
    if (template === 'LevelError') {
      return <LevelError type={type} />;
    }

    return <span className={styles.statusBox}>{type?.toString()}</span>;
  };

  return (
    <div className={`${styles.block}`} key={option.key}>
      <SortButton id={option.key} isLoading={isLoading} isVisible={option.isSortable} onSort={onSort} />
      <MultiSelect
        ariaLabelledBy={`${option.key}_input`}
        checkAllHeader={resourcesContext.messages['checkAllFilter']}
        className={`${styles.multiSelect} ${
          filterBy[option.key]?.length > 0 ? styles.elementFilterSelected : styles.elementFilter
        }`}
        filter={option?.showInput}
        headerClassName={styles.selectHeader}
        id={option.key}
        inputClassName={`p-float-label ${styles.label}`}
        inputId={`${option.key}_input`}
        isFilter={true}
        itemTemplate={item => renderTemplate(option.template, item.type)}
        key={option.key}
        label={option.label || ''}
        notCheckAllHeader={resourcesContext.messages['uncheckAllFilter']}
        onChange={event => onFilter(event.target.value)}
        optionLabel="type"
        options={option.multiSelectOptions || MultiSelectFilterUtils.getOptionsTypes(data, option.key)}
        value={filterBy[option.key]}
      />
    </div>
  );
};