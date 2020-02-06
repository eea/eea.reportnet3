import React, { useContext, forwardRef } from 'react';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const ConfirmDialog = forwardRef((props, _) => {
  const {
    children,
    className,
    dialogStyle,
    divRef,
    hasPasteOption = false,
    header,
    iconCancel,
    iconConfirm,
    isPasting,
    disabledConfirm,
    labelCancel,
    labelConfirm,
    maximizable,
    onConfirm,
    onHide,
    onPaste,
    onPasteAsync,
    styleConfirm,
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
          label={labelConfirm}
          icon={!isPasting ? (iconConfirm ? iconConfirm : 'check') : 'spinnerAnimate'}
          onClick={onConfirm}
          disabled={disabledConfirm || isPasting}
          style={styleConfirm}
        />
      ) : (
        <Button
          label={labelConfirm}
          icon={iconConfirm ? iconConfirm : 'check'}
          onClick={onConfirm}
          disabled={disabledConfirm}
          style={styleConfirm}
        />
      )}
      <Button
        className="p-button-secondary"
        icon={iconCancel ? iconCancel : 'cancel'}
        label={labelCancel}
        onClick={onHide}
      />
    </div>
  );

  return (
    <div onPaste={onPaste} ref={divRef}>
      <Dialog
        className={className}
        focusOnShow={true}
        footer={footer}
        header={header}
        maximizable={maximizable}
        onHide={onHide}
        style={dialogStyle ? dialogStyle : { minWidth: '50vw', maxWidth: '80vw', maxHeight: '80vh' }}
        visible={visible}>
        {children}
      </Dialog>
    </div>
  );
});

export { ConfirmDialog };
