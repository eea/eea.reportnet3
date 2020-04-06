import React, { useState, useContext } from 'react';
import { isNull } from 'lodash';

import styles from './UserImg.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const UserImg = () => {
  const userContext = useContext(UserContext);

  const imageUploader = React.useRef(null);
  const uploadedImage = React.useRef(undefined);

  //isNull(uploadedImage) ? console.log('//// HAY IMAGEN antes del click') : console.log('// HAY ICONO antes del click');

  const handleImageUpload = e => {
    const [file] = e.target.files;

    if (file) {
      const reader = new FileReader();
      const { current } = uploadedImage;
      current.file = file;
      reader.onload = e => {
        current.src = e.target.result;
      };
      reader.readAsDataURL(file);

      userContext.onClickUserIcon(uploadedImage);

      // uploadedImage
      //   ? console.log('//// HAY IMAGEN después del click')
      //   : console.log('// SIGUE ICONO después del click');
    }
  };
  return (
    <div>
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
        <input
          type="file"
          accept="image/*"
          onChange={handleImageUpload}
          ref={imageUploader}
          style={{
            display: 'none'
          }}
        />

        <img
          ref={uploadedImage}
          icon={<FontAwesomeIcon icon={AwesomeIcons('user-profile')} className={styles.userDataIcon} />}
          //src={userIcon}
          className={styles.userDataIcon}
          onClick={() => imageUploader.current.click()}
        />

        {/* <img
          ref={uploadedImage}
          icon={AwesomeIcons('user-profile')}
          className={styles.userDataIcon}
          onClick={() => imageUploader.current.click()}
        /> */}
      </div>
    </div>
    // <FontAwesomeIcon icon={AwesomeIcons('user-profile')} className={styles.userDataIcon} />
  );
};

export { UserImg };
