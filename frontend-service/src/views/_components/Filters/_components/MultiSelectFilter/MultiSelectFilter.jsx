import { useContext } from 'react';
import { useRecoilValue } from 'recoil';

import styles from '../../Filters.module.scss';

import { LevelError } from 'views/_components/LevelError';
import { MultiSelect } from 'views/_components/MultiSelect';
import { SortButton } from '../SortButton';

import { dataStore } from '../../_functions/Stores/filterStore';
import { filterByKeyMultiSelectStore } from '../../_functions/Stores/filterKeysStore';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useFilters } from '../../_functions/Hooks/useFilters';

import { MultiSelectFilterUtils } from './_functions/Utils/MultiSelectFilterUtils';

export const MultiSelectFilter = ({ onFilterData, option, recoilId }) => {
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
      <SortButton id={option.key} isVisible={option.isSortable} />
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
