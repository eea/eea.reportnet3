import React, { useContext, useState } from 'react';

import styles from './AttachmentEditor.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Chips } from 'ui/views/_components/Chips';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputNumber } from 'primereact/inputnumber';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const AttachmentEditor = ({
  isAttachmentEditorVisible,
  onCancelSaveAttachment,
  onSaveAttachment,
  selectedAttachment
}) => {
  const resources = useContext(ResourcesContext);
  const [validExtensions, setValidExtensionsItems] = useState(selectedAttachment.validExtensions || []);
  const [maxSize, setMaxSize] = useState(selectedAttachment.maxSize || 0);

  const [isVisible, setIsVisible] = useState(isAttachmentEditorVisible);

  const onPasteChips = event => {
    if (event) {
      const clipboardData = event.clipboardData;
      const pastedData = clipboardData.getData('Text');
      const inmvalidExtensions = [...validExtensions];
      inmvalidExtensions.push(...pastedData.split(',').filter(value => value.trim() !== ''));
      setValidExtensionsItems([...new Set(inmvalidExtensions.map(value => value.trim()))]);
    }
  };

  const attachmentDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['save']}
        icon="check"
        onClick={() => {
          onSaveAttachment({ validExtensions, maxSize });
          setValidExtensionsItems([]);
          setIsVisible(false);
        }}
      />
      <Button
        className="p-button-secondary"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => {
          onCancelSaveAttachment();
          setValidExtensionsItems([]);
          setIsVisible(false);
        }}
      />
    </div>
  );

  const renderChips = () => {
    return (
      <div onPaste={onPasteChips} className={styles.validExtensionsWrapper}>
        <div className={styles.inputTitleWrapper}>
          <span
            dangerouslySetInnerHTML={{
              __html: resources.messages['attachmentEditorItemsMessage']
            }}></span>
        </div>
        <Chips
          checkForDuplicates={true}
          clearOnPaste={true}
          deleteWhiteSpaces={true}
          errorMessage={resources.messages['duplicatedItem']}
          forbiddenCommas={true}
          inputClassName={styles.validExtensionsChips}
          onChange={e => setValidExtensionsItems(e.value)}
          showErrorMessage={true}
          tooltip={resources.messages['validExtensionEditor']}
          tooltipOptions={{ position: 'bottom' }}
          value={validExtensions}></Chips>
      </div>
    );
  };

  return (
    <Dialog
      blockScroll={false}
      contentStyle={{ overflow: 'auto' }}
      closeOnEscape={false}
      focusOnShow={false}
      footer={attachmentDialogFooter}
      header={resources.messages['validExtensionEditor']}
      modal={true}
      onHide={() => {
        onCancelSaveAttachment();
        setIsVisible(false);
      }}
      style={{ width: '40%' }}
      visible={isVisible}
      zIndex={3003}>
      {renderChips()}
      {
        <span
          dangerouslySetInnerHTML={{
            __html: resources.messages['attachmentEditorSizeMessage']
          }}></span>
      }
      <div className={styles.maxSizeWrapper}>
        <InputNumber
          // placeholder={resourcesContext.messages.value}
          buttonLayout="horizontal"
          decrementButtonIcon="pi pi-minus"
          format={false}
          incrementButtonIcon="pi pi-plus"
          max={20}
          min={0}
          // mode="decimal"
          onChange={e => setMaxSize(e.target.value)}
          showButtons
          step={0.25}
          value={maxSize}
        />
        <span className={styles.mbSpan}>{`${resources.messages['MB']} (${Number(maxSize) * 1024} ${
          resources.messages['KB']
        })`}</span>
      </div>
    </Dialog>
  );
};

export { AttachmentEditor };
