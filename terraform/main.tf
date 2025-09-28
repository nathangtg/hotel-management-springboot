# Resource Group
resource "azurerm_resource_group" "main" {
  name     = "rg-${var.project_name}-${var.environment}"
  location = var.location

  tags = {
    Environment = var.environment
    Project     = var.project_name
    CostCenter  = "Development"
  }
}

# Container Registry (Basic tier for cost optimization)
resource "azurerm_container_registry" "main" {
  name                = "acr${replace(var.project_name, "-", "")}${var.environment}"
  resource_group_name = azurerm_resource_group.main.name
  location           = azurerm_resource_group.main.location
  sku                = "Basic"  # Most cost-effective option
  admin_enabled      = true

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# SQL Server (Basic tier for cost optimization)
resource "azurerm_mssql_server" "main" {
  name                         = "sql-${var.project_name}-${var.environment}"
  resource_group_name          = azurerm_resource_group.main.name
  location                    = azurerm_resource_group.main.location
  version                     = "12.0"
  administrator_login         = var.mysql_admin_login
  administrator_login_password = var.mysql_admin_password
  minimum_tls_version         = "1.2"

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# SQL Database (Basic tier for cost optimization)
resource "azurerm_mssql_database" "main" {
  name           = "hoteldb"
  server_id      = azurerm_mssql_server.main.id
  collation      = "SQL_Latin1_General_CP1_CI_AS"
  license_type   = "LicenseIncluded"
  max_size_gb    = 2
  sku_name       = "Basic"  # Most cost-effective option (~$5/month)
  zone_redundant = false

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# Firewall rule to allow Azure services
resource "azurerm_mssql_firewall_rule" "allow_azure_services" {
  name             = "AllowAzureServices"
  server_id        = azurerm_mssql_server.main.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

# Container App Environment
resource "azurerm_container_app_environment" "main" {
  name                       = "cae-${var.project_name}-${var.environment}"
  location                  = azurerm_resource_group.main.location
  resource_group_name       = azurerm_resource_group.main.name
  log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# Log Analytics Workspace (minimal retention for cost savings)
resource "azurerm_log_analytics_workspace" "main" {
  name                = "log-${var.project_name}-${var.environment}"
  location           = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  sku                = "PerGB2018"
  retention_in_days  = 30  # Minimum retention for cost savings

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# Container App
resource "azurerm_container_app" "main" {
  name                         = "ca-${var.project_name}-${var.environment}"
  container_app_environment_id = azurerm_container_app_environment.main.id
  resource_group_name          = azurerm_resource_group.main.name
  revision_mode               = "Single"

  template {
    min_replicas = 0  # Scale to zero when not in use
    max_replicas = 2  # Limit for cost control
    
    container {
      name   = "hotel-management"
      image  = "ghcr.io/${var.github_repository}:${var.image_tag}"
      cpu    = 0.25  # Minimum CPU allocation
      memory = "0.5Gi"  # Minimum memory allocation

      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "prod"
      }
      
      env {
        name  = "SPRING_DATASOURCE_URL"
        value = "jdbc:sqlserver://${azurerm_mssql_server.main.fully_qualified_domain_name}:1433;database=${azurerm_mssql_database.main.name};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
      }
      
      env {
        name  = "SPRING_DATASOURCE_USERNAME"
        value = var.mysql_admin_login
      }
      
      env {
        name        = "SPRING_DATASOURCE_PASSWORD"
        secret_name = "mysql-password"
      }
      
      env {
        name  = "SPRING_JPA_HIBERNATE_DDL_AUTO"
        value = "update"
      }
      
      env {
        name  = "SPRING_JPA_DATABASE_PLATFORM"
        value = "org.hibernate.dialect.SQLServerDialect"
      }

      env {
        name  = "SPRING_H2_CONSOLE_ENABLED"
        value = "false"
      }
    }
  }

  secret {
    name  = "mysql-password"
    value = var.mysql_admin_password
  }

  ingress {
    allow_insecure_connections = false
    external_enabled          = true
    target_port              = 8080
    
    traffic_weight {
      percentage      = 100
      latest_revision = true
    }
  }

  registry {
    server   = azurerm_container_registry.main.login_server
    username = azurerm_container_registry.main.admin_username
    password_secret_name = "registry-password"
  }

  secret {
    name  = "registry-password"
    value = azurerm_container_registry.main.admin_password
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}