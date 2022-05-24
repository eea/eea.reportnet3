import { useEffect, useState } from 'react';

import { useRecoilState } from 'recoil';
import uniqueId from 'lodash/uniqueId';

import styles from './Dialog.module.scss';

import { Dialog as PrimeDialog } from 'primereact/dialog';

import { dialogsStore } from 'views/_components/Dialog/_functions/Stores/dialogsStore';

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

  const [openedDialogs, setOpenedDialogs] = useRecoilState(dialogsStore);

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
    setOpenedDialogs(prev => [...prev, newDialogId]);
    return () => {
      onDialogUnmount(dialogId);
    };
  }, []);

  useEffect(() => {
    const body = document.querySelector('body');
    visible && (body.style.overflow = 'hidden');
  }, [visible]);

  useEffect(() => {
    if (openedDialogs.indexOf(dialogId) >= 0) {
      setMaskStyle({
        ...maskStyle,
        zIndex: zIndex + openedDialogs.indexOf(dialogId)
      });
    }
  }, [openedDialogs]);

  const onDialogUnmount = () => {
    const filteredDialogs = openedDialogs.filter(dialog => dialog !== dialogId);
    setOpenedDialogs(filteredDialogs);
    restoreBodyScroll();
  };

  const restoreBodyScroll = () => {
    if (openedDialogs.length === 0) {
      document.body.style.overflow = 'hidden auto';
    }
  };

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
