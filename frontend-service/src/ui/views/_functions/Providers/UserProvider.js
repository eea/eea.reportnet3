import React, { useContext, useReducer } from 'react';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
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
    case 'ADD_SOCKET':
      return {
        ...state,
        socket: payload
      };
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
  const notificationContext = useContext(NotificationContext);

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
        onAddSocket: socket => {
          dispatch({
            type: 'ADD_SOCKET',
            payload: socket
          });
        },
        onLogout: () => {
          notificationContext.deleteAll();
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
