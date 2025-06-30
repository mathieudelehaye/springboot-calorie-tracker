# Docker Deployment Guide

## 🚀 Quick Start (5 Minutes)

### 1. Setup Configuration
```bash
# Copy the environment template
cp docker.env.example config/env.properties

# Edit with your actual Neon database credentials
notepad config/env.properties  # Windows or nano config/env.properties (Linux/macOS)
```

### 2. Run Both Applications
```bash
# Build and start both applications
docker-compose up --build

# Access applications:
# 🍎 Diet Manager: http://localhost:8080
# ⚙️ Food Admin: http://localhost:8081
```

### 3. Health Checks
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
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

```bash
# Copy the template
cp docker.env.example config/env.properties

# Edit with your actual Neon database credentials
nano config/env.properties  # or use your preferred editor
```

### Build and Run Both Applications

```bash
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

```bash
# Build Diet Manager App
./gradlew :calorie-tracker-app:build
docker build -f calorie-tracker-app/Dockerfile -t diet-manager:latest .

# Build Food Categories Admin
./gradlew :food-categories-admin:build  
docker build -f food-categories-admin/Dockerfile -t food-admin:latest .
```

#### Run with Environment Variables (Secure Method)

**Linux/macOS:**
```bash
# Diet Manager App
docker run -d \
  --name diet-manager \
  -p 8080:8080 \
  -v $(pwd)/config:/app/config:ro \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_CONFIG_IMPORT=optional:file:/app/config/env.properties \
  diet-manager:latest

# Food Categories Admin
docker run -d \
  --name food-admin \
  -p 8081:8081 \
  -v $(pwd)/config:/app/config:ro \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_CONFIG_IMPORT=optional:file:/app/config/env.properties \
  food-admin:latest
```

**Windows PowerShell:**
```powershell
# Diet Manager App
docker run -d `
  --name diet-manager `
  -p 8080:8080 `
  -v ${PWD}/config:/app/config:ro `
  -e SPRING_PROFILES_ACTIVE=docker `
  -e SPRING_CONFIG_IMPORT=optional:file:/app/config/env.properties `
  diet-manager:latest

# Food Categories Admin  
docker run -d `
  --name food-admin `
  -p 8081:8081 `
  -v ${PWD}/config:/app/config:ro `
  -e SPRING_PROFILES_ACTIVE=docker `
  -e SPRING_CONFIG_IMPORT=optional:file:/app/config/env.properties `
  food-admin:latest
```

**🔒 Security Benefits:**
- Credentials are **NOT baked into Docker images**
- Configuration is **mounted at runtime** from external file
- Images can be safely shared without exposing secrets

---

## ☁️ Azure Container Apps Deployment

### 1. Prerequisites Setup

```bash
# Install Azure CLI (if not already installed)
# https://docs.microsoft.com/en-us/cli/azure/install-azure-cli

# Install Container Apps extension
az extension add --name containerapp

# Login to Azure
az login

# Set your subscription (if you have multiple)
az account set --subscription "your-subscription-id"
```

### 2. Create Azure Resources

```bash
# Create resource group
az group create \
  --name calorie-tracker-rg \
  --location eastus

# Create Container Apps environment
az containerapp env create \
  --name calorie-tracker-env \
  --resource-group calorie-tracker-rg \
  --location eastus

# Create Azure Container Registry (ACR)
az acr create \
  --resource-group calorie-tracker-rg \
  --name calorietrackeracr \
  --sku Basic \
  --admin-enabled true
```

### 3. Build and Push Images to Azure Container Registry

```bash
# Login to ACR
az acr login --name calorietrackeracr

# Build and push Diet Manager App
./gradlew :calorie-tracker-app:build
docker build -f calorie-tracker-app/Dockerfile -t calorietrackeracr.azurecr.io/diet-manager:latest .
docker push calorietrackeracr.azurecr.io/diet-manager:latest

# Build and push Food Categories Admin
./gradlew :food-categories-admin:build
docker build -f food-categories-admin/Dockerfile -t calorietrackeracr.azurecr.io/food-admin:latest .
docker push calorietrackeracr.azurecr.io/food-admin:latest
```

### 4. Create Secrets in Azure

```bash
# Create secrets for database connections
az containerapp env secret set \
  --name calorie-tracker-env \
  --resource-group calorie-tracker-rg \
  --secrets \
    primary-db-url="jdbc:postgresql://your-primary-db-host.aws.neon.tech:5432/neondb?sslmode=require" \
    primary-db-username="your-username" \
    primary-db-password="your-password" \
    foodcategories-db-url="jdbc:postgresql://your-foodcategories-db-host.aws.neon.tech:5432/neondb?sslmode=require" \
    foodcategories-db-username="your-username" \
    foodcategories-db-password="your-password"
```

### 5. Deploy Diet Manager App to Azure Container Apps

```bash
az containerapp create \
  --name diet-manager-app \
  --resource-group calorie-tracker-rg \
  --environment calorie-tracker-env \
  --image calorietrackeracr.azurecr.io/diet-manager:latest \
  --target-port 8080 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 3 \
  --cpu 1.0 \
  --memory 2.0Gi \
  --registry-server calorietrackeracr.azurecr.io \
  --env-vars \
    SPRING_PROFILES_ACTIVE=docker \
    SPRING_DATASOURCE_PRIMARY_URL=secretref:primary-db-url \
    SPRING_DATASOURCE_PRIMARY_USERNAME=secretref:primary-db-username \
    SPRING_DATASOURCE_PRIMARY_PASSWORD=secretref:primary-db-password \
    SPRING_DATASOURCE_FOODCATEGORIES_URL=secretref:foodcategories-db-url \
    SPRING_DATASOURCE_FOODCATEGORIES_USERNAME=secretref:foodcategories-db-username \
    SPRING_DATASOURCE_FOODCATEGORIES_PASSWORD=secretref:foodcategories-db-password
```

### 6. Deploy Food Categories Admin to Azure Container Apps

```bash
az containerapp create \
  --name food-admin-app \
  --resource-group calorie-tracker-rg \
  --environment calorie-tracker-env \
  --image calorietrackeracr.azurecr.io/food-admin:latest \
  --target-port 8081 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 2 \
  --cpu 0.5 \
  --memory 1.0Gi \
  --registry-server calorietrackeracr.azurecr.io \
  --env-vars \
    SPRING_PROFILES_ACTIVE=docker \
    SPRING_DATASOURCE_FOODCATEGORY_URL=secretref:foodcategories-db-url \
    SPRING_DATASOURCE_FOODCATEGORY_USERNAME=secretref:foodcategories-db-username \
    SPRING_DATASOURCE_FOODCATEGORY_PASSWORD=secretref:foodcategories-db-password
```

### 7. Get Application URLs

```bash
# Get Diet Manager App URL
az containerapp show \
  --name diet-manager-app \
  --resource-group calorie-tracker-rg \
  --query properties.configuration.ingress.fqdn \
  --output tsv

# Get Food Categories Admin URL  
az containerapp show \
  --name food-admin-app \
  --resource-group calorie-tracker-rg \
  --query properties.configuration.ingress.fqdn \
  --output tsv
```

---

## 🔧 Management & Troubleshooting

### Local Docker Management

```bash
# Check running containers
docker ps

# View logs
docker-compose logs -f [service-name]

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
```bash
./gradlew clean build
docker-compose up --build --force-recreate
```

**Can't connect to database?**
- Check your `config/env.properties` file has correct credentials
- Ensure your Neon databases are not sleeping (free tier auto-suspends)

**Port conflicts?**
```bash
# Stop any existing services on ports 8080/8081
docker ps
docker stop $(docker ps -q)
```

### Azure Container Apps Management

```bash
# Check app status
az containerapp show --name diet-manager-app --resource-group calorie-tracker-rg
az containerapp show --name food-admin-app --resource-group calorie-tracker-rg

# View logs
az containerapp logs show --name diet-manager-app --resource-group calorie-tracker-rg
az containerapp logs show --name food-admin-app --resource-group calorie-tracker-rg

# Scale applications
az containerapp update --name diet-manager-app --resource-group calorie-tracker-rg --min-replicas 2 --max-replicas 5
az containerapp update --name food-admin-app --resource-group calorie-tracker-rg --min-replicas 1 --max-replicas 3

# Update applications (after pushing new images)
az containerapp update --name diet-manager-app --resource-group calorie-tracker-rg --image calorietrackeracr.azurecr.io/diet-manager:latest
az containerapp update --name food-admin-app --resource-group calorie-tracker-rg --image calorietrackeracr.azurecr.io/food-admin:latest

# Delete applications (cleanup)
az containerapp delete --name diet-manager-app --resource-group calorie-tracker-rg
az containerapp delete --name food-admin-app --resource-group calorie-tracker-rg
az group delete --name calorie-tracker-rg
```

---

## 📊 Monitoring and Health Checks

### Health Check Endpoints

- **Diet Manager**: `https://your-diet-manager-url/actuator/health`
- **Food Admin**: `https://your-food-admin-url/actuator/health`

### Azure Application Insights (Optional)

Add Application Insights for monitoring:

```bash
# Create Application Insights
az monitor app-insights component create \
  --app calorie-tracker-insights \
  --location eastus \
  --resource-group calorie-tracker-rg

# Get instrumentation key
az monitor app-insights component show \
  --app calorie-tracker-insights \
  --resource-group calorie-tracker-rg \
  --query instrumentationKey \
  --output tsv

# Update container apps with Application Insights
az containerapp update \
  --name diet-manager-app \
  --resource-group calorie-tracker-rg \
  --set-env-vars APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=your-key"
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
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Login to Azure
        uses: azure/login@v1
        with:
          creds: ${{ secrets.AZURE_CREDENTIALS }}
          
      - name: Build and deploy
        run: |
          # Build applications
          ./gradlew build
          
          # Push images
          az acr login --name calorietrackeracr
          docker build -f calorie-tracker-app/Dockerfile -t calorietrackeracr.azurecr.io/diet-manager:latest .
          docker build -f food-categories-admin/Dockerfile -t calorietrackeracr.azurecr.io/food-admin:latest .
          docker push calorietrackeracr.azurecr.io/diet-manager:latest
          docker push calorietrackeracr.azurecr.io/food-admin:latest
          
          # Update container apps
          az containerapp update --name diet-manager-app --resource-group calorie-tracker-rg --image calorietrackeracr.azurecr.io/diet-manager:latest
          az containerapp update --name food-admin-app --resource-group calorie-tracker-rg --image calorietrackeracr.azurecr.io/food-admin:latest
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