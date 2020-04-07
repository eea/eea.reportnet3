import React, { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './SearchAll.module.scss';

import { Button } from 'ui/views/_components/Button';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { searchReducer } from './_functions/Reducers/searchReducer';

import { SearchUtils } from './_functions/Utils/SearchUtils';

export const SearchAll = ({ data, getValues, searchInitialState }) => {
  const resources = useContext(ResourcesContext);

  const [searchState, searchDispatch] = useReducer(searchReducer, {
    data: [],
    searchedData: [],
    searchBy: ''
  });

  useEffect(() => {
    onLoadInitialState();
  }, [data]);

  useEffect(() => {
    if (getValues) getValues(searchState.searchedData);
  }, [searchState.searchedData]);

  const onLoadInitialState = () =>
    searchDispatch({
      type: 'INITIAL_LOAD',
      payload: { data, searchedData: isEmpty(searchState.searchBy) ? data : searchInitialState }
    });

  const onSearchData = value => {
    const searchedValues = SearchUtils.onApplySearch(searchState.data, value);

    searchDispatch({ type: 'ON_SEARCH_DATA', payload: { searchedValues, value } });
  };

  return (
    <span className={`p-float-label ${styles.dataflowInput}`}>
      <InputText
        className={styles.searchInput}
        id={'searchInput'}
        onChange={event => onSearchData(event.target.value)}
        value={searchState.searchBy}
      />
      {searchState.searchBy && (
        <Button
          className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
          icon="cancel"
          onClick={() => onSearchData('')}
        />
      )}
      <label htmlFor={'searchInput'}>{resources.messages['searchAllLabel']}</label>
    </span>
  );
};
