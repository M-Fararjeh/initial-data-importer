#!/bin/bash

# Kubernetes logs viewing script for Data Import Service
set -e

# Default values
NAMESPACE="data-import"
COMPONENT="all"
FOLLOW=false
LINES=100
RELEASE_NAME="data-import-service"

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

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: $0 [OPTIONS]

View logs from Data Import Service components in Kubernetes

OPTIONS:
    -n, --namespace NAMESPACE     Kubernetes namespace (default: data-import)
    -c, --component COMPONENT     Component: backend, frontend, mysql, or all (default: all)
    -f, --follow                  Follow log output
    -l, --lines LINES             Number of lines to show (default: 100)
    -r, --release RELEASE_NAME    Helm release name (default: data-import-service)
    -h, --help                    Show this help message

EXAMPLES:
    # View all logs
    $0

    # Follow backend logs
    $0 -c backend -f

    # View last 50 lines of frontend logs
    $0 -c frontend -l 50

    # View MySQL logs in specific namespace
    $0 -n data-import-prod -c mysql
EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -c|--component)
            COMPONENT="$2"
            shift 2
            ;;
        -f|--follow)
            FOLLOW=true
            shift
            ;;
        -l|--lines)
            LINES="$2"
            shift 2
            ;;
        -r|--release)
            RELEASE_NAME="$2"
            shift 2
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

# Validate component
if [[ ! "$COMPONENT" =~ ^(backend|frontend|mysql|all)$ ]]; then
    print_error "Invalid component: $COMPONENT. Must be 'backend', 'frontend', 'mysql', or 'all'"
    exit 1
fi

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed or not in PATH"
    exit 1
fi

# Check if connected to Kubernetes cluster
if ! kubectl cluster-info &> /dev/null; then
    print_error "Not connected to a Kubernetes cluster"
    exit 1
fi

# Check if namespace exists
if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
    print_error "Namespace does not exist: $NAMESPACE"
    exit 1
fi

# Build kubectl logs command options
KUBECTL_OPTS="--namespace=$NAMESPACE --tail=$LINES"
if [[ "$FOLLOW" == "true" ]]; then
    KUBECTL_OPTS="$KUBECTL_OPTS -f"
fi

# Function to show logs for a specific component
show_component_logs() {
    local comp=$1
    local selector="app.kubernetes.io/instance=$RELEASE_NAME,app.kubernetes.io/component=$comp"
    
    print_status "Showing logs for $comp component..."
    
    # Check if pods exist for this component
    if ! kubectl get pods -n "$NAMESPACE" -l "$selector" &> /dev/null; then
        print_warning "No pods found for component: $comp"
        return
    fi
    
    # Show pod status first
    echo ""
    print_status "Pod status for $comp:"
    kubectl get pods -n "$NAMESPACE" -l "$selector"
    
    echo ""
    print_status "Logs for $comp:"
    kubectl logs $KUBECTL_OPTS -l "$selector"
}

# Show logs based on component selection
case $COMPONENT in
    backend)
        show_component_logs "backend"
        ;;
    frontend)
        show_component_logs "frontend"
        ;;
    mysql)
        show_component_logs "mysql"
        ;;
    all)
        print_status "Showing logs for all components..."
        echo ""
        
        # Show overall pod status
        print_status "All pods status:"
        kubectl get pods -n "$NAMESPACE" -l "app.kubernetes.io/instance=$RELEASE_NAME"
        
        echo ""
        show_component_logs "backend"
        echo ""
        show_component_logs "frontend"
        echo ""
        show_component_logs "mysql"
        ;;
esac

print_success "Log viewing completed"