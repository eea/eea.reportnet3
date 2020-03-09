import React, { useContext, useState } from 'react';

import styles from './CodelistEditor.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Chips } from 'ui/views/_components/Chips';
import { Dialog } from 'ui/views/_components/Dialog';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const CodelistEditor = ({ isCodelistEditorVisible, onCancelSaveCodelist, onSaveCodelist, selectedCodelist }) => {
  const resources = useContext(ResourcesContext);
  const [codelistItems, setCodelistItems] = useState(selectedCodelist);
  const [isVisible, setIsVisible] = useState(isCodelistEditorVisible);

  const codelistDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['save']}
        icon="check"
        onClick={() => {
          onSaveCodelist(codelistItems);
          setIsVisible(false);
        }}
      />
      <Button
        className="p-button-secondary"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => {
          onCancelSaveCodelist();
          setIsVisible(false);
        }}
      />
    </div>
  );

  const renderChips = () => {
    return (
      <React.Fragment>
        <div className={styles.inputTitleWrapper}>
          <span>{resources.messages['codelistEditorItems']} </span>
          <span className={styles.subIndex}>{resources.messages['codelistEditorItemsMessage']}</span>
        </div>
        <Chips
          checkForDuplicates={true}
          inputClassName={styles.codelistChips}
          onChange={e => setCodelistItems(e.value)}
          tooltip={resources.messages['codelistEditorMessage']}
          tooltipOptions={{ position: 'bottom' }}
          value={codelistItems}></Chips>
      </React.Fragment>
    );
  };

  return (
    <Dialog
      blockScroll={false}
      contentStyle={{ overflow: 'auto' }}
      closeOnEscape={false}
      footer={codelistDialogFooter}
      header={resources.messages['codelistEditor']}
      modal={true}
      onHide={() => setIsVisible(false)}
      style={{ width: '40%' }}
      visible={isVisible}
      zIndex={3003}>
      {renderChips()}
    </Dialog>
  );
};

export { CodelistEditor };
