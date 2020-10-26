// import { config } from 'conf';

import { apiFeedback } from 'core/infrastructure/api/domain/model/Feedback';

import { Feedback } from 'core/domain/model/Feedback/Feedback';

const all = async () => {
  return await apiFeedback.all();
};

const allUnread = async (first, rows, dataProviderId) => {
  const response = await apiFeedback.allUnread(first, rows, dataProviderId);

  const data = [
    {
      id: 1000,
      message: 'This is a message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-13',
      sender: false
    },
    {
      id: 1001,
      message: 'This is another message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-14',
      sender: false
    },
    {
      id: 1002,
      message:
        'This is a message. Please read it bla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blabla',
      read: false,
      datetime: '2015-09-13',
      sender: true
    },
    {
      id: 1003,
      message: 'This is another message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-14',
      sender: false
    },
    {
      id: 1004,
      message: 'This is a message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-13',
      sender: true
    },
    {
      id: 1005,
      message: 'This is another message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-14',
      sender: true
    },
    {
      id: 1006,
      message: 'This is a message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-13',
      sender: false
    },
    {
      id: 1007,
      message: 'This is another message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-14',
      sender: false
    },
    {
      id: 1008,
      message: 'This is a message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-13',
      sender: false
    },
    {
      id: 1009,
      message: 'This is another message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-14',
      sender: true
    },
    {
      id: 1010,
      message: 'This is a message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-13',
      sender: true
    },
    {
      id: 1011,
      message: 'This is another message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-14',
      sender: false
    },
    {
      id: 1012,
      message: 'This is a message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-13',
      sender: true
    },
    {
      id: 1013,
      message: 'This is another message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-14',
      sender: true
    },
    {
      id: 1014,
      message: 'This is a message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-13',
      sender: true
    },
    {
      id: 1015,
      message: 'This is another message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-14',
      sender: false
    },
    {
      id: 1016,
      message: 'This is a message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-13',
      sender: false
    },
    {
      id: 1017,
      message: 'This is another message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-14',
      sender: true
    },
    {
      id: 1018,
      message: 'This is a message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-13',
      sender: false
    },
    {
      id: 1019,
      message: 'This is another message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-14',
      sender: true
    },
    {
      id: 1020,
      message: 'This is a message. Please read it bla bla blabla',
      read: false,
      datetime: '2015-09-13',
      sender: false
    },
    {
      id: 1021,
      message: 'This is another message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-14',
      sender: false
    },
    {
      id: 1022,
      message: 'This is a message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-13',
      sender: true
    },
    {
      id: 1023,
      message: 'This is another message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-14',
      sender: true
    },
    {
      id: 1024,
      message: 'This is a message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-13',
      sender: false
    },
    {
      id: 1025,
      message: 'This is ano456456ther message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-14',
      sender: false
    },
    {
      id: 1026,
      message: 'This is a 34534534message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-13',
      sender: false
    },
    {
      id: 1027,
      message: 'This is ano345345345ther message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-14',
      sender: true
    },
    {
      id: 1028,
      message: 'This is34534534 a message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-13',
      sender: true
    },
    {
      id: 1029,
      message: 'This is 345345345another message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-14',
      sender: true
    },
    {
      id: 1030,
      message: 'This is a345345345 message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-13',
      sender: true
    },
    {
      id: 1031,
      message: 'This is a435345345nother message. Please read it bla bla blabla',
      read: true,
      datetime: '2015-09-14',
      sender: true
    }
  ];
  let messages = [];
  for (let i = first; i < rows; i++) {
    messages.push(new Feedback({ ...data[i] }));
  }
  return messages;
};

export const ApiFeedbackRepository = {
  all,
  allUnread
};
