import { useState } from 'react';
import isEmpty from 'lodash/isEmpty';

import styles from './InputFile.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { Button } from 'views/_components/Button';

import { AwesomeIcons } from 'conf/AwesomeIcons';

export const InputFile = ({ onChange, buttonTextNoFile, buttonTextWithFile, accept, fileRef, onClearFile }) => {
  const [fileName, setFileName] = useState('');

  const onFileSelect = e => {
    e.preventDefault();
    setFileName(e.target?.files[0]?.name ? e.target.files[0].name : '');

    onChange(e);
  };

  const onFileClear = () => {
    onClearFile();
    setFileName('');
    fileRef.current.value = '';
  };

  return (
    <div style={{ display: 'flex' }}>
      <div style={{ flexShrink: 0 }}>
        <Button
          className="p-button p-component p-button-primary p-button-animated-blink p-button-text-icon-left"
          icon="upload"
          label={isEmpty(fileName) ? buttonTextNoFile : buttonTextWithFile}
          onClick={() => fileRef.current.click()}
        />
      </div>

      {!isEmpty(fileName) && (
        <div className={styles.fileNameWrapper}>
          <div className={styles.fileName}>{fileName}</div>

          <FontAwesomeIcon
            className={styles.clearButton}
            icon={AwesomeIcons('cross')}
            onClick={onFileClear}
            style={{ float: 'center', color: 'var(--font-color)' }}
          />
        </div>
      )}

      <input
        accept={accept}
        hidden
        id="fileInput"
        name="fileInput"
        onChange={onFileSelect}
        ref={fileRef}
        style={{ display: 'none' }}
        type="file"
      />
    </div>
  );
};
