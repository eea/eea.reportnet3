import React, { useRef } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import styles from './BigButton.module.css';

import { DropdownButton } from 'ui/views/_components/DropdownButton';
import { DropDownMenu } from 'ui/views/_components/DropdownButton/_components/DropDownMenu';
import { Icon } from 'ui/views/_components/Icon';
import { Menu } from 'primereact/menu';

export const BigButton = ({ layout, handleRedirect, model, caption, isReleased }) => {
  const newDatasetRef = useRef();
  const dataset = model ? (
    <>
      <div className={`${styles.bigButton} ${styles.dataset}`}>
        <a
          href="#"
          onClick={e => {
            e.preventDefault();
            handleRedirect();
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('dataset')} />
        </a>
        <DropdownButton
          icon="caretDown"
          model={model}
          buttonStyle={{ position: 'absolute', bottom: '-5px', right: '0px' }}
          iconStyle={{ fontSize: '1.8rem' }}
        />
        {isReleased && (
          <Icon style={{ position: 'absolute', top: '0', right: '0', fontSize: '1.8rem' }} icon="cloudUpload" />
        )}
      </div>
      <p className={styles.caption}>{caption}</p>
    </>
  ) : (
    <></>
  );
  const dashboard = (
    <>
      <div className={`${styles.bigButton} ${styles.dashboard}`}>
        <a
          href="#"
          onClick={e => {
            e.preventDefault();
            handleRedirect();
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('barChart')} />
        </a>
      </div>
      <p className={styles.caption}>{caption}</p>
    </>
  );
  const designDatasetSchema = model ? (
    <>
      <div className={`${styles.bigButton} ${styles.designDatasetSchema}`}>
        <a
          href="#"
          onClick={e => {
            e.preventDefault();
            handleRedirect();
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('pencilRuler')} />
        </a>
        <DropdownButton
          icon="caretDown"
          model={model}
          buttonStyle={{ position: 'absolute', bottom: '-5px', right: '0px' }}
          iconStyle={{ fontSize: '1.8rem' }}
        />
      </div>
      <p className={styles.caption}>{caption}</p>
    </>
  ) : (
    <></>
  );
  const documents = (
    <>
      <div className={`${styles.bigButton} ${styles.documents}`}>
        <a
          href="#"
          onClick={e => {
            e.preventDefault();
            handleRedirect();
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('file')} />
        </a>
      </div>
      <p className={styles.caption}>{caption}</p>
    </>
  );
  const newItem = (
    <>
      <div className={`${styles.bigButton} ${styles.newItem}`}>
        <a
          href="#"
          onClick={e => {
            e.preventDefault();
            newDatasetRef.current.show(e);
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('plus')} />
        </a>
        <DropDownMenu ref={newDatasetRef} model={model} />
      </div>
      <p className={styles.caption}>{caption}</p>
    </>
  );
  const buttons = {
    dataset,
    dashboard,
    designDatasetSchema,
    documents,
    newItem
  };
  return <div className={`${styles.datasetItem}`}>{buttons[layout]}</div>;
};
