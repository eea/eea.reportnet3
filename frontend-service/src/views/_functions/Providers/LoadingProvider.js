import { useReducer } from 'react';
import { GlobalLoading } from 'views/_components/GlobalLoading';
import { LoadingContext } from 'views/_functions/Contexts/LoadingContext';

const generalLoadingReducer = (state, { type }) => {
  switch (type) {
    case 'SHOW_LOADING':
      return { ...state, loadingCount: state.loadingCount + 1 };

    case 'HIDE_LOADING':
      return { ...state, loadingCount: 0 };

    default:
      return state;
  }
};

export const LoadingProvider = ({ children }) => {
  const [state, dispatch] = useReducer(generalLoadingReducer, { loadingCount: 0 });

  const LoadingInit = {
    ...state,
    showLoading: () => {
      dispatch({ type: 'SHOW_LOADING' });
    },
    hideLoading: () => {
      dispatch({ type: 'HIDE_LOADING' });
    }
  };

  return (
    <LoadingContext.Provider value={LoadingInit}>
      <GlobalLoading />
      {children}
    </LoadingContext.Provider>
  );
};
