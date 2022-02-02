import { useContext, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './InputFile.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { Button } from 'views/_components/Button';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { ErrorMessage } from 'views/_components/ErrorMessage';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const InputFile = ({
  accept,
  buttonTextNoFile,
  buttonTextWithFile,
  className,
  errorMessage,
  fileRef,
  hasError,
  onChange,
  onClearFile,
  onClick = () => {},
  onKeyPress
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const [fileName, setFileName] = useState('');

  const onFileSelect = e => {
    e.preventDefault();
    fileInputClicked();
    setFileName(e.target?.files[0]?.name ? e.target.files[0].name : '');
    onChange(e);
  };

  const onFileClear = () => {
    onClearFile();
    setFileName('');
    fileRef.current.value = '';
  };

  const fileInputClicked = () => {
    window.removeEventListener('focus', handleFocusBack);
  };

  const handleFocusBack = () => {
    window.removeEventListener('focus', handleFocusBack);
    onClick();
  };

  const clickFileInput = () => {
    window.addEventListener('focus', handleFocusBack);
  };

  const renderMessage = () => {
    if (hasError) {
      return (
        <div className={styles.messageWrapper}>
          <div className={styles.message}>
            <ErrorMessage message={errorMessage || resourcesContext.messages['fileNotSelectedError']} />
          </div>
        </div>
      );
    }

    if (!isEmpty(fileName)) {
      return (
        <div className={styles.messageWrapper}>
          <div className={styles.message}>{fileName}</div>
          <FontAwesomeIcon className={styles.clearButton} icon={AwesomeIcons('cross')} onClick={onFileClear} />
        </div>
      );
    }
  };

  return (
    <div className={`${styles.container} ${className ? className : ''}`}>
      <div className={styles.buttonWrap}>
        <Button
          className="p-button p-component p-button-primary p-button-animated-blink p-button-text-icon-left"
          icon="upload"
          label={isEmpty(fileName) ? buttonTextNoFile : buttonTextWithFile}
          onClick={() => fileRef.current.click()}
        />
      </div>

      {renderMessage()}

      <input
        accept={accept}
        className={styles.hiddenFileInput}
        hidden
        id="fileInput"
        name="fileInput"
        onChange={onFileSelect}
        onClick={clickFileInput}
        onKeyPress={onKeyPress}
        ref={fileRef}
        type="file"
      />
    </div>
  );
};
