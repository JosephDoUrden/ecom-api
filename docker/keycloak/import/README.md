# Keycloak Configuration for E-Commerce Platform

This directory contains the configuration files for Keycloak, which provides identity and access management for the e-commerce platform.

## Default Configuration

The default realm configuration (`ecommerce-realm.json`) sets up:

1. The "ecommerce" realm with basic settings
2. Three roles: admin, customer, and vendor
3. An OAuth2 client for the API with proper redirect URIs
4. Default security settings

## Default Admin Credentials

The Keycloak admin console is accessible at: http://localhost:8081/admin/
- Username: admin
- Password: admin

## E-Commerce Realm Test Users

Once initialized, the following test users will be available:
- admin_user@example.com / password (Admin role)
- customer@example.com / password (Customer role)
- vendor@example.com / password (Vendor role)

## OAuth2 Client Details

The API client is configured with:
- Client ID: ecommerce-api
- Client Secret: ecommerce-api-secret
- Grant Types: Authorization Code, Password, Client Credentials
- Redirect URIs: http://localhost:8080/*, http://localhost:3000/*

## How to Use in Development

Use the following endpoints:
- Authorization URL: http://localhost:8081/realms/ecommerce/protocol/openid-connect/auth
- Token URL: http://localhost:8081/realms/ecommerce/protocol/openid-connect/token
- User Info URL: http://localhost:8081/realms/ecommerce/protocol/openid-connect/userinfo
- End Session URL: http://localhost:8081/realms/ecommerce/protocol/openid-connect/logout

## Additional Configuration

To modify the realm configuration:
1. Export updated configuration from Keycloak admin console
2. Replace the `ecommerce-realm.json` file in this directory
3. Restart Keycloak with the updated configuration

## Troubleshooting

If you encounter issues:
- Check the Keycloak container logs with `docker-compose logs -f keycloak`
- Verify database connections and permissions
- Ensure the realm configuration JSON is valid
- Try importing the realm manually through the admin UI
