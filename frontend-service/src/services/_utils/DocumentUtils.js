import { config } from 'conf';

import { Document } from 'entities/Document';

const parseDocumentListDTO = documentsDTO => documentsDTO?.map(documentDTO => parseDocumentDTO(documentDTO));

const parseDocumentDTO = documentDTO => {
  return new Document({
    category: documentDTO.category,
    date: documentDTO.date,
    description: documentDTO.description,
    id: documentDTO.id,
    isPublic: documentDTO.isPublic,
    language: config.languages.filter(language => language.code === documentDTO.language).map(name => name.name),
    size: documentDTO.size,
    title: documentDTO.name,
    url: documentDTO.url
  });
};

export const DocumentUtils = { parseDocumentListDTO };
