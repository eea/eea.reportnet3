import React, { useRef, useState, useContext } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { isEmpty, isUndefined } from 'lodash';

import styles from './BigButton.module.css';

import { config } from 'conf';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import DataflowConf from 'conf/dataflow.config.json';

import { DropdownButton } from 'ui/views/_components/DropdownButton';
import { DropDownMenu } from 'ui/views/_components/DropdownButton/_components/DropDownMenu';
import { Icon } from 'ui/views/_components/Icon';
import { InputText } from 'ui/views/_components/InputText';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const BigButton = ({
  buttonClass,
  buttonIcon,
  buttonIconClass,
  caption,
  dataflowStatus,
  datasetSchemaInfo,
  handleRedirect,
  index,
  isReleased,
  layout,
  model,
  onDuplicateName,
  onSaveError,
  onSaveName,
  onWheel,
  placeholder
}) => {
  const resources = useContext(ResourcesContext);

  const [buttonsTitle, setButtonsTitle] = useState(!isUndefined(caption) ? caption : '');
  const [initialValue, setInitialValue] = useState();
  const [isEditEnabled, setIsEditEnabled] = useState(false);

  const menuBigButtonRef = useRef();

  if (isEditEnabled && document.getElementsByClassName('p-inputtext p-component').length > 0) {
    document.getElementsByClassName('p-inputtext p-component')[0].focus();
  }

  const checkDuplicates = (header, idx) => {
    const schemas = [...datasetSchemaInfo];
    const repeat = schemas.filter(title => title.schemaName.toLowerCase() === header.toLowerCase());
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

  const onWheelClick = event => {
    if (event.button === 1) {
      window.open(onWheel);
    }
  };

  const defaultBigButton = (
    <>
      <div className={`${styles.bigButton} ${styles.defaultBigButton} ${styles[buttonClass]}`}>
        <a
          onClick={e => {
            e.preventDefault();
            handleRedirect();
          }}
          onMouseDown={event => onWheelClick(event)}>
          <FontAwesomeIcon icon={AwesomeIcons(buttonIcon)} className={styles[buttonIconClass]} />
        </a>
        {model ? (
          <>
            <DropdownButton
              icon="caretDown"
              model={designModel}
              buttonStyle={{ position: 'absolute', bottom: '-5px', right: '0px' }}
              iconStyle={{ fontSize: '1.8rem' }}
            />
            {isReleased && (
              <Icon style={{ position: 'absolute', top: '0', right: '0', fontSize: '1.8rem' }} icon="cloudUpload" />
            )}
          </>
        ) : (
          <></>
        )}
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
        <p
          className={styles.caption}
          onDoubleClick={dataflowStatus === DataflowConf.dataflowStatus['DESIGN'] ? onEnableSchemaNameEdit : null}>
          {!isUndefined(buttonsTitle) ? buttonsTitle : caption}
        </p>
      )}
    </>
  );

  const menuBigButton = (
    <>
      <div className={`${styles.bigButton} ${styles.menuBigButton} ${styles[buttonClass]}`}>
        <a
          onClick={e => {
            e.preventDefault();
            menuBigButtonRef.current.show(e);
          }}>
          <FontAwesomeIcon icon={AwesomeIcons(buttonIcon)} className={styles[buttonIconClass]} />
        </a>
        <DropDownMenu ref={menuBigButtonRef} model={model} />
      </div>
      <p className={styles.caption}>{caption}</p>
    </>
  );

  const buttons = {
    defaultBigButton,
    menuBigButton
  };

  return <div className={`${styles.datasetItem}`}>{buttons[layout]}</div>;
};
