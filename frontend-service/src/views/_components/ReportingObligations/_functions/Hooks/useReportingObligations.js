import { useReducer } from 'react';

export const useReportingObligations = () => {
  const initialState = {
    obligation: { id: null, title: '' },
    previousObligation: { id: null, title: '' },
    checkedObligation: { id: null, title: '' }
  };

  const reducer = (state, { type, payload }) => {
    switch (type) {
      case 'SET_OBLIGATION':
        return { ...state, obligation: { id: payload.id, title: payload.title } };

      case 'SET_PREVIOUS_OBLIGATION':
        return { ...state, previousObligation: { id: payload.id, title: payload.title } };

      case 'SET_CHECKED_OBLIGATION':
        return { ...state, checkedObligation: { id: payload.id, title: payload.title } };

      case 'SET_TO_CHECKED':
        return { ...state, previousObligation: state.checkedObligation, obligation: state.checkedObligation };

      case 'RESET_OBLIGATIONS':
        return { ...initialState };

      default:
        return state;
    }
  };

  const [state, dispatch] = useReducer(reducer, initialState);

  const setObligation = ({ id, title }) => {
    dispatch({ type: 'SET_OBLIGATION', payload: { id, title } });
  };

  const setPreviousObligation = ({ id, title }) => {
    dispatch({ type: 'SET_PREVIOUS_OBLIGATION', payload: { id, title } });
  };

  const setCheckedObligation = ({ id, title }) => {
    dispatch({ type: 'SET_CHECKED_OBLIGATION', payload: { id, title } });
  };

  const setToCheckedObligation = () => {
    dispatch({ type: 'SET_TO_CHECKED' });
  };

  const setObligationToPrevious = () => setObligation(state.previousObligation);

  const resetObligations = () => {
    dispatch({ type: 'RESET_OBLIGATIONS' });
  };

  return {
    obligation: state.obligation,
    resetObligations,
    setObligationToPrevious,
    setObligation,
    setPreviousObligation,
    setCheckedObligation,
    setToCheckedObligation
  };
};
