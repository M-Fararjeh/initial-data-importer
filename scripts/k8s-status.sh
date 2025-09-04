#!/bin/bash

# Kubernetes status checking script for Data Import Service
set -e

# Default values
NAMESPACE="data-import"
RELEASE_NAME="data-import-service"
WATCH=false

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Check status of Data Import Service deployment in Kubernetes

OPTIONS:
    -n, --namespace NAMESPACE     Kubernetes namespace (default: data-import)
    -r, --release RELEASE_NAME    Helm release name (default: data-import-service)
    -w, --watch                   Watch for changes
    -h, --help                    Show this help message

EXAMPLES:
    # Check status
    $0

    # Check status in specific namespace
    $0 -n data-import-prod

    # Watch for changes
    $0 --watch
EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -r|--release)
            RELEASE_NAME="$2"
            shift 2
            ;;
        -w|--watch)
            WATCH=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed or not in PATH"
    exit 1
fi

# Check if helm is available
if ! command -v helm &> /dev/null; then
    print_error "helm is not installed or not in PATH"
    exit 1
fi

# Check if connected to Kubernetes cluster
if ! kubectl cluster-info &> /dev/null; then
    print_error "Not connected to a Kubernetes cluster"
    exit 1
fi

# Function to show status
show_status() {
    clear
    echo "======================================"
    echo "Data Import Service - Kubernetes Status"
    echo "======================================"
    echo "Namespace: $NAMESPACE"
    echo "Release: $RELEASE_NAME"
    echo "Time: $(date)"
    echo "======================================"
    echo ""
    
    # Check if namespace exists
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        print_error "Namespace does not exist: $NAMESPACE"
        return 1
    fi
    
    # Helm release status
    print_status "Helm Release Status:"
    if helm status "$RELEASE_NAME" -n "$NAMESPACE" &> /dev/null; then
        helm status "$RELEASE_NAME" -n "$NAMESPACE" --show-desc
    else
        print_warning "Helm release not found: $RELEASE_NAME"
    fi
    
    echo ""
    
    # Pods status
    print_status "Pods Status:"
    kubectl get pods -n "$NAMESPACE" -l "app.kubernetes.io/instance=$RELEASE_NAME" -o wide
    
    echo ""
    
    # Services status
    print_status "Services Status:"
    kubectl get svc -n "$NAMESPACE" -l "app.kubernetes.io/instance=$RELEASE_NAME"
    
    echo ""
    
    # Ingress status
    if kubectl get ingress -n "$NAMESPACE" -l "app.kubernetes.io/instance=$RELEASE_NAME" &> /dev/null; then
        print_status "Ingress Status:"
        kubectl get ingress -n "$NAMESPACE" -l "app.kubernetes.io/instance=$RELEASE_NAME"
        echo ""
    fi
    
    # PVC status
    if kubectl get pvc -n "$NAMESPACE" -l "app.kubernetes.io/instance=$RELEASE_NAME" &> /dev/null; then
        print_status "Persistent Volume Claims:"
        kubectl get pvc -n "$NAMESPACE" -l "app.kubernetes.io/instance=$RELEASE_NAME"
        echo ""
    fi
    
    # ConfigMaps and Secrets
    print_status "ConfigMaps:"
    kubectl get configmap -n "$NAMESPACE" -l "app.kubernetes.io/instance=$RELEASE_NAME"
    
    echo ""
    print_status "Secrets:"
    kubectl get secret -n "$NAMESPACE" -l "app.kubernetes.io/instance=$RELEASE_NAME"
    
    echo ""
    
    # Recent events
    print_status "Recent Events:"
    kubectl get events -n "$NAMESPACE" --sort-by=.metadata.creationTimestamp --field-selector involvedObject.kind!=Event | tail -10
    
    echo ""
    
    # Health check
    print_status "Health Check:"
    
    # Check backend health
    BACKEND_POD=$(kubectl get pods -n "$NAMESPACE" -l "app.kubernetes.io/component=backend" -o jsonpath="{.items[0].metadata.name}" 2>/dev/null || echo "")
    if [[ -n "$BACKEND_POD" ]]; then
        if kubectl exec -n "$NAMESPACE" "$BACKEND_POD" -- curl -f http://localhost:8080/data-import/api/health &> /dev/null; then
            print_success "Backend health check: PASSED"
        else
            print_warning "Backend health check: FAILED"
        fi
    else
        print_warning "Backend pod not found"
    fi
    
    # Check frontend health
    FRONTEND_POD=$(kubectl get pods -n "$NAMESPACE" -l "app.kubernetes.io/component=frontend" -o jsonpath="{.items[0].metadata.name}" 2>/dev/null || echo "")
    if [[ -n "$FRONTEND_POD" ]]; then
        if kubectl exec -n "$NAMESPACE" "$FRONTEND_POD" -- curl -f http://localhost/ &> /dev/null; then
            print_success "Frontend health check: PASSED"
        else
            print_warning "Frontend health check: FAILED"
        fi
    else
        print_warning "Frontend pod not found"
    fi
    
    # Check MySQL health
    MYSQL_POD=$(kubectl get pods -n "$NAMESPACE" -l "app.kubernetes.io/component=mysql" -o jsonpath="{.items[0].metadata.name}" 2>/dev/null || echo "")
    if [[ -n "$MYSQL_POD" ]]; then
        if kubectl exec -n "$NAMESPACE" "$MYSQL_POD" -- mysqladmin ping -h 127.0.0.1 &> /dev/null; then
            print_success "MySQL health check: PASSED"
        else
            print_warning "MySQL health check: FAILED"
        fi
    else
        print_warning "MySQL pod not found"
    fi
    
    echo ""
    print_status "Use the following commands for more details:"
    echo "  kubectl describe pods -n $NAMESPACE -l app.kubernetes.io/instance=$RELEASE_NAME"
    echo "  kubectl logs -n $NAMESPACE -l app.kubernetes.io/component=backend -f"
    echo "  kubectl logs -n $NAMESPACE -l app.kubernetes.io/component=frontend -f"
    echo "  kubectl logs -n $NAMESPACE -l app.kubernetes.io/component=mysql -f"
}

# Show status
if [[ "$WATCH" == "true" ]]; then
    print_status "Watching status (Press Ctrl+C to exit)..."
    while true; do
        show_status
        sleep 10
    done
else
    show_status
fi