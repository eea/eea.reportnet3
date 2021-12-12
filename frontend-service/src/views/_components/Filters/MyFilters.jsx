import { useContext, useEffect, useLayoutEffect } from 'react';
import { useRecoilState } from 'recoil';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './MyFilters.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'views/_components/Calendar';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { LevelError } from 'views/_components/LevelError';
import { MultiSelect } from 'views/_components/MultiSelect';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { filterByKeysFamily, filtersStateFamily, sortByStateFamily } from './_functions/Stores/filtersStores';

import { ErrorUtils } from 'views/_functions/Utils';
import { MyFiltersUtils } from './_functions/Utils/MyFiltersUtils';
import { MySortUtils } from './_functions/Utils/MySortUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

const SORT_CATEGORY = 'pinned';

const { getEndOfDay, getStartOfDay, parseDateValues, getOptionsTypes } = MyFiltersUtils;
const { switchSortByIcon, switchSortByOption } = MySortUtils;

export const MyFilters = ({ data, getFilteredData, isSearchVisible, isStrictMode, onFilter, options, viewType }) => {
  const [filterByKeys, setFilterByKeys] = useRecoilState(filterByKeysFamily(viewType));
  const [filters, setFilters] = useRecoilState(filtersStateFamily(viewType));
  const [sortBy, setSortBy] = useRecoilState(sortByStateFamily(viewType));

  const { filterBy, filteredData, loadingStatus } = filters;

  const { userProps } = useContext(UserContext);
  const resourcesContext = useContext(ResourcesContext);

  useLayoutEffect(() => {
    if (!isEmpty(data)) loadFilters();
  }, [data]);

  useEffect(() => {
    getFilterByKeys();
  }, []);

  useEffect(() => {
    if (getFilteredData) getFilteredData(filteredData);
  }, [filteredData]);

  const applyFilters = () => {
    try {
      if (isEmpty(filterBy)) return data;

      return onApplyFilters({ filterBy });
    } catch (error) {
      console.log('error :>> ', error);
    }
  };

  const checkMultiSelect = ({ filterBy, item }) => {
    const filteredKeys = filterByKeys.MULTI_SELECT.filter(key => Object.keys(filterBy).includes(key));
    for (let index = 0; index < filteredKeys.length; index++) {
      const filteredKey = filteredKeys[index];
      if (!TextUtils.areEquals(filterBy[filteredKey], '') && filterBy[filteredKey].length > 0) {
        if (!filterBy[filteredKey].includes(item[filteredKey].toUpperCase())) {
          return false;
        }
      }
    }
    return true;
  };

  const checkFilters = ({ item, filterBy }) => {
    const filteredKeys = filterByKeys.INPUT.filter(key => Object.keys(filterBy).includes(key));

    for (let index = 0; index < filteredKeys.length; index++) {
      const filteredKey = filteredKeys[index];

      if (!TextUtils.areEquals(filterBy[filteredKey], '')) {
        if (!item[filteredKey].toLowerCase().includes(filterBy[filteredKey].toLowerCase())) {
          return false;
        }
      }
    }

    return true;
  };

  const checkDates = ({ item, filterBy }) => {
    const filteredKeys = filterByKeys.DATE.filter(key => Object.keys(filterBy).includes(key));

    for (let index = 0; index < filteredKeys.length; index++) {
      const filteredKey = filteredKeys[index];
      const dates = filterBy[filteredKey];
      const value = new Date(item[filteredKey]).getTime();

      if (dates[0] && !dates[1]) return value >= getStartOfDay(dates[0]) && getEndOfDay(dates[0]) >= value;

      if (dates[0] && dates[1]) return value >= getStartOfDay(dates[0]) && getEndOfDay(dates[1]) >= value;
    }

    return true;
  };

  const getFilterByKeys = () => {
    const filterKeys = { CHECKBOX: [], DATE: [], DROPDOWN: [], INPUT: [], MULTI_SELECT: [] };

    options.forEach(option => {
      filterKeys[option.type] = option.nestedOptions?.map(nestedOption => nestedOption.key) || [option.key];
    });

    setFilterByKeys(filterKeys);
  };

  const loadFilters = async () => {
    try {
      const filteredData = await applyFilters();

      setFilters({ ...filters, data: data, filteredData, loadingStatus: 'SUCCESS' });
    } catch (error) {
      setLoadingStatus('FAILED');
      console.log('error :>> ', error);
    }
  };

  const onApplyFilters = ({ filterBy }) => {
    return data.filter(
      item =>
        checkFilters({ filteredKeys: Object.keys(filterBy), item, filterBy }) &&
        checkDates({ filterBy, item }) &&
        checkMultiSelect({ filterBy, item })
    );
  };

  const onChange = ({ key, value }) => {
    const filteredData = onApplyFilters({ filterBy: { ...filters.filterBy, [key]: value } });

    setFilters({ ...filters, filterBy: { ...filters.filterBy, [key]: value }, filteredData });
  };

  const setLoadingStatus = status => setFilters({ ...filters, loadingStatus: status });

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

        default:
          throw new Error('The option type is not correct.');
      }
    });
  };

  const renderDate = option => {
    if (option.nestedOptions) return option.nestedOptions.map(option => renderDate(option));

    return (
      <div style={{ display: 'flex', alignItems: 'center' }}>
        {renderSortButton({ key: option.key })}
        <Calendar
          baseZIndex={9999}
          className={styles.calendarFilter}
          dateFormat={userProps.dateFormat.toLowerCase().replace('yyyy', 'yy')}
          inputClassName={styles.inputFilter}
          key={option.key}
          // inputId={inputId}
          monthNavigator={true}
          onChange={event => onChange({ key: option.key, value: parseDateValues(event.value) })}
          // onChange={event => onFilterData(property, event.value)}
          // onFocus={() => onAnimateLabel(property, true)}
          placeholder={option.label}
          readOnlyInput={true}
          selectionMode="range"
          value={parseDateValues(filterBy[option.key])}
          yearNavigator={true}
          yearRange="2015:2030"
        />
      </div>
    );
  };

  const renderDropdown = option => {
    if (option.nestedOptions) return option.nestedOptions.map(option => renderDropdown(option));

    return <Dropdown />;
  };

  const renderInput = option => {
    if (option.nestedOptions) {
      return option.nestedOptions.map(option => renderInput(option));
    }

    return (
      <div style={{ display: 'flex', alignItems: 'center' }}>
        {renderSortButton({ key: option.key })}
        <InputText
          className={styles.input}
          key={option.key}
          onChange={event => onChange({ key: option.key, value: event.target.value })}
          placeholder={option.label}
          value={filterBy[option.key] || ''}
        />
      </div>
    );
  };

  const onSortData = key => {
    const sortOption = switchSortByOption(sortBy[key]);
    const arrayForSort = [...filteredData];

    const sortedData = arrayForSort.sort((a, b) => {
      if (!isNil(SORT_CATEGORY) && a[SORT_CATEGORY] !== b[SORT_CATEGORY]) {
        return a[SORT_CATEGORY] < b[SORT_CATEGORY] ? -2 : 2;
      }

      const optionA = a[key].toUpperCase();
      const optionB = b[key].toUpperCase();

      switch (sortOption) {
        case 'asc':
          return optionA > optionB ? 1 : -1;

        case 'desc':
          return optionA < optionB ? 1 : -1;

        case 'idle':
          return 0;

        default:
          return 0;
      }
    });

    setSortBy({ [key]: sortOption });
    setFilters({ ...filters, filteredData: sortedData });
  };

  const renderSortButton = ({ key }) => {
    return <Button icon={switchSortByIcon(sortBy[key])} onClick={() => onSortData(key)} />;
  };

  const renderMultiSelect = option => {
    if (option.nestedOptions) {
      return option.nestedOptions.map(netedOption => renderMultiSelect(netedOption));
    }

    const selectTemplate = (optionMultiSelect, nestedOption) => {
      switch (nestedOption?.category) {
        case 'LEVEL_ERROR':
          if (!isNil(optionMultiSelect.type)) {
            return <LevelError type={optionMultiSelect.type} />;
          }
          break;

        default:
          return <span className={`${styles.statusBox}`}>{optionMultiSelect.value.toString().toUpperCase()}</span>;
      }
    };

    return (
      <div style={{ display: 'flex', alignItems: 'center' }}>
        {renderSortButton({ key: option.key })}
        <MultiSelect
          ariaLabelledBy={`${option.key}_input`}
          checkAllHeader={resourcesContext.messages['checkAllFilter']}
          className={styles.multiselectFilter}
          filter={option?.showInput}
          headerClassName={styles.selectHeader}
          id={options.key}
          inputClassName={`p-float-label ${styles.label}`}
          inputId={`${options.key}_input`}
          //isFilter
          itemTemplate={op => selectTemplate(op, option)}
          key={option.key}
          label={option.label || ''}
          notCheckAllHeader={resourcesContext.messages['uncheckAllFilter']}
          onChange={event => onChange({ key: option.key, value: event.target.value })}
          optionLabel="type"
          options={getOptionsTypes(data, option.key, undefined, ErrorUtils.orderLevelErrors)}
          value={filterBy[option.key]}
        />
      </div>
    );
  };

  if (loadingStatus === 'PENDING') return <div>LOADING</div>;

  return (
    <div className={styles.filters}>
      {isSearchVisible ? <InputText placeholder="Search" /> : null}
      {renderFilters()}
      {isStrictMode ? <InputText placeholder="StrictMode" /> : null}

      {!isNil(onFilter) && (
        <Button
          className="p-button-primary p-button-rounded p-button-animated-blink"
          icon="filter"
          label={resourcesContext.messages['filter']}
          onClick={onFilter}
        />
      )}

      <Button
        className="p-button-secondary p-button-rounded p-button-animated-blink"
        icon="undo"
        label={resourcesContext.messages['reset']}
        onClick={loadFilters}
      />
    </div>
  );
};

MyFilters.propTypes = {
  data: PropTypes.array,
  getFilteredData: PropTypes.func,
  isSearchVisible: PropTypes.bool,
  isStrictMode: PropTypes.bool,
  onFilter: PropTypes.func,
  options: PropTypes.arrayOf(
    PropTypes.shape({
      category: PropTypes.string | undefined,
      nestedOptions: PropTypes.arrayOf(
        PropTypes.shape({
          isInputVisible: PropTypes.bool,
          key: PropTypes.string,
          label: PropTypes.string,
          order: PropTypes.number
        })
      ),
      type: PropTypes.oneOf('CHECKBOX' | 'DATE' | 'DROPDOWN' | 'INPUT' | 'MULTI_SELECT').isRequired
    })
  ),
  viewType: PropTypes.string
};

MyFilters.defaultProps = {
  data: [],
  getFilteredData: null,
  isSearchVisible: false,
  isStrictMode: false,
  onFilter: null,
  options: []
};
