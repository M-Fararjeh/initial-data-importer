#!/bin/bash

# Kubernetes deployment script for Data Import Service
set -e

# Default values
NAMESPACE="data-import"
RELEASE_NAME="data-import-service"
ENVIRONMENT="dev"
CHART_PATH="./helm-chart/data-import-service"
VALUES_FILE=""
DRY_RUN=false
UPGRADE=false

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

Deploy Data Import Service to Kubernetes using Helm

OPTIONS:
    -n, --namespace NAMESPACE     Kubernetes namespace (default: data-import)
    -r, --release RELEASE_NAME    Helm release name (default: data-import-service)
    -e, --environment ENV         Environment: dev, prod, or custom (default: dev)
    -f, --values-file FILE        Custom values file path
    -d, --dry-run                 Perform a dry run without installing
    -u, --upgrade                 Upgrade existing installation
    -h, --help                    Show this help message

EXAMPLES:
    # Deploy development environment
    $0 -e dev

    # Deploy production environment
    $0 -e prod -n data-import-prod -r data-import-prod

    # Deploy with custom values
    $0 -f my-custom-values.yaml

    # Dry run for production
    $0 -e prod --dry-run

    # Upgrade existing deployment
    $0 --upgrade -e prod
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
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -f|--values-file)
            VALUES_FILE="$2"
            shift 2
            ;;
        -d|--dry-run)
            DRY_RUN=true
            shift
            ;;
        -u|--upgrade)
            UPGRADE=true
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

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(dev|prod|custom)$ ]]; then
    print_error "Invalid environment: $ENVIRONMENT. Must be 'dev', 'prod', or 'custom'"
    exit 1
fi

# Set values file based on environment if not provided
if [[ -z "$VALUES_FILE" ]]; then
    case $ENVIRONMENT in
        dev)
            VALUES_FILE="$CHART_PATH/values-dev.yaml"
            ;;
        prod)
            VALUES_FILE="$CHART_PATH/values-prod.yaml"
            ;;
        custom)
            print_warning "Custom environment specified but no values file provided. Using default values."
            VALUES_FILE=""
            ;;
    esac
fi

# Validate chart path
if [[ ! -d "$CHART_PATH" ]]; then
    print_error "Chart path not found: $CHART_PATH"
    exit 1
fi

# Validate values file if specified
if [[ -n "$VALUES_FILE" && ! -f "$VALUES_FILE" ]]; then
    print_error "Values file not found: $VALUES_FILE"
    exit 1
fi

print_status "Starting deployment with the following configuration:"
echo "  Namespace: $NAMESPACE"
echo "  Release Name: $RELEASE_NAME"
echo "  Environment: $ENVIRONMENT"
echo "  Chart Path: $CHART_PATH"
echo "  Values File: ${VALUES_FILE:-"default values"}"
echo "  Dry Run: $DRY_RUN"
echo "  Upgrade: $UPGRADE"
echo ""

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

print_success "Prerequisites check passed"

# Create namespace if it doesn't exist
if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
    print_status "Creating namespace: $NAMESPACE"
    if [[ "$DRY_RUN" == "false" ]]; then
        kubectl create namespace "$NAMESPACE"
        print_success "Namespace created: $NAMESPACE"
    else
        print_status "DRY RUN: Would create namespace: $NAMESPACE"
    fi
else
    print_status "Namespace already exists: $NAMESPACE"
fi

# Prepare Helm command
HELM_CMD="helm"
if [[ "$UPGRADE" == "true" ]]; then
    HELM_CMD="$HELM_CMD upgrade"
else
    HELM_CMD="$HELM_CMD install"
fi

HELM_CMD="$HELM_CMD $RELEASE_NAME $CHART_PATH"
HELM_CMD="$HELM_CMD --namespace $NAMESPACE"

if [[ -n "$VALUES_FILE" ]]; then
    HELM_CMD="$HELM_CMD -f $VALUES_FILE"
fi

if [[ "$DRY_RUN" == "true" ]]; then
    HELM_CMD="$HELM_CMD --dry-run --debug"
fi

if [[ "$UPGRADE" == "false" ]]; then
    HELM_CMD="$HELM_CMD --create-namespace"
fi

# Execute Helm command
print_status "Executing Helm command:"
echo "  $HELM_CMD"
echo ""

if eval "$HELM_CMD"; then
    if [[ "$DRY_RUN" == "true" ]]; then
        print_success "Dry run completed successfully"
    elif [[ "$UPGRADE" == "true" ]]; then
        print_success "Upgrade completed successfully"
    else
        print_success "Installation completed successfully"
    fi
else
    print_error "Helm command failed"
    exit 1
fi

# Show deployment status (only if not dry run)
if [[ "$DRY_RUN" == "false" ]]; then
    echo ""
    print_status "Deployment Status:"
    
    # Wait a moment for resources to be created
    sleep 5
    
    # Show pods status
    echo ""
    print_status "Pods:"
    kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/instance="$RELEASE_NAME"
    
    # Show services
    echo ""
    print_status "Services:"
    kubectl get svc -n "$NAMESPACE" -l app.kubernetes.io/instance="$RELEASE_NAME"
    
    # Show ingress if enabled
    if kubectl get ingress -n "$NAMESPACE" -l app.kubernetes.io/instance="$RELEASE_NAME" &> /dev/null; then
        echo ""
        print_status "Ingress:"
        kubectl get ingress -n "$NAMESPACE" -l app.kubernetes.io/instance="$RELEASE_NAME"
    fi
    
    # Show notes
    echo ""
    print_status "Getting deployment notes..."
    helm get notes "$RELEASE_NAME" -n "$NAMESPACE"
    
    echo ""
    print_success "Deployment information displayed above"
    print_status "Monitor the deployment with: kubectl get pods -n $NAMESPACE -w"
fi