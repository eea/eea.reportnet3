import { useContext, useEffect, useState } from 'react';

import styles from './CodelistEditor.module.scss';

import { Button } from 'views/_components/Button';
import { Chips } from 'views/_components/Chips';
import { Dialog } from 'views/_components/Dialog';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'repositories/_utils/TextUtils';

const CodelistEditor = ({ isCodelistEditorVisible, onCancelSaveCodelist, onSaveCodelist, selectedCodelist, type }) => {
  const resourcesContext = useContext(ResourcesContext);

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
        label={resourcesContext.messages['save']}
        onClick={() => {
          setIsSaved(true);
        }}
      />
      <Button
        className="p-button-secondary button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
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
              __html: resourcesContext.messages['codelistEditorItemsMessage']
            }}></span>
        </div>
        <Chips
          checkForDuplicates={true}
          clearOnPaste={true}
          deleteWhiteSpaces={true}
          errorMessage={resourcesContext.messages['duplicatedItem']}
          forbiddenChar={true}
          inputClassName={styles.codelistChips}
          name={resourcesContext.messages['multipleSingleMessage']}
          onChange={e => setCodelistItems(e.value)}
          pasteSeparator=";"
          showErrorMessage={true}
          tooltip={
            TextUtils.areEquals(type, 'SINGLE SELECT')
              ? resourcesContext.messages['codelistEditorMessage']
              : resourcesContext.messages['multiselectCodelistEditorMessage']
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
            ? resourcesContext.messages['codelistEditor']
            : resourcesContext.messages['multiselectCodelistEditor']
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
