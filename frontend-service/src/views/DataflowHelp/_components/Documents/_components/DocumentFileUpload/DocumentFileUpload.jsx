import { useContext, useEffect, useRef, useState } from 'react';

import sortBy from 'lodash/sortBy';

import styles from './DocumentFileUpload.module.scss';

import { config } from 'conf';

import { Button } from 'views/_components/Button';
import { Dropdown } from 'views/_components/Dropdown';
import { ErrorMessage } from 'views/_components/ErrorMessage';

import { DocumentService } from 'services/DocumentService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

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

  const [areAllInputsChecked, setAreAllInputsChecked] = useState(false);
  const [errors, setErrors] = useState({
    description: { message: '', hasErrors: false },
    lang: { message: '', hasErrors: false },
    uploadFile: { message: '', hasErrors: false }
  });
  const [isUploading, setIsUploading] = useState(false);
  const [isSubmitting, setSubmitting] = useState(false);
  const [inputs, setInputs] = useState(documentInitialValues);
  const [inputsChecked, setInputsChecked] = useState({
    description: false,
    lang: false,
    uploadFile: false
  });
  useEffect(() => {
    if (isUploadDialogVisible) inputRef.current.focus();
  }, [isUploadDialogVisible]);

  useEffect(() => {
    if (inputs?.isTouchedFileUpload) {
      checkInputForErrors('uploadFile');
    }
  }, [inputs]);

  useEffect(() => {
    if (!Object.values(inputsChecked).includes(false) || isEditForm) {
      setAreAllInputsChecked(true);
    }
  }, [inputsChecked, isEditForm]);

  useEffect(() => {
    isEditForm && setAreAllInputsChecked(true);
  }, [isEditForm]);

  const checkIsEmptyInput = inputValue => {
    return inputValue.trim() === '';
  };

  const checkIsCorrectLength = inputValue => inputValue.length <= 255;

  const checkIsEmptyFile = inputUpload => {
    return inputUpload.files.length === 0;
  };

  const checkExсeedsMaxFileSize = inputUpload => {
    if (inputUpload.files.length === 0) {
      return false;
    }
    return inputUpload.files[0].size > config.MAX_FILE_SIZE;
  };

  const checkInputForErrors = inputName => {
    let hasErrors = false;
    let message = '';
    const inputValue = inputName === 'lang' ? inputs[inputName].value : inputs[inputName];

    const inputUpload = document.querySelector('#uploadFile');

    if (inputName !== 'uploadFile' && checkIsEmptyInput(inputValue)) {
      message = '';
      hasErrors = true;
    } else if (inputName === 'description' && !checkIsCorrectLength(inputValue)) {
      message = resources.messages['documentDescriptionValidationMax'];
      hasErrors = true;
    } else if (inputName === 'uploadFile') {
      if (isEditForm && checkExсeedsMaxFileSize(inputUpload)) {
        message = resources.messages['tooLargeFileValidationError'];
        hasErrors = true;
      }
      if (!isEditForm) {
        if (checkIsEmptyFile(inputUpload)) {
          message = '';
          hasErrors = true;
        } else if (checkExсeedsMaxFileSize(inputUpload)) {
          message = resources.messages['tooLargeFileValidationError'];
          hasErrors = true;
        }
      }
    }

    setErrors(previousErrors => {
      return { ...previousErrors, [inputName]: { message, hasErrors } };
    });

    setInputsChecked(previousInputsChecked => {
      return { ...previousInputsChecked, [inputName]: true };
    });

    return hasErrors;
  };

  const onConfirm = async () => {
    checkInputForErrors('description');
    checkInputForErrors('lang');
    checkInputForErrors('uploadFile');

    if (
      areAllInputsChecked &&
      !errors.description.hasErrors &&
      !errors.lang.hasErrors &&
      !errors.uploadFile.hasErrors
    ) {
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
          await DocumentService.update(
            dataflowId,
            inputs.description,
            inputs.lang.value,
            inputs.uploadFile,
            inputs.isPublic,
            inputs.id
          );
          onUpload();
        } else {
          await DocumentService.upload(
            dataflowId,
            inputs.description,
            inputs.lang.value,
            inputs.uploadFile,
            inputs.isPublic
          );
          onUpload();
        }
      } catch (error) {
        console.error('DocumentFileUpload - onConfirm.', error);
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

  const getOptionTypes = () => {
    const template = [];
    config.languages.forEach(language => {
      template.push({ label: language.name, value: language.code });
    });
    return sortBy(template, 'type');
  };

  return (
    <form onSubmit={e => e.preventDefault()}>
      <fieldset>
        <div className={`formField ${errors.description.hasErrors ? 'error' : ''}`}>
          <input
            id={'descriptionDocumentFileUpload'}
            maxLength={255}
            name={resources.messages['description']}
            onBlur={() => checkInputForErrors('description')}
            onChange={e => {
              e.persist();
              setInputs(previousValues => {
                return { ...previousValues, description: e.target.value };
              });
            }}
            onFocus={() =>
              setErrors(previousErrors => {
                return { ...previousErrors, description: { message: '', hasErrors: false } };
              })
            }
            onKeyPress={e => {
              if (!checkInputForErrors('description') && e.key === 'Enter') onConfirm();
            }}
            placeholder={resources.messages['fileDescription']}
            ref={inputRef}
            type="text"
            value={inputs.description}
          />
          <label className="srOnly" htmlFor="descriptionDocumentFileUpload">
            {resources.messages['description']}
          </label>
          {errors.description.message !== '' && <ErrorMessage message={errors.description.message} />}
        </div>

        <div className={`formField ${errors.lang.hasErrors ? 'error' : ''}`}>
          <Dropdown
            appendTo={document.body}
            className={styles.dropdownWrapper}
            id="selectLanguage"
            name="lang"
            onChange={e => {
              setInputs(previousValues => {
                return { ...previousValues, lang: e.target.value };
              });
              setErrors(previousErrors => {
                return { ...previousErrors, lang: { message: '', hasErrors: false } };
              });
              setInputsChecked(previousInputsChecked => {
                return { ...previousInputsChecked, lang: true };
              });
            }}
            onKeyPress={e => {
              if (e.which === 13 && !checkInputForErrors('lang')) onConfirm();
            }}
            optionLabel="label"
            optionValue="value"
            options={getOptionTypes()}
            placeholder={resources.messages['selectLang']}
            value={inputs.lang}
          />
          <label className="srOnly" htmlFor="selectLanguage">
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
              onBlur={() => checkInputForErrors('uploadFile')}
              onChange={e => {
                const eventTarget = e.currentTarget;
                setInputs(previousValues => {
                  return { ...previousValues, uploadFile: eventTarget.files[0], isTouchedFileUpload: true };
                });
              }}
              onKeyPress={e => {
                if (e.key === 'Enter' && !checkInputForErrors('uploadFile')) onConfirm();
              }}
              placeholder="file upload"
              type="file"
            />
            <label className="srOnly" htmlFor="uploadFile">
              {resources.messages['uploadDocument']}
            </label>
          </span>
          {errors.uploadFile.message !== '' && <ErrorMessage message={errors.uploadFile.message} />}
        </div>
      </fieldset>

      <fieldset>
        <div className={styles.checkboxIsPublic}>
          <input
            checked={inputs.isPublic}
            id="isPublic"
            onChange={() => {
              setInputs(previousValues => {
                return { ...previousValues, isPublic: !previousValues.isPublic };
              });
            }}
            type="checkbox"
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
