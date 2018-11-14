
# Anypoint Template: Salesforce Opportunity to NetSuite Sales Order Broadcast	

<!-- Header (start) -->

<!-- Header (end) -->

# License Agreement
This template is subject to the conditions of the <a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>. Review the terms of the license before downloading and using this template. You can use this template for free with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio. 
# Use Case
<!-- Use Case (start) -->
As a Salesforce admin I want to synchronize Opportunities in Salesforce that are in the 'Closed Won' stage to NetSuite. In NetSuite, these opportunities become Sales Orders. Each time there is a new Opportunity that matches the criteria defined or if there is a change in an already existing one in SalesForce, the integration application will detect the changes and it will insert/update the Sales Order in NetSuite. This template can serve as a part of the Quote to Cash process for an Enterprise.

The application has been built in a manner wherein it can not only be used as an example, but it can also be used to establish a starting point on which you can build out your integration use case.

As implemented, this Anypoint Template leverages the [Batch Module](http://www.mulesoft.org/documentation/display/current/Batch+Processing) 

The batch job is divided into *Process* and *On Complete* stages.

The integration is triggered by a scheduler mechanism defined in the flow. The application queries for newest Salesforce updates/creations using a filter criteria and executes the batch job.
The application not only retrieves the data pertaining to the Opportunities that were modified/created, but also information about the related Account and Products. The reason being, Sales Order in NetSuite requires references to related Customer and Items in order to be created. 
Therefore, we filter out propagation of Opportunities that are not 'Closed Won', don't have an Account or at least one Product associated with it.

In the Batch Job's *Process* phase, the Customer corresponding to the source Opportunity's Account will be searched for in NetSuite. We use the *companyName* property of objects in NetSuite to match their SalesForce counterpart. 
This property should contain *Name* property of SalesForce object.
If the Customer does not exist in NetSuite, it will be created in the next step, so that we have it ready to reference it later to Sales Order.
Then all the products (Opportunity Line Items) associated with the Opportunity are upserted into NetSuite as InventoryItem objects.
Last step upserts the Sales Order object referencing the Customer and Items created/updated in the previous steps.
<!-- Use Case (end) -->

# Considerations
<!-- Default Considerations (start) -->

<!-- Default Considerations (end) -->

<!-- Considerations (start) -->
To make this Anypoint Template run, there are certain preconditions that must be considered. All of them deal with the preparations in both source and destination systems, that must be made in order for all to run smoothly. **Failing to do so could lead to unexpected behavior of the template.**
<!-- Considerations (end) -->



## Salesforce Considerations

Here's what you need to know about Salesforce to get this template to work:

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>.
- Can I modify the Field Access Settings? How? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US">Salesforce: Modifying Field Access Settings</a>.

### As a Data Source

If the user who configured the template for the source system does not have at least *read only* permissions for the fields that are fetched, then an *InvalidFieldFault* API fault displays.

```
java.lang.RuntimeException: [InvalidFieldFault [ApiQueryFault 
[ApiFault  exceptionCode='INVALID_FIELD'
exceptionMessage='Account.Phone, Account.Rating, Account.RecordTypeId, 
Account.ShippingCity
^
ERROR at Row:1:Column:486
No such column 'RecordTypeId' on entity 'Account'. If you are attempting to 
use a custom field, be sure to append the '__c' after the custom field name. 
Reference your WSDL or the describe call for the appropriate names.'
]
row='1'
column='486'
]
]
```






## NetSuite Considerations


### As a Data Destination

Customer must be assigned to subsidiary. In this template, this is done statically and you must configure the property file with subsidiary *internalId* that is already in the system. You can find out this number by entering 'subsidiaries' 
into the NetSuite search field and selecting 'Page - Subsidiaries'. When you click on the 'View' next to the subsidiary chosen, you will see the ID in the URL line. Please, use this Id to populate *nets.subsidiaryId* property in the property file.




# Run it!
Simple steps to get this template running.
<!-- Run it (start) -->

<!-- Run it (end) -->

## Running On Premises
In this section we help you run this template on your computer.
<!-- Running on premise (start) -->

<!-- Running on premise (end) -->

### Where to Download Anypoint Studio and the Mule Runtime
If you are new to Mule, download this software:

+ [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
+ [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)

**Note:** Anypoint Studio requires JDK 8.
<!-- Where to download (start) -->

<!-- Where to download (end) -->

### Importing a Template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your Anypoint Platform credentials, search for the template, and click Open.
<!-- Importing into Studio (start) -->

<!-- Importing into Studio (end) -->

### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources.
+ Complete all the properties required as per the examples in the "Properties to Configure" section.
+ Right click the template project folder.
+ Hover your mouse over `Run as`.
+ Click `Mule Application (configure)`.
+ Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`.
+ Click `Run`.
<!-- Running on Studio (start) -->

<!-- Running on Studio (end) -->

### Running on Mule Standalone
Update the properties in one of the property files, for example in mule.prod.properties, and run your app with a corresponding environment variable. In this example, use `mule.env=prod`. 


## Running on CloudHub
When creating your application in CloudHub, go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the mule.env value.
<!-- Running on Cloudhub (start) -->
Once your app is all set up and started, there is no need to do anything else. Every time a opportunity is created or modified, it will be automatically synchronised to NetSuite as long as it is 'Closed Won' and has products associated.
<!-- Running on Cloudhub (end) -->

### Deploying a Template in CloudHub
In Studio, right click your project name in Package Explorer and select Anypoint Platform > Deploy on CloudHub.
<!-- Deploying on Cloudhub (start) -->

<!-- Deploying on Cloudhub (end) -->

## Properties to Configure
To use this template, configure properties such as credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.
### Application Configuration
<!-- Application Configuration (start) -->
+ page.size `200`
+ scheduler.frequency `10000`
+ scheduler.start.delay `100`

#### Watermarking default last query timestamp
+ watermark.default.expression `2015-04-01T19:40:27.000Z`

#### Salesforce Connector configuration
+ sfdc.username `bob.dylan@orga`
+ sfdc.password `DylanPassword123`
+ sfdc.securityToken `avsfwCUl7apQs56Xq2AKi3X`

#### NetSuite Connector configuration
+ nets.email `email@example.com`
+ nets.password `password`
+ nets.account `TSTDRVxxxxxxx`
+ nets.roleId `9`
+ nets.appId `77EBCBD6-AF9F-11E5-BF7F-FEFF819CDC9F`
+ nets.subsidiaryId `1`
<!-- Application Configuration (end) -->

# API Calls
<!-- API Calls (start) -->
Salesforce imposes limits on the number of API Calls that can be made. However, in this template, only one call per scheduler cycle is done to retrieve all the information required.
<!-- API Calls (end) -->

# Customize It!
This brief guide provides a high level understanding of how this template is built and how you can change it according to your needs. As Mule applications are based on XML files, this page describes the XML files used with this template. More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml<!-- Customize it (start) -->

<!-- Customize it (end) -->

## config.xml
<!-- Default Config XML (start) -->
This file provides the configuration for connectors and configuration properties. Only change this file to make core changes to the connector processing logic. Otherwise, all parameters that can be modified should instead be in a properties file, which is the recommended place to make changes.<!-- Default Config XML (end) -->

<!-- Config XML (start) -->

<!-- Config XML (end) -->

## businessLogic.xml
<!-- Default Business Logic XML (start) -->
Functional aspect of the Template is implemented in this XML, directed by one flow that will schedule for Salesforce Opportunity creations/updates. The several message processors constitute the actions that fully implement the logic of this Template.
During the Process stage, each SFDC opportunity will be upserted to NetSuite system. Before this is possible, the template will query the NetSuite if Customer and the Items exists and if not, it makes sure these objects are created.<!-- Default Business Logic XML (end) -->

<!-- Business Logic XML (start) -->

<!-- Business Logic XML (end) -->

## endpoints.xml
<!-- Default Endpoints XML (start) -->
This file is conformed by a Flow containing the Scheduler that will periodically query Salesforce for updated/created Opportunities that meet the defined criteria in the query. And then executing the batch job process with the query results.<!-- Default Endpoints XML (end) -->

<!-- Endpoints XML (start) -->

<!-- Endpoints XML (end) -->

## errorHandling.xml
<!-- Default Error Handling XML (start) -->
This file handles how your integration reacts depending on the different exceptions. This file provides error handling that is referenced by the main flow in the business logic.<!-- Default Error Handling XML (end) -->

<!-- Error Handling XML (start) -->

<!-- Error Handling XML (end) -->

<!-- Extras (start) -->

<!-- Extras (end) -->
