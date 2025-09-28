variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "hotel-management"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "East US"
}

variable "image_tag" {
  description = "Docker image tag"
  type        = string
  default     = "latest"
}

variable "github_repository" {
  description = "GitHub repository in format owner/repo"
  type        = string
  default     = "nathangtg/hotel-management" # Update this with your GitHub username
}

variable "mysql_admin_login" {
  description = "MySQL administrator login"
  type        = string
  default     = "hotel_admin"
}

variable "mysql_admin_password" {
  description = "MySQL administrator password"
  type        = string
  sensitive   = true
  default     = "P@ssw0rd123!" # Change this in production
}