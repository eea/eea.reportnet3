package org.eea.document.controller;

import org.eea.document.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Document controller.
 */
@RestController
@RequestMapping("/document")
public class DocumentControllerImpl implements DocumentController {

	@Autowired
	private DocumentService documentService;

	@Override
	@RequestMapping(value = "/testLog", method = RequestMethod.GET)
	public void testLogging() throws Exception {
		documentService.testLogging();
	}

	@Override
	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public void uploadDocument() throws Exception {
		documentService.uploadDocument();
	}
}
