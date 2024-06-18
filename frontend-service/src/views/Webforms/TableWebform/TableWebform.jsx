import { Fragment, useEffect, useReducer } from 'react';
import ReactTooltip from 'react-tooltip';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import keys from 'lodash/keys';
import pickBy from 'lodash/pickBy';
import uniqueId from 'lodash/uniqueId';

import styles from './TableWebform.module.scss';

import { Button } from 'views/_components/Button';
import { Spinner } from 'views/_components/Spinner';
import { Toolbar } from 'views/_components/Toolbar';
import { WebformTable } from 'views/Webforms/_components/WebformTable';

import { tableWebformReducer } from './_functions/Reducers/tableWebformReducer';

import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';

export const TableWebform = ({ bigData, dataflowId, dataProviderId, datasetId, isReporting, state, tables = [] }) => {
  const { datasetSchema } = state;
  const { getWebformTabs, onParseWebformData } = WebformsUtils;

  const tableSchemaNames = state.schemaTables.map(table => table.name);

  const [tableWebformState, tableWebformDispatch] = useReducer(tableWebformReducer, {
    data: [],
    isLoading: false,
    isVisible: {}
  });

  useEffect(() => initialLoad(), [tables]);

  const changeUrl = tabSchemaName => {
    const filteredTable = state.schemaTables.filter(schemaTable => schemaTable.name === tabSchemaName);
    if (!isEmpty(filteredTable)) {
      window.history.replaceState(null, null, `?tab=${filteredTable[0].id}${`&view=webform`}`);
    }
  };

  const initialLoad = () => {
    const allTables = tables.map(table => table.name);
    const parsedData = onLoadData();

    tableWebformDispatch({
      type: 'INITIAL_LOAD',
      payload: { isVisible: getWebformTabs(allTables, state.schemaTables, tables), data: parsedData }
    });
  };

  const onChangeWebformTab = name => {
    const isVisible = { ...tableWebformState.isVisible };

    Object.keys(isVisible).forEach(tab => {
      isVisible[tab] = false;
      isVisible[name] = true;
    });

    changeUrl(name);

    tableWebformDispatch({ type: 'ON_CHANGE_TAB', payload: { isVisible } });
  };

  const onLoadData = () => {
    if (!isEmpty(datasetSchema)) {
      return onParseWebformData(datasetSchema, tables, datasetSchema.tables);
    }
  };

  const setIsLoading = value => tableWebformDispatch({ type: 'SET_IS_LOADING', payload: { value } });

  const renderWebFormContent = () => {
    const visibleTitle = keys(pickBy(tableWebformState.isVisible))[0];
    const visibleContent = tableWebformState.data.filter(table => table.name === visibleTitle)[0];
    return (
      <WebformTable
        bigData={bigData}
        dataflowId={dataflowId}
        dataProviderId={dataProviderId}
        datasetSchemaId={datasetSchema.datasetSchemaId}
        datasetId={datasetId}
        isReporting={isReporting}
        onTabChange={tableWebformState.isVisible}
        setIsLoading={setIsLoading}
        webform={visibleContent}
        webformType="TABLES"
      />
    );
  };

  const renderWebFormHeaders = () => {
    const filteredTabs = tableWebformState.data.filter(header => tableSchemaNames.includes(header.name));
    const headers = filteredTabs.map(tab => tab.header || tab.name);

    return tableWebformState.data.map(webform => {
      const isCreated = headers.includes(webform.name);
      const childHasErrors = webform.elements
        .filter(element => element.type === 'TABLE' && !isNil(element.hasErrors))
        .map(table => table.hasErrors);

      const hasErrors = [webform.hasErrors].concat(childHasErrors);

      return (
        <Fragment key={uniqueId()}>
          <Button
            className={`${styles.headerButton} ${
              tableWebformState.isVisible[webform.name] ? 'p-button-primary' : 'p-button-secondary'
            }`}
            data-for={!isCreated ? 'TableNotExists' : ''}
            data-tip
            disabled={tableWebformState.isLoading}
            icon={!isCreated ? 'info' : hasErrors.includes(true) ? 'warning' : 'table'}
            iconClasses={
              !tableWebformState.isVisible[webform.title] ? (hasErrors.includes(true) ? 'warning' : 'info') : ''
            }
            iconPos={!isCreated || hasErrors.includes(true) ? 'right' : 'left'}
            key={uniqueId()}
            label={webform.label}
            onClick={() => onChangeWebformTab(webform.name)}
            style={{ display: isReporting && !isCreated ? 'none' : '' }}
          />

          {!isCreated && (
            <ReactTooltip border={true} effect="solid" id="TableNotExists" place="top">
              {`The table ${webform.name} is not created in the design, please check it`}
            </ReactTooltip>
          )}
        </Fragment>
      );
    });
  };

  if (isEmpty(tableWebformState.data)) {
    return <Spinner className={styles.spinner} />;
  }

  return (
    <div className={styles.webform}>
      <Toolbar className={styles.toolbar}>
        <div className="p-toolbar-group-left">{renderWebFormHeaders()}</div>
      </Toolbar>
      {renderWebFormContent()}
    </div>
  );
};
