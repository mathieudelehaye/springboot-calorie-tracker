# Docker Deployment Guide

## 🚀 Quick Start (5 Minutes)

### 1. Setup Configuration
```powershell
# Copy the environment template
Copy-Item -Path "docker.env.example" -Destination "config/env.properties"

# Edit with your actual database credentials
notepad config/env.properties  # Windows
# or code config/env.properties for VS Code
```

### 2. Run Both Applications
```powershell
# Build and start both applications
docker-compose up --build

# Access applications:
# 🍎 Diet Manager: http://localhost:8080
# ⚙️ Food Admin: http://localhost:8081
```

### 3. Health Checks
```powershell
# Using PowerShell's Invoke-WebRequest
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health"
Invoke-WebRequest -Uri "http://localhost:8081/actuator/health"
```

**That's it!** Both applications are now running in Docker containers.

---

## 📋 Complete Guide

This guide covers local Docker testing and Azure Container Apps deployment for both the **Diet Manager App** and **Food Categories Admin App**.

### Architecture Overview

The project consists of two Spring Boot applications:

- **🍎 Diet Manager App** (`calorie-tracker-app`) - Port 8080
  - Main application for coaches to manage athletes' diet plans
  - Uses both primary database and food categories database
  
- **⚙️ Food Categories Admin** (`food-categories-admin`) - Port 8081
  - Admin interface to manage food categories
  - Uses only the food categories database

### Prerequisites

- Docker Desktop installed and running
- Your Neon database credentials (both primary DB and food categories DB)
- Azure CLI (for Azure Container Apps deployment)
- PowerShell 7+ installed

---

## 🐳 Local Docker Deployment

### Database Configuration

**Option A: Use Existing Config File (Recommended)**

Update your existing `config/env.properties` file with database credentials:

```properties
# =================================================================
# CALORIE TRACKER APP (Main Application) - Port 8080
# =================================================================
# Primary Database (athletes, coaches, days, meals, foods)
SPRING_DATASOURCE_PRIMARY_URL=jdbc:postgresql://your-primary-db-host.aws.neon.tech:5432/neondb?sslmode=require
SPRING_DATASOURCE_PRIMARY_USERNAME=your-username
SPRING_DATASOURCE_PRIMARY_PASSWORD=your-password

# Food Categories Database (for cross-database lookups)
SPRING_DATASOURCE_FOODCATEGORIES_URL=jdbc:postgresql://your-foodcategories-db-host.aws.neon.tech:5432/neondb?sslmode=require
SPRING_DATASOURCE_FOODCATEGORIES_USERNAME=your-username
SPRING_DATASOURCE_FOODCATEGORIES_PASSWORD=your-password

# =================================================================
# FOOD CATEGORIES ADMIN APP - Port 8081
# =================================================================
# Food Categories Database (primary database for admin app)
SPRING_DATASOURCE_FOODCATEGORY_URL=jdbc:postgresql://your-foodcategories-db-host.aws.neon.tech:5432/neondb?sslmode=require
SPRING_DATASOURCE_FOODCATEGORY_USERNAME=your-username
SPRING_DATASOURCE_FOODCATEGORY_PASSWORD=your-password
```

**Option B: Create from Template**

```powershell
# Copy the template
Copy-Item -Path "docker.env.example" -Destination "config/env.properties"

# Edit with your preferred editor
notepad config/env.properties  # or code config/env.properties for VS Code
```

### Build and Run Both Applications

```powershell
# Build and start both applications
docker-compose up --build

# Run in background
docker-compose up --build -d

# View logs for both services
docker-compose logs -f

# View logs for specific service
docker-compose logs -f calorie-tracker
docker-compose logs -f food-categories-admin

# Stop both applications
docker-compose down
```

### Access Applications

- **🍎 Diet Manager App**: http://localhost:8080
  - Health Check: http://localhost:8080/actuator/health
  
- **⚙️ Food Categories Admin**: http://localhost:8081  
  - Health Check: http://localhost:8081/actuator/health

### Individual Docker Commands (Alternative)

#### Build Individual Images

```powershell
# Build Diet Manager App
./gradlew :calorie-tracker-app:build
docker build -f calorie-tracker-app/Dockerfile -t diet-manager:latest .

# Build Food Categories Admin
./gradlew :food-categories-admin:build  
docker build -f food-categories-admin/Dockerfile -t food-admin:latest .
```

#### Run with Environment Variables (Secure Method)

```powershell
# Diet Manager App
docker run -d `
    --name diet-manager `
    -p 8080:8080 `
    -v ${PWD}/config:/app/config:ro `
    -e SPRING_PROFILES_ACTIVE=docker `
    -e SPRING_CONFIG_IMPORT="optional:file:/app/config/env.properties" `
    diet-manager:latest

# Food Categories Admin
docker run -d `
    --name food-admin `
    -p 8081:8081 `
    -v ${PWD}/config:/app/config:ro `
    -e SPRING_PROFILES_ACTIVE=docker `
    -e SPRING_CONFIG_IMPORT="optional:file:/app/config/env.properties" `
    food-admin:latest
```

**🔒 Security Benefits:**
- Credentials are **NOT baked into Docker images**
- Configuration is **mounted at runtime** from external file
- Images can be safely shared without exposing secrets

---

## 🔧 Management & Troubleshooting

### Local Docker Management

```powershell
# Check running containers
docker ps

# View logs
docker-compose logs -f

# Restart services
docker-compose restart

# Update and restart
docker-compose up --build -d

# Clean up
docker-compose down
docker system prune -f
```

### Common Troubleshooting

**Build fails?**
```powershell
# Clean and rebuild
./gradlew clean build
docker-compose up --build --force-recreate
```

**Can't connect to database?**
- Check your `config/env.properties` file has correct credentials
- Ensure your Neon databases are not sleeping (free tier auto-suspends)

**Port conflicts?**
```powershell
# List running containers
docker ps

# Stop all running containers
$containers = docker ps -q
if ($containers) {
    docker stop $containers
}

# Alternative: Stop specific ports
Get-NetTCPConnection -LocalPort 8080,8081 | ForEach-Object {
    Stop-Process -Id $_.OwningProcess -Force
}
```

### Azure Container Apps Management

```powershell
# Check app status
az containerapp show `
    --name diet-manager-app `
    --resource-group calorie-tracker-rg

az containerapp show `
    --name food-admin-app `
    --resource-group calorie-tracker-rg

# View logs
az containerapp logs show `
    --name diet-manager-app `
    --resource-group calorie-tracker-rg

az containerapp logs show `
    --name food-admin-app `
    --resource-group calorie-tracker-rg

# Scale applications
az containerapp update `
    --name diet-manager-app `
    --resource-group calorie-tracker-rg `
    --min-replicas 2 `
    --max-replicas 5

az containerapp update `
    --name food-admin-app `
    --resource-group calorie-tracker-rg `
    --min-replicas 1 `
    --max-replicas 3

# Update applications (after pushing new images)
az containerapp update `
    --name diet-manager-app `
    --resource-group calorie-tracker-rg `
    --image "$acrName.azurecr.io/diet-manager:latest"

az containerapp update `
    --name food-admin-app `
    --resource-group calorie-tracker-rg `
    --image "$acrName.azurecr.io/food-admin:latest"

# Delete applications (cleanup)
az containerapp delete `
    --name diet-manager-app `
    --resource-group calorie-tracker-rg

az containerapp delete `
    --name food-admin-app `
    --resource-group calorie-tracker-rg

az group delete `
    --name calorie-tracker-rg
```

---

## 📊 Monitoring and Health Checks

### Health Check Endpoints

```powershell
# Test health endpoints
$dietManagerUrl = "https://your-diet-manager-url/actuator/health"
$foodAdminUrl = "https://your-food-admin-url/actuator/health"

Invoke-WebRequest -Uri $dietManagerUrl
Invoke-WebRequest -Uri $foodAdminUrl
```

### Azure Application Insights (Optional)

Add Application Insights for monitoring:

```powershell
# Create Application Insights
az monitor app-insights component create `
    --app calorie-tracker-insights `
    --location eastus `
    --resource-group calorie-tracker-rg

# Get instrumentation key
$instrumentationKey = az monitor app-insights component show `
    --app calorie-tracker-insights `
    --resource-group calorie-tracker-rg `
    --query instrumentationKey `
    --output tsv

# Update container apps with Application Insights
az containerapp update `
    --name diet-manager-app `
    --resource-group calorie-tracker-rg `
    --set-env-vars APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=$instrumentationKey"
```

---

## 🔒 Security Best Practices

✅ **Implemented:**
- Credentials stored as Azure secrets (not in images)
- Non-root user in Docker containers
- Read-only config volume mounts
- Network isolation with Container Apps environment

✅ **Recommended:**
- Enable Azure Container Registry vulnerability scanning
- Use Azure Key Vault for sensitive secrets
- Configure network access restrictions
- Enable audit logging

---

## 🚀 CI/CD Integration

### GitHub Actions Example

```yaml
name: Deploy to Azure Container Apps

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: windows-latest  # Using Windows runner for PowerShell
    steps:
      - uses: actions/checkout@v2
      
      - name: Login to Azure
        uses: azure/login@v1
        with:
          creds: ${{ secrets.AZURE_CREDENTIALS }}
          
      - name: Build and deploy
        shell: pwsh
        run: |
          # Build applications
          ./gradlew build
          
          # Push images
          az acr login --name calorietrackeracr
          docker build -f calorie-tracker-app/Dockerfile -t "$env:ACR_NAME.azurecr.io/diet-manager:latest" .
          docker build -f food-categories-admin/Dockerfile -t "$env:ACR_NAME.azurecr.io/food-admin:latest" .
          docker push "$env:ACR_NAME.azurecr.io/diet-manager:latest"
          docker push "$env:ACR_NAME.azurecr.io/food-admin:latest"
          
          # Update container apps
          az containerapp update `
              --name diet-manager-app `
              --resource-group calorie-tracker-rg `
              --image "$env:ACR_NAME.azurecr.io/diet-manager:latest"
          
          az containerapp update `
              --name food-admin-app `
              --resource-group calorie-tracker-rg `
              --image "$env:ACR_NAME.azurecr.io/food-admin:latest"
```

---

## 📝 Summary

**Local Development:**
- Use `docker-compose up --build` for both apps
- Access Diet Manager at `http://localhost:8080`
- Access Food Admin at `http://localhost:8081`

**Azure Production:**
- Both apps deployed as separate Container Apps
- Secrets managed securely with Azure
- Auto-scaling and health monitoring included
- URLs provided after deployment

**Security:**
- No credentials in Docker images
- External configuration management
- Azure secrets integration 

## Local Deployment

### Prerequisites
- Docker Desktop installed
- Docker Compose installed
- Azure CLI installed
- PowerShell 7+ installed

### Local Setup
1. Copy `docker.env.example` to `config/env.properties`
2. Update database credentials in `config/env.properties`
3. Build and run locally:
```powershell
# Copy config file
Copy-Item -Path "docker.env.example" -Destination "config/env.properties"

# Start containers
docker-compose up -d
```

## Azure Deployment

### Prerequisites
- Azure subscription
- Azure CLI installed and logged in
- Azure Container Registry (ACR) access

### 1. Login to Azure and Set Subscription
```powershell
# Login to Azure
az login

# List subscriptions
az account list --output table

# Set your subscription (PowerShell variables)
$subscriptionId = "<your-subscription-id>"
$resourceGroup = "calorie-tracker-rg"
$location = "eastus"
$acrName = "calorietrackeracr"
$storageAccount = "<storage-account-name>"

# Set subscription
az account set --subscription $subscriptionId
```

### 2. Create Azure Container Registry
```powershell
# Create resource group
az group create --name $resourceGroup --location $location

# Create container registry
az acr create `
    --resource-group $resourceGroup `
    --name $acrName `
    --sku Basic

# Login to ACR
az acr login --name $acrName

# Get ACR credentials
$acrCredentials = az acr credential show `
    --name $acrName `
    --resource-group $resourceGroup | ConvertFrom-Json

$acrUsername = $acrCredentials.username
$acrPassword = $acrCredentials.passwords[0].value
```

### 3. Build and Push Docker Images
```powershell
# Set your ACR name
$acrName = "calorietrackeracr"  # Replace with your actual ACR name

# Login to Azure (if not already logged in)
az login

# Login to Azure Container Registry
az acr login --name $acrName

# Build and tag the images (make sure to include the dot at the end for build context)
docker build -t "$acrName.azurecr.io/calorie-tracker:latest" -f calorie-tracker-app/Dockerfile .
docker build -t "$acrName.azurecr.io/food-categories-admin:latest" -f food-categories-admin/Dockerfile .

# Push images to ACR (will only work after successful ACR login)
docker push "$acrName.azurecr.io/calorie-tracker:latest"
docker push "$acrName.azurecr.io/food-categories-admin:latest"

# Verify the images are in ACR
az acr repository list --name $acrName --output table
```

**Note:** If you get a 401 Unauthorized error, it means you need to:
1. Make sure you're logged into Azure (`az login`)
2. Make sure you're logged into ACR (`az acr login --name $acrName`)
3. Verify you have proper permissions on the ACR
4. Check that your ACR name is correct

### 4. Create Azure Container Instances (ACI) on Different Nodes

#### Create Config File Secret
```powershell
# Convert config file to base64
$configContent = Get-Content -Path "config/env.properties" -Raw
$configBase64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($configContent))
Set-Content -Path "config.b64" -Value $configBase64

# Create Key Vault and store secret
az keyvault create `
    --name "calorie-tracker-kv" `
    --resource-group $resourceGroup `
    --location $location

az keyvault secret set `
    --vault-name "calorie-tracker-kv" `
    --name "env-properties" `
    --file "config.b64"

# Create storage account and file share
az storage account create `
    --name $storageAccount `
    --resource-group $resourceGroup `
    --location $location `
    --sku Standard_LRS

# Get storage key
$storageKey = (az storage account keys list `
    --resource-group $resourceGroup `
    --account-name $storageAccount `
    --query "[0].value" `
    --output tsv)

# Create file share
az storage share create `
    --name "config" `
    --account-name $storageAccount `
    --account-key $storageKey
```

#### Deploy Calorie Tracker App
```powershell
# Create container instance for calorie tracker
az container create `
    --resource-group $resourceGroup `
    --name "calorie-tracker" `
    --image "$acrName.azurecr.io/calorie-tracker:latest" `
    --cpu 1 `
    --memory 1.5 `
    --registry-login-server "$acrName.azurecr.io" `
    --registry-username $acrUsername `
    --registry-password $acrPassword `
    --dns-name-label "calorie-tracker-app" `
    --ports 8080 `
    --environment-variables `
        SPRING_PROFILES_ACTIVE=docker `
        SPRING_CONFIG_IMPORT="optional:file:/app/config/env.properties" `
        SPRING_JPA_SHOW_SQL=false `
    --azure-file-volume-account-name $storageAccount `
    --azure-file-volume-account-key $storageKey `
    --azure-file-volume-share-name "config" `
    --azure-file-volume-mount-path "/app/config" `
    --location $location
```

#### Deploy Food Categories Admin
```powershell
# Create container instance for food categories admin (different region)
az container create `
    --resource-group $resourceGroup `
    --name "food-categories-admin" `
    --image "$acrName.azurecr.io/food-categories-admin:latest" `
    --cpu 1 `
    --memory 1.5 `
    --registry-login-server "$acrName.azurecr.io" `
    --registry-username $acrUsername `
    --registry-password $acrPassword `
    --dns-name-label "food-categories-admin" `
    --ports 8081 `
    --environment-variables `
        SPRING_PROFILES_ACTIVE=docker `
        SPRING_CONFIG_IMPORT="optional:file:/app/config/env.properties" `
        SPRING_JPA_SHOW_SQL=false `
    --azure-file-volume-account-name $storageAccount `
    --azure-file-volume-account-key $storageKey `
    --azure-file-volume-share-name "config" `
    --azure-file-volume-mount-path "/app/config" `
    --location "westus"
```

### 5. Get Container Status and URLs
```powershell
# Get calorie tracker status and URL
az container show `
    --resource-group $resourceGroup `
    --name "calorie-tracker" `
    --query "{Status:instanceView.state, FQDN:ipAddress.fqdn, IP:ipAddress.ip}" `
    --output table

# Get food categories admin status and URL
az container show `
    --resource-group $resourceGroup `
    --name "food-categories-admin" `
    --query "{Status:instanceView.state, FQDN:ipAddress.fqdn, IP:ipAddress.ip}" `
    --output table
```

### 6. Access Your Applications
- Calorie Tracker: `http://<calorie-tracker-fqdn>:8080`
- Food Categories Admin: `http://<food-categories-admin-fqdn>:8081`

### Important Notes
1. PowerShell backtick (`) is used for line continuation
2. Variables are prefixed with $
3. The containers are deployed to different regions (eastus and westus)
4. Both containers share the same config file through Azure File Share
5. Each container has its own public DNS name and IP
6. Container instances are configured with 1 CPU and 1.5GB memory each

### Monitoring and Logs
```powershell
# Get logs for calorie tracker
az container logs `
    --resource-group $resourceGroup `
    --name "calorie-tracker"

# Get logs for food categories admin
az container logs `
    --resource-group $resourceGroup `
    --name "food-categories-admin"

# Stream logs (PowerShell 7+)
az container attach `
    --resource-group $resourceGroup `
    --name "calorie-tracker"
```

### Cleanup
```powershell
# Delete container instances
az container delete `
    --resource-group $resourceGroup `
    --name "calorie-tracker" `
    --yes

az container delete `
    --resource-group $resourceGroup `
    --name "food-categories-admin" `
    --yes

# Delete resource group (this deletes everything including ACR)
az group delete --name $resourceGroup --yes

# Remove local config
Remove-Item -Path "config.b64" -Force
```

### Set Up Database Credentials (Using Container Apps Secrets)

```powershell
# Set your resource names
$resourceGroup = "calorie-tracker-rg"
$environmentName = "calorie-tracker-env"

# Create secrets directly in Container Apps environment
az containerapp env secret set `
    --name $environmentName `
    --resource-group $resourceGroup `
    --secrets `
        "primary-db-url=jdbc:postgresql://your-primary-db-host:5432/neondb?sslmode=require" `
        "primary-db-username=your-username" `
        "primary-db-password=your-password" `
        "foodcategories-db-url=jdbc:postgresql://your-foodcategories-db-host:5432/neondb?sslmode=require" `
        "foodcategories-db-username=your-username" `
        "foodcategories-db-password=your-password"

# Deploy Calorie Tracker App with secrets
az containerapp create `
    --name "calorie-tracker" `
    --resource-group $resourceGroup `
    --environment $environmentName `
    --image "$acrName.azurecr.io/calorie-tracker:latest" `
    --target-port 8080 `
    --ingress "external" `
    --registry-server "$acrName.azurecr.io" `
    --registry-username $acrUsername `
    --registry-password $acrPassword `
    --min-replicas 1 `
    --max-replicas 3 `
    --env-vars `
        "SPRING_PROFILES_ACTIVE=docker" `
        "SPRING_DATASOURCE_PRIMARY_URL=secretref:primary-db-url" `
        "SPRING_DATASOURCE_PRIMARY_USERNAME=secretref:primary-db-username" `
        "SPRING_DATASOURCE_PRIMARY_PASSWORD=secretref:primary-db-password" `
        "SPRING_DATASOURCE_FOODCATEGORIES_URL=secretref:foodcategories-db-url" `
        "SPRING_DATASOURCE_FOODCATEGORIES_USERNAME=secretref:foodcategories-db-username" `
        "SPRING_DATASOURCE_FOODCATEGORIES_PASSWORD=secretref:foodcategories-db-password"

# Deploy Food Categories Admin with secrets
az containerapp create `
    --name "food-categories-admin" `
    --resource-group $resourceGroup `
    --environment $environmentName `
    --image "$acrName.azurecr.io/food-categories-admin:latest" `
    --target-port 8081 `
    --ingress "external" `
    --registry-server "$acrName.azurecr.io" `
    --registry-username $acrUsername `
    --registry-password $acrPassword `
    --min-replicas 1 `
    --max-replicas 3 `
    --env-vars `
        "SPRING_PROFILES_ACTIVE=docker" `
        "SPRING_DATASOURCE_FOODCATEGORY_URL=secretref:foodcategories-db-url" `
        "SPRING_DATASOURCE_FOODCATEGORY_USERNAME=secretref:foodcategories-db-username" `
        "SPRING_DATASOURCE_FOODCATEGORY_PASSWORD=secretref:foodcategories-db-password"
```

**Important Notes:**
1. Secrets are stored directly in the Container Apps environment
2. Both apps share the same environment and secrets
3. Use `secretref:` to reference secrets in environment variables
4. No need for Key Vault or managed identities
5. Much simpler than Key Vault approach!

**To update a secret:**
```powershell
# Update secrets in Container Apps environment
az containerapp env secret set `
    --name $environmentName `
    --resource-group $resourceGroup `
    --secrets "primary-db-password=new-password"
```

**To list current secrets:**
```powershell
# List all secrets (shows only names, not values)
az containerapp env secret list `
    --name $environmentName `
    --resource-group $resourceGroup `
    --output table
```

## Latest Azure Container Apps Deployment (January 2025)

### 1. Build and Push Docker Images
```powershell
# Build app 1 Docker image
docker build `
    -t calorietrackeracr.azurecr.io/calorie-tracker-app:v1 `
    -f calorie-tracker-app/Dockerfile `
    .

# Push app 1 to Azure Container Registry
docker push calorietrackeracr.azurecr.io/calorie-tracker-app:v1

# Build app 2 Docker image
docker build `
    -t calorietrackeracr.azurecr.io/food-categories-admin:v1 `
    -f food-categories-admin/Dockerfile `
    .

# Push app 2 to Azure Container Registry
docker push calorietrackeracr.azurecr.io/food-categories-admin:v1
```

### 2. Create Container Apps

```powershell
# Create app 1 (Calorie Tracker)
az containerapp create `
  --name calorie-tracker-app `
  --resource-group calorie-tracker-rg `
  --environment calorie-tracker-env `
  --image calorietrackeracr.azurecr.io/calorie-tracker-app:v1 `
  --target-port 8080 `
  --ingress external `
  --registry-server calorietrackeracr.azurecr.io `
  --cpu 1.0 `
  --memory 2.0Gi `
  --min-replicas 1 `
  --max-replicas 3

# Create app 2 (Food Categories Admin)
az containerapp create `
  --name food-categories-admin `
  --resource-group calorie-tracker-rg `
  --environment calorie-tracker-env `
  --image calorietrackeracr.azurecr.io/food-categories-admin:v1 `
  --target-port 8080 `
  --ingress external `
  --registry-server calorietrackeracr.azurecr.io `
  --cpu 1.0 `
  --memory 2.0Gi `
  --min-replicas 1 `
  --max-replicas 3
```

### 3. Set Database Secrets and Environment Variables

```powershell
# Set secrets for app 1
az containerapp secret set `
  --name "calorie-tracker-app" `
  --resource-group "calorie-tracker-rg" `
  --secrets `
    primary-db-url="jdbc:postgresql://ep-lively-moon-a9lmw6at-pooler.gwc.azure.neon.tech:5432/calorie-tracker?sslmode=require" `
    primary-db-username="XXX" `
    primary-db-password="XXX"

# Update app 1 environment variables
az containerapp update `
  --name calorie-tracker-app `
  --resource-group calorie-tracker-rg `
  --set-env-vars `
    "spring.datasource.url=secretref:primary-db-url" `
    "spring.datasource.username=secretref:primary-db-username" `
    "spring.datasource.password=secretref:primary-db-password"

# Set secrets for app 2
az containerapp secret set `
  --name "food-categories-admin" `
  --resource-group "calorie-tracker-rg" `
  --secrets `
    primary-db-url="jdbc:postgresql://ep-lively-moon-a9lmw6at-pooler.gwc.azure.neon.tech:5432/calorie-tracker?sslmode=require" `
    primary-db-username="XXX" `
    primary-db-password="XXX" `
    foodcategories-db-url="jdbc:postgresql://ep-purple-fire-a98jliex-pooler.gwc.azure.neon.tech:5432/food-category?sslmode=require" `
    foodcategories-db-username="XXX" `
    foodcategories-db-password="XXX"

# Update app 2 environment variables
az containerapp update `
  --name food-categories-admin `
  --resource-group calorie-tracker-rg `
  --set-env-vars `
    "spring.datasource.primary.url=secretref:primary-db-url" `
    "spring.datasource.primary.username=secretref:primary-db-username" `
    "spring.datasource.primary.password=secretref:primary-db-password" `
    "spring.datasource.foodcategories.url=secretref:foodcategories-db-url" `
    "spring.datasource.foodcategories.username=secretref:foodcategories-db-username" `
    "spring.datasource.foodcategories.password=secretref:foodcategories-db-password"
```

### 4. Update Container Apps (After Code Changes)

```powershell
# Update app 1
az containerapp update `
  --name calorie-tracker-app `
  --resource-group calorie-tracker-rg `
  --revision-suffix update1

# Update app 2
az containerapp update `
  --name food-categories-admin `
  --resource-group calorie-tracker-rg `
  --revision-suffix update1
```

### 5. View Application Logs

```powershell
# View app 1 logs
az containerapp logs show `
  --name calorie-tracker-app `
  --resource-group calorie-tracker-rg `
  --follow

# View app 2 logs
az containerapp logs show `
  --name food-categories-admin `
  --resource-group calorie-tracker-rg `
  --follow
```

### 6. Access Applications

- Calorie Tracker App: https://calorie-tracker-app.wittyflower-c2822a5a.eastus.azurecontainerapps.io
- Food Categories Admin: https://food-categories-admin.wittyflower-c2822a5a.eastus.azurecontainerapps.io

ls 