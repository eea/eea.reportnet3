import { useContext } from 'react';
import { useRecoilValue } from 'recoil';

import { config } from 'conf';

import sharedStyles from 'views/_components/Filters/Filters.module.scss';
import styles from './MultiSelectFilter.module.scss';

import { LevelError } from 'views/_components/LevelError';
import { MultiSelect } from 'views/_components/MultiSelect';
import { SortButton } from 'views/_components/Filters/_components/SortButton';

import { dataStore } from 'views/_components/Filters/_functions/Stores/filterStore';
import { filterByKeyMultiSelectStore } from 'views/_components/Filters/_functions/Stores/filterKeysStore';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useFilters } from 'views/_components/Filters/_functions/Hooks/useFilters';

import { MultiSelectFilterUtils } from './_functions/Utils/MultiSelectFilterUtils';

export const MultiSelectFilter = ({
  getFilterBy,
  hasCustomSort,
  isLoading,
  onFilterData,
  onSort,
  option,
  panelClassName,
  recoilId
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const data = useRecoilValue(dataStore(recoilId));

  const { filterBy, onFilter } = useFilters({
    hasCustomSort,
    keyStore: filterByKeyMultiSelectStore,
    onFilterData,
    option,
    recoilId
  });

  const renderTemplate = (template, item) => {
    if (template === 'LevelError') {
      return <LevelError type={item.type} />;
    }

    if (template === 'ValidationsStatus') {
      return <LevelError className={`${config.datasetRunningStatus[item.value].label}`} type={item.type} />;
    }

    return <span>{item.type?.toString()}</span>;
  };

  return (
    <div
      className={`${styles.block} ${
        filterBy[option.key]?.length > 0 ? sharedStyles.elementFilterSelected : sharedStyles.elementFilter
      }`}
      key={option.key}>
      <SortButton
        getFilterBy={getFilterBy}
        id={option.key}
        isLoading={isLoading}
        isVisible={option.isSortable}
        onSort={onSort}
        recoilId={recoilId}
      />
      <MultiSelect
        ariaLabelledBy={`${option.key}_input`}
        checkAllHeader={resourcesContext.messages['checkAllFilter']}
        className={styles.multiSelect}
        filter={option?.showInput}
        headerClassName={styles.selectHeader}
        id={option.key}
        inputClassName={`p-float-label ${styles.label}`}
        inputId={`${option.key}_input`}
        isFilter={true}
        itemTemplate={item => renderTemplate(option.template, item)}
        key={option.key}
        label={option.label || ''}
        notCheckAllHeader={resourcesContext.messages['uncheckAllFilter']}
        onChange={event => onFilter(event.target.value)}
        optionLabel="type"
        options={option.multiSelectOptions || MultiSelectFilterUtils.getOptionsTypes(data, option.key)}
        panelClassName={panelClassName}
        value={filterBy[option.key]}
      />
    </div>
  );
};
