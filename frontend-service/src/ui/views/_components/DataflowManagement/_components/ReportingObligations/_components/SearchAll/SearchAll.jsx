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
        return { ...state, searchedData: payload.test, searchBy: payload.value };

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

  const onLoadK = () => {
    if (!isNil(searchState.data[0])) {
      return Object.keys(searchState.data[0]).filter(item => item !== 'id');
    }
  };

  const keis = onLoadK();

  // console.log('searchState', searchState);

  const onLoad = value => [
    ...searchState.data.filter(data => {
      // console.log('data', data);
      // for (let index = 0; index < data.length; index++) {
      if (keis) {
        // keis.forEach(key => {
        // console.log('hei!!');
        // console.log('INCLUDES:', data[key].toLowerCase().includes(value.toLowerCase()));
        return (
          data['title'].toLowerCase().includes(value.toLowerCase()) ||
          data['legalInstrument'].toLowerCase().includes(value.toLowerCase()) ||
          data['dueDate'].toLowerCase().includes(value.toLowerCase())
        );
        // });
      }
      return true;
      // }
    })
  ];

  const onSearchData = (search, value) => {
    const test = onLoad(value);

    searchDispatch({ type: 'ON_SEARCH_DATA', payload: { test, value } });
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
