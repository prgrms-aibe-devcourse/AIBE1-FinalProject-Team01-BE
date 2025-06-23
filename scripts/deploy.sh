#!/bin/bash
set -e

SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}
APP_PORT=${APP_PORT:-8080}
CONTAINER_NAME="app"

if [ -z "$REGISTRY" ] || [ -z "$IMAGE_NAME" ] || [ -z "$IMAGE_TAG" ]; then
    echo "오류: REGISTRY, IMAGE_NAME, IMAGE_TAG 환경변수가 설정되지 않았습니다"
    exit 1
fi

TARGET_IMAGE=$(echo "${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}" | tr '[:upper:]' '[:lower:]')

echo "배포 시작: $TARGET_IMAGE (포트: $APP_PORT)"

# 포트 사용하는 컨테이너 중지 & 제거
docker ps -q --filter "publish=${APP_PORT}" | xargs -r docker stop
docker ps -aq --filter "publish=${APP_PORT}" | xargs -r docker rm

# 같은 이름의 컨테이너가 있다면 제거
docker rm -f "$CONTAINER_NAME" 2>/dev/null || true

# GHCR 로그인
if [ -n "$GITHUB_TOKEN" ]; then
    echo "GHCR 로그인 중..."
    echo "$GITHUB_TOKEN" | docker login "$REGISTRY" -u "$GITHUB_ACTOR" --password-stdin
else
    echo "경고: GITHUB_TOKEN이 없어 public 이미지만 pull 가능합니다"
    exit 1
fi

# 이미지 pull
echo "이미지 pull 중: $TARGET_IMAGE"
docker pull "$TARGET_IMAGE"

# 새 컨테이너 실행
docker run -d --name "$CONTAINER_NAME" -p "${APP_PORT}:${APP_PORT}" --restart unless-stopped "$TARGET_IMAGE"

echo "배포 완료!"
