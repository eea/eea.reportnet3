import React, { useContext, useEffect, useRef, useState } from 'react';

// import * as Yup from 'yup';
import isEqual from 'lodash/isEqual';
import isNull from 'lodash/isNull';
import isPlainObject from 'lodash/isPlainObject';
import isUndefined from 'lodash/isUndefined';
import sortBy from 'lodash/sortBy';

import styles from './DocumentFileUpload.module.scss';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { ErrorMessage } from 'ui/views/_components/ErrorMessage';

import { DocumentService } from 'core/services/Document';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DocumentFileUpload = ({
  dataflowId,
  documentInitialValues,
  isEditForm = false,
  isUploadDialogVisible,
  onUpload,
  setIsUploadDialogVisible,
  setFileUpdatingId,
  setIsUpdating
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const inputRef = useRef(null);

  const [errors, setErrors] = useState({
    description: { message: '', hasErrors: false },
    lang: { message: '', hasErrors: false },
    uploadFile: { message: '', hasErrors: false }
  });
  const [isUploading, setIsUploading] = useState(false);
  const [isSubmitting, setSubmitting] = useState(false);
  const [values, setValues] = useState(documentInitialValues);

  useEffect(() => {
    if (isUploadDialogVisible) inputRef.current.focus();
  }, [isUploadDialogVisible]);

  // const validationSchema = Yup.object().shape({
  //   description: Yup.string().required(' ').max(255, resources.messages['documentDescriptionValidationMax']),
  //   lang: Yup.string().required(),
  //   uploadFile: isEditForm
  //     ? Yup.mixed()
  //         .test('checkSizeWhenNotEmpty', resources.messages['tooLargeFileValidationError'], value => {
  //           if (isUndefined(value) || isPlainObject(value) || isNull(value)) {
  //             return true;
  //           } else {
  //             return value.size <= config.MAX_FILE_SIZE;
  //           }
  //         })
  //         .nullable()
  //     : Yup.mixed()
  //         .test('fileEmpty', ' ', value => {
  //           return !isPlainObject(value);
  //         })
  //         .test('fileSize', resources.messages['tooLargeFileValidationError'], value => {
  //           return value.size <= config.MAX_FILE_SIZE;
  //         })
  // });

  const checkIsEmptyInput = inputValue => inputValue.trim() === '';

  //description message resources.messages['documentDescriptionValidationMax'])
  const checkIsCorrectLength = inputValue => inputValue.length <= 255;

  //---------------------------------------------------------------------
  const checkIsCorrectFileSizeWhenNotEmpty = value => {
    //TODO uploadFile isEditForm = true => nullable()?
    if (isUndefined(value) || isPlainObject(value) || isNull(value)) {
      return true;
    } else {
      return value.size <= config.MAX_FILE_SIZE;
    }
  };

  const checkIsEmptyFile = value => {
    //uploadFile isEditForm = false
    //message: '';
    return !isPlainObject(value);
  };
  const checkIsCorrectFileSize = value => {
    //uploadFile isEditForm = false
    //message:resources.messages['tooLargeFileValidationError']
    return value.size <= config.MAX_FILE_SIZE;
  };

  //---------------------------------------------------------------------

  const onSubmit = async () => {
    if (!isEqual(documentInitialValues, values)) {
      setIsUploading(true);
      setSubmitting(true);
      setFileUpdatingId(values.id);
      notificationContext.add({
        type: 'DOCUMENT_UPLOADING_INIT_INFO',
        content: {}
      });
      try {
        if (isEditForm) {
          setIsUpdating(true);
          await DocumentService.editDocument(
            dataflowId,
            values.description,
            values.lang,
            values.uploadFile,
            values.isPublic,
            values.id
          );
          onUpload();
        } else {
          await DocumentService.uploadDocument(
            dataflowId,
            values.description,
            values.lang,
            values.uploadFile,
            values.isPublic
          );
          onUpload();
        }
      } catch (error) {
        if (isEditForm) {
          notificationContext.add({
            type: 'DOCUMENT_EDITING_ERROR',
            content: {}
          });
          setIsUpdating(false);
        } else {
          notificationContext.add({
            type: 'DOCUMENT_UPLOADING_ERROR',
            content: {}
          });
        }
        onUpload();
        setFileUpdatingId('');
      } finally {
        setIsUploading(false);
      }
    } else {
      setSubmitting(false);
    }
  };

  return (
    <form>
      <fieldset>
        <div className={`formField ${errors.description.hasErrors ? 'error' : ''}`}>
          <input
            id={'descriptionDocumentFileUpload'}
            ref={inputRef}
            maxLength={255}
            name="description"
            placeholder={resources.messages['fileDescription']}
            type="text"
            value={values.description}
            onChange={e => {
              e.persist();
              setValues(previousValues => {
                return { ...previousValues, description: e.target.value };
              });
            }}
          />
          <label htmlFor="descriptionDocumentFileUpload" className="srOnly">
            {resources.messages['description']}
          </label>
          {errors.description.message !== '' && <ErrorMessage message={errors.description.message} />}
        </div>

        <div className={`formField ${errors.lang.hasErrors ? 'error' : ''}`}>
          <select
            id="selectLanguage"
            name="lang"
            component="select"
            value={values.lang}
            onChange={e => {
              e.persist();
              setValues(previousValues => {
                return { ...previousValues, lang: e.target.value };
              });
            }}>
            <option value="">{resources.messages['selectLang']}</option>
            {sortBy(config.languages, ['name']).map(language => (
              <option key={language.code} value={language.code}>
                {language.name}
              </option>
            ))}
          </select>
          <label htmlFor="selectLanguage" className="srOnly">
            {resources.messages['selectLang']}
          </label>
        </div>
      </fieldset>

      <fieldset>
        <div className={`formField ${errors.uploadFile.hasErrors ? 'error' : ''}`}>
          <span>
            <input
              className="uploadFile"
              id="uploadFile"
              name="uploadFile"
              onChange={e => {
                const value = e.currentTarget;
                setValues(previousValues => {
                  return { ...previousValues, uploadFile: value.files[0] };
                });
              }}
              placeholder="file upload"
              type="file"
            />
            <label htmlFor="uploadFile" className="srOnly">
              {resources.messages['uploadDocument']}
            </label>
          </span>
          {errors.uploadFile.message !== '' && <ErrorMessage message={errors.uploadFile.message} />}
        </div>
      </fieldset>

      <fieldset>
        <div className={styles.checkboxIsPublic}>
          <input
            id="isPublic"
            type="checkbox"
            checked={values.isPublic}
            onChange={e => {
              e.persist();
              setValues(previousValues => {
                return { ...previousValues, isPublic: e.target.value };
              });
            }}
          />
          <label htmlFor="isPublic" style={{ display: 'block' }}>
            {resources.messages['documentUploadCheckboxIsPublic']}
          </label>
        </div>
      </fieldset>

      <fieldset>
        <div className={`${styles.buttonWrap} ui-dialog-buttonpane p-clearfix`}>
          <Button
            disabled={isSubmitting || isUploading}
            icon={!isUploading ? (isEditForm ? 'check' : 'add') : 'spinnerAnimate'}
            label={isEditForm ? resources.messages['save'] : resources.messages['upload']}
            onClick={onSubmit}
          />
          <Button
            className={`${styles.cancelButton} p-button-secondary button-right-aligned`}
            icon="cancel"
            label={resources.messages['cancel']}
            onClick={() => setIsUploadDialogVisible(false)}
          />
        </div>
      </fieldset>
    </form>
  );
};

export { DocumentFileUpload };
