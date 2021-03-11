import { apiWebLink } from 'core/infrastructure/api/domain/model/WebLinks';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

const all = async dataflowId => {
  const webLinksDTO = await apiWebLink.all(dataflowId);
  webLinksDTO.data.weblinks = webLinksDTO.data.weblinks.map(webLinkDTO => new WebLink(webLinkDTO));
  return webLinksDTO;
};

const create = async (dataflowId, weblinkToCreate) => await apiWebLink.create(dataflowId, weblinkToCreate);

const deleteWebLink = async weblinkToDelete => await apiWebLink.deleteWebLink(weblinkToDelete);

const update = async (dataflowId, weblinkToUpdate) => await apiWebLink.update(dataflowId, weblinkToUpdate);

export const ApiWebLinkRepository = { all, create, deleteWebLink, update };
