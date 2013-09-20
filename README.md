soa-testing-framework
=====================

Framework for automated testing of Oracle OSB and SOA applications

Description and idea:
Altough there already exists 2 well known testing frameworks to me, Citrus and Robot and both are great and robust, they didn't looked to me so easy to use as soon as I am focused purelly now on just Oracle Service Bus automated testing. 
One of the perspectives is also to use that framework by less technical focused people like testers itself. For this approach I selected transformation of Oracle Service Bus into Maven projects, so I can easily use SoapUI maven plugin and create test cases as groovy scripts. This framework should be then called by these groovy template scripts and should be as easy as possible to use by non-developers (another step will be web-based configuration).

I understand that this way is less generic to apply to other systems, but that is its main purpose.

Supported document generation functions:
* Generate CRUD SQL scripts for tasks based on database connection and object (table/view) - currently supported only 1 object.
* Generate default SOAP Envelope for Proxy and Business services from end point URI (WSDL)
* Generate default JMS message based on XML schema (XSD only)

Supported endpoint connection types:
* HTTP SOAP Services 
* JDBC - focused on Orale Database
* JMS Server - currently only queue supported (even DISTRIBUTED ones)
* FTP Server
* File System

Supported OSB Functionality:
* Enable/Disable Proxy or Business services in flow to support stop temporarily their functionalities for testing purposes

Main test definition user interface:
* XML configuration file for framework which includes mainly general settings and end point connection information
* SoapUI for configuring the TestCase steps via Groovy templates created for specific operations - I am starting with one scenario and will extend the number, but this should be free to configure based on the user knowledge of the framework.

DEMO Scenario Flow:

Database Source -> Proxy DB Adapter Pooling Service -> Business JMS Service -> JMS Server Queue -> Proxy JMS Pooling Service -> Database DB Adapter Service -> Database Target

Testing Scenario Steps:
Manual:

1. User will configure XML end point connection XML file so framework will be able to communicate with necessary points:
	* "Database Source"
	* "JMS Server Queue"
	* "Database Target"
2. User will use command line or GUI(in future version) tool to generate default SQL DML script - insert in this case - and will be able to customize it before the test exectuion.
Automatic executed by the soap-ui maven plugin (Groovy script templates in place):
1. Disable "Proxy JMS Pooling Service" so any incoming JMS message will stay in queue.
2. Insert row into "Database Source"
3. Delay
4. Check JMS Server Queue for new message
5. If message exists -> Validate it against defined schema
6. Map DB columns to XSD elements - currently it is 1:1 flat mapping where DB column looks like MESSAGE_ID and XML element as messageId, so the mapping is done by processor and is hardcoded (This functionality will be extended in the future by provide mapping file).
7. Check if the values in the DB corespons to the values included in the message.
8. If yes, everything looks good and the test result is OK.
9. Generate report of the test result in PDF or HTML form.
10. Enable "Proxy JMS Pooling Service" so the generated test message can continue in its flow.
11. ...... and so on ....

NOTE: Enabling and Disabling the services is important functionality which can help for example in Pre-Production environments test when sometimes you need to block last service in flow to store the message in the final storage (DB, File, FTP, JMS, whatever it is.)

Ladislav
