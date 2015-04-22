/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import static org.mule.templates.builders.SfdcObjectBuilder.anAccount;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Prober;
import org.mule.templates.builders.SfdcObjectBuilder;
import org.mule.templates.test.utils.ListenerProbe;
import org.mule.templates.test.utils.PipelineSynchronizeListener;

import com.google.common.collect.Lists;
import com.netsuite.webservices.platform.core_2014_1.RecordRef;
import com.netsuite.webservices.platform.core_2014_1.types.RecordType;
import com.sforce.soap.partner.SaveResult;
import com.mulesoft.module.batch.BatchTestHelper;

import de.schlichtherle.io.FileInputStream;

/**
 * The objective of this class is to validate the correct behavior of the flows
 * for this Anypoint Template that make calls to external systems.
 * 
 */
public class BusinessLogicIT extends AbstractTemplateTestCase {

	private static final String PATH_TO_TEST_PROPERTIES = "./src/test/resources/mule.test.properties";
	protected static final String TEMPLATE_NAME = "opportunity-aggregation";
//	private static Map<String, Object> testSfdcOpportunity = new HashMap<>();
//	private static Map<String, Object> testSFDCAccount = new HashMap<>();
//	private static List<Map<String, Object>> createdOpportunities = new ArrayList<>();
	
	private String TEST_OPPORTUNITY_ID;

	private BatchTestHelper helper;
	private final Prober pollProber = new PollingProber(10000, 1000);
	private static final int TIMEOUT_SEC = 120;
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
		System.setProperty("poll.startDelayMillis", "20000");

		// Setting Default Watermark Expression to query SFDC with
		// LastModifiedDate greater than ten seconds before current time
		System.setProperty("watermark.default.expression", "#[groovy: new Date(System.currentTimeMillis() - 10000).format(\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\", TimeZone.getTimeZone('UTC'))]");

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
		helper = new BatchTestHelper(muleContext);
		
		stopFlowSchedulers(POLL_FLOW_NAME);
		muleContext.registerListener(pipelineListener);
		
		updateSFDCOpportunity(TEST_OPPORTUNITY_ID);
		//createSFDCAccount();
		//createSFDCOpportunities();
	}

	/**
	 * Deletes test opportunities from both systems.
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
	//	deleteTestOpportunityFromSFDC(createdOpportunities);
	}

	
	/**
	 * Tests the correct execution of the template. Does not create test opportunities. 
	 * Verifies execution without errors and that this template sends an e-mail.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testMainFlow() throws Exception {
		
		Thread.sleep(3000);
		// Run poll and wait for it to run
		runSchedulersOnce(POLL_FLOW_NAME);
		waitForPollToRun();

		// Wait for the batch job executed by the poll flow to finish
		helper.awaitJobTermination(TIMEOUT_SEC * 1000, 500);
		helper.assertJobWasSuccessful();
		
		//retrieve NetSuite SO based on SFDC Opportunity ID (as externalId)
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("retrieveSalesOrderFromNetsuite");
		flow.initialise();
		MuleEvent response = flow.process(getTestEvent(TEST_OPPORTUNITY_ID, MessageExchangePattern.REQUEST_RESPONSE));
		
		System.err.println("After retrieving data (class): " + response.getMessage().getPayload().getClass());
		System.err.println("After retrieving data: " + response.getMessage().getPayload());
		
		Map<String, Object> payload = (Map<String,Object>) response.getMessage().getPayload();
		
		Assert.assertNotNull(payload.get("internalId"));
		
	}


	/**
	 * Updates the existing opportunity so that the poller will receive the data for test.
	 * @param Id
	 * @throws Exception
	 */
	private void updateSFDCOpportunity(String Id) throws Exception {
		System.out.println("Updating the test opportunity!");
		
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
	}
	
	private void waitForPollToRun() {
		pollProber.check(new ListenerProbe(pipelineListener));
	}
	
	
//	protected void deleteTestOpportunityFromSFDC(List<Map<String, Object>> createdOpportunities) throws Exception {
//		List<String> idList = new ArrayList<String>();
//
//		// Delete the created opportunities
//		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("deleteOpportunitiesFromSFDC");
//		flow.initialise();
//		for (Map<String, Object> opp : createdOpportunities) {
//			idList.add((String) opp.get("Id"));
//		}
//		flow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
//
//	}
	
//	/**
//	 * Deletes opportunity from Netsuite
//	 * @param salesOrder Sales Order to delete. It should contain the internalId.
//	 * @throws Exception
//	 */
//	private void deleteTestSalesOrderFromNetsuite(String Id) throws Exception {
//		// reference to the object to delete
//		RecordRef ref = new RecordRef();
//		ref.setType(RecordType.SALES_ORDER);
//		ref.setExternalId(Id);
//		
//		// initialize
//		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("deleteSalesOrderFromNetsuite");
//		flow.initialise();
//
//		// proceed with deletion
//		flow.process(getTestEvent(ref, MessageExchangePattern.REQUEST_RESPONSE));
//	}

//	/**
//	 * Builds object used to create Salesforce Opportunity.
//	 * @return Map to be used with Salesforce connector to create opportunity.
//	 */
//	private Map<String, Object> buildSFDCOpportunityObject() {
//		// fields Name, StageName and CloseDate are required in SalesForce
//		Map<String, Object> opportunity = SfdcObjectBuilder.anOpportunity()
//				.with("Name", buildUniqueName(TEMPLATE_NAME, "TestOppSFDC"))
//				.with("Amount", "120000.0")
//				.with("StageName", "Qualification")
//				.with("CloseDate", Calendar.getInstance().getTime())
//				.build();
//		return opportunity;
//	}
	
	
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
	
//	@SuppressWarnings("unchecked")
//	private void createSFDCAccount() throws Exception {
//		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("createAccountsInSFDC");
//		flow.initialise();
//		
//		// build the object to insert
//		testSFDCAccount = anAccount().with("Name", buildUniqueName(TEMPLATE_NAME, "ReferencedAccountTest"))
//										.with("BillingCity", "San Francisco")
//										.with("BillingCountry", "USA")
//										.with("Phone", "123456789")
//										.with("Industry", "Education")
//										.with("NumberOfEmployees", 9000)
//										.build();
//		
//		List<Map<String,Object>> list = new ArrayList<>();
//		list.add(testSFDCAccount);
//
//		// process it
//		MuleEvent event = flow.process(getTestEvent(list, MessageExchangePattern.REQUEST_RESPONSE));
//		List<SaveResult> results = (List<SaveResult>) event.getMessage().getPayload();
//		testSFDCAccount.put("Id", results.get(0).getId());
//	
//
//		System.out.println("Results of data creation in sandbox" + testSFDCAccount.toString());
//	}
	
	
//	@SuppressWarnings("unchecked")
//	private void createSFDCOpportunities() throws Exception {
//		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("createOpportunityInSFDC");
//		flow.initialise();
//
//		// This opportunity should not be sync
//		Map<String, Object> opportunity = buildOpportunityObject(0);
//		opportunity.put("Amount", 300);
//		createdOpportunities.add(opportunity);
//
//		// This opportunity should not be sync
//		opportunity = buildOpportunityObject(1);
//		opportunity.put("Amount", 1000);
//		createdOpportunities.add(opportunity);
//
//		// This opportunity should BE sync with it's account
//		opportunity = buildOpportunityObject(2);
//		opportunity.put("Amount", 30000);
//		opportunity.put("AccountId", testSFDCAccount.get("Id"));
//		createdOpportunities.add(opportunity);
//
//		MuleEvent event = flow.process(getTestEvent(createdOpportunities, MessageExchangePattern.REQUEST_RESPONSE));
//		List<SaveResult> results = (List<SaveResult>) event.getMessage().getPayload();
//		for (int i = 0; i < results.size(); i++) {
//			createdOpportunities.get(i).put("Id", results.get(i).getId());
//		}
//	}
	
//	protected Map<String, Object> buildOpportunityObject(int sequence) throws ParseException {
//		return SfdcObjectBuilder.anOpportunity()
//								.with("Name", buildUniqueName(TEMPLATE_NAME, "OppName" + sequence + "_"))
//								.with("StageName", "Closed Won")
//								.with("CloseDate", new SimpleDateFormat("yyyy-MM-dd").parse("2050-10-10"))
//								.with("Probability", "1")
//								.build();
//
//	}
}
