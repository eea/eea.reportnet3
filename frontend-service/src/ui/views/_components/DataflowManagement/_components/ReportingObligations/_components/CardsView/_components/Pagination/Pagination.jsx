import React, { Fragment } from 'react';

import styles from './Pagination.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Toolbar } from 'ui/views/_components/Toolbar';

export const Pagination = ({ postsPerPage, totalPosts, paginate }) => {
  const pageNumbers = [];

  for (let i = 1; i <= Math.ceil(totalPosts / postsPerPage); i++) {
    pageNumbers.push(i);
  }

  return (
    <Toolbar>
      <ul className={styles.pagination}>
        <Button className={`p-button-secondary-transparent ${styles.icon}`} layout="simple" />
        {pageNumbers.map(number => (
          <li key={number} className={styles.pageItem}>
            <span onClick={() => paginate(number)} className="page-link">
              {number}
            </span>
          </li>
        ))}
        <Button className={`p-button-secondary-transparent ${styles.icon}`} layout="simple" />
      </ul>
      <div className="p-toolbar-group-right">Total pages: {totalPosts.lenght}</div>
    </Toolbar>
  );
};
