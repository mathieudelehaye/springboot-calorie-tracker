# Docker Deployment Guide

This guide covers local Docker testing and Azure Container Apps deployment for the SpringBoot Calorie Tracker application.

## Prerequisites

- Docker Desktop installed and running
- Your Neon database credentials (both main DB and food categories DB)
- Azure CLI (for Azure Container Apps deployment)

## Local Docker Testing

### 1. Use Existing Database Configuration

**Option A: Use your existing `config/env.properties` file (Recommended)**

Your application already uses `config/env.properties` with your Neon database credentials:
```properties
# Primary Database (athletes, coaches, days, foods, meals)
SPRING_DATASOURCE_PRIMARY_URL=jdbc:postgresql://your-primary-db-host/neondb?sslmode=require
SPRING_DATASOURCE_PRIMARY_USERNAME=your-username
SPRING_DATASOURCE_PRIMARY_PASSWORD=your-password

# Food Categories Database (food categories only)
SPRING_DATASOURCE_FOODCATEGORIES_URL=jdbc:postgresql://your-foodcategories-db-host/neondb?sslmode=require
SPRING_DATASOURCE_FOODCATEGORIES_USERNAME=your-username
SPRING_DATASOURCE_FOODCATEGORIES_PASSWORD=your-password
```

No additional setup needed! Docker will use these existing credentials.

**Option B: Create separate `.env` file (Alternative)**

If you prefer to keep Docker credentials separate:
1. Copy the example: `cp docker.env.example .env`
2. Edit `.env` with your Neon credentials:
   ```env
   # Main Database (Neon DB1)
   MAIN_DB_URL=jdbc:postgresql://ep-xxx-xxx.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
   MAIN_DB_USERNAME=your-username
   MAIN_DB_PASSWORD=your-password

   # Food Categories Database (Neon DB2)
   FOODCATEGORY_DB_URL=jdbc:postgresql://ep-yyy-yyy.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
   FOODCATEGORY_DB_USERNAME=your-username
   FOODCATEGORY_DB_PASSWORD=your-password
   ```

### 2. Build and Run with Docker Compose

```bash
# Build and start the application
docker-compose up --build

# Run in background
docker-compose up --build -d

# View logs
docker-compose logs -f calorie-tracker

# Stop the application
docker-compose down
```

### 3. Test the Application

- **Application URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Login Credentials**: Use your existing coach credentials from the database

### 4. Direct Docker Commands with Environment Variables (Secure Method)

#### Linux/macOS (Bash)

```bash
# Build the image
cd app
docker build -t calorie-tracker:latest .

# Run the container with environment variables (SECURE - no credentials in image)
docker run -d \
  --name calorie-tracker \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_PRIMARY_URL="jdbc:postgresql://your-primary-host/neondb?sslmode=require" \
  -e SPRING_DATASOURCE_PRIMARY_USERNAME="your-username" \
  -e SPRING_DATASOURCE_PRIMARY_PASSWORD="your-new-password" \
  -e SPRING_DATASOURCE_FOODCATEGORIES_URL="jdbc:postgresql://your-foodcategories-host/neondb?sslmode=require" \
  -e SPRING_DATASOURCE_FOODCATEGORIES_USERNAME="your-username" \
  -e SPRING_DATASOURCE_FOODCATEGORIES_PASSWORD="your-new-password" \
  -e SPRING_PROFILES_ACTIVE=docker \
  calorie-tracker:latest

# Check container status
docker ps

# View logs
docker logs calorie-tracker

# Stop and remove container
docker stop calorie-tracker
docker rm calorie-tracker
```

#### Windows PowerShell

```powershell
# Build the image
cd app
docker build -t calorie-tracker:latest .

# Run the container with environment variables (SECURE - no credentials in image)
docker run -d `
  --name calorie-tracker `
  -p 8080:8080 `
  -e SPRING_DATASOURCE_PRIMARY_URL="jdbc:postgresql://your-primary-host/neondb?sslmode=require" `
  -e SPRING_DATASOURCE_PRIMARY_USERNAME="your-username" `
  -e SPRING_DATASOURCE_PRIMARY_PASSWORD="your-password" `
  -e SPRING_DATASOURCE_FOODCATEGORIES_URL="jdbc:postgresql://your-foodcategories-host/neondb?sslmode=require" `
  -e SPRING_DATASOURCE_FOODCATEGORIES_USERNAME="your-username" `
  -e SPRING_DATASOURCE_FOODCATEGORIES_PASSWORD="your-password" `
  -e SPRING_PROFILES_ACTIVE=docker `
  calorie-tracker:latest

# Check container status
docker ps

# View logs
docker logs calorie-tracker

# Stop and remove container
docker stop calorie-tracker
docker rm calorie-tracker
```

**🔒 Security Benefits:**
- Credentials are **NOT baked into the Docker image**
- Environment variables are **passed at runtime**
- Image can be safely shared without exposing secrets

## Azure Container Apps Deployment

### 1. Install Azure CLI and Container Apps Extension

```bash
# Install Azure CLI (if not already installed)
# https://docs.microsoft.com/en-us/cli/azure/install-azure-cli

# Install Container Apps extension
az extension add --name containerapp
```

### 2. Login and Setup

#### Linux/macOS (Bash)
```bash
# Login to Azure
az login

# Set your subscription (if you have multiple)
az account set --subscription "your-subscription-id"

# Create resource group
az group create \
  --name calorie-tracker-rg \
  --location eastus
```

#### Windows PowerShell
```powershell
# Login to Azure
az login

# Set your subscription (if you have multiple)
az account set --subscription "your-subscription-id"

# Create resource group
az group create `
  --name calorie-tracker-rg `
  --location eastus
```

### 3. Create Container Apps Environment

#### Linux/macOS (Bash)
```bash
# Create Container Apps environment
az containerapp env create \
  --name calorie-tracker-env \
  --resource-group calorie-tracker-rg \
  --location eastus
```

#### Windows PowerShell
```powershell
# Create Container Apps environment
az containerapp env create `
  --name calorie-tracker-env `
  --resource-group calorie-tracker-rg `
  --location eastus
```

### 4. Build and Push Image to Azure Container Registry

#### Linux/macOS (Bash)
```bash
# Create Azure Container Registry
az acr create \
  --resource-group calorie-tracker-rg \
  --name calorietrackeracr \
  --sku Basic \
  --admin-enabled true

# Login to ACR
az acr login --name calorietrackeracr

# Option 1: Build and push using ACR Tasks (may fail on some subscriptions)
az acr build \
  --registry calorietrackeracr \
  --image calorie-tracker:latest \
  ./app

# Option 2: Build locally and push (if ACR Tasks fail)
cd app
docker build -t calorie-tracker:latest .
docker tag calorie-tracker:latest calorietrackeracr.azurecr.io/calorie-tracker:latest
docker push calorietrackeracr.azurecr.io/calorie-tracker:latest
```

#### Windows PowerShell
```powershell
# Create Azure Container Registry
az acr create `
  --resource-group calorie-tracker-rg `
  --name calorietrackeracr `
  --sku Basic `
  --admin-enabled true

# Login to ACR
az acr login --name calorietrackeracr

# Option 1: Build and push using ACR Tasks (may fail on some subscriptions)
az acr build `
  --registry calorietrackeracr `
  --image calorie-tracker:latest `
  ./app

# Option 2: Build locally and push (if ACR Tasks fail)
cd app
docker build -t calorie-tracker:latest .
docker tag calorie-tracker:latest calorietrackeracr.azurecr.io/calorie-tracker:latest
docker push calorietrackeracr.azurecr.io/calorie-tracker:latest
```

### 5. Deploy Container App with Secure Secrets

**Option A: Using Secrets (Recommended for Production)**

#### Linux/macOS (Bash)
```bash
# Step 1: Create the container app first (without secrets)
az containerapp create \
  --name calorie-tracker-app \
  --resource-group calorie-tracker-rg \
  --environment calorie-tracker-env \
  --image calorietrackeracr.azurecr.io/calorie-tracker:latest \
  --registry-server calorietrackeracr.azurecr.io \
  --target-port 8080 \
  --ingress external \
  --min-replicas 0 \
  --max-replicas 1 \
  --cpu 0.5 \
  --memory 1Gi \
  --env-vars \
    "spring.datasource.primary.url=jdbc:postgresql://your-primary-host/neondb?sslmode=require" \
    "spring.datasource.foodcategories.url=jdbc:postgresql://your-foodcategories-host/food-category?sslmode=require" \
    SPRING_PROFILES_ACTIVE=docker

# Step 2: Add secrets to the container app (both usernames and passwords for security)
az containerapp secret set \
  --name calorie-tracker-app \
  --resource-group calorie-tracker-rg \
  --secrets \
    primary-db-username="your-username" \
    primary-db-password="your-password" \
    foodcategories-db-username="your-username" \
    foodcategories-db-password="your-password"

# Step 3: Update the container app to use secrets for all credentials
az containerapp update \
  --name calorie-tracker-app \
  --resource-group calorie-tracker-rg \
  --set-env-vars \
    "spring.datasource.primary.username=secretref:primary-db-username" \
    "spring.datasource.primary.password=secretref:primary-db-password" \
    "spring.datasource.foodcategories.username=secretref:foodcategories-db-username" \
    "spring.datasource.foodcategories.password=secretref:foodcategories-db-password"
```

#### Windows PowerShell
```powershell
# Step 1: Create the container app first (without secrets)
az containerapp create `
  --name calorie-tracker-app `
  --resource-group calorie-tracker-rg `
  --environment calorie-tracker-env `
  --image calorietrackeracr.azurecr.io/calorie-tracker:latest `
  --registry-server calorietrackeracr.azurecr.io `
  --target-port 8080 `
  --ingress external `
  --min-replicas 0 `
  --max-replicas 1 `
  --cpu 0.5 `
  --memory 1Gi `
  --env-vars `
    "spring.datasource.primary.url=jdbc:postgresql://your-primary-host/neondb?sslmode=require" `
    "spring.datasource.foodcategories.url=jdbc:postgresql://your-foodcategories-host/food-category?sslmode=require" `
    SPRING_PROFILES_ACTIVE=docker

# Step 2: Add secrets to the container app (both usernames and passwords for security)
az containerapp secret set `
  --name calorie-tracker-app `
  --resource-group calorie-tracker-rg `
  --secrets `
    primary-db-username="your-username" `
    primary-db-password="your-password" `
    foodcategories-db-username="your-username" `
    foodcategories-db-password="your-password"

# Step 3: Update the container app to use secrets for all credentials
az containerapp update `
  --name calorie-tracker-app `
  --resource-group calorie-tracker-rg `
  --set-env-vars `
    "spring.datasource.primary.username=secretref:primary-db-username" `
    "spring.datasource.primary.password=secretref:primary-db-password" `
    "spring.datasource.foodcategories.username=secretref:foodcategories-db-username" `
    "spring.datasource.foodcategories.password=secretref:foodcategories-db-password"
```

**Option B: Single Command Deployment (All Secrets Set During Creation)**

#### Linux/macOS (Bash)
```bash
# Create the container app with all credentials as secrets (recommended)
az containerapp create \
  --name calorie-tracker-app \
  --resource-group calorie-tracker-rg \
  --environment calorie-tracker-env \
  --image calorietrackeracr.azurecr.io/calorie-tracker:latest \
  --registry-server calorietrackeracr.azurecr.io \
  --target-port 8080 \
  --ingress external \
  --min-replicas 0 \
  --max-replicas 1 \
  --cpu 0.5 \
  --memory 1Gi \
  --secrets \
    primary-db-username="your-username" \
    primary-db-password="your-password" \
    foodcategories-db-username="your-username" \
    foodcategories-db-password="your-password" \
  --env-vars \
    "spring.datasource.primary.url=jdbc:postgresql://your-primary-host/neondb?sslmode=require" \
    "spring.datasource.primary.username=secretref:primary-db-username" \
    "spring.datasource.primary.password=secretref:primary-db-password" \
    "spring.datasource.foodcategories.url=jdbc:postgresql://your-foodcategories-host/food-category?sslmode=require" \
    "spring.datasource.foodcategories.username=secretref:foodcategories-db-username" \
    "spring.datasource.foodcategories.password=secretref:foodcategories-db-password" \
    SPRING_PROFILES_ACTIVE=docker
```

#### Windows PowerShell
```powershell
# Create the container app with all credentials as secrets (recommended)
az containerapp create `
  --name calorie-tracker-app `
  --resource-group calorie-tracker-rg `
  --environment calorie-tracker-env `
  --image calorietrackeracr.azurecr.io/calorie-tracker:latest `
  --registry-server calorietrackeracr.azurecr.io `
  --target-port 8080 `
  --ingress external `
  --min-replicas 0 `
  --max-replicas 1 `
  --cpu 0.5 `
  --memory 1Gi `
  --secrets `
    primary-db-username="your-username" `
    primary-db-password="your-password" `
    foodcategories-db-username="your-username" `
    foodcategories-db-password="your-password" `
  --env-vars `
    "spring.datasource.primary.url=jdbc:postgresql://your-primary-host/neondb?sslmode=require" `
    "spring.datasource.primary.username=secretref:primary-db-username" `
    "spring.datasource.primary.password=secretref:primary-db-password" `
    "spring.datasource.foodcategories.url=jdbc:postgresql://your-foodcategories-host/food-category?sslmode=require" `
    "spring.datasource.foodcategories.username=secretref:foodcategories-db-username" `
    "spring.datasource.foodcategories.password=secretref:foodcategories-db-password" `
    SPRING_PROFILES_ACTIVE=docker
```

### 6. Get Application URL

#### Linux/macOS (Bash)
```bash
# Get the application URL
az containerapp show \
  --name calorie-tracker-app \
  --resource-group calorie-tracker-rg \
  --query properties.configuration.ingress.fqdn \
  --output tsv
```

#### Windows PowerShell
```powershell
# Get the application URL
az containerapp show `
  --name calorie-tracker-app `
  --resource-group calorie-tracker-rg `
  --query properties.configuration.ingress.fqdn `
  --output tsv
```

## Troubleshooting

### Common Issues

1. **ACR Tasks Not Permitted Error**
   ```
   TasksOperationsNotAllowed: ACR Tasks requests are not permitted
   ```
   - **Solution**: Use local build and push instead of `az acr build`
   - This is common on free/student Azure subscriptions
   - Use Option 2 in the build commands above

2. **Database Connection Timeout**
   - Verify your Neon database URLs and credentials
   - Ensure databases are not sleeping (Neon free tier has auto-suspend)

3. **Health Check Failures**
   - Check application logs: `docker logs calorie-tracker`
   - Verify actuator endpoint: `/actuator/health`

4. **Memory Issues**
   - Increase memory allocation in Azure Container Apps
   - Monitor resource usage with `az containerapp logs`

5. **Secret Management Issues**
   - Secrets are set on the **Container App**, not the environment
   - Use `az containerapp secret set` (not `az containerapp env secret set`)
   - Reference secrets with `secretref:secret-name` format

### Useful Commands

#### Linux/macOS (Bash)
```bash
# Check container logs locally
docker logs calorie-tracker -f

# Check Azure Container App logs
az containerapp logs show \
  --name calorie-tracker-app \
  --resource-group calorie-tracker-rg \
  --follow

# Scale down to save costs (scales to 0)
az containerapp revision set-active \
  --name calorie-tracker-app \
  --resource-group calorie-tracker-rg \
  --revision-name "revision-name"

# Delete resources when done
az group delete --name calorie-tracker-rg --yes --no-wait
```

#### Windows PowerShell
```powershell
# Check container logs locally
docker logs calorie-tracker -f

# Check Azure Container App logs
az containerapp logs show `
  --name calorie-tracker-app `
  --resource-group calorie-tracker-rg `
  --follow

# Scale down to save costs (scales to 0)
az containerapp revision set-active `
  --name calorie-tracker-app `
  --resource-group calorie-tracker-rg `
  --revision-name "revision-name"

# Delete resources when done
az group delete --name calorie-tracker-rg --yes --no-wait
```

## Cost Optimization

- **Scale to Zero**: Container Apps automatically scales to 0 replicas when not in use
- **Resource Limits**: Use minimal CPU (0.5) and memory (1Gi) for demo purposes
- **Free Tier**: Stay within the monthly free allowances:
  - 2 million requests
  - 400,000 GB-seconds execution time
  - 1 GB outbound data transfer

## Security Notes

### 🔐 Credential Management

- **Never commit credentials** to source control (`.env` files, `config/env.properties`)
- **Use environment variables** for Docker deployment - credentials are NOT baked into images
- **Azure Container Apps Secrets** - encrypted at rest, secure injection at runtime
- **Azure Key Vault integration** - for enterprise-grade secret management

### 🛡️ Security Hierarchy (Best to Worst)

1. **Azure Key Vault** - Enterprise secret management
2. **Container Apps Secrets** - Encrypted environment secrets  
3. **Environment Variables** - Runtime injection (not stored in image)
4. **Config Files** - ❌ NOT recommended for production (embedded in image)

### 🔐 Database Credential Security

**All database credentials (usernames AND passwords) are now stored as secrets for maximum security:**

**Why Username Security Matters:**
- ❌ **Information Disclosure**: Usernames reveal database structure and purpose
- ❌ **Enumeration Attacks**: Helps attackers understand your system architecture
- ❌ **Compliance**: Many regulations require all credentials as secrets
- ❌ **Audit Trail**: Environment variables don't provide access logging

**Security Benefits:**
- ✅ **Defense in Depth**: Multiple layers of credential protection
- ✅ **Audit Trails**: Azure logs all secret access attempts
- ✅ **Encrypted Storage**: All credentials encrypted at rest and in transit
- ✅ **Access Control**: RBAC controls who can view/modify secrets
- ✅ **Compliance Ready**: Meets enterprise and regulatory requirements

### 🔒 Additional Security

- The application runs as a **non-root user** in the container
- Consider using **Azure Managed Identity** for Azure resource access
- Enable **HTTPS only** for production deployments
- Use **private container registries** for sensitive applications 