import React, { useState, useEffect } from 'react';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { UserService } from 'core/services/User';

export const UserImgUpload = () => {
  const [userImage, setUserImage] = useState({ images: [] });

  const onUpload = async () => {
    const {} = await UserService.uploadImg();
  };
  // const onUpload = .toDataURL('base64');

  return <CustomFileUpload fileLimit={1} onUpload={onUpload} name="file" accept="image/*" />;
};
