import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import styles from './Webform.module.scss';

// import webformJson from './webform.config.json';
import article15 from './article15.webform.json';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from '../_components/Calendar/Calendar';
import { Dropdown } from '../_components/Dropdown/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from '../_components/InputTextarea/InputTextarea';
import { MultiSelect } from '../_components/MultiSelect/MultiSelect';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { WebformUtils } from './_functions/Utils/WebformUtils';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { webformReducer } from './_functions/Reducers/webformReducer';

export const Webform = ({ dataflowId, datasetId, state }) => {
  const notificationContext = useContext(NotificationContext);
  const { datasetSchema, tableSchemaNames } = state;

  const [webformState, webformDispatch] = useReducer(webformReducer, {
    data: [],
    inputData: '',
    isVisible: {},
    multipleView: [{ id: 0 }]
  });

  useEffect(() => {
    if (isEmpty(webformState.isVisible)) initialLoad();
  }, [webformState.isVisible]);

  useEffect(() => {
    if (!isEmpty(datasetSchema)) {
      const data = WebformUtils.mergeArrays(article15, datasetSchema.tables, 'webformTitle', 'tableSchemaName');

      const parsedData = data.map((element, i) => {
        if (element.records) {
          const fields = element.records[0].fields;
          const webformFields = element.webformRecords[0].webformFields;
          element.webformRecords[0].webformFields = WebformUtils.mergeArrays(
            fields,
            webformFields,
            'name',
            'fieldName'
          );

          // WebformUtils.mergeArrays(fields, webformFields, 'fieldName', 'name');

          return element;
        } else return data[i];
      });

      webformDispatch({ type: 'ON_LOAD_DATA', payload: { data: parsedData } });
    }
  }, [datasetSchema]);

  const changeUrl = index => {
    return window.history.replaceState(
      null,
      null,
      `?tab=${index}${!isUndefined(state.isPreviewModeOn) ? `&design=${state.isPreviewModeOn}` : ''}`
    );
  };

  const initialLoad = () => {
    webformDispatch({
      type: 'INITIAL_LOAD',
      payload: { isVisible: WebformUtils.getWebformTabs(state.datasetSchemaAllTables) }
    });
  };

  const onAddMultipleWebform = () => {
    const newForm = { id: webformState.multipleView.length };

    webformDispatch({ type: 'ON_ADD_MULTIPLE_WEBFORM', payload: { newForm } });
  };

  const onChangeWebformTab = index => {
    const isVisible = { ...webformState.isVisible };
    Object.keys(isVisible).forEach(tab => {
      isVisible[tab] = false;
      isVisible[index] = true;
    });

    changeUrl(index);
    webformDispatch({ type: 'ON_CHANGE_TAB', payload: { isVisible } });
  };

  const onDeleteMultipleWebform = id => {
    const multipleList = webformState.multipleView.filter(form => form.id !== id);

    webformDispatch({ type: 'ON_DELETE_MULTIPLE_WEBFORM', payload: { list: multipleList } });
  };

  const renderWebform = () => {
    const visibleTab = WebformUtils.getUrlParamValue('tab');

    if (!isEmpty(webformState.data)) {
      return webformState.data[visibleTab].webformRecords.map((webform, i) => {
        return (
          <div className={styles.body} key={i}>
            <h3 className={styles.title}>
              {webform.title}
              {webform.multiple ? (
                <Button label={'Add'} icon={'plus'} onClick={() => onAddMultipleWebform()} />
              ) : (
                <Fragment />
              )}
            </h3>
            {webform.description ? <h3 className={styles.description}>{webform.description}</h3> : <Fragment />}

            {webform.multiple
              ? webformState.multipleView.map(element =>
                  renderContent(webform.webformFields, webform.multiple, element.id)
                )
              : renderContent(webform.webformFields, webform.multiple)}
          </div>
        );
      });
    }
  };

  const onChangeInputValue = value => webformDispatch({ type: 'ON_CHANGE_VALUE', payload: { value } });

  const onSavaValue = async value => {
    // // if (!isEmpty(record)) {
    // let field = record.dataRow.filter(row => Object.keys(row.fieldData)[0] === cell.field)[0].fieldData;
    // try {
    //   const fieldUpdated = DatasetService.updateFieldById(
    //     datasetId,
    //     null,
    //     field.id,
    //     field.type,
    //     field.type === 'MULTISELECT_CODELIST' || (field.type === 'LINK' && Array.isArray(value))
    //       ? value.join(',')
    //       : value
    //   );
    //   if (!fieldUpdated) {
    //     throw new Error('UPDATE_FIELD_BY_ID_ERROR');
    //   }
    // } catch (error) {}
    // // }
  };

  const renderTemplate = (selectedTemplate, options = [], value = '') => {
    const template = {
      DATE: <Calendar />,
      LINK: <Fragment />,
      MULTISELECT: <MultiSelect />,
      SELECT: <Dropdown options={options} />,
      TEXT: <InputText value={value} />,
      TEXTAREA: <InputTextarea />
    };

    return template[selectedTemplate];
  };

  const renderContent = (fields, multiple, element) => {
    return (
      <div className={styles.contentWrap}>
        {multiple ? (
          <div className={styles.actionButtons}>
            <Button
              className={`${styles.collapse} p-button-rounded p-button-secondary p-button-animated-blink`}
              icon={'plus'}
            />
            <Button
              className={`${styles.delete} p-button-rounded p-button-secondary p-button-animated-blink`}
              icon={'trash'}
              onClick={() => onDeleteMultipleWebform(element)}
            />
          </div>
        ) : (
          <Fragment />
        )}

        {fields.map((form, i) => (
          <div key={i} className={styles.content}>
            <p>{form.fieldName}</p>
            <div>{renderTemplate(form.fieldType, form.options, form.value)}</div>
          </div>
        ))}
      </div>
    );
  };

  const renderWebFormHeaders = () => {
    // Check if the table exists
    const headers = article15.filter(header => tableSchemaNames.includes(header.webformTitle));

    return headers.map((webform, i) => (
      <Button
        className={`${styles.headerButton} ${
          webformState.isVisible[webform.webformIndex] ? 'p-button-primary' : 'p-button-secondary'
        }`}
        key={i}
        label={webform.webformTitle}
        onClick={() => onChangeWebformTab(webform.webformIndex)}
      />
    ));
  };

  return (
    <div className={styles.webform}>
      <Toolbar className={styles.toolbar}>
        <div className="p-toolbar-group-left">{renderWebFormHeaders()}</div>
      </Toolbar>
      {/* {renderWebformBody()} */}
      {renderWebform()}
    </div>
  );
};
