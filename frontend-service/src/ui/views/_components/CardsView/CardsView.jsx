import React, { Fragment, useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';

import styles from './CardsView.module.scss';

import { Card } from './_components/Card';

import { Paginator } from 'ui/views/_components/DataTable/_components/Paginator';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const CardsView = ({ checkedCard, data, handleRedirect, onChangePagination, onSelectCard, pagination }) => {
  console.log('checkedCard', checkedCard);
  const resources = useContext(ResourcesContext);

  const [cards, setCards] = useState(data);
  const [cardsPerPage, setCardsPerPage] = useState(pagination.rows);
  const [currentPage, setCurrentPage] = useState(pagination.page);
  const [first, setFirst] = useState(pagination.first);

  useEffect(() => {
    onLoadData();
  }, [data]);

  const onLoadData = () => setCards(data);

  const onPaginate = event => {
    setCardsPerPage(event.rows);
    setCurrentPage(event.page);
    setFirst(event.first);

    onChangePagination({ first: event.first, rows: event.rows, page: event.page });
  };

  const begin = Math.max(0, Math.ceil(currentPage * cardsPerPage));
  const end = begin + cardsPerPage;
  const currentPosts = cards.slice(begin, end);

  const paginatorRightText = `${resources.messages['totalObligations']}: ${data.length}`;

  return isEmpty(data) ? (
    <h3 className={styles.noObligations}>{resources.messages['noObligationsWithSelectedParameters']}</h3>
  ) : (
    <Fragment>
      <div
        className={styles.cardWrap}
        style={{ justifyContent: currentPosts.length === cardsPerPage ? 'space-between' : 'flex-start' }}>
        {currentPosts.map(card => {
          return (
            <Card
              card={card}
              checked={checkedCard}
              date={card.dueDate || card.expirationDate}
              handleRedirect={handleRedirect}
              icon="externalLink"
              id={card.id}
              key={card.id}
              onCheck={onSelectCard}
              status={card.status}
              subtitle={card.legalInstrument || card.description}
              title={card.title || card.name}
            />
          );
        })}
      </div>

      <Paginator
        className={'p-paginator-bottom'}
        first={first}
        onPageChange={onPaginate}
        rightContent={paginatorRightText}
        rows={cardsPerPage}
        rowsPerPageOptions={[5, 10, 15]}
        totalRecords={data.length}
      />
    </Fragment>
  );
};

CardsView.propTypes = {
  pagination: PropTypes.object
};

CardsView.defaultProps = {
  pagination: { first: 0, rows: 10, page: 0 }
};
