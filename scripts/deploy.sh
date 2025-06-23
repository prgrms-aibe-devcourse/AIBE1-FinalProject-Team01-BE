#!/bin/bash

set -e

SERVICE_NAME=${SERVICE_NAME:-spring-prod}
TARGET_DIR="/home/ubuntu/docker/${SERVICE_NAME}"

echo "=== 배포 시작: ${SERVICE_NAME} ==="

# 디렉토리 존재 확인
if [ ! -d "$TARGET_DIR" ]; then
    echo "오류: 대상 디렉토리가 존재하지 않습니다: $TARGET_DIR"
    exit 1
fi

cd "$TARGET_DIR"

echo "GHCR 로그인 중..."
echo "$GITHUB_TOKEN" | docker login ghcr.io -u "$GITHUB_ACTOR" --password-stdin

echo "기존 컨테이너 정리 중..."
docker-compose down

echo "애플리케이션 시작 중..."
docker-compose up -d --pull always

echo "사용하지 않는 Docker 이미지 정리 중..."
docker image prune -f

echo "배포 완료: ${SERVICE_NAME}"