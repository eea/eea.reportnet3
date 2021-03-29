import React, { useContext, useEffect, useRef, useState } from 'react';

import sortBy from 'lodash/sortBy';

import styles from './DocumentFileUpload.module.scss';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { ErrorMessage } from 'ui/views/_components/ErrorMessage';

import { DocumentService } from 'core/services/Document';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import isEmpty from 'lodash/isEmpty';
import { query } from 'esri-leaflet';

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
  const [inputs, setInputs] = useState(documentInitialValues);

  useEffect(() => {
    if (isUploadDialogVisible) inputRef.current.focus();
  }, [isUploadDialogVisible]);

  useEffect(() => {
    if (inputs?.isTouchedFileUpload) {
      checkInputForErrors('uploadFile');
    }
  }, [inputs]);

  const checkIsEmptyInput = inputValue => {
    return inputValue.trim() === '';
  };

  const checkIsCorrectLength = inputValue => inputValue.length <= 255;

  const checkIsEmptyFile = uploadedDocument => {
    return uploadedDocument.files.length === 0;
  };

  const checkExсeedsMaxFileSize = uploadedDocument => {
    if (uploadedDocument.files.length === 0) {
      return false;
    }
    return uploadedDocument.files[0].size > config.MAX_FILE_SIZE;
  };

  const checkInputForErrors = inputName => {
    let hasErrors = false;
    let message = '';
    const inputValue = inputs[inputName];

    const uploadedDocument = document.querySelector('#uploadFile');

    if (inputName !== 'uploadFile' && checkIsEmptyInput(inputValue)) {
      message = '';
      hasErrors = true;
    } else if (inputName === 'description' && !checkIsCorrectLength(inputValue)) {
      message = resources.messages['documentDescriptionValidationMax'];
      hasErrors = true;
    } else if (inputName === 'uploadFile') {
      if (isEditForm && checkExсeedsMaxFileSize(uploadedDocument)) {
        message = resources.messages['tooLargeFileValidationError'];
        hasErrors = true;
      }
      if (!isEditForm) {
        if (checkIsEmptyFile(uploadedDocument)) {
          message = '';
          hasErrors = true;
        } else if (checkExсeedsMaxFileSize(uploadedDocument)) {
          message = resources.messages['tooLargeFileValidationError'];
          hasErrors = true;
        }
      }
    }

    setErrors(previousErrors => {
      return { ...previousErrors, [inputName]: { message, hasErrors } };
    });

    return hasErrors;
  };

  const onConfirm = async () => {
    checkInputForErrors('description');
    checkInputForErrors('lang');
    checkInputForErrors('uploadFile');

    if (!errors.description.hasErrors && !errors.lang.hasErrors && !errors.uploadFile.hasErrors) {
      setIsUploading(true);
      setSubmitting(true);
      setFileUpdatingId(inputs.id);
      notificationContext.add({
        type: 'DOCUMENT_UPLOADING_INIT_INFO',
        content: {}
      });
      try {
        if (isEditForm) {
          setIsUpdating(true);
          await DocumentService.editDocument(
            dataflowId,
            inputs.description,
            inputs.lang,
            inputs.uploadFile,
            inputs.isPublic,
            inputs.id
          );
          onUpload();
        } else {
          await DocumentService.uploadDocument(
            dataflowId,
            inputs.description,
            inputs.lang,
            inputs.uploadFile,
            inputs.isPublic
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
    <form onSubmit={e => e.preventDefault()}>
      <fieldset>
        <div className={`formField ${errors.description.hasErrors ? 'error' : ''}`}>
          <input
            id={'descriptionDocumentFileUpload'}
            ref={inputRef}
            maxLength={255}
            name="description"
            placeholder={resources.messages['fileDescription']}
            type="text"
            value={inputs.description}
            onBlur={() => checkInputForErrors('description')}
            onChange={e => {
              e.persist();
              setInputs(previousValues => {
                return { ...previousValues, description: e.target.value };
              });
            }}
            onKeyPress={e => {
              if (e.key === 'Enter' && !checkInputForErrors('description')) {
                onConfirm();
              }
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
            multiple={false}
            value={inputs.lang}
            onBlur={() => checkInputForErrors('lang')}
            onChange={e => {
              e.persist();
              setInputs(previousValues => {
                return { ...previousValues, lang: e.target.value };
              });
            }}
            onKeyPress={e => {
              if (e.key === 'Enter' && !checkInputForErrors('lang')) onConfirm();
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
              onKeyPress={e => {
                if (e.key === 'Enter' && !checkInputForErrors('uploadFile')) onConfirm();
              }}
              onBlur={() => checkInputForErrors('uploadFile')}
              onChange={e => {
                const eventTarget = e.currentTarget;
                setInputs(previousValues => {
                  return { ...previousValues, uploadFile: eventTarget.files[0], isTouchedFileUpload: true };
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
            checked={inputs.isPublic}
            onChange={() => {
              setInputs(previousValues => {
                return { ...previousValues, isPublic: !previousValues.isPublic };
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
            onClick={() => onConfirm()}
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
