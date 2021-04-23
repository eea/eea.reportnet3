import { useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import trim from 'lodash/trim';

import styles from './SqlHelp.module.scss';

import { SqlHelpListBox } from './_components/SqlHelpListBox';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'core/services/Dataflow';

import { parseDatasetSchemas } from './_functions/utils/parseDatasetSchemas';
import { parseHelpItem } from './_functions/utils/parseHelpItem';
import { toLower } from 'lodash';

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
  const resourcesContext = useContext(ResourcesContext);
  const [state, dispatch] = useReducer(sqlHelpReducer, initState);

  const fetchData = async () => {
    const {
      params: { dataflowId }
    } = match;
    dispatch({ type: 'UPDATE_PROPERTY', payload: { key: 'datasetSpinner', value: true } });
    const dataflowDetails = await DataflowService.getAllSchemas(dataflowId);
    const dataflowData = await DataflowService.reporting(dataflowId);

    const { designDatasets } = dataflowData.data;
    dispatch({
      type: 'UPDATE_PROPERTY',
      payload: { key: 'rawDatasets', value: { dataflowDetails: dataflowDetails.data, designDatasets } }
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
    const tablesOptions = state?.datasets?.datasetSchemas?.find(
      dataset => dataset.datasetId === state.selectedDataset?.value
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
        title="Dataset"
        level="dataset"
        selectedItem={state.selectedDataset}
        options={state.datasets.datasetSchemaOptions}
        onChange={onSelectDataset}
        onAddHelpItem={onAddHelpItem}
        isSpinnerVisible={state.datasetSpinner}
      />
      <SqlHelpListBox
        title="Tables"
        level="table"
        selectedItem={state.selectedTable}
        options={state.tables}
        onChange={onSelectTable}
        onAddHelpItem={onAddHelpItem}
      />
      <SqlHelpListBox
        title="Fields"
        level="field"
        selectedItem={state.selectedField}
        options={state.fields}
        onChange={onSelectField}
        onAddHelpItem={onAddHelpItem}
      />
    </div>
  );
});
