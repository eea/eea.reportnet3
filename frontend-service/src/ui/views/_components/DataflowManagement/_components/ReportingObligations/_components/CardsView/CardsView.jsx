import React from 'react';

import styles from './CardsView.module.scss';

import { Card } from './_components/Card';

export const CardsView = ({ checkedObligation, data, onSelectObl }) => {
  return (
    <div className={styles.cardWrap}>
      {data.map(obligation => {
        console.log('obligation', obligation);
        return (
          <Card
            key={obligation.id}
            // checked={checkedObligation}
            date={obligation.dueDate}
            icon="externalLink"
            id={obligation.id}
            // obligation={obligation}
            // onCheck={onSelectObl}
            subtitle={obligation.legalInstrument}
            title={obligation.title}
          />
        );
      })}
    </div>
  );
};
