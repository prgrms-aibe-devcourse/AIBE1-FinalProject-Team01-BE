#!/bin/bash

set -e

if [ -z "$SERVICE_NAME" ]; then
    echo "오류: SERVICE_NAME 환경변수가 설정되지 않았습니다"
    exit 1
fi

TARGET_DIR="/home/ubuntu/docker/${SERVICE_NAME}"
STATE_FILE="${TARGET_DIR}/blue-green-state"

if [ ! -f "$STATE_FILE" ]; then
    echo "오류: 배포 상태 파일을 찾을 수 없습니다"
    exit 1
fi

source "$STATE_FILE"

cd "$TARGET_DIR"

echo "기존 $ACTIVE_ENV 환경 정리 중 (포트: $ACTIVE_PORT)..."
docker-compose -p ${SERVICE_NAME}-${ACTIVE_ENV} down || true

echo "사용하지 않는 Docker 이미지 정리 중..."
docker image prune -f

rm -f "$STATE_FILE"

echo "$STANDBY_ENV 환경이 포트 $STANDBY_PORT에서 활성화됨"