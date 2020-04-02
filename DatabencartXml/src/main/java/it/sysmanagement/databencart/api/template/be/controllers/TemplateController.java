package it.sysmanagement.databencart.api.template.be.controllers;

import io.swagger.annotations.*;
import it.sysmanagement.databencart.api.template.be.dto.SavedValuesDTO;
import it.sysmanagement.databencart.api.template.be.service.CreateTemplateService;
import it.sysmanagement.databencart.api.template.be.util.Constants;
import net.sf.jasperreports.engine.JRException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "", produces = { APPLICATION_JSON_VALUE })
@Api(value = "", tags = "tipoMappatura")
public class TemplateController {

	private Logger logger = LogManager.getLogger(TemplateController.class);

	@Autowired
	private CreateTemplateService createTemplatePdf;

	
	@Value("${rest.layer.backend.host}")
	private String restLayerBackendHost;

	@Value("${rest.layer.backend.path.get.scheda.ids}")
	private String restLayerBackendPathGetSchedaIds;


	@ApiOperation(value = "Get report of a specific Scheda ", notes = "")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 404, message = "Scheda/e not found"),
			@ApiResponse(code = 400, message = "wrong/empty fileT value"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@ResponseBody
	@RequestMapping(value = "/getById", method = RequestMethod.GET)
	public void getMappaturaSchedaById(@RequestHeader("x-jwt-assertion") String jwtHeader,
			@ApiParam(value = "id or list of ids separated by a comma", required = true) @RequestParam(name = "request", required = true) String[] request,
			@ApiParam(value = "Type of output File, [json,pdf,xml,csv]", required = true) @RequestParam(name = "fileT", required = true) String fileT,
			@ApiParam(value = "group", required = true) @RequestParam(name = "group", required = true) String group,
			@ApiParam(value = "researchValue", required = false) @RequestParam(name = "research", required = false) String research,
			HttpServletRequest requests, HttpServletResponse response) throws JRException, IOException {

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.add("x-jwt-assertion", jwtHeader);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(restLayerBackendHost.concat(restLayerBackendPathGetSchedaIds))
				.queryParam("request", request).queryParam("group", group);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		HttpEntity<String> response2 = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entity,
				String.class);

		if (response2.getBody().isEmpty() || response2.getBody().equals("[]")) {
			response.setStatus(400);
			response.sendError(400, "Scheda not Found");
			response.flushBuffer();

		}

		SavedValuesDTO bankerDepotDTO = new SavedValuesDTO(fileT, response, response2.getBody(), research);
		bankerDepotDTO.setTypeOfOutput(fileT);
		downloadManager(bankerDepotDTO);

	}

	@ApiOperation(value = "Get a List of schede report with specific type and domain", notes = "")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 404, message = "Schede not found"),
			@ApiResponse(code = 302, message = "report found in archive"),
			@ApiResponse(code = 400, message = "wrong/empty fileT value"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@ResponseBody
	@RequestMapping(value = "/filtered", method = RequestMethod.GET)
	public void getSchedaByDomainAndGroupt(
			@ApiParam(value = "TSK", required = false) @RequestParam(name = "TSK", required = false) String tsk,
			@ApiParam(value = "group", required = false) @RequestParam(name = "group", required = false) String group,
			@ApiParam(value = "retrieve", required = false) @RequestParam(name = "retrieve", required = false) Boolean retrieve,
			@ApiParam(value = "isBatch", required = false) @RequestParam(name = "isBatch", required = false) Boolean isBatch,
			@ApiParam(value = "Type of output File, [json,pdf,xml,csv]", required = true) @RequestParam(name = "fileT", required = true) String fileT,
			HttpServletRequest request, HttpServletResponse response) throws JRException, IOException {
		
		SavedValuesDTO bankerDepotDto = new SavedValuesDTO(fileT, response,
				createTemplatePdf.findSchedaListJsonGroupAndType(group, tsk,isBatch), true, Constants.TYPE_KEY, group);

		downloadManager(bankerDepotDto);
	}

	@ApiOperation(value = "Get a List of schede report who have a relation with a specific collection ", notes = "")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 404, message = "Schede not found"),
			@ApiResponse(code = 400, message = "wrong/empty fileT value"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@ResponseBody
	@RequestMapping(value = "/collection", method = RequestMethod.GET)
	public void getSchedeWithCollection(
			@ApiParam(value = "collectionId", required = true) @RequestParam(name = "collectionId", required = true) String collectionId,
			@ApiParam(value = "Type of output File, [json,pdf,xml,csv]", required = true) @RequestParam(name = "fileT", required = true) String fileT,
			HttpServletRequest request, HttpServletResponse response) throws JRException, IOException {

		SavedValuesDTO bankerDepotDto = new SavedValuesDTO(fileT, response,
				createTemplatePdf.findSchedaListJsonCollection(collectionId), true, Constants.COLLECTION_CASE,
				collectionId);

		downloadManager(bankerDepotDto);

	}

	public void downloadManager(SavedValuesDTO bankerDepotDTO) throws JRException, IOException {

		if (bankerDepotDTO.getResponse2().isEmpty() || bankerDepotDTO.getResponse2().equals("[]")) {
			bankerDepotDTO.getResponse().setStatus(404);
			bankerDepotDTO.getResponse().sendError(404, "Schede not Found");
			bankerDepotDTO.getResponse().flushBuffer();
			return;

		}
		switch (bankerDepotDTO.getTypeOfOutput()) {
		case "xml":
			bankerDepotDTO.getResponse().addHeader("Content-disposition", "attachment; filename= Report.xml");
			bankerDepotDTO.getResponse().setContentType("application/xml");
			JSONArray json = new JSONArray(bankerDepotDTO.getResponse2());
			String xml = XML.toString(json);
			prepareResponse(bankerDepotDTO.getResponse(), Constants.XML_INTESTATION + "<root>" + xml + "</root>");
			bankerDepotDTO.getResponse().setStatus(200);
			bankerDepotDTO.getResponse().flushBuffer();
			break;

		default:
			bankerDepotDTO.getResponse().setStatus(400);
			bankerDepotDTO.getResponse().sendError(400, "the file output must be : [xml]");
			break;
		}

	}

	public void prepareResponse(HttpServletResponse response, String scheda) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(byteArrayOutputStream);
		try {

			output.write(scheda.getBytes());
			byteArrayOutputStream.flush();
			byteArrayOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		final byte[] bytes = byteArrayOutputStream.toByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		org.apache.commons.io.IOUtils.copy(inputStream, response.getOutputStream());
	}

}
