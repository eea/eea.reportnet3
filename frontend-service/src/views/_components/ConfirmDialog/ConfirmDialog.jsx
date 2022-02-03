import { forwardRef } from 'react';

import styles from './ConfirmDialog.module.scss';

import ReactTooltip from 'react-tooltip';

import isUndefined from 'lodash/isUndefined';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';

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
    header,
    iconCancel,
    iconConfirm,
    labelCancel,
    labelConfirm,
    onConfirm,
    onHide,
    visible
  } = props;

  const onKeyPress = event => {
    if (event.key === 'Enter') {
      event.preventDefault();
      onConfirm();
    }
  };

  const footer = (
    <div>
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
    <div className={styles.confirmDialog} onKeyPress={!disabledConfirm ? onKeyPress : null} ref={divRef}>
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
