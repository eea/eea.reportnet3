import React, { Fragment, useEffect, useReducer } from 'react';
import ReactTooltip from 'react-tooltip';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import keys from 'lodash/keys';
import pickBy from 'lodash/pickBy';

import styles from './Article15.module.scss';

import { tables } from './article15.webform.json';

import { Button } from 'ui/views/_components/Button';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { WebformTable } from './_components/WebformTable';

import { article15Reducer } from './_functions/Reducers/article15Reducer';

import { Article15Utils } from './_functions/Utils/Article15Utils';

export const Article15 = ({ datasetId, state }) => {
  const { datasetSchema, tableSchemaNames } = state;

  const [article15State, article15Dispatch] = useReducer(article15Reducer, { data: [], isVisible: {} });

  useEffect(() => initialLoad(), []);

  const initialLoad = () => {
    const allTables = tables.map(table => table.name);
    const parsedData = onLoadData();

    article15Dispatch({
      type: 'INITIAL_LOAD',
      payload: { isVisible: Article15Utils.getWebformTabs(allTables), data: parsedData }
    });
  };

  const onChangeWebformTab = name => {
    const isVisible = { ...article15State.isVisible };

    Object.keys(isVisible).forEach(tab => {
      isVisible[tab] = false;
      isVisible[name] = true;
    });

    article15Dispatch({ type: 'ON_CHANGE_TAB', payload: { isVisible } });
  };

  const onParseWebformData = (allTables, schemaTables) => {
    const data = Article15Utils.mergeArrays(allTables, schemaTables, 'name', 'tableSchemaName');

    data.map(table => {
      if (table.records) {
        table.records[0].fields = table.records[0].fields.map(field => {
          const { fieldId, recordId, type } = field;

          return { fieldSchema: fieldId, fieldType: type, recordSchemaId: recordId, ...field };
        });
      }
    });

    for (let index = 0; index < data.length; index++) {
      const table = data[index];

      if (table.records) {
        const { elements, records } = table;

        const result = [];
        for (let index = 0; index < elements.length; index++) {
          if (elements[index].type === 'FIELD') {
            result.push({
              ...elements[index],
              ...records[0].fields.find(element => element['name'] === elements[index]['name']),
              type: elements[index].type
            });
          } else if (elements[index].type === 'TABLE') {
            const filteredTable = datasetSchema.tables.filter(table => table.tableSchemaName === elements[index].name);
            const parsedTable = onParseWebformData([elements[index]], filteredTable);

            result.push({ ...elements[index], ...parsedTable[0], type: elements[index].type });
          }
        }

        table.elements = result;
      }
    }

    return data;
  };

  const onLoadData = () => {
    if (!isEmpty(datasetSchema)) return onParseWebformData(tables, datasetSchema.tables);
  };

  const renderWebFormContent = () => {
    const visibleTitle = keys(pickBy(article15State.isVisible))[0];
    const visibleContent = article15State.data.filter(table => table.name === visibleTitle)[0];

    return <WebformTable webform={visibleContent} datasetId={datasetId} onTabChange={article15State.isVisible} />;
  };

  const renderWebFormHeaders = () => {
    const filteredTabs = article15State.data.filter(header => tableSchemaNames.includes(header.name));
    const headers = filteredTabs.map(tab => tab.header);

    return article15State.data.map((webform, i) => {
      const isCreated = headers.includes(webform.name);
      const childHasErrors = webform.elements
        .filter(element => element.type === 'TABLE' && !isNil(element.hasErrors))
        .map(table => table.hasErrors);

      const hasErrors = [webform.hasErrors].concat(childHasErrors);

      return (
        <Fragment key={i}>
          <Button
            data-tip
            data-for={!isCreated ? 'TableNotExists' : ''}
            className={`${styles.headerButton} ${
              article15State.isVisible[webform.name] ? 'p-button-primary' : 'p-button-secondary'
            }`}
            icon={!isCreated ? 'info' : hasErrors.includes(true) ? 'warning' : null}
            iconClasses={
              !article15State.isVisible[webform.title] ? (hasErrors.includes(true) ? 'warning' : 'info') : ''
            }
            iconPos={'right'}
            key={i}
            label={webform.title}
            onClick={() => onChangeWebformTab(webform.name)}
          />

          {!isCreated && (
            <ReactTooltip effect="solid" id="TableNotExists" place="top">
              {`The table ${webform.name} is not created in the design, please check it`}
            </ReactTooltip>
          )}
        </Fragment>
      );
    });
  };

  if (isEmpty(article15State.data)) return <Spinner className={styles.spinner} />;

  return (
    <div className={styles.webform}>
      <Toolbar className={styles.toolbar}>
        <div className="p-toolbar-group-left">{renderWebFormHeaders()}</div>
      </Toolbar>
      {renderWebFormContent()}
    </div>
  );
};
