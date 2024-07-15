import { useEffect, useState } from 'react';

import { useRecoilState } from 'recoil';
import uniqueId from 'lodash/uniqueId';

import styles from './Dialog.module.scss';

import { Dialog as PrimeDialog } from 'primereact/dialog';

import { TabMenu } from 'views/Dataflows/_components/TabMenu';

import { dialogsStore } from 'views/_components/Dialog/_functions/Stores/dialogsStore';

export const Dialog = ({
  activeIndex,
  blockScroll = true,
  children,
  className,
  closeOnEscape = false,
  contentStyle,
  dialogType,
  focusOnShow = true,
  footer,
  header,
  isJobsStatusesDialog,
  onHide,
  style,
  tabChange,
  tabMenuItems,
  visible,
  zIndex = 5000
}) => {
  const id = uniqueId();

  const [openedDialogs, setOpenedDialogs] = useRecoilState(dialogsStore);

  const [dialogClass, setDialogClass] = useState(styles.dialog_mask_wrapper);
  const [dialogId, setDialogId] = useState('');
  const [maskStyle, setMaskStyle] = useState({
    display: visible ? 'flex' : 'none',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: zIndex
  });

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
        {isJobsStatusesDialog && (
          <div className={styles.tabMenu}>
            <TabMenu
              activeIndex={activeIndex}
              model={tabMenuItems}
              onTabChange={event => tabChange(event.index, event.value)}
            />
          </div>
        )}
        {children}
      </PrimeDialog>
      <label className="srOnly" id={id}>
        {header}
      </label>
    </div>
  );
};
