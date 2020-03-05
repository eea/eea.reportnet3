import { apiWebLink } from 'core/infrastructure/api/domain/model/WebLink';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

const all = async dataflowId => {
  const webLinksDTO = await apiWebLink.all(dataflowId);
  return webLinksDTO.map(webLinkDTO => new WebLink(webLinkDTO));
};

const create = async (dataflowId, weblinkToCreate) => {
  const isCreated = await apiWebLink.create(dataflowId, weblinkToCreate);
  weblinkToCreate.isCreated = isCreated;
  return weblinkToCreate;
};

const deleteWeblink = async weblinkToDelete => {
  const isDeleted = await apiWebLink.deleteWeblink(weblinkToDelete);
  weblinkToDelete.isDeleted = isDeleted;
  return weblinkToDelete;
};

const update = async (dataflowId, weblinkToUpdate) => {
  const isUpdated = await apiWebLink.update(dataflowId, weblinkToUpdate);
  weblinkToUpdate.isUpdated = isUpdated;
  return weblinkToUpdate;
};

export const ApiWebLinkRepository = {
  all,
  create,
  deleteWeblink,
  update
};
