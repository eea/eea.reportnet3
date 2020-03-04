import React, { useState, useEffect } from 'react';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { UserService } from 'core/services/User';

export const UserImgUpload = () => {
  const [userImage, setUserImage] = useState({ images: [] });

  const onUpload = async () => {
    const {} = await UserService.uploadImg();
  };

  return <CustomFileUpload fileLimit={4} onUpload={onUpload} />;
};
