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
  designDatasetSchemas,
  handleRedirect,
  index,
  // isNameEditable,
  isReleased,
  layout,
  model,
  onNameDuplicate,
  // onNameEdit,
  onSaveError,
  onSaveName,
  onSelectIndex,
  placeholder
  // schemaId
  // schemaName
}) => {
  // const [buttonsIds, setButtonsIds] = useState();
  const [buttonsTitle, setButtonsTitle] = useState(!isUndefined(caption) ? caption : '');
  const [initialValue, setInitialValue] = useState();
  // const [focusedValue, setFocusedValue] = useState();

  const [isEditEnabled, setIsEditEnabled] = useState(false);

  // console.log('schemaId', schemaId);

  // console.log('index', index);

  const newDatasetRef = useRef();

  const checkDuplicates = (header, index) => {
    console.log('index of check: ', index);
    const inmTitles = [...designDatasetSchemas];
    const repeat = inmTitles.filter(title => title.datasetSchemaName.toLowerCase() === header.toLowerCase());
    return repeat.length > 0 && index !== repeat[0].index;
  };

  const onEditorKeyChange = (event, index) => {
    console.log('testing index: ', index);
    if (event.key === 'Enter') {
      onInputSave(event.target.value, onSelectIndex(index));
    }
    if (event.key === 'Escape') {
      if (!isEmpty(initialValue)) {
        setButtonsTitle(initialValue);
        setIsEditEnabled(false);
      }
    }
  };

  // console.log('buttonsIds', buttonsIds);

  const onEditorValueFocus = (value, index) => {
    setInitialValue(!isEmpty(value) ? value : initialValue);
    // const allValues = [...designDatasetSchemas];
    // const focusedValue = allValues.filter(title => title.datasetSchemaName.toLowerCase() === value.toLowerCase());
    // setFocusedValue(focusedValue[0].datasetId);
    // setButtonsIds(onSelectIndex(index));
  };

  const onInputSave = (value, index) => {
    if (!isEmpty(buttonsTitle)) {
      if (checkDuplicates(value, index)) {
        console.log('name is already taken');
        onNameDuplicate();
      } else {
        initialValue !== value
          ? onSaveName(value, index) && setIsEditEnabled(false) && setInitialValue(buttonsTitle)
          : setIsEditEnabled(false);
      }
    } else {
      if (!isUndefined(onSaveError)) {
        onSaveError();
        document.getElementsByClassName('p-inputtext p-component')[0].focus();
      }
    }
  };

  const onUpdateNameValidation = value => {
    if (!isUndefined(value) && value.match(/^[a-zA-Z0-9-_\s]*$/)) {
      setButtonsTitle(value);
    }
  };

  const doubleClick = () => {
    setIsEditEnabled(true);
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
      {!isUndefined(isEditEnabled) && isEditEnabled ? (
        <InputText
          key={index}
          autoFocus={true}
          className={`${styles.inputText}`}
          onBlur={e => {
            onInputSave(e.target.value, onSelectIndex(index));
          }}
          onChange={e => onUpdateNameValidation(e.target.value)}
          onFocus={e => {
            e.preventDefault();
            onEditorValueFocus(e.target.value, index);
          }}
          onKeyDown={e => onEditorKeyChange(e, index)}
          placeholder={placeholder}
          value={!isUndefined(buttonsTitle) ? buttonsTitle : caption}
        />
      ) : (
        <p className={styles.caption} onDoubleClick={doubleClick}>
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
