docker run -d --name taier-db -m 1g -e MYSQL_DATABASE=taier -e MYSQL_ROOT_PASSWORD=123456 -e TZ=Asia/Shanghai -p 3306:3306 dtopensource/taier-mysql:latest

docker run -d --name taier-zk -m 256M -e ZOO_HEAP_SIZE=64m -p 2181:2181 zookeeper:3.4.9



telnet localhost 2181
echo stat | nc localhost 2181


docker run -p 8822:22 -d --name sftp --memory 250m emberstack/sftp



TAIER_CONF_DIR=/Users/jialiangcai/Personal/opensource/taier/conf
POST http://localhost:8090/taier/api/batchJob/startSqlImmediately
Accept: application/json
Cookie: userId=1; username=admin%40dtstack.com; isAdmin=1; token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0ZW5hbnRfaWQiOiIzIiwidXNlcl9pZCI6IjEiLCJ1c2VyX25hbWUiOiJhZG1pbkBkdHN0YWNrLmNvbSIsImV4cCI6MTcyNjIzODk2NCwiaWF0IjoxNzI0NDg2OTY0fQ.OkqFQTSIbCILmNmh1_oYtKrHIX2dPZ-dV_MVsYUIGUE; tenantId=3; tenant_name=test
Content-Type: application/json

{"taskVariables":[],"singleSession":false,"taskParams":"","uniqueKey":"1_1724738533662","sql":"select user_name from user limit 2","isEnd":true,"taskId":1}


###


POST http://localhost:8090/taier/api/batchJob/sessions
Accept: application/json
Cookie: userId=1; username=admin%40dtstack.com; isAdmin=1; token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0ZW5hbnRfaWQiOiIzIiwidXNlcl9pZCI6IjEiLCJ1c2VyX25hbWUiOiJhZG1pbkBkdHN0YWNrLmNvbSIsImV4cCI6MTcyNjIzODk2NCwiaWF0IjoxNzI0NDg2OTY0fQ.OkqFQTSIbCILmNmh1_oYtKrHIX2dPZ-dV_MVsYUIGUE; tenantId=3; tenant_name=test
Content-Type: application/json

{"taskVariables":[],"singleSession":false,"taskParams":"","uniqueKey":"1_1724738533662","sql":"select user_name from user limit 2","isEnd":true,"taskId":1}

<> 2024-08-27T152907.200.json



###
POST http://localhost:8090/taier/api/batchJob/a35ab5e9-fe6d-47e3-ac48-08beadb9bf26/operations/statement
Accept: application/json
Cookie: userId=1; username=admin%40dtstack.com; isAdmin=1; token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0ZW5hbnRfaWQiOiIzIiwidXNlcl9pZCI6IjEiLCJ1c2VyX25hbWUiOiJhZG1pbkBkdHN0YWNrLmNvbSIsImV4cCI6MTcyNjIzODk2NCwiaWF0IjoxNzI0NDg2OTY0fQ.OkqFQTSIbCILmNmh1_oYtKrHIX2dPZ-dV_MVsYUIGUE; tenantId=3; tenant_name=test
Content-Type: application/json

{"taskVariables":[],"singleSession":false,"taskParams":"","uniqueKey":"1_1724738533662","sql":"select * from user limit 2","isEnd":true,"taskId":1,"sessionHandle": "d4e83f98-3c7d-46f7-bc3b-d743b75e360a"}

<> 2024-08-27T153124.200.json





###
GET http://localhost:8090/taier/api/batchJob/6772d91a-c5f1-4c68-a597-02dda070e5d5/rowset
Accept: application/json
Cookie: userId=1; username=admin%40dtstack.com; isAdmin=1; token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0ZW5hbnRfaWQiOiIzIiwidXNlcl9pZCI6IjEiLCJ1c2VyX25hbWUiOiJhZG1pbkBkdHN0YWNrLmNvbSIsImV4cCI6MTcyNjIzODk2NCwiaWF0IjoxNzI0NDg2OTY0fQ.OkqFQTSIbCILmNmh1_oYtKrHIX2dPZ-dV_MVsYUIGUE; tenantId=3; tenant_name=test
Content-Type: application/json

{"taskVariables":[],"singleSession":false,"taskParams":"","uniqueKey":"1_1724738533662","sql":"select * from user limit 2","isEnd":true,"taskId":1,"operationHandle": "13df0ec9-2830-4bf2-be0d-70625d7bfe6d"}

<> 2024-08-27T153136.200.json
<> 2024-08-27T153106.200.json
<> 2024-08-27T153022.200.json

