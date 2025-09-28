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

output "sql_server_fqdn" {
  description = "SQL Server FQDN"
  value       = azurerm_mssql_server.main.fully_qualified_domain_name
}

output "sql_database_name" {
  description = "SQL Database name"
  value       = azurerm_mssql_database.main.name
}

output "log_analytics_workspace_id" {
  description = "Log Analytics workspace ID"
  value       = azurerm_log_analytics_workspace.main.id
}