#!/bin/bash

set -e

echo "트래픽 전환 중..."

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

echo "$ACTIVE_ENV($ACTIVE_PORT) -> $STANDBY_ENV($STANDBY_PORT) 전환 중..."

echo "NGINX 설정 테스트 중..."
if ! sudo nginx -t; then
    echo "NGINX 설정 테스트 실패"
    exit 1
fi

echo "NGINX 재시작 중..."
sudo systemctl reload nginx

echo "트래픽 전환 완료"