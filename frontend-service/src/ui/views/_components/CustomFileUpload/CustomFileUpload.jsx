import React, { Component, Fragment } from 'react';

import PropTypes from 'prop-types';
import classNames from 'classnames';

import styles from './CustomFileUpload.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Messages } from 'primereact/messages';
import { ProgressBar } from 'primereact/progressbar';
import { userStorage } from 'core/domain/model/User/UserStorage';
import ReactTooltip from 'react-tooltip';

import DomHandler from 'ui/views/_functions/PrimeReact/DomHandler';

export class CustomFileUpload extends Component {
  static defaultProps = {
    accept: undefined,
    auto: false,
    cancelLabel: 'Reset',
    chooseLabel: 'Choose',
    className: null,
    disabled: false,
    fileLimit: 1,
    id: null,
    infoTooltip: '',
    invalidExtensionMessage: '',
    invalidFileSizeMessageDetail: 'maximum upload size is {0}.',
    invalidFileSizeMessageSummary: '{0}: Invalid file size, ',
    maxFileSize: null,
    mode: 'advanced',
    multiple: false,
    name: null,
    onBeforeSend: null,
    onBeforeUpload: null,
    onClear: null,
    onError: null,
    onProgress: null,
    onSelect: null,
    onUpload: null,
    previewWidth: 50,
    style: null,
    uploadLabel: 'Upload',
    url: null,
    widthCredentials: false
  };

  static propTypes = {
    accept: PropTypes.string,
    auto: PropTypes.bool,
    cancelLabel: PropTypes.string,
    chooseLabel: PropTypes.string,
    className: PropTypes.string,
    disabled: PropTypes.bool,
    fileLimit: PropTypes.number,
    id: PropTypes.string,
    infoTooltip: PropTypes.string,
    invalidExtensionMessage: PropTypes.string,
    invalidFileSizeMessageDetail: PropTypes.string,
    invalidFileSizeMessageSummary: PropTypes.string,
    maxFileSize: PropTypes.number,
    mode: PropTypes.string,
    multiple: PropTypes.bool,
    name: PropTypes.string,
    onBeforeSend: PropTypes.func,
    onBeforeUpload: PropTypes.func,
    onClear: PropTypes.func,
    onError: PropTypes.func,
    onProgress: PropTypes.func,
    onSelect: PropTypes.func,
    onUpload: PropTypes.func,
    previewWidth: PropTypes.number,
    style: PropTypes.object,
    uploadLabel: PropTypes.string,
    url: PropTypes.string,
    widthCredentials: PropTypes.bool
  };

  constructor(props) {
    super(props);
    this.state = {
      files: [],
      isValid: true,
      msgs: []
    };

    this.upload = this.upload.bind(this);
    this.clear = this.clear.bind(this);
    this.onFileSelect = this.onFileSelect.bind(this);
    this.onDragEnter = this.onDragEnter.bind(this);
    this.onDragOver = this.onDragOver.bind(this);
    this.onDragLeave = this.onDragLeave.bind(this);
    this.onDrop = this.onDrop.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onSimpleUploaderClick = this.onSimpleUploaderClick.bind(this);
  }

  checkValidExtension(file) {
    const acceptedExtensions = this.props.accept.split(', ');
    console.log({ acceptedExtensions });
    if (file) {
      const extension = file.name.substring(file.name.lastIndexOf('.') + 1, file.name.length) || file.name;
      return acceptedExtensions.includes(`.${extension}`);
    }
    console.log(this.hasFiles());
    if (this.hasFiles()) {
      const selectedExtension = this.state.files.map(
        file => file.name.substring(file.name.lastIndexOf('.') + 1, file.name.length) || file.name
      );
      console.log({ selectedExtension });
      return !selectedExtension.some(ext => acceptedExtensions.includes(`.${ext}`));
    }
    return false;
  }

  clearInputElement() {
    if (this.fileInput) {
      this.fileInput.value = '';
      if (this.props.mode === 'basic') {
        this.fileInput.style.display = 'inline';
      }
    }
  }

  hasFiles() {
    return this.state.files && this.state.files.length > 0;
  }

  isImage(file) {
    return /^image\//.test(file.type);
  }

  remove(index) {
    this.clearInputElement();
    let currentFiles = [...this.state.files];
    currentFiles.splice(index, 1);
    this.setState({ files: currentFiles });
  }

  formatSize(bytes) {
    if (bytes === 0) {
      return '0 B';
    }
    let k = 1000,
      dm = 2,
      sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
      i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  }

  onFileSelect(event) {
    this.setState({ msgs: [] });
    this.files = this.state.files || [];
    let files = event.dataTransfer ? event.dataTransfer.files : event.target.files;

    if (this.props.fileLimit > 1) {
      for (let i = 0; i < files.length; i++) {
        let file = files[i];

        if (!this.isFileSelected(file)) {
          if (this.validate(file)) {
            if (this.isImage(file)) {
              file.objectURL = window.URL.createObjectURL(file);
            }
            this.files.push(file);
          }
        }
      }
    } else {
      let file = files[0];
      if (!this.isFileSelected(file)) {
        if (this.validate(file)) {
          if (this.isImage(file)) {
            file.objectURL = window.URL.createObjectURL(file);
          }
          this.files = [];
          this.files.push(file);
        }
      }
    }

    this.setState({ files: this.files }, () => {
      if (this.hasFiles() && this.props.auto) {
        this.upload();
      }
    });

    if (this.props.onSelect) {
      this.props.onSelect({ originalEvent: event, files: files });
    }

    this.clearInputElement();

    if (this.props.mode === 'basic') {
      this.fileInput.style.display = 'none';
    }
  }

  isFileSelected(file) {
    for (let sFile of this.state.files) {
      if (sFile.name + sFile.type + sFile.size === file.name + file.type + file.size) return true;
    }
    return false;
  }

  validate(file) {
    if (this.props.maxFileSize && file.size > this.props.maxFileSize) {
      this.messagesUI.show({
        severity: 'error',
        summary: this.props.invalidFileSizeMessageSummary.replace('{0}', file.name),
        detail: this.props.invalidFileSizeMessageDetail.replace('{0}', this.formatSize(this.props.maxFileSize))
      });

      return false;
    }
    if (this.props.accept) {
      console.log(this.props.accept);
      if (!this.checkValidExtension(file)) {
        this.setState({ isValid: false });
        return false;
      }
    }

    this.setState({ isValid: true });
    return true;
  }

  upload() {
    this.setState({ msgs: [] });
    let xhr = new XMLHttpRequest();
    let formData = new FormData();

    if (this.props.onBeforeUpload) {
      this.props.onBeforeUpload({
        xhr: xhr,
        formData: formData
      });
    }

    for (let file of this.state.files) {
      formData.append(this.props.name, file, file.name);
    }

    xhr.upload.addEventListener('progress', event => {
      if (event.lengthComputable) {
        this.setState({ progress: Math.round((event.loaded * 100) / event.total) });
      }

      if (this.props.onProgress) {
        this.props.onProgress({
          originalEvent: event,
          progress: this.progress
        });
      }
    });

    xhr.onreadystatechange = () => {
      if (xhr.readyState === 4) {
        this.setState({ progress: 0 });

        if (xhr.status >= 200 && xhr.status < 300) {
          if (this.props.onUpload) {
            this.props.onUpload({ xhr: xhr, files: this.files });
          }
        } else {
          if (this.props.onError) {
            this.props.onError({ xhr: xhr, files: this.files });
          }
        }

        this.clear();
      }
    };

    xhr.open('POST', this.props.url, true);
    const tokens = userStorage.get();
    xhr.setRequestHeader('Authorization', `Bearer ${tokens.accessToken}`);

    if (this.props.onBeforeSend) {
      this.props.onBeforeSend({
        xhr: xhr,
        formData: formData
      });
    }

    xhr.withCredentials = this.props.withCredentials;

    xhr.send(formData);
  }

  clear() {
    this.setState({ files: [] });
    if (this.props.onClear) {
      this.props.onClear();
    }
    this.clearInputElement();
  }

  onFocus(event) {
    DomHandler.addClass(event.currentTarget.parentElement, 'p-focus');
  }

  onBlur(event) {
    DomHandler.removeClass(event.currentTarget.parentElement, 'p-focus');
  }

  onDragEnter(event) {
    if (!this.props.disabled) {
      event.stopPropagation();
      event.preventDefault();
    }
  }

  onDragOver(event) {
    if (!this.props.disabled) {
      DomHandler.addClass(this.content, 'p-fileupload-highlight');
      event.stopPropagation();
      event.preventDefault();
    }
  }

  onDragLeave(event) {
    if (!this.props.disabled) {
      DomHandler.removeClass(this.content, 'p-fileupload-highlight');
    }
  }

  onDrop(event) {
    if (!this.props.disabled) {
      DomHandler.removeClass(this.content, 'p-fileupload-highlight');
      event.stopPropagation();
      event.preventDefault();

      let files = event.dataTransfer ? event.dataTransfer.files : event.target.files;
      let allowDrop = this.props.multiple || (files && files.length === 1);

      if (allowDrop) {
        this.onFileSelect(event);
      }
    }
  }

  onSimpleUploaderClick() {
    if (this.hasFiles()) {
      this.upload();
    }
  }

  renderChooseButton() {
    let className = classNames('p-button p-fileupload-choose p-component p-button-text-icon-left');

    return (
      <span className={styles.chooseButton}>
        <span icon="pi pi-plus" className={className}>
          <input
            ref={el => (this.fileInput = el)}
            type="file"
            onChange={this.onFileSelect}
            onFocus={this.onFocus}
            onBlur={this.onBlur}
            multiple={this.props.multiple}
            accept={this.props.accept}
            disabled={this.props.disabled}
          />
          <span className="p-button-icon p-button-icon-left p-clickable pi pi-fw pi-plus" />
          <span className="p-button-text p-clickable">{this.props.chooseLabel}</span>
        </span>
        <Button
          className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
          icon="infoCircle"
          tooltip={this.props.infoTooltip}
          tooltipOptions={{ position: 'top' }}
        />
      </span>
    );
  }

  renderFiles() {
    return (
      <div className="p-fileupload-files">
        {this.state.files.map((file, index) => {
          let preview = this.isImage(file) ? (
            <div>
              <img alt={file.name} role="presentation" src={file.objectURL} width={this.props.previewWidth} />
            </div>
          ) : null;
          let fileName = <div>{file.name}</div>;
          let size = <div>{this.formatSize(file.size)}</div>;
          let removeButton = (
            <div>
              <Button type="button" icon="cancel" onClick={() => this.remove(index)} />
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
  }

  renderAdvanced() {
    let className = classNames('p-fileupload p-component', this.props.className);
    let uploadButton, cancelButton, filesList, progressBar;
    let chooseButton = this.renderChooseButton();
    console.log('ADVANCED');
    if (!this.props.auto) {
      uploadButton = (
        <Fragment>
          <span data-tip data-for="inValidExtension">
            <Button
              label={this.props.uploadLabel}
              icon="upload"
              onClick={this.upload}
              disabled={this.props.disabled || !this.hasFiles() || (this.props.accept && this.checkValidExtension())}
            />
          </span>

          {this.props.accept && this.checkValidExtension() && (
            <ReactTooltip effect="solid" id="inValidExtension" place="top">
              {this.props.invalidExtensionMessage}
            </ReactTooltip>
          )}
        </Fragment>
      );
      cancelButton = (
        <Button
          className={'p-button-secondary'}
          label={this.props.cancelLabel}
          icon="undo"
          onClick={this.clear}
          disabled={this.props.disabled || !this.hasFiles()}
        />
      );
    }

    if (this.hasFiles()) {
      filesList = this.renderFiles();
      progressBar = <ProgressBar value={this.state.progress} showValue={false} />;
    }

    return (
      <Fragment>
        <div id={this.props.id} className={className} style={this.props.style}>
          <div className="p-fileupload-buttonbar">
            {chooseButton}
            <div className="p-toolbar-group-right">
              {uploadButton}
              {cancelButton}
            </div>
          </div>
          <div
            ref={el => {
              this.content = el;
            }}
            className="p-fileupload-content"
            onDragEnter={this.onDragEnter}
            onDragOver={this.onDragOver}
            onDragLeave={this.onDragLeave}
            onDrop={this.onDrop}>
            {progressBar}
            <Messages ref={el => (this.messagesUI = el)} />
            {filesList}
          </div>
        </div>
        <p className={`${styles.invalidExtensionMsg} ${this.state.isValid ? styles.isValid : undefined}`}>
          {this.props.invalidExtensionMessage}
        </p>
      </Fragment>
    );
  }

  renderBasic() {
    let buttonClassName = classNames('p-button p-fileupload-choose p-component p-button-text-icon-left', {
      'p-fileupload-choose-selected': this.hasFiles()
    });
    let iconClassName = classNames('p-button-icon-left pi', {
      'pi-plus': !this.hasFiles() || this.props.auto,
      'pi-upload': this.hasFiles() && !this.props.auto
    });

    return (
      <span className={buttonClassName} onMouseUp={this.onSimpleUploaderClick}>
        <span className={iconClassName} />
        <span className="p-button-text p-clickable">
          {this.props.auto
            ? this.props.chooseLabel
            : this.hasFiles()
            ? this.state.files[0].name
            : this.props.chooseLabel}
        </span>
        <input
          ref={el => (this.fileInput = el)}
          type="file"
          multiple={this.props.multiple}
          accept={this.props.accept}
          disabled={this.props.disabled}
          onChange={this.onFileSelect}
          onFocus={this.onFocus}
          onBlur={this.onBlur}
        />
      </span>
    );
  }

  render() {
    if (this.props.mode === 'advanced') return this.renderAdvanced();
    else if (this.props.mode === 'basic') return this.renderBasic();
  }
}
