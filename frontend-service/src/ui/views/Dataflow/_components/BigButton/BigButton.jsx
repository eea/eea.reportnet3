import React, { useContext, useEffect, useRef, useState } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';
import uuid from 'uuid';

import styles from './BigButton.module.scss';

import { config } from 'conf';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { DropdownButton } from 'ui/views/_components/DropdownButton';
import { DropDownMenu } from 'ui/views/_components/DropdownButton/_components/DropDownMenu';
import { Icon } from 'ui/views/_components/Icon';
import { InputText } from 'ui/views/_components/InputText';
import ReactTooltip from 'react-tooltip';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

export const BigButton = ({
  buttonClass,
  buttonIcon,
  buttonIconClass,
  canEditName = true,
  caption,
  dataflowStatus,
  datasetSchemaInfo,
  enabled = true,
  handleRedirect = () => {},
  helpClassName,
  index,
  infoStatus,
  infoStatusIcon,
  layout,
  model,
  onSaveName,
  onWheel,
  placeholder,
  setErrorDialogData,
  tooltip
}) => {
  const resources = useContext(ResourcesContext);
  const regex = new RegExp(/[a-zA-Z0-9_-\s()]/);

  const [buttonsTitle, setButtonsTitle] = useState('');
  const [initialValue, setInitialValue] = useState();
  const [isEditEnabled, setIsEditEnabled] = useState(false);

  const menuBigButtonRef = useRef();
  const tooltipId = uuid.v4();

  useEffect(() => {
    setButtonsTitle(caption);
  }, [caption]);

  if (isEditEnabled && document.getElementsByClassName('p-inputtext p-component').length > 0) {
    document.getElementsByClassName('p-inputtext p-component')[0].focus();
  }

  const checkDuplicates = (header, idx) => {
    const schemas = [...datasetSchemaInfo];
    const repeat = schemas.filter(title => TextUtils.areEquals(title.schemaName, header));
    return repeat.length > 0 && idx !== repeat[0].schemaIndex;
  };

  const checkInvalidCharacters = name => {
    const invalidCharsRegex = new RegExp(/[^a-zA-Z0-9_-\s()]/);
    return invalidCharsRegex.test(name);
  };

  const onEditorKeyChange = (event, index) => {
    if (event.key === 'Enter') {
      if (buttonsTitle !== '') {
        onInputSave(event.target.value, index);
      } else {
        setErrorDialogData({ isVisible: true, message: resources.messages['emptyDatasetSchema'] });
        document.getElementsByClassName('p-inputtext p-component')[0].focus();
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
        let hasErrors = false;
        if (checkDuplicates(title, index)) {
          setErrorDialogData({
            isVisible: true,
            message: resources.messages['duplicateSchemaError']
          });
          hasErrors = true;
        } else if (title.length > 250) {
          setErrorDialogData({
            isVisible: true,
            message: resources.messages['tooLongSchemaNameError']
          });
          hasErrors = true;
        } else if (checkInvalidCharacters(title)) {
          setErrorDialogData({
            isVisible: true,
            message: resources.messages['invalidCharactersSchemaError']
          });
          hasErrors = true;
        }
        if (hasErrors) {
          document.getElementsByClassName('p-inputtext p-component')[0].focus();
          return { correct: false, originalSchemaName: initialValue, wrongName: title };
        } else {
          onSaveName(title, index) && setIsEditEnabled(false) && setInitialValue(buttonsTitle);
        }
      } else {
        setIsEditEnabled(false);
      }
    } else {
      setErrorDialogData({ isVisible: true, message: resources.messages['emptyDatasetSchema'] });
      document.getElementsByClassName('p-inputtext p-component')[0].focus();
    }
  };

  const onWheelClick = event => {
    if (event.button === 1) {
      window.open(onWheel);
    }
  };

  const defaultBigButton = (
    <>
      <div
        className={`${styles.bigButton} ${styles.defaultBigButton} ${styles[buttonClass]} ${helpClassName} ${
          !enabled && styles.bigButtonDisabled
        }`}>
        <span onClick={() => handleRedirect()} onMouseDown={event => onWheelClick(event)} data-tip data-for={caption}>
          <FontAwesomeIcon icon={AwesomeIcons(buttonIcon)} className={styles[buttonIconClass]} />
        </span>
        {model && !isEmpty(model) && (
          <DropdownButton
            icon="caretDown"
            model={designModel}
            buttonStyle={{ position: 'absolute', bottom: '-5px', right: '0px' }}
            iconStyle={{ fontSize: '1.8rem' }}
          />
        )}
        {infoStatus &&
          (infoStatusIcon ? (
            <Icon style={{ position: 'absolute', top: '0', right: '0', fontSize: '1.8rem' }} icon="cloudUpload" />
          ) : (
            <p
              style={{
                position: 'absolute',
                top: '0',
                right: '0',
                fontSize: '1.1rem',
                margin: '0 0.5rem',
                fontWeight: '600'
              }}>
              {resources.messages['new'].toUpperCase()}
            </p>
          ))}
      </div>
      {!isUndefined(isEditEnabled) && isEditEnabled ? (
        <InputText
          autoFocus={true}
          className={`${styles.inputText}`}
          key={index}
          onBlur={e => {
            onInputSave(e.target.value, index);
          }}
          onChange={e => setButtonsTitle(e.target.value)}
          onFocus={e => {
            e.preventDefault();
            onEditorValueFocus(e.target.value);
          }}
          onKeyDown={e => {
            if (!regex.test(e.key) || e.key === 'Dead') {
              e.preventDefault();
              return false;
            } else {
              onEditorKeyChange(e, index);
            }
          }}
          placeholder={placeholder}
          value={!isUndefined(buttonsTitle) ? buttonsTitle : caption}
        />
      ) : (
        <>
          <p
            data-tip
            data-for={tooltipId}
            className={styles.caption}
            onDoubleClick={
              dataflowStatus === config.dataflowStatus.DESIGN && canEditName ? onEnableSchemaNameEdit : null
            }>
            {!isUndefined(buttonsTitle) ? buttonsTitle : caption}
          </p>
          {buttonsTitle.length > 60 && (
            <ReactTooltip effect="solid" id={tooltipId} place="top" className={styles.tooltip}>
              {!isUndefined(buttonsTitle) ? buttonsTitle : caption}
            </ReactTooltip>
          )}
        </>
      )}
    </>
  );

  const menuBigButton = (
    <>
      <div className={`${styles.bigButton} ${styles.menuBigButton} ${styles[buttonClass]} ${helpClassName}`}>
        <span onClick={event => menuBigButtonRef.current.show(event)}>
          <FontAwesomeIcon icon={AwesomeIcons(buttonIcon)} className={styles[buttonIconClass]} />
        </span>
        <DropDownMenu ref={menuBigButtonRef} model={model} />
      </div>
      <p className={styles.caption}>{caption}</p>
    </>
  );

  const buttons = {
    defaultBigButton,
    menuBigButton
  };

  return (
    <>
      <div className={`${styles.datasetItem} ${!enabled && styles.datasetItemDisabled}`}>{buttons[layout]}</div>
      {tooltip && (
        <ReactTooltip effect="solid" id={caption} place="top">
          {tooltip}
        </ReactTooltip>
      )}
    </>
  );
};
