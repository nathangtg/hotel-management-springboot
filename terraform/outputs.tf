output "resource_group_name" {
  description = "Name of the resource group"
  value       = azurerm_resource_group.main.name
}

output "container_registry_login_server" {
  description = "Container registry login server URL"
  value       = azurerm_container_registry.main.login_server
}

output "container_app_url" {
  description = "Container app URL"
  value       = "https://${azurerm_container_app.main.ingress[0].fqdn}"
}

output "mysql_server_fqdn" {
  description = "MySQL server FQDN"
  value       = azurerm_mysql_flexible_server.main.fqdn
}

output "mysql_database_name" {
  description = "MySQL database name"
  value       = azurerm_mysql_flexible_database.main.name
}

output "log_analytics_workspace_id" {
  description = "Log Analytics workspace ID"
  value       = azurerm_log_analytics_workspace.main.id
}