import React, { useEffect, useState, Fragment } from 'react';

import styles from './CardsView.module.scss';

import { Card } from './_components/Card';
import { Pagination } from './_components/Pagination';

import { usePagination } from './_functions/Hooks/usePagination';

import { Paginator } from 'ui/views/_components/DataTable/_components/Paginator';

export const CardsView = ({ checkedObligation, data, onSelectObl }) => {
  const [cards, setCards] = useState(data);
  const [currentPage, setCurrentPage] = useState(1);
  const [cardsPerPage] = useState(8);

  useEffect(() => {
    onLoadData();
  }, [data]);

  const onLoadData = () => setCards(data);

  const indexOfLastPost = currentPage * cardsPerPage;
  const indexOfFirstPost = indexOfLastPost - cardsPerPage;
  const currentPosts = cards.slice(indexOfFirstPost, indexOfLastPost);

  const paginate = pageNumber => setCurrentPage(pageNumber.page);

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
      {/* <Pagination cardsPerPage={cardsPerPage} totalPosts={cards.length} paginate={paginate} /> */}
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
