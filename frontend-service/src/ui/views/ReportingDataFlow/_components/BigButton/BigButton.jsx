import React, { useRef, useState, useContext } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { isEmpty, isUndefined, uniqBy } from 'lodash';

import styles from './BigButton.module.css';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { DropdownButton } from 'ui/views/_components/DropdownButton';
import { DropDownMenu } from 'ui/views/_components/DropdownButton/_components/DropDownMenu';
import { Icon } from 'ui/views/_components/Icon';
import { InputText } from 'ui/views/_components/InputText';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const BigButton = ({
  caption,
  datasetSchemaNames,
  handleRedirect,
  index,
  isReleased,
  layout,
  model,
  onDuplicateName,
  onSaveError,
  onSaveName,
  placeholder
}) => {
  const resources = useContext(ResourcesContext);

  const [buttonsTitle, setButtonsTitle] = useState(!isUndefined(caption) ? caption : '');
  const [initialValue, setInitialValue] = useState();
  const [isEditEnabled, setIsEditEnabled] = useState(false);

  const newDatasetRef = useRef();

  if (isEditEnabled && document.getElementsByClassName('p-inputtext p-component').length > 0) {
    document.getElementsByClassName('p-inputtext p-component')[0].focus();
  }

  const checkDuplicates = (header, idx) => {
    const inmTitles = [...datasetSchemaNames];
    const repeat = inmTitles.filter(title => title.schemaName.toLowerCase() === header.toLowerCase());
    return repeat.length > 0 && idx !== repeat[0].schemaIndex;
  };

  const onEditorKeyChange = (event, index) => {
    if (event.key === 'Enter') {
      if (buttonsTitle !== '') {
        onInputSave(event.target.value, index);
      } else {
        if (!isUndefined(onSaveError)) {
          onSaveError();
          document.getElementsByClassName('p-inputtext p-component')[0].focus();
        }
      }
    }
    if (event.key === 'Escape') {
      if (!isEmpty(initialValue)) {
        setButtonsTitle(initialValue);
        setIsEditEnabled(false);
      }
    }
  };

  const onEditorValueFocus = value => {
    setInitialValue(!isEmpty(value) ? value : initialValue);
  };

  const onEnableSchemaNameEdit = () => {
    setIsEditEnabled(true);
  };

  const designModel =
    !isUndefined(model) &&
    model.map(button => {
      if (button.label === resources.messages['rename']) {
        button.command = onEnableSchemaNameEdit;
      }
      return button;
    });

  const onInputSave = (value, index) => {
    const changeTitle = onUpdateName(value, index);
    if (!isUndefined(changeTitle)) {
      setInitialValue(changeTitle.originalSchemaName);
      if (changeTitle.correct) {
        setIsEditEnabled(false);
        setInitialValue(changeTitle.originalSchemaName);
      }
    }
  };

  const onUpdateName = (title, index) => {
    if (!isEmpty(buttonsTitle)) {
      if (initialValue !== title) {
        if (checkDuplicates(title, index)) {
          onDuplicateName();
          document.getElementsByClassName('p-inputtext p-component')[0].focus();
          return { correct: false, originalSchemaName: initialValue, wrongName: title };
        } else {
          onSaveName(title, index) && setIsEditEnabled(false) && setInitialValue(buttonsTitle);
        }
      } else {
        setIsEditEnabled(false);
      }
    } else {
      if (!isUndefined(onSaveError)) {
        onSaveError();
        document.getElementsByClassName('p-inputtext p-component')[0].focus();
      }
    }
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
  const designDatasetSchema = designModel ? (
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
          model={designModel}
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
            onInputSave(e.target.value, index);
          }}
          onChange={e => setButtonsTitle(e.target.value)}
          onFocus={e => {
            e.preventDefault();
            onEditorValueFocus(e.target.value);
          }}
          onKeyDown={e => onEditorKeyChange(e, index)}
          placeholder={placeholder}
          value={!isUndefined(buttonsTitle) ? buttonsTitle : caption}
        />
      ) : (
        <p className={styles.caption} onDoubleClick={onEnableSchemaNameEdit}>
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
          <FontAwesomeIcon icon={AwesomeIcons('info')} />
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
