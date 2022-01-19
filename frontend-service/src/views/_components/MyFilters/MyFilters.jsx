import { useContext, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { useRecoilState } from 'recoil';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';

import styles from './MyFilters.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'views/_components/Calendar';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { LevelError } from 'views/_components/LevelError';
import { MultiSelect } from 'views/_components/MultiSelect';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import {
  filterByKeysState,
  filterByState,
  filteredDataState,
  searchState,
  sortByState
} from './_functions/Stores/filtersStores';

import { ApplyFiltersUtils } from './_functions/Utils/ApplyFiltersUtils';
import { FiltersUtils } from './_functions/Utils/FiltersUtils';
import { SortUtils } from './_functions/Utils/SortUtils';

const { applyDates, applyInputs, applyMultiSelects, applySearch } = ApplyFiltersUtils;
const { applySort, switchSortByIcon, switchSortByOption } = SortUtils;
const { getLabelsAnimationDateInitial, getOptionsTypes, getPositionLabelAnimationDate, parseDateValues } = FiltersUtils;

export const MyFilters = ({ className, data = [], isStrictMode, onFilter, options = [], viewType }) => {
  const isFilteredByBE = !isNil(onFilter);

  const [filterBy, setFilterBy] = useRecoilState(filterByState(viewType));
  const [filterByKeys, setFilterByKeys] = useRecoilState(filterByKeysState(viewType));
  const [filteredData, setFilteredData] = useRecoilState(filteredDataState(viewType));
  const [searchBy, setSearchBy] = useRecoilState(searchState(viewType));
  const [sortBy, setSortBy] = useRecoilState(sortByState(viewType));

  const { userProps } = useContext(UserContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [labelsAnimationDate, setLabelsAnimationDate] = useState([]);

  const calendarRefs = useRef([]);

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
    getFilterByKeys();
    getSortDefaultValues(options);
  }, [data, viewType]);

  const applyFilters = () => {
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

  const getSortDefaultValues = options => {
    options.forEach(option => {
      if (!option) return;

      if (option.isSortable && option.defaultOrder) {
        setSortBy({ [option.key]: option.defaultOrder });
      }

      if (option.nestedOptions) {
        getSortDefaultValues(option.nestedOptions);
      }
    });
  };

  const getFilterByKeys = () => {
    const filterKeys = { CHECKBOX: [], DATE: [], DROPDOWN: [], INPUT: [], MULTI_SELECT: [], SEARCH: [] };

    options.forEach(option => {
      if (!option) return;

      if (option.type === 'SEARCH') {
        filterKeys.SEARCH = option.searchBy;
      }

      filterKeys[option.type] = option.nestedOptions?.map(nestedOption => nestedOption.key) || [
        ...filterKeys[option.type],
        option.key
      ];
    });

    setFilterByKeys(filterKeys);
  };

  const loadFilters = async () => {
    try {
      let filteredData = await applyFilters();

      if (!isEmpty(sortBy)) {
        const [key, value] = Object.entries(sortBy)[0];

        filteredData = applySort({ filteredData, order: value, prevSortState: applyFilters(), sortByKey: key });
      }

      setFilteredData(filteredData);
    } catch (error) {
      console.error('MyFilters - loadFilters.', error);
    }
  };

  const onApplyFilters = ({ filterBy, searchValue = searchBy }) => {
    if (isFilteredByBE) {
      return data;
    }

    return data.filter(
      item =>
        applyInputs({ filterBy, filterByKeys, item }) &&
        applyDates({ filterBy, filterByKeys, item }) &&
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

    setSearchBy(value);
    setFilteredData(filteredData);
  };

  const onSortData = key => {
    const sortOption = switchSortByOption(sortBy[key]);
    const sortedData = applySort({ filteredData, order: sortOption, prevSortState: applyFilters(), sortByKey: key });

    setSortBy({ [key]: sortOption });
    setFilteredData(sortedData);
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
          return [];

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

  const renderDate = option => {
    const positionLabelAnimationDate = getPositionLabelAnimationDate(labelsAnimationDate, option.key);
    const getClassNameLabelCalendar = () => {
      if (positionLabelAnimationDate && labelsAnimationDate[positionLabelAnimationDate][option.key] === false) {
        return styles.labelDown;
      } else {
        return styles.label;
      }
    };

    if (option.nestedOptions) return option.nestedOptions.map(nestedOption => renderDate(nestedOption));

    const inputId = uniqueId();

    return (
      <div className={styles.block} key={option.key}>
        {option.isSortable ? renderSortButton({ key: option.key }) : renderSortButtonEmpty()}
        <div
          className={`p-float-label ${styles.label} ${styles.elementFilter}`}
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

    return <Dropdown />;
  };

  const renderInput = option => {
    if (option.nestedOptions) {
      return option.nestedOptions.map(nestedOption => renderInput(nestedOption));
    }

    return (
      <div className={styles.block} key={option.key}>
        {option.isSortable ? renderSortButton({ key: option.key }) : renderSortButtonEmpty()}
        <div className={`p-float-label ${styles.label} ${styles.elementFilter}`}>
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

    return (
      <div className={`${styles.block}`} key={option.key}>
        {option.isSortable ? renderSortButton({ key: option.key }) : renderSortButtonEmpty()}
        <MultiSelect
          ariaLabelledBy={`${option.key}_input`}
          checkAllHeader={resourcesContext.messages['checkAllFilter']}
          className={styles.multiselectFilter}
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
          options={option.multiSelectOptions ? option.multiSelectOptions : getOptionsTypes(data, option.key)}
          value={filterBy[option.key]}
        />
      </div>
    );
  };

  const renderMultiSelectOptionTemplate = (template, type) => {
    if (template === 'LevelError') {
      return <LevelError type={type} />;
    }

    return <span className={styles.statusBox}>{type}</span>;
  };

  const renderSearch = option => {
    if (option.nestedOptions) {
      return option.nestedOptions.map(nestedOption => renderSearch(nestedOption));
    }

    return (
      <div className={styles.block} key={option.key}>
        {option.isSortable ? renderSortButton({ key: option.key }) : renderSortButtonEmpty()}
        <div className={`p-float-label ${styles.label} ${styles.elementFilter}`}>
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

  const renderSortButton = ({ key }) => (
    <Button
      className={`p-button-secondary-transparent ${styles.sortButton} ${
        isNil(sortBy[key]) || sortBy[key] === 'idle' ? null : styles.iconActive
      }`}
      icon={switchSortByIcon(sortBy[key])}
      onClick={() => onSortData(key)}
    />
  );

  const renderSortButtonEmpty = () => <div className={styles.sortButtonSize} />;

  return (
    <div className={className ? styles[className] : styles.default}>
      {renderFilters()}
      {isStrictMode ? <InputText placeholder="StrictMode" /> : null}

      {isFilteredByBE && (
        <Button
          className="p-button-primary p-button-rounded p-button-animated-blink"
          icon="filter"
          label={resourcesContext.messages['filter']}
          onClick={onFilter}
        />
      )}

      <div className={`${styles.resetButton}`}>
        <Button
          className="p-button-secondary p-button-rounded p-button-animated-blink"
          icon="undo"
          label={resourcesContext.messages['reset']}
          onClick={() => {
            onResetFilters();
            setLabelsAnimationDate(getLabelsAnimationDateInitial(options, filterBy));
          }}
        />
      </div>
    </div>
  );
};
