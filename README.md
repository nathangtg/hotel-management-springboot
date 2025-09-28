# Hotel Management API - Azure Deployment Guide

This repository contains a Spring Boot application with GitHub Actions CI/CD pipeline and Terraform infrastructure for Azure deployment.

## 🏗️ Architecture

- **Azure Container Apps**: Serverless container hosting with scale-to-zero capability
- **Azure MySQL Flexible Server**: Managed database with burstable tier pricing
- **Azure Container Registry**: Docker image storage
- **GitHub Actions**: CI/CD pipeline with automated testing and deployment

## 💰 Cost Optimization Features

This setup is optimized for learning and development with minimal costs:

1. **Container Apps**: Scale to zero when not in use (pay only when running)
2. **MySQL Flexible Server**: Burstable B1s tier (cheapest option)
3. **Container Registry**: Basic tier
4. **Storage**: Minimum sizes and reduced retention periods
5. **No redundancy**: Single zone deployment to save costs

**Estimated monthly cost**: ~$15-30 USD (depending on usage)

## 🚀 Setup Instructions

### 1. Prerequisites

- Azure subscription
- GitHub account
- Azure CLI installed locally (optional, for initial setup)

### 2. GitHub Repository Setup

1. Fork or clone this repository
2. Update the `github_repository` variable in `terraform/variables.tf` with your GitHub username
3. Enable GitHub Container Registry for your repository

### 3. Azure Service Principal Setup

Create an Azure Service Principal for GitHub Actions:

```bash
# Login to Azure
az login

# Create service principal
az ad sp create-for-rbac --name "github-actions-hotel-management" \
  --role contributor \
  --scopes /subscriptions/{subscription-id} \
  --sdk-auth
```

### 4. GitHub Secrets Configuration

Add these secrets to your GitHub repository (Settings → Secrets and variables → Actions):

```
AZURE_CREDENTIALS: {
  "clientId": "your-client-id",
  "clientSecret": "your-client-secret",
  "subscriptionId": "your-subscription-id",
  "tenantId": "your-tenant-id"
}
```

### 5. Terraform Backend (Optional but Recommended)

For production use, configure Terraform remote state:

```bash
# Create storage account for Terraform state
az group create --name terraform-state-rg --location "East US"
az storage account create --name yourstorageaccount --resource-group terraform-state-rg --location "East US" --sku Standard_LRS
az storage container create --name tfstate --account-name yourstorageaccount
```

Then uncomment and configure the backend in `terraform/providers.tf`.

### 6. Deployment

1. Push your code to the `main` branch
2. GitHub Actions will automatically:
   - Run tests
   - Build and push Docker image
   - Deploy infrastructure with Terraform
   - Deploy the application

## 🔧 Local Development

### Running Locally

```bash
# Clone the repository
git clone <your-repo-url>
cd hotel-management

# Run with Maven
./mvnw spring-boot:run

# Or build and run with Docker
docker build -t hotel-management .
docker run -p 8080:8080 hotel-management
```

### Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=UserControllerTest
```

## 📊 Monitoring and Management

### Application URLs (after deployment)

- **Application**: https://ca-hotel-management-dev.{region}.azurecontainerapps.io
- **Health Check**: https://ca-hotel-management-dev.{region}.azurecontainerapps.io/actuator/health
- **Metrics**: https://ca-hotel-management-dev.{region}.azurecontainerapps.io/actuator/metrics

### Azure Resources

After deployment, you'll have:

- Resource Group: `rg-hotel-management-dev`
- Container App: `ca-hotel-management-dev`
- MySQL Server: `mysql-hotel-management-dev`
- Container Registry: `acrhotelmanagementdev`

## 🔒 Security Features

- Non-root container user
- Secrets management via Azure Container Apps
- HTTPS-only ingress
- Database connection with SSL
- Vulnerability scanning with Trivy

## 📈 Scaling and Production

### To increase performance:
1. Update container resources in `terraform/main.tf`:
   ```hcl
   cpu    = 0.5    # Increase CPU
   memory = "1Gi"  # Increase memory
   ```

2. Scale replicas:
   ```hcl
   min_replicas = 1  # Always-on instance
   max_replicas = 5  # Higher maximum
   ```

### To add redundancy:
1. Enable geo-redundant backup for MySQL
2. Use zone-redundant high availability
3. Deploy to multiple regions

## 🛠️ Troubleshooting

### Check application logs:
```bash
az containerapp logs show --name ca-hotel-management-dev --resource-group rg-hotel-management-dev
```

### Check database connectivity:
```bash
az mysql flexible-server connect --name mysql-hotel-management-dev --resource-group rg-hotel-management-dev --username hotel_admin
```

### Common Issues:

1. **Container not starting**: Check application logs and ensure all environment variables are set
2. **Database connection issues**: Verify MySQL server is running and firewall rules allow Container Apps
3. **Image pull issues**: Ensure Container Registry credentials are correctly configured

## 💡 Cost Management Tips

1. **Stop resources when not needed**:
   ```bash
   az containerapp revision deactivate --resource-group rg-hotel-management-dev --name ca-hotel-management-dev
   ```

2. **Monitor costs** in Azure Cost Management dashboard

3. **Set up budget alerts** to track spending

4. **Use Azure Dev/Test pricing** if you have eligible subscriptions

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `./mvnw test`
5. Submit a pull request

The CI pipeline will automatically test your changes before merging.