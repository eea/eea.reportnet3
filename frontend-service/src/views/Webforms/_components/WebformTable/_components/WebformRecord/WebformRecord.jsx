/* eslint-disable react/no-array-index-key */
import { Fragment, useContext, useEffect, useReducer } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniqBy from 'lodash/uniqBy';
import uniqueId from 'lodash/uniqueId';

import styles from './WebformRecord.module.scss';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { IconTooltip } from 'views/_components/IconTooltip';
import { WebformField } from './_components/WebformField';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { webformRecordReducer } from './_functions/Reducers/webformRecordReducer';

import { ErrorUtils, MetadataUtils } from 'views/_functions/Utils';
import { WebformRecordUtils } from './_functions/Utils/WebformRecordUtils';
import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

const checkShowRequired = (element, elements) => {
  if (element.required || element.showRequiredCharacter) return true;
  if (element.showRequiredOnCondition) {
    const dependantElement = elements.find(el => el.name === element.showRequiredOnCondition.field);

    // Don't check for specific values
    // We only want the dependant element's value to be not null
    if (element.showRequiredOnCondition.valueNotNull) {
      return (
        (typeof dependantElement.value === 'string' && !!dependantElement.value?.length) ||
        (Array.isArray(dependantElement.value) && !!dependantElement.value?.length) ||
        !!dependantElement.value
      );
    }

    // Check for specific values in dependant element
    if (Array.isArray(element.showRequiredOnCondition.value) && Array.isArray(dependantElement.value)) {
      return element.showRequiredOnCondition.value.some(value => dependantElement.value.includes(value));
    } else if (Array.isArray(element.showRequiredOnCondition.value) && !Array.isArray(dependantElement.value)) {
      return element.showRequiredOnCondition.value.includes(dependantElement.value);
    } else if (!Array.isArray(element.showRequiredOnCondition.value) && Array.isArray(dependantElement.value)) {
      return dependantElement.value.includes(element.showRequiredOnCondition.value);
    } else if (!Array.isArray(element.showRequiredOnCondition.value) && !Array.isArray(dependantElement.value)) {
      return element.showRequiredOnCondition.value === dependantElement.value;
    }
  }
  return false;
};

export const WebformRecord = ({
  addingOnTableSchemaId,
  bigData,
  columnsSchema,
  dataflowId,
  dataProviderId,
  datasetId,
  datasetSchemaId,
  entitiesRecords,
  hasFields,
  isAddingMultiple,
  isFixedNumber = true,
  isReporting,
  multipleRecords,
  onAddMultipleWebform,
  onRefresh,
  onTabChange,
  onUpdateEntitiesValue,
  record,
  referencedTableSchemaId,
  rootPkFieldId,
  tableId,
  tableName,
  webformType
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

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
  const { parseRecordValidations } = WebformsUtils;

  useEffect(() => {
    webformRecordDispatch({
      type: 'INITIAL_LOAD',
      payload: { newRecord: parseNewRecordData(record.elements, undefined), record, isDeleting: false }
    });
  }, [record, onTabChange]);

  const onDeleteMultipleWebform = async () => {
    webformRecordDispatch({ type: 'SET_IS_DELETING', payload: { isDeleting: true } });
    let updateInCascade = webformRecordState.record?.elements?.some(element => element.deleteInCascade);
    try {
      await DatasetService.deleteRecord({
        datasetId,
        selectedRecordId,
        tableId,
        updateInCascade
      });
      onRefresh();
      handleDialogs('deleteRow', false);
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('WebformRecord - onDeleteMultipleWebform.', error);
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add(
          {
            type: 'DELETE_RECORD_BY_ID_ERROR',
            content: { dataflowId, dataflowName, datasetId, datasetName, customContent: { tableName } }
          },
          true
        );
      }
    }
  };

  const onFillField = (field, option, value, conditional) => {
    webformRecordDispatch({ type: 'ON_FILL_FIELD', payload: { field, option, value, conditional } });
  };

  const onSaveField = async () => {
    try {
      await DatasetService.createRecord(datasetId, tableId, [parseMultiselect(webformRecordState.newRecord)]);
    } catch (error) {
      console.error('WebformRecord - onSaveField.', error);
    }
  };

  const onToggleFieldVisibility = (dependency, fields = []) => {
    if (isNil(dependency)) return true;
    const filteredDependency = fields
      .filter(field => TextUtils.areEquals(field.name, dependency.referenceField))
      .map(filtered => (Array.isArray(filtered?.value) ? filtered?.value : filtered?.value?.split('; ')));
    //Check
    return filteredDependency
      .flat()
      .map(field => dependency.referenceField.includes(field))
      .includes(true);
  };

  const handleDialogs = (dialog, value) => {
    webformRecordDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, value } });
  };

  const renderElements = (elements = [], fieldsBlock = false) => {
    return elements.map((element, i) => {
      const isFieldVisible = element.fieldType === 'EMPTY' && isReporting;
      const isSubTableVisible = element.tableNotCreated && isReporting;
      if (element.type === 'BLOCK') {
        const isSubTable = () => element.elementsRecords.length > 1;
        if (isSubTable()) {
          return (
            <div className={styles.fieldsBlock} key={`BLOCK_${i}`}>
              {element.elementsRecords
                .filter(record => elements.some(el => el.recordId === record.recordId))
                .map(record => renderElements(record.elements, true))}
            </div>
          );
        }

        return (
          <div className={styles.fieldsBlock} key={`BLOCK_${i}`}>
            {element.elementsRecords.map(record => renderElements(record.elements))}
          </div>
        );
      }

      if (element.type === 'FIELD') {
        const fieldStyle = { width: '100%' };
        if (fieldsBlock) {
          const elementCount = elements.length;
          const elementGap = 5 * elementCount;
          const elementWidth = (100 - elementGap) / elementCount;
          fieldStyle.width = elementWidth;
        }

        return (
          !isFieldVisible &&
          onToggleFieldVisibility(element.dependency, elements, element) && (
            <div className={styles.field} key={element.fieldId} style={fieldStyle}>
              {(element.required || element.title) && isNil(element.customType) && (
                <label>
                  {element.title}
                  {<span className={styles.requiredMark}>{checkShowRequired(element, elements) ? ' *' : ''}</span>}
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
                  {
                    <WebformField
                      bigData={bigData}
                      columnsSchema={columnsSchema}
                      dataflowId={dataflowId}
                      dataProviderId={dataProviderId}
                      datasetId={datasetId}
                      datasetSchemaId={datasetSchemaId}
                      element={element}
                      entitiesRecords={entitiesRecords}
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
                      onUpdateEntitiesValue={onUpdateEntitiesValue}
                      record={record}
                      referencedTableSchemaId={referencedTableSchemaId}
                      rootPkFieldId={rootPkFieldId}
                      tableSchemaId={tableId}
                    />
                  }
                </div>
                {element.validations &&
                  uniqBy(element.validations, element => {
                    return [element.message, element.errorLevel].join();
                  }).map(validation => (
                    <IconTooltip
                      className="webform-validationErrors"
                      key={validation.id}
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
          <div key={uniqueId(element.title)}>
            {element.level === 2 && <h2 className={styles[`label${element.level}`]}>{element.title}</h2>}
            {element.level === 3 && <h3 className={styles[`label${element.level}`]}>{element.title}</h3>}
            {element.level === 4 && <h3 className={styles[`label${element.level}`]}>{element.title}</h3>}
            {<span style={{ color: 'var(--errors)' }}>{element.showRequiredCharacter ? ' *' : ''}</span>}
            {element.tooltip && isNil(element.customType) && (
              <Button
                className={`${styles.infoCircle} p-button-rounded p-button-secondary-transparent`}
                icon="infoCircle"
                tooltip={element.tooltip}
                tooltipOptions={{ position: 'top' }}
              />
            )}
          </div>
        );
      } else {
        let fkHasEmptyValues = false;

        const referencePkFieldId = element.records[0].fields.filter(
          field => !isNil(field?.referencedField?.idPk) && field?.referencedField?.idPk !== rootPkFieldId
        )[0]?.referencedField?.idPk;

        const referencePkValue = record.fields.find(
          field => field.fieldId === referencePkFieldId || field.fieldSchemaId === referencePkFieldId
        )?.value;

        const fkFields = element?.elements
          .filter(element => !isNil(element.dependency))
          .map(
            field =>
              (field = {
                fieldName: field.name,
                referenceFieldName: field?.dependency?.referenceField,
                value: record.elements.filter(element =>
                  TextUtils.areEquals(field?.dependency?.referenceField, element.name)
                )[0].value
              })
          );

        if (!isEmpty(fkFields)) {
          fkHasEmptyValues = fkFields.some(field => isEmpty(field.value));
        }

        return (
          !isSubTableVisible &&
          onToggleFieldVisibility(element.dependency, elements, element) && (
            <div
              className={element.showInsideParentTable ? styles.showInsideParentTable : styles.subTable}
              key={element.recordSchemaId}>
              {!element.showInsideParentTable && (
                <div className={styles.title} key={element.recordSchemaId}>
                  <h3>
                    {element.title ? element.title : element.name}
                    {<span style={{ color: 'var(--errors)' }}>{element.showRequiredCharacter ? ' *' : ''}</span>}
                    {element.hasErrors && (
                      <IconTooltip levelError={'ERROR'} message={resourcesContext.messages['tableWithErrorsTooltip']} />
                    )}
                  </h3>

                  {element.multipleRecords && (
                    <Button
                      disabled={
                        fkHasEmptyValues ||
                        isEmpty(referencePkValue) ||
                        (addingOnTableSchemaId === element.tableSchemaId && isAddingMultiple)
                      }
                      icon={
                        addingOnTableSchemaId === element.tableSchemaId && isAddingMultiple ? 'spinnerAnimate' : 'plus'
                      }
                      label={resourcesContext.messages['addRecord']}
                      onClick={() => {
                        onAddMultipleWebform(element.tableSchemaId, referencePkValue, false, fkFields);
                      }}
                    />
                  )}
                </div>
              )}

              {element.tableNotCreated && (
                <span
                  className={styles.nonExistTable}
                  dangerouslySetInnerHTML={{
                    __html: TextUtils.parseText(resourcesContext.messages['tableIsNotCreated'], {
                      tableName: element.name
                    })
                  }}
                />
              )}

              {element.elementsRecords.map((record, i) => {
                return (
                  <WebformRecord
                    addingOnTableSchemaId={addingOnTableSchemaId}
                    bigData={bigData}
                    columnsSchema={columnsSchema}
                    dataflowId={dataflowId}
                    dataProviderId={dataProviderId}
                    datasetId={datasetId}
                    datasetSchemaId={datasetSchemaId}
                    entitiesRecords={entitiesRecords}
                    isAddingMultiple={isAddingMultiple}
                    key={i}
                    multipleRecords={element.multipleRecords}
                    newRecord={webformRecordState.newRecord}
                    onAddMultipleWebform={onAddMultipleWebform}
                    onRefresh={onRefresh}
                    onTabChange={onTabChange}
                    onUpdateEntitiesValue={onUpdateEntitiesValue}
                    record={record}
                    referencedTableSchemaId={element?.tableSchemaId}
                    rootPkFieldId={rootPkFieldId}
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

  const validationsTemplate = recordData => {
    return (
      <div className={styles.iconTooltipWrapper}>
        {ErrorUtils.getValidationsTemplate(recordData, {
          blockers: resourcesContext.messages['recordBlockers'],
          errors: resourcesContext.messages['recordErrors'],
          warnings: resourcesContext.messages['recordWarnings'],
          infos: resourcesContext.messages['recordInfos']
        })}
      </div>
    );
  };

  const renderWebformContent = content => {
    const errorMessages = renderErrorMessages(content);

    return (
      <div className={styles.content}>
        {multipleRecords && !isEmpty(content.elements) && (
          <div className={styles.actionButtons}>
            {validationsTemplate(parseRecordValidations(webformRecordState.record))}
            <Button
              className={`${styles.delete} p-button-rounded p-button-secondary p-button-animated-blink`}
              disabled={webformRecordState.isDeleting}
              icon={webformRecordState.isDeleting ? 'spinnerAnimate' : 'trash'}
              onClick={() => {
                handleDialogs('deleteRow', true);
                webformRecordDispatch({
                  type: 'GET_DELETE_ROW_ID',
                  payload: { selectedRecordId: content.recordId }
                });
              }}
            />
          </div>
        )}
        {isEmpty(errorMessages) ? (
          renderElements(content.elements)
        ) : (
          <ul className={styles.errorList}>
            {errorMessages.map(msg => (
              <li className={styles.errorItem} key={msg}>
                {msg}
              </li>
            ))}
          </ul>
        )}
      </div>
    );
  };

  const renderErrorMessages = content => {
    switch (webformType) {
      case 'TABLES':
        return renderTableWebformErrorMessages(content);
      case 'ENTITIES':
        return renderWebformEntitiesErrorMessages(content);
      default:
        return [];
    }
  };

  const renderWebformEntitiesErrorMessages = content => {
    const errorMessages = [];

    if (isEmpty(record)) {
      errorMessages.push('PLEASE CHOOSE ONE');
    }
    if (hasFields) {
      errorMessages.push(resourcesContext.messages['emptyWebformTable']);
    }
    if (content.totalRecords === 0) {
      errorMessages.push(resourcesContext.messages['webformTableWithLessRecords']);
    }

    return errorMessages;
  };

  const renderTableWebformErrorMessages = content => {
    const errorMessages = [];
    if (hasFields) {
      errorMessages.push(resourcesContext.messages['emptyWebformTable']);
    }
    if (content.totalRecords === 0) {
      errorMessages.push(resourcesContext.messages['webformTableWithLessRecords']);
    }
    if (!multipleRecords && content.totalRecords > 1) {
      errorMessages.push(resourcesContext.messages['webformTableWithMoreRecords']);
    }
    if (!multipleRecords && !isFixedNumber) {
      errorMessages.push(resourcesContext.messages['webformTableWithoutFixedNumber']);
    }

    return errorMessages;
  };

  return (
    <Fragment>
      {renderWebformContent(webformRecordState.record)}

      {isDialogVisible.deleteRow && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={webformRecordState.isDeleting}
          header={resourcesContext.messages['deleteRow']}
          iconConfirm={webformRecordState.isDeleting ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => onDeleteMultipleWebform(selectedRecordId)}
          onHide={() => handleDialogs('deleteRow', false)}
          visible={isDialogVisible.deleteRow}>
          {resourcesContext.messages['confirmDeleteRow']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};

WebformRecord.propTypes = { record: PropTypes.shape({ elements: PropTypes.array }) };

WebformRecord.defaultProps = { record: { elements: [], totalRecords: 0 } };
