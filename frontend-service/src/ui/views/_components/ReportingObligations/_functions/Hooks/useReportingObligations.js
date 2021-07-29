import { useState } from 'react';

export const useReportingObligations = () => {
  const [obligation, setObligation] = useState({ id: null, title: '' });
  const [previousObligation, setPreviousObligation] = useState({ id: null, title: '' });

  const resetObligation = () => setObligation({ id: null, title: '' });

  const resetPreviousObligation = () => setPreviousObligation({ id: null, title: '' });

  const setObligationToPrevious = () => setObligation(previousObligation);

  return [
    obligation,
    resetObligation,
    resetPreviousObligation,
    setObligationToPrevious,
    setObligation,
    setPreviousObligation
  ];
};
