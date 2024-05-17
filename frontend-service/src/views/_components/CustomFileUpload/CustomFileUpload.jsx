import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';

import DomHandler from 'views/_functions/PrimeReact/DomHandler';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import classNames from 'classnames';

import styles from './CustomFileUpload.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { Dialog } from 'views/_components/Dialog';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { Messages } from 'primereact/messages';
import { ProgressBar } from 'primereact/progressbar';
import { LocalUserStorageUtils } from 'services/_utils/LocalUserStorageUtils';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { customFileUploadReducer } from './_functions/Reducers/customFileUploadReducer';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const CustomFileUpload = ({
  accept = undefined,
  auto = false,
  bigData = false,
  cancelLabel = 'Reset',
  chooseLabel = 'Choose',
  className = null,
  dataflowId,
  datasetId,
  dialogClassName = null,
  dialogHeader = null,
  dialogOnHide = null,
  dialogVisible = null,
  disabled = false,
  draggedFiles = null,
  fileLimit = 1,
  id = null,
  infoTooltip = '',
  invalidExtensionMessage = '',
  invalidFileSizeMessageDetail = 'maximum upload size is {0}.',
  invalidFileSizeMessageSummary = '{0}= Invalid file size, ',
  invalidNumberOfFilesMessageSummary = 'You can only upload {0} {1}.',
  isDialog = false,
  isImportDatasetDesignerSchema = false,
  isImportLeadReportersDialog = false,
  maxFileSize = null,
  mode = 'advanced',
  multiple = false,
  name = null,
  onBeforeSend = null,
  onBeforeUpload = null,
  onClear = null,
  onError = null,
  onProgress = null,
  onSelect = null,
  onUpload = null,
  onValidateFile = null,
  operation = 'POST',
  previewWidth = 50,
  replaceCheck = false,
  replaceCheckLabel = 'Replace data',
  replaceCheckLabelMessage = '',
  replaceCheckDisabled = false,
  s3Check = false,
  s3CheckLabel = 'S3',
  style = null,
  tableSchemaId = null,
  uploadLabel = 'Upload',
  url = null,
  withCredentials = false
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [state, dispatch] = useReducer(customFileUploadReducer, {
    files: [],
    isUploadClicked: false,
    isUploading: false,
    isValid: true,
    msgs: [],
    replace: false,
    uploadWithS3: false
  });

  const [isValidating, setIsValidating] = useState(false);
  const [isCreateDatasetSchemaConfirmDialogVisible, setIsCreateDatasetSchemaConfirmDialogVisible] = useState(false);
  const [jobId, setJobId] = useState();
  const [presignedUrl, setPresignedUrl] = useState();

  const _files = useRef([]);
  const content = useRef(null);
  const fileInput = useRef(null);
  const messagesUI = useRef(null);

  useEffect(() => {
    if (hasFiles() && auto) upload();
  }, [state]);

  useEffect(() => {
    if (presignedUrl) {
      uploadToS3();
      importS3ToDlh();
    }
  }, [presignedUrl]);

  useEffect(() => {
    if (state.isUploadClicked) upload();
  }, [state.isUploadClicked]);

  useEffect(() => {
    if (state.progress === 100 && bigData && !isImportLeadReportersDialog) {
      const timer = setTimeout(() => {
        onUpload({ files: state.files });
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [state.progress]);

  useEffect(() => {
    if (!isNil(draggedFiles)) {
      if (draggedFiles.length > fileLimit) {
        messagesUI.current.show({
          life: config.IMPORT_ERROR_LIFETIME,
          severity: 'error',
          summary: invalidNumberOfFilesMessageSummary
            .replace('{0}', fileLimit)
            .replace(
              '{1}',
              fileLimit > 1
                ? resourcesContext.messages['files'].toLowerCase()
                : resourcesContext.messages['file'].toLowerCase()
            )
        });
      } else {
        dispatch({
          type: 'UPLOAD_PROPERTY',
          payload: { files: [...draggedFiles] }
        });
      }
    }
  }, [draggedFiles]);

  const checkValidExtension = file => {
    const acceptedExtensions = accept.toLowerCase().split(/,\s*/);

    if (file) {
      const extension = file.name.substring(file.name.lastIndexOf('.') + 1, file.name.length) || file.name;
      return acceptedExtensions.includes('*') || acceptedExtensions.includes(`.${extension.toLowerCase()}`);
    }

    if (hasFiles()) {
      const selectedExtension = state.files.map(
        file => file.name.substring(file.name.lastIndexOf('.') + 1, file.name.length) || file.name
      );

      return (
        !acceptedExtensions.includes('*') &&
        !selectedExtension.some(ext => acceptedExtensions.includes(`.${ext.toLowerCase()}`))
      );
    }
    return false;
  };

  const clearInputElement = () => {
    if (fileInput.current) {
      fileInput.current.value = '';
      if (mode === 'basic') {
        fileInput.current.style.display = 'inline';
      }
    }
  };

  const hasFiles = () => state.files && state.files.length > 0;

  const isImage = file => /^image\//.test(file.type);

  const formatSize = bytes => {
    if (bytes === 0) {
      return '0 B';
    }

    const k = 1024;
    const dm = 2;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  };

  const onFileSelect = event => {
    dispatch({ type: 'UPLOAD_PROPERTY', payload: { msgs: [] } });
    _files.current = state.files || [];
    let cFiles = event.dataTransfer ? event.dataTransfer.files : event.target.files;

    if (fileLimit === 1) {
      let file = cFiles[0];
      if (!isFileSelected(file)) {
        if (validate(file)) {
          if (isImage(file)) {
            file.objectURL = window.URL.createObjectURL(file);
          }
          _files.current = [];
          _files.current.push(file);
        }
      }
    } else {
      for (let i = 0; i < cFiles.length; i++) {
        let file = cFiles[i];

        if (!isFileSelected(file)) {
          if (validate(file)) {
            if (isImage(file)) {
              file.objectURL = window.URL.createObjectURL(file);
            }
            _files.current.push(file);
          }
        }
      }
    }
    dispatch({ type: 'UPLOAD_PROPERTY', payload: { files: _files.current } });

    if (onSelect) {
      onSelect({ originalEvent: event, files: _files.current });
    }

    clearInputElement();

    if (mode === 'basic') {
      fileInput.current.style.display = 'none';
    }
  };

  const isFileSelected = file => {
    for (let sFile of state.files) {
      if (sFile.name + sFile.type + sFile.size === file.name + file.type + file.size) return true;
    }
    return false;
  };

  const validate = file => {
    if (maxFileSize && file.size > maxFileSize) {
      messagesUI.current.show({
        life: config.IMPORT_ERROR_LIFETIME,
        severity: 'error',
        summary: invalidFileSizeMessageSummary.replace('{0}', file.name),
        detail: invalidFileSizeMessageDetail.replace('{0}', formatSize(maxFileSize))
      });

      return false;
    }

    if (accept) {
      if (!checkValidExtension(file)) {
        dispatch({ type: 'UPLOAD_PROPERTY', payload: { isValid: false } });
        return false;
      }
    }

    if (onValidateFile) {
      setIsValidating(true);
      const validations = onValidateFile(file);
      if (!isEmpty(validations)) {
        let hasErrors = false;
        validations.forEach(validation => {
          if (validation.severity === 'error') {
            hasErrors = true;
          }
          messagesUI.current.show({
            life: config.IMPORT_ERROR_LIFETIME,
            severity: validation.severity,
            summary: validation.summary,
            detail: validation.detail
          });
        });
        setIsValidating(false);
        return hasErrors;
      }
      setIsValidating(false);
    }

    dispatch({ type: 'UPLOAD_PROPERTY', payload: { isValid: true } });
    return true;
  };

  const upload = async () => {
    dispatch({ type: 'UPLOAD_PROPERTY', payload: { msgs: [], isUploading: true } });
    let xhr = new XMLHttpRequest();
    let formData = new FormData();

    if (onBeforeUpload) {
      onBeforeUpload({ xhr: xhr, formData: formData }, state.files);
    }

    for (let file of state.files) {
      formData.append(name, file, file.name);
    }

    xhr.upload.addEventListener('progress', event => {
      if (event.lengthComputable) {
        dispatch({ type: 'UPLOAD_PROPERTY', payload: { progress: Math.round((event.loaded * 100) / event.total) } });
      }

      if (onProgress) {
        onProgress({ originalEvent: event, progress: state.progress });
      }
    });

    xhr.onreadystatechange = () => {
      if (xhr.readyState === 4) {
        dispatch({ type: 'UPLOAD_PROPERTY', payload: { progress: 0 } });

        if (xhr.status >= 200 && xhr.status < 300) {
          if (onUpload && !(bigData && !isImportLeadReportersDialog)) {
            onUpload({ xhr: xhr, files: _files.current });
          }
        } else {
          if (onError) {
            onError({ xhr: xhr, files: _files.current });
          }
        }

        clear();
      }
    };

    let nUrl = url;

    if (replaceCheck) {
      nUrl += nUrl.indexOf('?') !== -1 ? '&' : '?';
      nUrl += 'replace=' + state.replace;
    }

    xhr.open(operation, nUrl, true);
    const tokens = LocalUserStorageUtils.getTokens();
    xhr.setRequestHeader('Authorization', `Bearer ${tokens.accessToken}`);

    if (onBeforeSend) {
      onBeforeSend({ xhr: xhr, formData: formData });
    }

    xhr.withCredentials = withCredentials;

    xhr.send(formData);

    dispatch({ type: 'UPLOAD_PROPERTY', payload: { isUploadClicked: false } });
  };

  const uploadToS3 = async () => {
    dispatch({ type: 'UPLOAD_PROPERTY', payload: { msgs: [], isUploading: true } });
    try {
      await fetch(presignedUrl, {
        method: 'PUT',
        body: state.files[0]
      });

      onUpload({ files: state.files });

      dispatch({ type: 'UPLOAD_PROPERTY', payload: { isUploadClicked: false } });
    } catch (error) {
      console.error('CustomFileUpload - uploadToS3.', error);
      notificationContext.add({ type: 'UPLOAD_TO_S3_ERROR' }, true);
      dispatch({ type: 'UPLOAD_PROPERTY', payload: { isUploadClicked: false } });
    }
  };

  const importS3ToDlh = async () => {
    try {
      await DatasetService.importFileWithS3({
        dataflowId,
        datasetId,
        delimiter: encodeURIComponent(config.IMPORT_FILE_DELIMITER),
        jobId,
        tableSchemaId
      });
    } catch (error) {
      console.error('CustomFileUpload - importS3ToDlh.', error);
      notificationContext.add({ type: 'IMPORT_S3_TO_DLH_ERROR' }, true);
    }
  };

  const clear = () => {
    dispatch({ type: 'UPLOAD_PROPERTY', payload: { files: [], isUploading: false } });
    if (onClear) {
      onClear();
    }
    clearInputElement();
  };

  const onFocus = event => DomHandler.addClass(event.currentTarget.parentElement, 'p-focus');

  const onBlur = event => DomHandler.removeClass(event.currentTarget.parentElement, 'p-focus');

  const onDragEnter = event => {
    if (!disabled) {
      event.stopPropagation();
      event.preventDefault();
    }
  };

  const onDragOver = event => {
    if (!disabled) {
      DomHandler.addClass(content.current, 'p-fileupload-highlight');
      event.stopPropagation();
      event.preventDefault();
    }
  };

  const onDragLeave = () => {
    if (!disabled) {
      DomHandler.removeClass(content.current, 'p-fileupload-highlight');
    }
  };

  const onDrop = event => {
    if (!disabled) {
      DomHandler.removeClass(content.current, 'p-fileupload-highlight');
      event.stopPropagation();
      event.preventDefault();

      let files = event.dataTransfer ? event.dataTransfer.files : event.target.files;

      if (files && files.length > fileLimit) {
        messagesUI.current.show({
          life: config.IMPORT_ERROR_LIFETIME,
          severity: 'error',
          summary: invalidNumberOfFilesMessageSummary
            .replace('{0}', fileLimit)
            .replace(
              '{1}',
              fileLimit > 1
                ? resourcesContext.messages['files'].toLowerCase()
                : resourcesContext.messages['file'].toLowerCase()
            )
        });
      } else {
        onFileSelect(event);
      }
    }
  };

  const onSimpleUploaderClick = () => {
    if (hasFiles()) upload();
  };

  const remove = index => {
    clearInputElement();
    let currentFiles = [...state.files];
    currentFiles.splice(index, 1);
    dispatch({ type: 'UPLOAD_PROPERTY', payload: { files: currentFiles } });
  };

  const renderChooseButton = () => {
    let className = classNames('p-button p-button-primary p-fileupload-choose p-component p-button-text-icon-left');

    return (
      <span className={styles.chooseButton}>
        <input
          accept={accept}
          className={styles.chooseInput}
          disabled={disabled}
          id="file"
          multiple={multiple}
          onBlur={onBlur}
          onChange={onFileSelect}
          onFocus={onFocus}
          ref={fileInput}
          type="file"
        />
        <label className={className} htmlFor="file">
          <span className="pi pi-fw pi-plus" />
          <span>{chooseLabel}</span>
        </label>

        <Button
          className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
          icon="infoCircle"
          tooltip={infoTooltip}
          tooltipOptions={{ position: 'top' }}
        />
      </span>
    );
  };

  const onGetPresignedUrl = async () => {
    const fileName = state?.files[0].name;
    const data = await DatasetService.getPresignedUrl({ datasetId, dataflowId, fileName });
    setJobId(data?.jobId);
    setPresignedUrl(data?.presignedUrl);
  };

  const renderFiles = () => {
    return (
      <div className="p-fileupload-files">
        {state.files.map((file, index) => {
          let preview = isImage(file) ? (
            <div>
              <img alt={file.name} role="presentation" src={file.objectURL} width={previewWidth} />
            </div>
          ) : (
            <div>
              <FontAwesomeIcon
                className={styles.iconPreview}
                icon={AwesomeIcons(file.name.split('.')[1])}
                role="presentation"
              />
            </div>
          );
          let fileName = (
            <div>
              <label>{file.name}</label>
            </div>
          );
          let size = (
            <div className={styles.sizeDiv}>
              <label>{formatSize(file.size)}</label>
            </div>
          );
          let removeButton = (
            <div>
              <Button icon="cancel" onClick={() => remove(index)} type="button" />
            </div>
          );

          return (
            <div className={styles['p-fileupload-row']} key={file.name + file.type + file.size}>
              {preview}
              {fileName}
              {size}
              {removeButton}
            </div>
          );
        })}
      </div>
    );
  };

  const renderReplaceCheck = () => {
    return (
      <div className={styles.replaceCheckboxWrapper}>
        <Checkbox
          checked={state.replace}
          disabled={replaceCheckDisabled}
          id="replaceCheckbox"
          inputId="replaceCheckbox"
          onChange={() => dispatch({ type: 'UPLOAD_PROPERTY', payload: { replace: !state.replace } })}
          role="checkbox"
        />
        <label htmlFor="replaceCheckbox">
          <span
            className={replaceCheckDisabled ? styles.replaceCheckboxSpanDisabled : null}
            onClick={() =>
              !replaceCheckDisabled && dispatch({ type: 'UPLOAD_PROPERTY', payload: { replace: !state.replace } })
            }>
            {replaceCheckLabel}
          </span>
        </label>
        {replaceCheckDisabled && (
          <Button
            className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
            icon="infoCircle"
            tooltip={replaceCheckLabelMessage}
            tooltipOptions={{ position: 'top' }}
          />
        )}
      </div>
    );
  };
  const renderS3Check = () => {
    return (
      <div className={styles.s3CheckboxWrapper}>
        <Checkbox
          checked={state.uploadWithS3}
          id="s3Checkbox"
          inputId="s3Checkbox"
          onChange={() => dispatch({ type: 'UPLOAD_PROPERTY', payload: { uploadWithS3: !state.uploadWithS3 } })}
          role="checkbox"
        />
        <label htmlFor="s3Checkbox">
          <span onClick={() => dispatch({ type: 'UPLOAD_PROPERTY', payload: { uploadWithS3: !state.uploadWithS3 } })}>
            {s3CheckLabel}
          </span>
        </label>
      </div>
    );
  };

  const renderAdvanced = () => {
    const cClassName = classNames('p-fileupload p-component', className);
    let filesList, progressBar;

    if (hasFiles()) {
      filesList = renderFiles();
      progressBar = <ProgressBar showValue={false} value={state.progress} />;
    }

    return (
      <Fragment>
        <div className={cClassName} id={id} style={style}>
          <div className="p-fileupload-buttonbar">{renderChooseButton()}</div>
          <div
            className="p-fileupload-content"
            onDragEnter={onDragEnter}
            onDragLeave={onDragLeave}
            onDragOver={onDragOver}
            onDrop={onDrop}
            ref={content}>
            {progressBar}
            <Messages ref={messagesUI} />
            {filesList}
          </div>
          {replaceCheck && renderReplaceCheck()}
          {bigData && s3Check && renderS3Check()}
        </div>
        <p className={`${styles.invalidExtensionMsg} ${state.isValid ? styles.isValid : undefined}`}>
          {invalidExtensionMessage}
        </p>
      </Fragment>
    );
  };

  const renderAdvancedFooter = () => {
    let uploadButton;
    let cancelButton;

    const getButtonLabel = () => {
      if (isValidating) {
        return resourcesContext.messages['validating'];
      } else if (state.isUploading) {
        return resourcesContext.messages['uploading'];
      } else {
        return uploadLabel;
      }
    };

    if (!auto) {
      uploadButton = (
        <span data-for="invalidExtension" data-tip>
          <Button
            disabled={disabled || !hasFiles() || checkValidExtension() || state.isUploading || isValidating}
            icon={state.isUploading || isValidating ? 'spinnerAnimate' : 'upload'}
            label={getButtonLabel()}
            onClick={() => {
              if (isImportDatasetDesignerSchema) {
                setIsCreateDatasetSchemaConfirmDialogVisible(true);
              } else {
                if (bigData && state.uploadWithS3) {
                  onGetPresignedUrl();
                } else {
                  upload();
                }
              }
            }}
          />
        </span>
      );
      cancelButton = (
        <Button
          className="p-button-secondary"
          disabled={disabled || !hasFiles()}
          icon="undo"
          label={cancelLabel}
          onClick={clear}
        />
      );
    }

    if (hasFiles()) {
      // eslint-disable-next-line no-native-reassign
      FileList = renderFiles();
    }

    return (
      <Fragment>
        <div className={styles.dialogFooter}>
          <div className={styles.secondaryZone}>{cancelButton}</div>
          <div className={styles.primaryZone}>
            {uploadButton}
            <Button
              className="p-button-secondary p-button-animated-blink p-button-right-aligned"
              icon="cancel"
              label={resourcesContext.messages['close']}
              onClick={() => dialogOnHide()}
            />
          </div>
        </div>

        {isCreateDatasetSchemaConfirmDialogVisible && (
          <ConfirmDialog
            header={resourcesContext.messages['confirmNewDatasetSchemaCreationHeader']}
            labelCancel={resourcesContext.messages['no']}
            labelConfirm={resourcesContext.messages['yes']}
            onConfirm={() => {
              dispatch({ type: 'UPLOAD_PROPERTY', payload: { isUploadClicked: true } });
              setIsCreateDatasetSchemaConfirmDialogVisible(false);
            }}
            onHide={() => {
              setIsCreateDatasetSchemaConfirmDialogVisible(false);
            }}
            visible={isCreateDatasetSchemaConfirmDialogVisible}>
            <p
              dangerouslySetInnerHTML={{
                __html: TextUtils.parseText(resourcesContext.messages['confirmNewDatasetSchemaCreationBody'])
              }}></p>
          </ConfirmDialog>
        )}
      </Fragment>
    );
  };

  const renderBasic = () => {
    const buttonClassName = classNames('p-button p-fileupload-choose p-component p-button-text-icon-left', {
      'p-fileupload-choose-selected': hasFiles()
    });
    const iconClassName = classNames('p-button-icon-left pi', {
      'pi-plus': !hasFiles() || auto,
      'pi-upload': hasFiles() && !auto
    });

    return (
      <span className={buttonClassName} onMouseUp={onSimpleUploaderClick}>
        <span className={iconClassName} />
        <span className="p-button-text p-clickable">
          {auto ? chooseLabel : hasFiles() ? state.files[0].name : chooseLabel}
        </span>
        <input
          accept={accept}
          disabled={disabled}
          multiple={multiple}
          onBlur={onBlur}
          onChange={onFileSelect}
          onFocus={onFocus}
          ref={fileInput}
          type="file"
        />
      </span>
    );
  };

  if (isDialog) {
    return (
      <Dialog
        className={dialogClassName || styles.dialogDefaultCustomFileUpload}
        footer={renderAdvancedFooter()}
        header={dialogHeader}
        onHide={dialogOnHide}
        style={{ width: '35vw' }}
        visible={dialogVisible}>
        {mode === 'advanced' && renderAdvanced()}
        {mode === 'basic' && renderBasic()}
      </Dialog>
    );
  } else {
    if (mode === 'advanced') return renderAdvanced();
    if (mode === 'basic') return renderBasic();
  }
};
