import { Fragment, useContext, useEffect, useReducer } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniqBy from 'lodash/uniqBy';

import styles from './WebformRecord.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { GroupedRecordValidations } from 'ui/views/Webforms/_components/GroupedRecordValidations';
import { IconTooltip } from 'ui/views/_components/IconTooltip';

import { WebformField } from './_components/WebformField';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { webformRecordReducer } from './_functions/Reducers/webformRecordReducer';

import { MetadataUtils } from 'ui/views/_functions/Utils';
import { TextUtils } from 'ui/views/_functions/Utils';
import { WebformsUtils } from 'ui/views/Webforms/_functions/Utils/WebformsUtils';
import { WebformRecordUtils } from './_functions/Utils/WebformRecordUtils';

export const WebformRecord = ({
  addingOnTableSchemaId,
  calculateSingle,
  columnsSchema,
  dataProviderId,
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
  onUpdateSinglesList,
  onUpdatePamsValue,
  pamsRecords,
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
      const response = await DatasetService.deleteRecordById(
        datasetId,
        selectedRecordId,
        webformRecordState.record?.elements?.some(element => element.deleteInCascade)
      );
      if (response.status >= 200 && response.status <= 299) {
        onRefresh();
        handleDialogs('deleteRow', false);
      }
    } catch (error) {
      console.error('error', error);
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      } else {
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add({
          type: 'DELETE_RECORD_BY_ID_ERROR',
          content: { dataflowId, dataflowName, datasetId, datasetName, tableName }
        });
      }
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
    if (!isNil(isGroup) && isGroup()) return true;
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

  const checkCalculatedFieldVisibility = el => {
    if (isNil(isGroup)) {
      return false;
    } else {
      if (isGroup() && el.calculatedWhenGroup && !el.hideWhenCalculated) {
        return true;
      } else {
        return false;
      }
    }
  };

  const checkCalculatedTableVisibility = el => {
    if (isNil(isGroup)) {
      return false;
    } else {
      if (isGroup() && !isNil(el.hasCalculatedFields)) {
        return true;
      } else {
        return false;
      }
    }
  };

  const checkLabelVisibility = el => {
    if (isNil(isGroup)) {
      return true;
    } else {
      if ((isGroup() && el.hideWhenCalculated) || (!isGroup() && el.hideWhenSingle)) {
        return false;
      } else {
        return true;
      }
    }
  };

  const handleDialogs = (dialog, value) => {
    webformRecordDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, value } });
  };

  const renderElements = (elements = [], fieldsBlock = false) => {
    return elements.map((element, i) => {
      const isFieldVisible = element.fieldType === 'EMPTY' && isReporting;
      const isSubTableVisible = element.tableNotCreated && isReporting;
      if (element.type === 'BLOCK') {
        const isSubtable = () => {
          return element.elementsRecords.length > 1;
        };

        if (isSubtable()) {
          return (
            <div className={styles.fieldsBlock} key={i}>
              {element.elementsRecords
                .filter(record => elements[0].recordId === record.recordId)
                .map(record => renderElements(record.elements, true))}
            </div>
          );
        }

        return (
          <div className={styles.fieldsBlock} key={i}>
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
          checkLabelVisibility(element) &&
          !isFieldVisible &&
          onToggleFieldVisibility(element.dependency, elements, element) && (
            <div className={styles.field} key={i} style={fieldStyle}>
              {(element.required || element.title) && isNil(element.customType) && (
                <label>
                  {element.title}
                  {
                    <span className={styles.requiredMark}>
                      {element.required || element.showRequiredCharacter ? ' *' : ''}
                    </span>
                  }
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
                  {checkCalculatedFieldVisibility(element) ? (
                    calculateSingle(element)
                  ) : (
                    <WebformField
                      columnsSchema={columnsSchema}
                      dataProviderId={dataProviderId}
                      dataflowId={dataflowId}
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
                      onUpdatePamsValue={onUpdatePamsValue}
                      onUpdateSinglesList={onUpdateSinglesList}
                      pamsRecords={pamsRecords}
                      record={record}
                    />
                  )}
                </div>
                {element.validations &&
                  uniqBy(element.validations, element => {
                    return [element.message, element.errorLevel].join();
                  }).map((validation, index) => (
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
            <div key={element.title}>
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
          )
        );
      } else {
        return (
          !isSubTableVisible &&
          onToggleFieldVisibility(element.dependency, elements, element) && (
            <div className={element.showInsideParentTable ? styles.showInsideParentTable : styles.subTable} key={i}>
              {!element.showInsideParentTable && (
                <div className={styles.title}>
                  <h3>
                    {element.title ? element.title : element.name}
                    {<span style={{ color: 'var(--errors)' }}>{element.showRequiredCharacter ? ' *' : ''}</span>}
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
                      onClick={() => {
                        let filteredRecordId = null;
                        if (TextUtils.areEquals(element.name, 'OtherObjectives')) {
                          const filteredTable = elements.filter(element =>
                            TextUtils.areEquals(element.name, 'SectorAffected')
                          );

                          if (!isEmpty(filteredTable)) filteredRecordId = filteredTable[0].recordId;
                        }
                        onAddMultipleWebform(element.tableSchemaId, filteredRecordId);
                      }}
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

              {checkCalculatedTableVisibility(element)
                ? calculateSingle(element)
                : filterRecords(element, elements).map(record => {
                    return (
                      <WebformRecord
                        addingOnTableSchemaId={addingOnTableSchemaId}
                        calculateSingle={calculateSingle}
                        columnsSchema={columnsSchema}
                        dataProviderId={dataProviderId}
                        dataflowId={dataflowId}
                        datasetId={datasetId}
                        datasetSchemaId={datasetSchemaId}
                        isAddingMultiple={isAddingMultiple}
                        isGroup={isGroup}
                        key={record.recordId}
                        multipleRecords={element.multipleRecords}
                        newRecord={webformRecordState.newRecord}
                        onAddMultipleWebform={onAddMultipleWebform}
                        onRefresh={onRefresh}
                        onTabChange={onTabChange}
                        onUpdatePamsValue={onUpdatePamsValue}
                        onUpdateSinglesList={onUpdateSinglesList}
                        pamsRecords={pamsRecords}
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

  const filterRecords = (element, elements) => {
    if (!TextUtils.areEquals(element.name, 'OtherObjectives')) {
      return element.elementsRecords;
    }
    const filteredIdField = elements.filter(element => TextUtils.areEquals(element.name, 'Id_SectorObjectives'))[0];
    const filteredIdSchema = element.elements.filter(element =>
      TextUtils.areEquals(element.name, 'Fk_SectorObjectives')
    )[0];

    const filtered = element.elementsRecords.filter(
      record =>
        record.fields.filter(field => field.fieldSchemaId === filteredIdSchema.fieldSchema)[0].value ===
        filteredIdField.value
    );

    return filtered;
  };

  const renderWebformContent = content => {
    const errorMessages = renderErrorMessages(content);

    return (
      <div className={styles.content}>
        {multipleRecords && !isEmpty(content.elements) && (
          <div className={styles.actionButtons}>
            <GroupedRecordValidations parsedRecordData={parseRecordValidations(webformRecordState.record)} />
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
          disabledConfirm={webformRecordState.isDeleting}
          header={resources.messages['deleteRow']}
          iconConfirm={webformRecordState.isDeleting ? 'spinnerAnimate' : 'check'}
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
