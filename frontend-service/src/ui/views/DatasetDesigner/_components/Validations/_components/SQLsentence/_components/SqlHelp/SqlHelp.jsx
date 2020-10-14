import React, { useEffect, useReducer } from 'react';

const sqlHelpReducer = (state, { type, payload }) => {
  switch (type) {
    case 'UPDATE_PROPERTY':
      return {
        ...state,
        [payload.key]: payload.value
      };

    default:
      return state;
  }
};

export const SqlHelp = ({ sqlSentence, onSetSqlSentence }) => {
  const initState = {
    datasets: [],
    tabs: [],
    fields: [],
    selectedDataset: '',
    selectedTab: '',
    selectedField: ''
  };
  const [state, dispatch] = useReducer(sqlHelpReducer, initState);
  useEffect(() => {
    //call service to get datasets in mode

    //parse datasets to model

    //update datasets state
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'datasets', value: [] } });
  }, []);

  const onSelectDataset = e => {
    // select dataset
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'dataset', value: e.target.value } });
    // call service to get datasetSchema
    const rawTabs = [];
    //parse dataset to model
    const tabs = [];
    //update tabs state
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'tabs', value: tabs } });
  };

  const onSelectTab = e => {
    //select the tab
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'tab', value: e.target.value } });
    //select tab fields from schema
    const rawFields = [];
    //parse dataset to model
    const fields = [];
    //update fields state
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'fields', value: fields } });
  };

  const onSelectField = e => {
    //select the field
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'field', value: e.target.value } });
  };

  const onAddElement = element => {
    //parse insert in sql sentence the element;
    onSetSqlSentence(`${sqlSentence} ${element}`);
  };

  const onDobleClickOnElement = element => {
    //parse and insert element in sql sentence;
    onSetSqlSentence(`${sqlSentence} ${element}`);
  };
  return <div></div>;
};
