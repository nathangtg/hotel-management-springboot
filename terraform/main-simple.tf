# Simplified Terraform configuration for reliable first deployment
terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
}

provider "azurerm" {
  features {}
}

# Resource Group
resource "azurerm_resource_group" "main" {
  name     = "rg-${var.app_name}-${var.environment}"
  location = var.location
}

# Container Registry
resource "azurerm_container_registry" "main" {
  name                = "cr${replace(var.app_name, "-", "")}${var.environment}"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "Basic"
  admin_enabled       = true
}

# MySQL Flexible Server (Simplified)
resource "azurerm_mysql_flexible_server" "main" {
  name                = "mysql-${var.app_name}-${var.environment}"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  
  administrator_login    = var.mysql_admin_login
  administrator_password = var.mysql_admin_password
  
  backup_retention_days = 7
  sku_name             = "B_Standard_B1s"
  version              = "8.0.21"
  
  storage {
    size_gb = 20
  }
}

# MySQL Database
resource "azurerm_mysql_flexible_database" "main" {
  name                = var.mysql_database_name
  resource_group_name = azurerm_resource_group.main.name
  server_name         = azurerm_mysql_flexible_server.main.name
  charset             = "utf8mb3"
  collation           = "utf8mb3_general_ci"
}

# MySQL Firewall Rule (Allow Azure Services)
resource "azurerm_mysql_flexible_server_firewall_rule" "azure_services" {
  name                = "AllowAzureServices"
  resource_group_name = azurerm_resource_group.main.name
  server_name         = azurerm_mysql_flexible_server.main.name
  start_ip_address    = "0.0.0.0"
  end_ip_address      = "0.0.0.0"
}

# Container App Environment
resource "azurerm_container_app_environment" "main" {
  name                = "cae-${var.app_name}-${var.environment}"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
}

# Container App
resource "azurerm_container_app" "main" {
  name                         = "ca-${var.app_name}-${var.environment}"
  container_app_environment_id = azurerm_container_app_environment.main.id
  resource_group_name          = azurerm_resource_group.main.name
  revision_mode               = "Single"

  template {
    min_replicas = 0
    max_replicas = 2

    container {
      name   = "hotel-management"
      image  = "${azurerm_container_registry.main.login_server}/hotel-management:${var.image_tag}"
      cpu    = 0.25
      memory = "0.5Gi"

      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "prod"
      }

      env {
        name  = "SPRING_DATASOURCE_URL"
        value = "jdbc:mysql://${azurerm_mysql_flexible_server.main.fqdn}:3306/${var.mysql_database_name}"
      }

      env {
        name  = "SPRING_DATASOURCE_USERNAME"
        value = var.mysql_admin_login
      }

      env {
        name        = "SPRING_DATASOURCE_PASSWORD"
        secret_name = "mysql-password"
      }
    }
  }

  secret {
    name  = "mysql-password"
    value = var.mysql_admin_password
  }

  ingress {
    external_enabled = true
    target_port      = 8080

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
}