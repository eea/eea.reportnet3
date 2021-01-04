import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import ReactTooltip from 'react-tooltip';

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

import { getUrl } from 'core/infrastructure/CoreUtils';
import { MetadataUtils } from 'ui/views/_functions/Utils';
import { RecordUtils, TextUtils } from 'ui/views/_functions/Utils';

export const NationalSystemsField = ({ field, key, title, tooltip }) => {
  const resources = useContext(ResourcesContext);
  // console.log('field', field);

  console.log('title', title);
  console.log('tooltip', tooltip);

  const onFillField = () => {};

  const renderTemplate = () => {
    const { type } = field;

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
            // onChange={event => onFillField(field, option, event.target.value)}
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
              // onClick={() => onFileDeleteVisible(field.fieldId, field.fieldSchemaId)}
            />
          </div>
        );

      default:
        break;
    }
  };

  return (
    <Fragment key={key}>
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
    </Fragment>
  );
};
