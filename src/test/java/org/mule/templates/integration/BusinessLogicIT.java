/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.templates.builders.SfdcObjectBuilder;

import com.google.common.collect.Lists;
import com.netsuite.webservices.platform.core_2014_1.RecordRef;
import com.netsuite.webservices.platform.core_2014_1.types.RecordType;
import com.sforce.soap.partner.SaveResult;

import de.schlichtherle.io.FileInputStream;

/**
 * The objective of this class is to validate the correct behavior of the flows
 * for this Anypoint Tempalte that make calls to external systems.
 * 
 */
public class BusinessLogicIT extends AbstractTemplateTestCase {

	private static final String PATH_TO_TEST_PROPERTIES = "./src/test/resources/mule.test.properties";
	protected static final String TEMPLATE_NAME = "opportunity-aggregation";
	private static Map<String, Object> testSfdcOpportunity = new HashMap<String, Object>();
	private static Map<String, Object> testNetsuiteOpportunity = new HashMap<String, Object>();



	@Rule
	public DynamicPort port = new DynamicPort("http.port");

	/**
	 * Precedes testing. Creates test opportunities in both systems and loads test properties.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		Properties props = new Properties();
		props.load(new FileInputStream(new File(PATH_TO_TEST_PROPERTIES)));
		
		createTestOpportunityInSFDC();
	}

	/**
	 * Deletes test opportunities from both systems.
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		deleteTestOpportunityFromSFDC(testSfdcOpportunity);
		deleteTestOpportunityFromNetsuite(testNetsuiteOpportunity);
	}

	
	/**
	 * Tests the correct execution of the template. Does not create test opportunities. 
	 * Verifies execution without errors and that this template sends an e-mail.
	 * @throws Exception
	 */
	@Test
	public void testMainFlow() throws Exception {
		MuleEvent event = runFlow("mainFlow");

		//to implement
	}


	/**
	 * Creates test opportunity in Salesforce system.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void createTestOpportunityInSFDC() throws Exception {

		List<Map<String,Object>> list = new ArrayList<>();
		testSfdcOpportunity = buildSFDCOpportunity();
		list.add(testSfdcOpportunity);
		
		// initialize flow
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("createOpportunityInSFDC");
		flow.initialise();

		// process
		MuleEvent event = flow.process(getTestEvent(list, MessageExchangePattern.REQUEST_RESPONSE));
		List<SaveResult> results = (List<SaveResult>) event.getMessage().getPayload();
		
		// update object with new ID
		testSfdcOpportunity.put("Id", results.get(0).getId());

	}
	

	/**
	 * Deletes opportunity from Salesforce.
	 * @param opportunity Opportunity to delete. It should contain the Id.
	 * @throws Exception
	 */
	private void deleteTestOpportunityFromSFDC(Map<String, Object> opportunity) throws Exception {
		// to delete, we must use list even for 1 object
		List<String> identifiers = new ArrayList<String>();
		identifiers.add((String) opportunity.get("Id"));
		
		// initialize
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("deleteOpportunityFromSFDC");
		flow.initialise();

		// proceed with deletion
		flow.process(getTestEvent(identifiers, MessageExchangePattern.REQUEST_RESPONSE));
	}
	
	/**
	 * Deletes opportunity from Netsuite
	 * @param opportunity Opportunity to delete. It should contain the internalId.
	 * @throws Exception
	 */
	private void deleteTestOpportunityFromNetsuite(Map<String, Object> opportunity) throws Exception {
		// reference to the object to delete
		RecordRef ref = new RecordRef();
		ref.setType(RecordType.OPPORTUNITY);
		ref.setInternalId((String) opportunity.get("internalId"));
		
		// initialize
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("deleteOpportunityFromNetsuite");
		flow.initialise();

		// proceed with deletion
		flow.process(getTestEvent(ref, MessageExchangePattern.REQUEST_RESPONSE));
	}

	/**
	 * Builds object used to create Salesforce Opportunity.
	 * @return Map to be used with Salesforce connector to create opportunity.
	 */
	private Map<String, Object> buildSFDCOpportunity() {
		// fields Name, StageName and CloseDate are required in SalesForce
		Map<String, Object> opportunity = SfdcObjectBuilder.anOpportunity()
				.with("Name", buildUniqueName(TEMPLATE_NAME, "TestOppSFDC"))
				.with("Amount", "120000.0")
				.with("StageName", "Qualification")
				.with("CloseDate", Calendar.getInstance().getTime())
				.build();
		return opportunity;
	}
	
	
	/**
	 * Builds unique opportunity name for testing purposes.
	 * @param templateName name of this template
	 * @param name static part of opportunity name
	 * @return
	 */
	private String buildUniqueName(String templateName, String name) {
		String timeStamp = new Long(new Date().getTime()).toString();

		StringBuilder builder = new StringBuilder();
		builder.append(name)
		.append("_")
		.append(templateName)
		.append("_")
		.append(timeStamp);
		
		return builder.toString();
	}
}
