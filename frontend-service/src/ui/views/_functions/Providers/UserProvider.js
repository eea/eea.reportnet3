import React, { useReducer } from 'react';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const userReducer = (state, { type, payload }) => {
  switch (type) {
    case 'LOGIN':
      return {
        ...state,
        ...payload.user
      };
    case 'LOGOUT':
      return {};
    case 'REFRESH_TOKEN':
      return {
        ...state,
        ...payload.user
      };

    default:
      return state;
  }
};

export const UserProvider = ({ children }) => {
  const [state, dispatch] = useReducer(userReducer, {});

  return (
    <UserContext.Provider
      value={{
        ...state,
        onLogin: user => {
          dispatch({
            type: 'LOGIN',
            payload: {
              user
            }
          });
        },
        onLogout: () => {
          dispatch({
            type: 'LOGOUT',
            payload: {
              user: {}
            }
          });
        },
        onTokenRefresh: user => {
          dispatch({
            type: 'REFRESH_TOKEN',
            payload: {
              user
            }
          });
        }
      }}>
      {children}
    </UserContext.Provider>
  );
};
