package org.eea.document.controller;

import org.eea.document.service.DocumentService;
import org.eea.interfaces.controller.document.DocumentController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
	@GetMapping(value = "/testLog")
	public void testLogging() throws Exception {
		documentService.testLogging();
	}

	@Override
	@GetMapping(value = "/create")
	public void uploadDocument() throws Exception {
		documentService.uploadDocument();
	}
	
	@GetMapping
	public void getDocument() throws Exception {
		documentService.getDocument();
	}
}
