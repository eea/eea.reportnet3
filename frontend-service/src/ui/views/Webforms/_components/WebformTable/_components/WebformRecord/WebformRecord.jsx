import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './WebformRecord.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { IconTooltip } from 'ui/views/_components/IconTooltip';

import { WebformField } from './_components/WebformField';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { webformRecordReducer } from './_functions/Reducers/webformRecordReducer';

import { MetadataUtils } from 'ui/views/_functions/Utils';
import { TextUtils } from 'ui/views/_functions/Utils';
import { WebformRecordUtils } from './_functions/Utils/WebformRecordUtils';

export const WebformRecord = ({
  addingOnTableSchemaId,
  calculateSingle,
  columnsSchema,
  dataflowId,
  datasetId,
  datasetSchemaId,
  hasFields,
  isAddingMultiple,
  isFixedNumber = true,
  isGroup,
  isReporting,
  multipleRecords,
  onAddMultipleWebform,
  onRefresh,
  onTabChange,
  record,
  tableId,
  tableName,
  webformType
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [webformRecordState, webformRecordDispatch] = useReducer(webformRecordReducer, {
    isConditionalChanged: false,
    isDialogVisible: { deleteRow: false, uploadFile: false },
    newRecord: {},
    record,
    selectedMaxSize: '',
    selectedRecordId: null
  });

  const { isConditionalChanged, isDialogVisible, selectedRecordId } = webformRecordState;

  const { parseMultiselect, parseNewRecordData } = WebformRecordUtils;

  useEffect(() => {
    webformRecordDispatch({
      type: 'INITIAL_LOAD',
      payload: { newRecord: parseNewRecordData(record.elements, undefined), record, isDeleting: false }
    });
  }, [record, onTabChange]);

  const onDeleteMultipleWebform = async () => {
    webformRecordDispatch({ type: 'SET_IS_DELETING', payload: { isDeleting: true } });

    try {
      const isDataDeleted = await DatasetService.deleteRecordById(datasetId, selectedRecordId);
      if (isDataDeleted) {
        onRefresh();
        handleDialogs('deleteRow', false);
      }
    } catch (error) {
      console.error('error', error);

      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'DELETE_RECORD_BY_ID_ERROR',
        content: { dataflowId, dataflowName, datasetId, datasetName, tableName }
      });
    }
  };

  const onFillField = (field, option, value, conditional) => {
    webformRecordDispatch({ type: 'ON_FILL_FIELD', payload: { field, option, value, conditional } });
  };

  const onSaveField = async (option, value, recordId) => {
    try {
      await DatasetService.addRecordsById(datasetId, tableId, [parseMultiselect(webformRecordState.newRecord)]);
    } catch (error) {
      console.error('error', error);
    }
  };

  const onToggleFieldVisibility = (dependency, fields = []) => {
    if (isNil(dependency)) return true;
    const filteredDependency = fields
      .filter(field => TextUtils.areEquals(field.name, dependency.field))
      .map(filtered => (Array.isArray(filtered?.value) ? filtered?.value : filtered?.value?.split(', ')));

    return filteredDependency
      .flat()
      .map(field => dependency.value.includes(field))
      .includes(true);
  };

  const checkAddButtonVisibility = el => {
    if (isNil(isGroup)) {
      return true;
    } else {
      if (isGroup() && !isNil(el.hasCalculatedFields)) {
        return false;
      } else {
        return true;
      }
    }
  };

  const checkLabelVisibility = el => {
    if (isNil(isGroup)) {
      return true;
    } else {
      if (isGroup() && el.hideWhenCalculated) {
        return false;
      } else {
        return true;
      }
    }
  };

  const checkRequiredLabelVisibility = el => {
    if (isNil(isGroup)) {
      return true;
    } else {
      if (isGroup() && el.calculatedWhenGroup) {
        return false;
      } else {
        return true;
      }
    }
  };

  const handleDialogs = (dialog, value) => {
    webformRecordDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, value } });
  };

  const renderElements = (elements = []) => {
    return elements.map((element, i) => {
      const isFieldVisible = element.fieldType === 'EMPTY' && isReporting;
      const isSubTableVisible = element.tableNotCreated && isReporting;

      if (element.type === 'BLOCK') {
        return (
          !isFieldVisible && (
            <div key={i} className={styles.fieldsBlock}>
              {renderElements(element.elementsRecords[0].elements)}
            </div>
          )
        );
      }

      if (element.type === 'FIELD') {
        return (
          checkLabelVisibility(element) &&
          !isFieldVisible &&
          onToggleFieldVisibility(element.dependency, elements, element) && (
            <div key={i} className={styles.field}>
              {(element.required || element.title) && isNil(element.customType) && (
                <label>
                  {element.title}
                  {checkRequiredLabelVisibility(element) && (
                    <span className={styles.requiredMark}>{element.required ? '*' : ''}</span>
                  )}
                </label>
              )}

              {element.tooltip && isNil(element.customType) && (
                <Button
                  className={`${styles.infoCircle} p-button-rounded p-button-secondary-transparent`}
                  icon="infoCircle"
                  tooltip={element.tooltip}
                  tooltipOptions={{ position: 'top' }}
                />
              )}
              <div className={styles.fieldWrapper}>
                <div className={styles.template}>
                  {!isNil(isGroup) && element.calculatedWhenGroup && isGroup() ? (
                    !element.hideWhenCalculated ? (
                      calculateSingle(element)
                    ) : null
                  ) : (
                    <WebformField
                      columnsSchema={columnsSchema}
                      datasetId={datasetId}
                      datasetSchemaId={datasetSchemaId}
                      element={element}
                      isConditional={
                        !isNil(webformRecordState.record) &&
                        webformRecordState.record.elements.filter(
                          col =>
                            !isNil(col.referencedField) &&
                            col.referencedField.masterConditionalFieldId === element.fieldSchemaId
                        ).length > 0
                      }
                      isConditionalChanged={isConditionalChanged}
                      onFillField={onFillField}
                      onSaveField={onSaveField}
                      record={record}
                    />
                  )}
                  {/* {renderTemplate(element, element.fieldSchemaId, element.fieldType)} */}
                </div>
                {element.validations &&
                  element.validations.map((validation, index) => (
                    <IconTooltip
                      className={'webform-validationErrors'}
                      key={index}
                      levelError={validation.levelError}
                      message={validation.message}
                    />
                  ))}
              </div>
            </div>
          )
        );
      } else if (element.type === 'LABEL') {
        return (
          checkLabelVisibility(element) && (
            <Fragment>
              {element.level === 2 && <h2 className={styles[`label${element.level}`]}>{element.title}</h2>}
              {element.level === 3 && <h3 className={styles[`label${element.level}`]}>{element.title}</h3>}
              {element.level === 4 && <h3 className={styles[`label${element.level}`]}>{element.title}</h3>}
              {element.tooltip && isNil(element.customType) && (
                <Button
                  className={`${styles.infoCircle} p-button-rounded p-button-secondary-transparent`}
                  icon="infoCircle"
                  tooltip={element.tooltip}
                  tooltipOptions={{ position: 'top' }}
                />
              )}
            </Fragment>
          )
        );
      } else {
        return (
          !isSubTableVisible &&
          onToggleFieldVisibility(element.dependency, elements, element) && (
            <div key={i} className={element.showInsideParentTable ? styles.showInsideParentTable : styles.subTable}>
              {!element.showInsideParentTable && (
                <div className={styles.title}>
                  <h3>
                    {element.title ? element.title : element.name}
                    {element.hasErrors && (
                      <IconTooltip levelError={'ERROR'} message={resources.messages['tableWithErrorsTooltip']} />
                    )}
                  </h3>

                  {checkAddButtonVisibility(element) && element.multipleRecords && (
                    <Button
                      disabled={addingOnTableSchemaId === element.tableSchemaId && isAddingMultiple}
                      icon={
                        addingOnTableSchemaId === element.tableSchemaId && isAddingMultiple ? 'spinnerAnimate' : 'plus'
                      }
                      label={resources.messages['addRecord']}
                      onClick={() => onAddMultipleWebform(element.tableSchemaId)}
                    />
                  )}
                </div>
              )}

              {element.tableNotCreated && (
                <span
                  className={styles.nonExistTable}
                  dangerouslySetInnerHTML={{
                    __html: TextUtils.parseText(resources.messages['tableIsNotCreated'], { tableName: element.name })
                  }}
                />
              )}
              {element.elementsRecords.map((record, i) => {
                return (
                  <WebformRecord
                    calculateSingle={calculateSingle}
                    columnsSchema={columnsSchema}
                    dataflowId={dataflowId}
                    datasetId={datasetId}
                    datasetSchemaId={datasetSchemaId}
                    isGroup={isGroup}
                    key={i}
                    multipleRecords={element.multipleRecords}
                    onAddMultipleWebform={onAddMultipleWebform}
                    onRefresh={onRefresh}
                    onTabChange={onTabChange}
                    newRecord={webformRecordState.newRecord}
                    record={record}
                    tableId={tableId}
                    tableName={element.title}
                  />
                );
              })}
            </div>
          )
        );
      }
    });
  };

  const renderWebformContent = content => {
    const errorMessages = renderErrorMessages(content);

    return (
      <div className={styles.content}>
        <div className={styles.actionButtons}>
          {!isEmpty(content.validations) &&
            content.validations.map((validation, index) => (
              <IconTooltip key={index} levelError={validation.levelError} message={validation.message} />
            ))}
        </div>
        {multipleRecords && !isEmpty(content.elements) && (
          <div className={styles.actionButtons}>
            <Button
              className={`${styles.delete} p-button-rounded p-button-secondary p-button-animated-blink`}
              disabled={webformRecordState.isDeleting}
              icon={webformRecordState.isDeleting ? 'spinnerAnimate' : 'trash'}
              onClick={() => {
                handleDialogs('deleteRow', true);
                webformRecordDispatch({ type: 'GET_DELETE_ROW_ID', payload: { selectedRecordId: content.recordId } });
              }}
            />
          </div>
        )}
        {isEmpty(errorMessages) ? (
          renderElements(content.elements)
        ) : (
          <ul className={styles.errorList}>
            {errorMessages.map(msg => (
              <li className={styles.errorItem}>{msg}</li>
            ))}
          </ul>
        )}
      </div>
    );
  };

  const renderErrorMessages = content => {
    switch (webformType) {
      case 'ARTICLE_15':
        return renderArticle15ErrorMessages(content);

      case 'ARTICLE_13':
        return renderArticle13ErrorMessages(content);

      default:
        return [];
    }
  };

  const renderArticle13ErrorMessages = content => {
    const errorMessages = [];

    if (isEmpty(record)) errorMessages.push('PLEASE CHOOSE ONE');
    if (hasFields) {
      errorMessages.push(resources.messages['emptyWebformTable']);
    }
    if (content.totalRecords === 0) {
      errorMessages.push(resources.messages['webformTableWithLessRecords']);
    }
    return errorMessages;
  };

  const renderArticle15ErrorMessages = content => {
    const errorMessages = [];
    if (hasFields) {
      errorMessages.push(resources.messages['emptyWebformTable']);
    }
    if (content.totalRecords === 0) {
      errorMessages.push(resources.messages['webformTableWithLessRecords']);
    }
    if (content.totalRecords > 1) {
      errorMessages.push(resources.messages['webformTableWithMoreRecords']);
    }
    if (!isFixedNumber) {
      errorMessages.push(resources.messages['webformTableWithoutFixedNumber']);
    }

    return errorMessages;
  };

  return (
    <Fragment>
      {renderWebformContent(webformRecordState.record)}

      {isDialogVisible.deleteRow && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resources.messages['deleteRow']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteMultipleWebform(selectedRecordId)}
          onHide={() => handleDialogs('deleteRow', false)}
          visible={isDialogVisible.deleteRow}>
          {resources.messages['confirmDeleteRow']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};

WebformRecord.propTypes = { record: PropTypes.shape({ elements: PropTypes.array }) };

WebformRecord.defaultProps = { record: { elements: [], totalRecords: 0 } };
