import { useContext, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { useRecoilState, useSetRecoilState } from 'recoil';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';

import styles from './MyFilters.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'views/_components/Calendar';
import { Checkbox } from 'views/_components/Checkbox';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { LevelError } from 'views/_components/LevelError';
import { MultiSelect } from 'views/_components/MultiSelect';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import {
  filterByKeysState,
  filterByNestedKeysState,
  filterByState,
  filteredDataState,
  searchState,
  sortByState
} from './_functions/Stores/filtersStores';

import { ApplyFiltersUtils } from './_functions/Utils/ApplyFiltersUtils';
import { FiltersUtils } from './_functions/Utils/FiltersUtils';
import { SortUtils } from './_functions/Utils/SortUtils';

const { applyCheckBox, applyDates, applyInputs, applyMultiSelects, applySearch } = ApplyFiltersUtils;
const { switchSortByIcon, switchSortByOption } = SortUtils;
const { getLabelsAnimationDateInitial, getOptionsTypes, getPositionLabelAnimationDate, parseDateValues } = FiltersUtils;

export const MyFilters = ({
  className,
  data = [],
  isLoading,
  isStrictMode,
  onFilter,
  onReset = () => {},
  onSort,
  options,
  viewType
}) => {
  const [filterBy, setFilterBy] = useRecoilState(filterByState(viewType));
  const [filterByKeys, setFilterByKeys] = useRecoilState(filterByKeysState(viewType));
  const [filterByNestedKeys, setFilterByNestedKeys] = useRecoilState(filterByNestedKeysState(viewType));
  const [searchBy, setSearchBy] = useRecoilState(searchState(viewType));
  const [sortBy, setSortBy] = useRecoilState(sortByState(viewType));

  const setFilteredData = useSetRecoilState(filteredDataState(viewType));

  const { userProps } = useContext(UserContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [labelsAnimationDate, setLabelsAnimationDate] = useState([]);

  const calendarRefs = useRef([]);

  const hasCustomSort = !isNil(onFilter) || !isNil(onSort);

  useLayoutEffect(() => {
    if (!isEmpty(data)) {
      loadFilters();
    }
  }, [data]);

  useEffect(() => {
    setLabelsAnimationDate(getLabelsAnimationDateInitial(options, filterBy));
    return () => {
      setLabelsAnimationDate([]);
    };
  }, [filterBy, options]);

  useEffect(() => {
    const listener = event => {
      for (const position in labelsAnimationDate) {
        const key = Object.keys(labelsAnimationDate[position])[0];
        if (!calendarRefs.current[key] || calendarRefs.current[key].contains(event.target)) {
          return;
        }
        if (!isEmpty(filterBy[key])) {
          updateValueLabelsAnimationDate(labelsAnimationDate, position, key, true);
        } else {
          updateValueLabelsAnimationDate(labelsAnimationDate, position, key, false);
        }
      }
    };
    document.addEventListener('mousedown', listener);
    document.addEventListener('touchstart', listener);

    return () => {
      document.removeEventListener('mousedown', listener);
      document.removeEventListener('touchstart', listener);
    };
  }, [calendarRefs, labelsAnimationDate, filterBy]);

  useEffect(() => {
    getFilterByKeys('key');
    getFilterByKeys('nestedKey');
  }, [data, viewType]);

  const applyFilters = filterBy => {
    try {
      if (isEmpty(filterBy)) {
        return data;
      }

      return onApplyFilters({ filterBy });
    } catch (error) {
      console.error('MyFilters - applyFilters.', error);
      notificationContext.add({ type: 'FILTER_DATA_ERROR' });
    }
  };

  const clearFilterByMultiselectDueToChangesData = () => {
    const cloneFilterBy = cloneDeep(filterBy);

    if (!isEmpty(data) && !isEmpty(Object.keys(filterBy)) && !isEmpty(Object.keys(filterByKeys))) {
      const filteredKeysMultiSelect = filterByKeys.MULTI_SELECT.filter(key => Object.keys(filterBy).includes(key));
      filteredKeysMultiSelect.forEach(key => {
        if (filterBy[key] && !isEmpty(filterBy[key])) {
          const dataByKey = data.map(itemData => itemData[key]).filter(itemValue => itemValue !== undefined);
          const dataItemsByKey = [];

          filterBy[key].forEach(itemFilterBy => {
            if (dataByKey.includes(itemFilterBy)) {
              dataItemsByKey.push(itemFilterBy);
            }
          });
          cloneFilterBy[key] = dataItemsByKey;
        }
      });
    }

    return cloneFilterBy;
  };

  const getFilterByKeys = auxKey => {
    const filterKeys = { CHECKBOX: [], DATE: [], DROPDOWN: [], INPUT: [], MULTI_SELECT: [], SEARCH: [] };

    options.forEach(option => {
      if (!option) return;

      if (option.type === 'SEARCH') {
        filterKeys.SEARCH = option.searchBy;
      }

      filterKeys[option.type] = option.nestedOptions?.map(nestedOption => nestedOption[auxKey]) || [
        ...filterKeys[option.type],
        option[auxKey]
      ];
    });

    if (auxKey === 'key') {
      setFilterByKeys(filterKeys);
    } else {
      setFilterByNestedKeys(filterKeys);
    }
  };

  const loadFilters = async () => {
    try {
      const clearFilterby = clearFilterByMultiselectDueToChangesData();
      const filteredData = applyFilters(clearFilterby);

      setFilterBy(clearFilterby);
      setFilteredData(filteredData);
    } catch (error) {
      console.error('MyFilters - loadFilters.', error);
    }
  };

  const onApplyFilters = ({ filterBy, searchValue = searchBy }) => {
    if (hasCustomSort) {
      return data;
    }

    return data.filter(
      item =>
        applyInputs({ filterBy, filterByKeys, item, filterByNestedKeys }) &&
        applyDates({ filterBy, filterByKeys, item }) &&
        applyCheckBox({ filterBy, filterByKeys, item }) &&
        applyMultiSelects({ filterBy, filterByKeys, item }) &&
        applySearch({ filterByKeys, item, value: searchValue })
    );
  };

  const onChange = ({ key, value }) => {
    const filteredData = onApplyFilters({ filterBy: { ...filterBy, [key]: value } });
    setFilterBy({ ...filterBy, [key]: value });
    setFilteredData(filteredData);
  };

  const onResetFilters = () => {
    setFilterBy({});
    setFilteredData(data);
    setSearchBy('');
  };

  const onSearch = value => {
    const filteredData = onApplyFilters({ filterBy, searchValue: value });
    setFilterBy({ ...filterBy, searchBy: value });
    setSearchBy(value);
    setFilteredData(filteredData);
  };

  const onSortData = key => {
    setSortBy(prevSortBy => {
      const sortByHeader = switchSortByOption(prevSortBy.sortByOption) === 'idle' ? '' : key;
      const sortByOption = switchSortByOption(prevSortBy.sortByOption);

      if (hasCustomSort) {
        onSort({ sortByHeader, sortByOption });
      }

      return { sortByHeader, sortByOption };
    });
  };

  const updateValueLabelsAnimationDate = (labelsAnimationDate, position, key, value) => {
    if (position !== undefined && labelsAnimationDate.length > 0) {
      const copyLabelsAnimationDate = [...labelsAnimationDate];
      copyLabelsAnimationDate[position][key] = value;
      setLabelsAnimationDate(copyLabelsAnimationDate);
    }
  };

  const renderFilters = () => {
    return options.map(option => {
      switch (option.type) {
        case 'CHECKBOX':
          return renderCheckbox(option);
        case 'DATE':
          return renderDate(option);
        case 'DROPDOWN':
          return renderDropdown(option);
        case 'INPUT':
          return renderInput(option);
        case 'MULTI_SELECT':
          return renderMultiSelect(option);
        case 'SEARCH':
          return renderSearch(option);
        default:
          throw new Error('The option type is not correct.');
      }
    });
  };

  const renderCheckbox = option => {
    if (option.nestedOptions) {
      return option.nestedOptions.map(nestedOption => renderCheckbox(nestedOption));
    }

    return (
      <div className={styles.block} key={option.key}>
        <div className={styles.labelCheckbox}>{option.label}</div>
        <div className={styles.checkbox}>
          <Checkbox
            ariaLabel={resourcesContext.messages[option.key]}
            checked={filterBy[option.key] || false}
            id={option.key}
            inputId={option.key}
            label={option.key}
            onChange={event => onChange({ key: option.key, value: event.target.checked })}
          />
          <label className="srOnly" htmlFor={option.key}>
            {resourcesContext.messages[option.key]}
          </label>
        </div>
      </div>
    );
  };

  const renderDate = option => {
    const positionLabelAnimationDate = getPositionLabelAnimationDate(labelsAnimationDate, option.key);
    const getClassNameLabelCalendar = () => {
      if (positionLabelAnimationDate && labelsAnimationDate[positionLabelAnimationDate][option.key] === false) {
        return styles.labelDown;
      } else {
        return styles.label;
      }
    };

    if (option.nestedOptions) {
      return option.nestedOptions.map(nestedOption => renderDate(nestedOption));
    }

    const inputId = uniqueId();

    return (
      <div className={styles.block} key={option.key}>
        {option.isSortable ? renderSortButton({ key: option.key }) : renderSortButtonEmpty()}
        <div
          className={`p-float-label ${styles.label} ${styles.dateBlock} ${
            filterBy[option.key]?.length > 0 ? styles.elementFilterSelected : styles.elementFilter
          }`}
          id={`calendar_${option.key}`}
          ref={el => (calendarRefs.current[option.key] = el)}>
          <Calendar
            baseZIndex={9999}
            dateFormat={userProps.dateFormat.toLowerCase().replace('yyyy', 'yy')}
            inputClassName={styles.inputFilter}
            inputId={inputId}
            monthNavigator={true}
            onChange={event => onChange({ key: option.key, value: parseDateValues(event.value) })}
            onFocus={() =>
              updateValueLabelsAnimationDate(labelsAnimationDate, positionLabelAnimationDate, option.key, true)
            }
            readOnlyInput={true}
            selectionMode="range"
            value={parseDateValues(filterBy[option.key])}
            yearNavigator={true}
          />

          <label className={getClassNameLabelCalendar()} htmlFor={inputId}>
            {option.label || ''}
          </label>

          {!isEmpty(filterBy[option.key]) && (
            <Button
              className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
              icon="cancel"
              onClick={() => {
                onChange({ key: option.key, value: [] });
                updateValueLabelsAnimationDate(labelsAnimationDate, positionLabelAnimationDate, option.key, false);
                document.getElementById(inputId).value = '';
              }}
            />
          )}
        </div>
      </div>
    );
  };

  const renderDropdown = option => {
    if (option.nestedOptions) {
      return option.nestedOptions.map(nestedOption => renderDropdown(nestedOption));
    }

    return (
      <div className={styles.block} key={option.key}>
        {option.isSortable ? renderSortButton({ key: option.key }) : renderSortButtonEmpty()}
        <Dropdown
          ariaLabel={option.key}
          className={styles.dropdownFilter}
          filter={option.dropdownOptions.length > 10}
          filterPlaceholder={option.label}
          id={`${option.key}_dropdown`}
          inputClassName={`p-float-label ${styles.label}`}
          inputId={option.key}
          label={option.label}
          onChange={event => {
            onChange({ key: option.key, value: event.value });
          }}
          onMouseDown={event => {
            event.preventDefault();
            event.stopPropagation();
          }}
          optionLabel="label"
          options={option.dropdownOptions}
          showClear={true}
          showFilterClear={true}
          value={filterBy[option.key]}
        />
      </div>
    );
  };

  const renderInput = option => {
    if (option.nestedOptions) {
      return option.nestedOptions.map(nestedOption => renderInput(nestedOption));
    }

    return (
      <div className={styles.block} key={option.key}>
        {option.isSortable ? renderSortButton({ key: option.key }) : renderSortButtonEmpty()}
        <div
          className={`p-float-label ${styles.label} ${
            filterBy[option.key]?.length > 0 ? styles.elementFilterSelected : styles.elementFilter
          }`}>
          <InputText
            className={styles.inputFilter}
            id={`${option.key}_input`}
            key={option.key}
            onChange={event => onChange({ key: option.key, value: event.target.value })}
            value={filterBy[option.key] || ''}
          />
          <label className={styles.label} htmlFor={`${option.key}_input`}>
            {option.label || ''}
          </label>
          {!isEmpty(filterBy[option.key]) && (
            <Button
              className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
              icon="cancel"
              onClick={() => onChange({ key: option.key, value: '' })}
            />
          )}
        </div>
      </div>
    );
  };

  const renderMultiSelect = option => {
    if (option.nestedOptions) {
      return option.nestedOptions.map(nestedOption => renderMultiSelect(nestedOption));
    }

    let options = option.multiSelectOptions ?? getOptionsTypes(data, option.key);

    options.sort((a, b) => {
      const fieldA = convertToComparableString(a.value);
      const fieldB = convertToComparableString(b.value);

      return fieldA.localeCompare(fieldB, undefined, { sensitivity: 'base' });
    });

    function convertToComparableString(value) {
      if (value === undefined || value === null) {
        return '';
      }
      if (typeof value !== 'string') {
        return String(value);
      }
      return value;
    }

    // TaskMan 263503: If a table has been selected, filter fields to only show applicable ones
    // The below simulates a filtering for every field present among all tables
    // if resulting array (tempdata) is empty, this means that filtering with the
    // selected table and the iterated field (opt) does not yield results
    // thus this field is not pushed to the final array of fields to be rendered in the dropdown
    let finoptions=[];
    if (option?.label === 'Field'){
      if(filterBy?.table && filterBy?.table.length>0){
        options?.forEach((opt)=>{
          let tempfilt = { filterBy: { ...filterBy, 'field': [opt.value] } }
          let tempdata = onApplyFilters(tempfilt);
          if(tempdata?.length>0) finoptions.push(opt)
        })
      }else{
        finoptions=options;
      }
    }else{
      finoptions=options;
    }

    return (
      <div className={styles.block} key={option.key}>
        {option.isSortable ? renderSortButton({ key: option.key }) : renderSortButtonEmpty()}
        <MultiSelect
          ariaLabelledBy={`${option.key}_input`}
          checkAllHeader={resourcesContext.messages['checkAllFilter']}
          className={`${styles.multiselectFilter} ${
            filterBy[option.key]?.length > 0 ? styles.elementFilterSelected : styles.elementFilter
          }`}
          filter={option?.showInput}
          headerClassName={styles.selectHeader}
          id={option.key}
          inputClassName={`p-float-label ${styles.label}`}
          inputId={`${option.key}_input`}
          isFilter={true}
          itemTemplate={item => renderMultiSelectOptionTemplate(option.template, item.type)}
          key={option.key}
          label={option.label || ''}
          notCheckAllHeader={resourcesContext.messages['uncheckAllFilter']}
          onChange={event => onChange({ key: option.key, value: event.target.value })}
          optionLabel="type"
          options={finoptions}
          value={filterBy[option.key]}
        />
      </div>
    );
  };

  const renderMultiSelectOptionTemplate = (template, type) => {
    if (template === 'LevelError') {
      return <LevelError type={type} />;
    }

    return <span className={styles.statusBox}>{type?.toString()}</span>;
  };

  const renderSearch = option => {
    if (option.nestedOptions) {
      return option.nestedOptions.map(nestedOption => renderSearch(nestedOption));
    }

    return (
      <div className={styles.block} key={option.key}>
        {option.isSortable ? renderSortButton({ key: option.key }) : renderSortButtonEmpty()}
        <div
          className={`p-float-label ${styles.label} ${styles.elementFilter} ${
            searchBy.length > 0 ? styles.elementFilterSelected : styles.elementFilter
          }`}>
          <InputText
            className={styles.searchInput}
            id="searchInput"
            onChange={event => onSearch(event.target.value)}
            value={searchBy}
          />
          {searchBy && (
            <Button
              className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
              icon="cancel"
              onClick={() => onSearch('')}
            />
          )}

          <label className={styles.label} htmlFor={'searchInput'}>
            {option.label}
          </label>
        </div>
      </div>
    );
  };

  const renderSortButton = ({ key }) => {
    const isSortActive = key === sortBy.sortByHeader && sortBy.sortByOption !== 'idle';

    return (
      <Button
        className={`p-button-secondary-transparent ${styles.sortButton} ${isSortActive ? styles.iconActive : null}`}
        disabled={isLoading}
        icon={key === sortBy.sortByHeader ? switchSortByIcon(sortBy.sortByOption) : 'sortAlt'}
        onClick={() => onSortData(key)}
      />
    );
  };

  const renderSortButtonEmpty = () => <div className={styles.sortButtonSize} />;

  return (
    <div className={className ? styles[className] : styles.default}>
      {renderFilters()}
      {isStrictMode ? <InputText placeholder="StrictMode" /> : null}

      <div className={styles.buttonsContainer}>
        {hasCustomSort && (
          <div className={styles.filterButton}>
            <Button
              className="p-button-primary p-button-rounded p-button-animated-blink"
              icon="filter"
              label={resourcesContext.messages['filter']}
              onClick={onFilter}
            />
          </div>
        )}

        <div className={styles.resetButton}>
          <Button
            className="p-button-secondary p-button-rounded p-button-animated-blink"
            icon="undo"
            label={resourcesContext.messages['reset']}
            onClick={() => {
              onResetFilters();
              setLabelsAnimationDate(getLabelsAnimationDateInitial(options, filterBy));
              onReset(true);
            }}
          />
        </div>
      </div>
    </div>
  );
};
