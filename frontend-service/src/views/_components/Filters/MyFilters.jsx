import { useLayoutEffect, useContext, useEffect, useReducer } from 'react';
import { useRecoilCallback, useRecoilState } from 'recoil';
import PropTypes from 'prop-types';

import isNil from 'lodash/isNil';

import styles from './MyFilters.module.scss';

import { Button } from 'views/_components/Button';
import { InputText } from 'views/_components/InputText';
import { MultiSelect } from 'views/_components/MultiSelect';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { filtersStateFamily } from './_functions/Stores/filtersStores';

export const MyFilters = ({ data, getFilteredData, isSearchVisible, isStrickMode, onFilter, options, viewType }) => {
  const [filters, setFilters] = useRecoilState(filtersStateFamily(viewType));

  // const updateRecord = useRecoilCallback(({ set }, record) => {
  //   set(filtersStateFamily(viewType), record);
  // });

  const { filterBy, filteredData, loadingStatus } = filters;

  console.log('filters :>> ', filters);

  const resourcesContext = useContext(ResourcesContext);

  // const [filtersState, filtersDispatch] = useReducer(filtersReducer, {
  //   data,
  //   filterBy: {},
  //   filteredData: data,
  //   loadingStatus: 'IDLE'
  // });

  // const { filterBy, filteredData, loadingStatus } = filtersState;

  // useLayoutEffect(() => {
  //   loadFilters();
  // }, []);

  useEffect(() => {
    if (getFilteredData) getFilteredData(filteredData);
  }, [filteredData]);

  const loadFilters = () => {
    setLoadingStatus('PENDING');

    try {
      setFilters({ ...filters, data: data, filteredData: data, loadingStatus: 'SUCCESS' });
      // setLoadingStatus('SUCCESS');
    } catch (error) {
      setLoadingStatus('FAILED');
      console.log('error :>> ', error);
    }
  };

  const onClear = () => {};

  const onChange = ({ key, value }) => {
    const test = doFilter({ key, value });

    setFilters({ ...filters, filterBy: { ...filters.filterBy, [key]: value }, filteredData: test });
  };

  const doFilter = ({ key, value }) => {
    return filteredData.filter(item => item[key].toLowerCase().includes(value.toLowerCase()));
  };

  const setLoadingStatus = status => setFilters({ ...filters, loadingStatus: status });

  const renderFilters = () => {
    return options.map(option => {
      switch (option.type) {
        case 'CHECKBOX':
          return [];

        case 'DATE':
          return [];

        case 'DROPDOWN':
          return [];

        case 'INPUT':
          return renderInput(option);

        case 'MULTI_SELECT':
          return renderMultiSelect(option);

        default:
          throw new Error('The option type is not correct.');
      }
    });
  };

  //   const renderDate = () => {};

  //   const renderDropdown = () => {};

  const renderInput = option => {
    if (option.options) {
      return option.options.map(option => renderInput(option));
    }

    return (
      <InputText
        key={option.key}
        onChange={event => onChange({ key: option.key, value: event.target.value })}
        placeholder={option.label}
        value={filterBy[option.key] || ''}
      />
    );
  };

  const renderMultiSelect = option => {
    if (option.options) {
      return option.options.map(option => renderMultiSelect(option));
    }

    return <MultiSelect key={option.key} />;
  };

  if (loadingStatus === 'PENDING') return <div>LOADING</div>;

  return (
    <div className={styles.filters}>
      {isSearchVisible ? <InputText placeholder="Search" /> : null}
      {renderFilters()}
      {isStrickMode ? <InputText placeholder="StrictMode" /> : null}

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

const filtersReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_LOADING_STATUS':
      return { ...state, loadingStatus: payload.status };

    default:
      return state;
  }
};

MyFilters.propTypes = {
  data: PropTypes.array,
  getFilteredData: PropTypes.func,
  isSearchVisible: PropTypes.bool,
  isStrickMode: PropTypes.bool,
  onFilter: PropTypes.func,
  options: PropTypes.object,
  viewType: PropTypes.string
};

MyFilters.defaultProps = {
  data: [],
  getFilteredData: null,
  isSearchVisible: false,
  isStrickMode: false,
  onFilter: null,
  options: []
};
