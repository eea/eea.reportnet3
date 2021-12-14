import { useContext, useEffect, useLayoutEffect, useState, useRef } from 'react';
import { useRecoilState } from 'recoil';
import PropTypes from 'prop-types';

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

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { filterByKeysFamily, filtersStateFamily, sortByStateFamily } from './_functions/Stores/filtersStores';

import { ApplyFiltersUtils } from './_functions/Utils/ApplyFiltersUtils';
import { FiltersUtils } from './_functions/Utils/FiltersUtils';
import { SortUtils } from './_functions/Utils/SortUtils';

const { applyDates, applyInputs, applyMultiSelects } = ApplyFiltersUtils;
const { applySort, switchSortByIcon, switchSortByOption } = SortUtils;
const { getOptionsTypes, parseDateValues, getLabelsAnimationDateInitial, getPositionLabelAnimationDate } = FiltersUtils;

export const MyFilters = ({
  className,
  data,
  getFilteredData,
  isSearchVisible,
  isStrictMode,
  onFilter,
  options,
  viewType
}) => {
  const [filterByKeys, setFilterByKeys] = useRecoilState(filterByKeysFamily(viewType));
  const [filters, setFilters] = useRecoilState(filtersStateFamily(viewType));
  const [sortBy, setSortBy] = useRecoilState(sortByStateFamily(viewType));

  const [labelsAnimationDate, setLabelsAnimationDate] = useState([]);

  const { filterBy, filteredData, loadingStatus } = filters;

  const { userProps } = useContext(UserContext);
  const resourcesContext = useContext(ResourcesContext);

  const calendarRefs = useRef([]);

  useLayoutEffect(() => {
    if (!isEmpty(data)) loadFilters();
  }, [data]);

  useEffect(() => {
    setLabelsAnimationDate(getLabelsAnimationDateInitial(options));
    return () => {
      setLabelsAnimationDate([]);
    };
  }, []);

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
  }, [viewType]);

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

  const getFilterByKeys = () => {
    const filterKeys = { CHECKBOX: [], DATE: [], DROPDOWN: [], INPUT: [], MULTI_SELECT: [] };

    options.forEach(option => {
      if (!option) return;

      filterKeys[option.type] = option.nestedOptions?.map(nestedOption => nestedOption.key) || [option.key];
    });

    setFilterByKeys(filterKeys);
  };

  const loadFilters = async () => {
    try {
      let filteredData = await applyFilters();

      if (!isEmpty(sortBy)) {
        const key = Object.keys(sortBy)[0];
        filteredData = applySort({ filteredData, itemKey: key, sortOption: sortBy[key] });
      }

      setFilters({ ...filters, data: data, filteredData, loadingStatus: 'SUCCESS' });
    } catch (error) {
      setLoadingStatus('FAILED');
      console.log('error :>> ', error);
    }
  };

  const onApplyFilters = ({ filterBy }) => {
    return data.filter(
      item =>
        applyInputs({ filterBy, filterByKeys, item }) &&
        applyDates({ filterBy, filterByKeys, item }) &&
        applyMultiSelects({ filterBy, filterByKeys, item })
    );
  };

  const onChange = ({ key, value }) => {
    const filteredData = onApplyFilters({ filterBy: { ...filters.filterBy, [key]: value } });

    setFilters({ ...filters, filterBy: { ...filters.filterBy, [key]: value }, filteredData });
  };

  const onResetFilters = () => setFilters({ data, filteredData: data, filterBy: {} });

  const onSortData = key => {
    const sortOption = switchSortByOption(sortBy[key]);
    const sortedData = applySort({ filteredData, itemKey: key, sortOption });

    setSortBy({ [key]: sortOption });
    setFilters({ ...filters, filteredData: sortedData });
  };

  const setLoadingStatus = status => setFilters({ ...filters, loadingStatus: status });

  const updateValueLabelsAnimationDate = (labelsAnimationDate, position, key, value) => {
    if (position !== undefined && labelsAnimationDate.length > 0) {
      const copyLabelsAnimationDate = [...labelsAnimationDate];
      copyLabelsAnimationDate[position][key] = value;
      setLabelsAnimationDate(copyLabelsAnimationDate);
    }
  };

  const renderFilters = () => {
    return options.map(option => {
      if (option == null) return [];

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
    const positionLabelAnimationDate = getPositionLabelAnimationDate(labelsAnimationDate, option.key);
    const getClassNameLabelCalendar = () => {
      if (positionLabelAnimationDate && labelsAnimationDate[positionLabelAnimationDate][option.key] === false) {
        return styles.labelDown;
      } else {
        return styles.label;
      }
    };

    const inputId = uniqueId();
    if (option.nestedOptions) return option.nestedOptions.map(option => renderDate(option));

    return (
      <div className={styles.input} key={option.key}>
        {renderSortButton({ key: option.key })}
        <div
          className={`p-float-label ${styles.label}`}
          id={`calendar_${option.key}`}
          ref={el => (calendarRefs.current[option.key] = el)}>
          <Calendar
            baseZIndex={9999}
            // className={styles.calendarFilter}
            dateFormat={userProps.dateFormat.toLowerCase().replace('yyyy', 'yy')}
            inputClassName={styles.inputFilter}
            key={option.key}
            // inputId={inputId}
            monthNavigator={true}
            onChange={event => {
              onChange({ key: option.key, value: parseDateValues(event.value) });
            }}
            onFocus={() =>
              updateValueLabelsAnimationDate(labelsAnimationDate, positionLabelAnimationDate, option.key, true)
            }
            // onChange={event => onFilterData(property, event.value)}
            //placeholder={option.label}
            readOnlyInput={true}
            selectionMode="range"
            value={parseDateValues(filterBy[option.key])}
            yearNavigator={true}
            yearRange="2015:2030"
          />
          <label className={getClassNameLabelCalendar()} htmlFor={inputId}>
            {option.label || ''}
          </label>
        </div>
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
      <div className={styles.input} key={option.key}>
        {renderSortButton({ key: option.key })}
        <div className={`p-float-label ${styles.label}`}>
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
        </div>
      </div>
    );
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
      <div className={`${styles.input}`} key={option.key}>
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
          isFilter={true}
          itemTemplate={op => selectTemplate(op, option)}
          key={option.key}
          label={option.label || ''}
          notCheckAllHeader={resourcesContext.messages['uncheckAllFilter']}
          onChange={event => onChange({ key: option.key, value: event.target.value })}
          optionLabel="type"
          options={getOptionsTypes(data, option.key)}
          value={filterBy[option.key]}
        />
      </div>
    );
  };

  const renderSortButton = ({ key }) => {
    return (
      <Button
        className={`p-button-secondary-transparent ${styles.sortButton}`}
        icon={switchSortByIcon(sortBy[key])}
        onClick={() => onSortData(key)}
        style={{ fontSize: '0.12rem' }}
      />
    );
  };

  if (loadingStatus === 'PENDING') return <div>LOADING</div>;

  return (
    <div className={className ? styles[className] : styles.default}>
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

      <div className={`${styles.resetButton}`}>
        <Button
          className={`p-button-secondary p-button-rounded p-button-animated-blink`}
          icon="undo"
          label={resourcesContext.messages['reset']}
          onClick={() => {
            onResetFilters();
            setLabelsAnimationDate(getLabelsAnimationDateInitial(options));
          }}
        />
      </div>
    </div>
  );
};

MyFilters.propTypes = {
  className: PropTypes.string,
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
