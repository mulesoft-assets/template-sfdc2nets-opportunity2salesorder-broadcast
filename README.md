
# Anypoint Template: Salesforce Opportunity to NetSuite Sales Order Broadcast	

<!-- Header (start) -->
Broadcast changes or created opportunities from Salesforce to NetSuite as Sales Orders in real time. You can use this template to implement a quote to cash process in an enterprise. 

The detection criteria and fields to move are configurable. Additional systems can be added to be notified of the changes. Real time synchronization is achieved either by rapid polling of Salesforce or Outbound Notifications that reduce the number of API calls. This template uses Mule batching and watermarking capabilities to capture only recent changes, and to efficiently process large numbers of records.

![cb54a332-dac6-4fbb-9ef0-2d3d7394fce4-image.png](https://exchange2-file-upload-service-kprod.s3.us-east-1.amazonaws.com:443/cb54a332-dac6-4fbb-9ef0-2d3d7394fce4-image.png)

![f5417c2d-fe3e-43a2-9433-2afd0a9f1b15-image.png](https://exchange2-file-upload-service-kprod.s3.us-east-1.amazonaws.com:443/f5417c2d-fe3e-43a2-9433-2afd0a9f1b15-image.png)

![48d760e2-1a23-4cf4-9e27-94193f46ac54-image.png](https://exchange2-file-upload-service-kprod.s3.us-east-1.amazonaws.com:443/48d760e2-1a23-4cf4-9e27-94193f46ac54-image.png)

![a2c81f9a-87cd-45dc-bd3d-7d84d0daeceb-image.png](https://exchange2-file-upload-service-kprod.s3.us-east-1.amazonaws.com:443/a2c81f9a-87cd-45dc-bd3d-7d84d0daeceb-image.png)

<!-- Header (end) -->

# License Agreement
This template is subject to the conditions of the <a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>. Review the terms of the license before downloading and using this template. You can use this template for free with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio. 
# Use Case
<!-- Use Case (start) -->
A Salesforce administrator wants to synchronize opportunities in Salesforce that are in the 'Closed Won' stage to NetSuite. In NetSuite, these opportunities become Sales Orders. Each time there is a new opportunity that matches the criteria defined, or if there is a change in an existing one in SalesForce, the integration application detects the changes and inserts or updates the Sales Order in NetSuite. This template can serve as a part of the quote to cash process for an Enterprise.

This application can be used as an example or as a starting point upon which you can build your integration's use case. 

This template leverages the Mule batch module, which is divided into *Process* and *On Complete* stages. After the integration starts from a scheduler, the application queries for the latest Salesforce updates or creates, and when found, executes the batch job. The application retrieves modified or created opportunities, and also information about a related account and products. This is done because to create a NetSuite sales order, the sales order requires references to a customer and items. Thereafter, the application filters out opportunities that are not 'Closed Won' and don't have an account or at least one product associated with it.

In the batch job's *Process* phase, the customer corresponding to the source opportunity's account is located in NetSuite. The application uses the *companyName* property of objects in NetSuite to match a SalesForce counterpart. 
This property should contain the *Name* property of a SalesForce object.
If a customer does not exist in NetSuite, the application creates the customer, so that it's ready to reference later in the sales order.
Then all the products (opportunity line items) associated with the opportunity are upserted into NetSuite as InventoryItem objects. The last step upserts the Sales Order object referencing the customer and items created or updated in the previous steps.
<!-- Use Case (end) -->

# Considerations
<!-- Default Considerations (start) -->

<!-- Default Considerations (end) -->

<!-- Considerations (start) -->
To make this template run, there are certain preconditions that must be considered. All of them deal with the preparations in both source and destination systems, that must be made for the template to run smoothly. Failing to do so can lead to unexpected behavior of the template.
<!-- Considerations (end) -->

## Salesforce Considerations

To get this template to work:

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
No such column 'RecordTypeId' on entity 'Account'. If you are 
attempting to use a custom field, be sure to append the '__c' 
after the custom field name. Reference your WSDL or the describe 
call for the appropriate names.'
]
row='1'
column='486'
]
]
```

## NetSuite Considerations


### As a Data Destination

A customer must be assigned to a subsidiary. In this template, this is done statically and you must configure the property file with subsidiary *internalId* that is already in the system. You can find this number by entering 'subsidiaries' 
in the NetSuite search field and selecting 'Page - Subsidiaries'. When you click the 'View' next to the subsidiary chosen, you see the ID in the URL line. Use this ID to populate *nets.subsidiaryId* property in the property file.

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

1. Locate the properties file `mule.dev.properties`, in src/main/resources.
2. Complete all the properties required per the examples in the "Properties to Configure" section.
3. Right click the template project folder.
4. Hover your mouse over `Run as`.
5. Click `Mule Application (configure)`.
6. Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`.
7. Click `Run`.
<!-- Running on Studio (start) -->

<!-- Running on Studio (end) -->

### Running on Mule Standalone
Update the properties in one of the property files, for example in mule.prod.properties, and run your app with a corresponding environment variable. In this example, use `mule.env=prod`. 


## Running on CloudHub
When creating your application in CloudHub, go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the mule.env value.
<!-- Running on Cloudhub (start) -->
After your app is  started, there is no need to do anything else. Each time an opportunity is created or modified, it is automatically synchronized to NetSuite as long as it is 'Closed Won' and has products associated.
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
+ watermark.default.expression `2019-04-01T19:40:27.000Z`

#### Salesforce Connector Configuration
+ sfdc.username `bob.dylan@orga`
+ sfdc.password `DylanPassword123`
+ sfdc.securityToken `avsfwCUl7apQs56Xq2AKi3X`

#### NetSuite Connector Configuration
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
This file provides the configuration for connectors and configuration properties. Only change this file to make core changes to the connector processing logic. Otherwise, all parameters that can be modified should instead be in a properties file, which is the recommended place to make changes.
<!-- Default Config XML (end) -->

<!-- Config XML (start) -->

<!-- Config XML (end) -->

## businessLogic.xml
<!-- Default Business Logic XML (start) -->
The functional aspect of this template is implemented in this XM file, directed by a flow that schedules the Salesforce opportunity creates or updates. The several message processors constitute the actions that fully implement the logic of this template. During the Process stage, each Salesforce opportunity is upserted to the NetSuite system. Before this is possible, the template queries the NetSuite if a customer and items exist and if not, it creates these objects.
<!-- Default Business Logic XML (end) -->

<!-- Business Logic XML (start) -->

<!-- Business Logic XML (end) -->

## endpoints.xml
<!-- Default Endpoints XML (start) -->
This file is a flow containing the scheduler that periodically queries Salesforce for updated or created opportunities that meet the defined criteria in the query. This file executes the batch job process with the query results.
<!-- Default Endpoints XML (end) -->

<!-- Endpoints XML (start) -->

<!-- Endpoints XML (end) -->

## errorHandling.xml
<!-- Default Error Handling XML (start) -->
This file handles how your integration reacts depending on the different exceptions. This file provides error handling that is referenced by the main flow in the business logic.
<!-- Default Error Handling XML (end) -->

<!-- Error Handling XML (start) -->

<!-- Error Handling XML (end) -->

<!-- Extras (start) -->

<!-- Extras (end) -->
