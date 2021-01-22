import React, { useContext, forwardRef } from 'react';

import { isUndefined } from 'lodash';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const ConfirmDialog = forwardRef((props, _) => {
  const {
    children,
    className,
    classNameCancel,
    classNameConfirm,
    dialogStyle,
    divRef,
    footerAddon = null,
    hasPasteOption = false,
    header,
    iconCancel,
    iconConfirm,
    isDeleting,
    isPasting,
    disabledConfirm,
    labelCancel,
    labelConfirm,
    onConfirm,
    onHide,
    onPaste,
    onPasteAsync,
    visible
  } = props;
  const resources = useContext(ResourcesContext);

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
          label={resources.messages['paste']}
          onClick={async () => {
            onPasteAsync();
          }}
          style={{ float: 'left' }}
          tooltip={!isChrome() ? resources.messages['pasteDisableButtonMessage'] : null}
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
        <>
          {footerAddon}
          <Button
            className={`${
              !isUndefined(classNameConfirm) ? classNameConfirm : 'p-button-primary p-button-animated-blink'
            } ${!disabledConfirm ? 'p-button-animated-blink' : null}`}
            disabled={disabledConfirm || isDeleting}
            icon={!isDeleting ? (iconConfirm ? iconConfirm : 'check') : 'spinnerAnimate'}
            label={labelConfirm}
            onClick={onConfirm}
          />
        </>
      )}
      <Button
        className={`${!isUndefined(classNameCancel) ? classNameCancel : 'p-button-secondary p-button-animated-blink'}`}
        icon={iconCancel ? iconCancel : 'cancel'}
        label={labelCancel}
        onClick={onHide}
      />
    </div>
  );

  return (
    <div className="confirmDialog" onPaste={onPaste} ref={divRef} onKeyPress={!disabledConfirm ? onKeyPress : null}>
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

export { ConfirmDialog };
