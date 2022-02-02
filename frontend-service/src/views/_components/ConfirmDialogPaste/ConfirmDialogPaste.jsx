import { useContext, forwardRef } from 'react';

import styles from './ConfirmDialogPaste.module.scss';

import isUndefined from 'lodash/isUndefined';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const ConfirmDialogPaste = forwardRef((props, _) => {
  const {
    children,
    className,
    classNameCancel,
    dialogStyle,
    disabledConfirm,
    divRef,
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
    const isChromium = window.chrome !== null;
    const isChromiumNoUndefined = typeof window.chrome !== 'undefined';
    const winNav = window.navigator;
    const vendorGoogle = winNav.vendor === 'Google Inc.';
    const isOpera = typeof window.opr !== 'undefined';
    const isIEedge = winNav.userAgent.indexOf('Edge') > -1;
    const isIOSChrome = winNav.userAgent.match('CriOS');

    return isIOSChrome || (isChromium && isChromiumNoUndefined && vendorGoogle && !isOpera && !isIEedge);
  };

  const isHTTPS = () => window.location.protocol === 'https:';

  const onKeyPress = event => {
    if (event.key === 'Enter') {
      event.preventDefault();
      onConfirm();
    }
  };

  const footer = (
    <div>
      {isHTTPS() ? (
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
      <Button
        className={`p-button-primary ${disabledConfirm || isPasting ? null : 'p-button-animated-blink'}`}
        disabled={disabledConfirm || isPasting}
        icon={!isPasting ? (iconConfirm ? iconConfirm : 'check') : 'spinnerAnimate'}
        label={labelConfirm}
        onClick={onConfirm}
      />
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
