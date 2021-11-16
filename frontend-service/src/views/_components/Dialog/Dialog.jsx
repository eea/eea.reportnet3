import { useContext, useEffect, useState } from 'react';

import uniqueId from 'lodash/uniqueId';

import styles from './Dialog.module.scss';

import { Dialog as PrimeDialog } from 'primereact/dialog';

import { DialogContext } from 'views/_functions/Contexts/DialogContext';

export const Dialog = ({
  blockScroll = true,
  children,
  className,
  closeOnEscape = false,
  contentStyle,
  dialogType,
  focusOnShow = true,
  footer,
  header,
  onHide,
  style,
  visible,
  zIndex = 5000
}) => {
  const id = uniqueId();
  const dialogContext = useContext(DialogContext);
  const [dialogId, setDialogId] = useState('');
  const [maskStyle, setMaskStyle] = useState({
    display: visible ? 'flex' : 'none',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: zIndex
  });
  const [dialogClass, setDialogClass] = useState(styles.dialog_mask_wrapper);

  const dialogStyle = {
    top: 'auto',
    left: 'auto',
    zIndex: zIndex
  };

  useEffect(() => {
    if (dialogType) {
      setDialogClass(`${styles.dialog_mask_wrapper} ${styles[dialogType]}`);
    }
  }, []);

  useEffect(() => {
    const newDialogId = uniqueId();
    setDialogId(newDialogId);
    dialogContext.add(newDialogId);
    return () => {
      dialogContext.remove(dialogId);
      restoreBodyScroll();
    };
  }, []);

  const restoreBodyScroll = () => {
    if (dialogContext.open.length === 0) {
      document.body.style.overflow = 'hidden auto';
    }
  };

  useEffect(() => {
    const body = document.querySelector('body');
    visible && (body.style.overflow = 'hidden');
    return () => {
      body.style.overflow = 'auto';
    };
  }, [visible]);

  useEffect(() => {
    if (dialogContext.open.indexOf(dialogId) >= 0) {
      setMaskStyle({
        ...maskStyle,
        zIndex: zIndex + dialogContext.open.indexOf(dialogId)
      });
    }
  }, [dialogContext.open]);

  return (
    <div className={dialogClass} style={maskStyle}>
      <PrimeDialog
        blockScroll={blockScroll}
        className={className}
        closeOnEscape={closeOnEscape}
        contentStyle={contentStyle}
        focusOnShow={focusOnShow}
        footer={footer}
        header={header}
        id={id}
        maximizable={false}
        onHide={onHide}
        style={style ? { ...style, ...dialogStyle } : { ...dialogStyle, width: '50vw' }}
        visible={visible}>
        {children}
      </PrimeDialog>
      <label className="srOnly" id={id}>
        {header}
      </label>
    </div>
  );
};
