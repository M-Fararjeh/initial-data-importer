#!/bin/bash

# Build Docker images for Kubernetes deployment
set -e

# Default values
REGISTRY=""
TAG="latest"
PUSH=false
BUILD_BACKEND=true
BUILD_FRONTEND=true

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

Build Docker images for Data Import Service Kubernetes deployment

OPTIONS:
    -r, --registry REGISTRY       Docker registry URL (e.g., your-registry.com/)
    -t, --tag TAG                 Image tag (default: latest)
    -p, --push                    Push images to registry after building
    --backend-only                Build only backend image
    --frontend-only               Build only frontend image
    -h, --help                    Show this help message

EXAMPLES:
    # Build both images with default tag
    $0

    # Build and push to registry
    $0 -r your-registry.com/ -t v1.0.0 --push

    # Build only backend
    $0 --backend-only -t dev

    # Build for development
    $0 -t dev
EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -r|--registry)
            REGISTRY="$2"
            shift 2
            ;;
        -t|--tag)
            TAG="$2"
            shift 2
            ;;
        -p|--push)
            PUSH=true
            shift
            ;;
        --backend-only)
            BUILD_FRONTEND=false
            shift
            ;;
        --frontend-only)
            BUILD_BACKEND=false
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

# Validate Docker is available
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not in PATH"
    exit 1
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running"
    exit 1
fi

print_status "Starting image build process..."
echo "  Registry: ${REGISTRY:-"local"}"
echo "  Tag: $TAG"
echo "  Push to registry: $PUSH"
echo "  Build backend: $BUILD_BACKEND"
echo "  Build frontend: $BUILD_FRONTEND"
echo ""

# Build backend image
if [[ "$BUILD_BACKEND" == "true" ]]; then
    BACKEND_IMAGE="${REGISTRY}data-import-service:${TAG}"
    
    print_status "Building backend image: $BACKEND_IMAGE"
    
    if docker build -t "$BACKEND_IMAGE" .; then
        print_success "Backend image built successfully: $BACKEND_IMAGE"
        
        if [[ "$PUSH" == "true" ]]; then
            print_status "Pushing backend image to registry..."
            if docker push "$BACKEND_IMAGE"; then
                print_success "Backend image pushed successfully"
            else
                print_error "Failed to push backend image"
                exit 1
            fi
        fi
    else
        print_error "Failed to build backend image"
        exit 1
    fi
fi

# Build frontend image
if [[ "$BUILD_FRONTEND" == "true" ]]; then
    FRONTEND_IMAGE="${REGISTRY}data-import-frontend:${TAG}"
    
    print_status "Building frontend image: $FRONTEND_IMAGE"
    
    if docker build -t "$FRONTEND_IMAGE" ./angular-migration-ui/; then
        print_success "Frontend image built successfully: $FRONTEND_IMAGE"
        
        if [[ "$PUSH" == "true" ]]; then
            print_status "Pushing frontend image to registry..."
            if docker push "$FRONTEND_IMAGE"; then
                print_success "Frontend image pushed successfully"
            else
                print_error "Failed to push frontend image"
                exit 1
            fi
        fi
    else
        print_error "Failed to build frontend image"
        exit 1
    fi
fi

print_success "Image build process completed!"

# Show built images
echo ""
print_status "Built images:"
if [[ "$BUILD_BACKEND" == "true" ]]; then
    echo "  Backend: ${REGISTRY}data-import-service:${TAG}"
fi
if [[ "$BUILD_FRONTEND" == "true" ]]; then
    echo "  Frontend: ${REGISTRY}data-import-frontend:${TAG}"
fi

# Show next steps
echo ""
print_status "Next steps:"
if [[ "$PUSH" == "false" ]]; then
    echo "  1. Push images to registry:"
    if [[ "$BUILD_BACKEND" == "true" ]]; then
        echo "     docker push ${REGISTRY}data-import-service:${TAG}"
    fi
    if [[ "$BUILD_FRONTEND" == "true" ]]; then
        echo "     docker push ${REGISTRY}data-import-frontend:${TAG}"
    fi
fi

echo "  2. Deploy to Kubernetes:"
echo "     ./scripts/deploy-k8s.sh -e $ENVIRONMENT -t $TAG"

if [[ -n "$REGISTRY" ]]; then
    echo "  3. Update values file with registry:"
    echo "     global:"
    echo "       imageRegistry: \"$REGISTRY\""
fi