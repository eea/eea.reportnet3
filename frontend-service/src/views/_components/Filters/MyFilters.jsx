import { useContext, useEffect, useLayoutEffect } from 'react';
import { useRecoilState } from 'recoil';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './MyFilters.module.scss';

import { Button } from 'views/_components/Button';
import { InputText } from 'views/_components/InputText';
import { MultiSelect } from 'views/_components/MultiSelect';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { filtersStateFamily } from './_functions/Stores/filtersStores';

export const MyFilters = ({ data, getFilteredData, isSearchVisible, isStrictMode, onFilter, options, viewType }) => {
  const [filters, setFilters] = useRecoilState(filtersStateFamily(viewType));

  const { filterBy, filteredData, loadingStatus } = filters;

  console.log('filterBy :>> ', filterBy);

  const resourcesContext = useContext(ResourcesContext);

  useLayoutEffect(() => {
    loadFilters();
  }, []);

  useEffect(() => {
    if (getFilteredData) getFilteredData(filteredData);
  }, [filteredData]);

  const applyFilters = () => {
    if (isEmpty(filterBy)) return data;

    return data.filter(item => {
      return Object.keys(filterBy).map(filterKey =>
        item[filterKey].toLowerCase().includes(filterBy[filterKey].toLowerCase())
      );
    });
  };

  const loadFilters = () => {
    setLoadingStatus('PENDING');
    const filteredData = applyFilters();

    try {
      setFilters({ ...filters, data: data, filteredData, loadingStatus: 'SUCCESS' });
    } catch (error) {
      setLoadingStatus('FAILED');
      console.log('error :>> ', error);
    }
  };

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
  options: PropTypes.object,
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
