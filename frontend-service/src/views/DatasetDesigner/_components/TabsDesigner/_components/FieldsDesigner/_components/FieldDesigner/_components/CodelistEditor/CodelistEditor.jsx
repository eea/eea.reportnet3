import { useContext, useEffect, useState } from 'react';

import styles from './CodelistEditor.module.scss';

import { Button } from 'views/_components/Button';
import { Chips } from 'views/_components/Chips';
import { Dialog } from 'views/_components/Dialog';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'repositories/_utils/TextUtils';

const CodelistEditor = ({ isCodelistEditorVisible, onCancelSaveCodelist, onSaveCodelist, selectedCodelist, type }) => {
  const resources = useContext(ResourcesContext);
  const [codelistItems, setCodelistItems] = useState(selectedCodelist);
  const [isVisible, setIsVisible] = useState(isCodelistEditorVisible);
  const [isSaved, setIsSaved] = useState(false);

  const onPasteChips = event => {
    if (event) {
      const clipboardData = event.clipboardData;
      const pastedData = clipboardData.getData('Text');
      const inmCodelistItems = [...codelistItems];
      inmCodelistItems.push(...pastedData.split(';').filter(value => value.trim() !== ''));
      setCodelistItems([...new Set(inmCodelistItems.map(value => value.trim()))]);
    }
  };

  useEffect(() => {
    if (isSaved) {
      setIsVisible(false);
    }
  }, [isSaved]);

  useEffect(() => {
    if (!isVisible && isSaved) {
      onSaveCodelist(codelistItems);
      setCodelistItems([]);
    }
  }, [isVisible]);

  const codelistDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        icon="check"
        label={resources.messages['save']}
        onClick={() => {
          setIsSaved(true);
        }}
      />
      <Button
        className="p-button-secondary button-right-aligned"
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
          forbiddenChar={true}
          inputClassName={styles.codelistChips}
          name={resources.messages['multipleSingleMessage']}
          onChange={e => setCodelistItems(e.value)}
          pasteSeparator=";"
          showErrorMessage={true}
          tooltip={
            TextUtils.areEquals(type, 'SINGLE SELECT')
              ? resources.messages['codelistEditorMessage']
              : resources.messages['multiselectCodelistEditorMessage']
          }
          tooltipOptions={{ position: 'bottom' }}
          value={codelistItems}></Chips>
      </div>
    );
  };

  return (
    isVisible && (
      <Dialog
        blockScroll={false}
        contentStyle={{ overflow: 'auto' }}
        focusOnShow={false}
        footer={codelistDialogFooter}
        header={
          TextUtils.areEquals(type, 'SINGLE SELECT')
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
    )
  );
};

export { CodelistEditor };
