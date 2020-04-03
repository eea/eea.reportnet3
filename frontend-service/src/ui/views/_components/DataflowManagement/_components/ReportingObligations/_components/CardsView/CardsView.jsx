import React, { useEffect, useState, Fragment } from 'react';

import styles from './CardsView.module.scss';

import { Card } from './_components/Card';
import { Pagination } from './_components/Pagination';

import { usePagination } from './_functions/Hooks/usePagination';

import { Paginator } from 'ui/views/_components/DataTable/_components/Paginator';

export const CardsView = ({ checkedObligation, data, onSelectObl }) => {
  const [posts, setPosts] = useState(data);
  const [currentPage, setCurrentPage] = useState(1);
  const [postsPerPage] = useState(8);

  useEffect(() => {
    onLoadData();
  }, [data]);

  const onLoadData = () => setPosts(data);

  const indexOfLastPost = currentPage * postsPerPage;
  const indexOfFirstPost = indexOfLastPost - postsPerPage;
  const currentPosts = posts.slice(indexOfFirstPost, indexOfLastPost);

  const paginate = pageNumber => setCurrentPage(pageNumber.page);

  // const { next, prev, jump, currentData, currentPage, maxPage } = usePagination(data, 9);
  // console.log('currentPage', currentPage);
  // console.log('maxPage', maxPage);

  return (
    <Fragment>
      <div className={styles.cardWrap}>
        {currentPosts.map(obligation => {
          return (
            <Card
              key={obligation.id}
              checked={checkedObligation}
              date={obligation.dueDate}
              icon="externalLink"
              id={obligation.id}
              obligation={obligation}
              onCheck={onSelectObl}
              subtitle={obligation.legalInstrument}
              title={obligation.title}
            />
          );
        })}
      </div>
      {/* <Pagination postsPerPage={postsPerPage} totalPosts={posts.length} paginate={paginate} /> */}
      <Paginator
        totalRecords={data.length}
        className={'p-paginator-bottom'}
        onPageChange={paginate}
        rows={Math.ceil(data.length / 4)}
        first={currentPage}
        rowsPerPageOptions={[4, 8, 12]}
        // pageLinkSize={4}
      />
    </Fragment>
  );
};
