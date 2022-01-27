import { Fragment, useContext, forwardRef } from 'react';

import styles from './ConfirmDialog.module.scss';

import ReactTooltip from 'react-tooltip';

import isUndefined from 'lodash/isUndefined';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const ConfirmDialog = forwardRef((props, _) => {
  const {
    children,
    className,
    classNameCancel,
    classNameConfirm,
    confirmTooltip,
    dialogStyle,
    disabledConfirm,
    divRef,
    footerAddon = null,
    hasPasteOption = false,
    header,
    iconCancel,
    iconConfirm,
    isPasting,
    labelCancel,
    labelConfirm,
    onConfirm,
    onHide,
    onPaste,
    onPasteAsync,
    visible
  } = props;
  const resourcesContext = useContext(ResourcesContext);

  const isChrome = () => {
    const isChromium = window.chrome;
    const winNav = window.navigator;
    const vendorName = winNav.vendor;
    const isOpera = typeof window.opr !== 'undefined';
    const isIEedge = winNav.userAgent.indexOf('Edge') > -1;
    const isIOSChrome = winNav.userAgent.match('CriOS');

    if (isIOSChrome) {
      return true;
    } else if (
      isChromium !== null &&
      typeof isChromium !== 'undefined' &&
      vendorName === 'Google Inc.' &&
      isOpera === false &&
      isIEedge === false
    ) {
      return true;
    } else {
      return false;
    }
  };

  const isHTTPS = () => {
    return window.location.protocol === 'https:';
  };

  const onKeyPress = event => {
    if (event.key === 'Enter') {
      event.preventDefault();
      onConfirm();
    }
  };

  const footer = (
    <div>
      {hasPasteOption && isHTTPS() ? (
        <Button
          disabled={!isChrome() || isPasting}
          icon={!isPasting ? 'clipboard' : 'spinnerAnimate'}
          label={resourcesContext.messages['paste']}
          onClick={async () => {
            onPasteAsync();
          }}
          style={{ float: 'left' }}
          tooltip={!isChrome() ? resourcesContext.messages['pasteDisableButtonMessage'] : null}
        />
      ) : null}
      {hasPasteOption ? (
        <Button
          className={`p-button-primary ${disabledConfirm || isPasting ? null : 'p-button-animated-blink'}`}
          disabled={disabledConfirm || isPasting}
          icon={!isPasting ? (iconConfirm ? iconConfirm : 'check') : 'spinnerAnimate'}
          label={labelConfirm}
          onClick={onConfirm}
        />
      ) : (
        <Fragment>
          {footerAddon}
          <span data-for="confirmTooltipId" data-tip>
            <Button
              className={`${
                !isUndefined(classNameConfirm)
                  ? classNameConfirm
                  : `p-button-primary ${!disabledConfirm ? 'p-button-animated-blink' : ''}`
              } ${!disabledConfirm ? 'p-button-animated-blink' : null}`}
              disabled={disabledConfirm}
              icon={iconConfirm ? iconConfirm : 'check'}
              label={labelConfirm}
              onClick={onConfirm}
            />
          </span>
          {confirmTooltip && (
            <ReactTooltip border={true} effect="solid" id="confirmTooltipId" place="top">
              {confirmTooltip}
            </ReactTooltip>
          )}
        </Fragment>
      )}
      <Button
        className={`${!isUndefined(classNameCancel) ? classNameCancel : 'p-button-secondary p-button-animated-blink'}`}
        icon={iconCancel ? iconCancel : 'cancel'}
        label={labelCancel}
        onClick={onHide}
        style={{ marginRight: '0' }}
      />
    </div>
  );

  return (
    <div
      className={styles.confirmDialog}
      onKeyPress={!disabledConfirm ? onKeyPress : null}
      onPaste={onPaste}
      ref={divRef}>
      <Dialog
        className={className}
        focusOnShow={true}
        footer={footer}
        header={header}
        onHide={onHide}
        style={
          dialogStyle
            ? { minWidth: '50vw', maxWidth: '80vw', maxHeight: '80vh', ...dialogStyle }
            : { minWidth: '50vw', maxWidth: '80vw', maxHeight: '80vh' }
        }
        visible={visible}>
        {children}
      </Dialog>
    </div>
  );
});
