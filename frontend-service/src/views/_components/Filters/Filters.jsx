import { useContext, useEffect, useReducer, useRef } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';

import styles from './Filters.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'views/_components/Calendar';
import { Checkbox } from 'views/_components/Checkbox';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { LevelError } from 'views/_components/LevelError';
import { MultiSelect } from 'views/_components/MultiSelect';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { filterReducer } from './_functions/Reducers/filterReducer';

import { useOnClickOutside } from 'views/_functions/Hooks/useOnClickOutside';

import { ApplyFilterUtils } from './_functions/Utils/ApplyFilterUtils';
import { ErrorUtils } from 'views/_functions/Utils';
import { FiltersUtils } from './_functions/Utils/FiltersUtils';
import { SortUtils } from './_functions/Utils/SortUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const Filters = ({
  className,
  data = [],
  dropDownList,
  filterByList,
  getFilteredData,
  getFilteredSearched = () => {},
  matchMode,
  options = [],
  searchAll,
  searchBy = [],
  selectList,
  sendData,
  sortable,
  sortCategory,
  validations,
  validationsAllTypesFilters
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const dateRef = useRef(null);

  const [filterState, filterDispatch] = useReducer(filterReducer, {
    checkboxes: [],
    clearedFilters: false,
    data: data,
    filterBy: {},
    filtered: false,
    filteredData: data,
    filteredSearched: false,
    isOrdering: false,
    labelAnimations: {},
    matchMode: true,
    orderBy: {},
    previousState: {},
    property: '',
    searchBy: '',
    searched: false
  });

  useEffect(() => {
    if (isNil(sendData)) getInitialState();
  }, [data]);

  useEffect(() => {
    if (filterState.filtered && !filterState.isOrdering) parsePrevFilters();
  }, [filterState.data]);

  useEffect(() => {
    if (getFilteredData) getFilteredData(filterState.filteredData);
  }, [filterState.filteredData]);

  useEffect(() => {
    onReApplyFilters(Object.keys(filterState.filterBy), filterState.filterBy);
  }, [filterState.matchMode]);

  useEffect(() => {
    getFilteredSearchedStateValue();
  }, [filterState.filtered, filterState.searched]);

  useEffect(() => {
    getFilteredState();
  }, [filterState.filterBy]);

  useEffect(() => {
    getFilteredSearched(filterState.filteredSearched);
  }, [filterState.filteredSearched]);

  useEffect(() => {
    getChangedCheckboxes(filterState.property);
  }, [JSON.stringify(filterState.checkboxes), filterState.property]);

  useEffect(() => {
    if (sendData && filterState.clearedFilters) {
      sendData(filterState.filterBy);
      filterDispatch({ type: 'SET_CLEARED_FILTERS', payload: false });
    }
  }, [filterState.clearedFilters]);

  const { input, multiselect, date, dropdown, checkbox } = FiltersUtils.getOptionsNames(options);

  useOnClickOutside(dateRef, () => isEmpty(filterState.filterBy[date]) && onAnimateLabel([date], false));

  const getCheckboxFilterState = property => {
    const [checkBox] = filterState.checkboxes.filter(checkbox => checkbox.property === property);
    return isNil(checkBox) ? false : checkBox.isChecked;
  };

  const getFilteredSearchedStateValue = () => {
    const filteredSearchedValue = filterState.filtered || filterState.searched ? true : false;
    filterDispatch({ type: 'FILTERED_SEARCHED_STATE', payload: { filteredSearchedValue } });
  };

  const getFilteredState = () => {
    let filteredStateValue = false;
    if (filterState.checkboxes.length > 0) {
      const filtersValue = [];
      Object.values(filterState.filterBy).forEach(value => {
        !isEmpty(value) && filtersValue.push(!isEmpty(value));
        if (value.includes(true) && value.includes(false)) {
          filtersValue.pop();
        }
        filteredStateValue = filtersValue.includes(true);
      });
    } else {
      filteredStateValue = Object.values(filterState.filterBy)
        .map(value => isEmpty(value))
        .includes(false);
    }

    filterDispatch({ type: 'FILTERED', payload: { filteredStateValue } });
  };

  const getInitialState = () => {
    const initialData = cloneDeep(data);

    const initialFilterBy = FiltersUtils.getFilterInitialState(
      data,
      input,
      multiselect,
      date,
      dropdown,
      checkbox,
      filterByList
    );

    const initialFilteredData = ApplyFilterUtils.onApplySearch(data, searchBy, filterState.searchBy, filterState);

    const initialLabelAnimations = FiltersUtils.getLabelInitialState(
      input,
      multiselect,
      date,
      dropdown,
      checkbox,
      filterState.filterBy
    );

    const initialOrderBy = SortUtils.getOrderInitialState(input, multiselect, date, dropdown, checkbox);

    const initialCheckboxes = FiltersUtils.getCheckboxFilterInitialState(checkbox);

    filterDispatch({
      type: 'INITIAL_STATE',
      payload: {
        initialData,
        initialFilterBy,
        initialFilteredData,
        initialLabelAnimations,
        initialOrderBy,
        initialCheckboxes
      }
    });
  };

  const onAnimateLabel = (property, value) => {
    filterDispatch({ type: 'ANIMATE_LABEL', payload: { animatedProperty: property, isAnimated: value } });
  };

  const getChangedCheckboxes = property => {
    filterState.checkboxes.forEach(checkbox => {
      if (checkbox.property === property) {
        checkbox.isChecked
          ? onFilterData(checkbox.property, [checkbox.isChecked])
          : onFilterData(checkbox.property, [checkbox.isChecked, !checkbox.isChecked]);
      }
    });
  };

  const onChangeCheckboxFilter = property => {
    filterDispatch({ type: 'ON_CHECKBOX_FILTER', payload: { property } });
  };

  const onClearAllFilters = () => {
    filterDispatch({
      type: 'CLEAR_ALL',
      payload: {
        filterBy: FiltersUtils.getFilterInitialState(data, input, multiselect, date, dropdown, checkbox),
        filteredData: cloneDeep(data),
        labelAnimations: ApplyFilterUtils.onClearLabelState(input, multiselect, date, dropdown, checkbox),
        orderBy: SortUtils.getOrderInitialState(input, multiselect, date, dropdown, checkbox),
        searchBy: '',
        checkboxes: FiltersUtils.getCheckboxFilterInitialState(checkbox),
        filtered: false,
        filteredSearched: false,
        clearedFilters: true
      }
    });
  };

  const onFilterData = (filter, value, actualFilterBy) => {
    const inputKeys = FiltersUtils.getFilterKeys(filterState, filter, input);

    const searchedKeys = !isEmpty(searchBy) ? searchBy : ApplyFilterUtils.getSearchKeys(filterState.data);

    const selectedKeys = FiltersUtils.getSelectedKeys(filterState, filter, multiselect);

    const checkedKeys = FiltersUtils.getSelectedKeys(filterState, filter, checkbox);

    const filteredData = ApplyFilterUtils.onApplyFilters({
      actualFilterBy,
      checkbox,
      checkedKeys,
      data,
      date,
      filter,
      filteredKeys: inputKeys,
      searchedKeys,
      selectedKeys,
      multiselect,
      state: filterState,
      value
    });

    filterDispatch({ type: 'FILTER_DATA', payload: { filteredData, filter, value } });
  };

  const onOrderData = (order, property) => {
    const { input, multiselect, date } = FiltersUtils.getOptionsNames(options);

    const sortedData = SortUtils.onSortData([...filterState.data], order, property, sortCategory);
    const filteredSortedData = SortUtils.onSortData([...filterState.filteredData], order, property, sortCategory);
    const orderBy = order === 0 ? -1 : order;
    const resetOrder = SortUtils.onResetOrderData(input, multiselect, date, checkbox);

    filterDispatch({ type: 'ORDER_DATA', payload: { filteredSortedData, orderBy, property, resetOrder, sortedData } });
  };

  const onSearchData = value => {
    const { input, multiselect } = FiltersUtils.getOptionsNames(options);
    const inputKeys = FiltersUtils.getFilterKeys(filterState, '', input);
    const selectedKeys = FiltersUtils.getSelectedKeys(filterState, '', multiselect);
    const checkedKeys = FiltersUtils.getSelectedKeys(filterState, '', checkbox);
    const searchedValues = ApplyFilterUtils.onApplySearch(
      filterState.data,
      searchBy,
      value,
      filterState,
      inputKeys,
      selectedKeys,
      checkedKeys
    );

    const isSearched = !isEmpty(value);

    filterDispatch({ type: 'ON_SEARCH_DATA', payload: { searchedValues, value, searched: isSearched } });
  };

  const onToggleMatchMode = () => filterDispatch({ type: 'TOGGLE_MATCH_MODE', payload: !filterState.matchMode });

  const parsePrevFilters = () => {
    let filterBy = { ...filterState.filterBy };

    const filterKeys = Object.keys(filterBy);

    if (!isEmpty(filterBy)) {
      filterBy = filterState.previousState.filterBy;

      const possibleOptions = new Map();

      filterKeys.forEach(key => possibleOptions.set(key, []));

      const initialFilteredData = ApplyFilterUtils.onApplySearch(data, searchBy, filterState.searchBy, filterState);

      initialFilteredData.forEach(item =>
        multiselect.forEach(filterKey => {
          const currentValue = possibleOptions.get(filterKey);

          currentValue.push(item[filterKey]);
        })
      );

      const distinct = (value, index, self) => self.indexOf(value) === index;

      const removeInexistentMultiselectFilters = entryKeyValue => {
        const [entryKey, entryValue] = entryKeyValue;

        multiselect.forEach(multiselectKey => {
          const option = possibleOptions.get(multiselectKey).filter(distinct);

          if (multiselectKey === entryKey && !isEmpty(entryValue)) {
            entryKeyValue[1] = entryValue?.filter(value => option.some(opt => TextUtils.areEquals(opt, value)));
          }
        });

        return entryKeyValue;
      };

      const parsedResult = Object.entries(filterBy).map(removeInexistentMultiselectFilters);

      filterBy = Object.fromEntries(parsedResult);

      filterDispatch({ type: 'UPDATE_FILTER_BY', payload: { filterBy } });
    }

    onReApplyFilters(filterKeys, filterBy);
  };

  const onReApplyFilters = (filterKeys, filterBy) => {
    for (let index = 0; index < filterKeys.length; index++) {
      const filter = filterKeys[index];
      const value = filterBy[filter];

      if (!isEmpty(value)) {
        onFilterData(filter, filterBy[filter], filterBy);
      }
    }
  };

  const renderCalendarFilter = (property, label) => {
    const inputId = uniqueId();
    return (
      <span className={styles.input} key={property} ref={dateRef}>
        {renderOrderFilter(property)}
        <span className={`p-float-label ${!sendData ? styles.label : ''}`}>
          <Calendar
            baseZIndex={9999}
            className={styles.calendarFilter}
            dateFormat={userContext.userProps.dateFormat.toLowerCase().replace('yyyy', 'yy')}
            inputClassName={styles.inputFilter}
            inputId={inputId}
            monthNavigator={true}
            onChange={event => onFilterData(property, event.value)}
            onFocus={() => onAnimateLabel(property, true)}
            readOnlyInput={true}
            selectionMode="range"
            showWeek={true}
            style={{ zoom: '0.95' }}
            value={filterState.filterBy[property]}
            yearNavigator={true}
            yearRange="2015:2030"
          />
          {!isEmpty(filterState.filterBy[property]) && (
            <Button
              className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
              icon="cancel"
              onClick={() => {
                onFilterData(property, []);
                onAnimateLabel(property, false);
                document.getElementById(inputId).value = '';
              }}
            />
          )}
          <label className={!filterState.labelAnimations[property] ? styles.labelDown : styles.label} htmlFor={inputId}>
            {isEmpty(label) ? resourcesContext.messages[property] : label}
          </label>
        </span>
        <label className="srOnly" htmlFor={inputId}>
          {resourcesContext.messages[property]}
        </label>
      </span>
    );
  };

  const matchModeCheckbox = () => (
    <span className={styles.checkboxWrap} data-for="checkboxTooltip" data-tip>
      {resourcesContext.messages['strictModeCheckboxFilter']}
      <Button
        className={`${styles.strictModeInfoButton} p-button-rounded p-button-secondary-transparent`}
        icon="infoCircle"
        tooltip={resourcesContext.messages['strictModeTooltip']}
        tooltipOptions={{ position: 'top' }}
      />
      <span className={styles.checkbox}>
        <Checkbox
          ariaLabel={resourcesContext.messages['strictModeCheckboxFilter']}
          checked={filterState.matchMode}
          id="matchMode_checkbox"
          inputId="matchMode_checkbox"
          onChange={() => onToggleMatchMode()}
          role="checkbox"
        />
      </span>
    </span>
  );

  const renderCheckboxFilter = (property, label, i) => {
    return (
      <span className={styles.inputCheckbox} key={i}>
        <div className={styles.flex}>
          <span className={styles.switchTextInput}>{label}</span>
          <span className={styles.checkbox}>
            <Checkbox
              ariaLabel={resourcesContext.messages[property]}
              checked={getCheckboxFilterState(property)}
              id={property}
              inputId={property}
              label={property}
              onChange={() => onChangeCheckboxFilter(property)}
              style={{ marginRight: '50px' }}
            />
            <label className="srOnly" htmlFor={property}>
              {resourcesContext.messages[property]}
            </label>
          </span>
        </div>
      </span>
    );
  };

  const renderDropdown = property => (
    <span className={`${styles.input}`} key={property}>
      {renderOrderFilter(property)}
      <Dropdown
        ariaLabel={resourcesContext.messages[property]}
        className={styles.dropdownFilter}
        filter={FiltersUtils.getOptionsTypes(data, property, dropDownList).length > 10}
        filterPlaceholder={resourcesContext.messages[property]}
        id={`${property}_dropdown`}
        inputClassName={`p-float-label ${styles.label}`}
        inputId={property}
        label={resourcesContext.messages[property]}
        onChange={event => onFilterData(property, event.value)}
        onMouseDown={event => {
          event.preventDefault();
          event.stopPropagation();
        }}
        optionLabel="type"
        options={FiltersUtils.getOptionsTypes(data, property, dropDownList)}
        showClear={!isEmpty(filterState.filterBy[property])}
        showFilterClear={true}
        value={filterState.filterBy[property]}
      />
    </span>
  );

  const renderInputFilter = (property, label = '') => (
    <span className={styles.input} key={property}>
      {renderOrderFilter(property)}
      <span className={`p-float-label ${styles.label}`}>
        <InputText
          className={styles.inputFilter}
          id={`${property}_input`}
          name={resourcesContext.messages[property]}
          onChange={event => onFilterData(property, event.target.value)}
          value={filterState.filterBy[property] ? filterState.filterBy[property] : ''}
        />
        {filterState.filterBy[property] && (
          <Button
            className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
            icon="cancel"
            onClick={() => onFilterData(property, '')}
          />
        )}
        <label className={styles.label} htmlFor={`${property}_input`}>
          {isEmpty(label) ? resourcesContext.messages[property] : label}
        </label>
      </span>
    </span>
  );

  const renderOrderFilter = property =>
    sortable && (
      <Button
        className={`p-button-secondary-transparent ${styles.icon}`}
        icon={SortUtils.getOrderIcon(filterState.orderBy[property])}
        id={`${property}_sort`}
        layout="simple"
        onClick={() => onOrderData(filterState.orderBy[property], property)}
        style={{ fontSize: '12pt' }}
        tooltip={resourcesContext.messages['sort']}
        tooltipOptions={{ position: 'bottom' }}
        value={`${property}_sortOrder`}
      />
    );

  const renderMultiselectSelectFilter = (property, showInput, label = '') => (
    <span className={`${styles.input}`} key={property}>
      {renderOrderFilter(property)}
      <MultiSelect
        ariaLabelledBy={`${property}_input`}
        checkAllHeader={resourcesContext.messages['checkAllFilter']}
        className={styles.multiselectFilter}
        filter={showInput}
        headerClassName={styles.selectHeader}
        id={property}
        inputClassName={`p-float-label ${styles.label}`}
        inputId={`${property}_input`}
        isFilter
        itemTemplate={selectTemplate}
        label={isEmpty(label) ? resourcesContext.messages[property] : label}
        notCheckAllHeader={resourcesContext.messages['uncheckAllFilter']}
        onChange={event => onFilterData(property, event.value)}
        optionLabel="type"
        options={
          validations
            ? FiltersUtils.getValidationsOptionTypes(validationsAllTypesFilters, property)
            : FiltersUtils.getOptionsTypes(data, property, selectList, ErrorUtils.orderLevelErrors)
        }
        value={filterState.filterBy[property]}
      />
    </span>
  );

  const renderSearchAll = () => (
    <span className={`p-float-label ${styles.input}`}>
      <InputText
        className={styles.searchInput}
        id={'searchInput'}
        onChange={event => onSearchData(event.target.value)}
        value={filterState.searchBy}
      />
      {filterState.searchBy && (
        <Button
          className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
          icon="cancel"
          onClick={() => onSearchData('')}
        />
      )}

      <label
        className={styles.label}
        dangerouslySetInnerHTML={{
          __html: TextUtils.parseText(resourcesContext.messages['searchAllLabel'], {
            searchData: !isEmpty(searchBy) ? `(${getSearchByLabelParams(searchBy).join(', ').toLowerCase()})` : ''
          })
        }}
        htmlFor={'searchInput'}></label>
    </span>
  );

  const getSearchByLabelParams = (searchBy = []) => searchBy.map(key => resourcesContext.messages[key]);

  const filtersRenderer = () => {
    return options.map(filterOption => {
      switch (filterOption.type) {
        case 'input':
          return filterOption.properties.map(property => renderInputFilter(property.name, property.label));
        case 'multiselect':
          return filterOption.properties.map(property =>
            renderMultiselectSelectFilter(property.name, property.showInput, property.label)
          );
        case 'dropdown':
          return filterOption.properties.map(property => renderDropdown(property.name));
        case 'checkbox':
          return filterOption.properties.map((property, i) => renderCheckboxFilter(property.name, property.label, i));
        case 'date':
          return filterOption.properties.map(property => renderCalendarFilter(property.name, property.label));
        default:
          return '';
      }
    });
  };

  const selectTemplate = option => {
    if (!isNil(option.type)) {
      return <LevelError type={option.type} />;
    }
  };

  return (
    <div className={className ? styles[className] : styles.header}>
      {searchAll && renderSearchAll()}
      {filtersRenderer()}
      {matchMode && matchModeCheckbox()}

      <div className={styles.buttonWrapper} style={{ width: sendData ? 'inherit' : '' }}>
        {sendData && (
          <Button
            className={`p-button-animated-blink ${styles.sendButton}`}
            icon="filter"
            label={resourcesContext.messages['applyFilters']}
            onClick={() => sendData(filterState.filterBy)}
          />
        )}

        {(input || multiselect || date || checkbox) && (
          <Button
            className={`${
              sendData ? 'p-button-secondary' : 'p-button-secondary'
            } p-button-rounded  p-button-animated-blink`}
            icon="undo"
            label={resourcesContext.messages['reset']}
            onClick={() => onClearAllFilters()}
            style={{ marginLeft: sendData ? '1rem' : '' }}
          />
        )}
      </div>
    </div>
  );
};
