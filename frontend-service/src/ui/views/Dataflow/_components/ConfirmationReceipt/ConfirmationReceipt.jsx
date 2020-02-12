import React from 'react';
import styles from './ConfirmationReceipt.module.scss';
const ConfirmationReceipt = ({ pdfRef, receiptData }) => {
  return (
    <div ref={pdfRef} className={styles.pdf}>
      <div id="html-page">
        <h3>REPORTNET 3</h3>
        <h4>Confirmation receipt</h4>
        <h5>Name</h5>
      </div>
    </div>
  );
};

export { ConfirmationReceipt };
