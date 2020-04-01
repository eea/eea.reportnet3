import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './SearchAll.module.scss';

import { Button } from 'ui/views/_components/Button';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { SearchUtils } from './_functions/Utils/SearchUtils';

export const SearchAll = ({ data, getValues }) => {
  const resources = useContext(ResourcesContext);

  const searchReducer = (state, { type, payload }) => {
    switch (type) {
      case 'INITIAL_LOAD':
        return { ...state, ...payload };

      case 'ON_SEARCH_DATA':
        return { ...state, searchBy: payload.value };

      default:
        return state;
    }
  };

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

  // const onLoadI = !isEmpty(searchState.data) ? Object.values(searchState.data) : null;

  const onLoadInitialState = () => searchDispatch({ type: 'INITIAL_LOAD', payload: { data, searchedData: data } });

  const onSearchData = (search, value) => {
    // const searche = searchState.data.toLowerCase().includes(value.toLowerCase());
    for (let index = 0; index < searchState.data.length; index++) {
      const element = searchState.data[index];
    }
    searchDispatch({ type: 'ON_SEARCH_DATA', payload: { value } });
  };

  return (
    <span className={`p-float-label ${styles.dataflowInput}`}>
      <InputText
        className={styles.searchInput}
        id={'searchInput'}
        onChange={event => onSearchData('test', event.target.value)}
        value={searchState.searchBy}
      />
      {searchState.searchBy && (
        <Button
          className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
          icon="cancel"
          onClick={() => onSearchData('', '')}
        />
      )}
      <label htmlFor={'searchInput'}>{resources.messages['searchAllLabel']}</label>
    </span>
  );
};
