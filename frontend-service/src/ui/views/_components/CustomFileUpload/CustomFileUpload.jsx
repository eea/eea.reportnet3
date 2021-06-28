import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import classNames from 'classnames';

import styles from './CustomFileUpload.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox';
import { Dialog } from 'ui/views/_components/Dialog';

import { Messages } from 'primereact/messages';
import { ProgressBar } from 'primereact/progressbar';
import { userStorage } from 'core/domain/model/User/UserStorage';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { customFileUploadReducer } from './_functions/customFileUploadReducer';
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
  const resourcesContext = useContext(ResourcesContext);

  const [state, dispatch] = useReducer(customFileUploadReducer, {
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
  }, [state]);

  const checkValidExtension = file => {
    const acceptedExtensions = accept.toLowerCase().split(', ');

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
    if (bytes === 0) return '0 B';

    let k = 1024,
      dm = 2,
      sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
      i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  };

  const onFileSelect = event => {
    dispatch({ type: 'UPLOAD_PROPERTY', payload: { msgs: [] } });
    _files.current = state.files || [];
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

    dispatch({ type: 'UPLOAD_PROPERTY', payload: { isValid: true } });
    return true;
  };

  const upload = () => {
    dispatch({ type: 'UPLOAD_PROPERTY', payload: { msgs: [], isUploading: true } });
    let xhr = new XMLHttpRequest();
    let formData = new FormData();

    if (onBeforeUpload) {
      onBeforeUpload({ xhr: xhr, formData: formData });
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
      nUrl += nUrl.lastIndexOf('?') !== -1 ? '&' : '?';
      nUrl += 'replace=' + state.replace;
    }

    xhr.open(operation, nUrl, true);
    const tokens = userStorage.getTokens();
    xhr.setRequestHeader('Authorization', `Bearer ${tokens.accessToken}`);

    if (onBeforeSend) {
      onBeforeSend({ xhr: xhr, formData: formData });
    }

    xhr.withCredentials = withCredentials;

    xhr.send(formData);
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
      let allowDrop = multiple || (files && files.length === 1);

      if (allowDrop) onFileSelect(event);
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
    let className = classNames('p-button p-fileupload-choose p-component p-button-text-icon-left');

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

  const renderFiles = () => {
    return (
      <div className="p-fileupload-files">
        {state.files.map((file, index) => {
          let preview = isImage(file) ? (
            <div>
              <img alt={file.name} role="presentation" src={file.objectURL} width={previewWidth} />
            </div>
          ) : null;
          let fileName = <div>{file.name}</div>;
          let size = <div>{formatSize(file.size)}</div>;
          let removeButton = (
            <div>
              <Button icon="cancel" onClick={() => remove(index)} type="button" />
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
          checked={state.replace}
          id="replaceCheckbox"
          inputId="replaceCheckbox"
          onChange={() => dispatch({ type: 'UPLOAD_PROPERTY', payload: { replace: !state.replace } })}
          role="checkbox"
        />
        <label htmlFor="replaceCheckbox">
          <span onClick={() => dispatch({ type: 'UPLOAD_PROPERTY', payload: { replace: !state.replace } })}>
            {replaceCheckLabel}
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

    if (!auto) {
      uploadButton = (
        <span data-for="invalidExtension" data-tip>
          <Button
            disabled={disabled || !hasFiles() || checkValidExtension() || state.isUploading}
            icon={state.isUploading ? 'spinnerAnimate' : 'upload'}
            label={uploadLabel}
            onClick={upload}
          />
        </span>
      );
      cancelButton = (
        <Button
          className={'p-button-secondary'}
          disabled={disabled || !hasFiles()}
          icon="undo"
          label={cancelLabel}
          onClick={clear}
        />
      );
    }

    if (hasFiles()) {
      FileList = renderFiles();
    }

    return (
      <div className={styles.dialogFooter}>
        <div className={styles.secondaryZone}>{cancelButton}</div>
        <div className={styles.primaryZone}>
          {uploadButton}
          <Button
            className="p-button-secondary p-button-animated-blink p-button-right-aligned"
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
        className={dialogClassName}
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
