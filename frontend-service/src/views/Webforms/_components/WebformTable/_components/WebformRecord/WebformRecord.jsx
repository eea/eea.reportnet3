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
  onFieldUpdate,
  columnsSchema,
  dataflowId,
  dataProviderId,
  datasetId,
  datasetSchemaId,
  hasFields,
  isAddingMultiple,
  isFixedNumber = true,
  isOptional,
  isReporting,
  updatingField,
  isViewMode,
  multipleRecords,
  onAddMultipleWebform,
  onRefresh,
  onTabChange,
  record,
  referencedTableSchemaId,
  rootPkFieldId,
  rootTableName,
  selectedTableId,
  tableId,
  tableName,
  webformType
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [webformRecordState, webformRecordDispatch] = useReducer(webformRecordReducer, {
    changedConditionalFieldData: null,
    conditionalFieldChange: false,
    dependantConditionalFieldId: '',
    isConditionalChanged: false,
    isDependantConditionalField: false,
    isDialogVisible: { deleteRow: false, uploadFile: false },
    newRecord: {},
    record,
    selectedMaxSize: '',
    selectedRecordId: null
  });

  const {
    changedConditionalFieldData,
    conditionalFieldChange,
    dependantConditionalFieldId,
    isConditionalChanged,
    isDependantConditionalField,
    isDialogVisible,
    selectedRecordId
  } = webformRecordState;

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

    try {
      await DatasetService.deleteRecord({
        datasetId,
        selectedRecordId,
        tableId: selectedTableId ? selectedTableId : tableId,
        updateInCascade: true
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

  const getCreatedSubTable = (record, element) => {
    const subTableCreated =
      !isNil(record) &&
      record.elements.filter(
        col => col.name !== rootTableName && (col.type === 'TABLE') & !isEmpty(col.elementsRecords)
      ).length > 0;

    let subTablesList;
    let isSubTableFk;

    if (subTableCreated) {
      subTablesList =
        !isNil(record) &&
        record.elements.filter(
          col => col.name !== rootTableName && (col.type === 'TABLE') & !isEmpty(col.elementsRecords)
        );

      const subTablesFkNamesList = subTablesList.map(subTable => {
        const namesList = subTable.elements
          .filter(col => col.type === 'FIELD' && col.referenceParentField)
          .map(field => (field = { name: field.referenceParentField }));

        return (subTable = namesList);
      });

      isSubTableFk = subTablesFkNamesList.some(subTable =>
        subTable.some(fieldName => fieldName.name === element.name || element.isPrimary === true)
      );
    }
    return isSubTableFk;
  };

  const onSaveField = async () => {
    try {
      await DatasetService.createRecord(datasetId, tableId, [parseMultiselect(webformRecordState.newRecord)]);
    } catch (error) {
      console.error('WebformRecord - onSaveField.', error);
    }
  };

  const onToggleFieldVisibility = (referenceParentField, fields = []) => {
    if (isNil(referenceParentField)) return true;
    const filteredDependency = fields
      .filter(field => TextUtils.areEquals(field.name, referenceParentField.field))
      .map(filtered => (Array.isArray(filtered?.value) ? filtered?.value : filtered?.value?.split(/\s*;\s*/)));

    return filteredDependency
      .flat()
      .map(field => referenceParentField.value.includes(field))
      .includes(true);
  };

  const handleDialogs = (dialog, value) => {
    webformRecordDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, value } });
  };

  const checkIfElementIsConditional = element => {
    const matchesCondition = el =>
      el?.referencedField?.masterConditionalFieldId === element.fieldSchemaId ||
      (!isEmpty(el?.referenceParentField) && el.referenceParentField.field === element.name) ||
      (el?.type === 'BLOCK' && el.elements?.some(matchesCondition));

    return webformRecordState.record?.elements?.some(matchesCondition) ?? false;
  };

  const renderElements = (elements = [], isLabelFieldBlock = false) => {
    return elements.map((element, i) => {
      const isFieldVisible = element.fieldType === 'EMPTY' && isReporting;
      const isSubTableVisible = element.tableNotCreated && isReporting;
      if (element.type === 'BLOCK') {
        const isBlockVisible = element.referenceParentField
          ? onToggleFieldVisibility(element.referenceParentField, elements)
          : true;
        const isSubTable = () => element.elementsRecords.length > 1;
        if (isSubTable()) {
          return (
            isBlockVisible && (
              <div className={styles.fieldsBlock} key={`BLOCK_${i}`}>
                {element.elementsRecords
                  .filter(elementsRecord => elementsRecord.recordId === record.recordId)
                  .map(record => renderElements(record.elements))}
              </div>
            )
          );
        }

        return (
          isBlockVisible && (
            <div className={styles.fieldsBlock} key={`BLOCK_${i}`}>
              {element.elementsRecords.map(record => renderElements(record.elements))}
            </div>
          )
        );
      }

      if (element.type === 'FIELD') {
        const fieldStyle = { width: '100%' };
        if (isLabelFieldBlock) {
          const elementCount = elements.length;
          const elementGap = 5 * elementCount;
          const elementWidth = (100 - elementGap) / elementCount;
          fieldStyle.width = elementWidth;
        }

        return (
          !isFieldVisible &&
          element.isVisible !== false &&
          onToggleFieldVisibility(element.referenceParentField, elements, element) && (
            <div
              className={isLabelFieldBlock ? styles.tableField : styles.field}
              key={element.fieldId || element.fieldSchemaId}
              style={fieldStyle}>
              {(element.required || element.title) && isNil(element.customType) && (
                <label className={isLabelFieldBlock ? styles.fieldLabel : undefined}>
                  {element.title}
                  {<span className={styles.requiredMark}>{checkShowRequired(element, elements) ? ' *' : ''}</span>}
                  {isLabelFieldBlock && element.tooltip && isNil(element.customType) && (
                    <Button
                      className={`${styles.infoCircle} p-button-rounded p-button-secondary-transparent`}
                      icon="infoCircle"
                      tooltip={element.tooltip}
                      tooltipOptions={{ position: 'top' }}
                    />
                  )}
                </label>
              )}

              {!isLabelFieldBlock && element.tooltip && isNil(element.customType) && (
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
                      changedConditionalFieldData={changedConditionalFieldData}
                      onFieldUpdate={onFieldUpdate}
                      columnsSchema={columnsSchema}
                      conditionalFieldChange={conditionalFieldChange}
                      dataflowId={dataflowId}
                      dataProviderId={dataProviderId}
                      datasetId={datasetId}
                      datasetSchemaId={datasetSchemaId}
                      dependantConditionalFieldId={dependantConditionalFieldId}
                      element={element}
                      hasErrors={!isNil(element.validations)}
                      isConditional={checkIfElementIsConditional(element)}
                      isConditionalChanged={isConditionalChanged}
                      isDependantConditionalField={isDependantConditionalField}
                      isSubTableCreated={getCreatedSubTable(webformRecordState.record, element)}
                      isViewMode={isViewMode}
                      onFillField={onFillField}
                      onSaveField={onSaveField}
                      record={record}
                      referencedTableSchemaId={referencedTableSchemaId}
                      rootPkFieldId={rootPkFieldId}
                      tableSchemaId={tableId}
                      updatingField={updatingField}
                      webformType={webformType}
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

        // Find reference PK field ID
        const referencePkFieldId = element.records[0]?.fields.find(
          field => !isNil(field?.referencedField?.idPk) && field?.referencedField?.idPk !== rootPkFieldId
        )?.referencedField?.idPk;

        // Find the PK value for that field ID
        let referencePkValue = record.elements.find(el =>
          [el.fieldSchema, el.fieldSchemaId].includes(referencePkFieldId)
        )?.value;

        // Fallback to root PK if needed
        if (isEmpty(referencePkValue) && !isNil(referencePkFieldId)) {
          referencePkValue = record.elements.find(el =>
            [el.fieldSchema, el.fieldSchemaId].includes(rootPkFieldId)
          )?.value;
        }

        // Build FK fields list
        const fkFields =
          element?.elements
            ?.filter(el => !isNil(el.referenceParentField))
            ?.map(field => {
              const referencedElement = record.elements.find(recordElement =>
                TextUtils.areEquals(field?.referenceParentField, recordElement.name)
              );
              return {
                fieldName: field.name,
                referenceFieldName: field?.referenceParentField,
                value: referencedElement?.value
              };
            }) ?? [];

        // Determine if any FK has an empty value
        if (!isEmpty(fkFields)) {
          fkHasEmptyValues = fkFields.some(field => isEmpty(field.value));
        }

        return (
          !isSubTableVisible &&
          onToggleFieldVisibility(element.referenceParentField, elements, element) && (
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

                  {(element.multipleRecords || (!element.multipleRecords && element.elementsRecords.length === 0)) && (
                    <Button
                      disabled={
                        fkHasEmptyValues ||
                        isEmpty(referencePkValue) ||
                        (addingOnTableSchemaId === element.tableSchemaId && isAddingMultiple) ||
                        isViewMode ||
                        updatingField.isUpdating
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

              {element.tableSchemaNotEmpty && isEmpty(element.elementsRecords) && (
                <span
                  className={styles.nonExistTable}
                  dangerouslySetInnerHTML={{
                    __html: TextUtils.parseText(resourcesContext.messages['mandatoryTableMessage'], {
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
                    onFieldUpdate={onFieldUpdate}
                    columnsSchema={columnsSchema}
                    dataflowId={dataflowId}
                    dataProviderId={dataProviderId}
                    datasetId={datasetId}
                    datasetSchemaId={datasetSchemaId}
                    isAddingMultiple={isAddingMultiple}
                    isViewMode={isViewMode}
                    key={i}
                    multipleRecords={element.multipleRecords}
                    newRecord={webformRecordState.newRecord}
                    onAddMultipleWebform={onAddMultipleWebform}
                    onRefresh={onRefresh}
                    onTabChange={onTabChange}
                    record={record}
                    referencedTableSchemaId={element?.tableSchemaId}
                    rootPkFieldId={rootPkFieldId}
                    rootTableName={rootTableName}
                    selectedTableId={element.tableSchemaId}
                    tableId={tableId}
                    tableName={element.title}
                    updatingField={updatingField}
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
        {(multipleRecords || isOptional) && !isEmpty(content.elements) && (
          <div className={styles.actionButtons}>
            {validationsTemplate(parseRecordValidations(webformRecordState.record))}
            <Button
              className={`${styles.delete} p-button-rounded p-button-secondary p-button-animated-blink`}
              disabled={webformRecordState.isDeleting || isViewMode || updatingField.isUpdating}
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
