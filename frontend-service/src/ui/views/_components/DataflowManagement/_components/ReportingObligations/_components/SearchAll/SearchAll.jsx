import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

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
        return { ...state, searchedData: payload.searchedValues, searchBy: payload.value };

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

  const onLoadInitialState = () => searchDispatch({ type: 'INITIAL_LOAD', payload: { data, searchedData: data } });

  const getSearchKeys = () => {
    if (!isNil(searchState.data[0])) {
      return Object.keys(searchState.data[0]).filter(item => item !== 'id');
    }
  };

  const searchKeys = getSearchKeys();

  const onApplySearch = value => [
    ...searchState.data.filter(data => {
      if (searchKeys) {
        return (
          data['title'].toLowerCase().includes(value.toLowerCase()) ||
          data['legalInstrument'].toLowerCase().includes(value.toLowerCase()) ||
          data['dueDate'].toLowerCase().includes(value.toLowerCase())
        );
      }
      return true;
    })
  ];

  const onSearchData = value => {
    const searchedValues = onApplySearch(value);

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
