import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isNil from 'lodash/isNil';

import styles from './NationalSystemsField.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { nationalSystemsFieldReducer } from './_functions/Reducers/nationalSystemsFieldReducer';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { MetadataUtils } from 'ui/views/_functions/Utils';
import { RecordUtils, TextUtils } from 'ui/views/_functions/Utils';

export const NationalSystemsField = ({ datasetId, field, key, title, tooltip }) => {
  const resources = useContext(ResourcesContext);

  const [nationalSystemsFieldState, nationalSystemsFieldDispatch] = useReducer(nationalSystemsFieldReducer, {
    isDialogVisible: { deleteAttachment: false, uploadFile: false },
    selected: { validExtensions: [] },
    selectedValidExtensions: []
  });

  const { isDialogVisible, selectedValidExtensions } = nationalSystemsFieldState;

  const getAttachExtensions = [{ fileExtension: selectedValidExtensions || [] }]
    .map(file => file.fileExtension.map(extension => (extension.indexOf('.') > -1 ? extension : `.${extension}`)))
    .flat()
    .join(', ');

  const handleDialogs = (dialog, value) => {
    nationalSystemsFieldDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, value } });
  };

  const onEditorSubmitValue = async (field, option, value) => {
    const parsedValue =
      field.fieldType === 'MULTISELECT_CODELIST' || (field.fieldType === 'LINK' && Array.isArray(value))
        ? value.join(',')
        : value;

    try {
      await DatasetService.updateFieldById(datasetId, option, field.fieldId, field.fieldType, parsedValue);
    } catch (error) {
      console.error('error', error);
    }
  };

  const onFillField = (field, option, value) => {
    nationalSystemsFieldDispatch({ type: 'ON_FILL_FIELD', payload: { field, option, value } });
  };

  const renderTemplate = () => {
    const { fieldSchemaId, type } = field;

    switch (type) {
      case 'EMAIL':
      case 'NUMBER_DECIMAL':
      case 'NUMBER_INTEGER':
      case 'PHONE':
      case 'RICH_TEXT':
      case 'TEXT':
      case 'URL':
        return (
          <InputText
            keyfilter={RecordUtils.getFilter(type)}
            // id={field.fieldId}
            // maxLength={getInputMaxLength[type]}
            onBlur={event => {}}
            onChange={event => onFillField(field, fieldSchemaId, event.target.value)}
            // onKeyDown={event => onEditorKeyChange(event, field, option)}
            // value={field.value}
          />
        );

      case 'TEXTAREA':
        return (
          <InputTextarea
            // className={field.required ? styles.required : undefined}
            // id={field.fieldId}
            // maxLength={getInputMaxLength[type]}
            collapsedHeight={150}
            onBlur={event => {}}
            // onChange={event => onFillField(field, option, event.target.value)}
            // onKeyDown={event => onEditorKeyChange(event, field, option)}
            value={field.value}
          />
        );

      case 'ATTACHMENT':
        // const colSchema = columnsSchema.filter(colSchema => colSchema.fieldSchemaId === field.fieldSchemaId)[0];

        return (
          <div className={styles.attachmentWrapper}>
            {!isNil(field.value) && field.value !== '' && (
              <Button
                className={`${field.value === '' && 'p-button-animated-blink'} p-button-primary-transparent`}
                icon="export"
                iconPos="right"
                label={field.value}
                // onClick={() => onFileDownload(field.value, field.fieldId)}
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
                  // onToggleDialogVisible(true);
                  // onFileUploadVisible(
                  //   field.fieldId,
                  //   field.fieldSchemaId,
                  //   !isNil(colSchema) ? colSchema.validExtensions : [],
                  //   !isNil(colSchema) ? colSchema.maxSize : 20
                  // );
                }}
              />
            }

            <Button
              className={`p-button-animated-blink p-button-primary-transparent`}
              icon="trash"
              onClick={() => {
                // onFileDeleteVisible(field.fieldId, field.fieldSchemaId);
                handleDialogs('deleteAttachment', true);
              }}
            />
          </div>
        );

      default:
        break;
    }
  };

  return (
    <div className={styles.content} key={key}>
      <div className={styles.titleWrapper}>
        <h4>{title.value || title}</h4>
        <Button
          className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
          icon="infoCircle"
          tooltip={tooltip || tooltip.value}
          tooltipOptions={{ position: 'top' }}
        />
      </div>
      {renderTemplate()}

      {isDialogVisible.uploadFile && (
        <CustomFileUpload
          accept={getAttachExtensions || '*'}
          chooseLabel={resources.messages['selectFile']}
          // className={styles.fileUpload}
          // dialogClassName={styles.dialog}
          // dialogHeader={resources.messages['uploadAttachment']}
          // dialogOnHide={() => onToggleDialogVisible(false)}
          // dialogVisible={uploadFile}
          // fileLimit={1}
          // invalidExtensionMessage={resources.messages['invalidExtensionFile']}
          // isDialog={true}
          // mode="advanced"
          // multiple={false}
          // name="file"
          // onUpload={onAttach}
          // operation="PUT"
          // url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.addAttachment, {
          //   datasetId,
          //   fieldId: selectedFieldId
          // })}`}
        />
      )}
      {isDialogVisible.deleteAttachment && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resources.messages['deleteAttachmentHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          // onConfirm={onConfirmDeleteAttachment}
          onHide={() => handleDialogs('deleteAttachment', false)}
          visible={isDialogVisible.deleteAttachment}>
          {resources.messages['deleteAttachmentConfirm']}
        </ConfirmDialog>
      )}
    </div>
  );
};
