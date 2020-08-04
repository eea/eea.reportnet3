import React, { useContext, useState } from 'react';

import styles from './AttachmentEditor.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Chips } from 'ui/views/_components/Chips';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const AttachmentEditor = ({
  isAttachmentEditorVisible,
  onCancelSaveAttachment,
  onSaveAttachment,
  selectedAttachment,
  type
}) => {
  const resources = useContext(ResourcesContext);
  const [validExtensions, setValidExtensionsItems] = useState(selectedAttachment.validExtensions);
  const [maxSize, setMaxSize] = useState(selectedAttachment.maxSize);

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
          onSaveAttachment(validExtensions);
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
      <div onPaste={onPasteChips}>
        <div className={styles.inputTitleWrapper}>
          <span
            dangerouslySetInnerHTML={{
              __html: resources.messages['codelistEditorItemsMessage']
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
            __html: resources.messages['codelistEditorItemsMessage']
          }}></span>
      }
      <InputText id="fileMaxSize" onChange={event => setMaxSize(event.target.value)} value={maxSize} />
      <span>{resources.messages['Mb']}</span>
    </Dialog>
  );
};

export { AttachmentEditor };
