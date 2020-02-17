import React, { useReducer, useContext } from 'react';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

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
    case 'TOGGLE_LOGOUT_CONFIRM': 
      return {
        ...state,
        showLogoutConfirmation: !state.showLogoutConfirmation
      }

    default:
      return state;
  }
};

export const UserProvider = ({ children }) => {
  const [state, dispatch] = useReducer(userReducer, {});
  const notificationContext = useContext(NotificationContext);

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
        },
        onToggleLogoutConfirm: () => {
          dispatch({
            type: 'TOGGLE_LOGOUT_CONFIRM',
            payload:{}
          })
        }
      }}>
      {children}
    </UserContext.Provider>
  );
};
