import React from 'react';

import styles from './QCManagement.module.scss';

import { Button } from 'ui/views/_components/Button';

const QCManagement = () => {
  return (
    <div>
      <form action="">
        <div id={styles.QCFormWrapper}>
          <div className={styles.section}>
            <div>
              <label htmlFor="table">
                Table
                <select name="table" placeholder="table">
                  <option value=""></option>
                </select>
              </label>
              <label htmlFor="field">
                Field
                <select name="field" placeholder="table">
                  <option value=""></option>
                </select>
              </label>
              <label htmlFor="shortCode">
                Short code
                <input type="text" placeholder="short code" />
              </label>
              <label htmlFor="description">
                Description
                <input type="text" placeholder="Description" />
              </label>
              <label htmlFor="errorMessage" className={styles.errorMessage}>
                Error message
                <input type="text" placeholder="Error mesage" />
              </label>
              <label htmlFor="description">
                Error type
                <select name="field" placeholder="table">
                  <option value=""></option>
                </select>
              </label>
            </div>
            <div>
              <label htmlFor="QcActive">
                Active
                <input type="checkbox" />
              </label>
            </div>
          </div>
          <div className={styles.section}>
            <div className={styles.qcHeader}>
              <span>Group</span>
              <span>AND / OR</span>
              <span>Opertators type</span>
              <span>Operator</span>
              <span>Value</span>
            </div>
            <div className={styles.qcList}>
              <div className={styles.qcListItem}>
                <label htmlFor="group" className={styles.groupChecker}>
                  <input type="checkbox" />
                </label>
                <select name="AndOr" className={styles.andOr}>
                  <option value=""></option>
                  <option value="AND">AND</option>
                  <option value="OR">OR</option>
                </select>
                <select name="operatorType" className={styles.operatorType}>
                  <option value=""></option>
                </select>
                <select name="operator" className={styles.operatorValue}>
                  <option value=""></option>
                </select>
                <input type="value" placeholder="value" className={styles.qcValue} />
              </div>
              <div className={styles.qcListItem}>
                <label htmlFor="group" className={styles.groupChecker}>
                  <input type="checkbox" />
                </label>
                <select name="AndOr" className={styles.andOr}>
                  <option value=""></option>
                  <option value="AND">AND</option>
                  <option value="OR">OR</option>
                </select>
                <select name="operatorType" className={styles.operatorType}>
                  <option value=""></option>
                </select>
                <select name="operator" className={styles.operatorValue}>
                  <option value=""></option>
                </select>
                <input type="value" placeholder="value" className={styles.qcValue} />
              </div>
              <div className={styles.qcListItem}>
                <label htmlFor="group" className={styles.groupChecker}>
                  <input type="checkbox" />
                </label>
                <select name="AndOr" className={styles.andOr}>
                  <option value=""></option>
                  <option value="AND">AND</option>
                  <option value="OR">OR</option>
                </select>
                <select name="operatorType" className={styles.operatorType}>
                  <option value=""></option>
                </select>
                <select name="operator" className={styles.operatorValue}>
                  <option value=""></option>
                </select>
                <input type="value" placeholder="value" className={styles.qcValue} />
              </div>
              <div className={styles.qcListItem}>
                <label htmlFor="group" className={styles.groupChecker}>
                  <input type="checkbox" />
                </label>
                <select name="AndOr" className={styles.andOr}>
                  <option value=""></option>
                  <option value="AND">AND</option>
                  <option value="OR">OR</option>
                </select>
                <select name="operatorType" className={styles.operatorType}>
                  <option value=""></option>
                </select>
                <select name="operator" className={styles.operatorValue}>
                  <option value=""></option>
                </select>
                <input type="value" placeholder="value" className={styles.qcValue} />
              </div>
            </div>
          </div>
          <div className={styles.section}>
            <textarea name="" id="" cols="30" rows="5"></textarea>
          </div>
          <div className={`${styles.section} ${styles.footerToolBar}`}>
            <div>
              <Button type="button" label="Add new QC" />
            </div>
            <div>
              <Button type="button" label="create" />
              <Button type="button" label="cancel" />
            </div>
          </div>
        </div>
      </form>
    </div>
  );
};

export { QCManagement };
