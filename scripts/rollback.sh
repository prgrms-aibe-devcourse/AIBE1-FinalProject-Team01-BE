#!/bin/bash

set -e

echo "롤백 실행 중..."

if [ -z "$SERVICE_NAME" ]; then
    echo "오류: SERVICE_NAME 환경변수가 설정되지 않았습니다"
    exit 1
fi

TARGET_DIR="/home/ubuntu/docker/${SERVICE_NAME}"
STATE_FILE="${TARGET_DIR}/blue-green-state"

if [ ! -f "$STATE_FILE" ]; then
    echo "상태 파일이 없습니다. 수동 정리가 필요할 수 있습니다."
    exit 0
fi

source "$STATE_FILE"

cd "$TARGET_DIR"

echo "실패한 $STANDBY_ENV 환경 롤백 중..."

echo "$STANDBY_ENV 환경 정리 중 (포트: $STANDBY_PORT)..."
docker-compose -p ${SERVICE_NAME}-${STANDBY_ENV} down || true

rm -f "$STATE_FILE"

echo "롤백 완료: $ACTIVE_ENV 환경이 포트 $ACTIVE_PORT에서 계속 서비스 중입니다"