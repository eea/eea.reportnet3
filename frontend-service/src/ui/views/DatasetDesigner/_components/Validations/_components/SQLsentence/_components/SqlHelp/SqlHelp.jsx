import React, { useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './SqlHelp.module.scss';

import { SqlHelpListBox } from './_components/SqlHelpListBox';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'core/services/Dataflow';

import { parseDatasetSchemas } from './_functions/utils/parseDatasetSchemas';

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
    tables: [],
    fields: [],
    selectedDataset: '',
    selectedTable: '',
    selectedField: ''
  };
  const resourcesContext = useContext(ResourcesContext);
  const [state, dispatch] = useReducer(sqlHelpReducer, initState);

  useEffect(async () => {
    const {
      params: { dataflowId }
    } = match;

    const dataflowDetails = parseDatasetSchemas(await DataflowService.getAllSchemas(dataflowId));
    console.log('dataflowDetails', dataflowDetails);
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'datasets', value: dataflowDetails } });
  }, []);

  const getDataflowDetails = async () => {
    const {
      params: { dataflowId }
    } = match;
    return await DataflowService.dataflowDetails(dataflowId);
  };

  const onSelectDataset = selectedDataset => {
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'selectedDataset', value: selectedDataset } });
  };

  useEffect(() => {
    const tablesOptions = state?.datasets?.datasetSchemas?.find(
      dataset => dataset.datasetSchemaId === state.selectedDataset?.value
    )?.tablesOptions;
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'tables', value: tablesOptions } });
  }, [state.selectedDataset]);

  useEffect(() => {
    if (!state.tables) {
      onSelectTable('');
    }
  }, [state.tables]);

  const onSelectTable = table => {
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'selectedTable', value: table } });
  };

  useEffect(() => {
    const fieldsOptions = state?.datasets?.datasetSchemas
      ?.find(dataset => dataset.datasetSchemaId === state.selectedDataset?.value)
      ?.tables.find(table => table.tableSchemaId === state.selectedTable?.value)?.fieldsOptions;
    console.log('fieldsOptions', fieldsOptions);
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'fields', value: fieldsOptions || [] } });
  }, [state.selectedTable]);

  useEffect(() => {
    if (!state.fieldsOptions) {
      onSelectField('');
    }
  }, [state.fieldsOptions]);

  const onSelectField = field => {
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'selectedField', value: field } });
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
      <SqlHelpListBox
        title="Dataset"
        selectedItem={state.selectedDataset}
        options={state.datasets.datasetSchemaOptions}
        onChange={onSelectDataset}
      />
      <SqlHelpListBox
        title="Tables"
        selectedItem={state.selectedTable}
        options={state.tables}
        onChange={onSelectTable}
      />
      <SqlHelpListBox
        title="Fields"
        selectedItem={state.selectedField}
        options={state.fields}
        onChange={onSelectField}
      />
    </div>
  );
});
