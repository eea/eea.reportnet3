import { Fragment, useEffect, useReducer } from 'react';
import ReactTooltip from 'react-tooltip';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import keys from 'lodash/keys';
import pickBy from 'lodash/pickBy';
import uniqueId from 'lodash/uniqueId';

import styles from './Article15.module.scss';

import { Button } from 'views/_components/Button';
import { Spinner } from 'views/_components/Spinner';
import { Toolbar } from 'views/_components/Toolbar';
import { WebformTable } from 'views/Webforms/_components/WebformTable';

import { article15Reducer } from './_functions/Reducers/article15Reducer';

import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';

export const Article15 = ({ dataflowId, dataProviderId, datasetId, isReporting, state, tables = [] }) => {
  const { datasetSchema } = state;
  const { getWebformTabs, onParseWebformData } = WebformsUtils;

  const tableSchemaNames = state.schemaTables.map(table => table.name);

  const [article15State, article15Dispatch] = useReducer(article15Reducer, {
    data: [],
    isLoading: false,
    isVisible: {}
  });

  useEffect(() => initialLoad(), []);

  const changeUrl = tabSchemaName => {
    const filteredTable = state.schemaTables.filter(schemaTable => schemaTable.name === tabSchemaName);
    if (!isEmpty(filteredTable)) {
      window.history.replaceState(null, null, `?tab=${filteredTable[0].id}${`&view=webform`}`);
    }
  };

  const initialLoad = () => {
    const allTables = tables.map(table => table.name);
    const parsedData = onLoadData();

    article15Dispatch({
      type: 'INITIAL_LOAD',
      payload: { isVisible: getWebformTabs(allTables, state.schemaTables, tables), data: parsedData }
    });
  };

  const onChangeWebformTab = name => {
    const isVisible = { ...article15State.isVisible };

    Object.keys(isVisible).forEach(tab => {
      isVisible[tab] = false;
      isVisible[name] = true;
    });

    changeUrl(name);

    article15Dispatch({ type: 'ON_CHANGE_TAB', payload: { isVisible } });
  };

  const onLoadData = () => {
    if (!isEmpty(datasetSchema)) return onParseWebformData(datasetSchema, tables, datasetSchema.tables);
  };

  const setIsLoading = value => article15Dispatch({ type: 'SET_IS_LOADING', payload: { value } });

  const renderWebFormContent = () => {
    const visibleTitle = keys(pickBy(article15State.isVisible))[0];
    const visibleContent = article15State.data.filter(table => table.name === visibleTitle)[0];

    return (
      <WebformTable
        dataflowId={dataflowId}
        dataProviderId={dataProviderId}
        datasetId={datasetId}
        isReporting={isReporting}
        onTabChange={article15State.isVisible}
        setIsLoading={setIsLoading}
        webform={visibleContent}
        webformType={'ARTICLE_15'}
      />
    );
  };

  const renderWebFormHeaders = () => {
    const filteredTabs = article15State.data.filter(header => tableSchemaNames.includes(header.name));
    const headers = filteredTabs.map(tab => tab.header || tab.name);

    return article15State.data.map(webform => {
      const isCreated = headers.includes(webform.name);
      const childHasErrors = webform.elements
        .filter(element => element.type === 'TABLE' && !isNil(element.hasErrors))
        .map(table => table.hasErrors);

      const hasErrors = [webform.hasErrors].concat(childHasErrors);

      return (
        <Fragment key={uniqueId()}>
          <Button
            className={`${styles.headerButton} ${
              article15State.isVisible[webform.name] ? 'p-button-primary' : 'p-button-secondary'
            }`}
            data-for={!isCreated ? 'TableNotExists' : ''}
            data-tip
            disabled={article15State.isLoading}
            icon={!isCreated ? 'info' : hasErrors.includes(true) ? 'warning' : 'table'}
            iconClasses={
              !article15State.isVisible[webform.title] ? (hasErrors.includes(true) ? 'warning' : 'info') : ''
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
