import React, { useRef, useState } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { isEmpty, isUndefined } from 'lodash';

import styles from './BigButton.module.css';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { DropdownButton } from 'ui/views/_components/DropdownButton';
import { DropDownMenu } from 'ui/views/_components/DropdownButton/_components/DropDownMenu';
import { Icon } from 'ui/views/_components/Icon';
import { InputText } from 'ui/views/_components/InputText';

export const BigButton = ({
  caption,
  handleRedirect,
  isNameEditable,
  isReleased,
  layout,
  model,
  onNameEdit,
  onSaveError,
  onSaveName
}) => {
  const [buttonsTitle, setButtonsTitle] = useState(!isUndefined(caption) ? caption : '');
  const [initialValue, setInitialValue] = useState();

  const newDatasetRef = useRef();

  const onEditorKeyChange = event => {
    if (event.key === 'Enter') {
      if (!isEmpty(buttonsTitle)) {
        onSaveName(event.target.value);
        onNameEdit();
      } else {
        if (!isUndefined(onSaveError)) {
          onSaveError();
          document.getElementsByClassName('p-inputtext p-component')[0].focus();
        }
      }
    } else if (event.key === 'Escape') {
      setButtonsTitle(initialValue);
      onNameEdit();
    }
  };

  const onEditorValueFocus = value => {
    setInitialValue(value);
  };

  const dataset = model ? (
    <>
      <div className={`${styles.bigButton} ${styles.dataset}`}>
        <a
          href="#"
          onClick={e => {
            e.preventDefault();
            handleRedirect();
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('dataset')} />
        </a>
        <DropdownButton
          icon="caretDown"
          model={model}
          buttonStyle={{ position: 'absolute', bottom: '-5px', right: '0px' }}
          iconStyle={{ fontSize: '1.8rem' }}
        />
        {isReleased && (
          <Icon style={{ position: 'absolute', top: '0', right: '0', fontSize: '1.8rem' }} icon="cloudUpload" />
        )}
      </div>
      <p className={styles.caption}>{caption}</p>
    </>
  ) : (
    <></>
  );
  const dashboard = (
    <>
      <div className={`${styles.bigButton} ${styles.dashboard}`}>
        <a
          href="#"
          onClick={e => {
            e.preventDefault();
            handleRedirect();
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('barChart')} />
        </a>
      </div>
      <p className={styles.caption}>{caption}</p>
    </>
  );
  const designDatasetSchema = model ? (
    <>
      <div className={`${styles.bigButton} ${styles.designDatasetSchema}`}>
        <a
          href="#"
          onClick={e => {
            e.preventDefault();
            handleRedirect();
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('pencilRuler')} />
        </a>
        <DropdownButton
          icon="caretDown"
          model={model}
          buttonStyle={{ position: 'absolute', bottom: '-5px', right: '0px' }}
          iconStyle={{ fontSize: '1.8rem' }}
        />
      </div>
      {!isUndefined(isNameEditable) && isNameEditable ? (
        <InputText
          className={`${styles.inputText}`}
          onBlur={e => {
            if (!isEmpty(buttonsTitle)) {
              onSaveName(e.target.value);
              onNameEdit();
            } else {
              if (!isUndefined(onSaveError)) {
                document.getElementsByClassName('p-inputtext p-component')[0].focus();
                onSaveError();
              }
            }
          }}
          onChange={e => setButtonsTitle(e.target.value)}
          onFocus={e => {
            e.preventDefault();
            onEditorValueFocus(e.target.value);
          }}
          onKeyDown={e => onEditorKeyChange(e)}
          placeholder="this is a placeholder"
          value={!isUndefined(buttonsTitle) ? buttonsTitle : caption}
        />
      ) : (
        <p className={styles.caption} onDoubleClick={onNameEdit}>
          {!isUndefined(buttonsTitle) ? buttonsTitle : caption}
        </p>
      )}
    </>
  ) : (
    <></>
  );
  const documents = (
    <>
      <div className={`${styles.bigButton} ${styles.documents}`}>
        <a
          href="#"
          onClick={e => {
            e.preventDefault();
            handleRedirect();
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('file')} />
        </a>
      </div>
      <p className={styles.caption}>{caption}</p>
    </>
  );
  const newItem = (
    <>
      <div className={`${styles.bigButton} ${styles.newItem}`}>
        <a
          href="#"
          onClick={e => {
            e.preventDefault();
            newDatasetRef.current.show(e);
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('plus')} />
        </a>
        <DropDownMenu ref={newDatasetRef} model={model} />
      </div>
      <p className={styles.caption}>{caption}</p>
    </>
  );
  const buttons = {
    dataset,
    dashboard,
    designDatasetSchema,
    documents,
    newItem
  };
  return <div className={`${styles.datasetItem}`}>{buttons[layout]}</div>;
};
