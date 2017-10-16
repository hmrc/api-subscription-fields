# API Subscription Fields

[![Build Status](https://travis-ci.org/hmrc/api-subscription-fields.svg)](https://travis-ci.org/hmrc/api-subscription-fields) [ ![Download](https://api.bintray.com/packages/hmrc/releases/api-subscription-fields/images/download.svg) ](https://bintray.com/hmrc/releases/api-subscription-fields/_latestVersion)

This microservice stores definitions and values for the HMRC Developer Hub.

### Fields Definition Endpoint examples

#### Add or modify fields definition record
curl -v -X PUT -H "Content-Type: application/json"  -H "Cache-Control: no-cache" -d '{ "fields": [ { "name": "callback-url", "description": "Callback URL", "type": "URL" }, { "name": "token", "description": "Secure Token", "type": "SecureToken" } ] }' "http://localhost:9000/definition/context/ciao-api/version/1.0"


#### Get fields definition by api context and api version
curl -v -X GET -H "Content-Type: application/json"  -H "Cache-Control: no-cache" "http://localhost:9000/definition/context/ciao-api/version/1.0"



### Subscription Fields Endpoint examples

#### Add or modify a subscription fields record
curl -v -X PUT -H "Content-Type: application/json"  -H "Cache-Control: no-cache" -d '{ "fields" : { "field-id" : "field-value" } }' "http://localhost:9000/application/327d9145-4965-4d28-a2c5-39dedee50334/context/ciao-api/version/1.0"

#### Get subscription fields by fields id
curl -v -X GET -H "Content-Type: application/json"  -H "Cache-Control: no-cache" "http://localhost:9000/fieldsId/f121ffa3-df94-43a0-8235-ac4530f9700a"

#### Get subscription fields by application id, api context, api version 
curl -v -X GET -H "Content-Type: application/json"  -H "Cache-Control: no-cache" "http://localhost:9000/application/327d9145-4965-4d28-a2c5-39dedee50334/context/ciao-api/version/1.0"

#### Delete subscription fields by application id, api context, api version
curl -v -X DELETE -H "Cache-Control: no-cache" "http://localhost:9000/application/327d9145-4965-4d28-a2c5-39dedee50334/context/ciao-api/version/1.0"


## Tests
Some tests require MongoDB to run. 
Thus, remember to start up MongoDB if you want to run the tests locally.
There are unit tests, integration tests and acceptance tests plus code coverage reports are generated too.
In order to run them, use this command line:
```
./precheck.sh
```


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
