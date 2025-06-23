#!/bin/bash

set -e

ENV_FILE="${ENV_FILE_ROOT:-/home/ubuntu/spring/.env}"

echo "=== 배포 시작 ==="

echo "환경변수 설정 중..."
if [ -n "$SECRETS_JSON" ]; then
    echo "$SECRETS_JSON" | jq -r '
        to_entries[] |
        select(.key | test("^(EC2_|GITHUB_|github_|N8N_)") | not) |
        "\(.key)=\(.value)"
    ' > "$ENV_FILE"

    echo "환경변수 설정 완료"
else
    echo "SECRETS_JSON이 없습니다."
    exit 1
fi

if [[ -z "$IMAGE_TAG" || -z "$REGISTRY" || -z "$IMAGE_NAME" ]]; then
    echo "필수 환경변수가 설정되지 않았습니다."
    exit 1
fi

TARGET_IMAGE=$(echo "${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}" | tr '[:upper:]' '[:lower:]')

echo "Docker 로그인 중..."
echo "${GITHUB_TOKEN}" | docker login ghcr.io -u "${GITHUB_ACTOR}" --password-stdin

docker pull "${TARGET_IMAGE}"

echo "기존 컨테이너 정리 중..."
docker-compose -f /home/ubuntu/spring/docker-compose.yml down || true

cd /home/ubuntu/spring

echo "애플리케이션 시작 중..."
docker-compose up -d

echo "사용하지 않는 Docker 이미지 정리 중..."
docker image prune -f

echo "=== 배포 완료 ==="