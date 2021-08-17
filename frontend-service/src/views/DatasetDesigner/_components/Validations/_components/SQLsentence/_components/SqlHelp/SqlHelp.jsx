import { useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import toLower from 'lodash/toLower';
import trim from 'lodash/trim';

import styles from './SqlHelp.module.scss';

import { SqlHelpListBox } from './_components/SqlHelpListBox';

import { DataflowService } from 'services/DataflowService';

import { parseDatasetSchemas } from './_functions/Utils/parseDatasetSchemas';
import { parseHelpItem } from './_functions/Utils/parseHelpItem';

const sqlHelpReducer = (state, { type, payload }) => {
  switch (type) {
    case 'UPDATE_PROPERTY':
      return {
        ...state,
        [payload.key]: payload.value || ''
      };

    default:
      return state;
  }
};

export const SqlHelp = withRouter(({ match, onSetSqlSentence, sqlSentence }) => {
  const initState = {
    rawDatasets: [],
    datasets: [],
    tables: [],
    fields: [],
    selectedDataset: '',
    selectedTable: '',
    selectedField: '',
    datasetSpinner: false,
    tableSpinner: false,
    fieldSpinner: false
  };
  const [state, dispatch] = useReducer(sqlHelpReducer, initState);

  const fetchData = async () => {
    const {
      params: { dataflowId }
    } = match;
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'datasetSpinner', value: true } });
    const dataflowDetails = await DataflowService.getSchemas(dataflowId);
    const { designDatasets } = await DataflowService.get(dataflowId);
    dispatch({
      type: 'UPDATE_PROPERTY',
      payload: { key: 'rawDatasets', value: { dataflowDetails, designDatasets } }
    });
  };

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    if (!isEmpty(state.rawDatasets)) {
      dispatch({
        type: 'UPDATE_PROPERTY',
        payload: { key: 'datasets', value: parseDatasetSchemas(state.rawDatasets) }
      });
    }
  }, [state.rawDatasets]);

  useEffect(() => {
    if (state.datasets) {
      dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'datasetSpinner', value: true } });
    }
  }, [state.datasets]);

  const onSelectDataset = selectedDataset => {
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'selectedDataset', value: selectedDataset } });
  };

  useEffect(() => {
    const tablesOptions =
      state?.datasets?.datasetSchemas?.find(dataset => dataset.datasetId === state.selectedDataset?.value)
        ?.tablesOptions || [];
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
      ?.find(dataset => dataset.datasetId === state.selectedDataset?.value)
      ?.tables.find(table => table.tableSchemaId === state.selectedTable?.value)?.fieldsOptions;
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'fields', value: fieldsOptions || [] } });
  }, [state.selectedTable]);

  useEffect(() => {
    if (!state.fieldsOptions) {
      onSelectField('');
    }
  }, [state.fieldsOptions]);

  useEffect(() => {
    onSelectTable('');
    onSelectField('');
  }, [state.selectedDataset]);
  useEffect(() => {
    onSelectField('');
  }, [state.selectedTable]);

  const onSelectField = field => {
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'selectedField', value: field } });
  };

  const onAddHelpItem = itemType => {
    const helpItem = parseHelpItem(itemType, state);
    if (!sqlSentence) {
      onSetSqlSentence('sqlSentence', toLower(trim(helpItem)));
    } else {
      onSetSqlSentence('sqlSentence', trim(`${sqlSentence}${toLower(helpItem)}`));
    }
  };

  return (
    <div className={styles.wrapper}>
      <SqlHelpListBox
        isSpinnerVisible={state.datasetSpinner}
        level="dataset"
        onAddHelpItem={onAddHelpItem}
        onChange={onSelectDataset}
        options={state.datasets.datasetSchemaOptions}
        selectedItem={state.selectedDataset}
        title="Dataset"
      />
      <SqlHelpListBox
        level="table"
        onAddHelpItem={onAddHelpItem}
        onChange={onSelectTable}
        options={state.tables}
        selectedItem={state.selectedTable}
        title="Tables"
      />
      <SqlHelpListBox
        level="field"
        onAddHelpItem={onAddHelpItem}
        onChange={onSelectField}
        options={state.fields}
        selectedItem={state.selectedField}
        title="Fields"
      />
    </div>
  );
});
