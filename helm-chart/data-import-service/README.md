# Data Import Service Helm Chart

This Helm chart deploys the Data Import Service on Kubernetes with MySQL database, Spring Boot backend, and Angular frontend.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.2.0+
- PV provisioner support in the underlying infrastructure (for MySQL persistence)

## Installing the Chart

### Quick Start

```bash
# Add the chart repository (if using a chart repository)
helm repo add data-import-service https://your-charts-repo.com/

# Install with default values
helm install my-data-import-service data-import-service/data-import-service

# Or install from local chart
helm install my-data-import-service ./helm-chart/data-import-service
```

### Development Environment

```bash
# Install for development
helm install data-import-dev ./helm-chart/data-import-service \
  -f ./helm-chart/data-import-service/values-dev.yaml \
  --namespace data-import-dev \
  --create-namespace
```

### Production Environment

```bash
# Install for production
helm install data-import-prod ./helm-chart/data-import-service \
  -f ./helm-chart/data-import-service/values-prod.yaml \
  --namespace data-import-prod \
  --create-namespace
```

### Custom Configuration

```bash
# Create custom values file
cat > my-values.yaml << EOF
frontend:
  ingress:
    enabled: true
    hosts:
      - host: my-data-import.example.com
        paths:
          - path: /
            pathType: Prefix

backend:
  env:
    API_BASE_URL: "https://my-api.example.com/data-import"
  secrets:
    KEYCLOAK_AUTH_PASSWORD: "your-secure-password"
    DESTINATION_API_TOKEN: "your-api-token"
EOF

# Install with custom values
helm install my-data-import-service ./helm-chart/data-import-service \
  -f my-values.yaml \
  --namespace my-namespace \
  --create-namespace
```

## Configuration

### Key Configuration Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `mysql.enabled` | Enable MySQL deployment | `true` |
| `mysql.auth.database` | MySQL database name | `data_import_db` |
| `mysql.auth.username` | MySQL username | `import_user` |
| `mysql.auth.password` | MySQL password | `import_password123` |
| `mysql.primary.persistence.size` | MySQL storage size | `20Gi` |
| `backend.enabled` | Enable backend deployment | `true` |
| `backend.replicaCount` | Number of backend replicas | `1` |
| `backend.image.repository` | Backend image repository | `data-import-service` |
| `backend.image.tag` | Backend image tag | `latest` |
| `backend.env.API_BASE_URL` | Backend API base URL | Auto-generated |
| `frontend.enabled` | Enable frontend deployment | `true` |
| `frontend.replicaCount` | Number of frontend replicas | `2` |
| `frontend.image.repository` | Frontend image repository | `data-import-frontend` |
| `frontend.image.tag` | Frontend image tag | `latest` |
| `frontend.env.API_BASE_URL` | Backend URL for frontend | Auto-generated |

### Environment-Specific Values

#### Development (`values-dev.yaml`)
- Smaller resource requests
- Debug logging enabled
- Single replica for backend
- Local ingress hostnames
- Persistence disabled for faster testing

#### Production (`values-prod.yaml`)
- Higher resource limits
- Auto-scaling enabled
- Multiple replicas for high availability
- SSL/TLS configuration
- Network policies enabled
- Pod disruption budgets
- Monitoring enabled

### Secrets Configuration

The chart automatically creates secrets for sensitive data:

```yaml
backend:
  secrets:
    MYSQL_PASSWORD: "your-mysql-password"
    KEYCLOAK_AUTH_PASSWORD: "your-keycloak-password"
    DESTINATION_API_TOKEN: "your-api-token"
```

### Ingress Configuration

#### Frontend Ingress
```yaml
frontend:
  ingress:
    enabled: true
    className: "nginx"
    hosts:
      - host: data-import.yourdomain.com
        paths:
          - path: /
            pathType: Prefix
    tls:
      - secretName: data-import-frontend-tls
        hosts:
          - data-import.yourdomain.com
```

#### Backend Ingress (Optional)
```yaml
backend:
  ingress:
    enabled: true
    className: "nginx"
    hosts:
      - host: api.data-import.yourdomain.com
        paths:
          - path: /
            pathType: Prefix
```

## Upgrading

```bash
# Upgrade with new values
helm upgrade my-data-import-service ./helm-chart/data-import-service \
  -f my-values.yaml

# Upgrade to new version
helm upgrade my-data-import-service ./helm-chart/data-import-service \
  --set backend.image.tag=1.1.0 \
  --set frontend.image.tag=1.1.0
```

## Uninstalling

```bash
# Uninstall the release
helm uninstall my-data-import-service

# Remove persistent volumes (WARNING: This will delete all data)
kubectl delete pvc -l app.kubernetes.io/instance=my-data-import-service
```

## Monitoring

### Health Checks

The chart includes comprehensive health checks:

- **Backend**: HTTP health check on `/data-import/api/health`
- **Frontend**: HTTP health check on `/`
- **MySQL**: MySQL ping command

### Prometheus Monitoring

Enable monitoring with:

```yaml
monitoring:
  enabled: true
  serviceMonitor:
    enabled: true
```

### Logs

Access logs using kubectl:

```bash
# Backend logs
kubectl logs -l app.kubernetes.io/component=backend -f

# Frontend logs
kubectl logs -l app.kubernetes.io/component=frontend -f

# MySQL logs
kubectl logs -l app.kubernetes.io/component=mysql -f
```

## Troubleshooting

### Common Issues

1. **Backend not connecting to MySQL**
   ```bash
   # Check MySQL service
   kubectl get svc -l app.kubernetes.io/component=mysql
   
   # Check MySQL logs
   kubectl logs -l app.kubernetes.io/component=mysql
   ```

2. **Frontend not reaching backend**
   ```bash
   # Check backend service
   kubectl get svc -l app.kubernetes.io/component=backend
   
   # Test backend connectivity
   kubectl exec -it deployment/data-import-service-frontend -- curl http://data-import-service-backend:8080/data-import/api/health
   ```

3. **Ingress not working**
   ```bash
   # Check ingress status
   kubectl get ingress
   
   # Check ingress controller logs
   kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx
   ```

### Debug Commands

```bash
# Get all resources
kubectl get all -l app.kubernetes.io/instance=my-data-import-service

# Describe problematic pods
kubectl describe pod -l app.kubernetes.io/component=backend

# Check events
kubectl get events --sort-by=.metadata.creationTimestamp

# Port forward for local testing
kubectl port-forward svc/data-import-service-frontend 8080:80
kubectl port-forward svc/data-import-service-backend 8081:8080
```

## Security

### Pod Security

The chart implements security best practices:

- Non-root containers
- Read-only root filesystem where possible
- Dropped capabilities
- Security contexts for all components

### Network Security

- Network policies to restrict traffic flow
- Service-to-service communication over cluster network
- External API access controlled

### Secrets Management

- Sensitive data stored in Kubernetes secrets
- Base64 encoding for secret values
- Separate secrets for different components

## Scaling

### Horizontal Pod Autoscaling

Both backend and frontend support HPA:

```yaml
backend:
  autoscaling:
    enabled: true
    minReplicas: 3
    maxReplicas: 10
    targetCPUUtilizationPercentage: 70

frontend:
  autoscaling:
    enabled: true
    minReplicas: 2
    maxReplicas: 5
    targetCPUUtilizationPercentage: 80
```

### Vertical Scaling

Adjust resource requests and limits:

```yaml
backend:
  resources:
    limits:
      memory: 4Gi
      cpu: 2000m
    requests:
      memory: 2Gi
      cpu: 1000m
```

## Backup and Recovery

### MySQL Backup

```bash
# Create backup job
kubectl create job mysql-backup-$(date +%Y%m%d) \
  --from=cronjob/mysql-backup

# Manual backup
kubectl exec -it deployment/data-import-service-mysql -- \
  mysqldump -u root -p data_import_db > backup.sql
```

### Persistent Volume Backup

Follow your cloud provider's documentation for PV snapshots and backups.