import React, { useEffect, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './UserImg.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Icon } from 'ui/views/_components/Icon';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { UserService } from 'core/services/User';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const UserImg = () => {
  const userContext = useContext(UserContext);

  const imageUploader = React.useRef(null);
  const uploadedImage = React.useRef();

  useEffect(() => {
    if (!isEmpty(userContext.userProps.userImage) && userContext.userProps.userImage.join('') !== '') {
      onLoadImage();
    }
  }, [userContext.userProps.userImage]);

  const handleImageUpload = e => {
    const [file] = e.target.files;

    if (file) {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');
      const maxW = 200;
      const maxH = 200;

      const { current } = uploadedImage;
      current.onload = function () {
        const iw = current.width;
        const ih = current.height;
        const scale = Math.min(maxW / iw, maxH / ih);
        const iwScaled = iw * scale;
        const ihScaled = ih * scale;
        canvas.width = iwScaled;
        canvas.height = ihScaled;
        ctx.drawImage(current, 0, 0, iwScaled, ihScaled);
        updateImage(splitBase64Image(canvas.toDataURL()));
      };

      current.src = URL.createObjectURL(e.target.files[0]);
    }
  };
  const splitBase64Image = base64Image => base64Image.match(/.{1,250}/g);

  const updateImage = async splittedBase64Image => {
    try {
      const inmUserProperties = { ...userContext.userProps };
      inmUserProperties.userImage = splittedBase64Image;
      const response = await UserService.updateAttributes(inmUserProperties);
      if (response.status >= 200 && response.status <= 299) {
        userContext.onUserFileUpload(splittedBase64Image);
      }
    } catch (error) {
      console.error(error);
      //Notification
    }
  };

  const onLoadImage = () => {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const { current } = uploadedImage;
    current.onload = function () {
      ctx.drawImage(current, 0, 0);
    };
    current.src = userContext.userProps.userImage.join('');
  };

  return (
    <div>
      <div className={styles.imageWrapper}>
        <input
          accept="image/*"
          onChange={handleImageUpload}
          ref={imageUploader}
          style={{
            display: 'none'
          }}
          type="file"
        />
        <img
          ref={uploadedImage}
          icon={<FontAwesomeIcon icon={AwesomeIcons('user-profile')} className={styles.userDataIcon} />}
          // src={}
          className={styles.userDataIcon}
          onClick={() => imageUploader.current.click()}
        />
        <Icon icon="edit" className={styles.editIcon} />
      </div>
    </div>
    // <FontAwesomeIcon icon={AwesomeIcons('user-profile')} className={styles.userDataIcon} />
  );
};

export { UserImg };
