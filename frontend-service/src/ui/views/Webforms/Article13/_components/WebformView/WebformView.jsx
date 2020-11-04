import React, { Fragment, useReducer } from 'react';

import isNil from 'lodash/isNil';
import keys from 'lodash/keys';
import pickBy from 'lodash/pickBy';

import styles from './WebformView.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { WebformTable } from 'ui/views/Webforms/_components/WebformTable';

import { webformViewReducer } from './_functions/Reducers/webformViewReducer';

import { Article15Utils } from '../../../Article15/_functions/Utils/Article15Utils';

export const WebformView = ({ data, dataflowId, datasetId, isReporting, selectedId, state, tables }) => {
  const tableSchemaNames = state.schemaTables.map(table => table.name);
  console.log({ state, tables });
  const [webformViewState, webformViewDispatch] = useReducer(webformViewReducer, {
    isVisible: Article15Utils.getWebformTabs(
      tables.map(table => table.name),
      state.schemaTables,
      tables
    )
  });

  const { isVisible } = webformViewState;

  const onChangeWebformTab = name => {
    Object.keys(isVisible).forEach(tab => {
      isVisible[tab] = false;
      isVisible[name] = true;
    });

    // changeUrl(name);

    webformViewDispatch({ type: 'ON_CHANGE_TAB', payload: { isVisible } });
  };

  const renderWebFormHeaders = () => {
    const filteredTabs = data.filter(header => tableSchemaNames.includes(header.name));
    const headers = filteredTabs.map(tab => tab.header || tab.name);

    return data.map((webform, i) => {
      const isCreated = headers.includes(webform.name);
      const childHasErrors = webform.elements
        .filter(element => element.type === 'TABLE' && !isNil(element.hasErrors))
        .map(table => table.hasErrors);

      const hasErrors = [webform.hasErrors].concat(childHasErrors);

      return (
        <Button
          className={`${styles.headerButton} ${isVisible[webform.name] ? 'p-button-primary' : 'p-button-secondary'}`}
          icon={!isCreated ? 'info' : hasErrors.includes(true) ? 'warning' : null}
          iconClasses={!isVisible[webform.title] ? (hasErrors.includes(true) ? 'warning' : 'info') : ''}
          iconPos={'right'}
          key={i}
          label={webform.label}
          onClick={() => onChangeWebformTab(webform.name)}
          style={{ display: isReporting && !isCreated ? 'none' : '' }}
        />
      );
    });
  };

  const renderWebFormContent = () => {
    const visibleTitle = keys(pickBy(isVisible))[0];
    const visibleContent = data.filter(table => table.name === visibleTitle)[0];

    return (
      <WebformTable
        dataflowId={dataflowId}
        datasetId={datasetId}
        isReporting={isReporting}
        onTabChange={isVisible}
        selectedId={selectedId}
        webform={visibleContent}
        webformType={'ARTICLE_13'}
      />
    );
  };

  return (
    <Fragment>
      <div className={styles.webform}>
        <Toolbar className={styles.toolbar}>
          <div className="p-toolbar-group-left">{renderWebFormHeaders()}</div>
        </Toolbar>
        {renderWebFormContent()}
      </div>
    </Fragment>
  );
};
