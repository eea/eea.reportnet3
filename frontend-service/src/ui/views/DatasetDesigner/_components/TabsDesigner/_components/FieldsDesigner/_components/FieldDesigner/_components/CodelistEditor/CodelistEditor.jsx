import React, { useContext, useState } from 'react';

import styles from './CodelistEditor.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Chips } from 'ui/views/_components/Chips';
import { Dialog } from 'ui/views/_components/Dialog';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const CodelistEditor = ({ isCodelistEditorVisible, onCancelSaveCodelist, onSaveCodelist, selectedCodelist, type }) => {
  const resources = useContext(ResourcesContext);
  const [codelistItems, setCodelistItems] = useState(selectedCodelist);
  const [isVisible, setIsVisible] = useState(isCodelistEditorVisible);
  console.log({ type });
  const onPasteChips = event => {
    if (event) {
      const clipboardData = event.clipboardData;
      const pastedData = clipboardData.getData('Text');
      const inmCodelistItems = [...codelistItems];
      inmCodelistItems.push(...pastedData.split(',').filter(value => value.trim() !== ''));
      setCodelistItems([...new Set(inmCodelistItems)]);
    }
  };

  const codelistDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['save']}
        icon="check"
        onClick={() => {
          onSaveCodelist(codelistItems);
          setCodelistItems([]);
          setIsVisible(false);
        }}
      />
      <Button
        className="p-button-secondary"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => {
          onCancelSaveCodelist();
          setCodelistItems([]);
          setIsVisible(false);
        }}
      />
    </div>
  );

  const renderChips = () => {
    return (
      <div onPaste={onPasteChips}>
        <div className={styles.inputTitleWrapper}>
          <span>
            {type.toUpperCase() === 'SINGLE SELECT'
              ? resources.messages['codelistEditorItems']
              : resources.messages['multiselectCodelists']}
          </span>
          <span className={styles.subIndex}>{resources.messages['codelistEditorItemsMessage']}</span>
        </div>
        <Chips
          checkForDuplicates={true}
          clearOnPaste={true}
          deleteWhiteSpaces={true}
          errorMessage={resources.messages['duplicatedItem']}
          forbiddenCommas={true}
          inputClassName={styles.codelistChips}
          onChange={e => setCodelistItems(e.value)}
          showErrorMessage={true}
          tooltip={
            type.toUpperCase() === 'SINGLE SELECT'
              ? resources.messages['codelistEditorMessage']
              : resources.messages['multiselectCodelistEditorMessage']
          }
          tooltipOptions={{ position: 'bottom' }}
          value={codelistItems}></Chips>
      </div>
    );
  };

  return (
    <Dialog
      blockScroll={false}
      contentStyle={{ overflow: 'auto' }}
      closeOnEscape={false}
      focusOnShow={false}
      footer={codelistDialogFooter}
      header={
        type.toUpperCase() === 'SINGLE SELECT'
          ? resources.messages['codelistEditor']
          : resources.messages['multiselectCodelistEditor']
      }
      modal={true}
      onHide={() => {
        onCancelSaveCodelist();
        setIsVisible(false);
      }}
      style={{ width: '40%' }}
      visible={isVisible}
      zIndex={3003}>
      {renderChips()}
    </Dialog>
  );
};

export { CodelistEditor };
