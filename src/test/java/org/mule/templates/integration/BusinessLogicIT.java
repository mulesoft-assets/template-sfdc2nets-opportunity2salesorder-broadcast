/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Prober;
import org.mule.templates.builders.SfdcObjectBuilder;
import org.mule.templates.test.utils.ListenerProbe;
import org.mule.templates.test.utils.PipelineSynchronizeListener;


import de.schlichtherle.io.FileInputStream;

/**
 * The objective of this class is to validate the correct behavior of the flows
 * for this Anypoint Template that make calls to external systems.
 * 
 */
public class BusinessLogicIT extends AbstractTemplateTestCase {

	private static final String PATH_TO_TEST_PROPERTIES = "./src/test/resources/mule.test.properties";
	protected static final String TEMPLATE_NAME = "opportunity-donotremove";
	
	private String TEST_OPPORTUNITY_ID;

	private final Prober pollProber = new PollingProber(10000, 1000);
	private static final String POLL_FLOW_NAME = "triggerFlow";
	private final PipelineSynchronizeListener pipelineListener = new PipelineSynchronizeListener(POLL_FLOW_NAME);


	@Rule
	public DynamicPort port = new DynamicPort("http.port");

	@BeforeClass
	public static void init() {

		System.setProperty("page.size", "100");

		// Set the frequency between polls to 10 seconds
		System.setProperty("poll.frequencyMillis", "10000");

		// Set the poll starting delay to 20 seconds
		System.setProperty("poll.startDelayMillis", "5000");

		// Setting Default Watermark Expression to query SFDC with
		// LastModifiedDate greater than ten seconds before current time
		System.setProperty("watermark.default.expression", "#[groovy: new Date(System.currentTimeMillis() - 20000).format(\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\", TimeZone.getTimeZone('UTC'))]");

	}
	
	/**
	 * Precedes testing. Creates test opportunities in both systems and loads test properties.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		Properties props = new Properties();
		props.load(new FileInputStream(new File(PATH_TO_TEST_PROPERTIES)));
		
		TEST_OPPORTUNITY_ID = props.getProperty("sfdc.test.opportunityId");
		
		stopFlowSchedulers(POLL_FLOW_NAME);
		muleContext.registerListener(pipelineListener);
		
		updateSFDCOpportunity(TEST_OPPORTUNITY_ID);
	}

	/**
	 * Deletes test opportunities from both systems.
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		deleteTestSalesOrderFromNetsuite(TEST_OPPORTUNITY_ID);
	}
	
	/**
	 * Tests the correct execution of the template. Does not create test opportunities. 
	 * Instead, it expects existing Opportunity in Salesforce system. This Opportunity 
	 * should have Account and at least one Product associated.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testMainFlow() throws Exception {
		
		// Run poll and wait for it to run
		runSchedulersOnce(POLL_FLOW_NAME);
		waitForPollToRun();

		// Wait for the batch job executed by the poll flow to finish
		Thread.sleep(60000);
		
		//retrieve NetSuite SO based on SFDC Opportunity ID (as externalId)
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("retrieveSalesOrderFromNetsuite");
		flow.initialise();
		MuleEvent response = flow.process(getTestEvent(TEST_OPPORTUNITY_ID, MessageExchangePattern.REQUEST_RESPONSE));
				
		//assertions
		Map<String, Object> payload = (Map<String,Object>) response.getMessage().getPayload();	
		Assert.assertNotNull(payload.get("internalId"));
	}

	/**
	 * Updates the existing opportunity so that the poller will receive the data for test.
	 * @param Id
	 * @throws Exception
	 */
	private void updateSFDCOpportunity(String Id) throws Exception {
		System.err.println("Updating the test opportunity!");
		
		Map<String, Object> opp = SfdcObjectBuilder.anOpportunity()
				.with("Id",Id)
				.with("Name",buildUniqueName(TEMPLATE_NAME, "test"))
				.build();
		
		List<Map<String, Object>> oppList = new ArrayList<>();
		oppList.add(opp);
		
		// Update the opportunity so that the poller wil receive it
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("updateSFDCOpportunity");
		flow.initialise();
		flow.process(getTestEvent(oppList, MessageExchangePattern.REQUEST_RESPONSE));
		
		System.err.println("test opportunity (Id: " + TEST_OPPORTUNITY_ID +  ") updated!");
	}
	
	private void waitForPollToRun() {
		pollProber.check(new ListenerProbe(pipelineListener));
	}
	
	/**
	 * Deletes opportunity from Netsuite
	 * @param salesOrder Sales Order to delete. It should contain the internalId.
	 * @throws Exception
	 */
	private void deleteTestSalesOrderFromNetsuite(String Id) throws Exception {
	
		// initialize
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("deleteSalesOrderFromNetsuite");
		flow.initialise();

		// proceed with deletion
		flow.process(getTestEvent(TEST_OPPORTUNITY_ID, MessageExchangePattern.REQUEST_RESPONSE));
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
