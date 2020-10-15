import React, { useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './SqlHelp.module.scss';

import { SqlHelpListBox } from './_components/SqlHelpListBox';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'core/services/Dataflow';

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

export const SqlHelp = withRouter(({ history, match, onSetSqlSentence, sqlSentence }) => {
  const initState = {
    datasets: [],
    tabs: [],
    fields: [],
    selectedDataset: '',
    selectedTab: '',
    selectedField: ''
  };
  const resourcesContext = useContext(ResourcesContext);
  const [state, dispatch] = useReducer(sqlHelpReducer, initState);
  useEffect(async () => {
    //call service to get datasets in mode
    const {
      params: { dataflowId }
    } = match;
    const dataflowDetails = parseDatasetSchemas(await DataflowService.getAllSchemas(dataflowId));
    console.log('dataflowDetails', dataflowDetails);

    //parse datasets to model

    //update datasets state
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'datasets', value: [] } });
  }, []);

  const parseDatasetSchemas = rowDatasetSchemas => {
    const datasetSchemas = {
      datasetSchemaOptions: [],
      datasetSchemas: []
    };
    for (rowDatasetSchema of rowDatasetSchemas) {
      const option = { label: rowDatasetSchema.datasetSchemaName, value: rowDatasetSchema.datasetSchemaId };
      const datasetSchema = parseDatasetSchema(datasetSchema);
      datasetSchemas.datasetSchemaOptions.push(option);
      datasetSchemas.datasetSchemas.push(datasetSchema);
    }
  };

  const getDataflowDetails = async () => {
    const {
      params: { dataflowId }
    } = match;
    return await DataflowService.dataflowDetails(dataflowId);
  };

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

  const onDoubleClickOnElement = element => {
    //parse and insert element in sql sentence;
    onSetSqlSentence(`${sqlSentence} ${element}`);
  };
  return (
    <div className={styles.wrapper}>
      <SqlHelpListBox title="Dataset" selectedItem={state.selectedDataset} />
      <SqlHelpListBox title="Tables" selectedItem={state.selectedDataset} />
      <SqlHelpListBox title="Fields" selectedItem={state.selectedDataset} />
    </div>
  );
});
