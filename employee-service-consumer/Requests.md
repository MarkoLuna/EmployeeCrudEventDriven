# Request as CURL

## Login as dev user 
```
curl --location 'http://localhost:8081/realms/dev/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=newClient' \
--data-urlencode 'client_secret=newClientSecret' \
--data-urlencode 'username=john@test.com' \
--data-urlencode 'password=123' \
--data-urlencode 'grant_type=password'
```

## List Users

```
curl --location 'localhost:8082/employees/0/10' \
--header 'Authorization: Bearer eyJhbGci...' 
```

## Get User By Id 
```
curl --location 'localhost:8082/employees/4274d437-e66a-4cc0-b0dc-2eb1dd433af5' \
--header 'Authorization: Bearer eyJhbGci...'
```

## Create User 
```
curl --location 'localhost:8082/employees' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsIn...' \
--data '{
    "firstName": "Mark",
    "middleInitial": "L",
    "lastName": "Luna",
    "dateOfBirth": "17-09-2012",
    "dateOfEmployment": "17-09-2014"
}'
```

## Update user 
```
curl --location --request PUT 'localhost:8082/employees/4274d437-e66a-4cc0-b0dc-2eb1dd433af5' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIg...' \
--data '{
    "firstName": "Mark2",
    "middleInitial": "L",
    "lastName": "Luna",
    "dateOfBirth": "17-09-2012",
    "dateOfEmployment": "17-09-2014"
}'
```

## Delete User 
```
curl --location --request DELETE 'localhost:8082/employees/1d5cf451-ffcd-403a-a8c0-8338d0ce381d' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1...'
```