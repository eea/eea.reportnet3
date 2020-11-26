import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { DatasetConfig } from 'conf/domain/model/Dataset';

import styles from './WebformRecord.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { webformRecordReducer } from './_functions/Reducers/webformRecordReducer';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { MetadataUtils } from 'ui/views/_functions/Utils';
import { PaMsUtils } from './_functions/Utils/PaMsUtils';
import { TextUtils } from 'ui/views/_functions/Utils';
import { WebformRecordUtils } from './_functions/Utils/WebformRecordUtils';

export const WebformRecord = ({
  addingOnTableSchemaId,
  columnsSchema,
  dataflowId,
  datasetId,
  hasFields,
  isAddingMultiple,
  isFixedNumber = true,
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
    isDeleteAttachmentVisible: false,
    isDeleteRowVisible: false,
    isDeletingRow: false,
    isDialogVisible: { deleteRow: false, uploadFile: false },
    isFileDialogVisible: false,
    newRecord: {},
    record,
    sectorAffectedValue: null,
    selectedField: {},
    selectedFieldId: '',
    selectedFieldSchemaId: '',
    selectedMaxSize: '',
    selectedRecordId: null,
    selectedValidExtensions: []
  });

  const {
    isDeleteAttachmentVisible,
    isDialogVisible,
    isFileDialogVisible,
    sectorAffectedValue,
    selectedField,
    selectedFieldId,
    selectedFieldSchemaId,
    selectedRecordId,
    selectedValidExtensions
  } = webformRecordState;

  const {
    formatDate,
    getInputMaxLength,
    getInputType,
    getMultiselectValues,
    parseMultiselect,
    parseNewRecordData
  } = WebformRecordUtils;

  const { getObjectiveOptions } = PaMsUtils;

  useEffect(() => {
    webformRecordDispatch({
      type: 'INITIAL_LOAD',
      payload: { newRecord: parseNewRecordData(record.elements, undefined), record, isDeleting: false }
    });
  }, [record, onTabChange]);

  const getAttachExtensions = [{ fileExtension: selectedValidExtensions || [] }]
    .map(file => file.fileExtension.map(extension => (extension.indexOf('.') > -1 ? extension : `.${extension}`)))
    .flat()
    .join(', ');

  const onAttach = async value => {
    onFillField(record, selectedFieldSchemaId, `${value.files[0].name}`);
    onToggleDialogVisible(false);
  };

  const onConfirmDeleteAttachment = async () => {
    const fileDeleted = await DatasetService.deleteFileData(datasetId, selectedFieldId);
    if (fileDeleted) {
      onFillField(record, selectedFieldSchemaId, '');
      onToggleDeleteAttachmentDialogVisible(false);
    }
  };

  const onDeleteMultipleWebform = async () => {
    webformRecordDispatch({
      type: 'SET_IS_DELETING',
      payload: { isDeleting: true }
    });
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

  const onFileDownload = async (fileName, fieldId) => {
    const fileContent = await DatasetService.downloadFileData(datasetId, fieldId);

    DownloadFile(fileContent, fileName);
  };

  const onEditorKeyChange = (event, field, option) => {
    if (event.key === 'Escape') {
    } else if (event.key === 'Enter') {
      onEditorSubmitValue(field, option, event.target.value);
    } else if (event.key === 'Tab') {
      onEditorSubmitValue(field, option, event.target.value);
    }
  };

  const onEditorSubmitValue = async (field, option, value) => {
    const parsedValue =
      field.fieldType === 'MULTISELECT_CODELIST' || (field.fieldType === 'LINK' && Array.isArray(value))
        ? value.join(',')
        : value;

    try {
      DatasetService.updateFieldById(datasetId, option, field.fieldId, field.fieldType, parsedValue);
    } catch (error) {
      console.error('error', error);
    }
  };

  const onFileDeleteVisible = (fieldId, fieldSchemaId) =>
    webformRecordDispatch({ type: 'ON_FILE_DELETE_OPENED', payload: { fieldId, fieldSchemaId } });

  const onFileUploadVisible = (fieldId, fieldSchemaId, validExtensions, maxSize) => {
    webformRecordDispatch({
      type: 'ON_FILE_UPLOAD_SET_FIELDS',
      payload: { fieldId, fieldSchemaId, validExtensions, maxSize }
    });
  };

  const onFillField = (field, option, value) => {
    webformRecordState.newRecord.dataRow.filter(data => Object.keys(data.fieldData)[0] === option)[0].fieldData[
      option
    ] = value;

    webformRecordState.record.elements.filter(field => field.fieldSchemaId === option)[0].value = value;

    webformRecordDispatch({ type: 'ON_FILL_FIELD', payload: { field, option, value } });
  };

  const onSaveField = async (option, value, recordId) => {
    try {
      await DatasetService.addRecordsById(datasetId, tableId, [parseMultiselect(webformRecordState.newRecord)]);
    } catch (error) {
      console.error('error', error);
    }
  };

  const onSelectField = field => webformRecordDispatch({ type: 'ON_SELECT_FIELD', payload: { field } });

  const onToggleDeleteAttachmentDialogVisible = value =>
    webformRecordDispatch({ type: 'ON_TOGGLE_DELETE_DIALOG', payload: { value } });

  const onToggleDialogVisible = value => webformRecordDispatch({ type: 'ON_TOGGLE_DIALOG', payload: { value } });

  const handleDialogs = (dialog, value) => {
    webformRecordDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, value } });
  };

  const renderTemplate = (field, option, type) => {
    switch (type) {
      case 'DATE':
        return (
          <Calendar
            appendTo={document.body}
            dateFormat="yy-mm-dd"
            id={field.fieldId}
            monthNavigator={true}
            onChange={event => {
              onFillField(field, option, formatDate(event.target.value, isNil(event.target.value)));
              if (isNil(field.recordId)) onSaveField(option, formatDate(event.target.value, isNil(event.target.value)));
              else onEditorSubmitValue(field, option, formatDate(event.target.value, isNil(event.target.value)));
            }}
            onFocus={event => {}}
            value={new Date(field.value)}
            yearNavigator={true}
            yearRange="2010:2030"
          />
        );

      case 'LINK':
        if (field.pkHasMultipleValues) {
          return (
            <MultiSelect
              // onChange={e => onChangeForm(field, e.value)}
              appendTo={document.body}
              clearButton={false}
              filter={true}
              filterPlaceholder={resources.messages['linkFilterPlaceholder']}
              maxSelectedLabels={10}
              onChange={e => {
                // try {
                //   setLinkItemsValue(e.value);
                //   onEditorValueChange(cells, e.value);
                //   onEditorSubmitValue(cells, e.value, record);
                // } catch (error) {
                //   console.error(error);
                // }
              }}
              // onFilterInputChangeBackend={onFilter}
              onFocus={e => {
                e.preventDefault();
                // if (!isUndefined(codelistItemValue)) {
                //   onEditorValueFocus(cells, codelistItemValue);
                // }
              }}
              // options={linkItemsOptions}
              options={[]}
              optionLabel="itemType"
              // value={RecordUtils.getMultiselectValues(linkItemsOptions, linkItemsValue)}
              value={field.value}
            />
          );
        } else {
          return (
            <Dropdown
              appendTo={document.body}
              // currentValue={RecordUtils.getCellValue(cells, cells.field)}
              currentValue={field.value}
              filter={true}
              filterPlaceholder={resources.messages['linkFilterPlaceholder']}
              filterBy="itemType,value"
              onChange={e => {
                // setLinkItemsValue(e.target.value.value);
                // onEditorValueChange(cells, e.target.value.value);
                // onEditorSubmitValue(cells, e.target.value.value, record);
              }}
              // onFilterInputChangeBackend={onFilter}
              onMouseDown={e => {
                // onEditorValueFocus(cells, e.target.value);
              }}
              optionLabel="itemType"
              // options={linkItemsOptions}
              options={[]}
              showFilterClear={true}
              // value={RecordUtils.getLinkValue(linkItemsOptions, linkItemsValue)}
              value={field.value}
            />
          );
        }
      // return (

      //   // <InputText
      //   //   // keyfilter={getFilter(type)}
      //   //   // maxLength={urlCharacters}
      //   //   className={'p-disabled'}
      //   //   disabled={field.isDisabled}
      //   //   id={field.fieldId}
      //   //   onBlur={event => {}}
      //   //   onChange={event => {}}
      //   //   onFocus={event => {
      //   //     event.preventDefault();
      //   //     // onEditorValueFocus(cells, event.target.value);
      //   //   }}
      //   //   onKeyDown={event => {}}
      //   //   value={field.value}
      //   // />
      // );

      case 'MULTISELECT_CODELIST':
        return (
          <MultiSelect
            appendTo={document.body}
            maxSelectedLabels={10}
            id={field.fieldId}
            onChange={event => {
              onFillField(field, option, event.target.value);
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            // onFocus={e => {
            //   e.preventDefault();
            //   if (!isUndefined(codelistItemValue)) {
            //     onEditorValueFocus(cells, codelistItemValue);
            //   }
            // }}
            options={
              field.name === 'Objective'
                ? getObjectiveOptions(sectorAffectedValue)
                : field.codelistItems.map(codelist => ({ label: codelist, value: codelist }))
            }
            // optionLabel="itemType"
            value={getMultiselectValues(
              field.codelistItems.map(codelist => ({ label: codelist, value: codelist })),
              field.value
            )}
          />
        );

      case 'CODELIST':
        return (
          <Dropdown
            appendTo={document.body}
            id={field.fieldId}
            // currentValue={RecordUtils.getCellValue(cells, cells.field)}
            // filter={true}
            // filterPlaceholder={resources.messages['linkFilterPlaceholder']}
            // filterBy="itemType,value"
            onChange={event => {
              onFillField(field, option, event.target.value);
              webformRecordDispatch({ type: 'SET_SECTOR_AFFECTED', payload: { value: event.target.value } });
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            // onFilterInputChangeBackend={onFilter}
            // onMouseDown={e => onEditorValueFocus(cells, event.target.value)}
            // optionLabel="label"
            options={field.codelistItems.map(codelist => ({ label: codelist, value: codelist }))}
            showFilterClear={true}
            value={field.value}
          />
        );

      case 'TEXT':
      case 'RICH_TEXT':
      case 'URL':
      case 'EMAIL':
      case 'PHONE':
      case 'NUMBER_INTEGER':
      case 'NUMBER_DECIMAL':
        return (
          <InputText
            id={field.fieldId}
            // keyfilter={getInputType[type]}
            maxLength={getInputMaxLength[type]}
            onBlur={event => {
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            onChange={event => onFillField(field, option, event.target.value)}
            onKeyDown={event => onEditorKeyChange(event, field, option)}
            type="text"
            value={field.value}
          />
        );
      case 'TEXTAREA':
        return (
          <InputTextarea
            className={field.required ? styles.required : undefined}
            id={field.fieldId}
            maxLength={getInputMaxLength[type]}
            collapsedHeight={150}
            onBlur={event => {
              if (isNil(field.recordId)) onSaveField(option, event.target.value);
              else onEditorSubmitValue(field, option, event.target.value);
            }}
            onChange={event => onFillField(field, option, event.target.value)}
            onKeyDown={event => onEditorKeyChange(event, field, option)}
            value={field.value}
          />
        );
      case 'EMPTY':
        return (
          <div className={styles.infoButtonWrapper}>
            <Button
              className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
              icon="errorCircle"
            />
            <span
              className={styles.nonExistField}
              dangerouslySetInnerHTML={{
                __html: TextUtils.parseText(resources.messages['fieldIsNotCreated'], { fieldName: field.name })
              }}
            />
          </div>
        );

      case 'ATTACHMENT':
        const colSchema = columnsSchema.filter(colSchema => colSchema.fieldSchemaId === field.fieldSchemaId)[0];
        return (
          <div className={styles.attachmentWrapper}>
            {!isNil(field.value) && field.value !== '' && (
              <Button
                className={`${field.value === '' && 'p-button-animated-blink'} p-button-primary-transparent`}
                icon="export"
                iconPos="right"
                label={field.value}
                onClick={() => onFileDownload(field.value, field.fieldId)}
              />
            )}
            {
              <Button
                className={`p-button-animated-blink p-button-primary-transparent`}
                icon="import"
                label={
                  !isNil(field.value) && field.value !== ''
                    ? resources.messages['uploadReplaceAttachment']
                    : resources.messages['uploadAttachment']
                }
                onClick={() => {
                  onToggleDialogVisible(true);
                  onFileUploadVisible(
                    field.fieldId,
                    field.fieldSchemaId,
                    !isNil(colSchema) ? colSchema.validExtensions : [],
                    !isNil(colSchema) ? colSchema.maxSize : 20
                  );
                }}
              />
            }

            <Button
              className={`p-button-animated-blink p-button-primary-transparent`}
              icon="trash"
              onClick={() => onFileDeleteVisible(field.fieldId, field.fieldSchemaId)}
            />
          </div>
        );
      // return (
      //   <Button
      //     className={`p-button-animated-blink p-button-primary-transparent`}
      //     // disabled={true}
      //     icon={'import'}
      //     label={resources.messages['uploadAttachment']}
      //     onClick={() => {
      //       onToggleDialogVisible(true);
      //       onSelectField(field);
      //       // setIsAttachFileVisible(true);
      //       // onFileUploadVisible(
      //       //   fieldId,
      //       //   fieldSchemaId,
      //       //   !isNil(colSchema) ? colSchema.validExtensions : [],
      //       //   colSchema.maxSize
      //       // );
      //     }}
      //   />
      //);

      default:
        break;
    }
  };

  const renderElements = (elements = []) => {
    return elements.map((element, i) => {
      const isFieldVisible = element.fieldType === 'EMPTY' && isReporting;
      const isSubTableVisible = element.tableNotCreated && isReporting;

      if (element.type === 'FIELD') {
        return (
          !isFieldVisible && (
            <div key={i} className={styles.field}>
              {(element.required || element.title) && <label>{`${element.required ? '*' : ''}${element.title}`}</label>}

              {element.tooltip && (
                <Button
                  className={`${styles.infoCircle} p-button-rounded p-button-secondary-transparent`}
                  icon="infoCircle"
                  tooltip={element.tooltip}
                  tooltipOptions={{ position: 'top' }}
                />
              )}
              <div className={styles.fieldWrapper}>
                <div className={styles.template}>
                  {renderTemplate(element, element.fieldSchemaId, element.fieldType)}
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
          <Fragment>
            {element.level === 2 && <h2 className={styles[`label${element.level}`]}>{element.title}</h2>}
            {element.level === 3 && <h3 className={styles[`label${element.level}`]}>{element.title}</h3>}
          </Fragment>
        );
      } else {
        return (
          !isSubTableVisible && (
            <div key={i} className={styles.subTable}>
              <h3 className={styles.title}>
                <div>
                  {element.title ? element.title : element.name}
                  {element.hasErrors && (
                    <IconTooltip levelError={'ERROR'} message={resources.messages['tableWithErrorsTooltip']} />
                  )}
                </div>
                {element.multipleRecords && (
                  <Button
                    disabled={addingOnTableSchemaId === element.tableSchemaId && isAddingMultiple}
                    icon={
                      addingOnTableSchemaId === element.tableSchemaId && isAddingMultiple ? 'spinnerAnimate' : 'plus'
                    }
                    label={resources.messages['addRecord']}
                    onClick={() => onAddMultipleWebform(element.tableSchemaId)}
                  />
                )}
              </h3>
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
                    dataflowId={dataflowId}
                    datasetId={datasetId}
                    key={i}
                    multipleRecords={element.multipleRecords}
                    onAddMultipleWebform={onAddMultipleWebform}
                    onRefresh={onRefresh}
                    onTabChange={onTabChange}
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
      {isFileDialogVisible && (
        <CustomFileUpload
          dialogClassName={styles.dialog}
          dialogHeader={resources.messages['uploadAttachment']}
          dialogOnHide={() => onToggleDialogVisible(false)}
          dialogVisible={isFileDialogVisible}
          accept={getAttachExtensions || '*'}
          chooseLabel={resources.messages['selectFile']}
          className={styles.fileUpload}
          fileLimit={1}
          isDialog={true}
          mode="advanced"
          multiple={false}
          invalidExtensionMessage={resources.messages['invalidExtensionFile']}
          name="file"
          onUpload={onAttach}
          operation="PUT"
          url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.importFileData, {
            datasetId,
            fieldId: selectedFieldId
          })}`}
        />
      )}

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

      {isDeleteAttachmentVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={`${resources.messages['deleteAttachmentHeader']}`}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={onConfirmDeleteAttachment}
          onHide={() => onToggleDeleteAttachmentDialogVisible(false)}
          visible={isDeleteAttachmentVisible}>
          {resources.messages['deleteAttachmentConfirm']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};

WebformRecord.propTypes = { record: PropTypes.shape({ elements: PropTypes.array }) };

WebformRecord.defaultProps = { record: { elements: [], totalRecords: 0 } };
