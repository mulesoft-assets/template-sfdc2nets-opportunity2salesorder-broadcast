
# Anypoint Template: Salesforce Opportunity to NetSuite Sales Order Broadcast

+ [License Agreement](#licenseagreement)
+ [Use Case](#usecase)
+ [Considerations](#considerations)
	* [Salesforce Considerations](#salesforceconsiderations)
	* [Netsuite Considerations](#netsuiteconsiderations)
+ [Run it!](#runit)
	* [Running on premise](#runonopremise)
	* [Running on Studio](#runonstudio)
	* [Running on Mule ESB stand alone](#runonmuleesbstandalone)
	* [Running on CloudHub](#runoncloudhub)
	* [Deploying your Anypoint Template on CloudHub](#deployingyouranypointtemplateoncloudhub)
	* [Properties to be configured (With examples)](#propertiestobeconfigured)
+ [API Calls](#apicalls)
+ [Customize It!](#customizeit)
	* [config.xml](#configxml)
	* [businessLogic.xml](#businesslogicxml)
	* [endpoints.xml](#endpointsxml)
	* [errorHandling.xml](#errorhandlingxml)


# License Agreement <a name="licenseagreement"/>
Note that using this template is subject to the conditions of this [License Agreement](AnypointTemplateLicense.pdf).
Please review the terms of the license before downloading and using this template. In short, you are allowed to use the template for free with Mule ESB Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case <a name="usecase"/>
As a Salesforce admin I want to synchronize Opportunities in Salesforce that are in the 'Closed Won' stage to NetSuite. In NetSuite, these opportunities become Sales Orders. Each time there is a new Opportunity that matches the criteria defined or if there is a change in an already existing one in SalesForce, the integration application will detect the changes and it will insert/update the Sales Order in NetSuite. This template can serve as a part of the Quote to Cash process for an Enterprise.

The application has been built in a manner wherein it can not only be used as an example, but it can also be used to establish a starting point on which you can build out your integration use case.

As implemented, this Anypoint Template leverages the [Batch Module](http://www.mulesoft.org/documentation/display/current/Batch+Processing) 

The batch job is divided into *Input*, *Process* and *On Complete* stages.

The integration is triggered by a polling mechanism defined in the flow. The application queries for newest Salesforce updates/creations using a filter criteria and executes the batch job.
The application not only retrieves the data pertaining to the Opportunities that were modified/created, but also information about the related Account and Products. The reason being, Sales Order in NetSuite requires references to related Customer and Items in order to be created. 
This decreases the number of queries made to SalesForce to 1 per poll cycle.
Therefore, we filter out propagation of Opportunities that are not 'Closed Won', don't have an Account or at least one Product associated with it.

In the Batch Job's *Process* phase, the Customer corresponding to the source Opportunity's Account will be searched for in NetSuite. We use the *externalId* property of objects in NetSuite to match their SalesForce counterpart. 
This property should contain *Id* property of SalesForce object.
If the Customer does not exist in NetSuite, it will be created in the next step, so that we have it ready to reference it later to Sales Order.
Then all the products (Opportunity Line Items) associated with the Opportunity are upserted into NetSuite as InventoryItem objects.
Last step upserts the Sales Order object referencing the Customer and Items created/updated in the previous steps.

Finally during the *On Complete* stage the Anypoint Template will log output statistics data into the console.

# Considerations <a name="considerations"/>

To make this Anypoint Template run, there are certain preconditions that must be considered. All of them deal with the preparations in both source and destination systems, that must be made in order for all to run smoothly. **Failling to do so could lead to unexpected behavior of the template.**



## Salesforce Considerations <a name="salesforceconsiderations"/>

There may be a few things that you need to know regarding Salesforce, in order for this template to work.

In order to have this template working as expected, you should be aware of your own Salesforce field configuration.

###FAQ

 - Where can I check that the field configuration for my Salesforce instance is the right one?

    [Salesforce: Checking Field Accessibility for a Particular Field][1]

- Can I modify the Field Access Settings? How?

    [Salesforce: Modifying Field Access Settings][2]


[1]: https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US
[2]: https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US

### As source of data

If the user configured in the template for the source system does not have at least *read only* permissions for the fields that are fetched, then a *InvalidFieldFault* API fault will show up.

```
java.lang.RuntimeException: [InvalidFieldFault [ApiQueryFault [ApiFault  exceptionCode='INVALID_FIELD'
exceptionMessage='
Account.Phone, Account.Rating, Account.RecordTypeId, Account.ShippingCity
^
ERROR at Row:1:Column:486
No such column 'RecordTypeId' on entity 'Account'. If you are attempting to use a custom field, be sure to append the '__c' after the custom field name. Please reference your WSDL or the describe call for the appropriate names.'
]
row='1'
column='486'
]
]
```






## Netsuite Considerations <a name="netsuiteconsiderations"/>


### As destination of data

Customer must be assigned to subsidiary. In this template, this is done statically and you must configure the property file with subsidiary *internalId* that is already in the system. You can find out this number by entering 'subsidiaries' 
into the NetSuite search field and selecting 'Page - Subsidiaries'. When you click on the 'View' next to the subsidiary chosen, you will see the ID in the URL line. Please, use this Id to populate *nets.subsidiaryId* property in the property file.



# Run it! <a name="runit"/>
Simple steps to get Salesforce Opportunity to NetSuite Sales Order Broadcast running.


## Running on premise <a name="runonopremise"/>
In this section we detail the way you should run your Anypoint Template on your computer.


### Where to Download Mule Studio and Mule ESB
First thing to know if you are a newcomer to Mule is where to get the tools.

+ You can download Mule Studio from this [Location](http://www.mulesoft.com/platform/mule-studio)
+ You can download Mule ESB from this [Location](http://www.mulesoft.com/platform/soa/mule-esb-open-source-esb)


### Importing an Anypoint Template into Studio
Mule Studio offers several ways to import a project into the workspace, for instance: 

+ Anypoint Studio generated Deployable Archive (.zip)
+ Anypoint Studio Project from External Location
+ Maven-based Mule Project from pom.xml
+ Mule ESB Configuration XML from External Location

You can find a detailed description on how to do so in this [Documentation Page](http://www.mulesoft.org/documentation/display/current/Importing+and+Exporting+in+Studio).


### Running on Studio <a name="runonstudio"/>
Once you have imported you Anypoint Template into Anypoint Studio you need to follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources
+ Complete all the properties required as per the examples in the section [Properties to be configured](#propertiestobeconfigured)
+ Once that is done, right click on you Anypoint Template project folder 
+ Hover you mouse over `"Run as"`
+ Click on  `"Mule Application"`


### Running on Mule ESB stand alone <a name="runonmuleesbstandalone"/>
Complete all properties in one of the property files, for example in [mule.prod.properties] (../master/src/main/resources/mule.prod.properties) and run your app with the corresponding environment variable to use it. To follow the example, this will be `mule.env=prod`. 


## Running on CloudHub <a name="runoncloudhub"/>
While [creating your application on CloudHub](http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub) (Or you can do it later as a next step), you need to go to Deployment > Advanced to set all environment variables detailed in **Properties to be configured** as well as the **mule.env**.
Once your app is all set up and started, there is no need to do anything else. Every time a opportunity is created or modified, it will be automatically synchronised to NetSuite as long as it is 'Closed Won' and has products associated.

### Deploying your Anypoint Template on CloudHub <a name="deployingyouranypointtemplateoncloudhub"/>
Mule Studio provides you with really easy way to deploy your Template directly to CloudHub, for the specific steps to do so please check this [link](http://www.mulesoft.org/documentation/display/current/Deploying+Mule+Applications#DeployingMuleApplications-DeploytoCloudHub)


## Properties to be configured (With examples) <a name="propertiestobeconfigured"/>
In order to use this Mule Anypoint Template you need to configure properties (Credentials, configurations, etc.) either in properties file or in CloudHub as Environment Variables. Detail list with examples:
### Application configuration
+ poll.frequencyMillis `60000`
+ poll.startDelayMillis `0`
+ watermark.defaultExpression `YESTERDAY` or `2015-04-01T19:40:27.000Z`
+ page.size `200`


#### Salesforce Connector configuration
+ sfdc.username `bob.dylan@orga`
+ sfdc.password `DylanPassword123`
+ sfdc.securityToken `avsfwCUl7apQs56Xq2AKi3X`
+ sfdc.url `https://login.salesforce.com/services/Soap/u/32.0`

#### NetSuite Connector configuration
+ nets.email `email@example.com`
+ nets.password `password`
+ nets.account `TSTDRVxxxxxxx`
+ nets.roleId `9`  
+ nets.subsidiaryId `1`

# API Calls <a name="apicalls"/>
Salesforce imposes limits on the number of API Calls that can be made. However, in this template, only one call per poll cycle is done to retrieve all the information required.


# Customize It!<a name="customizeit"/>
This brief guide intends to give a high level idea of how this Anypoint Template is built and how you can change it according to your needs.
As mule applications are based on XML files, this page will be organized by describing all the XML that conform the Anypoint Template.
Of course more files will be found such as Test Classes and [Mule Application Files](http://www.mulesoft.org/documentation/display/current/Application+Format), but to keep it simple we will focus on the XMLs.

Here is a list of the main XML files you'll find in this application:

* [config.xml](#configxml)
* [endpoints.xml](#endpointsxml)
* [businessLogic.xml](#businesslogicxml)
* [errorHandling.xml](#errorhandlingxml)


## config.xml<a name="configxml"/>
Configuration for Connectors and [Properties Place Holders](http://www.mulesoft.org/documentation/display/current/Configuring+Properties) are set in this file. **Even you can change the configuration here, all parameters that can be modified here are in properties file, and this is the recommended place to do it so.** Of course if you want to do core changes to the logic you will probably need to modify this file.

In the visual editor they can be found on the *Global Element* tab.


## businessLogic.xml<a name="businesslogicxml"/>
Functional aspect of the Template is implemented in this XML, directed by one flow that will poll for Salesforce Opportunity creations/updates. The severeal message processors constitute the actions that fully implement the logic of this Template:

1. During the Input stage the Template will query the Salesforce instance for existing opportunities (including associated Account and Products) that match the filter criteria.
2. During the Process stage, each SFDC opportunity will be upserted to NetSuite system. Before this is possible, the template will query the NetSuite if Customer and the Items exists and if not, it makes sure these objects are created.
Finally during the On Complete stage the Template will log batch statistics into the console.



## endpoints.xml<a name="endpointsxml"/>
This file is conformed by a Flow containing the Poll that will periodically query Salesforce for updated/created Opportunities that meet the defined criteria in the query. And then executing the batch job process with the query results.



## errorHandling.xml<a name="errorhandlingxml"/>
This is the right place to handle how your integration will react depending on the different exceptions. 
This file holds a [Choice Exception Strategy](http://www.mulesoft.org/documentation/display/current/Choice+Exception+Strategy) that is referenced by the main flow in the business logic.



