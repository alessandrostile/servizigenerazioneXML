package it.sysmanagement.databencart.api.template.be.service;


//import it.sysmanagement.databencart.api.template.be.repository.SchedaElasticRepository;
import it.sysmanagement.databencart.api.template.be.repository.SchedaElasticRepository;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JRException;

@Service
public class CreateTemplateService {

	@Autowired
	private SchedaElasticRepository schedaElasticRepository;

public String findSchedaListJsonGroup(String group) throws JRException, IOException {
		return schedaElasticRepository.findSchedeByGroup(group);
	}

	public String findSchedaListJsonType(String tsk) throws JRException, IOException {
		return schedaElasticRepository.findSchedeByType(tsk);
	}

	public String findSchedaListJsonGroupAndType(String group, String tsk, Boolean isBatch)
			throws JRException, IOException {
		return schedaElasticRepository.findSchedeByGroupAndType(group, tsk, isBatch);
	}

	public String findSchedaListJsonCollection(String idCollection) throws JRException, IOException {
		return schedaElasticRepository.findSchedeByCollectionReference(idCollection);
	}

}
