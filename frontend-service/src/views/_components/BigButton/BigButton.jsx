import { Fragment, useContext, useEffect, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';
import uniqueId from 'lodash/uniqueId';

import styles from './BigButton.module.scss';

import { config } from 'conf';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { DropdownButton } from 'views/_components/DropdownButton';
import { DropDownMenu } from 'views/_components/DropdownButton/_components/DropDownMenu';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Icon } from 'views/_components/Icon';
import { InputText } from 'views/_components/InputText';
import ReactTooltip from 'react-tooltip';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const BigButton = ({
  buttonClass,
  buttonIcon,
  buttonIconClass,
  canEditName = true,
  caption,
  dataflowStatus,
  datasetSchemaInfo,
  dataProviderId,
  enabled = true,
  handleRedirect = () => {},
  helpClassName,
  index,
  infoStatus,
  infoStatusIcon,
  isRestrictFromPublicUpdating,
  layout,
  manageDialogs = () => {},
  model,
  onSaveName,
  onWheel,
  placeholder,
  restrictFromPublicAccess,
  restrictFromPublicInfo,
  restrictFromPublicStatus,
  setSelectedRepresentative = () => {},
  setErrorDialogData,
  tooltip
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const regex = new RegExp(/[a-zA-Z0-9_-\s()]/);

  const [buttonsTitle, setButtonsTitle] = useState('');
  const [initialValue, setInitialValue] = useState();
  const [isEditEnabled, setIsEditEnabled] = useState(false);

  const menuBigButtonRef = useRef();
  const tooltipId = uniqueId();

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
        setErrorDialogData({ isVisible: true, message: resourcesContext.messages['emptyDatasetSchema'] });
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

  const onEditorValueFocus = (value, index) => {
    if (!checkDuplicates(value, index) && !checkInvalidCharacters(value) && value.length <= 250) {
      setInitialValue(!isEmpty(value) ? value : initialValue);
    }
  };

  const onEnableSchemaNameEdit = () => {
    setIsEditEnabled(true);
  };

  const designModel =
    !isUndefined(model) &&
    model.map(button => {
      if (button.label === resourcesContext.messages['rename']) {
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
            message: resourcesContext.messages['duplicateSchemaError']
          });
          hasErrors = true;
        } else if (title.length > 250) {
          setErrorDialogData({
            isVisible: true,
            message: resourcesContext.messages['tooLongSchemaNameError']
          });
          hasErrors = true;
        } else if (checkInvalidCharacters(title)) {
          setErrorDialogData({
            isVisible: true,
            message: resourcesContext.messages['invalidCharactersSchemaError']
          });
          hasErrors = true;
        }
        if (hasErrors) {
          document.getElementsByClassName('p-inputtext p-component')[0].focus();
          return { correct: false, originalSchemaName: initialValue, wrongName: title };
        } else {
          onSaveName(title.trim(), index) && setIsEditEnabled(false) && setInitialValue(buttonsTitle);
        }
      } else {
        setIsEditEnabled(false);
      }
    } else {
      setErrorDialogData({ isVisible: true, message: resourcesContext.messages['emptyDatasetSchema'] });
      document.getElementsByClassName('p-inputtext p-component')[0].focus();
    }
  };

  const onWheelClick = event => {
    if (event.button === 1) {
      window.open(onWheel);
    }
  };

  const getRestrictFromPublicIcon = () => {
    if (isRestrictFromPublicUpdating) {
      return AwesomeIcons('spinner');
    } else {
      return AwesomeIcons(restrictFromPublicStatus ? 'eyeSlash' : 'eye');
    }
  };

  const defaultBigButton = (
    <Fragment>
      <div
        className={`${styles.bigButton} ${styles.defaultBigButton} ${styles[buttonClass]} ${helpClassName} ${
          !enabled && styles.bigButtonDisabled
        }`}>
        <span data-for={caption} data-tip onClick={() => handleRedirect()} onMouseDown={event => onWheelClick(event)}>
          <FontAwesomeIcon className={styles[buttonIconClass]} icon={AwesomeIcons(buttonIcon)} role="presentation" />
        </span>
        {model && !isEmpty(model) && (
          <DropdownButton
            buttonStyle={{ position: 'absolute', bottom: '-5px', right: '0px' }}
            icon="caretDown"
            iconStyle={{ fontSize: '1.8rem' }}
            model={designModel}
          />
        )}
        {infoStatus &&
          (infoStatusIcon ? (
            <Icon
              className={styles.notClickableIcon}
              icon="checkCircle"
              style={{ position: 'absolute', top: '0', right: '0', fontSize: '1.8rem' }}
            />
          ) : (
            <p
              className={styles.notClickableIcon}
              style={{
                position: 'absolute',
                top: '0',
                right: '0',
                fontSize: '1.1rem',
                margin: '0 0.5rem',
                fontWeight: '600'
              }}>
              {resourcesContext.messages['new'].toUpperCase()}
            </p>
          ))}
        {restrictFromPublicInfo && (
          <FontAwesomeIcon
            className={`${!restrictFromPublicAccess && styles.notClickableIcon} ${
              isRestrictFromPublicUpdating && 'fa-spin'
            }`}
            icon={getRestrictFromPublicIcon()}
            onClick={() => {
              restrictFromPublicAccess && manageDialogs('isRestrictFromPublicDialogVisible', true);
              setSelectedRepresentative(dataProviderId);
            }}
            style={{ position: 'absolute', top: '4px', left: '2px', fontSize: '1.2rem' }}
          />
        )}
      </div>
      {!isUndefined(isEditEnabled) && isEditEnabled ? (
        <InputText
          autoFocus={true}
          className={`${styles.inputText}`}
          id="editName"
          key={index}
          name={resourcesContext.messages['editDatasetSchemaName']}
          onBlur={e => {
            onInputSave(e.target.value.trim(), index);
            setButtonsTitle(e.target.value.trim());
          }}
          onChange={e => setButtonsTitle(e.target.value)}
          onFocus={e => {
            e.preventDefault();
            onEditorValueFocus(e.target.value, index);
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
        <Fragment>
          <p
            className={styles.caption}
            data-for={tooltipId}
            data-tip
            onDoubleClick={
              dataflowStatus === config.dataflowStatus.DESIGN && canEditName ? onEnableSchemaNameEdit : null
            }>
            {!isUndefined(buttonsTitle) ? buttonsTitle : caption}
          </p>
          {!isUndefined(buttonsTitle) && buttonsTitle.length > 60 && (
            <ReactTooltip border={true} className={styles.tooltip} effect="solid" id={tooltipId} place="top">
              {!isUndefined(buttonsTitle) ? buttonsTitle : caption}
            </ReactTooltip>
          )}
        </Fragment>
      )}
    </Fragment>
  );

  const menuBigButton = (
    <Fragment>
      <div className={`${styles.bigButton} ${styles.menuBigButton} ${styles[buttonClass]} ${helpClassName}`}>
        <span onClick={event => menuBigButtonRef.current.show(event)}>
          <FontAwesomeIcon className={styles[buttonIconClass]} icon={AwesomeIcons(buttonIcon)} role="presentation" />
        </span>
        <DropDownMenu model={model} ref={menuBigButtonRef} />
      </div>
      <p className={styles.caption}>{caption}</p>
    </Fragment>
  );

  const buttons = {
    defaultBigButton,
    menuBigButton
  };

  return (
    <Fragment>
      <div className={`${styles.datasetItem} ${!enabled && styles.datasetItemDisabled}`}>{buttons[layout]}</div>
      {tooltip && (
        <ReactTooltip border={true} effect="solid" id={caption} place="top">
          {tooltip}
        </ReactTooltip>
      )}
    </Fragment>
  );
};
