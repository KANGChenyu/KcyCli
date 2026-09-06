$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

# Avoid inherited JVM options with broken dashes or stale encoding flags.
Remove-Item Env:JAVA_TOOL_OPTIONS -ErrorAction SilentlyContinue

# Make Windows Terminal / PowerShell and the JVM agree on UTF-8.
chcp 65001 | Out-Null
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
$OutputEncoding = [System.Text.UTF8Encoding]::new()

$logDir = Join-Path $env:USERPROFILE ".paicli\logs"
New-Item -ItemType Directory -Force $logDir | Out-Null

# Show the UI quickly; MCP servers continue starting in the background.
if ([string]::IsNullOrWhiteSpace($env:PAICLI_MCP_STARTUP_WAIT_SECONDS)) {
    $env:PAICLI_MCP_STARTUP_WAIT_SECONDS = "1"
}

$jar = Join-Path $projectRoot "target\paicli-1.0-SNAPSHOT.jar"
if (-not (Test-Path $jar)) {
    Write-Host "target\paicli-1.0-SNAPSHOT.jar not found, building first..."
    mvn clean package
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

java "-Dfile.encoding=UTF-8" `
     "-Dsun.stdout.encoding=UTF-8" `
     "-Dsun.stderr.encoding=UTF-8" `
     "-Dpaicli.log.dir=$logDir" `
     "--enable-native-access=ALL-UNNAMED" `
     -jar $jar

exit $LASTEXITCODE
