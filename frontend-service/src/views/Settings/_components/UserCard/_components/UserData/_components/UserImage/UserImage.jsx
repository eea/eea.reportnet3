import { useRef, useEffect, useContext, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';

import defaultAvatar from 'views/_assets/images/avatars/defaultAvatar.png';

import styles from './UserImage.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Icon } from 'views/_components/Icon';
import ReactTooltip from 'react-tooltip';

import { UserService } from 'services/UserService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

const UserImage = () => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [isAvatarDialogVisible, setIsAvatarDialogVisible] = useState(false);

  const imageUploader = useRef(null);
  const uploadedImage = useRef();

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
      e.target.value = '';
    }
  };

  const listOfImages = () =>
    config.avatars.map((avatar, i) => (
      <div className={styles.gridItem} key={avatar.name}>
        <img
          alt="Avatar to choose"
          className={styles.gridItem}
          onClick={() => {
            updateImage(splitBase64Image(avatar.base64));
            setIsAvatarDialogVisible(false);
          }}
          src={avatar.base64}
        />
      </div>
    ));

  const splitBase64Image = base64Image => base64Image.match(/.{1,250}/g);

  const updateImage = async splittedBase64Image => {
    try {
      const inmUserProperties = { ...userContext.userProps };
      inmUserProperties.userImage = splittedBase64Image;
      await UserService.updateConfiguration(inmUserProperties);
      userContext.onUserFileUpload(splittedBase64Image);
    } catch (error) {
      console.error('UserImage - updateImage.', error);
      notificationContext.add({
        type: 'UPDATE_ATTRIBUTES_USER_SERVICE_ERROR'
      });
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
          id={'userIcon'}
          onChange={handleImageUpload}
          ref={imageUploader}
          style={{
            display: 'none'
          }}
          type="file"
        />
        <label className="srOnly" htmlFor="userIcon">
          {resourcesContext.messages['selectImage']}
        </label>
        <img
          alt="User profile avatar"
          className={styles.userDataIcon}
          data-event="click"
          data-for="addAvatar"
          data-tip
          icon={<FontAwesomeIcon className={styles.userDataIcon} icon={AwesomeIcons('user-profile')} />}
          ref={uploadedImage}
          src={isEmpty(userContext.userProps.userImage) ? defaultAvatar : null}
        />
        <Icon className={styles.editIcon} icon="edit" />
      </div>
      {isAvatarDialogVisible && (
        <Dialog
          header={resourcesContext.messages['selectImage']}
          onHide={() => setIsAvatarDialogVisible(false)}
          style={{ width: '80%' }}
          visible={isAvatarDialogVisible}>
          <div className={styles.gridContainer}>{listOfImages()}</div>
        </Dialog>
      )}
      <ReactTooltip
        border={true}
        className={styles.tooltipClass}
        clickable={true}
        effect="solid"
        globalEventOff="click"
        id="addAvatar"
        place="top">
        <Button
          className={`p-button-secondary p-button-animated-blink`}
          icon={'add'}
          label={resourcesContext.messages['uploadImage']}
          onClick={() => imageUploader.current.click()}
          style={{ marginRight: '1rem' }}
        />
        <Button
          className={`p-button-secondary p-button-animated-blink`}
          icon={'userPlus'}
          label={resourcesContext.messages['selectImage']}
          onClick={() => setIsAvatarDialogVisible(true)}
        />
      </ReactTooltip>
    </div>
  );
};

export { UserImage };
