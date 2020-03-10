import React, { useContext, useState } from 'react';

// import styles from './CodelistEditor.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { ListBox } from './_components/ListBox';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const FKSelector = ({ isFKSelectorVisible, onCancelSaveFK, onSaveFK, selectedFK }) => {
  const resources = useContext(ResourcesContext);
  const [fk, setFK] = useState(selectedFK);
  const [isVisible, setIsVisible] = useState(isFKSelectorVisible);

  const fkSelectorDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['save']}
        icon="check"
        onClick={() => {
          onSaveFK(fk);
          setIsVisible(false);
        }}
      />
      <Button
        className="p-button-secondary"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => {
          onCancelSaveFK();
          setIsVisible(false);
        }}
      />
    </div>
  );

  const renderFKSelector = () => {
    return (
      <React.Fragment>
        <ListBox
          value={'RM'}
          options={[
            { name: 'New York', code: 'NY' },
            { name: 'Rome', code: 'RM' },
            { name: 'London', code: 'LDN' },
            { name: 'Istanbul', code: 'IST' },
            { name: 'Paris', code: 'PRS' }
          ]}
          onChange={e => {
            console.log(e.value);
            setFK(e.value.name);
          }}
          optionLabel="name"></ListBox>
        <span>{fk}</span>
      </React.Fragment>
    );
  };

  return (
    <Dialog
      blockScroll={false}
      contentStyle={{ overflow: 'auto' }}
      closeOnEscape={false}
      footer={fkSelectorDialogFooter}
      header={resources.messages['fkSelector']}
      modal={true}
      onHide={() => setIsVisible(false)}
      style={{ width: '40%' }}
      visible={isVisible}
      zIndex={3003}>
      {renderFKSelector()}
    </Dialog>
  );
};

export { FKSelector };
