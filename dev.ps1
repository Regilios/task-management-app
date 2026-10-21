param(
    [Parameter(Position = 0)]
    [ValidateSet('doctor', 'test', 'verify', 'up', 'down', 'db', 'run', 'logs', 'status')]
    [string]$Action = 'doctor'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Invoke-Checked {
    param([string]$Command, [string[]]$Arguments)
    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Command failed (exit $LASTEXITCODE). See output above." }
}

function Require-Docker {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw 'Docker Desktop is not installed or not on PATH.'
    }
    Invoke-Checked 'docker' @('info', '--format', '{{.OSType}}')
    Invoke-Checked 'docker' @('compose', 'version')
}

Push-Location $PSScriptRoot
try {
    if (-not (Test-Path -LiteralPath '.env')) { Copy-Item -LiteralPath '.env.example' -Destination '.env' }
    switch ($Action) {
        'doctor' {
            Invoke-Checked 'java' @('-version')
            Invoke-Checked '.\mvnw.cmd' @('-version')
            Require-Docker
            Invoke-Checked 'docker' @('compose', 'config', '--quiet')
            Write-Host 'Toolchain OK. Run .\dev.ps1 test, then .\dev.ps1 verify.'
        }
        'test' { Invoke-Checked '.\mvnw.cmd' @('-B', '-ntp', '-pl', 'core', 'test') }
        'verify' {
            Require-Docker
            Invoke-Checked '.\mvnw.cmd' @('-B', '-ntp', '-Pintegration', 'verify')
        }
        'up' {
            Require-Docker
            Invoke-Checked 'docker' @('compose', 'up', '-d', '--build', '--wait', '--wait-timeout', '180')
            Write-Host 'Ready. Default readiness URL: http://127.0.0.1:18080/actuator/health/readiness (APP_PORT in .env).'
        }
        'db' {
            Require-Docker
            Invoke-Checked 'docker' @('compose', 'up', '-d', '--wait', 'postgres')
        }
        'run' {
            Require-Docker
            Invoke-Checked 'docker' @('compose', 'stop', 'app')
            Invoke-Checked 'docker' @('compose', 'up', '-d', '--wait', 'postgres')
            Invoke-Checked '.\mvnw.cmd' @('-B', '-ntp', 'package')
            Invoke-Checked 'java' @('-jar', 'app/target/stockflow.jar')
        }
        'down' { Invoke-Checked 'docker' @('compose', 'down') }
        'logs' { Invoke-Checked 'docker' @('compose', 'logs', '--tail', '80', '-f', 'app') }
        'status' { Invoke-Checked 'docker' @('compose', 'ps') }
    }
}
finally { Pop-Location }
