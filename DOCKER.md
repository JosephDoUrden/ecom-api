# Docker Setup Documentation

## Prerequisites
- Docker
- Docker Compose

## Development Commands

### Build and Start Services
```bash
docker-compose up --build
```

### Stop Services
```bash
docker-compose down
```

### View Logs
```bash
docker-compose logs -f app
```

### Database Management
```bash
# Access PostgreSQL CLI
docker-compose exec db psql -U ecommerce_user -d ecommerce

# Backup Database
docker-compose exec db pg_dump -U ecommerce_user ecommerce > backup.sql
```

## Volumes
- `postgres_data`: Persistent PostgreSQL data
- `./logs`: Application log files

## Ports
- Application: 8080
- PostgreSQL: 5432
