export const UploadImg = ({ userRepository }) => async (userId, imgData) => userRepository.uploadImg(userId, imgData);
