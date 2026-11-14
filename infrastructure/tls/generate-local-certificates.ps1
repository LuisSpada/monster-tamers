[CmdletBinding()]
param(
    [string]$Password,
    [switch]$Force
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repositoryRoot = Resolve-Path (
    Join-Path $PSScriptRoot "..\.."
)

$outputRoot = Join-Path `
    $repositoryRoot `
    "infrastructure\runtime\tls"

$services = @(
    "gateway-service",
    "auth-service",
    "player-service",
    "monster-service",
    "encounter-service",
    "simulator-service"
)

function Assert-Command {
    param(
        [Parameter(Mandatory)]
        [string]$Name
    )

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "'$Name' was not found on PATH."
    }
}

function Invoke-Tool {
    param(
        [Parameter(Mandatory)]
        [string]$Command,

        [Parameter(Mandatory)]
        [string[]]$Arguments
    )

    & $Command @Arguments

    if ($LASTEXITCODE -ne 0) {
        throw "$Command failed with exit code $LASTEXITCODE."
    }
}

Assert-Command "openssl"
Assert-Command "keytool"
Assert-Command "git"

$trackedOutput = git `
    -C $repositoryRoot `
    ls-files `
    -- `
    "infrastructure/runtime/tls"

if ($trackedOutput) {
    throw (
        "Refusing to generate TLS material because files under " +
        "'infrastructure/runtime/tls' are tracked by Git."
    )
}

if (
    (Test-Path $outputRoot) -and
    -not $Force
) {
    throw (
        "TLS output already exists at '$outputRoot'. " +
        "Run with -Force to replace it."
    )
}

if (Test-Path $outputRoot) {
    Remove-Item `
        $outputRoot `
        -Recurse `
        -Force
}

New-Item `
    -ItemType Directory `
    -Path $outputRoot `
    -Force |
    Out-Null

if ([string]::IsNullOrWhiteSpace($Password)) {
    $first = Read-Host `
        "Enter the local TLS password" `
        -AsSecureString

    $second = Read-Host `
        "Repeat the local TLS password" `
        -AsSecureString

    $firstValue = [System.Net.NetworkCredential]::new(
        "",
        $first
    ).Password

    $secondValue = [System.Net.NetworkCredential]::new(
        "",
        $second
    ).Password

    if ($firstValue -cne $secondValue) {
        throw "The passwords do not match."
    }

    $Password = $firstValue
}

if ($Password.Length -lt 12) {
    throw "Use a local TLS password with at least 12 characters."
}

$passwordFile = Join-Path `
    $outputRoot `
    "tls-password.txt"

if ($Password -notmatch '^[\x21-\x7E]+$') {
    throw (
        "The TLS password must contain only printable ASCII characters. " +
        "Use letters, numbers and standard symbols without spaces."
    )
}

[System.IO.File]::WriteAllText(
    $passwordFile,
    $Password,
    [System.Text.UTF8Encoding]::new($false)
)

$caKey = Join-Path $outputRoot "ca.key"
$caCertificate = Join-Path $outputRoot "ca.crt"

Invoke-Tool "openssl" @(
    "genrsa",
    "-out",
    $caKey,
    "4096"
)

Invoke-Tool "openssl" @(
    "req",
    "-x509",
    "-new",
    "-sha256",
    "-days",
    "825",
    "-key",
    $caKey,
    "-out",
    $caCertificate,
    "-subj",
    "/CN=Monster Tamers Local Development CA"
)

$truststore = Join-Path `
    $outputRoot `
    "truststore.p12"

Invoke-Tool "keytool" @(
    "-importcert",
    "-noprompt",
    "-alias",
    "monster-tamers-local-ca",
    "-file",
    $caCertificate,
    "-keystore",
    $truststore,
    "-storetype",
    "PKCS12",
    "-storepass",
    $Password
)

foreach ($service in $services) {
    $serviceDirectory = Join-Path `
        $outputRoot `
        $service

    New-Item `
        -ItemType Directory `
        -Path $serviceDirectory `
        -Force |
        Out-Null

    $privateKey = Join-Path `
        $serviceDirectory `
        "server.key"

    $request = Join-Path `
        $serviceDirectory `
        "server.csr"

    $certificate = Join-Path `
        $serviceDirectory `
        "server.crt"

    $keystore = Join-Path `
        $serviceDirectory `
        "server.p12"

    $extensionFile = Join-Path `
        $serviceDirectory `
        "server.ext"

    @"
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=DNS:$service,DNS:localhost,IP:127.0.0.1
"@ |
        Set-Content `
            -Path $extensionFile `
            -Encoding ascii

    Invoke-Tool "openssl" @(
        "genrsa",
        "-out",
        $privateKey,
        "3072"
    )

    Invoke-Tool "openssl" @(
        "req",
        "-new",
        "-key",
        $privateKey,
        "-out",
        $request,
        "-subj",
        "/CN=$service"
    )

    Invoke-Tool "openssl" @(
        "x509",
        "-req",
        "-sha256",
        "-days",
        "397",
        "-in",
        $request,
        "-CA",
        $caCertificate,
        "-CAkey",
        $caKey,
        "-CAcreateserial",
        "-out",
        $certificate,
        "-extfile",
        $extensionFile
    )

    Invoke-Tool "openssl" @(
        "pkcs12",
        "-export",
        "-name",
        $service,
        "-inkey",
        $privateKey,
        "-in",
        $certificate,
        "-certfile",
        $caCertificate,
        "-out",
        $keystore,
        "-passout",
        "pass:$Password"
    )

    Remove-Item `
        $request, `
        $extensionFile `
        -Force
}

Write-Host ""
Write-Host "Generated local TLS material:"
Write-Host $outputRoot
Write-Host ""
Write-Host "The directory contains private keys and must not be committed."
Write-Host "Password file:"
Write-Host $passwordFile