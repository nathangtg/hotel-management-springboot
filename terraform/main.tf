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

# MySQL Flexible Server (Burstable tier for cost optimization)
resource "azurerm_mysql_flexible_server" "main" {
  name                   = "mysql-${var.project_name}-${var.environment}"
  resource_group_name    = azurerm_resource_group.main.name
  location              = azurerm_resource_group.main.location
  
  administrator_login    = var.mysql_admin_login
  administrator_password = var.mysql_admin_password
  
  backup_retention_days  = 7
  geo_redundant_backup_enabled = false  # Disable for cost savings
  
  sku_name = "B_Standard_B1s"  # Most cost-effective burstable tier
  version  = "8.0"
  
  zone = "1"
  
  high_availability {
    mode = "SameZone"  # Less expensive than ZoneRedundant
  }
  
  storage {
    auto_grow_enabled  = true
    io_scaling_enabled = false  # Disable for cost savings
    iops              = 360    # Minimum IOPS
    size_gb           = 20     # Minimum size
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# Database
resource "azurerm_mysql_flexible_database" "main" {
  name                = "hoteldb"
  resource_group_name = azurerm_resource_group.main.name
  server_name        = azurerm_mysql_flexible_server.main.name
  charset            = "utf8mb4"
  collation          = "utf8mb4_unicode_ci"
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
        value = "jdbc:mysql://${azurerm_mysql_flexible_server.main.fqdn}:3306/${azurerm_mysql_flexible_database.main.name}?useSSL=true&requireSSL=false&serverTimezone=UTC"
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
        value = "org.hibernate.dialect.MySQLDialect"
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