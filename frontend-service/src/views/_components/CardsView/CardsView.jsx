import { Fragment, useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';

import styles from './CardsView.module.scss';

import { Card } from 'views/_components/Card';
import { InputText } from 'views/_components/InputText';
import { Paginator } from 'views/_components/DataTable/_components/Paginator';
import ReactTooltip from 'react-tooltip';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const CardsView = ({
  checkedCard,
  contentType,
  data,
  handleRedirect,
  isReferenceDataflow,
  onChangePagination,
  onSelectCard,
  pagination,
  paginatorRightText,
  type
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const [cards, setCards] = useState(data);
  const [cardsPerPage, setCardsPerPage] = useState(pagination.rows);
  const [currentPage, setCurrentPage] = useState(pagination.page);
  const [goToPage, setGoToPage] = useState(pagination.page + 1);
  const [pageInputTooltip, setPageInputTooltip] = useState(resourcesContext.messages['currentPageInfoMessage']);

  useEffect(() => {
    onLoadData();
  }, [data]);

  const onChangeCurrentPage = event => {
    if (event.key === 'Enter' && goToPage !== '' && goToPage !== pagination.first + 1) {
      var pc = Math.ceil(data.length / cardsPerPage) || 1;
      var p = Math.floor(event.target.value - 1);

      if (p >= 0 && p < pc) {
        var newPageState = {
          first: (event.target.value - 1) * cardsPerPage,
          rows: cardsPerPage,
          page: p
        };
        onChangePagination(newPageState);
        setCurrentPage(p);
      }
    } else {
      setGoToPage(event.target.value);
      if (event.target.value <= 0 || event.target.value > Math.ceil(data.length / cardsPerPage)) {
        setPageInputTooltip(
          `${resourcesContext.messages['currentPageErrorMessage']} ${Math.ceil(data.length / cardsPerPage)}`
        );
      } else {
        setPageInputTooltip(resourcesContext.messages['currentPageInfoMessage']);
      }
    }
  };

  const onLoadData = () => setCards(data);

  const onPaginate = event => {
    setCardsPerPage(event.rows);
    setCurrentPage(event.page);
    setGoToPage(event.page + 1);

    onChangePagination({ first: event.first, rows: event.rows, page: event.page });
  };

  const currentPageTemplate = {
    layout: 'FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport',
    CurrentPageReport: options => {
      return (
        <span className={styles.currentPageWrapper}>
          <label className={styles.currentPageLabel}>{resourcesContext.messages['goTo']}</label>
          <InputText
            className={styles.currentPageInput}
            data-for="pageInputTooltip"
            data-tip
            id="currentPageInput"
            keyfilter="pint"
            onChange={onChangeCurrentPage}
            onKeyDown={onChangeCurrentPage}
            style={{
              border: (goToPage <= 0 || goToPage > options.totalPages) && '1px solid var(--errors)',
              boxShadow:
                goToPage <= 0 || goToPage > options.totalPages ? 'var(--inputtext-box-shadow-focus-error)' : 'none'
            }}
            value={goToPage}
          />
          <ReactTooltip border={true} effect="solid" id="pageInputTooltip" place="bottom">
            {pageInputTooltip}
          </ReactTooltip>
          <label className={styles.currentPageOf}>
            {data.length > 0 ? `${resourcesContext.messages['of']} ${Math.ceil(data.length / cardsPerPage)}` : 1}
          </label>
        </span>
      );
    }
  };

  const begin = Math.max(0, Math.ceil(currentPage * cardsPerPage));
  const end = begin + cardsPerPage;
  const currentPosts = cards.slice(begin, end);

  return isEmpty(data) ? (
    <h3 className={styles.noObligations}>{resourcesContext.messages[`no${contentType}WithSelectedParameters`]}</h3>
  ) : (
    <Fragment>
      <div className={styles.cardWrap}>
        {currentPosts.map(card => {
          return (
            <Card
              card={card}
              checked={checkedCard}
              date={card.dueDate || card.expirationDate}
              handleRedirect={handleRedirect}
              icon="externalUrl"
              id={card.id}
              isReferenceDataflow={isReferenceDataflow}
              key={card.id}
              onCheck={onSelectCard}
              status={card.status}
              subtitle={card.legalInstrument || card.description}
              title={card.title || card.name}
              type={type}
            />
          );
        })}
      </div>
      <Paginator
        className="p-paginator-bottom"
        first={pagination.first}
        onPageChange={onPaginate}
        rightContent={paginatorRightText}
        rows={cardsPerPage}
        rowsPerPageOptions={[5, 10, 15]}
        template={currentPageTemplate}
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
