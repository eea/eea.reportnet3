import React, { Fragment, useEffect, useContext, useState, useRef } from 'react';

import PropTypes from 'prop-types';
import classNames from 'classnames';

import styles from './CustomFileUpload.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox';
import { Dialog } from 'ui/views/_components/Dialog';

import { Messages } from 'primereact/messages';
import { ProgressBar } from 'primereact/progressbar';
import { userStorage } from 'core/domain/model/User/UserStorage';
import ReactTooltip from 'react-tooltip';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import DomHandler from 'ui/views/_functions/PrimeReact/DomHandler';

export const CustomFileUpload = ({
  accept = undefined,
  auto = false,
  cancelLabel = 'Reset',
  chooseLabel = 'Choose',
  className = null,
  dialogClassName = null,
  dialogFooter = null,
  dialogHeader = null,
  dialogOnHide = null,
  dialogVisible = null,
  disabled = false,
  fileLimit = 1,
  id = null,
  infoTooltip = '',
  invalidExtensionMessage = '',
  invalidFileSizeMessageDetail = 'maximum upload size is {0}.',
  invalidFileSizeMessageSummary = '{0}= Invalid file size, ',
  isDialog = false,
  manageDialogs,
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
  operation = 'POST',
  previewWidth = 50,
  replaceCheck = false,
  replaceCheckLabel = 'Replace data',
  style = null,
  uploadLabel = 'Upload',
  url = null,
  withCredentials = false
}) => {
  // static propTypes = {
  //   accept: PropTypes.string,
  //   auto: PropTypes.bool,
  //   cancelLabel: PropTypes.string,
  //   chooseLabel: PropTypes.string,
  //   className: PropTypes.string,
  //   disabled: PropTypes.bool,
  //   fileLimit: PropTypes.number,
  //   id: PropTypes.string,
  //   infoTooltip: PropTypes.string,
  //   invalidExtensionMessage: PropTypes.string,
  //   invalidFileSizeMessageDetail: PropTypes.string,
  //   invalidFileSizeMessageSummary: PropTypes.string,
  //   maxFileSize: PropTypes.number,
  //   mode: PropTypes.string,
  //   multiple: PropTypes.bool,
  //   name: PropTypes.string,
  //   onBeforeSend: PropTypes.func,
  //   onBeforeUpload: PropTypes.func,
  //   onClear: PropTypes.func,
  //   onError: PropTypes.func,
  //   onProgress: PropTypes.func,
  //   onSelect: PropTypes.func,
  //   onUpload: PropTypes.func,
  //   operation: PropTypes.string,
  //   previewWidth: PropTypes.number,
  //   replaceCheck: PropTypes.bool,
  //   style: PropTypes.object,
  //   uploadLabel: PropTypes.string,
  //   url: PropTypes.string,
  //   widthCredentials: PropTypes.bool
  // };

  const resourcesContext = useContext(ResourcesContext);

  const [fileUploadState, setFileUploadState] = useState({
    files: [],
    isUploading: false,
    isValid: true,
    msgs: [],
    replace: false
  });

  const _files = useRef([]);
  const content = useRef(null);
  const fileInput = useRef(null);
  const messagesUI = useRef(null);

  useEffect(() => {
    if (hasFiles() && auto) upload();
  }, [fileUploadState]);

  const checkValidExtension = file => {
    const acceptedExtensions = accept.toLowerCase().split(', ');
    if (file) {
      const extension = file.name.substring(file.name.lastIndexOf('.') + 1, file.name.length) || file.name;
      return acceptedExtensions.includes('*') || acceptedExtensions.includes(`.${extension.toLowerCase()}`);
    }

    if (hasFiles()) {
      const selectedExtension = fileUploadState.files.map(
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

  const hasFiles = () => fileUploadState.files && fileUploadState.files.length > 0;

  const isImage = file => /^image\//.test(file.type);

  const formatSize = bytes => {
    if (bytes === 0) return '0 B';

    let k = 1000,
      dm = 2,
      sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
      i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  };

  const onFileSelect = event => {
    setFileUploadState({ ...fileUploadState, msgs: [] });
    _files.current = fileUploadState.files || [];
    let cFiles = event.dataTransfer ? event.dataTransfer.files : event.target.files;

    if (fileLimit > 1) {
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
    } else {
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
    }

    setFileUploadState({ ...fileUploadState, files: _files.current });

    if (onSelect) {
      onSelect({ originalEvent: event, files: _files.current });
    }

    clearInputElement();

    if (mode === 'basic') {
      fileInput.style.display = 'none';
    }
  };

  const isFileSelected = file => {
    for (let sFile of fileUploadState.files) {
      if (sFile.name + sFile.type + sFile.size === file.name + file.type + file.size) return true;
    }
    return false;
  };

  const validate = file => {
    if (maxFileSize && file.size > maxFileSize) {
      messagesUI.show({
        severity: 'error',
        summary: invalidFileSizeMessageSummary.replace('{0}', file.name),
        detail: invalidFileSizeMessageDetail.replace('{0}', formatSize(maxFileSize))
      });

      return false;
    }
    if (accept) {
      if (!checkValidExtension(file)) {
        setFileUploadState({ ...fileUploadState, isValid: false });
        return false;
      }
    }

    setFileUploadState({ ...fileUploadState, isValid: true });
    return true;
  };

  const upload = () => {
    setFileUploadState({ ...fileUploadState, msgs: [], isUploading: true });
    let xhr = new XMLHttpRequest();
    let formData = new FormData();

    if (onBeforeUpload) {
      onBeforeUpload({ xhr: xhr, formData: formData });
    }

    for (let file of fileUploadState.files) {
      formData.append(name, file, file.name);
    }

    xhr.upload.addEventListener('progress', event => {
      if (event.lengthComputable) {
        setFileUploadState({ ...fileUploadState, progress: Math.round((event.loaded * 100) / event.total) });
      }

      if (onProgress) {
        onProgress({ originalEvent: event, progress: fileUploadState.progress });
      }
    });

    xhr.onreadystatechange = () => {
      if (xhr.readyState === 4) {
        setFileUploadState({ ...fileUploadState, progress: 0 });

        if (xhr.status >= 200 && xhr.status < 300) {
          if (onUpload) {
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
      nUrl += 'replace=' + fileUploadState.replace;
    }

    xhr.open(operation, nUrl, true);
    const tokens = userStorage.get();
    xhr.setRequestHeader('Authorization', `Bearer ${tokens.accessToken}`);

    if (onBeforeSend) {
      onBeforeSend({ xhr: xhr, formData: formData });
    }

    xhr.withCredentials = withCredentials;

    xhr.send(formData);
  };

  const clear = () => {
    setFileUploadState({ ...fileUploadState, files: [], isUploading: false });
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
      DomHandler.addClass(content, 'p-fileupload-highlight');
      event.stopPropagation();
      event.preventDefault();
    }
  };

  const onDragLeave = () => {
    if (!disabled) {
      DomHandler.removeClass(content, 'p-fileupload-highlight');
    }
  };

  const onDrop = event => {
    if (!disabled) {
      DomHandler.removeClass(content, 'p-fileupload-highlight');
      event.stopPropagation();
      event.preventDefault();

      let files = event.dataTransfer ? event.dataTransfer.files : event.target.files;
      let allowDrop = multiple || (files && files.length === 1);

      if (allowDrop) onFileSelect(event);
    }
  };

  const onSimpleUploaderClick = () => {
    if (hasFiles()) upload();
  };

  const remove = index => {
    clearInputElement();
    let currentFiles = [...fileUploadState.files];
    currentFiles.splice(index, 1);
    setFileUploadState({ ...fileUploadState, files: currentFiles });
  };

  const renderChooseButton = () => {
    let className = classNames('p-button p-fileupload-choose p-component p-button-text-icon-left');

    return (
      <span className={styles.chooseButton}>
        <span icon="pi pi-plus" className={className}>
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
          <span className="p-button-icon p-button-icon-left p-clickable pi pi-fw pi-plus" />
          <span className="p-button-text p-clickable">{chooseLabel}</span>
        </span>
        <Button
          className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
          icon="infoCircle"
          tooltip={infoTooltip}
          tooltipOptions={{ position: 'top' }}
        />
      </span>
    );
  };

  const renderFiles = () => {
    return (
      <div className="p-fileupload-files">
        {fileUploadState.files.map((file, index) => {
          let preview = isImage(file) ? (
            <div>
              <img alt={file.name} role="presentation" src={file.objectURL} width={previewWidth} />
            </div>
          ) : null;
          let fileName = <div>{file.name}</div>;
          let size = <div>{formatSize(file.size)}</div>;
          let removeButton = (
            <div>
              <Button type="button" icon="cancel" onClick={() => remove(index)} />
            </div>
          );

          return (
            <div className="p-fileupload-row" key={file.name + file.type + file.size}>
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
      <div className={styles.checkboxWrapper}>
        <Checkbox
          id="replaceCheckbox"
          inputId="replaceCheckbox"
          isChecked={fileUploadState.replace}
          onChange={() => setFileUploadState({ ...fileUploadState, replace: !fileUploadState.replace })}
          role="checkbox"
        />
        <label htmlFor="replaceCheckbox">
          <a onClick={() => setFileUploadState({ ...fileUploadState, replace: !fileUploadState.replace })}>
            {replaceCheckLabel}
          </a>
        </label>
      </div>
    );
  };

  const renderAdvanced = () => {
    const cClassName = classNames('p-fileupload p-component', className);
    let uploadButton, cancelButton, filesList, progressBar;

    if (!auto) {
      uploadButton = (
        <Fragment>
          <span data-tip data-for="inValidExtension">
            <Button
              disabled={disabled || !hasFiles() || checkValidExtension() || fileUploadState.isUploading}
              icon={fileUploadState.isUploading ? 'spinnerAnimate' : 'upload'}
              label={uploadLabel}
              onClick={upload}
            />
          </span>

          {accept && checkValidExtension() && (
            <ReactTooltip effect="solid" id="inValidExtension" place="top">
              {invalidExtensionMessage}
            </ReactTooltip>
          )}
        </Fragment>
      );
    }

    if (hasFiles()) {
      filesList = renderFiles();
      progressBar = <ProgressBar value={fileUploadState.progress} showValue={false} />;
    }

    return (
      <Fragment>
        <div id={id} className={cClassName} style={style}>
          <div className="p-fileupload-buttonbar">{renderChooseButton()}</div>
          <div
            ref={content}
            className="p-fileupload-content"
            onDragEnter={onDragEnter}
            onDragOver={onDragOver}
            onDragLeave={onDragLeave}
            onDrop={onDrop}>
            {progressBar}
            <Messages ref={messagesUI} />
            {filesList}
          </div>
          {replaceCheck && renderReplaceCheck()}
        </div>
        <p className={`${styles.invalidExtensionMsg} ${fileUploadState.isValid ? styles.isValid : undefined}`}>
          {invalidExtensionMessage}
        </p>
      </Fragment>
    );
  };

  const renderAdvancedFooter = () => {
    const cClassName = classNames('p-fileupload p-component', className);
    let uploadButton;
    let cancelButton;
    let filesList;
    let progressBar;

    if (!auto) {
      uploadButton = (
        <Fragment>
          <span data-tip data-for="invalidExtension">
            <Button
              disabled={disabled || !hasFiles() || checkValidExtension() || fileUploadState.isUploading}
              icon={fileUploadState.isUploading ? 'spinnerAnimate' : 'upload'}
              label={uploadLabel}
              onClick={upload}
            />
          </span>
        </Fragment>
      );
      cancelButton = (
        <Button
          className={'p-button-secondary'}
          label={cancelLabel}
          icon="undo"
          onClick={clear}
          disabled={disabled || !hasFiles()}
        />
      );
    }

    if (hasFiles()) {
      FileList = renderFiles();
      progressBar = <ProgressBar value={fileUploadState.progress} showValue={false} />;
    }

    return (
      <div className={styles.dialogFooter}>
        <div className={styles.secondaryZone}>{cancelButton}</div>
        <div className={styles.primaryZone}>
          {uploadButton}
          <Button
            className="p-button-secondary p-button-animated-blink"
            icon={'cancel'}
            label={resourcesContext.messages['close']}
            onClick={() => dialogOnHide()}
          />
        </div>
      </div>
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
          {auto ? chooseLabel : hasFiles() ? fileUploadState.files[0].name : chooseLabel}
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
        className={dialogClassName}
        footer={renderAdvancedFooter()}
        header={dialogHeader}
        onHide={dialogOnHide}
        visible={dialogVisible}
        style={{ width: '35vw' }}>
        {mode === 'advanced' && renderAdvanced()}
        {mode === 'basic' && renderBasic()}
      </Dialog>
    );
  } else {
    if (mode === 'advanced') return renderAdvanced();
    if (mode === 'basic') return renderBasic();
  }
};
