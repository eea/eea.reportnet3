import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import keys from 'lodash/keys';
import pickBy from 'lodash/pickBy';

import { config } from 'conf';

import styles from './Article15.module.scss';

import { tables } from '../article15.webform.json';

import { Button } from 'ui/views/_components/Button';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { WebformContent } from './_components/WebformContent';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { article15Reducer } from './_functions/Reducers/article15Reducer';

import { Article15Utils } from './_functions/Utils/Article15Utils';

export const Article15 = ({ dataflowId, datasetId, state }) => {
  const { datasetSchema, tableSchemaNames } = state;

  const [article15State, article15Dispatch] = useReducer(article15Reducer, { allTables: [], data: [], isVisible: {} });

  useEffect(() => initialLoad(), []);

  const changeUrl = index => {
    return window.history.replaceState(
      null,
      null,
      `?tab=${index}${!isUndefined(state.isPreviewModeOn) ? `&design=${state.isPreviewModeOn}` : ''}`
    );
  };

  const initialLoad = () => {
    const allTables = tables.map(table => table.name);
    const parsedData = onLoadData();

    article15Dispatch({
      type: 'INITIAL_LOAD',
      payload: { isVisible: Article15Utils.getWebformTabs(allTables), data: parsedData, allTables }
    });
  };

  const onChangeWebformTab = name => {
    const isVisible = { ...article15State.isVisible };

    Object.keys(isVisible).forEach(tab => {
      isVisible[tab] = false;
      isVisible[name] = true;
    });

    // changeUrl(Article15Utils.getIndexFromName(state.datasetSchemaAllTables, name));
    article15Dispatch({ type: 'ON_CHANGE_TAB', payload: { isVisible } });
  };

  const onLoadData = () => {
    if (!isEmpty(datasetSchema)) {
      return Article15Utils.mergeArrays(tables, datasetSchema.tables, 'name', 'tableSchemaName');
      // return data.map((element, i) => {
      //   if (element.records) {
      //     const fields = element.records[0].fields;
      //     const webformFields = element.webformRecords[0].webformFields;
      //     element.webformRecords[0].webformFields = Article15Utils.mergeArrays(
      //       fields,
      //       webformFields,
      //       'name',
      //       'fieldName'
      //     );
      //     return element;
      //   } else return data[i];
      // });
    }
  };

  const renderWebFormContent = () => {
    const visibleTitle = keys(pickBy(article15State.isVisible))[0];
    const visibleContent = article15State.data.filter(table => table.name === visibleTitle)[0];

    return <WebformContent webform={visibleContent} datasetId={datasetId} onTabChange={article15State.isVisible} />;
  };

  const renderWebFormHeaders = () => {
    const filteredTabs = article15State.data.filter(header => tableSchemaNames.includes(header.name));
    const headers = filteredTabs.map(tab => tab.header);

    return article15State.data.map((webform, i) => {
      const isCreated = headers.includes(webform.name);

      return (
        <Button
          className={`${styles.headerButton} ${
            article15State.isVisible[webform.name] ? 'p-button-primary' : 'p-button-secondary'
          }`}
          icon={!isCreated ? 'info' : webform.hasErrors ? 'warning' : null}
          iconClasses={!article15State.isVisible[webform.title] ? (webform.hasErrors ? 'warning' : 'info') : ''}
          iconPos={'right'}
          key={i}
          label={webform.title}
          onClick={() => onChangeWebformTab(webform.name)}
          style={{ padding: webform.hasErrors || !isCreated ? '0.2rem' : '0.5rem' }}
        />
      );
    });
  };

  if (isEmpty(article15State.data)) return <Spinner style={{ top: 0 }} />;

  return (
    <div className={styles.webform}>
      <Toolbar className={styles.toolbar}>
        <div className="p-toolbar-group-left">{renderWebFormHeaders()}</div>
      </Toolbar>
      {renderWebFormContent()}
    </div>
  );
};
