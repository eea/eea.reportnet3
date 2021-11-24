import { useEffect, useContext, useReducer } from 'react';
import PropTypes from 'prop-types';

import isNil from 'lodash/isNil';

import styles from './MyFilters.module.scss';

import { Button } from 'views/_components/Button';
import { InputText } from 'views/_components/InputText';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

// filterOptions = [
//   { name: 'campoDelListado', label: messages..., type: 'multiselect', special: 'levelError' },
//   { name: 'campoDelListado2', label: messages..., type: 'multiselect' }
// ]

// { type: 'input', properties: [{ name: 'integrationName' }] },

const options = [
  { category: 'LEVEL_ERROR', key: 'integrationName', label: 'Label', order: 0, type: 'INPUT' },
  { category: 'LEVEL_ERROR', key: 'operationName', label: 'Label', order: 1, type: 'INPUT' },
  { category: 'LEVEL_ERROR', key: 'anotherKeyName', label: 'Label', options: [], order: 2, type: 'MULTI_SELECT' }
];

export const MyFilters = ({ data, getFilteredData, isSearchVisible, isStrickMode, onFilter, options }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [filtersState, filtersDispatch] = useReducer(filtersReducer, {
    data,
    filterBy: {},
    filteredData: data,
    loadingStatus: 'IDLE'
  });

  const { filterBy, filteredData, loadingStatus } = filtersState;

  useEffect(() => {
    loadFilters();
  }, []);

  useEffect(() => {
    if (getFilteredData) getFilteredData(filteredData);
  }, [filteredData]);

  const loadFilters = () => {};

  const onClear = () => {};

  const renderFilters = () => {
    return options.map(option => {
      switch (option.type) {
        // case 'checkbox':
        //   return option.properties.map(property => renderInput({ label: property.label, name: property.name }));

        // case 'date':
        //   return option.properties.map(property => renderInput({ label: property.label, name: property.name }));

        // case 'dropdown':
        //   return option.properties.map(property => renderInput({ label: property.label, name: property.name }));

        case 'INPUT':
          return option.properties.map(property => renderInput({ label: property.label, property: property.name }));

        // case 'multiSelect':
        //   return option.properties.map(property => renderInput({ label: property.label, name: property.name }));

        default:
          return [];
      }
    });
  };

  //   const renderDate = () => {};

  //   const renderDropdown = () => {};

  const renderInput = ({ label, property }) => <InputText />;

  //   const renderMultiSelect = () => {};

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
        onClick={onClear}
      />
    </div>
  );
};

const filtersReducer = (state, { type, payload }) => {
  switch (type) {
    case 'typeName':
      return { ...state, ...payload };

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
  options: PropTypes.object
};

MyFilters.defaultProps = {
  data: [],
  getFilteredData: null,
  isSearchVisible: false,
  isStrickMode: false,
  onFilter: null,
  options: []
};
