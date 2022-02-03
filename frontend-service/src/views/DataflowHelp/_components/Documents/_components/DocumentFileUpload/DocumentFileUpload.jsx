import { useContext, useEffect, useImperativeHandle, useRef, useState } from 'react';

import sortBy from 'lodash/sortBy';
import isNil from 'lodash/isNil';

import styles from './DocumentFileUpload.module.scss';

import { config } from 'conf';

import { CharacterCounter } from 'views/_components/CharacterCounter';
import { Checkbox } from 'views/_components/Checkbox';
import { Dropdown } from 'views/_components/Dropdown';
import { InputFile } from 'views/_components/InputFile';

import { DocumentService } from 'services/DocumentService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const DocumentFileUpload = ({
  dataflowId,
  documentInitialValues,
  footerRef,
  isEditForm = false,
  isUploadDialogVisible,
  onUpload,
  setFileUpdatingId,
  setIsUpdating,
  setIsUploading,
  setSubmitting
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const inputRef = useRef(null);
  const fileRef = useRef(null);

  const [areAllInputsChecked, setAreAllInputsChecked] = useState(false);
  const [errors, setErrors] = useState({
    description: { message: '', hasErrors: false },
    lang: { message: '', hasErrors: false },
    uploadFile: { message: '', hasErrors: false }
  });

  const [inputs, setInputs] = useState(documentInitialValues);
  const [inputsChecked, setInputsChecked] = useState({
    description: false,
    lang: false,
    uploadFile: false
  });

  const onClearFile = () => {
    setInputs(previousValues => {
      return { ...previousValues, uploadFile: {}, isTouchedFileUpload: false };
    });
  };

  useEffect(() => {
    if (isUploadDialogVisible) {
      inputRef.current.focus();
    }
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
    if (isEditForm) {
      setAreAllInputsChecked(true);
    }
  }, [isEditForm]);

  const checkIsEmptyInput = inputValue => inputValue.trim() === '';

  const checkIsCorrectLength = inputValue => inputValue.length <= config.INPUT_MAX_LENGTH;

  const checkIsEmptyFile = inputUpload => inputUpload.files.length === 0;

  const checkExсeedsMaxFileSize = inputUpload =>
    inputUpload.files.length > 0 && inputUpload.files[0].size > config.MAX_FILE_SIZE;

  const checkInputForErrors = inputName => {
    let hasErrors = false;
    let message = '';

    const inputValue = inputName === 'lang' ? inputs[inputName].value : inputs[inputName];
    const inputUpload = fileRef.current;

    if (inputName !== 'uploadFile' && checkIsEmptyInput(inputValue)) {
      message = '';
      hasErrors = true;
    } else if (inputName === 'description' && !checkIsCorrectLength(inputValue)) {
      message = resourcesContext.messages['documentDescriptionValidationMax'];
      hasErrors = true;
    } else if (inputName === 'uploadFile') {
      if (checkExсeedsMaxFileSize(inputUpload)) {
        message = resourcesContext.messages['tooLargeFileValidationError'];
        hasErrors = true;
      } else if (!isEditForm && checkIsEmptyFile(inputUpload)) {
        message = '';
        hasErrors = true;
      }
    }

    setErrors(previousErrors => ({ ...previousErrors, [inputName]: { message, hasErrors } }));
    setInputsChecked(previousInputsChecked => ({ ...previousInputsChecked, [inputName]: true }));

    return hasErrors;
  };

  useImperativeHandle(footerRef, () => ({ onConfirm }));

  const onConfirm = async () => {
    const descHasError = checkInputForErrors('description');
    const langHasError = checkInputForErrors('lang');
    const fileHasError = checkInputForErrors('uploadFile');

    if (areAllInputsChecked && !descHasError && !langHasError && !fileHasError) {
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
          notificationContext.add({ type: 'DOCUMENT_EDITING_ERROR', content: {} }, true);
          setIsUpdating(false);
        } else {
          notificationContext.add({ type: 'DOCUMENT_UPLOADING_ERROR', content: {} }, true);
        }
        onUpload();
        setFileUpdatingId('');
      } finally {
        setIsUploading(false);
        setSubmitting(false);
      }
    }
  };

  const getOptionTypes = () => {
    const template = config.languages.map(language => ({ label: language.name, value: language.code }));
    return sortBy(template, 'type');
  };

  const onFileUpload = async e => {
    const eventTarget = e.currentTarget;
    if (!isNil(eventTarget.files[0])) {
      setInputs(previousValues => {
        return { ...previousValues, uploadFile: eventTarget.files[0], isTouchedFileUpload: true };
      });
    }
  };

  return (
    <form onSubmit={e => e.preventDefault()}>
      <fieldset>
        <div className={`formField ${errors.description.hasErrors ? 'error' : ''}`}>
          <input
            id="descriptionDocumentFileUpload"
            maxLength={config.INPUT_MAX_LENGTH}
            name={resourcesContext.messages['description']}
            onBlur={() => checkInputForErrors('description')}
            onChange={e => {
              e.persist();
              setInputs(previousValues => ({ ...previousValues, description: e.target.value }));
            }}
            onFocus={() =>
              setErrors(previousErrors => ({ ...previousErrors, description: { message: '', hasErrors: false } }))
            }
            onKeyPress={e => {
              if (!checkInputForErrors('description') && e.key === 'Enter') {
                onConfirm();
              }
            }}
            placeholder={resourcesContext.messages['fileDescription']}
            ref={inputRef}
            type="text"
            value={inputs.description}
          />
          <CharacterCounter currentLength={inputs.description.length} maxLength={config.INPUT_MAX_LENGTH} />
          <label className="srOnly" htmlFor="descriptionDocumentFileUpload">
            {resourcesContext.messages['description']}
          </label>
        </div>

        <div className={`formField ${errors.lang.hasErrors ? 'error' : ''}`}>
          <Dropdown
            appendTo={document.body}
            className={styles.dropdownWrapper}
            id="selectLanguage"
            name="lang"
            onChange={e => {
              setInputs(previousValues => ({ ...previousValues, lang: e.target.value }));
              setErrors(previousErrors => ({ ...previousErrors, lang: { message: '', hasErrors: false } }));
              setInputsChecked(previousInputsChecked => ({ ...previousInputsChecked, lang: true }));
            }}
            onKeyPress={e => {
              if (e.which === 13 && !checkInputForErrors('lang')) {
                onConfirm();
              }
            }}
            optionLabel="label"
            options={getOptionTypes()}
            optionValue="value"
            placeholder={resourcesContext.messages['selectLang']}
            value={inputs.lang}
          />
          <label className="srOnly" htmlFor="selectLanguage">
            {resourcesContext.messages['selectLang']}
          </label>
        </div>
      </fieldset>

      <fieldset>
        <div className={`formField ${errors.uploadFile.hasErrors ? 'error' : ''}`}>
          <span>
            <InputFile
              accept="*"
              buttonTextNoFile={resourcesContext.messages['inputFileButtonNotSelected']}
              buttonTextWithFile={resourcesContext.messages['inputFileButtonSelected']}
              errorMessage={errors.uploadFile.message}
              fileRef={fileRef}
              hasError={errors.uploadFile.hasErrors}
              onChange={onFileUpload}
              onClearFile={onClearFile}
              onKeyPress={e => {
                if (e.key === 'Enter' && !checkInputForErrors('uploadFile')) {
                  onConfirm();
                }
              }}
            />
            <label className="srOnly" htmlFor="uploadFile">
              {resourcesContext.messages['uploadDocument']}
            </label>
          </span>
        </div>
      </fieldset>

      <fieldset>
        <div className={styles.checkboxIsPublic}>
          <Checkbox
            ariaLabelledBy="isPublic"
            checked={inputs.isPublic}
            id="isPublic"
            inputId="isPublic"
            onChange={() => {
              setInputs(previousValues => ({ ...previousValues, isPublic: !previousValues.isPublic }));
            }}
            role="checkbox"
          />
          <label
            htmlFor="isPublic"
            onClick={() => {
              setInputs(previousValues => ({ ...previousValues, isPublic: !previousValues.isPublic }));
            }}>
            {resourcesContext.messages['checkboxIsPublic']}
          </label>
        </div>
      </fieldset>
    </form>
  );
};
