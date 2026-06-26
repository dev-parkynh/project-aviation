#!/bin/bash
set -e

EC2_USER=${EC2_USER:-ubuntu}
APP_DIR=${APP_DIR:-/app}

echo "▶ Deploying aviation to $EC2_USER@$EC2_HOST (tag: $IMAGE_TAG)"

# prod compose 파일과 nginx 설정 EC2로 전송
scp -o StrictHostKeyChecking=no \
    docker-compose.prod.yml \
    "$EC2_USER@$EC2_HOST:$APP_DIR/docker-compose.prod.yml"

scp -o StrictHostKeyChecking=no -r \
    nginx \
    "$EC2_USER@$EC2_HOST:$APP_DIR/"

# EC2에서 배포 실행
ssh -o StrictHostKeyChecking=no "$EC2_USER@$EC2_HOST" <<EOF
set -e
cd $APP_DIR

export DOCKERHUB_USERNAME="$DOCKERHUB_USERNAME"
export IMAGE_TAG="$IMAGE_TAG"

echo "Pulling images (tag: $IMAGE_TAG)..."
docker compose -f docker-compose.prod.yml pull

echo "Starting services..."
docker compose -f docker-compose.prod.yml up -d --remove-orphans

echo "Cleaning up old images..."
docker image prune -f

echo "Service status:"
docker compose -f docker-compose.prod.yml ps
EOF

echo "✔ Deploy complete!"
