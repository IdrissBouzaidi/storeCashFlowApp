terraform {
    required_providers {
        keycloak = {
            source  = "mrparkers/keycloak"
            version = ">= 4.1.0"
        }

        minio = {
          source  = "aminueza/minio"
          version = "~> 2.0"
        }
    }
}

provider "keycloak" {
    client_id = "admin-cli"
    username = "admin"
    password = "admin"
    url = "http://localhost:8080"
}

resource "keycloak_realm" "myRealm" {
  realm   = "myRealm"
  enabled = true
  
  access_token_lifespan                   = "1h"   # 1h
  sso_session_idle_timeout                = "2h"   # 2h
  sso_session_max_lifespan                = "8h"  # 8h
  offline_session_idle_timeout            = "168h" # 7 jours
  offline_session_max_lifespan            = "672h" # 28 jours (si activé)
}

resource "keycloak_openid_client" "StoreCashFlow" {
  realm_id                     = keycloak_realm.myRealm.id
  client_id                    = "StoreCashFlow"
  client_secret                = "gVbcKqq0ZrETt1G9cUXa79T96gs5dIpo"
  name                         = "Store Cash Flow"
  enabled                      = true
  access_type                  = "CONFIDENTIAL"
  service_accounts_enabled     = true
  standard_flow_enabled        = true
  direct_access_grants_enabled = true
  valid_redirect_uris = [
    "/*",
    "http://localhost:9091/swagger-ui/oauth2-redirect.html"
  ]
  web_origins                 = ["/*"]
}

resource "keycloak_openid_client" "swagger_ui" {
  realm_id                     = keycloak_realm.myRealm.id
  client_id                    = "swagger-client"
  name                         = "Swagger UI Client"
  access_type                  = "PUBLIC"
  standard_flow_enabled        = true
  direct_access_grants_enabled = false
  service_accounts_enabled     = false

  valid_redirect_uris = [
    "http://localhost:9090/swagger-ui/oauth2-redirect.html",
    "http://localhost:9091/swagger-ui/oauth2-redirect.html",
    "http://localhost:9092/swagger-ui/oauth2-redirect.html",
    "http://localhost:9093/swagger-ui/oauth2-redirect.html",

    "http://localhost:9190/swagger-ui/oauth2-redirect.html",
    "http://localhost:9191/swagger-ui/oauth2-redirect.html",
    "http://localhost:9192/swagger-ui/oauth2-redirect.html",
    "http://localhost:9193/swagger-ui/oauth2-redirect.html"
  ]

  web_origins = [
    "http://storecashflowapi:9090",
    "http://localhost:9090",

    "http://userapi:9091",
    "http://localhost:9091",

    "http://keycloakapi:9092",
    "http://localhost:9092",

    "http://minioapi:9093",
    "http://localhost:9093",

    
    "http://storecashflowapi:9190",
    "http://localhost:9190",

    "http://userapi:9191",
    "http://localhost:9191",

    "http://keycloakapi:9192",
    "http://localhost:9192",

    "http://minioapi:9193",
    "http://localhost:9193"
  ]
}

resource "keycloak_openid_client" "minio" {
  realm_id                     = keycloak_realm.myRealm.id
  client_id                    = "minio_client"
  name                         = "MinIO Client"
  client_secret                = "gVTcKhq5ZrHTt1G2cUXa79T96gs5dIpo"
  enabled                      = true
  access_type                  = "CONFIDENTIAL"
  service_accounts_enabled     = true
  standard_flow_enabled        = true
  direct_access_grants_enabled = true

  valid_redirect_uris = [
    "http://localhost:9001/oauth_callback"
  ]

  web_origins = [
    "http://minio:9001",
    "http://localhost:9001"
  ]
}

resource "keycloak_role" "store_cash_flow_user_role" {
  realm_id = "myRealm"
  name = "store_cash_flow_user"
  client_id = keycloak_openid_client.StoreCashFlow.id
}

data "keycloak_openid_client" "realm-management" {
  realm_id = keycloak_realm.myRealm.id
  client_id = "realm-management"
}

resource "keycloak_user" "adminUser" {
  realm_id = keycloak_realm.myRealm.id
  username = "admin"
  email = "idrissAdmin@gmail.com"
  enabled = true
  email_verified = true
  first_name = "Idriss"
  last_name = "Admin"
  initial_password {
    value = "admin"
    temporary = false
  }
}

data "keycloak_role" "realm_admin" {
  realm_id = keycloak_realm.myRealm.id
  client_id = data.keycloak_openid_client.realm-management.id
  name = "realm-admin"
}

resource "keycloak_user_roles" "admin_role_roles" {
  realm_id = keycloak_realm.myRealm.id
  user_id = keycloak_user.adminUser.id
  role_ids = [
    data.keycloak_role.realm_admin.id
  ]
}

data "keycloak_role" "view_users" {
  realm_id  = keycloak_realm.myRealm.id
  client_id = data.keycloak_openid_client.realm-management.id
  name      = "view-users"
}

resource "keycloak_openid_client_service_account_role" "storecf_view_users" {
  realm_id                = keycloak_realm.myRealm.id
  service_account_user_id = keycloak_openid_client.StoreCashFlow.service_account_user_id
  client_id               = data.keycloak_openid_client.realm-management.id
  role                    = data.keycloak_role.view_users.name
}


resource "keycloak_user" "idrissUser" {
  realm_id = keycloak_realm.myRealm.id
  username = "idriss@gmail.com"
  email = "idriss@gmail.com"
  enabled  = true
  email_verified = true
  first_name = "Idriss"
  last_name  = "Drissi El-Bouzaidi"
  
  initial_password {
    value     = "abcd"
    temporary = false
  }
}

resource "keycloak_user_roles" "assign_role_to_user" {
  realm_id = "myRealm"
  user_id = keycloak_user.idrissUser.id
  role_ids = [
    resource.keycloak_role.store_cash_flow_user_role.id
  ]
}





provider "minio" {
  minio_server   = "minio:9000"  # ou l’URL de ton MinIO
  minio_user     = "admin"
  minio_password = "aaaa1111AAAA"
  minio_ssl = false
}

resource "minio_s3_bucket" "min_io_bucket_name" {
  bucket = "store-cash-flow"
  acl    = "private"
}

resource "minio_s3_bucket" "min_io_thumbnail_bucket_name" {
  bucket = "store-cash-flow-compressed"
  acl = "private"
}