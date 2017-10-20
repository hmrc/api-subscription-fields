# API Subscription Fields

[![Build Status](https://travis-ci.org/hmrc/api-subscription-fields.svg)](https://travis-ci.org/hmrc/api-subscription-fields) [ ![Download](https://api.bintray.com/packages/hmrc/releases/api-subscription-fields/images/download.svg) ](https://bintray.com/hmrc/releases/api-subscription-fields/_latestVersion)

This microservice stores definitions and values for the HMRC Developer Hub.


### Examples of requests on the API subscription fields definitions

#### Creates or updates the definitions of subscriptions fields for an API
```
curl -v -X PUT "http://localhost:9650/definition/context/ciao-api/version/1.0" -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{ "fieldDefinitions": [ { "name": "callback-url", "description": "Callback URL", "type": "URL" }, { "name": "token", "description": "Secure Token", "type": "SecureToken" } ] }'
```

#### Retrieves the definitions of subscription fields for an API
```
curl -v -X GET "http://localhost:9650/definition/context/ciao-api/version/1.0" -H "Cache-Control: no-cache"
```

#### Retrieves the definitions of subscription fields for all APIs  
```
curl -v -X GET "http://localhost:9650/definition" -H "Cache-Control: no-cache"
```


### Examples of requests on the API subscription field values

#### Creates or updates the field values of an API subscription
```
curl -v -X PUT "http://localhost:9650/field/application/xp5036mSZooNOlD0Nfjz7LKnCy0a/context/ciao-api/version/1.0" -H "Content-Type: application/json" -H "Cache-Control: no-cache" -d '{ "fields" : { "callback-url" : "http://localhost:8080/url" , "token" : "abcDEF189q" } }'
```

#### Retrieves the field values of an API subscription by providing the `fieldsId`
```
curl -v -X GET "http://localhost:9650/field/f121ffa3-df94-43a0-8235-ac4530f9700a" -H "Cache-Control: no-cache"
```

#### Retrieves the field values of an API subscription by providing the application and API details
```
curl -v -X GET "http://localhost:9650/field/application/xp5036mSZooNOlD0Nfjz7LKnCy0a/context/ciao-api/version/1.0" -H "Cache-Control: no-cache"
```

#### Retrieves the field values of all API subscriptions related to a specific application
```
curl -v -X GET "http://localhost:9650/field/application/xp5036mSZooNOlD0Nfjz7LKnCy0a" -H "Cache-Control: no-cache"
```

#### Deletes the field values of an API subscription
```
curl -v -X DELETE "http://localhost:9650/field/application/xp5036mSZooNOlD0Nfjz7LKnCy0a/context/ciao-api/version/1.0" -H "Cache-Control: no-cache"
```


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
