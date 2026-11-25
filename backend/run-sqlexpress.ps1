# Runs the backend on a local SQL Server (port 1433, database claimsdb) with your Windows login.
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$dll = Join-Path $PSScriptRoot 'target\dll'
.\mvnw.cmd -q -ntp dependency:copy "-Dartifact=com.microsoft.sqlserver:mssql-jdbc_auth:12.10.2.x64:dll" "-DoutputDirectory=target/dll"
$env:DB_URL = 'jdbc:sqlserver://localhost:1433;databaseName=claimsdb;encrypt=true;trustServerCertificate=true;integratedSecurity=true'
$env:DB_USER = ''
$env:DB_PASSWORD = ''
.\mvnw.cmd -ntp spring-boot:run "-Dspring-boot.run.jvmArguments=-Djava.library.path=$dll"
