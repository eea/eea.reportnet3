import { useContext, useEffect, useState } from 'react';

import styles from './AttachmentEditor.module.scss';

import { Button } from 'views/_components/Button';
import { Chips } from 'views/_components/Chips';
import { Dialog } from 'views/_components/Dialog';
import { InputText } from 'views/_components/InputText';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const AttachmentEditor = ({
  isAttachmentEditorVisible,
  onCancelSaveAttachment,
  onSaveAttachment,
  selectedAttachment
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const [validExtensions, setValidExtensionsItems] = useState(selectedAttachment.validExtensions || []);
  const [maxSize, setMaxSize] = useState(selectedAttachment.maxSize || 0);
  const [isVisible, setIsVisible] = useState(isAttachmentEditorVisible);
  const [isSaved, setIsSaved] = useState(false);

  useEffect(() => {
    if (isSaved) {
      setIsVisible(false);
    }
  }, [isSaved]);

  useEffect(() => {
    if (!isVisible && isSaved) {
      onSaveAttachment({ validExtensions, maxSize });
      setValidExtensionsItems([]);
      setMaxSize(0);
    }
  }, [isVisible]);

  const onMaxSizeChange = size => {
    size = size.toString();
    if ((size.match(/\./g) || []).length === 0) {
      size = size.substring(0, 2);
    } else if ((size.match(/\./g) || []).length === 1) {
      const splittedNumber = size.split('.');
      size = `${splittedNumber[0] === '' ? '0' : splittedNumber[0]}.${splittedNumber[1].substring(0, 2)}`;
    } else {
      size = size.substring(0, size.length - 1);
    }

    setMaxSize(size > 20 ? 20 : size < 0 ? 0 : size);
  };

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
      <Button icon="check" label={resourcesContext.messages['save']} onClick={() => setIsSaved(true)} />
      <Button
        className="p-button-secondary button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
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
      <div className={styles.validExtensionsWrapper} onPaste={onPasteChips}>
        <div className={styles.inputTitleWrapper}>
          <span
            dangerouslySetInnerHTML={{
              __html: resourcesContext.messages['attachmentEditorItemsMessage']
            }}></span>
        </div>
        <Chips
          checkForDuplicates={true}
          clearOnPaste={true}
          deleteWhiteSpaces={true}
          errorMessage={resourcesContext.messages['duplicatedItem']}
          forbiddenChar={true}
          inputClassName={styles.validExtensionsChips}
          onChange={e => setValidExtensionsItems(e.value)}
          showErrorMessage={true}
          tooltip={resourcesContext.messages['validExtensionEditor']}
          tooltipOptions={{ position: 'bottom' }}
          value={validExtensions}></Chips>
      </div>
    );
  };

  return (
    isVisible && (
      <Dialog
        blockScroll={false}
        contentStyle={{ overflow: 'auto' }}
        focusOnShow={false}
        footer={attachmentDialogFooter}
        header={resourcesContext.messages['validExtensionEditor']}
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
              __html: resourcesContext.messages['attachmentEditorSizeMessage']
            }}></span>
        }
        <div className={styles.maxSizeWrapper}>
          <Button
            className={`secondary`}
            icon="minus"
            onClick={() => onMaxSizeChange((Number(maxSize) - 0.25).toString())}
            tooltip={resourcesContext.messages['minusFileSize']}
            tooltipOptions={{ position: 'bottom' }}
          />
          <InputText
            className={styles.maxSizeInput}
            id="maxFileSize"
            keyfilter="pnum"
            onChange={e => onMaxSizeChange(e.target.value)}
            value={maxSize}
          />
          <Button
            className={`secondary`}
            icon="plus"
            onClick={() => onMaxSizeChange((Number(maxSize) + 0.25).toString())}
            tooltip={resourcesContext.messages['plusFileSize']}
            tooltipOptions={{ position: 'bottom' }}
          />
          <label className="srOnly" htmlFor="maxFileSize">
            {resourcesContext.messages['supportedFileAttachmentsMaxSizeTooltip']}
          </label>
          <span className={styles.mbSpan}>{`${resourcesContext.messages['MB']} (${
            isNaN(Number(maxSize)) ? 0 : Number(maxSize) * 1024
          } ${resourcesContext.messages['KB']})`}</span>
        </div>
      </Dialog>
    )
  );
};

export { AttachmentEditor };
